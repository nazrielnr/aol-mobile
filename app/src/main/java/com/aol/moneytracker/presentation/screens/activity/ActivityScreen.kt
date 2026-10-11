package com.aol.moneytracker.presentation.screens.activity

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.components.AppDropdownMenu
import com.aol.moneytracker.presentation.components.EmptyStateView
import com.aol.moneytracker.presentation.components.ModernTransactionCard
import com.aol.moneytracker.presentation.components.TopFadeScrim
import com.aol.moneytracker.presentation.components.TransactionDetailBottomSheet
import com.aol.moneytracker.presentation.screens.home.HomeViewModel
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter
import com.aol.moneytracker.util.DateFormatter
import com.aol.moneytracker.util.LanguageManager
import java.util.Calendar
import kotlin.math.abs

private data class DayTransactionSummary(
    val hasIncome: Boolean,
    val hasExpense: Boolean,
    val totalExpense: Long,
    val totalIncome: Long,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    selectedMonth: String = "",
    months: List<String> = emptyList(),
    onMonthSelected: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val context = LocalContext.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 56.dp

    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) } // null = All
    var selectedDay by remember { mutableStateOf<Int?>(null) } // null = All Days (1..31)

    var selectedTransactionForDetail by remember { mutableStateOf<Transaction?>(null) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allPeriodsText = stringResource(R.string.period_all)
    val defaultOctName = stringResource(R.string.month_october)

    // Close expanded search bar on back press; when collapsed, let Android OS Predictive Back API handle directly
    BackHandler(enabled = isSearchExpanded) {
        isSearchExpanded = false
        searchQuery = ""
    }

    // Parse Month and Year from selectedMonth (supports both Indonesian & English month names)
    val (daysInMonth, leadingEmptySlots, monthInt, yearVal, monthName) = remember(selectedMonth, defaultOctName) {
        val parts = selectedMonth.split(" ")
        val mName = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: defaultOctName
        val yVal = parts.getOrNull(1)?.toIntOrNull() ?: 2026

        val mInt = when (mName.lowercase()) {
            "januari", "january" -> Calendar.JANUARY
            "februari", "february" -> Calendar.FEBRUARY
            "maret", "march" -> Calendar.MARCH
            "april" -> Calendar.APRIL
            "mei", "may" -> Calendar.MAY
            "juni", "june" -> Calendar.JUNE
            "juli", "july" -> Calendar.JULY
            "agustus", "august" -> Calendar.AUGUST
            "september" -> Calendar.SEPTEMBER
            "oktober", "october" -> Calendar.OCTOBER
            "november" -> Calendar.NOVEMBER
            "desember", "december" -> Calendar.DECEMBER
            else -> Calendar.OCTOBER
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, yVal)
            set(Calendar.MONTH, mInt)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Convert to Monday-first (Monday = 0, ..., Sunday = 6)
        val leading = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
        val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Quint(days, leading, mInt, yVal, mName)
    }

    val todayDay = remember(selectedMonth, yearVal, monthInt) {
        val today = Calendar.getInstance()
        if (today.get(Calendar.YEAR) == yearVal && today.get(Calendar.MONTH) == monthInt) {
            today.get(Calendar.DAY_OF_MONTH)
        } else -1
    }

    // Map day to transaction summary (income/expense indicator dots on calendar)
    val dayTxSummaryMap = remember(uiState.transactions, selectedMonth, allPeriodsText, yearVal, monthInt) {
        val map = mutableMapOf<Int, DayTransactionSummary>()
        val cal = Calendar.getInstance()
        uiState.transactions.forEach { tx ->
            cal.timeInMillis = tx.dateMillis
            val txYear = cal.get(Calendar.YEAR)
            val txMonth = cal.get(Calendar.MONTH)
            if (selectedMonth == allPeriodsText || (txYear == yearVal && txMonth == monthInt)) {
                val d = cal.get(Calendar.DAY_OF_MONTH)
                val existing = map[d]
                val hasInc = existing?.hasIncome == true || tx.type == TransactionType.INCOME
                val hasExp = existing?.hasExpense == true || tx.type == TransactionType.EXPENSE
                val expTot = (existing?.totalExpense ?: 0L) + if (tx.type == TransactionType.EXPENSE) tx.amount else 0L
                val incTot = (existing?.totalIncome ?: 0L) + if (tx.type == TransactionType.INCOME) tx.amount else 0L
                val cnt = (existing?.count ?: 0) + 1
                map[d] = DayTransactionSummary(hasInc, hasExp, expTot, incTot, cnt)
            }
        }
        map
    }

    // Base transactions filtered by search, month, and day (used for summary card totals so they never zero out)
    val baseTransactions = remember(
        uiState.transactions,
        searchQuery,
        selectedDay,
        selectedMonth,
        allPeriodsText,
        yearVal,
        monthInt,
        currentLanguage
    ) {
        val cal = Calendar.getInstance()
        uiState.transactions.filter { tx ->
            cal.timeInMillis = tx.dateMillis
            val txYear = cal.get(Calendar.YEAR)
            val txMonth = cal.get(Calendar.MONTH)

            val matchesMonth = if (selectedMonth == allPeriodsText) true else {
                txYear == yearVal && txMonth == monthInt
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val localizedCatName = context.getString(Category.getCategoryNameRes(tx.categoryId))
                tx.title.contains(searchQuery, ignoreCase = true) ||
                        tx.note.contains(searchQuery, ignoreCase = true) ||
                        tx.categoryName.contains(searchQuery, ignoreCase = true) ||
                        localizedCatName.contains(searchQuery, ignoreCase = true) ||
                        tx.amount.toString().contains(searchQuery)
            }

            val matchesDay = if (selectedDay == null) {
                true
            } else {
                cal.get(Calendar.DAY_OF_MONTH) == selectedDay
            }

            matchesMonth && matchesSearch && matchesDay
        }
    }

    val totalIncome = remember(baseTransactions) {
        baseTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(baseTransactions) {
        baseTransactions.filter {
            it.type == TransactionType.EXPENSE && !it.categoryId.equals("transfer", ignoreCase = true)
        }.sumOf { it.amount }
    }

    // Filter transactions by selected type
    val filteredTransactions = remember(baseTransactions, selectedTypeFilter) {
        if (selectedTypeFilter == null) {
            baseTransactions
        } else {
            baseTransactions.filter { it.type == selectedTypeFilter }
        }
    }

    // Group by date (recomputes when language switches so date headers update immediately)
    val groupedTransactions: Map<String, List<Transaction>> = remember(filteredTransactions, currentLanguage) {
        filteredTransactions.groupBy { DateFormatter.formatDateGroup(it.dateMillis) }
    }

    val totalSlots = leadingEmptySlots + daysInMonth
    val totalRows = (totalSlots + 6) / 7

    val dayHeaders = listOf(
        stringResource(R.string.calendar_day_mon),
        stringResource(R.string.calendar_day_tue),
        stringResource(R.string.calendar_day_wed),
        stringResource(R.string.calendar_day_thu),
        stringResource(R.string.calendar_day_fri),
        stringResource(R.string.calendar_day_sat),
        stringResource(R.string.calendar_day_sun)
    )

    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // MAIN SCROLLABLE CONTENT (Scrolls smoothly beneath TopFadeScrim)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBarHeight + topBarHeight + 8.dp,
                bottom = 120.dp
            )
        ) {
            // A. REAL MONTH CALENDAR GRID (CLEAN: No redundant titles/instructions)
            item(key = "calendar_card") {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        // Day Names Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayHeaders.forEach { name ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 7-Column Calendar Grid
                        for (r in 0 until totalRows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0..6) {
                                    val dayIndex = r * 7 + col - leadingEmptySlots + 1
                                    if (dayIndex in 1..daysInMonth) {
                                        val isSelected = selectedDay == dayIndex
                                        val dayTx = dayTxSummaryMap[dayIndex]
                                        val hasInc = dayTx?.hasIncome == true
                                        val hasExp = dayTx?.hasExpense == true
                                        val isToday = dayIndex == todayDay

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            RealCalendarCell(
                                                dayNumber = dayIndex,
                                                isSelected = isSelected,
                                                isToday = isToday,
                                                hasIncome = hasInc,
                                                hasExpense = hasExp,
                                                onClick = {
                                                    selectedDay = if (selectedDay == dayIndex) null else dayIndex
                                                }
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        // Selected Date Status & Reset Pill (shown when user taps a date)
                        if (selectedDay != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.activity_selected_date_status,
                                        selectedDay ?: 1,
                                        monthName,
                                        dayTxSummaryMap[selectedDay]?.count ?: 0
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.activity_show_all_days),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { selectedDay = null }
                                )
                            }
                        }
                    }
                }
            }

            // B. BENTO SUMMARY CARDS (Pengeluaran vs Pemasukan)
            item(key = "bento_summary") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pengeluaran Bento Card
                    ActivitySummaryCard(
                        title = stringResource(R.string.type_expense),
                        amount = totalExpense,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        accentColor = MaterialTheme.colorScheme.error,
                        isSelected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Pemasukan Bento Card
                    ActivitySummaryCard(
                        title = stringResource(R.string.type_income),
                        amount = totalIncome,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accentColor = MoneyTrackerTheme.colors.income,
                        isSelected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // D. TRANSACTION LIST (With Borders & Dividers matching HomeScreen)
            if (filteredTransactions.isEmpty()) {
                item(key = "empty_state") {
                    Spacer(modifier = Modifier.height(32.dp))
                    val emptyMessage = when {
                        selectedDay != null -> stringResource(
                            R.string.activity_empty_date_format,
                            selectedDay ?: 1,
                            monthName
                        )
                        searchQuery.isNotBlank() -> stringResource(
                            R.string.activity_empty_search_format,
                            searchQuery
                        )
                        else -> stringResource(R.string.activity_empty_period)
                    }
                    EmptyStateView(message = emptyMessage)
                }
            } else {
                if (selectedDay != null) {
                    // Single day selected
                    val dayTotal = filteredTransactions.sumOf {
                        when {
                            it.categoryId.equals("transfer", ignoreCase = true) -> 0L
                            it.type == TransactionType.EXPENSE -> -it.amount
                            else -> it.amount
                        }
                    }

                    item(key = "header_single_day_$selectedDay") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 10.dp, bottom = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                thickness = 1.dp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.activity_single_day_header_format,
                                        selectedDay ?: 1,
                                        monthName
                                    ),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val formattedDayTotal = CurrencyFormatter.formatRupiah(abs(dayTotal))
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
                        items = filteredTransactions,
                        key = { it.id }
                    ) { transaction ->
                        ModernTransactionCard(
                            transaction = transaction,
                            onClick = { selectedTransactionForDetail = transaction }
                        )
                    }
                } else {
                    // Grouped by date with HorizontalDivider between sections (Same as HomeScreen)
                    groupedTransactions.entries.forEach { (dateGroup, transactionsInGroup) ->
                        val dayTotal = transactionsInGroup.sumOf {
                            when {
                                it.categoryId.equals("transfer", ignoreCase = true) -> 0L
                                it.type == TransactionType.EXPENSE -> -it.amount
                                else -> it.amount
                            }
                        }

                        item(key = "header_$dateGroup") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 10.dp, bottom = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                    thickness = 1.dp
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 6.dp),
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

                                    val formattedDayTotal = CurrencyFormatter.formatRupiah(abs(dayTotal))
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

        // Top Fade Scrim: Smooth gradient fade beneath TopBar when scrolled
        TopFadeScrim(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(5f),
            height = statusBarHeight + topBarHeight + 48.dp,
            isScrolled = listState.canScrollBackward
        )

        // Sticky TopBar with Expandable Search
        ActivityTopBar(
            selectedMonth = selectedMonth,
            months = months,
            onMonthSelected = onMonthSelected,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isSearchExpanded = isSearchExpanded,
            onSearchExpandToggle = { isSearchExpanded = it },
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
                        com.aol.moneytracker.presentation.screens.wallet.WalletStateHolder.revertTransactionEffect(context, tx)
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
            }
        )
    }
}

