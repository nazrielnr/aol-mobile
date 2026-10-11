package com.aol.moneytracker.presentation.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.aol.moneytracker.R
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.AppLanguage
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.ParsedVoiceTransaction
import com.aol.moneytracker.util.VoiceParser
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clean, Modern Top Voice Popup & Unified Bottom Confirmation Modal:
 * 1. Appears directly at the TOP of the screen with a single long flowing blue wave extending
 *    down across the top hero section, Google Assistant-style dynamic equalizer bars, and live speech text.
 * 2. Automatically simulates a 3-second voice input after opening (while also listening to real mic)
 *    so the experience remains pure and clutter-free.
 * 3. Once the final speech result is ready, opens the unified `TransactionConfirmationBottomSheet`
 *    (`TransactionDetailBottomSheet`) to confirm whether the detected transaction is correct.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantPopupOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirmVoiceResult: (com.aol.moneytracker.domain.model.Transaction, Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    var spokenText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var voiceAmplitude by remember { mutableFloatStateOf(0f) }
    var simJob by remember { mutableStateOf<Job?>(null) }
    var sessionCounter by remember { mutableIntStateOf(0) }

    // Final parsed transaction ready for the Bottom Confirmation Modal
    var finalParsedResult by remember { mutableStateOf<ParsedVoiceTransaction?>(null) }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            runCatching { SpeechRecognizer.createSpeechRecognizer(context) }.getOrNull()
        } else null
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val samplePhrases = remember(currentLang) {
        if (currentLang == AppLanguage.ENGLISH) {
            listOf(
                "Lunch & Iced Coffee 35 thousand",
                "Car Fuel Pertamax 250 thousand",
                "Monthly Supermarket Shopping 185 thousand",
                "Freelance Project Bonus 2.5 million"
            )
        } else {
            listOf(
                "Makan Siang & Es Kopi 35 ribu",
                "Isi Bensin Pertamax 250 ribu",
                "Belanja Bulanan Supermarket 185 ribu",
                "Bonus Proyek Desain 2,5 juta"
            )
        }
    }

    // 3-Second Auto Simulation after opening
    val startThreeSecondSimulation: () -> Unit = {
        simJob?.cancel()
        val phrase = samplePhrases[sessionCounter % samplePhrases.size]
        sessionCounter++
        simJob = coroutineScope.launch {
            finalParsedResult = null
            spokenText = ""
            isListening = true
            isSpeaking = false
            voiceAmplitude = 0.08f

            // Initial brief pause (0.45s) while user begins speaking
            delay(450L)

            // Progressive word-by-word transcription over ~2.25s with active equalizer bars
            isSpeaking = true
            val words = phrase.split(" ")
            val totalSpeakMs = 2200L
            val stepMs = 85L
            val totalSteps = (totalSpeakMs / stepMs).toInt().coerceAtLeast(1)

            for (step in 1..totalSteps) {
                val progress = step.toFloat() / totalSteps.toFloat()
                val wordCount = (progress * words.size).toInt().coerceIn(1, words.size)
                spokenText = words.take(wordCount).joinToString(" ")

                // Organic speech envelope for the Google Assistant equalizer
                val syllablePulse = 0.38f +
                    0.34f * abs(sin(step * 0.92f)) +
                    0.26f * abs(cos(step * 1.48f))
                voiceAmplitude = syllablePulse.coerceIn(0.24f, 1.0f)
                delay(stepMs)
            }

            spokenText = phrase
            isSpeaking = false
            isListening = false
            voiceAmplitude = 0f

            // Short settle (0.3s) so total duration is ~3 seconds, then open Bottom Confirmation Modal
            delay(300L)
            runCatching { speechRecognizer?.stopListening() }
            finalParsedResult = VoiceParser.parse(phrase)
        }
    }

    val startRealSpeechRecognition: () -> Unit = {
        if (speechRecognizer != null && hasAudioPermission) {
            val localeTag = if (currentLang == AppLanguage.ENGLISH) "en-US" else "id-ID"
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            runCatching {
                speechRecognizer.startListening(recognizerIntent)
            }
        }
    }

    val beginSession: () -> Unit = {
        startThreeSecondSimulation()
        if (hasAudioPermission) {
            startRealSpeechRecognition()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            startRealSpeechRecognition()
        }
    }

    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() {
                simJob?.cancel()
                isListening = true
                isSpeaking = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                if (simJob?.isActive != true) {
                    val normalized = ((rmsdB + 2f) / 10f).coerceIn(0f, 1f)
                    voiceAmplitude = normalized
                    isSpeaking = normalized > 0.14f
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                if (simJob?.isActive != true) {
                    isSpeaking = false
                    isListening = false
                    voiceAmplitude = 0f
                }
            }

            override fun onError(error: Int) {
                // Keep 3-second auto simulation running seamlessly
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val best = matches?.firstOrNull().orEmpty()
                if (best.isNotBlank()) {
                    simJob?.cancel()
                    spokenText = best
                    isSpeaking = false
                    isListening = false
                    voiceAmplitude = 0f
                    finalParsedResult = VoiceParser.parse(best)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (partial.isNotBlank()) {
                    simJob?.cancel()
                    spokenText = partial
                    isSpeaking = true
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer?.setRecognitionListener(listener)

        onDispose {
            simJob?.cancel()
            runCatching {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            }
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            beginSession()
            if (!hasAudioPermission) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            simJob?.cancel()
            isListening = false
            isSpeaking = false
            voiceAmplitude = 0f
            spokenText = ""
            finalParsedResult = null
            runCatching { speechRecognizer?.stopListening() }
        }
    }

    BackHandler(enabled = visible) {
        simJob?.cancel()
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(28f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle backdrop scrim (tap anywhere outside to dismiss)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        simJob?.cancel()
                        onDismiss()
                    }
            )

            // ====================================================================
            // 1. TOP POPUP OVERLAY (Single Long Material Wave + Equalizer + Live Text)
            // ====================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                TopVoiceWaveAndEqualizerOverlay(
                    spokenText = spokenText,
                    voiceAmplitude = voiceAmplitude,
                    isListening = isListening,
                    isSpeaking = isSpeaking
                )
            }

            // ====================================================================
            // 2. BOTTOM MODAL CONFIRMATION (Uses the unified TransactionDetailBottomSheet component)
            // ====================================================================
            finalParsedResult?.let { parsed ->
                val encodedNote = remember(parsed, spokenText) {
                    val items = TransactionRichMetaCodec.inferItems(
                        title = parsed.title,
                        amount = parsed.amount,
                        categoryId = parsed.categoryId,
                        type = parsed.type
                    )
                    val loc = TransactionRichMetaCodec.inferLocation(
                        title = parsed.title,
                        categoryId = parsed.categoryId,
                        type = parsed.type
                    )
                    val plain = if (spokenText.isNotBlank()) "Input suara: \"$spokenText\"" else ""
                    TransactionRichMetaCodec.encode(items, loc, plain)
                }
                TransactionConfirmationBottomSheet(
                    title = parsed.title,
                    amount = parsed.amount,
                    type = parsed.type,
                    categoryId = parsed.categoryId,
                    note = encodedNote,
                    onDismiss = {
                        simJob?.cancel()
                        onDismiss()
                    },
                    onRetryClick = {
                        beginSession()
                    },
                    onEditDetails = { updatedTx ->
                        simJob?.cancel()
                        onConfirmVoiceResult(updatedTx, true)
                    },
                    onConfirmSave = { updatedTx ->
                        simJob?.cancel()
                        onConfirmVoiceResult(updatedTx, false)
                    }
                )
            }
        }
    }
}

