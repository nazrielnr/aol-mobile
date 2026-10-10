package com.aol.moneytracker.presentation.components

import android.os.SystemClock
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme

private const val TAG = "DROPDOWN_TRACE"

/**
 * Universal Reusable Global Dropdown Menu:
 * - Fluid, continuous open & close animation (scale + fade + subtle vertical slide) that can
 *   reverse smoothly mid-flight when tapped rapidly.
 * - Full 360-degree (top, bottom, left, right) multi-layer feathered RoundRect shadow with
 *   generous Popup bleed padding so no side is ever clipped and no sharp corner box ever appears.
 * - Built-in Logcat tracing ("DROPDOWN_TRACE") for trigger taps, outside dismiss, and item selection.
 */
@Composable
fun <T> AppDropdownMenu(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    dropdownName: String = "SelectorDropdown",
    itemLabel: (T) -> String = { it.toString() },
    itemIcon: (@Composable (item: T, isSelected: Boolean) -> Unit)? = null,
    triggerLeadingIcon: (@Composable () -> Unit)? = null,
    triggerShape: Shape = CircleShape,
    triggerHeight: Dp = 38.dp,
    triggerContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    triggerBorderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    menuAlignment: Alignment = Alignment.TopEnd,
    menuWidth: Dp = 196.dp,
    popupOffsetY: Dp = 44.dp
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "arrowRotation"
    )

    AppDropdownContainer(
        expanded = expanded,
        onExpandedChange = { newExpanded ->
            expanded = newExpanded
        },
        dropdownName = dropdownName,
        menuAlignment = menuAlignment,
        menuWidth = menuWidth,
        popupOffsetY = popupOffsetY,
        modifier = modifier,
        trigger = { onToggle ->
            Surface(
                onClick = onToggle,
                shape = triggerShape,
                color = triggerContainerColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = triggerBorderColor
                ),
                modifier = Modifier.height(triggerHeight)
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = if (triggerLeadingIcon != null) 10.dp else 14.dp,
                        end = 12.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (triggerLeadingIcon != null) {
                        triggerLeadingIcon()
                    }

                    Text(
                        text = itemLabel(selectedItem),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer { rotationZ = arrowRotation }
                    )
                }
            }
        }
    ) { closeDropdown ->
        items.forEach { item ->
            val isSelected = item == selectedItem

            val itemBgColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            }

            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            Surface(
                onClick = {
                    Log.d(TAG, "[$dropdownName] Item selected: '${itemLabel(item)}' -> closing")
                    closeDropdown()
                    onItemSelected(item)
                },
                shape = RoundedCornerShape(14.dp),
                color = itemBgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (itemIcon != null) {
                        itemIcon(item, isSelected)
                    }
                    Text(
                        text = itemLabel(item),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * Global Dropdown Action Item for custom dropdowns like the Profile Menu in [GlobalTopBar].
 */
@Composable
fun AppDropdownActionItem(
    label: String,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    isHighlighted: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isHighlighted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            Color.Transparent
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leadingIcon()
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Global Dropdown Container used by both [AppDropdownMenu] and the Profile Dropdown in [GlobalTopBar].
 *
 * Solves:
 * 1. All-around 360° shadow (top, bottom, left, right):
 *    Draws a 14-layer feathered RoundRect shadow extending 16.dp above the top edge, 18.dp on left/right,
 *    and 20.dp below the bottom edge inside a 26.dp top / 28.dp horizontal / 32.dp bottom padded Popup.
 * 2. Smooth open AND close animation without offscreen hardware layer artifacts:
 *    Animates a continuous [Animatable] `progress` (0f <-> 1f) that modulates shadow alpha directly
 *    in Canvas (no offscreen layer clipping box!) and smoothly animates scale + fade + slide on both open and close.
 * 3. Instant rapid-tap response:
 *    Sets `PopupProperties(focusable = expanded)` so the moment `expanded` becomes `false`, the closing
 *    Popup stops intercepting touches, allowing immediate re-opening and mid-flight animation reversal.
 */
@Composable
fun AppDropdownContainer(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    dropdownName: String,
    modifier: Modifier = Modifier,
    menuAlignment: Alignment = Alignment.TopStart,
    menuWidth: Dp = 240.dp,
    popupOffsetY: Dp = 44.dp,
    trigger: @Composable (onToggle: () -> Unit) -> Unit,
    content: @Composable ColumnScope.(closeDropdown: () -> Unit) -> Unit
) {
    val density = LocalDensity.current
    val shadowPadHorizontal = 28.dp
    val shadowPadTop = 26.dp
    val shadowPadBottom = 32.dp

    val shadowPadHorizontalPx = with(density) { shadowPadHorizontal.roundToPx() }
    val shadowPadTopPx = with(density) { shadowPadTop.roundToPx() }
    val popupOffsetYPx = with(density) { popupOffsetY.roundToPx() }
    val slideOffsetPx = with(density) { 8.dp.toPx() }

    // Compensate Popup offset for the transparent shadow padding so the visible card aligns accurately
    val adjustedOffsetX = when (menuAlignment) {
        Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> -shadowPadHorizontalPx
        Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> shadowPadHorizontalPx
        else -> 0
    }
    val adjustedOffsetY = popupOffsetYPx - shadowPadTopPx

    val pivotX = when (menuAlignment) {
        Alignment.TopEnd, Alignment.BottomEnd, Alignment.CenterEnd -> 1f
        Alignment.TopCenter, Alignment.BottomCenter, Alignment.Center -> 0.5f
        else -> 0f
    }

    var lastDismissUptimeMs by remember { mutableLongStateOf(0L) }
    var tapCount by remember { mutableIntStateOf(0) }

    // Continuous 0f..1f animation progress for silky-smooth open AND close transitions
    val progressAnim = remember { Animatable(0f) }
    var isPopupMounted by remember { mutableStateOf(false) }

    if (expanded && !isPopupMounted) {
        isPopupMounted = true
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            isPopupMounted = true
            Log.d(
                TAG,
                "[$dropdownName] Animating OPEN from progress=${"%.2f".format(progressAnim.value)} -> 1.0"
            )
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 165, easing = LinearOutSlowInEasing)
            )
        } else if (isPopupMounted) {
            Log.d(
                TAG,
                "[$dropdownName] Animating CLOSE from progress=${"%.2f".format(progressAnim.value)} -> 0.0"
            )
            progressAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 135, easing = FastOutLinearInEasing)
            )
            isPopupMounted = false
            Log.d(TAG, "[$dropdownName] Popup UNMOUNTED after smooth close")
        }
    }

    Box(modifier = modifier) {
        trigger {
            val now = SystemClock.uptimeMillis()
            val sinceDismiss = now - lastDismissUptimeMs
            tapCount++
            // Guard against same-gesture ACTION_DOWN (onDismissRequest) + ACTION_UP (onClick) double-fire
            if (!expanded && sinceDismiss in 0L..75L) {
                Log.d(
                    TAG,
                    "[$dropdownName] Trigger onClick #$tapCount IGNORED (just closed by onDismissRequest ${sinceDismiss}ms ago)"
                )
            } else {
                val nextState = !expanded
                Log.d(
                    TAG,
                    "[$dropdownName] Trigger onClick #$tapCount: expanded $expanded -> $nextState (sinceDismiss=${sinceDismiss}ms)"
                )
                onExpandedChange(nextState)
            }
        }

        if (isPopupMounted) {
            Popup(
                alignment = menuAlignment,
                offset = IntOffset(x = adjustedOffsetX, y = adjustedOffsetY),
                onDismissRequest = {
                    lastDismissUptimeMs = SystemClock.uptimeMillis()
                    Log.d(
                        TAG,
                        "[$dropdownName] onDismissRequest -> closing (uptime=$lastDismissUptimeMs)"
                    )
                    onExpandedChange(false)
                },
                properties = PopupProperties(
                    // Only focusable while expanded == true so a closing popup NEVER blocks rapid re-open taps
                    focusable = expanded,
                    clippingEnabled = false
                )
            ) {
                val progress = progressAnim.value
                val scale = 0.88f + (0.12f * progress)
                val translateY = (1f - progress) * (-slideOffsetPx)

                val isDark = MoneyTrackerTheme.colors.isDark
                val cornerRadiusDp = 22.dp
                val menuShape = RoundedCornerShape(cornerRadiusDp)
                val containerColor = if (isDark) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                }
                val borderColor = if (isDark) {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f * progress)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f * progress)
                }
                val baseShadowAlpha = (if (isDark) 0.22f else 0.065f) * progress

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationY = translateY
                            transformOrigin = TransformOrigin(pivotFractionX = pivotX, pivotFractionY = 0f)
                            clip = false
                        }
                        .padding(
                            start = shadowPadHorizontal,
                            end = shadowPadHorizontal,
                            top = shadowPadTop,
                            bottom = shadowPadBottom
                        )
                        .drawBehind {
                            // Subtle, soft all-around (top, bottom, left, right) feathered RoundRect shadow.
                            if (baseShadowAlpha > 0.001f) {
                                val baseCornerPx = cornerRadiusDp.toPx()
                                val layers = 12
                                for (i in layers downTo 1) {
                                    val fraction = i.toFloat() / layers.toFloat()
                                    val spreadPx = fraction * 12.dp.toPx()
                                    val gravityShiftYPx = fraction * 1.5.dp.toPx()
                                    val falloff = (1f - fraction * 0.85f)
                                    val layerAlpha = (baseShadowAlpha * falloff * falloff) / 4.2f
                                    drawRoundRect(
                                        color = Color.Black.copy(alpha = layerAlpha.coerceIn(0f, 1f)),
                                        topLeft = Offset(
                                            x = -spreadPx,
                                            y = -spreadPx + gravityShiftYPx
                                        ),
                                        size = Size(
                                            width = size.width + spreadPx * 2f,
                                            height = size.height + spreadPx * 2f
                                        ),
                                        cornerRadius = CornerRadius(
                                            x = baseCornerPx + spreadPx,
                                            y = baseCornerPx + spreadPx
                                        )
                                    )
                                }
                            }
                        }
                ) {
                    Surface(
                        shape = menuShape,
                        color = containerColor,
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                        border = BorderStroke(width = 1.dp, color = borderColor),
                        modifier = Modifier
                            .width(menuWidth)
                            .graphicsLayer {
                                alpha = progress
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            content {
                                Log.d(TAG, "[$dropdownName] Closed by menu action")
                                onExpandedChange(false)
                            }
                        }
                    }
                }
            }
        }
    }
}
