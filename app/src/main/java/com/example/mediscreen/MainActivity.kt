package com.example.mediscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mediscreen.data.ConditionCatalog
import com.example.mediscreen.ui.HomeScreen
import com.example.mediscreen.ui.navigation.AppScreen
import com.example.mediscreen.ui.questionnaire.QuestionnaireScreen
import com.example.mediscreen.ui.result.ResultScreen
import com.example.mediscreen.ui.theme.MediScreenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediScreenTheme {
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

                when (val screen = currentScreen) {
                    AppScreen.Home -> {
                        HomeScreen(
                            onCategorySelected = { conditionId ->
                                currentScreen = AppScreen.Questionnaire(conditionId)
                            }
                        )
                    }

                    is AppScreen.Questionnaire -> {
                        val condition = ConditionCatalog.findById(screen.conditionId)
                        if (condition != null) {
                            QuestionnaireScreen(
                                condition = condition,
                                onBack = { currentScreen = AppScreen.Home },
                                onComplete = { payload ->
                                    currentScreen = AppScreen.Result(payload)
                                }
                            )
                        } else {
                            currentScreen = AppScreen.Home
                        }
                    }

                    is AppScreen.Result -> {
                        ResultScreen(
                            payload = screen.payload,
                            onBack = { currentScreen = AppScreen.Home }
                        )
                    }
                }
            }
        }
    }
}
