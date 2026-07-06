package com.example.mediscreen.domain

import com.example.mediscreen.data.model.ConditionQuestionnaire
import com.example.mediscreen.data.model.QuestionAnswer
import com.example.mediscreen.data.model.QuestionType
import com.example.mediscreen.data.model.ResultPayload
import com.example.mediscreen.data.model.SymptomQuestion

private const val SEEK_CARE_MESSAGE =
    "This screening is not a diagnosis. If symptoms persist, worsen, or you are unsure, seek medical care or call 911."

fun evaluateQuestionnaire(
    condition: ConditionQuestionnaire,
    answers: Map<String, QuestionAnswer>
): ResultPayload {
    if (condition.urgentIfAnyOf.any { questionId -> isUrgentOverride(questionId, answers, condition) }) {
        return buildResult(condition, urgent = true)
    }

    val score = condition.questions.sumOf { question ->
        if (isConcerningAnswer(question, answers[question.id])) question.weight else 0
    }

    return buildResult(condition, urgent = score >= condition.urgentThreshold)
}

private fun isUrgentOverride(
    questionId: String,
    answers: Map<String, QuestionAnswer>,
    condition: ConditionQuestionnaire
): Boolean {
    val question = condition.questions.find { it.id == questionId } ?: return false
    return isConcerningAnswer(question, answers[questionId])
}

fun isConcerningAnswer(question: SymptomQuestion, answer: QuestionAnswer?): Boolean {
    if (answer == null) return false

    return when (question.type) {
        QuestionType.YesNo -> (answer as? QuestionAnswer.YesNo)?.yes == true
        QuestionType.SingleSelect -> {
            val selected = (answer as? QuestionAnswer.SingleSelect)?.selected ?: return false
            selected.isNotBlank() && !selected.equals("No", ignoreCase = true)
        }
        QuestionType.MultiSelect -> {
            val selected = (answer as? QuestionAnswer.MultiSelect)?.selected ?: return false
            selected.isNotEmpty() &&
                selected.none { it.equals("None of the above", ignoreCase = true) }
        }
    }
}

private fun buildResult(condition: ConditionQuestionnaire, urgent: Boolean): ResultPayload {
    return ResultPayload(
        urgent = urgent,
        conditionId = condition.conditionId,
        displayName = condition.displayName,
        instructions = if (urgent) condition.urgentInstructions else condition.nonUrgentInstructions,
        seekCareMessage = SEEK_CARE_MESSAGE
    )
}
