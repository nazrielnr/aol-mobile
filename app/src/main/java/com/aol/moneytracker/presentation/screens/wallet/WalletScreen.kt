package com.aol.moneytracker.presentation.screens.wallet

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
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
    val icon: ImageVector
)

@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    selectedMonth: String = ""
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val appColors = MoneyTrackerTheme.colors

    val wallets = remember(appColors) {
        listOf(
            WalletItem(
                id = "bca",
                nameRes = R.string.wallet_bca_name,
                accountNumberRes = R.string.wallet_bca_number,
                balance = 14500000L,
                typeRes = R.string.wallet_bca_type,
                gradientColors = appColors.walletBcaGradient,
                icon = Icons.Default.AccountBalance
            ),
            WalletItem(
                id = "mandiri",
                nameRes = R.string.wallet_mandiri_name,
                accountNumberRes = R.string.wallet_mandiri_number,
                balance = 8250000L,
                typeRes = R.string.wallet_mandiri_type,
                gradientColors = appColors.walletMandiriGradient,
                icon = Icons.Default.CreditCard
            ),
            WalletItem(
                id = "gopay",
                nameRes = R.string.wallet_gopay_name,
                accountNumberRes = R.string.wallet_gopay_number,
                balance = 1450000L,
                typeRes = R.string.wallet_gopay_type,
                gradientColors = appColors.walletGopayGradient,
                icon = Icons.Default.Payments
            ),
            WalletItem(
                id = "cash",
                nameRes = R.string.wallet_cash_name,
                accountNumberRes = R.string.wallet_cash_number,
                balance = 650000L,
                typeRes = R.string.wallet_cash_type,
                gradientColors = appColors.walletCashGradient,
                icon = Icons.Default.AccountBalanceWallet
            )
        )
    }

    val totalBalance = remember(wallets) { wallets.sumOf { it.balance } }
    var selectedWalletId by remember { mutableStateOf(wallets.first().id) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Fade Scrim
        TopFadeScrim(
            modifier = Modifier.align(Alignment.TopCenter)
        )

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
                            isSelected = wallet.id == selectedWalletId,
                            onClick = { selectedWalletId = wallet.id }
                        )
                    }
                }
            }

            // Quick Actions: Transfer, Top Up, Tambah Rekening
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WalletActionButton(
                        icon = Icons.Default.SwapHoriz,
                        label = stringResource(R.string.wallet_action_transfer),
                        modifier = Modifier.weight(1f)
                    )
                    WalletActionButton(
                        icon = Icons.Default.Payments,
                        label = stringResource(R.string.wallet_action_topup),
                        modifier = Modifier.weight(1f)
                    )
                    WalletActionButton(
                        icon = Icons.Default.Add,
                        label = stringResource(R.string.wallet_action_new_account),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Detail Rincian Akun Section Header
            item {
                Spacer(modifier = Modifier.height(28.dp))
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            // List of Wallets with Progress Bar / Distribution
            items(wallets, key = { "detail_${it.id}" }) { wallet ->
                val ratio = if (totalBalance > 0) wallet.balance.toFloat() / totalBalance.toFloat() else 0f
                val percentage = (ratio * 100).toInt()
                val walletName = stringResource(wallet.nameRes)
                val walletType = stringResource(wallet.typeRes)
                val walletNumber = stringResource(wallet.accountNumberRes)

                Surface(
                    onClick = { selectedWalletId = wallet.id },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        1.dp,
                        if (wallet.id == selectedWalletId) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = wallet.gradientColors.first().copy(alpha = 0.16f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = wallet.icon,
                                    contentDescription = walletName,
                                    tint = wallet.gradientColors.first(),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = walletName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.wallet_distribution_format, walletType, percentage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.formatRupiah(wallet.balance),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = walletNumber,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onWalletCard = MoneyTrackerTheme.colors.onWalletCard

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) onWalletCard.copy(alpha = 0.5f) else Color.Transparent
        ),
        modifier = modifier
            .width(260.dp)
            .height(155.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = wallet.gradientColors
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(wallet.nameRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = onWalletCard
                        )
                    )
                    Icon(
                        imageVector = wallet.icon,
                        contentDescription = null,
                        tint = onWalletCard.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(wallet.accountNumberRes),
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = 1.sp,
                            color = onWalletCard.copy(alpha = 0.75f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatRupiah(wallet.balance),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = onWalletCard
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun WalletActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = modifier.height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
