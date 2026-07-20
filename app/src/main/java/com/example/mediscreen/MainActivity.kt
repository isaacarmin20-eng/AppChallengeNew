package com.example.mediscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.mediscreen.data.ConditionCatalog
import com.example.mediscreen.ui.HomeScreen
// Uncomment after CameraScreen is created
import com.example.mediscreen.ui.camera.CameraScreen
import com.example.mediscreen.ui.navigation.AppScreen
import com.example.mediscreen.ui.questionnaire.QuestionnaireScreen
import com.example.mediscreen.ui.result.ResultScreen
import com.example.mediscreen.ui.theme.MediScreenTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MediScreenTheme {

                var currentScreen by rememberSaveable {
                    mutableStateOf<AppScreen>(AppScreen.Home)
                }

                when (val screen = currentScreen) {

                    AppScreen.Home -> {

                        HomeScreen(
                            onCategorySelected = { conditionId ->

                                currentScreen = if (conditionId == "stroke") {
                                    AppScreen.Camera(conditionId)
                                } else {
                                    AppScreen.Questionnaire(conditionId)
                                }
                            }
                        )
                    }

                    is AppScreen.Questionnaire -> {

                        val condition =
                            ConditionCatalog.findById(screen.conditionId)

                        if (condition == null) {

                            currentScreen = AppScreen.Home

                        } else {

                            QuestionnaireScreen(
                                condition = condition,

                                onBack = {
                                    currentScreen = AppScreen.Home
                                },

                                onComplete = { payload ->
                                    currentScreen =
                                        AppScreen.Result(payload)
                                }
                            )
                        }
                    }

                    /*
                    ===========================================================
                    PHASE B (Enable after CameraScreen exists)

                    is AppScreen.Camera -> {

                        CameraScreen(
                            conditionId = screen.conditionId,

                            onNavigateBack = {
                                currentScreen = AppScreen.Home
                            },

                            onAnalysisReady = { payload ->
                                currentScreen =
                                    AppScreen.Result(payload)
                            }
                        )
                    }

                    ===========================================================
                    */

                    is AppScreen.Result -> {

                        ResultScreen(
                            payload = screen.payload,

                            onBack = {
                                currentScreen = AppScreen.Home
                            }
                        )
                    }


                    is AppScreen.Camera -> {
                        CameraScreen(
                            conditionId = screen.conditionId,  // Use 'screen' instead of 'currentScreen'
                            onNavigateBack = { currentScreen = AppScreen.Home },
                            onAnalysisReady = { payload -> currentScreen = AppScreen.Result(payload) }
                        )
                    }

                }
            }
        }
    }
}
