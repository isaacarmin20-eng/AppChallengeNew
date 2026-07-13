package com.example.mediscreen.ui.camera



import android.Manifest
import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediscreen.data.ConditionCatalog
import com.example.mediscreen.data.model.ResultPayload
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

// Stroke theme color
private val StrokeRed = Color(0xFFD64550)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    conditionId: String,
    onNavigateBack: () -> Unit,
    onAnalysisReady: (ResultPayload) -> Unit,
    viewModel: CameraViewModel = viewModel(key = conditionId)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        if (cameraPermission.status.isGranted) {
            viewModel.onPermissionResult(true)
        } else {
            cameraPermission.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (uiState.permissionGranted) {
            when (uiState.currentStep) {
                CaptureStep.FACE, CaptureStep.ARMS -> {
                    CameraPreviewWithOverlay(
                        step = uiState.currentStep,
                        isCapturing = uiState.isCapturing,
                        onCapture = { imageCapture, executor ->
                            capturePhoto(
                                context = context,
                                imageCapture = imageCapture,
                                executor = executor,
                                onCaptured = { path ->
                                    if (uiState.currentStep == CaptureStep.FACE) {
                                        viewModel.onFaceCaptured(path)
                                    } else {
                                        viewModel.onArmsCaptured(path)
                                    }
                                    viewModel.setCapturing(false)
                                },
                                onError = { viewModel.setCapturing(false) }
                            )
                            viewModel.setCapturing(true)
                        },
                        onNavigateBack = onNavigateBack
                    )
                }
                CaptureStep.SPEECH -> {
                    SpeechInputStep(
                        speechText = uiState.speechText,
                        onTextChanged = viewModel::onSpeechTextChanged,
                        onSubmit = {
                            // Phase C: real AI call goes here
                            // For now, pass a mock ResultPayload
                            val mockPayload = ResultPayload(
                                urgent = true,
                                conditionId = conditionId,
                                displayName = "Face/Speech Changes (Stroke)",
                                instructions = listOf(
                                    "Call 911 immediately — don't wait to see if symptoms improve.",
                                    "Keep the person calm and still. Do not give them food or water.",
                                    "Note the time symptoms started — paramedics will need this.",
                                    "If they lose consciousness and stop breathing, begin CPR if trained."
                                ),
                                seekCareMessage = "Signs of stroke were detected. Every minute matters — call 911 now."
                            )
                            onAnalysisReady(mockPayload)
                        },
                        isReady = viewModel.isReadyToSubmit(),
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        } else {
            PermissionDeniedScreen(
                onRequestAgain = { cameraPermission.launchPermissionRequest() },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun CameraPreviewWithOverlay(
    step: CaptureStep,
    isCapturing: Boolean,
    onCapture: (ImageCapture, Executor) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    // Camera preview
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    // Overlay layer
    Box(modifier = Modifier.fillMaxSize()) {

        // Dark vignette scrim
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ovalRect = androidx.compose.ui.geometry.Rect(
                left = size.width * 0.1f,
                top = size.height * 0.15f,
                right = size.width * 0.9f,
                bottom = size.height * 0.65f
            )
            val path = androidx.compose.ui.graphics.Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                addOval(ovalRect)
            }
            drawPath(
                path = path,
                color = Color.Black.copy(alpha = 0.55f),
                blendMode = BlendMode.SrcOver
            )
            // Oval border
            drawOval(
                color = StrokeRed,
                topLeft = ovalRect.topLeft,
                size = androidx.compose.ui.geometry.Size(ovalRect.width, ovalRect.height),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            // Step indicator chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Face", "Arms", "Speech").forEachIndexed { i, label ->
                    val active = i < step.stepNumber
                    val current = i == step.stepNumber - 1
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            current -> StrokeRed
                            active -> StrokeRed.copy(alpha = 0.4f)
                            else -> Color.White.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp)) // balance back button
        }

        // Instruction card (below oval)
        val instruction = when (step) {
            CaptureStep.FACE -> "Center the person's face in the oval.\nLook for drooping on one side."
            CaptureStep.ARMS -> "Ask them to raise both arms.\nCapture the attempt — even partial weakness counts."
            CaptureStep.SPEECH -> "" // handled separately
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = instruction,
                    color = Color.White,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    lineHeight = 22.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // Capture button
            val scale by animateFloatAsState(
                targetValue = if (isCapturing) 0.9f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "captureScale"
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(scale)
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(enabled = !isCapturing) { onCapture(imageCapture, executor) }
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.Gray else StrokeRed)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Persistent 911 shortcut
            TextButton(onClick = { /* dial 911 */ }) {
                Text("Not sure? Call 911 now", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SpeechInputStep(
    speechText: String,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    isReady: Boolean,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Face" to true, "Arms" to true, "Speech" to true).forEach { (label, done) ->
                    Surface(shape = RoundedCornerShape(20.dp), color = if (label == "Speech") StrokeRed else StrokeRed.copy(alpha = 0.4f)) {
                        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(32.dp))

        Text("Step 3 of 3", color = StrokeRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("Speech Check", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2A2A2A), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ask them to say:", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "\"The sky is blue\"",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "FAST protocol — slurred or confused speech is a key stroke warning sign.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("What did you observe?", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = speechText,
            onValueChange = onTextChanged,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            placeholder = { Text("e.g. speech was slurred, couldn't finish the sentence…", color = Color.White.copy(alpha = 0.35f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = StrokeRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = StrokeRed
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onSubmit,
            enabled = isReady,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StrokeRed,
                disabledContainerColor = Color.White.copy(alpha = 0.15f)
            )
        ) {
            Text(
                if (isReady) "Analyze Symptoms" else "Complete all steps to continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { /* dial 911 */ }) {
            Text("Not sure? Call 911 now", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun PermissionDeniedScreen(onRequestAgain: () -> Unit, onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera Access Needed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("MediScreen needs camera access to capture photos for the visual symptom check. No photos are stored or shared.", color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRequestAgain, colors = ButtonDefaults.buttonColors(containerColor = StrokeRed), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)) {
            Text("Grant Camera Access", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateBack) {
            Text("Go back", color = Color.White.copy(alpha = 0.6f))
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onCaptured: (String) -> Unit,
    onError: () -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "mediscreen_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            onCaptured(photoFile.absolutePath)
        }
        override fun onError(exception: ImageCaptureException) {
            onError()
        }
    })
}