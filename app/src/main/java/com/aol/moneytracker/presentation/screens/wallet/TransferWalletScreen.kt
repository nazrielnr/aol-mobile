package com.aol.moneytracker.presentation.screens.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import com.aol.moneytracker.presentation.components.FinLogoCircleBadge
import com.aol.moneytracker.presentation.components.StandardModalBottomSheet
import com.aol.moneytracker.presentation.components.UniversalSubScreenScaffold
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter

@Composable
fun TransferWalletScreen(
    wallets: List<WalletItem>,
    onBackClick: () -> Unit,
    onConfirmTransfer: (
        fromWallet: WalletItem,
        toWallet: WalletItem,
        amount: Long,
        note: String
    ) -> Unit
) {
    val context = LocalContext.current

    var fromWalletId by remember(wallets) {
        mutableStateOf(wallets.firstOrNull()?.id.orEmpty())
    }
    var toWalletId by remember(wallets) {
        mutableStateOf(wallets.getOrNull(1)?.id ?: wallets.firstOrNull()?.id.orEmpty())
    }

    val fromWallet = wallets.firstOrNull { it.id == fromWalletId } ?: wallets.first()
    val toWallet = wallets.firstOrNull { it.id == toWalletId }
        ?: wallets.firstOrNull { it.id != fromWallet.id }
        ?: wallets.first()

    var isSelectingSource by remember { mutableStateOf(false) }
    var isSelectingDestination by remember { mutableStateOf(false) }

    var rawAmount by remember { mutableStateOf("") }
    var transferNote by remember { mutableStateOf("") }

    val parsedAmount = CurrencyFormatter.parseAmount(rawAmount)
    val isInsufficientBalance = parsedAmount > 0L && parsedAmount > fromWallet.balance
    val isSameAccount = fromWallet.id == toWallet.id
    val canSubmit = parsedAmount > 0L && !isSameAccount && !isInsufficientBalance
    val listState = rememberLazyListState()

    UniversalSubScreenScaffold(
        title = stringResource(R.string.wallet_transfer_title),
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
                        if (canSubmit) {
                            onConfirmTransfer(
                                fromWallet,
                                toWallet,
                                parsedAmount,
                                transferNote.trim()
                            )
                        }
                    },
                    enabled = canSubmit,
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
                        text = stringResource(R.string.wallet_transfer_submit),
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
            // 1. Unified Clean Route Card (Visual Icon-to-Icon Connector + Swap Button)
            item {
                TransferGroupCard {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top Row: Source Wallet (Dari)
                            WalletEndpointSelectorRow(
                                wallet = fromWallet,
                                projectedBalance = if (parsedAmount > 0L) {
                                    (fromWallet.balance - parsedAmount).coerceAtLeast(0L)
                                } else {
                                    null
                                },
                                isSource = true,
                                isInsufficient = isInsufficientBalance,
                                topPadding = 18.dp,
                                bottomPadding = 26.dp,
                                onClick = { isSelectingSource = true }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 74.dp, end = 76.dp),
                                thickness = 0.8.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                            )

                            // Bottom Row: Destination Wallet (Ke)
                            WalletEndpointSelectorRow(
                                wallet = toWallet,
                                projectedBalance = if (parsedAmount > 0L) {
                                    toWallet.balance + parsedAmount
                                } else {
                                    null
                                },
                                isSource = false,
                                isInsufficient = false,
                                topPadding = 26.dp,
                                bottomPadding = 18.dp,
                                onClick = { isSelectingDestination = true }
                            )
                        }

                        // Visual Vertical Route Connector between Top Icon & Bottom Icon
                        TransferRouteVisualConnector(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                                .width(44.dp)
                                .height(48.dp)
                        )

                        // Floating Swap Direction Button on the right side of the divider
                        Surface(
                            onClick = {
                                val temp = fromWalletId
                                fromWalletId = toWalletId
                                toWalletId = temp
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
                                    contentDescription = "Tukar rekening sumber dan tujuan",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Nominal Transfer & Catatan (Clean Single Card)
            item {
                TransferGroupCard {
                    // Nominal Transfer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        TransferRowHeader(
                            icon = Icons.Outlined.Payments,
                            title = stringResource(R.string.wallet_transfer_amount_label)
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

                        // Insufficient Balance Warning
                        if (isInsufficientBalance) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val shortage = parsedAmount - fromWallet.balance
                            val fromName = fromWallet.customName ?: context.getString(fromWallet.nameRes)
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
                                    text = "Saldo $fromName kurang ${CurrencyFormatter.formatRupiah(shortage)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Nominal Buttons (+100rb, +500rb, +1jt, Semua)
                        val quickDeltas = listOf(
                            "+100rb" to 100_000L,
                            "+500rb" to 500_000L,
                            "+1jt" to 1_000_000L
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

                            // "Semua" chip fills maximum available balance
                            Surface(
                                onClick = {
                                    val maxTransfer = fromWallet.balance.coerceAtLeast(0L)
                                    rawAmount = if (maxTransfer > 0L) maxTransfer.toString() else ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                                border = BorderStroke(
                                    0.8.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Semua",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )

                    // Catatan Transfer (Opsional)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        TransferRowHeader(
                            icon = Icons.Outlined.Description,
                            title = "Catatan (Opsional)"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = transferNote,
                            onValueChange = { transferNote = it },
                            placeholder = {
                                Text(
                                    text = "Contoh: Top up GoPay, Tabungan bulanan...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
        }
    }

    // Bottom Modal Selector for Source Wallet
    if (isSelectingSource) {
        WalletPickerBottomSheet(
            title = "Pilih Rekening Sumber",
            wallets = wallets,
            selectedWalletId = fromWallet.id,
            onSelectWallet = { picked ->
                if (picked.id == toWalletId) {
                    toWalletId = fromWalletId
                }
                fromWalletId = picked.id
            },
            onDismiss = { isSelectingSource = false }
        )
    }

    // Bottom Modal Selector for Destination Wallet
    if (isSelectingDestination) {
        WalletPickerBottomSheet(
            title = "Pilih Rekening Tujuan",
            wallets = wallets,
            selectedWalletId = toWallet.id,
            onSelectWallet = { picked ->
                if (picked.id == fromWalletId) {
                    fromWalletId = toWalletId
                }
                toWalletId = picked.id
            },
            onDismiss = { isSelectingDestination = false }
        )
    }
}

@Composable
private fun TransferRouteVisualConnector(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .weight(1f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                width = 1.2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            ),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .width(2.dp)
                .weight(1f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun WalletEndpointSelectorRow(
    wallet: WalletItem,
    projectedBalance: Long?,
    isSource: Boolean,
    isInsufficient: Boolean,
    topPadding: Dp = 16.dp,
    bottomPadding: Dp = 16.dp,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appColors = MoneyTrackerTheme.colors
    val walletName = wallet.bankWithCustomBracket(context)

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 68.dp, top = topPadding, bottom = bottomPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FinLogoCircleBadge(
                idOrName = wallet.providerKey,
                size = 44.dp,
                borderWidth = 1.dp,
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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

                // Clean balance row: never truncates because it has the full row width
                if (projectedBalance != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = CurrencyFormatter.formatRupiah(wallet.balance),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
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
                                fontSize = 12.5.sp
                            ),
                            color = when {
                                isSource && isInsufficient -> MaterialTheme.colorScheme.error
                                !isSource -> appColors.income
                                else -> appColors.expense
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = CurrencyFormatter.formatRupiah(wallet.balance),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp
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
private fun WalletPickerBottomSheet(
    title: String,
    wallets: List<WalletItem>,
    selectedWalletId: String,
    onSelectWallet: (WalletItem) -> Unit,
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
                            onSelectWallet(item)
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
private fun TransferRowHeader(
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
private fun TransferGroupCard(
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