/**
 * Sticky TopBar for ActivityScreen with expandable search.
 */
@Composable
fun ActivityTopBar(
    selectedMonth: String,
    months: List<String>,
    onMonthSelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchExpanded: Boolean,
    onSearchExpandToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedContent(
                targetState = isSearchExpanded,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "topbar_search_transition"
            ) { expanded ->
                if (expanded) {
                    // Expanded Search Bar at Top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = {
                                onSearchExpandToggle(false)
                                onSearchQueryChange("")
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_close_search),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.activity_search_placeholder),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = onSearchQueryChange,
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                    )
                                }

                                if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.action_clear),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .clickable { onSearchQueryChange("") }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Standard TopBar (Title + Search Button + Month Dropdown)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.activity_title),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.4).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Search Button beside Date
                            Surface(
                                onClick = { onSearchExpandToggle(true) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(R.string.cd_search_transactions),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Month Dropdown Menu
                            if (months.isNotEmpty()) {
                                AppDropdownMenu(
                                    items = months,
                                    selectedItem = selectedMonth,
                                    onItemSelected = onMonthSelected,
                                    dropdownName = "ActivityMonthDropdown"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Date Cell in Real Calendar Grid.
 */
@Composable
private fun RealCalendarCell(
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasIncome: Boolean,
    hasExpense: Boolean,
    onClick: () -> Unit
) {
    val cellBg = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(cellBg)
            .clickable(onClick = onClick)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = textColor
            )

            // Indicator dots (Income: Green, Expense: Red)
            if (hasIncome || hasExpense) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    if (hasIncome) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MoneyTrackerTheme.colors.income)
                        )
                    }
                    if (hasExpense) {
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2-Column Bento Summary Card (Interactive filter toggle without colored card background).
 */
@Composable
private fun ActivitySummaryCard(
    title: String,
    amount: Long,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            }
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
