package com.aol.moneytracker.presentation.screens.wallet

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.content.Context
import com.aol.moneytracker.AddWalletActivity
import com.aol.moneytracker.R
import com.aol.moneytracker.TransferWalletActivity
import com.aol.moneytracker.presentation.components.FinCategory
import com.aol.moneytracker.presentation.components.FinInstitution
import com.aol.moneytracker.presentation.components.FinInstitutionSelectorSheet
import com.aol.moneytracker.presentation.components.FinInstitutionSelectorTrigger
import com.aol.moneytracker.presentation.components.FinLogoCircleBadge
import com.aol.moneytracker.presentation.components.FinLogoMapping
import com.aol.moneytracker.presentation.components.FinLogoSquircleBadge
import com.aol.moneytracker.presentation.components.StandardModalBottomSheet
import com.aol.moneytracker.presentation.components.TopFadeScrim
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter

data class WalletItem(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val accountNumberRes: Int,
    val balance: Long,
    @StringRes val typeRes: Int,
    val gradientColors: List<Color>,
    val icon: ImageVector,
    val providerKey: String = id,
    val customName: String? = null,
    val customType: String? = null,
    val customAccountNumber: String? = null,
    val showCategoryOnCard: Boolean = true,
    val showAccountNumberOnCard: Boolean = true,
    val isBalanceHidden: Boolean = false
)

object WalletStateHolder {
    val wallets = mutableStateListOf<WalletItem>()
    private var initialized = false

    fun ensureInitialized(context: Context) {
        if (!initialized) {
            initialized = true
            wallets.addAll(
                listOf(
                    WalletItem(
                        id = "bca",
                        providerKey = "bca",
                        nameRes = R.string.wallet_bca_name,
                        accountNumberRes = R.string.wallet_bca_number,
                        balance = 14500000L,
                        typeRes = R.string.wallet_bca_type,
                        customType = "Rekening Utama",
                        gradientColors = FinLogoMapping.resolveCardGradient(context, "bca", false),
                        icon = Icons.Default.AccountBalance
                    ),
                    WalletItem(
                        id = "mandiri",
                        providerKey = "mandiri",
                        nameRes = R.string.wallet_mandiri_name,
                        accountNumberRes = R.string.wallet_mandiri_number,
                        balance = 8250000L,
                        typeRes = R.string.wallet_mandiri_type,
                        customType = "Tabungan",
                        gradientColors = FinLogoMapping.resolveCardGradient(context, "mandiri", false),
                        icon = Icons.Default.CreditCard
                    ),
                    WalletItem(
                        id = "gopay",
                        providerKey = "gopay",
                        nameRes = R.string.wallet_gopay_name,
                        accountNumberRes = R.string.wallet_gopay_number,
                        balance = 1450000L,
                        typeRes = R.string.wallet_gopay_type,
                        customType = "Kebutuhan Harian",
                        gradientColors = FinLogoMapping.resolveCardGradient(context, "gopay", false),
                        icon = Icons.Default.Payments
                    ),
                    WalletItem(
                        id = "cash",
                        providerKey = "cash",
                        nameRes = R.string.wallet_cash_name,
                        accountNumberRes = R.string.wallet_cash_number,
                        balance = 650000L,
                        typeRes = R.string.wallet_cash_type,
                        customType = "Pegangan Harian",
                        gradientColors = FinLogoMapping.resolveCardGradient(context, "cash", false),
                        icon = Icons.Default.AccountBalanceWallet
                    )
                )
            )
        }
    }

    fun addWallet(newWallet: WalletItem) {
        wallets.add(newWallet)
    }

    fun updateWallet(updatedWallet: WalletItem) {
        val index = wallets.indexOfFirst { it.id == updatedWallet.id }
        if (index != -1) {
            wallets[index] = updatedWallet
        }
    }

    fun toggleBalanceVisibility(walletId: String) {
        val index = wallets.indexOfFirst { it.id == walletId }
        if (index != -1) {
            val current = wallets[index]
            wallets[index] = current.copy(isBalanceHidden = !current.isBalanceHidden)
        }
    }

