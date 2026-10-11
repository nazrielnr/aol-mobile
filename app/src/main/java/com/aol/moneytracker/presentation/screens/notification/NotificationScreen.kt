package com.aol.moneytracker.presentation.screens.notification

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.components.EmptyStateView
import com.aol.moneytracker.presentation.components.FinLogoCircleBadge
import com.aol.moneytracker.presentation.components.StandardModalBottomSheet
import com.aol.moneytracker.presentation.components.UniversalSubScreenScaffold
import com.aol.moneytracker.presentation.components.getCategoryColor
import com.aol.moneytracker.presentation.components.getCategoryIcon
import com.aol.moneytracker.presentation.screens.wallet.TransactionWalletMeta
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.AppLanguage
import com.aol.moneytracker.util.CurrencyFormatter
import com.aol.moneytracker.util.DateFormatter
import com.aol.moneytracker.util.LanguageManager

enum class NotificationCategory {
    TRANSACTION,
    INSIGHT,
    REMINDER
}

enum class NotificationFilter(@StringRes val labelRes: Int) {
    ALL(R.string.notifications_filter_all),
    UNREAD(R.string.notifications_filter_unread),
    TRANSACTIONS(R.string.notifications_filter_transactions),
    REMINDERS(R.string.notifications_filter_reminders)
}

data class AppNotification(
    val id: String,
    val titleId: String,
    val titleEn: String,
    val messageId: String,
    val messageEn: String,
    val tagId: String,
    val tagEn: String,
    val category: NotificationCategory,
    val iconKey: String,
    val timestampMillis: Long,
    val isRead: Boolean = false,
    val amount: Long? = null,
    val transactionType: TransactionType? = null,
    val isTransfer: Boolean = false,
    val walletProviderKey: String? = null,
    val walletLabel: String? = null,
    val walletCategoryLabel: String? = null,
    val locationLabel: String? = null
) {
    fun localizedTitle(isEnglish: Boolean): String = if (isEnglish) titleEn else titleId
    fun localizedMessage(isEnglish: Boolean): String = if (isEnglish) messageEn else messageId
    fun localizedTag(isEnglish: Boolean): String = if (isEnglish) tagEn else tagId
}

/**
 * Process-wide reactive Notification State Holder.
 * Keeps the unread indicator in [com.aol.moneytracker.presentation.components.GlobalTopBar] and [NotificationScreen]
 * synchronized in real time across Activities.
 */
object NotificationStateHolder {
    val notifications = mutableStateListOf<AppNotification>()
    private var initialized = false

    val unreadCount: Int
        get() {
            ensureInitialized()
            return notifications.count { !it.isRead }
        }

    val hasUnread: Boolean
        get() = unreadCount > 0

    fun ensureInitialized() {
        if (!initialized) {
            initialized = true
            notifications.addAll(buildDefaultNotifications())
        }
    }

    fun markAsRead(id: String) {
        ensureInitialized()
        val index = notifications.indexOfFirst { it.id == id }
        if (index != -1 && !notifications[index].isRead) {
            notifications[index] = notifications[index].copy(isRead = true)
        }
    }

