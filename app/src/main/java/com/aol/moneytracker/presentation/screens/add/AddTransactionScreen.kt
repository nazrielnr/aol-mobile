package com.aol.moneytracker.presentation.screens.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.components.FinLogoCircleBadge
import com.aol.moneytracker.presentation.components.PurchasedItem
import com.aol.moneytracker.presentation.components.SegmentedTypeSwitch
import com.aol.moneytracker.presentation.components.StandardModalBottomSheet
import com.aol.moneytracker.presentation.components.TransactionLocationData
import com.aol.moneytracker.presentation.components.TransactionLocationMapCard
import com.aol.moneytracker.presentation.components.TransactionRichMetaCodec
import com.aol.moneytracker.presentation.components.UniversalSubScreenScaffold
import com.aol.moneytracker.presentation.components.getCategoryColor
import com.aol.moneytracker.presentation.components.getCategoryIcon
import com.aol.moneytracker.presentation.screens.wallet.TransactionWalletMeta
import com.aol.moneytracker.presentation.screens.wallet.WalletItem
import com.aol.moneytracker.presentation.screens.wallet.bankWithCustomBracket
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter
import com.aol.moneytracker.util.DateFormatter
import java.util.Calendar

@Composable
fun AddTransactionScreen(
    wallets: List<WalletItem>,
    isEditMode: Boolean = false,
    editingTransactionId: Long = 0L,
    initialTitle: String = "",
    initialAmount: Long = 0L,
    initialType: TransactionType = TransactionType.EXPENSE,
    initialCategoryId: String? = null,
    initialWalletId: String? = null,
    initialToWalletId: String? = null,
    initialDateMillis: Long = System.currentTimeMillis(),
    initialNote: String = "",
    onBackClick: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    val context = LocalContext.current
    val appColors = MoneyTrackerTheme.colors

    val isTransferMode = initialCategoryId.equals("transfer", ignoreCase = true)

    val initialRichMeta = remember(initialNote, initialTitle, initialAmount, initialCategoryId, initialType) {
        if (initialNote.isNotBlank() || isEditMode || initialTitle.isNotBlank()) {
            TransactionRichMetaCodec.decodeOrDefault(
                rawNote = initialNote,
                title = initialTitle,
                amount = initialAmount,
                categoryId = initialCategoryId,
                type = initialType
            )
        } else {
            com.aol.moneytracker.presentation.components.TransactionRichMeta(
                items = emptyList(),
                location = TransactionRichMetaCodec.presetLocations.first(),
                plainNote = ""
            )
        }
    }

    var type by remember { mutableStateOf(initialType) }
    var rawAmount by remember { mutableStateOf(if (initialAmount > 0L) initialAmount.toString() else "") }
    var title by remember { mutableStateOf(initialTitle) }
    var note by remember { mutableStateOf(initialRichMeta.plainNote) }
    var dateMillis by remember { mutableLongStateOf(initialDateMillis) }

    val purchasedItems = remember {
        mutableStateListOf<PurchasedItem>().apply {
            addAll(initialRichMeta.items)
        }
    }
    var selectedLocation by remember {
        mutableStateOf(
            if (isTransferMode) null
            else (initialRichMeta.location ?: TransactionRichMetaCodec.presetLocations.first())
        )
    }

    var selectedCategoryId by remember {
        mutableStateOf(
            when {
                isTransferMode -> "transfer"
                !initialCategoryId.isNullOrBlank() -> initialCategoryId
                type == TransactionType.EXPENSE -> Category.EXPENSE_CATEGORIES.first().id
                else -> Category.INCOME_CATEGORIES.first().id
            }
        )
    }

    var selectedWalletId by remember(wallets) {
        mutableStateOf(
            initialWalletId?.takeIf { id -> wallets.any { it.id == id } }
                ?: wallets.firstOrNull()?.id.orEmpty()
        )
    }

    var selectedToWalletId by remember(wallets) {
        mutableStateOf(
            initialToWalletId?.takeIf { id -> wallets.any { it.id == id } }
                ?: wallets.getOrNull(1)?.id
                ?: wallets.firstOrNull()?.id.orEmpty()
        )
    }

    val selectedWallet = wallets.firstOrNull { it.id == selectedWalletId } ?: wallets.first()
    val selectedToWallet = wallets.firstOrNull { it.id == selectedToWalletId }
        ?: wallets.firstOrNull { it.id != selectedWallet.id }
        ?: wallets.first()

    var isCategorySheetOpen by remember { mutableStateOf(false) }
    var isWalletSheetOpen by remember { mutableStateOf(false) }
    var isToWalletSheetOpen by remember { mutableStateOf(false) }
    var isDateSheetOpen by remember { mutableStateOf(false) }
    var dateTimeSheetInitialTab by remember { mutableIntStateOf(0) }
    var isItemSheetOpen by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableIntStateOf(-1) }
    var isLocationSheetOpen by remember { mutableStateOf(false) }

    val parsedAmount = CurrencyFormatter.parseAmount(rawAmount)

    // In Edit mode on the same wallet, account for existing balance if needed, or validate cleanly
    val effectiveAvailableBalance = remember(selectedWallet, isEditMode, initialWalletId, initialAmount, initialType, type) {
        if (isEditMode && selectedWallet.id == initialWalletId && initialType == TransactionType.EXPENSE) {
            selectedWallet.balance + initialAmount
        } else {
            selectedWallet.balance
        }
    }

    val isExpenseOrTransfer = isTransferMode || type == TransactionType.EXPENSE
    val isInsufficientBalance = isExpenseOrTransfer && parsedAmount > 0L && parsedAmount > effectiveAvailableBalance
    val isValid = parsedAmount > 0L &&
        (isTransferMode || title.isNotBlank()) &&
        !isInsufficientBalance

    val selectedCategory = Category.findCategory(selectedCategoryId, type)
    val listState = rememberLazyListState()

    UniversalSubScreenScaffold(
        title = if (isEditMode) "Edit Transaksi" else stringResource(R.string.add_tx_title),
        onBackClick = onBackClick,
        isScrolledOverride = listState.canScrollBackward,
        bottomBar = { bottomInset ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .padding(bottom = bottomInset),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.88f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )
                }

                Button(
                    onClick = {
                        if (isValid) {
                            val fromName = selectedWallet.bankWithCustomBracket(context)
                            val toName = selectedToWallet.bankWithCustomBracket(context)

                            val encodedCategoryName = if (isTransferMode) {
                                TransactionWalletMeta.encodeTransfer(
                                    fromWallet = selectedWallet,
                                    toWallet = selectedToWallet,
                                    fromName = fromName,
                                    toName = toName
                                )
                            } else {
                                TransactionWalletMeta.encodeNormal(
                                    wallet = selectedWallet,
                                    walletDisplayName = fromName,
                                    localizedCategoryName = context.getString(selectedCategory.nameRes)
                                )
                            }

                            val finalTitle = if (isTransferMode && title.isBlank()) {
                                "Transfer ke $toName"
                            } else {
                                title.trim()
                            }

                            val encodedNote = if (isTransferMode) {
                                note.trim()
                            } else {
                                TransactionRichMetaCodec.encode(
                                    items = purchasedItems.toList(),
                                    location = selectedLocation,
                                    plainNote = note.trim()
                                )
                            }

                            val transaction = Transaction(
                                id = editingTransactionId,
                                title = finalTitle,
                                amount = parsedAmount,
                                type = if (isTransferMode) TransactionType.EXPENSE else type,
                                categoryId = if (isTransferMode) "transfer" else selectedCategoryId,
                                categoryName = encodedCategoryName,
                                dateMillis = dateMillis,
                                note = encodedNote
                            )
                            onSave(transaction)
                        }
                    },
                    enabled = isValid,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_save),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    ) { contentPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Segmented Type Switch (for regular Expense / Income)
            if (!isTransferMode) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        SegmentedTypeSwitch(
                            selectedType = type,
                            onTypeSelected = { newType ->
                                type = newType
                                val defaultCat = if (newType == TransactionType.INCOME) {
                                    Category.INCOME_CATEGORIES.first()
                                } else {
                                    Category.EXPENSE_CATEGORIES.first()
                                }
                                selectedCategoryId = defaultCat.id
                            }
                        )
                    }
                }
            }

            // 2. Card: Kategori & Rekening / Sumber Dana (Clean Trigger Rows -> Modal Bottom Sheet)
            item {
                TxFormGroupCard {
                    if (isTransferMode) {
                        // Transfer Route Selector inside Edit Transfer (Visual icon-to-icon connector)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                TxWalletSelectorRow(
                                    roleLabel = null,
                                    wallet = selectedWallet,
                                    effectiveBaseBalance = effectiveAvailableBalance,
                                    projectedBalance = if (parsedAmount > 0L) {
                                        (effectiveAvailableBalance - parsedAmount).coerceAtLeast(0L)
                                    } else null,
                                    isExpenseOrSource = true,
                                    isInsufficient = isInsufficientBalance,
                                    onClick = { isWalletSheetOpen = true },
                                    endPadding = 68.dp
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 76.dp, end = 78.dp),
                                    thickness = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                                )

                                TxWalletSelectorRow(
                                    roleLabel = null,
                                    wallet = selectedToWallet,
                                    effectiveBaseBalance = selectedToWallet.balance,
                                    projectedBalance = if (parsedAmount > 0L) {
                                        selectedToWallet.balance + parsedAmount
                                    } else null,
                                    isExpenseOrSource = false,
                                    isInsufficient = false,
                                    onClick = { isToWalletSheetOpen = true },
                                    endPadding = 68.dp
                                )
                            }

                            // Visual Connector Line & Directional Arrow between Top Logo and Bottom Logo
                            TxTransferRouteVisualConnector(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 28.dp)
                            )

                            Surface(
                                onClick = {
                                    val temp = selectedWalletId
                                    selectedWalletId = selectedToWalletId
                                    selectedToWalletId = temp
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 18.dp)
                                    .size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Tukar rekening",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Row 1: Rekening / Sumber Dana
                        val projectedBalance = if (parsedAmount > 0L) {
                            if (type == TransactionType.EXPENSE) {
                                (effectiveAvailableBalance - parsedAmount).coerceAtLeast(0L)
                            } else {
                                effectiveAvailableBalance + parsedAmount
                            }
                        } else null

                        TxWalletSelectorRow(
                            roleLabel = null,
                            wallet = selectedWallet,
                            effectiveBaseBalance = effectiveAvailableBalance,
                            projectedBalance = projectedBalance,
                            isExpenseOrSource = type == TransactionType.EXPENSE,
                            isInsufficient = isInsufficientBalance,
                            onClick = { isWalletSheetOpen = true }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.8.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )

                        // Row 2: Kategori Transaksi (Modal Selector Trigger, NOT pills)
                        val catColor = getCategoryColor(selectedCategory.id)
                        Surface(
                            onClick = { isCategorySheetOpen = true },
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 17.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(catColor.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(selectedCategory.iconKey),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.add_tx_category_label),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(selectedCategory.nameRes),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Card: Nominal & Nama/Judul Transaksi
            item {
                TxFormGroupCard {
                    // Nominal Input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        TxFieldHeader(
                            icon = Icons.Outlined.Payments,
                            title = "Nominal Transaksi"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = rawAmount,
                            onValueChange = { rawAmount = it.filter { ch -> ch.isDigit() } },
                            placeholder = {
                                Text(
                                    text = "0",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            prefix = {
                                Text(
                                    text = "Rp ",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingIcon = if (rawAmount.isNotEmpty()) {
                                {
                                    Surface(
                                        onClick = { rawAmount = "" },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                                contentDescription = "Reset",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            } else null,
                            isError = isInsufficientBalance,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isInsufficientBalance) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                unfocusedBorderColor = if (isInsufficientBalance) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                },
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isInsufficientBalance) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val shortage = parsedAmount - effectiveAvailableBalance
                            val wName = selectedWallet.customName ?: context.getString(selectedWallet.nameRes)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Saldo $wName kurang ${CurrencyFormatter.formatRupiah(shortage)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Nominal Chips
                        val quickDeltas = listOf(
                            "+10rb" to 10_000L,
                            "+20rb" to 20_000L,
                            "+50rb" to 50_000L,
                            "+100rb" to 100_000L
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickDeltas.forEach { (label, delta) ->
                                Surface(
                                    onClick = {
                                        val next = (parsedAmount + delta).coerceAtMost(999_999_999_999L)
                                        rawAmount = next.toString()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                                    border = BorderStroke(
                                        0.8.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )

                    // Judul / Nama Transaksi
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        TxFieldHeader(
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            title = stringResource(R.string.add_tx_field_title_label)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = {
                                Text(
                                    text = if (isTransferMode) {
                                        "Contoh: Transfer Tabungan"
                                    } else {
                                        stringResource(
                                            if (type == TransactionType.EXPENSE) {
                                                R.string.add_tx_field_title_placeholder_expense
                                            } else {
                                                R.string.add_tx_field_title_placeholder_income
                                            }
                                        )
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3B. Card: Universal Rincian Barang / Pesanan (Menu atau Barang yang Dibeli)
            if (!isTransferMode) {
                item {
                    TxFormGroupCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TxFieldHeader(
                                    icon = Icons.Outlined.ShoppingBag,
                                    title = "Rincian Barang / Pesanan"
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                        .clickable {
                                            editingItemIndex = -1
                                            isItemSheetOpen = true
                                        }
                                        .padding(horizontal = 11.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Tambah Item",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (purchasedItems.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.28f))
                                        .border(
                                            width = 0.8.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            editingItemIndex = -1
                                            isItemSheetOpen = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Catat menu makanan, belanjaan, atau daftar barang...",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.42f),
                                    border = BorderStroke(
                                        width = 0.8.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        purchasedItems.forEachIndexed { idx, item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        editingItemIndex = idx
                                                        isItemSheetOpen = true
                                                    }
                                                    .padding(vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 30.dp, height = 24.dp)
                                                        .clip(RoundedCornerShape(7.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                                        .border(
                                                            0.6.dp,
                                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                            RoundedCornerShape(7.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${item.qty}x",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = item.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 13.sp,
                                                            lineHeight = 17.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (item.qty > 1 && item.unitPrice > 0L) {
                                                        Text(
                                                            text = "@ ${CurrencyFormatter.formatRupiah(item.unitPrice)}",
                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                                        )
                                                    }
                                                }

                                                if (item.subtotal > 0L) {
                                                    Text(
                                                        text = CurrencyFormatter.formatRupiah(item.subtotal),
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            fontFeatureSettings = "tnum"
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .clickable {
                                                            purchasedItems.removeAt(idx)
                                                            val newSum = purchasedItems.sumOf { it.subtotal }
                                                            if (newSum > 0L) {
                                                                rawAmount = newSum.toString()
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Hapus item",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }

                                            if (idx < purchasedItems.lastIndex) {
                                                HorizontalDivider(
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f),
                                                    thickness = 0.6.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3C. Card: Lokasi Pembayaran & Data Maps
                item {
                    TxFormGroupCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TxFieldHeader(
                                    icon = Icons.Outlined.LocationOn,
                                    title = "Lokasi Pembayaran (Maps)"
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                        .clickable { isLocationSheetOpen = true }
                                        .padding(horizontal = 11.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (selectedLocation != null) "Ubah Lokasi" else "Pilih Lokasi",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val currentLoc = selectedLocation ?: TransactionRichMetaCodec.presetLocations.first()
                            TransactionLocationMapCard(
                                location = currentLoc,
                                onClick = { isLocationSheetOpen = true }
                            )
                        }
                    }
                }
            }

            // 4. Card: Waktu Transaksi & Catatan Opsional
            item {
                TxFormGroupCard {
                    // Row 1: Waktu Transaksi (Standard TxFieldHeader + 2 Separate Field Boxes below)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        TxFieldHeader(
                            icon = Icons.Outlined.CalendarToday,
                            title = "Waktu Transaksi"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TxDateTimeFieldsRow(
                            dateMillis = dateMillis,
                            onOpenDatePicker = {
                                dateTimeSheetInitialTab = 0
                                isDateSheetOpen = true
                            },
                            onOpenTimePicker = {
                                dateTimeSheetInitialTab = 1
                                isDateSheetOpen = true
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )

                    // Row 2: Catatan Opsional
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        TxFieldHeader(
                            icon = Icons.Outlined.Description,
                            title = stringResource(R.string.add_tx_note_label)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = {
                                Text(
                                    text = "Tambahkan detail, lokasi, atau nomor referensi...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            maxLines = 2,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Modal 1: Pilih Kategori (Clean List inside StandardModalBottomSheet)
    if (isCategorySheetOpen) {
        val categories = if (type == TransactionType.INCOME) {
            Category.INCOME_CATEGORIES
        } else {
            Category.EXPENSE_CATEGORIES
        }

        StandardModalBottomSheet(
            onDismissRequest = { isCategorySheetOpen = false },
            title = "Pilih Kategori Transaksi",
            scrollable = true
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    categories.forEachIndexed { index, cat ->
                        val isSelected = cat.id == selectedCategoryId
                        val catColor = getCategoryColor(cat.id)

                        Surface(
                            onClick = {
                                selectedCategoryId = cat.id
                                dismiss()
                            },
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                Color.Transparent
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(catColor.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(cat.iconKey),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = stringResource(cat.nameRes),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.5.sp
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (index < categories.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 66.dp, end = 16.dp),
                                thickness = 0.6.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal 2: Pilih Rekening / Wallet
    if (isWalletSheetOpen) {
        TxWalletPickerSheet(
            title = "Pilih Rekening",
            wallets = wallets,
            selectedWalletId = selectedWallet.id,
            onSelect = { picked ->
                if (isTransferMode && picked.id == selectedToWalletId) {
                    selectedToWalletId = selectedWalletId
                }
                selectedWalletId = picked.id
            },
            onDismiss = { isWalletSheetOpen = false }
        )
    }

    // Modal 2B: Pilih Rekening Tujuan (when editing Transfer)
    if (isToWalletSheetOpen) {
        TxWalletPickerSheet(
            title = "Pilih Rekening Tujuan",
            wallets = wallets,
            selectedWalletId = selectedToWallet.id,
            onSelect = { picked ->
                if (picked.id == selectedWalletId) {
                    selectedWalletId = selectedToWalletId
                }
                selectedToWalletId = picked.id
            },
            onDismiss = { isToWalletSheetOpen = false }
        )
    }

    // Modal 3: Pilih Tanggal & Waktu Transaksi (Native In-Sheet Calendar & Time Picker)
    if (isDateSheetOpen) {
        TxDateTimePickerSheet(
            initialMillis = dateMillis,
            initialTab = dateTimeSheetInitialTab,
            onConfirm = { updatedMillis ->
                dateMillis = updatedMillis
            },
            onDismiss = { isDateSheetOpen = false }
        )
    }

    // Modal 4: Tambah / Ubah Item Barang atau Pesanan
    if (isItemSheetOpen) {
        val existingItem = purchasedItems.getOrNull(editingItemIndex)
        TxPurchasedItemEditorSheet(
            initialItem = existingItem,
            onSaveItem = { savedItem ->
                if (editingItemIndex in purchasedItems.indices) {
                    purchasedItems[editingItemIndex] = savedItem
                } else {
                    purchasedItems.add(savedItem)
                }
                val totalItemsAmount = purchasedItems.sumOf { it.subtotal }
                if (totalItemsAmount > 0L) {
                    rawAmount = totalItemsAmount.toString()
                }
                if (title.isBlank()) {
                    title = savedItem.name
                }
                isItemSheetOpen = false
            },
            onDismiss = { isItemSheetOpen = false }
        )
    }

    // Modal 5: Pilih / Ubah Lokasi Pembayaran (Maps)
    if (isLocationSheetOpen) {
        TxLocationPickerSheet(
            currentLocation = selectedLocation ?: TransactionRichMetaCodec.presetLocations.first(),
            onSelectLocation = { newLoc ->
                selectedLocation = newLoc
                isLocationSheetOpen = false
            },
            onDismiss = { isLocationSheetOpen = false }
        )
    }
}

private fun isSameCalendarDay(millisA: Long, millisB: Long): Boolean {
    val calA = Calendar.getInstance().apply { timeInMillis = millisA }
    val calB = Calendar.getInstance().apply { timeInMillis = millisB }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}

private fun getDayOffsetFromToday(targetMillis: Long): Int? {
    val today = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = targetMillis }
    for (offset in 0 downTo -2) {
        val check = (today.clone() as Calendar).apply {
            if (offset != 0) add(Calendar.DAY_OF_YEAR, offset)
        }
        if (check.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            check.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        ) {
            return offset
        }
    }
    return null
}

private fun formatShortDateId(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val months = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = months[cal.get(Calendar.MONTH)]
    val year = cal.get(Calendar.YEAR)
    return "$day $month $year"
}

private fun formatFullDayDateId(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val days = arrayOf("", "Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    val dayName = days[cal.get(Calendar.DAY_OF_WEEK)]
    return "$dayName, ${formatShortDateId(millis)}"
}

@Composable
private fun TxDateTimeFieldsRow(
    dateMillis: Long,
    onOpenDatePicker: () -> Unit,
    onOpenTimePicker: () -> Unit
) {
    val fullDayDateLabel = remember(dateMillis) { formatFullDayDateId(dateMillis) }
    val timeLabel = remember(dateMillis) { DateFormatter.formatTime(dateMillis) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Field Box: Hari, Tanggal Bulan Tahun (e.g. "Selasa, 6 Okt 2026")
        Surface(
            onClick = onOpenDatePicker,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.25f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .weight(1.55f)
                .height(54.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fullDayDateLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Right Field Box: Jam (e.g. "02:55")
        Surface(
            onClick = onOpenTimePicker,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.25f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .weight(0.85f)
                .height(54.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TxDateTimePickerSheet(
    initialMillis: Long,
    initialTab: Int,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialCal = remember(initialMillis) {
        Calendar.getInstance().apply { timeInMillis = initialMillis }
    }

    var displayedYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH)) }

    val timePickerState = rememberTimePickerState(
        initialHour = initialCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCal.get(Calendar.MINUTE),
        is24Hour = true
    )

    val monthNames = remember {
        arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
    }

    fun buildMillisWithTime(hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = initialMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeInMillis
    }

    StandardModalBottomSheet(
        onDismissRequest = onDismiss,
        title = if (initialTab == 0) "Pilih Tanggal Transaksi" else "Pilih Jam Transaksi",
        scrollable = true,
        footer = if (initialTab == 1) {
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { dismiss() },
                        shape = CircleShape,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.90f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_cancel),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Button(
                        onClick = {
                            onConfirm(buildMillisWithTime(timePickerState.hour, timePickerState.minute))
                            dismiss()
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1.35f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simpan Jam",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        } else null
    ) {
        if (initialTab == 0) {
            // MODE 0: 1-TAP CALENDAR DATE PICKER (tapping any chip or day immediately selects & closes)
            val currentDayOffset = getDayOffsetFromToday(initialMillis)
            val datePresets = listOf(
                "Hari Ini" to 0,
                "Kemarin" to -1,
                "2 Hari Lalu" to -2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                datePresets.forEach { (label, offset) ->
                    val isSelected = currentDayOffset == offset
                    Surface(
                        onClick = {
                            val target = Calendar.getInstance().apply {
                                if (offset != 0) add(Calendar.DAY_OF_YEAR, offset)
                                set(Calendar.HOUR_OF_DAY, initialCal.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, initialCal.get(Calendar.MINUTE))
                            }
                            onConfirm(target.timeInMillis)
                            dismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            // Month Calendar Grid Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (displayedMonth == 0) {
                                    displayedMonth = 11
                                    displayedYear -= 1
                                } else {
                                    displayedMonth -= 1
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Bulan sebelumnya",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${monthNames[displayedMonth]} $displayedYear",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = {
                                if (displayedMonth == 11) {
                                    displayedMonth = 0
                                    displayedYear += 1
                                } else {
                                    displayedMonth += 1
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Bulan berikutnya",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day-of-week Headers (Sen..Min)
                    val dayHeaders = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        dayHeaders.forEach { d ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = d,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val monthCal = remember(displayedYear, displayedMonth) {
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR, displayedYear)
                            set(Calendar.MONTH, displayedMonth)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                    }
                    val firstDayOfWeekMondayZero =
                        ((monthCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) + 7) % 7
                    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val totalCells = ((firstDayOfWeekMondayZero + daysInMonth + 6) / 7) * 7
                    val todayCal = remember { Calendar.getInstance() }

                    val rows = totalCells / 7
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            for (c in 0 until 7) {
                                val cellIndex = r * 7 + c
                                val dayNum = cellIndex - firstDayOfWeekMondayZero + 1
                                if (dayNum in 1..daysInMonth) {
                                    val isSelectedDay =
                                        initialCal.get(Calendar.YEAR) == displayedYear &&
                                            initialCal.get(Calendar.MONTH) == displayedMonth &&
                                            initialCal.get(Calendar.DAY_OF_MONTH) == dayNum

                                    val isToday =
                                        todayCal.get(Calendar.YEAR) == displayedYear &&
                                            todayCal.get(Calendar.MONTH) == displayedMonth &&
                                            todayCal.get(Calendar.DAY_OF_MONTH) == dayNum

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            onClick = {
                                                val updated = Calendar.getInstance().apply {
                                                    timeInMillis = initialMillis
                                                    set(Calendar.YEAR, displayedYear)
                                                    set(Calendar.MONTH, displayedMonth)
                                                    set(Calendar.DAY_OF_MONTH, dayNum)
                                                }
                                                onConfirm(updated.timeInMillis)
                                                dismiss()
                                            },
                                            shape = CircleShape,
                                            color = if (isSelectedDay) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.Transparent
                                            },
                                            border = if (isToday && !isSelectedDay) {
                                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                            } else null,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelectedDay || isToday) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = when {
                                                        isSelectedDay -> MaterialTheme.colorScheme.onPrimary
                                                        isToday -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MODE 1: NATIVE M3 ANALOG DIAL + DIGITAL HEADER TIME PICKER + QUICK PRESETS
            val quickTimes = listOf(
                Triple("Sekarang", -1, -1),
                Triple("08:00", 8, 0),
                Triple("12:30", 12, 30),
                Triple("19:00", 19, 0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickTimes.forEach { (label, h, m) ->
                    val isSelected = h >= 0 && timePickerState.hour == h && timePickerState.minute == m
                    Surface(
                        onClick = {
                            val targetH: Int
                            val targetM: Int
                            if (h < 0) {
                                val now = Calendar.getInstance()
                                targetH = now.get(Calendar.HOUR_OF_DAY)
                                targetM = now.get(Calendar.MINUTE)
                            } else {
                                targetH = h
                                targetM = m
                            }
                            onConfirm(buildMillisWithTime(targetH, targetM))
                            dismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            // Native Material 3 TimePicker (24-Hour Digital Header + Circular Clock Dial)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun TxTransferRouteVisualConnector(
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    Column(
        modifier = modifier.width(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.15f), lineColor)
                    )
                )
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
            ),
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(lineColor, lineColor.copy(alpha = 0.15f))
                    )
                )
        )
    }
}

@Composable
private fun TxWalletSelectorRow(
    roleLabel: String?,
    wallet: WalletItem,
    effectiveBaseBalance: Long,
    projectedBalance: Long?,
    isExpenseOrSource: Boolean,
    isInsufficient: Boolean,
    onClick: () -> Unit,
    endPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    val context = LocalContext.current
    val appColors = MoneyTrackerTheme.colors
    val walletName = wallet.bankWithCustomBracket(context)
    val walletCategory = wallet.customType ?: context.getString(wallet.typeRes)

    val topSubtitle = when {
        roleLabel != null && wallet.showCategoryOnCard && walletCategory.isNotBlank() ->
            "$roleLabel • $walletCategory"
        roleLabel != null -> roleLabel
        wallet.showCategoryOnCard && walletCategory.isNotBlank() -> walletCategory
        else -> null
    }

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = endPadding, top = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FinLogoCircleBadge(
                idOrName = wallet.providerKey,
                size = 46.dp,
                borderWidth = 1.dp,
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (topSubtitle != null) {
                    Text(
                        text = topSubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = walletName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (projectedBalance != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = CurrencyFormatter.formatRupiah(effectiveBaseBalance),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = CurrencyFormatter.formatRupiah(projectedBalance),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = when {
                                isExpenseOrSource && isInsufficient -> MaterialTheme.colorScheme.error
                                !isExpenseOrSource -> appColors.income
                                else -> appColors.expense
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = CurrencyFormatter.formatRupiah(effectiveBaseBalance),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TxWalletPickerSheet(
    title: String,
    wallets: List<WalletItem>,
    selectedWalletId: String,
    onSelect: (WalletItem) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    StandardModalBottomSheet(
        onDismissRequest = onDismiss,
        title = title,
        scrollable = true
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                wallets.forEachIndexed { index, item ->
                    val isSelected = item.id == selectedWalletId
                    val itemName = item.bankWithCustomBracket(context)
                    val itemCategory = item.customType ?: context.getString(item.typeRes)

                    Surface(
                        onClick = {
                            onSelect(item)
                            dismiss()
                        },
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        } else {
                            Color.Transparent
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FinLogoCircleBadge(
                                idOrName = item.providerKey,
                                size = 40.dp,
                                borderWidth = 1.dp,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = itemName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = "$itemCategory • ${CurrencyFormatter.formatRupiah(item.balance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (index < wallets.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                            thickness = 0.6.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TxFieldHeader(
    icon: ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TxFormGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun TxPurchasedItemEditorSheet(
    initialItem: PurchasedItem?,
    onSaveItem: (PurchasedItem) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember(initialItem) { mutableStateOf(initialItem?.name.orEmpty()) }
    var itemQty by remember(initialItem) { mutableIntStateOf(initialItem?.qty ?: 1) }
    var itemPriceRaw by remember(initialItem) {
        mutableStateOf(
            if ((initialItem?.unitPrice ?: 0L) > 0L) initialItem!!.unitPrice.toString() else ""
        )
    }
    val parsedUnitPrice = CurrencyFormatter.parseAmount(itemPriceRaw)
    val canSave = itemName.isNotBlank() && parsedUnitPrice > 0L

    StandardModalBottomSheet(
        onDismissRequest = onDismiss,
        title = if (initialItem != null) "Ubah Item Barang / Pesanan" else "Tambah Item Barang / Pesanan",
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { dismiss() },
                    shape = CircleShape,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.90f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Button(
                    onClick = {
                        if (canSave) {
                            dismiss {
                                onSaveItem(
                                    PurchasedItem(
                                        name = itemName.trim(),
                                        qty = itemQty,
                                        unitPrice = parsedUnitPrice
                                    )
                                )
                            }
                        }
                    },
                    enabled = canSave,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1.35f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Simpan Item",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Nama Barang / Menu") },
                placeholder = { Text("Contoh: Es Kopi Susu, Roti Tawar...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jumlah (Qty)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = { if (itemQty > 1) itemQty-- },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "${itemQty}x",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        onClick = { itemQty++ },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = if (itemPriceRaw.isEmpty()) "" else CurrencyFormatter.formatRupiah(parsedUnitPrice),
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    if (digits.length <= 12) itemPriceRaw = digits
                },
                label = { Text("Harga Satuan (Rp)") },
                placeholder = { Text("Rp 25.000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (parsedUnitPrice > 0L) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subtotal (${itemQty}x)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = CurrencyFormatter.formatRupiah(parsedUnitPrice * itemQty),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TxLocationPickerSheet(
    currentLocation: TransactionLocationData,
    onSelectLocation: (TransactionLocationData) -> Unit,
    onDismiss: () -> Unit
) {
    var placeName by remember(currentLocation) { mutableStateOf(currentLocation.placeName) }
    var address by remember(currentLocation) { mutableStateOf(currentLocation.address) }
    var lat by remember(currentLocation) { mutableStateOf(currentLocation.latitude) }
    var lng by remember(currentLocation) { mutableStateOf(currentLocation.longitude) }

    val previewLoc = remember(placeName, address, lat, lng) {
        TransactionLocationData(
            placeName = placeName.ifBlank { "Lokasi Transaksi" },
            address = address.ifBlank { "Jakarta, Indonesia" },
            latitude = lat,
            longitude = lng
        )
    }

    StandardModalBottomSheet(
        onDismissRequest = onDismiss,
        title = "Lokasi Pembayaran (Maps)",
        scrollable = true,
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { dismiss() },
                    shape = CircleShape,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.90f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Button(
                    onClick = {
                        dismiss {
                            onSelectLocation(previewLoc)
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1.35f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Terapkan Lokasi",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TransactionLocationMapCard(
                location = previewLoc,
                mapHeight = 110.dp
            )

            OutlinedTextField(
                value = placeName,
                onValueChange = { placeName = it },
                label = { Text("Nama Tempat / Merchant") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat Lengkap") },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            TxFieldHeader(
                icon = Icons.Outlined.LocationOn,
                title = "Pilih Lokasi Cepat"
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TransactionRichMetaCodec.presetLocations.forEachIndexed { idx, preset ->
                        val isSelected = preset.placeName.equals(placeName, ignoreCase = true)
                        Surface(
                            onClick = {
                                placeName = preset.placeName
                                address = preset.address
                                lat = preset.latitude
                                lng = preset.longitude
                            },
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                Color.Transparent
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.placeName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = preset.address,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        if (idx < TransactionRichMetaCodec.presetLocations.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                thickness = 0.6.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
                            )
                        }
                    }
                }
            }
        }
    }
}

