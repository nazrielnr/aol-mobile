package com.aol.moneytracker.presentation.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aol.moneytracker.R
import com.aol.moneytracker.ReceiptCameraActivity
import com.aol.moneytracker.SettingsActivity
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.components.BottomFadeScrim
import com.aol.moneytracker.presentation.components.VoiceAssistantPopupOverlay
import com.aol.moneytracker.presentation.screens.activity.ActivityScreen
import com.aol.moneytracker.presentation.screens.add.AddTransactionBottomSheet
import com.aol.moneytracker.presentation.screens.home.HomeScreen
import com.aol.moneytracker.presentation.screens.home.HomeViewModel
import com.aol.moneytracker.presentation.screens.stats.StatsScreen
import com.aol.moneytracker.presentation.screens.stats.StatsViewModel
import com.aol.moneytracker.presentation.screens.wallet.TransactionWalletMeta
import com.aol.moneytracker.presentation.screens.wallet.WalletScreen
import com.aol.moneytracker.presentation.screens.wallet.WalletStateHolder
import com.aol.moneytracker.presentation.screens.wallet.bankWithCustomBracket
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.AppLanguage
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.VoiceParser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    homeViewModel: HomeViewModel,
    statsViewModel: StatsViewModel,
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    var isAddSheetVisible by remember { mutableStateOf(false) }

    // Mode 1: Normal Tap -> Vertical Pill Popup (Lead Icon + Text)
    var isTapMenuOpen by remember { mutableStateOf(false) }
    // Mode 2: Long-Press & Drag -> Curved Horizontal Radial Arc (Lead Icon only)
    var isHoldArcOpen by remember { mutableStateOf(false) }
    val isAnySpeedDialOpen = isTapMenuOpen || isHoldArcOpen

    // Google Assistant-style Voice Input Popup state
    var isVoicePopupOpen by remember { mutableStateOf(false) }

    // Currently hovered option index during press-hold-drag: 0 = Voice (Left), 1 = Camera (Center-Top), 2 = Manual (Right)
    var hoveredOptionIndex by remember { mutableStateOf<Int?>(null) }

    // Prefill data passed to AddTransactionBottomSheet
    var initialTitle by remember { mutableStateOf("") }
    var initialAmount by remember { mutableLongStateOf(0L) }
    var initialType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var initialCategoryId by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val months = listOf(
        stringResource(R.string.month_aug_2026),
        stringResource(R.string.month_sep_2026),
        stringResource(R.string.month_oct_2026),
        stringResource(R.string.month_nov_2026),
        stringResource(R.string.period_all)
    )
    var selectedMonthIndex by remember { mutableIntStateOf(2) }
    val selectedMonth = months.getOrElse(selectedMonthIndex) { months[2] }
    val onMonthSelected: (String) -> Unit = { chosen ->
        val idx = months.indexOf(chosen)
        if (idx >= 0) selectedMonthIndex = idx
    }

    fun triggerOption(index: Int) {
        when (index) {
            0 -> {
                isVoicePopupOpen = true
            }
            1 -> {
                ReceiptCameraActivity.start(context)
            }
            2 -> {
                com.aol.moneytracker.AddTransactionActivity.startCreate(context = context)
            }
        }
    }

    // Only intercept back when Speed Dial overlay is open; otherwise let Android OS handle Predictive Back directly via Android API
    BackHandler(enabled = isAnySpeedDialOpen) {
        isTapMenuOpen = false
        isHoldArcOpen = false
        hoveredOptionIndex = null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Horizontal Swipeable Pager with all 4 pages pre-composed (0ms tab switch lag)
            val pageDividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 3,
                key = { page -> page }
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val strokeWidthPx = 0.5.dp.toPx()
                            // Single hairline divider drawn once per seam (on the right edge of pages 0..2 when mid-transition)
                            if (page < 3 && pageOffset > 0.0005f && pageOffset < 0.9995f) {
                                drawLine(
                                    color = pageDividerColor,
                                    start = Offset(size.width - strokeWidthPx / 2f, 0f),
                                    end = Offset(size.width - strokeWidthPx / 2f, size.height),
                                    strokeWidth = strokeWidthPx
                                )
                            }
                        }
                ) {
                    when (page) {
                        0 -> {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onAddTransactionClick = {
                                    isHoldArcOpen = false
                                    isTapMenuOpen = true
                                },
                                selectedMonth = selectedMonth,
                                months = months,
                                onMonthSelected = onMonthSelected,
                                onSettingsClick = {
                                    SettingsActivity.start(context)
                                },
                                onNotificationClick = {
                                    com.aol.moneytracker.NotificationActivity.start(context)
                                },
                                onNavigateToReports = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(3)
                                    }
                                }
                            )
                        }
                        1 -> {
                            WalletScreen(
                                selectedMonth = selectedMonth
                            )
                        }
                        2 -> {
                            ActivityScreen(
                                viewModel = homeViewModel,
                                selectedMonth = selectedMonth,
                                months = months,
                                onMonthSelected = onMonthSelected
                            )
                        }
                        3 -> {
                            StatsScreen(
                                viewModel = statsViewModel,
                                selectedMonth = selectedMonth,
                                months = months,
                                onMonthSelected = onMonthSelected
                            )
                        }
                    }
                }
            }

            // 2. Bottom Fade Scrim
            BottomFadeScrim(
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // 4. Floating Backdrop Scrim (When either Speed Dial mode is open)
            AnimatedVisibility(
                visible = isAnySpeedDialOpen,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150)),
                modifier = Modifier.zIndex(15f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MoneyTrackerTheme.colors.backdropScrim)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isTapMenuOpen = false
                            isHoldArcOpen = false
                            hoveredOptionIndex = null
                        }
                )
            }

            // 5A. MODE 1 (Normal Tap): Vertical Stacked Pill Popup (Lead Icon + Text)
            AnimatedVisibility(
                visible = isTapMenuOpen && !isHoldArcOpen,
                enter = fadeIn(tween(180)) + slideInVertically(tween(200)) { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 121.dp)
                    .zIndex(20f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Option 1: 🎙️ Catat Suara
                    SpeedDialActionItem(
                        label = stringResource(R.string.speed_dial_voice),
                        icon = Icons.Default.Mic,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            isTapMenuOpen = false
                            triggerOption(0)
                        }
                    )

                    // Option 2: 📸 Foto Struk
                    SpeedDialActionItem(
                        label = stringResource(R.string.speed_dial_camera),
                        icon = Icons.Outlined.PhotoCamera,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            isTapMenuOpen = false
                            triggerOption(1)
                        }
                    )

                    // Option 3: ✍️ Input Manual
                    SpeedDialActionItem(
                        label = stringResource(R.string.speed_dial_manual),
                        icon = Icons.Outlined.EditNote,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            isTapMenuOpen = false
                            triggerOption(2)
                        }
                    )
                }
            }

            // 5B. MODE 2 (Long-Press & Drag): Curved Horizontal Radial Arc (Lead Icon ONLY, No Text)
            // Left (-76dp, -66dp) = Manual (2) | Center Peak (0dp, -96dp) = Camera (1) | Right (+76dp, -66dp) = Voice (0)
            AnimatedVisibility(
                visible = isHoldArcOpen,
                enter = fadeIn(tween(140)) + scaleIn(
                    initialScale = 0.45f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.55f, animationSpec = tween(120)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 49.dp)
                    .zIndex(22f)
            ) {
                Box(
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Left Arc Button (option 2): ✍️ Manual Input
                    SpeedDialArcButton(
                        icon = Icons.Outlined.EditNote,
                        contentDescription = stringResource(R.string.speed_dial_manual),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        activeContainerColor = MaterialTheme.colorScheme.tertiary,
                        activeContentColor = MaterialTheme.colorScheme.onTertiary,
                        isHovered = hoveredOptionIndex == 2,
                        offsetX = (-76).dp,
                        offsetY = (-66).dp,
                        onClick = {
                            triggerHapticPulse(context, view, isConfirm = true)
                            isHoldArcOpen = false
                            hoveredOptionIndex = null
                            triggerOption(2)
                        }
                    )

                    // Center-Top Arc Button (option 1): 📸 Camera
                    SpeedDialArcButton(
                        icon = Icons.Outlined.PhotoCamera,
                        contentDescription = stringResource(R.string.speed_dial_camera),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        activeContainerColor = MaterialTheme.colorScheme.secondary,
                        activeContentColor = MaterialTheme.colorScheme.onSecondary,
                        isHovered = hoveredOptionIndex == 1,
                        offsetX = 0.dp,
                        offsetY = (-96).dp,
                        onClick = {
                            triggerHapticPulse(context, view, isConfirm = true)
                            isHoldArcOpen = false
                            hoveredOptionIndex = null
                            triggerOption(1)
                        }
                    )

                    // Right Arc Button (option 0): 🎙️ Mic (Voice)
                    SpeedDialArcButton(
                        icon = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.speed_dial_voice),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        isHovered = hoveredOptionIndex == 0,
                        offsetX = 76.dp,
                        offsetY = (-66).dp,
                        onClick = {
                            triggerHapticPulse(context, view, isConfirm = true)
                            isHoldArcOpen = false
                            hoveredOptionIndex = null
                            triggerOption(0)
                        }
                    )
                }
            }

            // 6. Floating Rounded Navigation Bar with Dual-Mode (+) Button (Tap vs Hold & Drag)
            FloatingBottomBar(
                pagerState = pagerState,
                onAddClick = {
                    if (isAnySpeedDialOpen) {
                        isTapMenuOpen = false
                        isHoldArcOpen = false
                        hoveredOptionIndex = null
                    } else {
                        isTapMenuOpen = true
                        isHoldArcOpen = false
                    }
                },
                isExpanded = isAnySpeedDialOpen,
                onDragStart = {
                    isTapMenuOpen = false
                    if (!isHoldArcOpen) {
                        isHoldArcOpen = true
                        triggerHapticPulse(context, view, isConfirm = false)
                    }
                    hoveredOptionIndex = null
                },
                onDragMove = { pointerOffsetFromButtonCenter ->
                    val distFromButtonCenter = pointerOffsetFromButtonCenter.getDistance()
                    val minActivationPx = with(density) { 26.dp.toPx() }
                    val maxBottomPx = with(density) { 18.dp.toPx() }

                    val newHover = if (distFromButtonCenter < minActivationPx || pointerOffsetFromButtonCenter.y > maxBottomPx) {
                        null
                    } else {
                        // Left = Manual (2), Center-Top = Camera (1), Right = Voice (0)
                        val targets = listOf(
                            2 to Offset(with(density) { (-76).dp.toPx() }, with(density) { (-66).dp.toPx() }),
                            1 to Offset(0f, with(density) { (-96).dp.toPx() }),
                            0 to Offset(with(density) { 76.dp.toPx() }, with(density) { (-66).dp.toPx() })
                        )
                        val maxSnapRadiusPx = with(density) { 84.dp.toPx() }
                        targets
                            .map { (optionIdx, targetOffset) ->
                                optionIdx to (targetOffset - pointerOffsetFromButtonCenter).getDistance()
                            }
                            .filter { it.second <= maxSnapRadiusPx }
                            .minByOrNull { it.second }
                            ?.first
                    }

                    if (newHover != hoveredOptionIndex) {
                        hoveredOptionIndex = newHover
                        if (newHover != null) {
                            triggerHapticPulse(context, view, isConfirm = false)
                        }
                    }
                },
                onDragEnd = { wasDragged ->
                    val chosen = hoveredOptionIndex
                    hoveredOptionIndex = null
                    if (chosen != null) {
                        triggerHapticPulse(context, view, isConfirm = true)
                        isHoldArcOpen = false
                        isTapMenuOpen = false
                        triggerOption(chosen)
                    } else if (wasDragged) {
                        isHoldArcOpen = false
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(25f)
            )

            // 7. Google Assistant-Style Voice Input Popup (Catat Suara AI with TopBar Wave & Equalizer)
            VoiceAssistantPopupOverlay(
                visible = isVoicePopupOpen,
                onDismiss = { isVoicePopupOpen = false },
                onConfirmVoiceResult = { confirmedTx, openEditor ->
                    isVoicePopupOpen = false
                    if (openEditor || confirmedTx.amount <= 0L) {
                        com.aol.moneytracker.AddTransactionActivity.startCreate(
                            context = context,
                            initialTitle = confirmedTx.title,
                            initialAmount = confirmedTx.amount,
                            initialType = confirmedTx.type,
                            initialCategoryId = confirmedTx.categoryId,
                            initialNote = confirmedTx.note
                        )
                    } else {
                        WalletStateHolder.applyTransactionEffect(context, confirmedTx)
                        homeViewModel.addTransaction(confirmedTx)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.snackbar_transaction_saved, confirmedTx.title)
                            )
                        }
                    }
                }
            )
        }
    }

    // 8. Compact & Clean Add Transaction Modal
    if (isAddSheetVisible) {
        AddTransactionBottomSheet(
            sheetState = sheetState,
            initialTitle = initialTitle,
            initialAmount = initialAmount,
            initialType = initialType,
            initialCategoryId = initialCategoryId,
            onDismiss = { isAddSheetVisible = false },
            onSave = { newTransaction ->
                homeViewModel.addTransaction(newTransaction)
                coroutineScope.launch {
                    sheetState.hide()
                    isAddSheetVisible = false
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.snackbar_transaction_saved, newTransaction.title)
                    )
                }
            }
        )
    }
}