    fun transferBalance(fromId: String, toId: String, amount: Long) {
        if (fromId == toId || amount <= 0L) return
        val fromIdx = wallets.indexOfFirst { it.id == fromId }
        val toIdx = wallets.indexOfFirst { it.id == toId }
        if (fromIdx != -1 && toIdx != -1) {
            val fromWallet = wallets[fromIdx]
            val toWallet = wallets[toIdx]
            if (fromWallet.balance >= amount) {
                wallets[fromIdx] = fromWallet.copy(balance = fromWallet.balance - amount)
                wallets[toIdx] = toWallet.copy(balance = toWallet.balance + amount)
            }
        }
    }

    fun adjustWalletBalance(walletId: String, delta: Long) {
        val idx = wallets.indexOfFirst { it.id == walletId }
        if (idx != -1) {
            val current = wallets[idx]
            wallets[idx] = current.copy(balance = (current.balance + delta).coerceAtLeast(0L))
        }
    }

    fun applyTransactionEffect(context: Context, transaction: com.aol.moneytracker.domain.model.Transaction) {
        ensureInitialized(context)
        if (transaction.categoryId.equals("transfer", ignoreCase = true)) {
            val info = TransactionWalletMeta.resolveTransfer(context, transaction)
            adjustWalletBalance(info.fromWalletId, -transaction.amount)
            adjustWalletBalance(info.toWalletId, +transaction.amount)
        } else {
            val info = TransactionWalletMeta.resolveNormal(context, transaction)
            val delta = if (transaction.type == com.aol.moneytracker.domain.model.TransactionType.INCOME) {
                +transaction.amount
            } else {
                -transaction.amount
            }
            adjustWalletBalance(info.walletId, delta)
        }
        com.aol.moneytracker.presentation.screens.notification.NotificationStateHolder.onTransactionRecorded(
            context = context,
            transaction = transaction
        )
    }

    fun revertTransactionEffect(context: Context, transaction: com.aol.moneytracker.domain.model.Transaction) {
        ensureInitialized(context)
        if (transaction.categoryId.equals("transfer", ignoreCase = true)) {
            val info = TransactionWalletMeta.resolveTransfer(context, transaction)
            adjustWalletBalance(info.fromWalletId, +transaction.amount)
            adjustWalletBalance(info.toWalletId, -transaction.amount)
        } else {
            val info = TransactionWalletMeta.resolveNormal(context, transaction)
            val delta = if (transaction.type == com.aol.moneytracker.domain.model.TransactionType.INCOME) {
                -transaction.amount
            } else {
                +transaction.amount
            }
            adjustWalletBalance(info.walletId, delta)
        }
    }

    fun updateTransactionEffect(
        context: Context,
        oldTransaction: com.aol.moneytracker.domain.model.Transaction,
        newTransaction: com.aol.moneytracker.domain.model.Transaction
    ) {
        revertTransactionEffect(context, oldTransaction)
        applyTransactionEffect(context, newTransaction)
    }

