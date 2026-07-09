package com.example.mediscreen.ui.questionnaire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediscreen.data.model.ConditionQuestionnaire
import com.example.mediscreen.data.model.QuestionAnswer
import com.example.mediscreen.data.model.QuestionType
import com.example.mediscreen.data.model.ResultPayload

private val DisclaimerColor = Color(0xFF66737C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    condition: ConditionQuestionnaire,
    onBack: () -> Unit,
    onComplete: (ResultPayload) -> Unit,
    viewModel: QuestionnaireViewModel = viewModel(
        key = condition.conditionId,
        factory = QuestionnaireViewModel.factory(condition)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress = (uiState.currentIndex + 1).toFloat() / uiState.totalQuestions.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(condition.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = uiState.progressText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Screening aid only — not a diagnosis. When in doubt, call 911.",
                color = DisclaimerColor,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )

            condition.footnote?.let { footnote ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = footnote,
                    color = DisclaimerColor,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = uiState.currentQuestion.prompt,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            QuestionAnswerControls(
                questionType = uiState.currentQuestion.type,
                options = uiState.currentQuestion.options,
                currentAnswer = uiState.currentAnswer,
                onYesNo = { yes -> viewModel.answerCurrentQuestion(QuestionAnswer.YesNo(yes)) },
                onSingleSelect = { option ->
                    viewModel.answerCurrentQuestion(QuestionAnswer.SingleSelect(option))
                },
                onToggleMultiSelect = viewModel::toggleMultiSelectOption,
                onConfirmMultiSelect = viewModel::confirmMultiSelectAndAdvance
            )

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.canGoBack) {
                OutlinedButton(
                    onClick = viewModel::goBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Previous question")
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.isLastQuestion) {
                Button(
                    onClick = { onComplete(viewModel.evaluate()) },
                    enabled = uiState.isCurrentQuestionAnswered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Check now",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionAnswerControls(
    questionType: QuestionType,
    options: List<String>,
    currentAnswer: QuestionAnswer?,
    onYesNo: (Boolean) -> Unit,
    onSingleSelect: (String) -> Unit,
    onToggleMultiSelect: (String) -> Unit,
    onConfirmMultiSelect: () -> Unit
) {
    when (questionType) {
        QuestionType.YesNo -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnswerButton(text = "Yes", onClick = { onYesNo(true) })
                AnswerButton(text = "No", onClick = { onYesNo(false) })
            }
        }

        QuestionType.SingleSelect -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { option ->
                    AnswerButton(text = option, onClick = { onSingleSelect(option) })
                }
            }
        }

        QuestionType.MultiSelect -> {
            val selected = (currentAnswer as? QuestionAnswer.MultiSelect)?.selected.orEmpty()

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    FilterChip(
                        selected = option in selected,
                        onClick = { onToggleMultiSelect(option) },
                        label = { Text(option) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConfirmMultiSelect,
                enabled = currentAnswer != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
