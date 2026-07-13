package com.example.mediscreen.data.model

import androidx.compose.ui.graphics.Color

data class ConditionQuestionnaire(
    val conditionId: String,
    val displayName: String,
    val protocolBasis: String,
    val questions: List<SymptomQuestion>,
    val urgentIfAnyOf: List<String> = emptyList(),
    val urgentThreshold: Int,
    val urgentInstructions: List<String>,
    val nonUrgentInstructions: List<String>,
    val footnote: String? = null,
    val themeColor: Color = Color(0xFF087EA4),
    val waitingTip: String? = null
)