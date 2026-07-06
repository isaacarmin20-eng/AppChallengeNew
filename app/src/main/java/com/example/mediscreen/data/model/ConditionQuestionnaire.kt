package com.example.mediscreen.data.model

data class ConditionQuestionnaire(
    val conditionId: String,
    val displayName: String,
    val protocolBasis: String,
    val questions: List<SymptomQuestion>,
    val urgentIfAnyOf: List<String> = emptyList(),
    val urgentThreshold: Int,
    val urgentInstructions: List<String>,
    val nonUrgentInstructions: List<String>,
    val footnote: String? = null
)
