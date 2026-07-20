package com.example.mediscreen.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediscreen.ui.theme.MediScreenTheme

private val CategoryIconColor = Color(0xFF087EA4)
private val CardBackground = Color(0xFFEAF7F8)
private val EmergencyRed = Color(0xFFD32F2F)

private data class EmergencyCategory(
    val label: String,
    val icon: CategoryIcon,
    val conditionId: String? = null
)

enum class CategoryIcon {
    FaceSpeech,
    Breathing,
    AllergicReaction,
    HeatIllness,
    ChestPain,
    Choking,
    Seizure,
    DiabeticEmergency,
    AsthmaAttack,
    Poisoning
}

private val EmergencyCategories = listOf(
    EmergencyCategory("Face/Speech Changes", CategoryIcon.FaceSpeech, conditionId = "stroke"),
    EmergencyCategory("Trouble Breathing", CategoryIcon.Breathing),
    EmergencyCategory("Allergic Reaction", CategoryIcon.AllergicReaction),
    EmergencyCategory("Heat Illness", CategoryIcon.HeatIllness),
    EmergencyCategory("Chest Pain", CategoryIcon.ChestPain, conditionId = "heart_attack"),
    EmergencyCategory("Choking", CategoryIcon.Choking, conditionId = "choking"),
    EmergencyCategory("Seizure", CategoryIcon.Seizure),
    EmergencyCategory("Diabetic Emergency", CategoryIcon.DiabeticEmergency, conditionId = "diabetic_emergency"),
    EmergencyCategory("Asthma Attack", CategoryIcon.AsthmaAttack, conditionId = "asthma_attack"),
    EmergencyCategory("Poisoning/Overdose", CategoryIcon.Poisoning, conditionId = "poisoning")
)