/**
 * Top-of-Screen Seamless Voice Overlay:
 * - Single long Material wave (`1 wave aja tapi panjang`) extending down ~365.dp to cover the top hero section
 *   using standard Material 3 surface colors (`surfaceContainerHigh` / `surfaceContainer`) and subtle `outlineVariant` crest.
 */
@Composable
private fun TopVoiceWaveAndEqualizerOverlay(
    spokenText: String,
    voiceAmplitude: Float,
    isListening: Boolean,
    isSpeaking: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "topSingleVoiceWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "singleWavePhase"
    )

    val waveIntensity by animateFloatAsState(
        targetValue = when {
            isSpeaking -> (0.50f + voiceAmplitude * 0.50f).coerceIn(0.50f, 1f)
            isListening -> 0.25f
            else -> 0.15f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "singleWaveIntensity"
    )

    val topSurfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val bottomSurfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Consume taps on top overlay */ }
    ) {
        // Single Long Material Wave extending down across the Top Hero section (~365.dp)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(365.dp)
        ) {
            val w = size.width
            val h = size.height

            val fillPath = Path()
            val crestPath = Path()

            // Position the single wave's bottom edge around 82% of the 365.dp height (~300.dp)
            // with crests reaching down to ~345.dp (right at "Keuangan Anda")
            val baseY = h * 0.82f
            val maxAmp = 28.dp.toPx() * waveIntensity
            val freq = 2.2f * (2f * PI.toFloat() / w)

            fillPath.moveTo(0f, 0f)
            fillPath.lineTo(0f, baseY)

            val steps = 80
            val dx = w / steps
            for (i in 0..steps) {
                val x = i * dx
                val y = baseY + (
                    0.70f * sin(x * freq + wavePhase) +
                        0.30f * cos(x * freq * 1.85f - wavePhase * 1.2f)
                    ) * maxAmp
                fillPath.lineTo(x, y)
                if (i == 0) {
                    crestPath.moveTo(x, y)
                } else {
                    crestPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(w, 0f)
            fillPath.close()

            // 1. Standard Material 3 surface gradient fill (solid so underlying Home text never bleeds through)
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    0.00f to topSurfaceColor,
                    1.00f to bottomSurfaceColor
                )
            )

            // 2. Clean Material 3 outline border along the bottom wave curve
            drawPath(
                path = crestPath,
                color = outlineColor.copy(alpha = 0.55f),
                style = Stroke(
                    width = 1.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Clean Floating Equalizer & Live Speech Text centered inside the single long wave
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 42.dp, bottom = 56.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Google Assistant Dynamic Equalizer
            GoogleAssistantEqualizer(
                voiceAmplitude = voiceAmplitude,
                isListening = isListening,
                isVoiceActive = isSpeaking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Live Speech-to-Text ("Tulisan Hasil")
            AnimatedContent(
                targetState = spokenText,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                },
                label = "topLiveSpeechText"
            ) { currentText ->
                if (currentText.isBlank()) {
                    Text(
                        text = stringResource(R.string.voice_status_listening),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    val highlightColor = MaterialTheme.colorScheme.primary
                    val annotated = remember(currentText, highlightColor) {
                        buildHighlightedVoiceAnnotatedString(
                            text = currentText,
                            highlightColor = highlightColor
                        )
                    }
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            lineHeight = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Google Assistant-inspired Multi-Bar Dynamic Equalizer:
 * - Silent: gentle breathing dots.
 * - Speaking (`isVoiceActive`): smooth multi-band frequency pill bars.
 */
@Composable
fun GoogleAssistantEqualizer(
    voiceAmplitude: Float,
    isListening: Boolean,
    isVoiceActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "googleAssistEq")
    val eqPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 640, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "eqPhase"
    )
    val idlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idlePhase"
    )

    val smoothedAmp by animateFloatAsState(
        targetValue = if (isVoiceActive) voiceAmplitude.coerceIn(0.18f, 1f) else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "smoothedEqAmp"
    )

    val isDark = MoneyTrackerTheme.colors.isDark
    val barColors = remember(isDark) {
        if (isDark) {
            listOf(
                Color(0xFF60A5FA),
                Color(0xFFA8C7FA),
                Color(0xFF38BDF8),
                Color(0xFFFB7185),
                Color(0xFFFBBF24),
                Color(0xFF4ADE80),
                Color(0xFFFBBF24),
                Color(0xFFFB7185),
                Color(0xFF38BDF8),
                Color(0xFFA8C7FA),
                Color(0xFF60A5FA)
            )
        } else {
            listOf(
                Color(0xFF3B82F6),
                Color(0xFF0B57D0),
                Color(0xFF0284C7),
                Color(0xFFE11D48),
                Color(0xFFD97706),
                Color(0xFF15803D),
                Color(0xFFD97706),
                Color(0xFFE11D48),
                Color(0xFF0284C7),
                Color(0xFF0B57D0),
                Color(0xFF3B82F6)
            )
        }
    }

    val barWeights = remember {
        listOf(0.35f, 0.55f, 0.75f, 0.92f, 0.85f, 1.00f, 0.85f, 0.92f, 0.75f, 0.55f, 0.35f)
    }

    Canvas(modifier = modifier) {
        val barCount = barColors.size
        val barWidthPx = 6.5.dp.toPx()
        val barSpacingPx = 7.dp.toPx()
        val totalWidth = barCount * barWidthPx + (barCount - 1) * barSpacingPx
        val startX = (size.width - totalWidth) / 2f
        val centerY = size.height / 2f
        val maxBarHeight = size.height * 0.92f
        val minDotHeight = 6.5.dp.toPx()

        for (i in 0 until barCount) {
            val weight = barWeights[i]
            val color = barColors[i]

            val targetHeight = if (smoothedAmp > 0.04f) {
                val osc1 = abs(sin(eqPhase + i * 0.65f))
                val osc2 = abs(cos(eqPhase * 1.6f - i * 0.9f))
                val dynamicFactor = 0.35f + 0.65f * (0.6f * osc1 + 0.4f * osc2)
                (minDotHeight + (maxBarHeight - minDotHeight) * smoothedAmp * weight * dynamicFactor)
                    .coerceIn(minDotHeight, maxBarHeight)
            } else if (isListening) {
                val gentle = (sin(idlePhase + i * 0.55f) + 1f) * 0.5f
                minDotHeight + 4.dp.toPx() * gentle * weight
            } else {
                minDotHeight
            }

            val x = startX + i * (barWidthPx + barSpacingPx)
            val topY = centerY - targetHeight / 2f

            drawRoundRect(
                color = color,
                topLeft = Offset(x, topY),
                size = Size(barWidthPx, targetHeight),
                cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)
            )
        }
    }
}

private fun buildHighlightedVoiceAnnotatedString(
    text: String,
    highlightColor: Color
) = buildAnnotatedString {
    val numberOrUnitRegex = Regex("""(?i)(\d+(?:[.,]\d+)?\s*(?:juta|jt|ribu|rb|k|thousand|million)?|rp\.?\s*\d+)""")
    var lastIdx = 0
    for (match in numberOrUnitRegex.findAll(text)) {
        if (match.range.first > lastIdx) {
            append(text.substring(lastIdx, match.range.first))
        }
        withStyle(
            SpanStyle(
                color = highlightColor,
                fontWeight = FontWeight.ExtraBold
            )
        ) {
            append(match.value)
        }
        lastIdx = match.range.last + 1
    }
    if (lastIdx < text.length) {
        append(text.substring(lastIdx))
    }
}
