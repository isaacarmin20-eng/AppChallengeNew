package com.example.mediscreen.domain

import com.example.mediscreen.data.ConditionCatalog
import com.example.mediscreen.data.model.QuestionAnswer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateQuestionnaireTest {

    @Test
    fun heartAttack_nonUrgent_whenOnlyOneSymptomReported() {
        val condition = ConditionCatalog.heartAttack
        val answers = mapOf(
            "chest_pain" to QuestionAnswer.YesNo(true),
            "radiating_pain" to QuestionAnswer.YesNo(false),
            "shortness_of_breath" to QuestionAnswer.YesNo(false),
            "nausea" to QuestionAnswer.YesNo(false)
        )

        val result = evaluateQuestionnaire(condition, answers)

        assertFalse(result.urgent)
        assertTrue(result.instructions.isNotEmpty())
        assertTrue(result.seekCareMessage.contains("not a diagnosis", ignoreCase = true))
    }

    @Test
    fun heartAttack_urgent_whenThresholdMet() {
        val condition = ConditionCatalog.heartAttack
        val answers = mapOf(
            "chest_pain" to QuestionAnswer.YesNo(true),
            "radiating_pain" to QuestionAnswer.YesNo(true),
            "shortness_of_breath" to QuestionAnswer.YesNo(false),
            "nausea" to QuestionAnswer.YesNo(false)
        )

        val result = evaluateQuestionnaire(condition, answers)

        assertTrue(result.urgent)
        assertTrue(result.instructions.any { it.contains("911", ignoreCase = true) })
    }

    @Test
    fun choking_urgent_whenAnySignPresent() {
        val condition = ConditionCatalog.choking
        val answers = mapOf(
            "cant_speak" to QuestionAnswer.YesNo(false),
            "clutching_throat" to QuestionAnswer.YesNo(true)
        )

        val result = evaluateQuestionnaire(condition, answers)

        assertTrue(result.urgent)
    }

    @Test
    fun choking_nonUrgent_whenNoSignsReported() {
        val condition = ConditionCatalog.choking
        val answers = mapOf(
            "cant_speak" to QuestionAnswer.YesNo(false),
            "clutching_throat" to QuestionAnswer.YesNo(false)
        )

        val result = evaluateQuestionnaire(condition, answers)

        assertFalse(result.urgent)
        assertTrue(result.instructions.any { it.contains("worsen", ignoreCase = true) })
    }

    @Test
    fun poisoning_urgentImmediately_whenUnresponsive() {
        val condition = ConditionCatalog.poisoning
        val answers = mapOf(
            "unresponsive" to QuestionAnswer.YesNo(true),
            "vomiting" to QuestionAnswer.YesNo(false),
            "confusion" to QuestionAnswer.YesNo(false),
            "known_exposure" to QuestionAnswer.YesNo(false)
        )

        val result = evaluateQuestionnaire(condition, answers)

        assertTrue(result.urgent)
    }

    @Test
    fun poisoning_nonUrgent_whenBelowThresholdAndNoOverride() {
        val condition = ConditionCatalog.poisoning
        val answers = mapOf(
            "unresponsive" to QuestionAnswer.YesNo(false),
            "vomiting" to QuestionAnswer.YesNo(true),
            "confusion" to QuestionAnswer.YesNo(false),
            "known_exposure" to QuestionAnswer.YesNo(false)
        )

        val result = evaluateQuestionnaire(condition, answers)

        assertFalse(result.urgent)
    }

    @Test
    fun poisoning_urgent_whenThresholdMetWithoutOverride() {
        val condition = ConditionCatalog.poisoning
        val answers = mapOf(
            "unresponsive" to QuestionAnswer.YesNo(false),
            "vomiting" to QuestionAnswer.YesNo(true),
            "confusion" to QuestionAnswer.YesNo(true),
            "known_exposure" to QuestionAnswer.YesNo(false)
        )

        val result = evaluateQuestionnaire(condition, answers)

        assertTrue(result.urgent)
    }
}
