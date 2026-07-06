package com.example.mediscreen.data.model

sealed class QuestionAnswer {
    data class YesNo(val yes: Boolean) : QuestionAnswer()
    data class SingleSelect(val selected: String) : QuestionAnswer()
    data class MultiSelect(val selected: Set<String>) : QuestionAnswer()
}
