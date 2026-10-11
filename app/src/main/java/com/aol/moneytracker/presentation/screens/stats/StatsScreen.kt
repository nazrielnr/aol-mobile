package com.aol.moneytracker.presentation.screens.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.components.AppDropdownMenu
import com.aol.moneytracker.presentation.components.EmptyStateView
import com.aol.moneytracker.presentation.components.TopFadeScrim
import com.aol.moneytracker.presentation.components.getCategoryColor
import com.aol.moneytracker.presentation.components.getCategoryIcon
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter
import com.aol.moneytracker.util.LanguageManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Modern Financial Analytics & Stats Screen:
 * - Transparent TopBar & StatusBar area with TopFadeScrim gradient (matching bottom nav scrim)
 * - Semi-kotak donut segments (CornerPathEffect rounded fillets, clean 4.5-degree spacing, NO outline borders)
 * - Fluid spring transitions: selected segment expands outward, shrinks back on deselect
 * - Tap anywhere on screen / background to dismiss active selection
 * - Rounded cards with clipped ripples (zero square bleed)
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier,
    selectedMonth: String = "",
    months: List<String> = emptyList(),
    onMonthSelected: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var isDonutView by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 52.dp
    val topScrimHeight = statusBarHeight + topBarHeight + 48.dp
    val listState = rememberLazyListState()

    val currentCategories = if (selectedType == TransactionType.EXPENSE) {
        uiState.expenseCategories
    } else {
        uiState.incomeCategories
    }

    val totalAmount = if (selectedType == TransactionType.EXPENSE) {
        uiState.summary.totalExpense
    } else {
        uiState.summary.totalIncome
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                // Tapping anywhere on the screen background deselects the active segment
                detectTapGestures {
                    selectedCategoryId = null
                }
            }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = statusBarHeight + topBarHeight + 12.dp,
                bottom = 135.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. REPORT HEADER ROW (Type Pill Action Dropdown & Chart View Toggle)
            item(key = "report_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppDropdownMenu(
                        items = TransactionType.entries,
                        selectedItem = selectedType,
                        onItemSelected = { newType ->
                            selectedType = newType
                            selectedCategoryId = null
                        },
                        dropdownName = "StatsTypeDropdown",
                        itemLabel = { context.getString(it.labelRes) },
                        itemIcon = { type, isSelected ->
                            val typeIcon = when (type) {
                                TransactionType.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown
                                TransactionType.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
                            }

                            val iconTint = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                type == TransactionType.INCOME -> MoneyTrackerTheme.colors.income
                                else -> MaterialTheme.colorScheme.error
                            }

                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        triggerLeadingIcon = null,
                        triggerShape = CircleShape,
                        triggerHeight = 38.dp,
                        triggerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        triggerBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        menuAlignment = Alignment.TopStart,
                        menuWidth = 175.dp
                    )

                    // Bar vs Donut Chart View Toggle Pill
                    ChartViewToggle(
                        isDonutView = isDonutView,
                        onToggle = { isDonutView = it }
                    )
                }
            }

            // 3. CHART VISUALIZATION (Semi-Kotak Donut Chart or Bar Chart)
            item(key = "chart_visualizer") {
                if (currentCategories.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        EmptyStateView(
                            message = if (selectedType == TransactionType.EXPENSE) {
                                stringResource(R.string.stats_empty_expense)
                            } else {
                                stringResource(R.string.stats_empty_income)
                            }
                        )
                    }
                } else {
                    if (isDonutView) {
                        ModernDonutChart(
                            categories = currentCategories,
                            totalAmount = totalAmount,
                            isExpense = selectedType == TransactionType.EXPENSE,
                            selectedCategoryId = selectedCategoryId,
                            onCategorySelect = { catId ->
                                selectedCategoryId = if (selectedCategoryId == catId) null else catId
                            },
                            onDeselect = {
                                selectedCategoryId = null
                            }
                        )
                    } else {
                        ModernBarChart(
                            categories = currentCategories,
                            totalAmount = totalAmount,
                            isExpense = selectedType == TransactionType.EXPENSE,
                            selectedCategoryId = selectedCategoryId,
                            onCategorySelect = { catId ->
                                selectedCategoryId = if (selectedCategoryId == catId) null else catId
                            }
                        )
                    }
                }
            }

            // 4. "ALL EXPENSES" / "ALL INCOME" SECTION HEADER
            item(key = "all_items_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedType == TransactionType.EXPENSE) {
                            stringResource(R.string.stats_all_expenses)
                        } else {
                            stringResource(R.string.stats_all_income)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.stats_total_label) + "  ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatRupiah(totalAmount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 5. DETAILED CATEGORY BREAKDOWN CARDS
            itemsIndexed(
                items = currentCategories,
                key = { _, stat -> stat.categoryId }
            ) { _, stat ->
                val categoryColor = getCategoryColor(stat.categoryId)
                val isSelected = selectedCategoryId == stat.categoryId

                CategoryBreakdownCard(
                    stat = stat,
                    color = categoryColor,
                    isSelected = isSelected,
                    type = selectedType,
                    onClick = {
                        selectedCategoryId = if (isSelected) null else stat.categoryId
                    }
                )
            }
        }

        // Top Fade Scrim: Transparent gradient fade just like the bottom nav area
        TopFadeScrim(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(5f),
            height = topScrimHeight,
            isScrolled = listState.canScrollBackward
        )

        // Stats TopBar (slides together with StatsScreen during pager swipe)
        StatsTopBar(
            months = months,
            selectedMonth = selectedMonth,
            onMonthSelected = onMonthSelected,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )
    }
}

