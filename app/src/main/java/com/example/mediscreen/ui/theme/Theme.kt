package com.example.mediscreen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MediScreenColorScheme = lightColorScheme(
    primary = Color(0xFF087EA4),
    secondary = Color(0xFF0F9F96),
    background = Color(0xFFF6FAFB),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF14212A),
    onSurface = Color(0xFF14212A)
)

@Composable
fun MediScreenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MediScreenColorScheme,
        content = content
    )
}