    fun deleteWallet(walletId: String) {
        if (wallets.size <= 1) return
        wallets.removeAll { it.id == walletId }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    selectedMonth: String = ""
) {
    val context = LocalContext.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    remember {
        WalletStateHolder.ensureInitialized(context)
        true
    }
    val wallets = WalletStateHolder.wallets

    val totalBalance = wallets.sumOf { it.balance }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBarHeight + 12.dp,
                bottom = 120.dp
            )
        ) {
            // Header Title
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.wallet_total_balance_all),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatRupiah(totalBalance),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Wallet Horizontal Cards Carousel
            item {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(wallets, key = { it.id }) { wallet ->
                        WalletCard(
                            wallet = wallet,
                            onClick = { AddWalletActivity.startEdit(context, wallet.id) },
                            onToggleBalanceVisibility = {
                                WalletStateHolder.toggleBalanceVisibility(wallet.id)
                            }
                        )
                    }
                }
            }

            // Sleek Bento Quick Actions: Transfer & Akun Baru
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WalletActionCard(
                        icon = Icons.Default.SwapHoriz,
                        title = stringResource(R.string.wallet_action_transfer),
                        subtitle = stringResource(R.string.wallet_action_transfer_sub),
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            if (wallets.size >= 2) {
                                TransferWalletActivity.start(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    WalletActionCard(
                        icon = Icons.Default.Add,
                        title = stringResource(R.string.wallet_action_new_account),
                        subtitle = stringResource(R.string.wallet_action_new_account_sub),
                        accentColor = MoneyTrackerTheme.colors.income,
                        onClick = { AddWalletActivity.start(context) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Detail Rincian Akun Section Header
            item {
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.wallet_accounts_list_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.wallet_accounts_count_format, wallets.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of Wallets synced with Wallet Configuration (showCategoryOnCard, isBalanceHidden)
            items(wallets, key = { "detail_${it.id}" }) { wallet ->
                val bankName = wallet.officialBankName(context)
                val customLabel = wallet.customLabelOrNull(context)
                val mainTitle = customLabel ?: bankName
                val walletType = wallet.customType ?: stringResource(wallet.typeRes)
                val logoKey = wallet.providerKey

                Surface(
                    onClick = { AddWalletActivity.startEdit(context, wallet.id) },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FinLogoCircleBadge(
                            idOrName = logoKey,
                            size = 44.dp,
                            borderWidth = 1.dp,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Left-Center: Default large title = Bank Name; if custom name exists -> Top = Custom Name, Bottom = Bank Name
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = mainTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (customLabel != null) {
                                Text(
                                    text = bankName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Right: Top = Balance, Bottom = Category (replacing account number)
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = if (wallet.isBalanceHidden) {
                                    "Rp ••••••••"
                                } else {
                                    CurrencyFormatter.formatRupiah(wallet.balance)
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (wallet.showCategoryOnCard && walletType.isNotBlank()) {
                                Text(
                                    text = walletType,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletCard(
    wallet: WalletItem,
    onClick: () -> Unit,
    onToggleBalanceVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = MoneyTrackerTheme.colors
    val onWalletCard = appColors.onWalletCard
    val bankName = wallet.officialBankName(context)
    val customLabel = wallet.customLabelOrNull(context)
    val mainTitle = customLabel ?: bankName
    val walletCategory = wallet.customType ?: stringResource(wallet.typeRes)
    val logoKey = wallet.providerKey
    val cardGradient = remember(logoKey, appColors.isDark) {
        FinLogoMapping.resolveCardGradient(context, logoKey, appColors.isDark)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, onWalletCard.copy(alpha = 0.14f)),
        modifier = modifier
            .width(268.dp)
            .height(158.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = cardGradient
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Left = Default Bank Name (or Custom Name + Bank Name below it) | Right = Bordered Official Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = mainTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = onWalletCard
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (customLabel != null) {
                            Text(
                                text = bankName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = onWalletCard.copy(alpha = 0.8f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    FinLogoCircleBadge(
                        idOrName = logoKey,
                        size = 40.dp,
                        borderWidth = 1.2.dp,
                        borderColor = onWalletCard.copy(alpha = 0.35f)
                    )
                }

                // Bottom Row: Left = Category (if enabled) + Balance | Right = Eye Visibility Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (wallet.showCategoryOnCard && walletCategory.isNotBlank()) {
                            Text(
                                text = walletCategory,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = onWalletCard.copy(alpha = 0.8f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = if (wallet.isBalanceHidden) {
                                "Rp ••••••••"
                            } else {
                                CurrencyFormatter.formatRupiah(wallet.balance)
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = onWalletCard
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        onClick = onToggleBalanceVisibility,
                        shape = CircleShape,
                        color = onWalletCard.copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, onWalletCard.copy(alpha = 0.25f)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (wallet.isBalanceHidden) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = null,
                                tint = onWalletCard,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Refined, proportional Bento Action Card for Transfer & Akun Baru:
 * - Soft translucent squircle icon matching the rest of the app (no harsh solid blue circle)
 * - Left-aligned Title + Subtitle + subtle chevron arrow so there is zero dead space
 */
@Composable
fun WalletActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
