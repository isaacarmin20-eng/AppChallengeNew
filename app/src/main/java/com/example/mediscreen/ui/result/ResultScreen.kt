package com.example.mediscreen.ui.result

import androidx.compose.foundation.clickable
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediscreen.data.model.ResultPayload

private val EmergencyRed = Color(0xFFD32F2F)
private val CautionAmber = Color(0xFFF57C00)
private val ReassuranceGreen = Color(0xFF2E7D32)
private val DisclaimerColor = Color(0xFF66737C)

private val PhoneNumberRegex = Regex("""\b1?[-\s]?\(?\d{3}\)?[-\s]?\d{3}[-\s]?\d{4}\b""")

private fun extractPhoneNumber(text: String): String? {
    val match = PhoneNumberRegex.find(text) ?: return null
    return match.value
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    payload: ResultPayload,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val headerColor = when {
        payload.urgent -> EmergencyRed
        payload.resultHeadline != null -> ReassuranceGreen
        else -> CautionAmber
    }
    val headerText = payload.resultHeadline
        ?: if (payload.urgent) "Seek emergency care now" else "Monitor closely and seek care if needed"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(payload.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (payload.urgent) {
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
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Call 911",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = headerColor.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = headerColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = headerText,
                        color = headerColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = payload.seekCareMessage,
                color = DisclaimerColor,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Guidance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                payload.instructions.forEachIndexed { index, instruction ->
                    val phoneNumber = extractPhoneNumber(instruction)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let { base ->
                                if (phoneNumber != null) {
                                    base.clickable {
                                        val intent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:${phoneNumber.filter { it.isDigit() }}")
                                        )
                                        context.startActivity(intent)
                                    }
                                } else {
                                    base
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${index + 1}. $instruction",
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 22.sp
                            )
                            if (phoneNumber != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap to call $phoneNumber",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
