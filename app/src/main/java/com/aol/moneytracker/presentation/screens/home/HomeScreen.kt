package com.aol.moneytracker.presentation.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.FinancialSummary
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.components.AppDropdownMenu
import com.aol.moneytracker.presentation.components.EmptyStateView
import com.aol.moneytracker.presentation.components.FilterDropdownMenu
import com.aol.moneytracker.presentation.components.GlobalTopBar
import com.aol.moneytracker.presentation.components.ModernTransactionCard
import com.aol.moneytracker.presentation.components.TransactionDetailBottomSheet
import com.aol.moneytracker.presentation.screens.wallet.WalletStateHolder
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedMonth: String = "",
    months: List<String> = emptyList(),
    onMonthSelected: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var selectedTransactionForDetail by remember { mutableStateOf<Transaction?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val density = LocalDensity.current
    var syncedHeaderPillWidthPx by remember { mutableIntStateOf(0) }
    val syncedHeaderPillWidthDp = remember(syncedHeaderPillWidthPx, density) {
        if (syncedHeaderPillWidthPx > 0) {
            with(density) { syncedHeaderPillWidthPx.toDp() }
        } else {
            76.dp
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val bgColor = MaterialTheme.colorScheme.background

    // Sticky header bottom fade gradient with 10 real, visible steps (cached per theme surface color)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val stickyBottomFadeBrush = remember(surfaceColor) {
        Brush.verticalGradient(
            0.00f to surfaceColor.copy(alpha = 1.00f), // 100%
            0.10f to surfaceColor.copy(alpha = 0.90f), // 90%
            0.20f to surfaceColor.copy(alpha = 0.80f), // 80%
            0.30f to surfaceColor.copy(alpha = 0.70f), // 70%
            0.40f to surfaceColor.copy(alpha = 0.60f), // 60%
            0.50f to surfaceColor.copy(alpha = 0.50f), // 50%
            0.60f to surfaceColor.copy(alpha = 0.40f), // 40%
            0.70f to surfaceColor.copy(alpha = 0.30f), // 30%
            0.80f to surfaceColor.copy(alpha = 0.20f), // 20%
            0.90f to surfaceColor.copy(alpha = 0.10f), // 10%
            1.00f to Color.Transparent                 // 0%
        )
    }

    // Scroll state triggers: gradient bottom only appears when scroll is triggered!
    val isYourMoneyScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 4
        }
    }
    val yourMoneyFadeAlphaState = animateFloatAsState(
        targetValue = if (isYourMoneyScrolled) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "yourMoneyFadeAlpha"
    )

    val isTransactionsStuck by remember {
        derivedStateOf {
            val header = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "header_transactions" }
            if (header != null) {
                header.offset <= 4
            } else {
                listState.firstVisibleItemIndex >= 3
            }
        }
    }
    val transactionsFadeAlphaState = animateFloatAsState(
        targetValue = if (isTransactionsStuck) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "transactionsFadeAlpha"
    )

    // Heights calculation
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarContentHeight = 52.dp
    val topBarSpacing = 16.dp // Proper breathing space below the topbar when lifted

    val allPeriodsText = stringResource(R.string.period_all)

    // Dynamic space: when comparison pill is absent, hero area shrinks and Your Money slides up.
    // Keep true while isLoading so startup Frame 1 -> Frame 2 never triggers an unwanted 92dp -> 128dp spring animation.
    val hasComparisonPill = (uiState.isLoading || uiState.transactions.isNotEmpty()) && selectedMonth != allPeriodsText
    val heroSectionHeight by animateDpAsState(
        targetValue = if (hasComparisonPill) 128.dp else 92.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "heroSectionHeight"
    )

    val heroHeightPx = with(density) { heroSectionHeight.toPx() }
    val currentHeroHeightPx by androidx.compose.runtime.rememberUpdatedState(heroHeightPx)

    // Offset of the sliding sheet: 0f = resting, -heroHeightPx = lifted up to topBarTotalHeightPx
    var collapseOffsetPx by remember { mutableFloatStateOf(0f) }

    suspend fun animateToTarget(target: Float) {
        Animatable(collapseOffsetPx).animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) {
            collapseOffsetPx = value
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val maxCollapse = currentHeroHeightPx
                val delta = available.y
                if (delta < 0 && collapseOffsetPx > -maxCollapse) {
                    val newOffset = (collapseOffsetPx + delta).coerceIn(-maxCollapse, 0f)
                    val consumed = newOffset - collapseOffsetPx
                    collapseOffsetPx = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val maxCollapse = currentHeroHeightPx
                val delta = available.y
                if (delta > 0 && collapseOffsetPx < 0f) {
                    val newOffset = (collapseOffsetPx + delta).coerceIn(-maxCollapse, 0f)
                    val consumedDelta = newOffset - collapseOffsetPx
                    collapseOffsetPx = newOffset
                    return Offset(0f, consumedDelta)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val maxCollapse = currentHeroHeightPx
                val vy = available.y
                if (collapseOffsetPx > -maxCollapse && collapseOffsetPx < 0f) {
                    val target = if (vy < -300f) {
                        -maxCollapse
                    } else if (vy > 300f) {
                        0f
                    } else {
                        if (collapseOffsetPx < -maxCollapse / 2f) -maxCollapse else 0f
                    }
                    animateToTarget(target)
                    return available
                }
                return super.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val vy = available.y
                if (vy > 300f && collapseOffsetPx < 0f) {
                    animateToTarget(0f)
                    return available
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    val isSurplus = uiState.summary.balance >= 0
    val appColors = MoneyTrackerTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .nestedScroll(nestedScrollConnection)
    ) {
        // 0. AMBIENT FULL-SPREAD GRADIENT (Cached via drawWithCache: 0 per-frame Shader allocations, 0 background VSYNC wakeups)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight + topBarContentHeight + heroSectionHeight + 40.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    val gStops = appColors.heroGradientStops
                    val wStops = appColors.heroWaveStops
                    val vStops = appColors.heroVerticalStops

                    val primaryBrush = Brush.linearGradient(
                        0.00f to gStops[0],
                        0.25f to gStops[1],
                        0.50f to gStops[2],
                        0.75f to gStops[3],
                        1.00f to gStops[4],
                        start = Offset(0f, 0f),
                        end = Offset(w * 1.1f, h * 1.1f)
                    )
                    val secondaryBrush = Brush.linearGradient(
                        0.00f to wStops[0],
                        0.25f to Color.Transparent,
                        0.50f to wStops[1],
                        0.75f to Color.Transparent,
                        1.00f to wStops[2],
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                    val verticalBrush = Brush.verticalGradient(
                        0.00f to vStops[0],
                        0.50f to Color.Transparent,
                        1.00f to vStops[1],
                        startY = 0f,
                        endY = h
                    )

                    onDrawBehind {
                        drawRect(brush = primaryBrush)
                        drawRect(brush = secondaryBrush)
                        drawRect(brush = verticalBrush)
                    }
                }
        )

        // 2. HERO BALANCE SECTION (Fades out and moves with parallax as the sheet lifts up)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarHeight + topBarContentHeight + 4.dp)
                .height(heroSectionHeight)
                .graphicsLayer {
                    val progress = (-collapseOffsetPx / heroHeightPx).coerceIn(0f, 1f)
                    alpha = (1f - progress * 1.35f).coerceIn(0f, 1f)
                    translationY = collapseOffsetPx * 0.35f
                }
                .draggable(
                    state = rememberDraggableState { delta ->
                        collapseOffsetPx = (collapseOffsetPx + delta).coerceIn(-heroHeightPx, 0f)
                    },
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            val target = if (velocity < -300f) {
                                -heroHeightPx
                            } else if (velocity > 300f) {
                                0f
                            } else {
                                if (collapseOffsetPx < -heroHeightPx / 2f) -heroHeightPx else 0f
                            }
                            animateToTarget(target)
                        }
                    }
                )
                .align(Alignment.TopCenter)
                .zIndex(2f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.home_total_balance),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            AnimatedRupiahText(
                targetValue = uiState.summary.balance,
                isLoading = uiState.isLoading,
                initialDelayMillis = 100L,
                initialDurationMillis = 1400,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    letterSpacing = (-0.5).sp,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (hasComparisonPill) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (isSurplus) Icons.AutoMirrored.Filled.TrendingUp
                            else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        AnimatedComparisonBadgeText(
                            isSurplus = isSurplus,
                            isLoading = uiState.isLoading,
                            initialDelayMillis = 140L,
                            initialDurationMillis = 1350,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. ROUNDED CHILD CONTAINER (Lifts up to topbar + 16dp spacing via GPU translationY — zero re-layout during scroll!)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarHeight + topBarContentHeight + topBarSpacing)
                .graphicsLayer {
                    translationY = (heroHeightPx + collapseOffsetPx).coerceAtLeast(0f)
                }
                .zIndex(5f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // SCROLLABLE LIST ITEMS: Only the list items are affected by the Android 12+ boing overscroll effect!
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 160.dp // Generous space so last item scrolls comfortably above floating bottom nav bar
                    )
                ) {
                    // Your Money Sticky Header
                    stickyHeader(key = "header_your_money") {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(surfaceColor)
                                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.home_your_money),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Surface(
                                    onClick = onNavigateToReports,
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .widthIn(min = syncedHeaderPillWidthDp)
                                        .onSizeChanged { size ->
                                            if (size.width > syncedHeaderPillWidthPx) {
                                                syncedHeaderPillWidthPx = size.width
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_detail),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(end = 2.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // 10-step gradient fade overlay: alpha controlled strictly in draw phase
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp)
                                    .align(Alignment.BottomCenter)
                                    .graphicsLayer {
                                        translationY = size.height
                                        alpha = yourMoneyFadeAlphaState.value
                                    }
                                    .background(stickyBottomFadeBrush)
                            )
                        }
                    }

                    // Bento Cards: Income & Expenses
                    item(key = "bento_cards") {
                        BentoSummaryCards(
                            summary = uiState.summary,
                            isLoading = uiState.isLoading,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }

                    // Insight Banner
                    item(key = "insight_banner") {
                        Surface(
                            onClick = onNavigateToReports,
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MoneyTrackerTheme.colors.insightAccent,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.home_insight_ready),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.action_view),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Transactions Sticky Header
                    stickyHeader(key = "header_transactions") {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(surfaceColor)
                                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.home_transactions),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                FilterDropdownMenu(
                                    selectedFilter = uiState.selectedFilter,
                                    onFilterSelected = { viewModel.onFilterChange(it) },
                                    triggerMinWidth = syncedHeaderPillWidthDp,
                                    modifier = Modifier.onSizeChanged { size ->
                                        if (uiState.selectedFilter == TransactionFilter.ALL && size.width > syncedHeaderPillWidthPx) {
                                            syncedHeaderPillWidthPx = size.width
                                        }
                                    }
                                )
                            }

                            // Dedicated wide 10-step bottom gradient fade: alpha controlled strictly in draw phase
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .align(Alignment.BottomCenter)
                                    .graphicsLayer {
                                        translationY = size.height
                                        alpha = transactionsFadeAlphaState.value
                                    }
                                    .background(stickyBottomFadeBrush)
                            )
                        }
                    }

                    // Transactions List Grouped by Date with Daily Totals
                    if (uiState.transactions.isEmpty()) {
                        item {
                            EmptyStateView(
                                message = stringResource(R.string.home_empty_transactions)
                            )
                        }
                    } else {
                        uiState.groupedTransactions.entries.forEachIndexed { index, (dateGroup, transactionsInGroup) ->
                            item(key = "header_$dateGroup") {
                                val dayTotal = transactionsInGroup.sumOf {
                                    when {
                                        it.categoryId.equals("transfer", ignoreCase = true) -> 0L
                                        it.type == TransactionType.EXPENSE -> -it.amount
                                        else -> it.amount
                                    }
                                }
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp)
                                                .padding(top = 10.dp, bottom = 4.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                            thickness = 1.dp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 20.dp, end = 20.dp, top = if (index > 0) 4.dp else 8.dp, bottom = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dateGroup,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        val formattedDayTotal = CurrencyFormatter.formatRupiah(if (dayTotal < 0) -dayTotal else dayTotal)
                                        Text(
                                            text = stringResource(R.string.home_daily_total_format, formattedDayTotal),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            items(
                                items = transactionsInGroup,
                                key = { it.id }
                            ) { transaction ->
                                ModernTransactionCard(
                                    transaction = transaction,
                                    onClick = { selectedTransactionForDetail = transaction }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pinned Home TopBar (slides together with HomeScreen during HorizontalPager swipe)
        GlobalTopBar(
            months = months,
            selectedMonth = selectedMonth,
            onMonthSelected = onMonthSelected,
            onSettingsClick = onSettingsClick,
            onNotificationClick = onNotificationClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )
    }

    // Transaction Detail Modal Bottom Sheet
    selectedTransactionForDetail?.let { transaction ->
        TransactionDetailBottomSheet(
            transaction = transaction,
            sheetState = detailSheetState,
            onDismiss = { selectedTransactionForDetail = null },
            onDeleteClick = {
                selectedTransactionForDetail = null
                transactionToDelete = it
            }
        )
    }

    // Confirmation Dialog for Deletion
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_transaction_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_delete_transaction_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        WalletStateHolder.revertTransactionEffect(context, tx)
                        viewModel.deleteTransaction(tx.id)
                        transactionToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

/**
 * 2-Column Bento Summary Cards for Income (Pemasukan) & Expenses (Pengeluaran).
 */
@Composable
fun BentoSummaryCards(
    summary: FinancialSummary,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Income Bento Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MoneyTrackerTheme.colors.income.copy(alpha = 0.14f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = MoneyTrackerTheme.colors.income,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.type_income),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(11.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedRupiahText(
                    targetValue = summary.totalIncome,
                    isLoading = isLoading,
                    initialDelayMillis = 160L,
                    initialDurationMillis = 1400,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Expenses Bento Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.type_expense),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(11.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedRupiahText(
                    targetValue = summary.totalExpense,
                    isLoading = isLoading,
                    initialDelayMillis = 200L,
                    initialDurationMillis = 1400,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private val CounterDecelerationEasing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)

/**
 * Isolated leaf composable for animated Rupiah amounts.
 * Uses a static transparent sizer + [Modifier.matchParentSize] so the animated [Text] is a strict
 * Compose Relayout Boundary (minWidth == maxWidth && minHeight == maxHeight) and NEVER triggers
 * parent layout measurement during the countdown.
 */
@Composable
private fun AnimatedRupiahText(
    targetValue: Long,
    isLoading: Boolean,
    initialDelayMillis: Long,
    initialDurationMillis: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    val animatedAmount = rememberAnimatedAmount(
        targetValue = targetValue,
        isLoading = isLoading,
        initialDelayMillis = initialDelayMillis,
        initialDurationMillis = initialDurationMillis
    )
    val finalTargetText = remember(targetValue) {
        CurrencyFormatter.formatRupiah(targetValue)
    }
    Box(
        modifier = modifier,
        contentAlignment = if (textAlign == TextAlign.Center) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = finalTargetText,
            style = style,
            color = Color.Transparent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = CurrencyFormatter.formatRupiah(animatedAmount),
            style = style,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
private fun AnimatedComparisonBadgeText(
    isSurplus: Boolean,
    isLoading: Boolean,
    initialDelayMillis: Long,
    initialDurationMillis: Int,
    style: TextStyle,
    color: Color
) {
    val animatedWeeklyComparison = rememberAnimatedAmount(
        targetValue = 50_000L,
        isLoading = isLoading,
        initialDelayMillis = initialDelayMillis,
        initialDurationMillis = initialDurationMillis
    )
    val rawComparisonText = if (isSurplus) {
        stringResource(R.string.home_comparison_surplus)
    } else {
        stringResource(R.string.home_comparison_deficit)
    }
    Box(contentAlignment = Alignment.CenterStart) {
        Text(
            text = rawComparisonText,
            style = style,
            color = Color.Transparent,
            maxLines = 1
        )
        Text(
            text = rawComparisonText.replace(
                "Rp 50.000",
                CurrencyFormatter.formatRupiah(animatedWeeklyComparison)
            ),
            style = style,
            color = color,
            maxLines = 1,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
private fun rememberAnimatedAmount(
    targetValue: Long,
    isLoading: Boolean = false,
    initialDelayMillis: Long = 100L,
    initialDurationMillis: Int = 1400,
    updateDurationMillis: Int = 850
): Long {
    var hasPlayedInitial by rememberSaveable { mutableStateOf(false) }
    var startValue by rememberSaveable { mutableLongStateOf(0L) }
    var settledValue by rememberSaveable { mutableLongStateOf(0L) }
    val progress = remember { Animatable(if (hasPlayedInitial) 1f else 0f) }

    val currentDisplayedValue = if (!hasPlayedInitial && (isLoading || targetValue == 0L)) {
        0L
    } else {
        startValue + ((settledValue - startValue) * progress.value.toDouble()).roundToLong()
    }

    LaunchedEffect(targetValue, isLoading) {
        if (isLoading) return@LaunchedEffect

        if (!hasPlayedInitial) {
            if (targetValue != 0L) {
                hasPlayedInitial = true
                startValue = 0L
                settledValue = targetValue
                progress.snapTo(0f)
                // Wait for all 4 pre-composed pager tabs to finish initial composition & layout frames
                repeat(3) {
                    androidx.compose.runtime.withFrameNanos { }
                }
                if (initialDelayMillis > 0L) {
                    delay(initialDelayMillis)
                }
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = initialDurationMillis,
                        easing = CounterDecelerationEasing
                    )
                )
            }
        } else if (targetValue != settledValue) {
            startValue = currentDisplayedValue
            settledValue = targetValue
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = updateDurationMillis,
                    easing = CounterDecelerationEasing
                )
            )
        }
    }

    return currentDisplayedValue
}