fun iconForConditionId(conditionId: String): CategoryIcon? =
    EmergencyCategories.firstOrNull { it.conditionId == conditionId }?.icon

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCategorySelected: (conditionId: String) -> Unit = {}
) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Not Sure / Just Call 911",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader()

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "This app does not provide a diagnosis. In a life-threatening emergency, call 911 immediately.",
                color = Color(0xFF66737C),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(EmergencyCategories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = {
                            category.conditionId?.let(onCategorySelected)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = CategoryIconColor,
                modifier = Modifier.size(42.dp)
            )
            Icon(
                imageVector = Icons.Filled.Emergency,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            text = "MediScreen",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CategoryCard(
    category: EmergencyCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.18f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MedicalCategoryIcon(
                icon = category.icon,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = category.label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun MedicalCategoryIcon(
    icon: CategoryIcon,
    modifier: Modifier = Modifier
) {
    val blue = CategoryIconColor
    val lightBlue = Color(0xFF2E93D8)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = w * 0.055f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val thinStroke = Stroke(
            width = w * 0.04f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (icon) {
            CategoryIcon.FaceSpeech -> {
                drawOval(
                    color = blue,
                    topLeft = Offset(w * 0.28f, h * 0.18f),
                    size = Size(w * 0.44f, h * 0.58f),
                    style = stroke
                )
                drawArc(
                    color = blue,
                    startAngle = 205f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(w * 0.24f, h * 0.06f),
                    size = Size(w * 0.54f, h * 0.44f),
                    style = Stroke(w * 0.09f, cap = StrokeCap.Round)
                )
                drawCircle(blue, w * 0.035f, Offset(w * 0.42f, h * 0.44f))
                drawCircle(blue, w * 0.035f, Offset(w * 0.60f, h * 0.44f))
                drawArc(
                    color = blue,
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(w * 0.41f, h * 0.54f),
                    size = Size(w * 0.22f, h * 0.13f),
                    style = thinStroke
                )
            }

            CategoryIcon.Breathing -> {
                drawRoundRect(
                    color = lightBlue,
                    topLeft = Offset(w * 0.12f, h * 0.26f),
                    size = Size(w * 0.32f, h * 0.46f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.16f, w * 0.16f)
                )
                drawRoundRect(
                    color = lightBlue,
                    topLeft = Offset(w * 0.56f, h * 0.26f),
                    size = Size(w * 0.32f, h * 0.46f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.16f, w * 0.16f)
                )
                drawLine(blue, Offset(w * 0.5f, h * 0.08f), Offset(w * 0.5f, h * 0.68f), strokeWidth = w * 0.075f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.32f, h * 0.58f), strokeWidth = w * 0.035f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.68f, h * 0.58f), strokeWidth = w * 0.035f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.32f, h * 0.58f), Offset(w * 0.24f, h * 0.51f), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.68f, h * 0.58f), Offset(w * 0.76f, h * 0.51f), strokeWidth = w * 0.03f, cap = StrokeCap.Round)
            }

            CategoryIcon.AllergicReaction -> {
                drawOval(
                    color = blue,
                    topLeft = Offset(w * 0.27f, h * 0.14f),
                    size = Size(w * 0.46f, h * 0.58f),
                    style = stroke
                )
                drawArc(
                    color = lightBlue,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.24f, h * 0.05f),
                    size = Size(w * 0.52f, h * 0.44f),
                    style = Stroke(w * 0.12f, cap = StrokeCap.Round)
                )
                listOf(
                    Offset(w * 0.34f, h * 0.47f),
                    Offset(w * 0.66f, h * 0.47f),
                    Offset(w * 0.40f, h * 0.60f),
                    Offset(w * 0.60f, h * 0.61f)
                ).forEach { drawCircle(lightBlue, w * 0.025f, it) }
                drawArc(
                    color = blue,
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(w * 0.40f, h * 0.54f),
                    size = Size(w * 0.22f, h * 0.14f),
                    style = thinStroke
                )
            }

            CategoryIcon.HeatIllness -> {
                drawCircle(lightBlue, w * 0.18f, Offset(w * 0.37f, h * 0.34f))
                listOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f).forEach { angle ->
                    val radians = Math.toRadians(angle.toDouble())
                    val start = Offset(
                        x = w * 0.37f + kotlin.math.cos(radians).toFloat() * w * 0.28f,
                        y = h * 0.34f + kotlin.math.sin(radians).toFloat() * w * 0.28f
                    )
                    val end = Offset(
                        x = w * 0.37f + kotlin.math.cos(radians).toFloat() * w * 0.36f,
                        y = h * 0.34f + kotlin.math.sin(radians).toFloat() * w * 0.36f
                    )
                    drawLine(blue, start, end, strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                }
                val flame = Path().apply {
                    moveTo(w * 0.65f, h * 0.78f)
                    cubicTo(w * 0.50f, h * 0.62f, w * 0.68f, h * 0.52f, w * 0.62f, h * 0.34f)
                    cubicTo(w * 0.84f, h * 0.48f, w * 0.87f, h * 0.65f, w * 0.72f, h * 0.80f)
                    cubicTo(w * 0.70f, h * 0.67f, w * 0.59f, h * 0.70f, w * 0.65f, h * 0.78f)
                    close()
                }
                drawPath(flame, blue)
            }

            CategoryIcon.ChestPain -> {
                val heart = Path().apply {
                    moveTo(w * 0.5f, h * 0.78f)
                    cubicTo(w * 0.12f, h * 0.54f, w * 0.18f, h * 0.18f, w * 0.42f, h * 0.25f)
                    cubicTo(w * 0.48f, h * 0.27f, w * 0.5f, h * 0.34f, w * 0.5f, h * 0.34f)
                    cubicTo(w * 0.5f, h * 0.34f, w * 0.52f, h * 0.27f, w * 0.58f, h * 0.25f)
                    cubicTo(w * 0.82f, h * 0.18f, w * 0.88f, h * 0.54f, w * 0.5f, h * 0.78f)
                    close()
                }
                drawPath(heart, lightBlue)
                drawLine(Color.White, Offset(w * 0.20f, h * 0.52f), Offset(w * 0.40f, h * 0.52f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.40f, h * 0.52f), Offset(w * 0.48f, h * 0.36f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.48f, h * 0.36f), Offset(w * 0.58f, h * 0.66f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.58f, h * 0.66f), Offset(w * 0.66f, h * 0.52f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.66f, h * 0.52f), Offset(w * 0.82f, h * 0.52f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
            }

            CategoryIcon.Choking -> {
                drawArc(
                    color = lightBlue,
                    startAngle = 230f,
                    sweepAngle = 210f,
                    useCenter = false,
                    topLeft = Offset(w * 0.36f, h * 0.10f),
                    size = Size(w * 0.30f, h * 0.35f),
                    style = Stroke(w * 0.10f, cap = StrokeCap.Round)
                )
                drawLine(blue, Offset(w * 0.57f, h * 0.40f), Offset(w * 0.48f, h * 0.57f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(blue, Offset(w * 0.48f, h * 0.57f), Offset(w * 0.62f, h * 0.70f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(blue, Offset(w * 0.63f, h * 0.55f), Offset(w * 0.78f, h * 0.45f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(blue, Offset(w * 0.66f, h * 0.62f), Offset(w * 0.82f, h * 0.52f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(lightBlue, Offset(w * 0.28f, h * 0.48f), Offset(w * 0.12f, h * 0.44f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(lightBlue, Offset(w * 0.29f, h * 0.58f), Offset(w * 0.12f, h * 0.60f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
            }

            CategoryIcon.Seizure -> {
                val wave = Path().apply {
                    moveTo(w * 0.08f, h * 0.52f)
                    lineTo(w * 0.30f, h * 0.52f)
                    lineTo(w * 0.39f, h * 0.20f)
                    lineTo(w * 0.50f, h * 0.82f)
                    lineTo(w * 0.58f, h * 0.48f)
                    lineTo(w * 0.66f, h * 0.58f)
                    lineTo(w * 0.74f, h * 0.42f)
                    lineTo(w * 0.84f, h * 0.52f)
                    lineTo(w * 0.94f, h * 0.52f)
                }
                drawPath(wave, lightBlue, style = Stroke(w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            CategoryIcon.DiabeticEmergency -> {
                drawLine(blue, Offset(w * 0.25f, h * 0.80f), Offset(w * 0.78f, h * 0.27f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                drawLine(blue, Offset(w * 0.55f, h * 0.13f), Offset(w * 0.90f, h * 0.48f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                drawLine(blue, Offset(w * 0.63f, h * 0.05f), Offset(w * 0.98f, h * 0.40f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                drawLine(blue, Offset(w * 0.73f, h * 0.18f), Offset(w * 0.86f, h * 0.05f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                drawLine(blue, Offset(w * 0.52f, h * 0.38f), Offset(w * 0.67f, h * 0.53f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                drawRoundRect(
                    color = lightBlue,
                    topLeft = Offset(w * 0.30f, h * 0.52f),
                    size = Size(w * 0.26f, h * 0.20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f, w * 0.05f)
                )
            }

            CategoryIcon.AsthmaAttack -> {
                drawLine(lightBlue, Offset(w * 0.12f, h * 0.38f), Offset(w * 0.68f, h * 0.38f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                drawLine(lightBlue, Offset(w * 0.12f, h * 0.56f), Offset(w * 0.62f, h * 0.56f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                drawLine(lightBlue, Offset(w * 0.12f, h * 0.74f), Offset(w * 0.50f, h * 0.74f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                drawArc(
                    color = blue,
                    startAngle = 200f,
                    sweepAngle = 250f,
                    useCenter = false,
                    topLeft = Offset(w * 0.40f, h * 0.16f),
                    size = Size(w * 0.30f, h * 0.24f),
                    style = thinStroke
                )
                drawArc(
                    color = blue,
                    startAngle = 200f,
                    sweepAngle = 250f,
                    useCenter = false,
                    topLeft = Offset(w * 0.62f, h * 0.36f),
                    size = Size(w * 0.28f, h * 0.22f),
                    style = thinStroke
                )
            }

            CategoryIcon.Poisoning -> {
                val warning = Path().apply {
                    moveTo(w * 0.50f, h * 0.12f)
                    lineTo(w * 0.90f, h * 0.82f)
                    lineTo(w * 0.10f, h * 0.82f)
                    close()
                }
                drawPath(warning, blue, style = stroke)
                drawCircle(blue, w * 0.11f, Offset(w * 0.50f, h * 0.52f))
                drawCircle(Color.White, w * 0.022f, Offset(w * 0.46f, h * 0.49f))
                drawCircle(Color.White, w * 0.022f, Offset(w * 0.54f, h * 0.49f))
                drawLine(blue, Offset(w * 0.34f, h * 0.70f), Offset(w * 0.66f, h * 0.58f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
                drawLine(blue, Offset(w * 0.34f, h * 0.58f), Offset(w * 0.66f, h * 0.70f), strokeWidth = w * 0.045f, cap = StrokeCap.Round)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MediScreenTheme {
        HomeScreen()
    }
}
