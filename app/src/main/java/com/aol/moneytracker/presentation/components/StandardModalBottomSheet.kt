package com.aol.moneytracker.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Global Modal Bottom Sheet defaults & state architecture ported from Amaya reference:
 * - Locked body scroll (`blockBottomSheetBodyDrag`) so inner scrolling never accidentally drags the sheet
 * - Custom draggable handle & header with spring settle & velocity dismissal
 * - Native Predictive Back support (`ModalBottomSheetProperties(shouldDismissOnBackPress = dismissible)`)
 * - Responsive top corner animation when reaching the status bar
 */
object StandardModalSheetDefaults {
    val MaxHeight: Dp = 720.dp
    val ContentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 66.dp, bottom = 28.dp)
    val ContentWithFooterPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 66.dp, bottom = 96.dp)
    val HeaderHorizontalPadding: Dp = 24.dp
    val HeaderTopPadding: Dp = 8.dp
    val HeaderBottomPadding: Dp = 20.dp
    val DragHandleTopPadding: Dp = 14.dp
    val DragHandleBottomPadding: Dp = 6.dp
    val DragHandleWidth: Dp = 32.dp
    val DragHandleHeight: Dp = 4.dp
    val DragDismissVelocityThreshold: Dp = 125.dp
    const val DragDismissFraction: Float = 0.2f
}

