package com.aol.moneytracker.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Symmetrical Floating Navigation Bar:
 * - Top Icon + Bottom Text Always Visible (64.dp height, responsive width up to 440.dp)
 *   so text never truncates on any device.
 * - Smooth sliding active indicator pill & horizontal drag-to-switch logic adapted from Amaya's
 *   `FloatingPillTabBar`:
 *   1. Sliding active background pill drawn in `drawBehind` (Draw phase only — 0 recompositions
 *      and 0 re-layouts of FloatingBottomBar during swipe/drag).
 *   2. Smooth horizontal drag across the bar (`detectDragGestures`) that finds the closest tab
 *      and animates the pager to it in real time.
 *   3. Smooth manual tap & screen swipe synchronization.
 */
@Composable
fun FloatingBottomBar(
    pagerState: PagerState,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onDragStart: () -> Unit = {},
    onDragMove: (Offset) -> Unit = {},
    onDragEnd: (Boolean) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val destinations = remember { NavDestination.items }
    var tabLayouts by remember { mutableStateOf(List(destinations.size) { Rect.Zero }) }
    val activePillColor = MaterialTheme.colorScheme.primaryContainer

    // 64dp center slot gives comfortable breathing room on both sides of the elevated (+) button
    val centerSpacerWidth = 64.dp
    val innerHorizontalPadding = 6.dp

    val currentOnAddClick by rememberUpdatedState(onAddClick)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragMove by rememberUpdatedState(onDragMove)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Floating Capsule Nav Pill (64dp height, responsive width up to 440dp, CircleShape)
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .height(64.dp)
                .pointerInput(Unit) {
                    var lastDraggedTab = -1
                    detectDragGestures(
                        onDragStart = { lastDraggedTab = pagerState.currentPage },
                        onDragEnd = { lastDraggedTab = -1 },
                        onDragCancel = { lastDraggedTab = -1 }
                    ) { change, _ ->
                        change.consume()
                        val x = change.position.x - innerHorizontalPadding.toPx()
                        var closest = 0
                        var minDistance = Float.MAX_VALUE
                        tabLayouts.forEachIndexed { i, rect ->
                            if (rect != Rect.Zero) {
                                val dist = abs(rect.center.x - x)
                                if (dist < minDistance) {
                                    minDistance = dist
                                    closest = i
                                }
                            }
                        }
                        if (closest != lastDraggedTab && pagerState.targetPage != closest) {
                            lastDraggedTab = closest
                            coroutineScope.launch { pagerState.animateScrollToPage(closest) }
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = innerHorizontalPadding, vertical = 5.dp)
                    .drawBehind {
                        // Smooth sliding active pill indicator rendered in Draw phase (0 recomposition / re-layout)
                        val currentTab = pagerState.currentPage
                        val fraction = pagerState.currentPageOffsetFraction
                        val actualNextTab = if (fraction > 0f) {
                            minOf(currentTab + 1, destinations.lastIndex)
                        } else {
                            maxOf(currentTab - 1, 0)
                        }

                        val currentRect = tabLayouts.getOrNull(currentTab) ?: Rect.Zero
                        val nextRect = tabLayouts.getOrNull(actualNextTab) ?: Rect.Zero

                        if (currentRect != Rect.Zero && nextRect != Rect.Zero) {
                            val t = abs(fraction)
                            val bgLeft = currentRect.left + (nextRect.left - currentRect.left) * t
                            val bgWidth = currentRect.width + (nextRect.width - currentRect.width) * t
                            val cornerRadius = size.height / 2f
                            drawRoundRect(
                                color = activePillColor,
                                topLeft = Offset(bgLeft, 0f),
                                size = Size(bgWidth, size.height),
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                            )
                        }
                    }
            ) {
                // Single Row containing all 4 tabs + center "Catat" slot in the same coordinate space
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Section: Beranda (0) & Dompet (1)
                    for (i in 0..1) {
                        if (i < destinations.size) {
                            if (i > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            NavTabButton(
                                destination = destinations[i],
                                index = i,
                                pagerState = pagerState,
                                onPositioned = { rect ->
                                    if (tabLayouts[i] != rect) {
                                        val newLayouts = tabLayouts.toMutableList()
                                        newLayouts[i] = rect
                                        tabLayouts = newLayouts
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                            )
                        }
                    }

                    // Symmetrical Center Slot for the elevated (+) button + aligned "Catat" label
                    Column(
                        modifier = Modifier
                            .width(centerSpacerWidth)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { currentOnAddClick() }
                            )
                            .padding(horizontal = 2.dp, vertical = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.nav_record),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.5.sp,
                                lineHeight = 12.sp,
                                letterSpacing = (-0.1).sp
                            ),
                            color = if (isExpanded) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Right Section: Aktivitas (2) & Laporan (3)
                    for (i in 2..3) {
                        if (i < destinations.size) {
                            if (i > 2) {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            NavTabButton(
                                destination = destinations[i],
                                index = i,
                                pagerState = pagerState,
                                onPositioned = { rect ->
                                    if (tabLayouts[i] != rect) {
                                        val newLayouts = tabLayouts.toMutableList()
                                        newLayouts[i] = rect
                                        tabLayouts = newLayouts
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. Separate Center Elevated Action Button (raised so "Catat" sits cleanly underneath)
        val addIconRotation by animateFloatAsState(
            targetValue = if (isExpanded) 45f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "addIconRotation"
        )
        val buttonBackgroundColor by animateColorAsState(
            targetValue = if (isExpanded) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primary,
            animationSpec = tween(durationMillis = 200),
            label = "buttonBackgroundColor"
        )
        val buttonIconColor by animateColorAsState(
            targetValue = if (isExpanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
            animationSpec = tween(durationMillis = 200),
            label = "buttonIconColor"
        )

        Box(
            modifier = Modifier
                .offset(y = (-37).dp)
                .size(54.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(buttonBackgroundColor)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    val touchSlop = viewConfiguration.touchSlop
                    val holdTimeoutMs = 180L
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        var isHoldActive = false
                        var isGestureDone = false
                        var maxDistance = 0f

                        val holdJob = coroutineScope.launch {
                            kotlinx.coroutines.delay(holdTimeoutMs)
                            if (!isGestureDone && !isHoldActive) {
                                isHoldActive = true
                                currentOnDragStart()
                            }
                        }

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                if (!change.pressed) {
                                    change.consume()
                                    isGestureDone = true
                                    holdJob.cancel()
                                    if (isHoldActive) {
                                        currentOnDragEnd(maxDistance > touchSlop)
                                    } else {
                                        currentOnAddClick()
                                    }
                                    break
                                }

                                val dist = (change.position - down.position).getDistance()
                                if (dist > maxDistance) maxDistance = dist

                                if (!isHoldActive && dist > touchSlop) {
                                    holdJob.cancel()
                                    isHoldActive = true
                                    currentOnDragStart()
                                }

                                if (isHoldActive) {
                                    change.consume()
                                    currentOnDragMove(change.position - center)
                                }
                            }
                        } finally {
                            isGestureDone = true
                            holdJob.cancel()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(if (isExpanded) R.string.cd_close_menu else R.string.cd_add_transaction),
                tint = buttonIconColor,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { rotationZ = addIconRotation }
            )
        }
    }
}

@Composable
private fun NavTabButton(
    destination: NavDestination,
    index: Int,
    pagerState: PagerState,
    onPositioned: (Rect) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabTitle = stringResource(destination.titleRes)
    val isSelected by remember(pagerState, index) {
        androidx.compose.runtime.derivedStateOf {
            abs((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction) < 0.5f
        }
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                onPositioned(coords.boundsInParent())
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top: Smoothly crossfading icon (offset read strictly in draw phase)
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = destination.unselectedIcon,
                    contentDescription = tabTitle,
                    tint = contentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            val pageOffset = pagerState.currentPage - index + pagerState.currentPageOffsetFraction
                            val visibilityFraction = (1f - abs(pageOffset)).coerceIn(0f, 1f)
                            alpha = (1f - visibilityFraction * 1.5f).coerceIn(0f, 1f)
                        }
                )
                Icon(
                    imageVector = destination.selectedIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            val pageOffset = pagerState.currentPage - index + pagerState.currentPageOffsetFraction
                            val visibilityFraction = (1f - abs(pageOffset)).coerceIn(0f, 1f)
                            alpha = ((visibilityFraction - 0.2f) * 1.25f).coerceIn(0f, 1f)
                        }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Bottom: Always-visible label text
            Text(
                text = tabTitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.5.sp,
                    lineHeight = 12.sp,
                    letterSpacing = (-0.1).sp
                ),
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                textAlign = TextAlign.Center
            )
        }
    }
}