    fun toggleReadStatus(id: String) {
        ensureInitialized()
        val index = notifications.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = notifications[index]
            notifications[index] = current.copy(isRead = !current.isRead)
        }
    }

    fun markAllAsRead() {
        ensureInitialized()
        for (i in notifications.indices) {
            if (!notifications[i].isRead) {
                notifications[i] = notifications[i].copy(isRead = true)
            }
        }
    }

    fun deleteNotification(id: String) {
        ensureInitialized()
        notifications.removeAll { it.id == id }
    }

    fun restoreDefaultNotifications() {
        initialized = true
        notifications.clear()
        notifications.addAll(buildDefaultNotifications())
    }

    /**
     * Automatically pushes a live unread notification whenever a new transaction or transfer is recorded.
     */
    fun onTransactionRecorded(context: Context, transaction: Transaction) {
        ensureInitialized()
        val isTransfer = transaction.categoryId.equals("transfer", ignoreCase = true)
        val formattedAmount = CurrencyFormatter.formatRupiah(transaction.amount)

        if (isTransfer) {
            val info = TransactionWalletMeta.resolveTransfer(context, transaction)
            notifications.add(
                0,
                AppNotification(
                    id = "tx_live_${System.currentTimeMillis()}",
                    titleId = transaction.title.ifBlank { "Transfer Antar Rekening" },
                    titleEn = transaction.title.ifBlank { "Account Transfer" },
                    messageId = "Transfer saldo sebesar $formattedAmount (${info.routeLabel}) berhasil diproses.",
                    messageEn = "Balance transfer of $formattedAmount (${info.routeLabel}) has been processed.",
                    tagId = "Transfer Saldo",
                    tagEn = "Transfer",
                    category = NotificationCategory.TRANSACTION,
                    iconKey = "swap_horiz",
                    timestampMillis = transaction.dateMillis,
                    isRead = false,
                    amount = transaction.amount,
                    transactionType = TransactionType.EXPENSE,
                    isTransfer = true,
                    walletProviderKey = info.fromProviderKey,
                    walletLabel = info.routeLabel,
                    walletCategoryLabel = info.fromTopWalletInfo
                )
            )
        } else {
            val info = TransactionWalletMeta.resolveNormal(context, transaction)
            val category = Category.findCategory(transaction.categoryId, transaction.type)
            val isIncome = transaction.type == TransactionType.INCOME
            notifications.add(
                0,
                AppNotification(
                    id = "tx_live_${System.currentTimeMillis()}",
                    titleId = transaction.title,
                    titleEn = transaction.title,
                    messageId = if (isIncome) {
                        "Pemasukan sebesar $formattedAmount telah ditambahkan ke ${info.bankWithCustomLabel}."
                    } else {
                        "Pengeluaran sebesar $formattedAmount dari ${info.bankWithCustomLabel} telah tercatat."
                    },
                    messageEn = if (isIncome) {
                        "Income of $formattedAmount has been added to ${info.bankWithCustomLabel}."
                    } else {
                        "Expense of $formattedAmount from ${info.bankWithCustomLabel} has been recorded."
                    },
                    tagId = if (isIncome) "Pemasukan" else "Pengeluaran",
                    tagEn = if (isIncome) "Income" else "Expense",
                    category = NotificationCategory.TRANSACTION,
                    iconKey = category.iconKey,
                    timestampMillis = transaction.dateMillis,
                    isRead = false,
                    amount = transaction.amount,
                    transactionType = transaction.type,
                    isTransfer = false,
                    walletProviderKey = info.providerKey,
                    walletLabel = info.bankWithCustomLabel,
                    walletCategoryLabel = info.topWalletInfoLabel
                )
            )
        }
    }

    private fun buildDefaultNotifications(): List<AppNotification> {
        val now = System.currentTimeMillis()
        val oneHour = 3600_000L
        val oneDay = 86400_000L

        return listOf(
            // HARI INI (3 Unread)
            AppNotification(
                id = "notif_fuel_bca",
                titleId = "Isi Bahan Bakar Mobil",
                titleEn = "Car Fuel Refill",
                messageId = "Pembayaran Rp 250.000 di SPBU Shell Gatot Subroto tercatat pada kategori Transportasi.",
                messageEn = "Payment of Rp 250.000 at Shell Gatot Subroto recorded under Transportation.",
                tagId = "Transportasi",
                tagEn = "Transportation",
                category = NotificationCategory.TRANSACTION,
                iconKey = "directions_car",
                timestampMillis = now - (2 * oneHour),
                isRead = false,
                amount = 250_000L,
                transactionType = TransactionType.EXPENSE,
                walletProviderKey = "bca",
                walletLabel = "Bank BCA",
                walletCategoryLabel = "Rekening Utama",
                locationLabel = "SPBU Shell • Gatot Subroto"
            ),
            AppNotification(
                id = "notif_ocr_coffee",
                titleId = "Makan Siang & Kopi",
                titleEn = "Lunch & Coffee",
                messageId = "Transaksi Rp 55.000 dari pindai struk berhasil disimpan ke GoPay.",
                messageEn = "Receipt transaction of Rp 55.000 has been saved to GoPay.",
                tagId = "Makanan & Minuman",
                tagEn = "Food & Drink",
                category = NotificationCategory.TRANSACTION,
                iconKey = "restaurant",
                timestampMillis = now - (4 * oneHour),
                isRead = false,
                amount = 55_000L,
                transactionType = TransactionType.EXPENSE,
                walletProviderKey = "gopay",
                walletLabel = "GoPay",
                walletCategoryLabel = "Kebutuhan Harian",
                locationLabel = "Kopi Kenangan • Grand Indonesia"
            ),
            AppNotification(
                id = "notif_insight_weekly",
                titleId = "Ringkasan Arus Kas Minggu Ini",
                titleEn = "Weekly Cash Flow Summary",
                messageId = "Pengeluaran minggu ini lebih hemat Rp 50.000 dibanding minggu lalu. Rasio tabungan berada di 68%.",
                messageEn = "Weekly spending is Rp 50.000 lower than last week. Your savings rate is at 68%.",
                tagId = "Ringkasan Keuangan",
                tagEn = "Financial Summary",
                category = NotificationCategory.INSIGHT,
                iconKey = "insight",
                timestampMillis = now - (6 * oneHour),
                isRead = false
            ),

            // KEMARIN (Read)
            AppNotification(
                id = "notif_bills_mandiri",
                titleId = "Tagihan Internet & Listrik",
                titleEn = "Internet & Electricity Bill",
                messageId = "Pembayaran tagihan bulanan Rp 420.000 melalui Bank Mandiri telah tercatat.",
                messageEn = "Monthly bill payment of Rp 420.000 via Bank Mandiri has been recorded.",
                tagId = "Tagihan & Utilitas",
                tagEn = "Bills & Utilities",
                category = NotificationCategory.TRANSACTION,
                iconKey = "receipt_long",
                timestampMillis = now - (1 * oneDay) - (2 * oneHour),
                isRead = true,
                amount = 420_000L,
                transactionType = TransactionType.EXPENSE,
                walletProviderKey = "mandiri",
                walletLabel = "Bank Mandiri",
                walletCategoryLabel = "Tabungan"
            ),
            AppNotification(
                id = "notif_budget_shopping",
                titleId = "Batas Anggaran Belanja",
                titleEn = "Shopping Budget Alert",
                messageId = "Pengeluaran kategori Belanja bulan ini mencapai Rp 915.000 (76% dari anggaran bulanan).",
                messageEn = "Shopping expenses this month reached Rp 915.000 (76% of monthly budget).",
                tagId = "Anggaran",
                tagEn = "Budget",
                category = NotificationCategory.REMINDER,
                iconKey = "shopping_bag",
                timestampMillis = now - (1 * oneDay) - (5 * oneHour),
                isRead = true,
                amount = 915_000L,
                transactionType = TransactionType.EXPENSE,
                locationLabel = "Ranch Market • Pondok Indah"
            ),
            AppNotification(
                id = "notif_daily_reminder",
                titleId = "Pengingat Catat Transaksi",
                titleEn = "Daily Transaction Reminder",
                messageId = "Jangan lupa catat pengeluaran tunai hari ini agar saldo dompet tetap akurat.",
                messageEn = "Remember to log your cash expenses today to keep your wallet balances accurate.",
                tagId = "Pengingat Harian",
                tagEn = "Daily Reminder",
                category = NotificationCategory.REMINDER,
                iconKey = "reminder",
                timestampMillis = now - (1 * oneDay) - (7 * oneHour),
                isRead = true
            ),

            // 2 HARI LALU (Read)
            AppNotification(
                id = "notif_freelance_income",
                titleId = "Proyek Mobile App Freelance",
                titleEn = "Mobile App Freelance Project",
                messageId = "Pemasukan termin 1 sebesar Rp 3.000.000 telah ditambahkan ke saldo Bank BCA.",
                messageEn = "Phase 1 payment of Rp 3.000.000 has been added to your Bank BCA balance.",
                tagId = "Pemasukan",
                tagEn = "Income",
                category = NotificationCategory.TRANSACTION,
                iconKey = "laptop_mac",
                timestampMillis = now - (2 * oneDay) - (3 * oneHour),
                isRead = true,
                amount = 3_000_000L,
                transactionType = TransactionType.INCOME,
                walletProviderKey = "bca",
                walletLabel = "Bank BCA",
                walletCategoryLabel = "Rekening Utama"
            ),
            AppNotification(
                id = "notif_cloud_backup",
                titleId = "Cadangan Data Tersimpan",
                titleEn = "Data Backup Completed",
                messageId = "Riwayat transaksi dan pengaturan dompet telah dicadangkan ke penyimpanan perangkat.",
                messageEn = "Transaction history and wallet settings have been backed up.",
                tagId = "Cadangan Data",
                tagEn = "Data Backup",
                category = NotificationCategory.REMINDER,
                iconKey = "cloud_backup",
                timestampMillis = now - (2 * oneDay) - (8 * oneHour),
                isRead = true
            )
        )
    }
}

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotificationStateHolder.ensureInitialized()
    val notifications = NotificationStateHolder.notifications
    val unreadCount = notifications.count { !it.isRead }

    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == AppLanguage.ENGLISH

    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var selectedNotificationForDetail by remember { mutableStateOf<AppNotification?>(null) }

    val filteredNotifications = remember(notifications.toList(), selectedFilter) {
        notifications.filter { item ->
            when (selectedFilter) {
                NotificationFilter.ALL -> true
                NotificationFilter.UNREAD -> !item.isRead
                NotificationFilter.TRANSACTIONS -> item.category == NotificationCategory.TRANSACTION
                NotificationFilter.REMINDERS ->
                    item.category == NotificationCategory.REMINDER || item.category == NotificationCategory.INSIGHT
            }
        }
    }

    val groupedNotifications = remember(filteredNotifications, currentLanguage) {
        filteredNotifications.groupBy { DateFormatter.formatDateGroup(it.timestampMillis) }
    }

    val listState = rememberLazyListState()

    UniversalSubScreenScaffold(
        title = stringResource(R.string.notifications_title),
        onBackClick = onBackClick,
        isScrolledOverride = listState.canScrollBackward,
        modifier = modifier,
        actions = {
            if (unreadCount > 0) {
                Surface(
                    onClick = { NotificationStateHolder.markAllAsRead() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.notifications_mark_all_read),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding
        ) {
            // 1. Clean Filter Pills Row (Directly at top, no redundant summary banner card)
            item(key = "notif_filter_pills") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 6.dp)
                ) {
                    items(
                        items = NotificationFilter.entries,
                        key = { it.name }
                    ) { filter ->
                        val isSelected = selectedFilter == filter
                        val badgeCount = when (filter) {
                            NotificationFilter.UNREAD -> unreadCount
                            else -> 0
                        }
                        NotificationFilterChip(
                            label = stringResource(filter.labelRes),
                            badgeCount = badgeCount,
                            selected = isSelected,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }

            // 2. Grouped Notification Items or Empty State
            if (filteredNotifications.isEmpty()) {
                item(key = "notif_empty_state") {
                    Spacer(modifier = Modifier.height(36.dp))
                    val emptyMsg = when (selectedFilter) {
                        NotificationFilter.UNREAD -> stringResource(R.string.notifications_empty_unread)
                        else -> stringResource(R.string.notifications_empty_category)
                    }
                    EmptyStateView(message = emptyMsg)

                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = { NotificationStateHolder.restoreDefaultNotifications() },
                                shape = CircleShape,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.notifications_action_reset_demo),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                groupedNotifications.entries.forEachIndexed { index, (dateGroup, itemsInGroup) ->
                    item(key = "header_$dateGroup") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 12.dp, bottom = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                    thickness = 1.dp
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 4.dp,
                                        bottom = 6.dp
                                    ),
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

                                Text(
                                    text = stringResource(
                                        R.string.notifications_count_format,
                                        itemsInGroup.size
                                    ),
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
                        items = itemsInGroup,
                        key = { it.id }
                    ) { notification ->
                        NotificationItemCard(
                            notification = notification,
                            isEnglish = isEnglish,
                            onClick = {
                                NotificationStateHolder.markAsRead(notification.id)
                                selectedNotificationForDetail = notification.copy(isRead = true)
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Modal Bottom Sheet
    selectedNotificationForDetail?.let { activeNotif ->
        val liveNotif = notifications.firstOrNull { it.id == activeNotif.id } ?: activeNotif
        NotificationDetailBottomSheet(
            notification = liveNotif,
            isEnglish = isEnglish,
            onDismiss = { selectedNotificationForDetail = null },
            onToggleRead = {
                NotificationStateHolder.toggleReadStatus(liveNotif.id)
                selectedNotificationForDetail = null
            },
            onDelete = {
                NotificationStateHolder.deleteNotification(liveNotif.id)
                selectedNotificationForDetail = null
            }
        )
    }
}

@Composable
private fun NotificationFilterChip(
    label: String,
    badgeCount: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(150),
        label = "filterChipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        animationSpec = tween(150),
        label = "filterChipBorder"
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        modifier = Modifier.height(34.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp
                ),
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                            } else {
                                MoneyTrackerTheme.colors.notificationBadge
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        ),
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            Color.White
                        }
                    )
                }
            }
        }
    }
}