/**
 * Mode 1 (Normal Tap): Centered Speed Dial Action Item Pill (Lead Icon + Text)
 */
@Composable
private fun SpeedDialActionItem(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Mode 2 (Long-Press & Drag): Enlarged Icon-Only Lead Icon in a Curved Horizontal Radial Arc
 */
@Composable
private fun SpeedDialArcButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    activeContainerColor: Color,
    activeContentColor: Color,
    isHovered: Boolean,
    offsetX: Dp,
    offsetY: Dp,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.32f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "arcBtnScale"
    )
    val badgeBg by animateColorAsState(
        targetValue = if (isHovered) activeContainerColor else containerColor,
        animationSpec = tween(140),
        label = "arcBtnBg"
    )
    val badgeTint by animateColorAsState(
        targetValue = if (isHovered) activeContentColor else contentColor,
        animationSpec = tween(140),
        label = "arcBtnTint"
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isHovered) 14.dp else 6.dp,
        border = BorderStroke(
            width = if (isHovered) 2.dp else 1.dp,
            color = if (isHovered) activeContainerColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(5.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = badgeBg,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = badgeTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tactile haptic vibration feedback on swipe/hover change and release selection.
 */
@Suppress("DEPRECATION")
private fun triggerHapticPulse(
    context: Context,
    view: View,
    isConfirm: Boolean
) {
    view.performHapticFeedback(
        if (isConfirm) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.CLOCK_TICK
    )
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(
                        if (isConfirm) VibrationEffect.EFFECT_CLICK else VibrationEffect.EFFECT_TICK
                    )
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        if (isConfirm) 26L else 14L,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(if (isConfirm) 26L else 14L)
            }
        }
    } catch (_: Exception) {
    }
}