@Stable
@OptIn(ExperimentalMaterial3Api::class)
class StandardModalSheetState internal constructor(
    internal val sheetState: SheetState,
    private val scope: CoroutineScope,
    private val onDismissRequest: () -> Unit,
    private val dismissible: Boolean,
    private val dismissVelocityThreshold: Float
) {
    private var dismissing = false
    private var sheetHeight = 0f
    private var settleJob: Job? = null

    internal var dragOffset by mutableFloatStateOf(0f)
        private set

    fun dismiss(afterDismiss: (() -> Unit)? = null) {
        if (dismissing) return
        dismissing = true
        settleJob?.cancel()
        scope.launch {
            try {
                sheetState.hide()
            } finally {
                onDismissRequest()
                afterDismiss?.invoke()
            }
        }
    }

    internal fun finishDismiss() {
        if (dismissing) return
        dismissing = true
        settleJob?.cancel()
        onDismissRequest()
    }

    internal fun updateSheetHeight(height: Int) {
        sheetHeight = height.toFloat()
    }

    internal fun dragBy(delta: Float): Float {
        if (!dismissible || dismissing) return 0f
        settleJob?.cancel()
        val previousOffset = dragOffset
        dragOffset = (dragOffset + delta).coerceAtLeast(0f)
        return dragOffset - previousOffset
    }

    internal fun settleDrag(velocity: Float) {
        if (dismissing || dragOffset == 0f) return
        val draggedFarEnough = sheetHeight > 0f &&
            dragOffset >= sheetHeight * StandardModalSheetDefaults.DragDismissFraction
        if (dismissible && (draggedFarEnough || velocity >= dismissVelocityThreshold)) {
            dismiss()
            return
        }

        settleJob?.cancel()
        settleJob = scope.launch {
            animate(
                initialValue = dragOffset,
                targetValue = 0f,
                initialVelocity = velocity,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { value, _ -> dragOffset = value }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberStandardModalSheetState(
    onDismissRequest: () -> Unit,
    dismissible: Boolean = true
): StandardModalSheetState {
    val currentOnDismissRequest = rememberUpdatedState(onDismissRequest)
    val density = LocalDensity.current
    val dismissVelocityThreshold = with(density) {
        StandardModalSheetDefaults.DragDismissVelocityThreshold.toPx()
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { dismissible || it != SheetValue.Hidden }
    )
    val scope = rememberCoroutineScope()
    return remember(sheetState, scope, dismissible, dismissVelocityThreshold) {
        StandardModalSheetState(
            sheetState = sheetState,
            scope = scope,
            onDismissRequest = { currentOnDismissRequest.value() },
            dismissible = dismissible,
            dismissVelocityThreshold = dismissVelocityThreshold
        )
    }
}

private val BlockBottomSheetBodyDragConnection = object : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = available

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}

@Composable
internal fun Modifier.blockBottomSheetBodyDrag(): Modifier {
    val bodyDragState = rememberDraggableState { }
    return draggable(
        state = bodyDragState,
        orientation = Orientation.Vertical
    ).nestedScroll(BlockBottomSheetBodyDragConnection)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun standardModalBottomSheetProperties(dismissible: Boolean = true) = ModalBottomSheetProperties(
    shouldDismissOnBackPress = dismissible
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun responsiveBottomSheetShape(sheetState: SheetState): Shape {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val isFullWidth by remember(sheetState) {
        derivedStateOf {
            val offset = try { sheetState.requireOffset() } catch (e: Exception) { Float.MAX_VALUE }
            offset <= (statusBarHeight + 1f)
        }
    }

    val cornerSize by animateDpAsState(
        targetValue = if (isFullWidth) 0.dp else 28.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "sheet_corner_animation"
    )
    return RoundedCornerShape(topStart = cornerSize, topEnd = cornerSize)
}

class StandardModalSheetScope internal constructor(
    private val state: StandardModalSheetState
) {
    fun dismiss(afterDismiss: (() -> Unit)? = null) = state.dismiss(afterDismiss)
}

class StandardModalHeaderScope internal constructor(
    private val state: StandardModalSheetState
) {
    fun dismiss(afterDismiss: (() -> Unit)? = null) = state.dismiss(afterDismiss)
}

@Composable
fun ModalTopBarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val isDark = MoneyTrackerTheme.colors.isDark
    val orbColor = if (isDark) Color(0xFF202228).copy(alpha = 0.92f) else Color(0xFFFAFAFC).copy(alpha = 0.96f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.10f)

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = orbColor,
        border = BorderStroke(0.7.dp, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = iconModifier.size(19.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardModalBottomSheet(
    onDismissRequest: () -> Unit,
    title: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    showCloseButton: Boolean = true,
    dismissible: Boolean = true,
    scrollable: Boolean = true,
    actions: @Composable StandardModalHeaderScope.() -> Unit = {},
    footer: (@Composable StandardModalSheetScope.() -> Unit)? = null,
    content: @Composable StandardModalSheetScope.() -> Unit
) {
    val state = rememberStandardModalSheetState(onDismissRequest, dismissible)
    val dragState = rememberDraggableState { delta -> state.dragBy(delta) }
    val dragModifier = Modifier.draggable(
        state = dragState,
        orientation = Orientation.Vertical,
        enabled = dismissible,
        onDragStopped = { velocity -> state.settleDrag(velocity) }
    )

    val modalTopScrollTailBrush = remember(containerColor) {
        Brush.verticalGradient(
            0.00f to containerColor.copy(alpha = 1.00f),
            0.18f to containerColor.copy(alpha = 0.88f),
            0.38f to containerColor.copy(alpha = 0.68f),
            0.58f to containerColor.copy(alpha = 0.45f),
            0.76f to containerColor.copy(alpha = 0.24f),
            0.90f to containerColor.copy(alpha = 0.08f),
            1.00f to Color.Transparent
        )
    }

    val modalBottomStickyFadeScrim = remember(containerColor) {
        Brush.verticalGradient(
            0.00f to Color.Transparent,
            0.18f to containerColor.copy(alpha = 0.42f),
            0.46f to containerColor.copy(alpha = 0.86f),
            0.72f to containerColor.copy(alpha = 0.98f),
            1.00f to containerColor
        )
    }

    val modalBottomSubtleScrim = remember(containerColor) {
        Brush.verticalGradient(
            0.00f to Color.Transparent,
            0.55f to containerColor.copy(alpha = 0.68f),
            1.00f to containerColor
        )
    }

    val scrollState = rememberScrollState()
    var nestedScrolledPx by remember { mutableFloatStateOf(0f) }
    val sheetScrollTracker = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0.5f) {
                    nestedScrolledPx = 0f
                } else if (consumed.y != 0f) {
                    nestedScrolledPx = (nestedScrolledPx - consumed.y).coerceAtLeast(0f)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y > 10f) {
                    nestedScrolledPx = 0f
                }
                return Velocity.Zero
            }
        }
    }

    val isTopScrolled = if (scrollable) {
        scrollState.canScrollBackward
    } else {
        nestedScrolledPx > 4f
    }
    val topFadeAlpha by animateFloatAsState(
        targetValue = if (isTopScrolled) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "SheetTopFadeAlpha"
    )

    ModalBottomSheet(
        onDismissRequest = state::finishDismiss,
        modifier = Modifier
            .onSizeChanged { state.updateSheetHeight(it.height) }
            .graphicsLayer { translationY = state.dragOffset },
        sheetState = state.sheetState,
        properties = standardModalBottomSheetProperties(dismissible),
        containerColor = containerColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(dragModifier)
                    .padding(
                        top = StandardModalSheetDefaults.DragHandleTopPadding,
                        bottom = StandardModalSheetDefaults.DragHandleBottomPadding
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(StandardModalSheetDefaults.DragHandleWidth)
                        .height(StandardModalSheetDefaults.DragHandleHeight)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        },
        shape = responsiveBottomSheetShape(state.sheetState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = StandardModalSheetDefaults.MaxHeight)
                .navigationBarsPadding()
                .nestedScroll(sheetScrollTracker)
        ) {
            val effectiveContentPadding = if (footer != null) {
                StandardModalSheetDefaults.ContentWithFooterPadding
            } else {
                StandardModalSheetDefaults.ContentPadding
            }

            if (scrollable) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blockBottomSheetBodyDrag()
                        .verticalScroll(scrollState)
                        .padding(effectiveContentPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StandardModalSheetScope(state).content()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blockBottomSheetBodyDrag()
                ) {
                    StandardModalSheetScope(state).content()
                }
            }

            // Sticky Top Header + Scroll-Reactive Feather Fade Below Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(dragModifier)
                        .background(containerColor)
                        .padding(horizontal = StandardModalSheetDefaults.HeaderHorizontalPadding)
                        .padding(
                            top = StandardModalSheetDefaults.HeaderTopPadding,
                            bottom = 10.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StandardModalHeaderScope(state).actions()
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (showCloseButton) {
                        ModalTopBarButton(
                            icon = Icons.Default.Close,
                            onClick = state::dismiss,
                            contentDescription = stringResource(R.string.action_close),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                if (topFadeAlpha > 0.005f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .graphicsLayer { alpha = topFadeAlpha }
                            .background(modalTopScrollTailBrush)
                    )
                }
            }

            // Sticky Bottom Footer + Gradient Fade Scrim
            if (footer != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(94.dp)
                        .align(Alignment.BottomCenter)
                        .background(modalBottomStickyFadeScrim)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    StandardModalSheetScope(state).footer()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .align(Alignment.BottomCenter)
                        .background(modalBottomSubtleScrim)
                )
            }
        }
    }
}

/**
 * Reusable Trigger Row for selecting a Bank or E-Wallet (opens `FinInstitutionSelectorSheet`).
 * Only shown when category is `FinCategory.BANK` or `FinCategory.EWALLET` (hidden for `FinCategory.CASH`).
 */
@Composable
fun FinInstitutionSelectorTrigger(
    selectedInstitution: FinInstitution,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FinLogoCircleBadge(
                idOrName = selectedInstitution.id,
                size = 38.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedInstitution.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (selectedInstitution.category == FinCategory.EWALLET) {
                        "Ketuk untuk ganti E-Wallet"
                    } else {
                        "Ketuk untuk ganti Bank"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Pilih",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bank / E-Wallet Selector Bottom Sheet built on Amaya's `ModelSelectorSheet` + `StandardModalBottomSheet`.
 */
@Composable
fun FinInstitutionSelectorSheet(
    category: FinCategory,
    selectedInstitutionId: String,
    onSelect: (FinInstitution) -> Unit,
    onDismiss: () -> Unit
) {
    val options = remember(category) { FinLogoMapping.byCategory(category) }
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(options, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) options else options.filter {
            it.displayName.lowercase().contains(q) || it.id.lowercase().contains(q)
        }
    }

    val sheetTitle = when (category) {
        FinCategory.EWALLET -> "Pilih E-Wallet"
        FinCategory.BANK -> "Pilih Bank"
        FinCategory.CASH -> "Dompet Tunai"
    }

    val groupLabel = when (category) {
        FinCategory.EWALLET -> "DOMPET DIGITAL POPULER"
        FinCategory.BANK -> "BANK INDONESIA POPULER"
        FinCategory.CASH -> "TUNAI"
    }

    StandardModalBottomSheet(
        onDismissRequest = onDismiss,
        title = sheetTitle,
        scrollable = false,
        actions = {
            ModalTopBarButton(
                icon = Icons.Default.Search,
                onClick = { showSearch = !showSearch },
                contentDescription = stringResource(R.string.cd_search_transactions)
            )
        }
    ) {
        val closeWithSelection: (FinInstitution) -> Unit = { inst ->
            dismiss { onSelect(inst) }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = StandardModalSheetDefaults.ContentPadding,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showSearch) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                if (category == FinCategory.EWALLET) "Cari e-wallet..." else "Cari nama bank..."
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            val groupTitle = when (category) {
                FinCategory.EWALLET -> "Dompet Digital Populer"
                FinCategory.BANK -> "Bank Indonesia Populer"
                FinCategory.CASH -> "Tunai"
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = "Tidak ditemukan hasil yang cocok.",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item(key = "header_$category") {
                    Text(
                        text = groupTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
                    )
                }

                item(key = "group_$category") {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(
                            0.8.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            filtered.forEachIndexed { index, inst ->
                                val isActive = inst.id == selectedInstitutionId
                                Surface(
                                    onClick = { closeWithSelection(inst) },
                                    color = if (isActive) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    } else {
                                        Color.Transparent
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FinLogoCircleBadge(
                                            idOrName = inst.id,
                                            size = 36.dp
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = inst.displayName,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 15.sp
                                                ),
                                                color = if (isActive) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (index < filtered.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        thickness = 0.7.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
