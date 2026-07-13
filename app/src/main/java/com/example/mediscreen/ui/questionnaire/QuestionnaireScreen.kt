package com.example.mediscreen.ui.questionnaire

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediscreen.data.model.ConditionQuestionnaire
import com.example.mediscreen.data.model.QuestionAnswer
import com.example.mediscreen.data.model.QuestionType
import com.example.mediscreen.data.model.ResultPayload
import com.example.mediscreen.ui.MedicalCategoryIcon
import com.example.mediscreen.ui.iconForConditionId

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
    val theme = condition.themeColor
    val icon = remember(condition.conditionId) { iconForConditionId(condition.conditionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        icon?.let {
                            MedicalCategoryIcon(
                                icon = it,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text(condition.displayName)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.copy(alpha = 0.06f)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Item 3: segmented step progress indicator
                SegmentedProgress(
                    totalSteps = uiState.totalQuestions,
                    currentStep = uiState.currentIndex,
                    color = theme
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = uiState.progressText,
                    style = MaterialTheme.typography.labelMedium,
                    color = theme,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Item 5: animated question transition
                Crossfade(
                    targetState = uiState.currentIndex,
                    animationSpec = tween(durationMillis = 220),
                    label = "question_transition"
                ) { _ ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Item 2: question in a tinted card for stronger hierarchy
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = uiState.currentQuestion.prompt,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 30.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Item 4: "why we ask" microcopy, only when present
                                uiState.currentQuestion.rationale?.let { rationale ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = null,
                                            tint = theme,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.size(6.dp))
                                        Text(
                                            text = rationale,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        QuestionAnswerControls(
                            questionType = uiState.currentQuestion.type,
                            options = uiState.currentQuestion.options,
                            currentAnswer = uiState.currentAnswer,
                            accentColor = theme,
                            isLastQuestion = uiState.isLastQuestion,
                            onYesNo = { yes -> viewModel.answerCurrentQuestion(QuestionAnswer.YesNo(yes)) },
                            onSingleSelect = { option ->
                                viewModel.answerCurrentQuestion(QuestionAnswer.SingleSelect(option))
                            },
                            onToggleMultiSelect = viewModel::toggleMultiSelectOption,
                            onConfirmMultiSelect = viewModel::confirmMultiSelectAndAdvance
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                condition.waitingTip?.let { tip ->
                    WaitingTipCard(tip = tip, accentColor = theme)
                    Spacer(modifier = Modifier.height(26.dp))
                }

                EmergencyShortcut()

                Spacer(modifier = Modifier.height(32.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.isLastQuestion) {
                    Button(
                        onClick = { onComplete(viewModel.evaluate()) },
                        enabled = uiState.isCurrentQuestionAnswered,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme)
                    ) {
                        Text(
                            text = "Check now",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Item 6: reassurance strip to fill empty space intentionally
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = DisclaimerColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Your answers stay on this device and only guide you toward next steps.",
                        color = DisclaimerColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Screening aid only — not a diagnosis. When in doubt, call 911.",
                    color = DisclaimerColor,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                condition.footnote?.let { footnote ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = footnote,
                        color = DisclaimerColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentedProgress(
    totalSteps: Int,
    currentStep: Int,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 0 until totalSteps) {
            val isFilled = i <= currentStep
            val animatedColor by animateFloatAsState(
                targetValue = if (isFilled) 1f else 0f,
                animationSpec = tween(durationMillis = 250),
                label = "segment_fill_$i"
            )
            // Equal-width segments without RowScope.weight: each segment claims an
            // equal fraction of whatever width remains in the Row.
            val remainingSegments = totalSteps - i
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f / remainingSegments)
                    .height(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .background(color.copy(alpha = 0.15f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .background(color.copy(alpha = animatedColor))
                )
            }
        }
    }
}

@Composable
private fun WaitingTipCard(
    tip: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "While you wait",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun EmergencyShortcut() {
    val context = LocalContext.current

    // Gentle, slow pulse — a quiet call to attention without feeling alarming.
    val infiniteTransition = rememberInfiniteTransition(label = "emergency_shortcut_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                context.startActivity(intent)
            }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Call,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Not sure? You can call 911 at any time.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionAnswerControls(
    questionType: QuestionType,
    options: List<String>,
    currentAnswer: QuestionAnswer?,
    accentColor: Color,
    isLastQuestion: Boolean,
    onYesNo: (Boolean) -> Unit,
    onSingleSelect: (String) -> Unit,
    onToggleMultiSelect: (String) -> Unit,
    onConfirmMultiSelect: () -> Unit
) {
    when (questionType) {
        QuestionType.YesNo -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnswerButton(text = "Yes", accentColor = accentColor, filled = true, onClick = { onYesNo(true) })
                AnswerButton(text = "No", accentColor = accentColor, filled = false, onClick = { onYesNo(false) })
            }
        }

        QuestionType.SingleSelect -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { option ->
                    AnswerButton(text = option, accentColor = accentColor, filled = true, onClick = { onSingleSelect(option) })
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

            // On the last question, the screen's own "Check now" button already
            // handles submission — showing a second "Continue" button here would
            // be a redundant control that silently does nothing when tapped.
            if (!isLastQuestion) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onConfirmMultiSelect,
                    enabled = currentAnswer != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    accentColor: Color,
    filled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "button_press_scale"
    )

    if (filled) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .scale(scale),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = text,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .scale(scale),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = text,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}