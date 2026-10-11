package com.aol.moneytracker.presentation.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.FlipCameraAndroid
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.components.ModalTopBarButton
import com.aol.moneytracker.presentation.components.PurchasedItem
import com.aol.moneytracker.presentation.components.TransactionConfirmationBottomSheet
import com.aol.moneytracker.presentation.components.TransactionLocationData
import com.aol.moneytracker.presentation.components.TransactionRichMetaCodec
import com.aol.moneytracker.presentation.components.UniversalBackButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DetectedReceiptData(
    val title: String,
    val amount: Long,
    val categoryId: String,
    val type: TransactionType = TransactionType.EXPENSE,
    val items: List<PurchasedItem>,
    val location: TransactionLocationData,
    val plainNote: String = ""
)

private enum class CameraUiState {
    SCANNING,
    CONFIRM_READY
}

@Composable
fun ReceiptCameraScreen(
    onBackClick: () -> Unit,
    onEditReceipt: (
        title: String,
        amount: Long,
        type: TransactionType,
        categoryId: String,
        note: String
    ) -> Unit,
    onSaveReceipt: (
        title: String,
        amount: Long,
        type: TransactionType,
        categoryId: String,
        note: String
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var cameraState by remember { mutableStateOf(CameraUiState.SCANNING) }
    var scanCycleKey by remember { mutableIntStateOf(0) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val simulatedReceipts = remember {
        listOf(
            DetectedReceiptData(
                title = "Kopi Kenangan",
                amount = 54000L,
                categoryId = "food",
                items = listOf(
                    PurchasedItem(name = "Kenangan Mantan (L)", qty = 1, unitPrice = 29000L),
                    PurchasedItem(name = "Croissant Butter", qty = 1, unitPrice = 25000L)
                ),
                location = TransactionLocationData(
                    placeName = "Kopi Kenangan • Grand Indonesia",
                    address = "Jl. M.H. Thamrin No.1, Menteng, Jakarta Pusat",
                    latitude = -6.1952,
                    longitude = 106.8213
                ),
                plainNote = "Struk pembayaran QRIS #KK-9082"
            ),
            DetectedReceiptData(
                title = "Belanja Indomaret",
                amount = 87500L,
                categoryId = "shopping",
                items = listOf(
                    PurchasedItem(name = "Air Mineral 1.5L", qty = 2, unitPrice = 7500L),
                    PurchasedItem(name = "Roti Tawar Gandum", qty = 1, unitPrice = 22500L),
                    PurchasedItem(name = "Sabun Mandi Cair 450ml", qty = 2, unitPrice = 25000L)
                ),
                location = TransactionLocationData(
                    placeName = "Indomaret Point • Sudirman",
                    address = "Jl. Jend. Sudirman Kav. 52, Senayan, Jakarta Selatan",
                    latitude = -6.2246,
                    longitude = 106.8096
                ),
                plainNote = "Struk belanja kebutuhan harian"
            ),
            DetectedReceiptData(
                title = "Shell Super",
                amount = 150000L,
                categoryId = "transport",
                items = listOf(
                    PurchasedItem(name = "Shell Super RON 92 (10.8L)", qty = 1, unitPrice = 150000L)
                ),
                location = TransactionLocationData(
                    placeName = "SPBU Shell • Gatot Subroto",
                    address = "Jl. Gatot Subroto Kav. 38, Kuningan, Jakarta Selatan",
                    latitude = -6.2381,
                    longitude = 106.8254
                ),
                plainNote = "Pengisian BBM kendaraan"
            )
        )
    }

    val currentReceipt = remember(scanCycleKey) {
        simulatedReceipts[scanCycleKey % simulatedReceipts.size]
    }

    // 3-Second Auto Simulation after opening (or restarting scan)
    LaunchedEffect(scanCycleKey, cameraState) {
        if (cameraState == CameraUiState.SCANNING) {
            delay(3000L)
            cameraState = CameraUiState.CONFIRM_READY
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            cameraState = CameraUiState.CONFIRM_READY
        }
    }

    val triggerManualCapture: () -> Unit = {
        if (cameraState == CameraUiState.SCANNING) {
            coroutineScope.launch {
                delay(350L)
                cameraState = CameraUiState.CONFIRM_READY
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Clean Standard TopBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UniversalBackButton(onClick = onBackClick)

                Text(
                    text = stringResource(R.string.speed_dial_camera),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                ModalTopBarButton(
                    icon = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    onClick = { isFlashOn = !isFlashOn },
                    contentDescription = stringResource(R.string.speed_dial_camera)
                )
            }

            // 2. Clean Modern Camera Viewfinder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                CleanCameraViewfinderOverlay(
                    isScanning = cameraState == CameraUiState.SCANNING,
                    isFlashOn = isFlashOn,
                    modifier = Modifier.fillMaxSize()
                )

                // Minimal bottom hint inside viewfinder
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AnimatedContent(
                        targetState = cameraState,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                        label = "cameraHintText"
                    ) { state ->
                        Text(
                            text = when (state) {
                                CameraUiState.SCANNING -> stringResource(R.string.camera_guide_align)
                                CameraUiState.CONFIRM_READY -> stringResource(R.string.camera_guide_captured)
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            // 3. Clean Floating Camera Controls (No boxy card wrapper)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Button
                Surface(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = stringResource(R.string.camera_action_gallery),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Primary Shutter Button
                val shutterScale by animateFloatAsState(
                    targetValue = if (cameraState == CameraUiState.SCANNING) 1f else 0.94f,
                    animationSpec = tween(200),
                    label = "shutterScale"
                )
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(shutterScale)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { triggerManualCapture() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.speed_dial_camera),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Retake / Flip Cycle Button
                Surface(
                    onClick = {
                        selectedImageUri = null
                        scanCycleKey++
                        cameraState = CameraUiState.SCANNING
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.FlipCameraAndroid,
                            contentDescription = stringResource(R.string.camera_action_retake),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 4. Unified Bottom Confirmation Modal (Exact same component as Transaction Detail & Voice Confirmation)
        if (cameraState == CameraUiState.CONFIRM_READY) {
            val encodedNote = remember(currentReceipt) {
                TransactionRichMetaCodec.encode(
                    items = currentReceipt.items,
                    location = currentReceipt.location,
                    plainNote = currentReceipt.plainNote
                )
            }

            TransactionConfirmationBottomSheet(
                title = currentReceipt.title,
                amount = currentReceipt.amount,
                type = currentReceipt.type,
                categoryId = currentReceipt.categoryId,
                note = encodedNote,
                onDismiss = {
                    cameraState = CameraUiState.SCANNING
                    scanCycleKey++
                },
                onRetryClick = {
                    selectedImageUri = null
                    scanCycleKey++
                    cameraState = CameraUiState.SCANNING
                },
                onEditDetails = { updatedTx ->
                    onEditReceipt(
                        updatedTx.title,
                        updatedTx.amount,
                        updatedTx.type,
                        updatedTx.categoryId,
                        updatedTx.note
                    )
                },
                onConfirmSave = { updatedTx ->
                    onSaveReceipt(
                        updatedTx.title,
                        updatedTx.amount,
                        updatedTx.type,
                        updatedTx.categoryId,
                        updatedTx.note
                    )
                }
            )
        }
    }
}

/**
 * Minimal, clean camera framing guides & subtle scan line inside the viewfinder.
 */
@Composable
private fun CleanCameraViewfinderOverlay(
    isScanning: Boolean,
    isFlashOn: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cleanCameraScan")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.86f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanProgress"
    )
    val cornerPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerPulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        if (isFlashOn) {
            drawRect(color = Color.White.copy(alpha = 0.05f))
        }

        // Subtle rule-of-thirds grid
        val gridColor = Color.White.copy(alpha = 0.06f)
        drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = 1.dp.toPx())

        // Framing box dimensions
        val frameMarginH = w * 0.12f
        val frameMarginV = h * 0.13f
        val left = frameMarginH
        val top = frameMarginV
        val right = w - frameMarginH
        val bottom = h - frameMarginV
        val cornerLen = 30.dp.toPx()
        val strokeW = 3.dp.toPx()

        // Subtle inner frame outline
        drawRoundRect(
            color = Color.White.copy(alpha = 0.12f),
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )

        val bracketColor = Color.White.copy(alpha = cornerPulse)

        // Top-Left Corner
        drawLine(bracketColor, Offset(left, top + cornerLen), Offset(left, top), strokeW, StrokeCap.Round)
        drawLine(bracketColor, Offset(left, top), Offset(left + cornerLen, top), strokeW, StrokeCap.Round)

        // Top-Right Corner
        drawLine(bracketColor, Offset(right - cornerLen, top), Offset(right, top), strokeW, StrokeCap.Round)
        drawLine(bracketColor, Offset(right, top), Offset(right, top + cornerLen), strokeW, StrokeCap.Round)

        // Bottom-Left Corner
        drawLine(bracketColor, Offset(left, bottom - cornerLen), Offset(left, bottom), strokeW, StrokeCap.Round)
        drawLine(bracketColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW, StrokeCap.Round)

        // Bottom-Right Corner
        drawLine(bracketColor, Offset(right - cornerLen, bottom), Offset(right, bottom), strokeW, StrokeCap.Round)
        drawLine(bracketColor, Offset(right, bottom - cornerLen), Offset(right, bottom), strokeW, StrokeCap.Round)

        // Smooth horizontal scan line when actively scanning
        if (isScanning) {
            val scanY = top + (bottom - top) * scanProgress
            val glowHeight = 42.dp.toPx()

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryColor.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    startY = scanY - glowHeight / 2f,
                    endY = scanY + glowHeight / 2f
                ),
                topLeft = Offset(left + 6.dp.toPx(), scanY - glowHeight / 2f),
                size = Size(right - left - 12.dp.toPx(), glowHeight)
            )

            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryColor,
                        Color.White,
                        primaryColor,
                        Color.Transparent
                    ),
                    startX = left,
                    endX = right
                ),
                start = Offset(left + 6.dp.toPx(), scanY),
                end = Offset(right - 6.dp.toPx(), scanY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