/**
 * Pinned TopBar for StatsScreen.
 * Slides naturally together with the screen during horizontal pager swiping.
 */
@Composable
fun StatsTopBar(
    months: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(52.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (months.isNotEmpty()) {
                    AppDropdownMenu(
                        items = months,
                        selectedItem = selectedMonth,
                        onItemSelected = onMonthSelected,
                        dropdownName = "StatsMonthDropdown",
                        triggerShape = CircleShape,
                        triggerHeight = 38.dp,
                        triggerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        triggerBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

/**
 * Chart View Toggle: Bar Chart vs Donut Chart
 */
@Composable
fun ChartViewToggle(
    isDonutView: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bar Chart Button
            Surface(
                onClick = { onToggle(false) },
                shape = CircleShape,
                color = if (!isDonutView) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = stringResource(R.string.cd_bar_chart),
                        tint = if (!isDonutView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Donut Chart Button
            Surface(
                onClick = { onToggle(true) },
                shape = CircleShape,
                color = if (isDonutView) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PieChart,
                        contentDescription = stringResource(R.string.cd_donut_chart),
                        tint = if (isDonutView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

/**
 * Segmented Donut Chart component:
 * - Annular sectors with CornerPathEffect subtle corner rounding (semi-kotak, zero borders)
 * - 4.5-degree clean gap between categories
 * - Fluid spring scale transition when enlarging/shrinking
 * - Percentage badge only shown when a slice is pressed/selected
 * - Tap anywhere outside to dismiss
 */
@Composable
fun ModernDonutChart(
    categories: List<CategoryStat>,
    totalAmount: Long,
    isExpense: Boolean,
    selectedCategoryId: String?,
    onCategorySelect: (String) -> Unit,
    onDeselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = categories.sumOf { it.totalAmount }.coerceAtLeast(1L)
    val otherLabel = stringResource(R.string.category_other_expense)

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donut_draw"
    )

    // For clean, balanced slices matching reference design: Top 5 categories + Other
    val displaySlices = remember(categories, otherLabel) {
        if (categories.size <= 5) {
            categories
        } else {
            val top = categories.take(4)
            val remaining = categories.drop(4)
            val otherAmount = remaining.sumOf { it.totalAmount }
            val otherCount = remaining.sumOf { it.count }
            val otherPercentage = remaining.sumOf { it.percentageOfTotal.toDouble() }.toFloat()
            val otherStat = CategoryStat(
                categoryId = "other",
                categoryName = otherLabel,
                totalAmount = otherAmount,
                count = otherCount,
                percentageOfTotal = otherPercentage,
                changePercentage = 0
            )
            top + otherStat
        }
    }

    val selectedCategory = remember(categories, displaySlices, selectedCategoryId) {
        if (selectedCategoryId != null) {
            categories.find { it.categoryId == selectedCategoryId }
                ?: displaySlices.find { it.categoryId == selectedCategoryId }
        } else {
            null
        }
    }

    val highlightAngleDegrees = remember(displaySlices, selectedCategoryId, total) {
        var angle = -90f
        var foundAngle = 45f
        for (stat in displaySlices) {
            val sweep = (stat.totalAmount.toFloat() / total.toFloat()) * 360f
            if (stat.categoryId == selectedCategoryId) {
                foundAngle = angle + (sweep / 2f)
                break
            }
            angle += sweep
        }
        foundAngle
    }
    val appColors = MoneyTrackerTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(240.dp)
                .pointerInput(displaySlices, total) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val centerOffset = Offset(w / 2f, h / 2f)
                        val dx = tapOffset.x - centerOffset.x
                        val dy = tapOffset.y - centerOffset.y
                        val dist = sqrt(dx * dx + dy * dy)
                        val outerRadiusPx = minOf(w, h) / 2f - 10.dp.toPx()
                        val innerRadiusPx = outerRadiusPx - 28.dp.toPx()

                        // Check if tap fell on the donut ring
                        if (dist in (innerRadiusPx - 10.dp.toPx())..(outerRadiusPx + 14.dp.toPx())) {
                            var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (deg < -90f) deg += 360f
                            val angleFromTop = (deg + 90f) % 360f

                            var accAngle = 0f
                            for (stat in displaySlices) {
                                val sliceDeg = (stat.totalAmount.toFloat() / total.toFloat()) * 360f
                                if (angleFromTop in accAngle..(accAngle + sliceDeg)) {
                                    onCategorySelect(stat.categoryId)
                                    return@detectTapGestures
                                }
                                accAngle += sliceDeg
                            }
                        } else {
                            // Tapped center hole or outside -> deselect
                            onDeselect()
                        }
                    }
                }
        ) {
            val canvasSize = size.minDimension
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            val baseOuterR = canvasSize / 2f - 14.dp.toPx()
            val thickness = 28.dp.toPx()
            val baseInnerR = baseOuterR - thickness

            var currentAngle = -90f // Start from 12 o'clock
            val gapDeg = if (displaySlices.size > 1) 4.5f else 0f // Generous, clean gap between categories

            val cornerRadiusPx = 5.dp.toPx()

            displaySlices.forEach { stat ->
                val sliceRatio = stat.totalAmount.toFloat() / total.toFloat()
                val fullSweep = sliceRatio * 360f * animProgress

                val isSelected = selectedCategoryId == stat.categoryId

                // Fluid spring transition when expanding or shrinking
                val expandR = if (isSelected) 6.dp.toPx() else 0f
                val contractR = if (isSelected) 2.dp.toPx() else 0f
                val outerR = baseOuterR + expandR
                val innerR = baseInnerR - contractR

                val effectiveSweep = (fullSweep - gapDeg).coerceAtLeast(0.5f)
                val startAngle = currentAngle + (gapDeg / 2f)

                val sliceColor = appColors.categoryColor(stat.categoryId)
                val colorToDraw = if (selectedCategoryId != null && !isSelected) {
                    sliceColor.copy(alpha = 0.45f)
                } else {
                    sliceColor
                }

                // Geometric Rounded Annular Sector:
                // Produces a mathematically true, smooth circle arc with clean rounded rectangle corners
                val sectorPath = buildRoundedSectorPath(
                    center = centerOffset,
                    innerR = innerR,
                    outerR = outerR,
                    startAngleDeg = startAngle,
                    sweepAngleDeg = effectiveSweep,
                    cornerRadius = cornerRadiusPx
                )

                drawPath(path = sectorPath, color = colorToDraw)

                currentAngle += fullSweep
            }
        }

        // Center Content of Donut (Icon, label, and amount centered)
        val activeCategory = if (selectedCategoryId != null && selectedCategory != null) {
            Category.findCategory(selectedCategory.categoryId, if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME)
        } else null

        val iconColor = if (activeCategory != null) {
            getCategoryColor(activeCategory.id)
        } else {
            MaterialTheme.colorScheme.primary
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .offset(y = (-8).dp)
                .padding(horizontal = 30.dp)
        ) {
            // Category Icon in the center
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.16f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (activeCategory != null) {
                            getCategoryIcon(activeCategory.iconKey)
                        } else {
                            if (isExpense) Icons.AutoMirrored.Filled.ReceiptLong else Icons.Default.AccountBalanceWallet
                        },
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val centerTitle = if (selectedCategoryId != null && selectedCategory != null) {
                stringResource(Category.getCategoryNameRes(selectedCategory.categoryId))
            } else {
                if (isExpense) {
                    stringResource(R.string.stats_total_expenses)
                } else {
                    stringResource(R.string.stats_total_income)
                }
            }

            Text(
                text = centerTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            val displayAmount = if (selectedCategoryId != null && selectedCategory != null) {
                selectedCategory.totalAmount
            } else {
                totalAmount
            }

            Text(
                text = CurrencyFormatter.formatRupiah(displayAmount),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Floating Percentage Badge: ONLY shown when a slice is pressed/selected!
        AnimatedVisibility(
            visible = selectedCategoryId != null && selectedCategory != null,
            enter = fadeIn(tween(200)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150))
        ) {
            if (selectedCategory != null) {
                val percentageInt = (selectedCategory.percentageOfTotal * 100).toInt()
                val rad = Math.toRadians(highlightAngleDegrees.toDouble())
                val badgeRadius = 96.dp

                val offsetX = (badgeRadius.value * cos(rad)).dp
                val offsetY = (badgeRadius.value * sin(rad)).dp

                Surface(
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    color = appColors.chartBadgeContainer,
                    border = BorderStroke(1.2.dp, appColors.chartBadgeBorder)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$percentageInt%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = appColors.chartBadgeContent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mathematically builds a true circular annular sector with filleted rounded-rectangle corners.
 * Keeps outer and inner arcs as exact, true circle arcs while smoothing all 4 vertices with tangent quadratic curves.
 */
private fun buildRoundedSectorPath(
    center: Offset,
    innerR: Float,
    outerR: Float,
    startAngleDeg: Float,
    sweepAngleDeg: Float,
    cornerRadius: Float
): Path {
    val path = Path()
    if (sweepAngleDeg <= 0.1f) return path

    val thickness = outerR - innerR
    val sweepRad = Math.toRadians(sweepAngleDeg.toDouble()).toFloat()

    // Clamp cornerRadius so small slices don't collapse or invert
    val maxR = minOf(
        thickness / 2.5f,
        (innerR * sweepRad / 2.5f)
    )
    val r = minOf(cornerRadius, maxR).coerceAtLeast(0f)

    if (r <= 0.5f) {
        path.arcTo(
            rect = Rect(center.x - outerR, center.y - outerR, center.x + outerR, center.y + outerR),
            startAngleDegrees = startAngleDeg,
            sweepAngleDegrees = sweepAngleDeg,
            forceMoveTo = false
        )
        path.arcTo(
            rect = Rect(center.x - innerR, center.y - innerR, center.x + innerR, center.y + innerR),
            startAngleDegrees = startAngleDeg + sweepAngleDeg,
            sweepAngleDegrees = -sweepAngleDeg,
            forceMoveTo = false
        )
        path.close()
        return path
    }

    val alphaOutRad = Math.asin((r / (outerR - r)).coerceIn(0f, 0.99f).toDouble())
    val alphaOutDeg = Math.toDegrees(alphaOutRad).toFloat()

    val alphaInRad = Math.asin((r / (innerR + r)).coerceIn(0f, 0.99f).toDouble())
    val alphaInDeg = Math.toDegrees(alphaInRad).toFloat()

    val dOut = Math.sqrt(((outerR - r) * (outerR - r) - r * r).coerceAtLeast(0f).toDouble()).toFloat()
    val dIn = Math.sqrt(((innerR + r) * (innerR + r) - r * r).coerceAtLeast(0f).toDouble()).toFloat()

    val theta0Deg = startAngleDeg
    val theta1Deg = startAngleDeg + sweepAngleDeg

    val theta0Rad = Math.toRadians(theta0Deg.toDouble())
    val theta1Rad = Math.toRadians(theta1Deg.toDouble())

    val cos0 = Math.cos(theta0Rad).toFloat()
    val sin0 = Math.sin(theta0Rad).toFloat()
    val cos1 = Math.cos(theta1Rad).toFloat()
    val sin1 = Math.sin(theta1Rad).toFloat()

    // 1. Point on radial start line near inner corner
    val pStart = Offset(center.x + dIn * cos0, center.y + dIn * sin0)
    path.moveTo(pStart.x, pStart.y)

    // Straight line along radial start line to outer corner
    val pRadialOut = Offset(center.x + dOut * cos0, center.y + dOut * sin0)
    path.lineTo(pRadialOut.x, pRadialOut.y)

    // Corner 1: Outer Start (Quadratic curve tangent to radial line and outer circle)
    val cp1 = Offset(center.x + outerR * cos0, center.y + outerR * sin0)
    val radArcOutStart = Math.toRadians((theta0Deg + alphaOutDeg).toDouble())
    val pArcOutStart = Offset(
        (center.x + outerR * Math.cos(radArcOutStart)).toFloat(),
        (center.y + outerR * Math.sin(radArcOutStart)).toFloat()
    )
    path.quadraticTo(cp1.x, cp1.y, pArcOutStart.x, pArcOutStart.y)

    // 2. Outer Circular Arc (True circle!)
    val outerArcSweep = sweepAngleDeg - 2f * alphaOutDeg
    if (outerArcSweep > 0.05f) {
        path.arcTo(
            rect = Rect(center.x - outerR, center.y - outerR, center.x + outerR, center.y + outerR),
            startAngleDegrees = theta0Deg + alphaOutDeg,
            sweepAngleDegrees = outerArcSweep,
            forceMoveTo = false
        )
    }

    // Corner 2: Outer End (Quadratic curve tangent to outer circle and radial end line)
    val cp2 = Offset(center.x + outerR * cos1, center.y + outerR * sin1)
    val pRadialEndOut = Offset(center.x + dOut * cos1, center.y + dOut * sin1)
    path.quadraticTo(cp2.x, cp2.y, pRadialEndOut.x, pRadialEndOut.y)

    // 3. Straight line along radial end line down to inner corner
    val pRadialEndIn = Offset(center.x + dIn * cos1, center.y + dIn * sin1)
    path.lineTo(pRadialEndIn.x, pRadialEndIn.y)

    // Corner 3: Inner End (Quadratic curve tangent to radial line and inner circle)
    val cp3 = Offset(center.x + innerR * cos1, center.y + innerR * sin1)
    val radArcInEnd = Math.toRadians((theta1Deg - alphaInDeg).toDouble())
    val pArcInEnd = Offset(
        (center.x + innerR * Math.cos(radArcInEnd)).toFloat(),
        (center.y + innerR * Math.sin(radArcInEnd)).toFloat()
    )
    path.quadraticTo(cp3.x, cp3.y, pArcInEnd.x, pArcInEnd.y)

    // 4. Inner Circular Arc (True circle backwards!)
    val innerArcSweep = sweepAngleDeg - 2f * alphaInDeg
    if (innerArcSweep > 0.05f) {
        path.arcTo(
            rect = Rect(center.x - innerR, center.y - innerR, center.x + innerR, center.y + innerR),
            startAngleDegrees = theta1Deg - alphaInDeg,
            sweepAngleDegrees = -innerArcSweep,
            forceMoveTo = false
        )
    }

    // Corner 4: Inner Start (Quadratic curve back to pStart)
    val cp4 = Offset(center.x + innerR * cos0, center.y + innerR * sin0)
    path.quadraticTo(cp4.x, cp4.y, pStart.x, pStart.y)

    path.close()
    return path
}

/**
 * Modern Bar Chart representation when user toggles to the Bar view
 */
@Composable
fun ModernBarChart(
    categories: List<CategoryStat>,
    totalAmount: Long,
    isExpense: Boolean,
    selectedCategoryId: String?,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val topCategories = categories.take(5)
    val maxAmount = topCategories.maxOfOrNull { it.totalAmount }?.coerceAtLeast(1L) ?: 1L

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpense) {
                        stringResource(R.string.stats_bar_top_expense_title)
                    } else {
                        stringResource(R.string.stats_bar_top_income_title)
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.stats_categories_count_format, topCategories.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Vertical Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                topCategories.forEach { stat ->
                    val categoryColor = getCategoryColor(stat.categoryId)
                    val ratio = (stat.totalAmount.toFloat() / maxAmount.toFloat()).coerceIn(0.08f, 1f)
                    val isSelected = selectedCategoryId == stat.categoryId

                    val animatedHeight by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                        label = "bar_height"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onCategorySelect(stat.categoryId) }
                            )
                    ) {
                        Text(
                            text = "${(stat.percentageOfTotal * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Animated Bar
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height((120 * animatedHeight).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (isSelected) categoryColor else categoryColor.copy(alpha = 0.85f)
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Icon
                        val category = Category.findCategory(
                            stat.categoryId,
                            if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(categoryColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(category.iconKey),
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category Breakdown Card:
 * - Uses Surface(onClick = onClick, shape = RoundedCornerShape(20.dp)) so ripple is 100% clipped inside rounded shape
 * - Vibrant category-tinted squircle icon
 * - Category name & percentage
 * - Formatted Amount & comparison pill ("+12% vs last month")
 * - Rounded horizontal progress bar
 */
@Composable
fun CategoryBreakdownCard(
    stat: CategoryStat,
    color: Color,
    isSelected: Boolean,
    type: TransactionType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = Category.findCategory(stat.categoryId, type)
    val localizedCategoryName = stringResource(Category.getCategoryNameRes(stat.categoryId))

    val animatedProgress by animateFloatAsState(
        targetValue = stat.percentageOfTotal,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "card_progress_${stat.categoryId}"
    )

    // Using Surface(onClick = ...) natively clips the ripple to the 20.dp rounded shape (zero rectangular bleed)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Squircle Icon Container (Vibrant & Category-colored)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(color.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.iconKey),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Name and Percentage of total
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizedCategoryName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val percentageFormatted = if (stat.percentageOfTotal < 0.1f) {
                        String.format(LanguageManager.getActiveLocale(), "%.1f", stat.percentageOfTotal * 100)
                    } else {
                        "${(stat.percentageOfTotal * 100).toInt()}"
                    }
                    Text(
                        text = stringResource(R.string.stats_percentage_of_total_format, percentageFormatted),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount and Comparison pill
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatRupiah(stat.totalAmount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = CircleShape,
                        color = MoneyTrackerTheme.colors.income.copy(alpha = 0.13f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+${stat.changePercentage}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MoneyTrackerTheme.colors.income
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = stringResource(R.string.stats_vs_last_month),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar Matching Category Color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
