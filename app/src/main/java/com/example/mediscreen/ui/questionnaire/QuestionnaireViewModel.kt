package com.example.mediscreen.ui.questionnaire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mediscreen.data.model.ConditionQuestionnaire
import com.example.mediscreen.data.model.QuestionAnswer
import com.example.mediscreen.data.model.QuestionType
import com.example.mediscreen.data.model.ResultPayload
import com.example.mediscreen.domain.evaluateQuestionnaire
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class QuestionnaireUiState(
    val condition: ConditionQuestionnaire,
    val currentIndex: Int = 0,
    val answers: Map<String, QuestionAnswer> = emptyMap()
) {
    val currentQuestion get() = condition.questions[currentIndex]
    val totalQuestions get() = condition.questions.size
    val progressText get() = "Question ${currentIndex + 1} of $totalQuestions"
    val isLastQuestion get() = currentIndex == condition.questions.lastIndex
    val canGoBack get() = currentIndex > 0
    val currentAnswer get() = answers[currentQuestion.id]
    val isCurrentQuestionAnswered get() = currentAnswer != null
}

class QuestionnaireViewModel(
    private val condition: ConditionQuestionnaire
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionnaireUiState(condition = condition))
    val uiState: StateFlow<QuestionnaireUiState> = _uiState.asStateFlow()

    fun answerCurrentQuestion(answer: QuestionAnswer) {
        val state = _uiState.value
        val updatedAnswers = state.answers + (state.currentQuestion.id to answer)
        _uiState.update {
            it.copy(
                answers = updatedAnswers,
                currentIndex = if (it.isLastQuestion) it.currentIndex else it.currentIndex + 1
            )
        }
    }

    fun toggleMultiSelectOption(option: String) {
        val state = _uiState.value
        val question = state.currentQuestion
        require(question.type == QuestionType.MultiSelect)

        val current = (state.currentAnswer as? QuestionAnswer.MultiSelect)?.selected.orEmpty().toMutableSet()
        if (option.equals("None of the above", ignoreCase = true)) {
            current.clear()
            current.add(option)
        } else {
            current.removeAll { it.equals("None of the above", ignoreCase = true) }
            if (option in current) current.remove(option) else current.add(option)
        }

        _uiState.update {
            it.copy(answers = it.answers + (question.id to QuestionAnswer.MultiSelect(current)))
        }
    }

    fun confirmMultiSelectAndAdvance() {
        val state = _uiState.value
        if (state.currentAnswer == null) return
        if (state.isLastQuestion) return

        _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
    }

    fun goBack() {
        _uiState.update { state ->
            if (!state.canGoBack) state else state.copy(currentIndex = state.currentIndex - 1)
        }
    }

    fun evaluate(): ResultPayload = evaluateQuestionnaire(condition, _uiState.value.answers)

    companion object {
        fun factory(condition: ConditionQuestionnaire): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return QuestionnaireViewModel(condition) as T
                }
            }
        }
    }
}
