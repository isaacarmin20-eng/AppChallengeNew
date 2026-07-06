package com.example.mediscreen.ui.navigation

import com.example.mediscreen.data.model.ResultPayload

sealed interface AppScreen {
    data object Home : AppScreen
    data class Questionnaire(val conditionId: String) : AppScreen
    data class Result(val payload: ResultPayload) : AppScreen
}
