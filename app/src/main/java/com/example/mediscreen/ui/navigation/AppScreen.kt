package com.example.mediscreen.ui.navigation

import com.example.mediscreen.data.model.ResultPayload
import java.io.Serializable

sealed interface AppScreen : Serializable {

    data object Home : AppScreen

    data class Questionnaire(
        val conditionId: String
    ) : AppScreen

    data class Camera(
        val conditionId: String
    ) : AppScreen

    data class Result(
        val payload: ResultPayload
    ) : AppScreen
}
