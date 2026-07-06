package com.example.mediscreen.data.model

enum class QuestionType {
    YesNo,
    MultiSelect,
    SingleSelect
}

data class SymptomQuestion(
    val id: String,
    val prompt: String,
    val type: QuestionType = QuestionType.YesNo,
    val options: List<String> = emptyList(),
    val weight: Int = 1
)