/**
 * Clean, human-crafted Notification Card matching `ModernTransactionCard` proportions:
 * - No cluttered colored tag pills above the title.
 * - No gaudy colored card borders.
 * - Left: 44dp squircle icon with subtle unread dot badge.
 * - Right: Title + Time on top line, natural 2-line message below, and compact wallet + amount row for transactions.
 */
@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    isEnglish: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnread = !notification.isRead
    val accentColor = resolveNotificationAccentColor(notification)
    val leadIcon = resolveNotificationIcon(notification.iconKey)
    val cardBgColor = MaterialTheme.colorScheme.surfaceContainer

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = cardBgColor,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Squircle Icon (44dp, RoundedCornerShape(13.dp) matching ModernTransactionCard) + Unread Dot
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(13.dp))
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = leadIcon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(cardBgColor)
                            .padding(1.5.dp)
                            .clip(CircleShape)
                            .background(MoneyTrackerTheme.colors.notificationBadge)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Row 1: Title + Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.localizedTitle(isEnglish),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = DateFormatter.formatTime(notification.timestampMillis),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    )
                }

                // Row 2: Concise Message Body
                Text(
                    text = notification.localizedMessage(isEnglish),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 17.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isUnread) 0.92f else 0.75f
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Row 3: Compact Wallet & Amount Footer (only for transaction items)
                if (notification.walletProviderKey != null || notification.amount != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (notification.walletProviderKey != null && notification.walletLabel != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                FinLogoCircleBadge(
                                    idOrName = notification.walletProviderKey,
                                    size = 18.dp
                                )
                                Text(
                                    text = notification.walletLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (notification.amount != null) {
                            val isIncome = notification.transactionType == TransactionType.INCOME
                            val prefix = when {
                                notification.isTransfer -> "⇄ "
                                isIncome -> "+ "
                                else -> "- "
                            }
                            val amountColor = when {
                                notification.isTransfer -> MaterialTheme.colorScheme.primary
                                isIncome -> MoneyTrackerTheme.colors.income
                                else -> MaterialTheme.colorScheme.error
                            }
                            Text(
                                text = prefix + CurrencyFormatter.formatRupiah(notification.amount),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = amountColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailBottomSheet(
    notification: AppNotification,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onToggleRead: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = resolveNotificationAccentColor(notification)
    val leadIcon = resolveNotificationIcon(notification.iconKey)

    StandardModalBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.notifications_detail_title),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { dismiss { onDelete() } },
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.90f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_delete),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = { dismiss() },
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
                        text = stringResource(R.string.action_close),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Pill Badge for Notification Tag
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(1.dp, accentColor.copy(alpha = 0.30f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = leadIcon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = notification.localizedTag(isEnglish),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = accentColor
                    )
                }
            }

            // 2. Optional Nominal Display
            if (notification.amount != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val isIncome = notification.transactionType == TransactionType.INCOME
                val prefix = when {
                    notification.isTransfer -> "⇄ "
                    isIncome -> "+ "
                    else -> "- "
                }
                val amountColor = when {
                    notification.isTransfer -> MaterialTheme.colorScheme.primary
                    isIncome -> MoneyTrackerTheme.colors.income
                    else -> MaterialTheme.colorScheme.error
                }
                Text(
                    text = prefix + CurrencyFormatter.formatRupiah(notification.amount),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = amountColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Notification Title
            Text(
                text = notification.localizedTitle(isEnglish),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Full Message Description
            Text(
                text = notification.localizedMessage(isEnglish),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Breakdown Details Card (Matching TransactionDetailBottomSheet & AddTransactionScreen field headers)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row 1: Category / Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NotificationDetailLeadLabel(
                            leadIcon = Icons.Outlined.Category,
                            label = stringResource(R.string.detail_label_category)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = leadIcon,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = notification.localizedTag(isEnglish),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Row 2: Associated Wallet (Stacked 2-line format matching TransactionDetailBottomSheet)
                    if (notification.walletProviderKey != null && notification.walletLabel != null) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 0.8.dp
                        )

                        val isIncome = notification.transactionType == TransactionType.INCOME
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NotificationDetailLeadLabel(
                                leadIcon = Icons.Outlined.AccountBalanceWallet,
                                label = if (isIncome) "Rekening Tujuan" else stringResource(R.string.detail_label_payment_source)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f, fill = false),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!notification.walletCategoryLabel.isNullOrBlank()) {
                                    Text(
                                        text = notification.walletCategoryLabel,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
                                ) {
                                    FinLogoCircleBadge(
                                        idOrName = notification.walletProviderKey,
                                        size = 19.dp,
                                        borderWidth = 0.8.dp,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                    Text(
                                        text = notification.walletLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = if (notification.walletCategoryLabel.isNullOrBlank()) 13.5.sp else 12.sp,
                                            fontWeight = if (notification.walletCategoryLabel.isNullOrBlank()) FontWeight.SemiBold else FontWeight.Medium
                                        ),
                                        color = if (notification.walletCategoryLabel.isNullOrBlank()) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
                                        },
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Row 3: Location (if present)
                    if (notification.locationLabel != null) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 0.8.dp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NotificationDetailLeadLabel(
                                leadIcon = Icons.Outlined.LocationOn,
                                label = if (isEnglish) "Location" else "Lokasi Pembayaran"
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = notification.locationLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                        thickness = 0.8.dp
                    )

                    // Row 4: Timestamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NotificationDetailLeadLabel(
                            leadIcon = Icons.Outlined.CalendarToday,
                            label = stringResource(R.string.detail_label_timestamp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f, fill = false),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = DateFormatter.formatTime(notification.timestampMillis),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = DateFormatter.formatDateOnly(notification.timestampMillis),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                        thickness = 0.8.dp
                    )

                    // Row 5: Quick Action to Mark as Unread
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NotificationDetailLeadLabel(
                            leadIcon = Icons.Outlined.MarkEmailUnread,
                            label = stringResource(R.string.notifications_detail_status)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            onClick = { dismiss { onToggleRead() } },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = BorderStroke(
                                0.8.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Text(
                                text = if (isEnglish) "Mark as Unread" else "Tandai Belum Dibaca",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailLeadLabel(
    leadIcon: ImageVector,
    label: String
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
                imageVector = leadIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun resolveNotificationAccentColor(notification: AppNotification): Color {
    return when (notification.iconKey) {
        "directions_car" -> getCategoryColor("transport")
        "restaurant" -> getCategoryColor("food")
        "shopping_bag" -> getCategoryColor("shopping")
        "receipt_long" -> getCategoryColor("bills")
        "laptop_mac" -> getCategoryColor("freelance")
        "swap_horiz" -> MaterialTheme.colorScheme.primary
        "insight" -> MoneyTrackerTheme.colors.insightAccent
        "reminder" -> getCategoryColor("bonus")
        "cloud_backup" -> getCategoryColor("savings")
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun resolveNotificationIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "insight" -> Icons.Outlined.TrendingUp
        "reminder" -> Icons.Outlined.Notifications
        "cloud_backup" -> Icons.Default.CloudDone
        else -> getCategoryIcon(iconKey)
    }
}
