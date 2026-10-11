package com.aol.moneytracker.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.AddTransactionActivity
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.screens.wallet.TransactionWalletMeta
import com.aol.moneytracker.presentation.screens.wallet.WalletStateHolder
import com.aol.moneytracker.presentation.screens.wallet.bankWithCustomBracket
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter
import com.aol.moneytracker.util.DateFormatter
import java.util.Locale
import kotlin.math.abs

/**
 * Represents a single purchased item or ordered menu entry in a transaction.
 */
data class PurchasedItem(
    val name: String,
    val qty: Int = 1,
    val unitPrice: Long = 0L
) {
    val subtotal: Long
        get() = qty.coerceAtLeast(1).toLong() * unitPrice.coerceAtLeast(0L)
}

/**
 * Represents the payment location and Google Maps coordinates for a transaction.
 */
data class TransactionLocationData(
    val placeName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
) {
    val formattedCoordinates: String
        get() = String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
}

/**
 * Unified rich metadata for a Transaction (Items + Payment Location Maps + Plain Note).
 */
data class TransactionRichMeta(
    val items: List<PurchasedItem>,
    val location: TransactionLocationData?,
    val plainNote: String
)

/**
 * Encodes and decodes `TransactionRichMeta` inside `Transaction.note` so that:
 * 1. Camera Scan, Voice Input, Full-Screen Add/Edit Transaction, and Transaction Detail
 *    all share the exact same structured items & Maps location data.
 * 2. Existing/seeded transactions automatically receive realistic fallback Maps & itemized data.
 */
object TransactionRichMetaCodec {
    private const val META_PREFIX = "@meta2:"
    private const val SECTION_SEP = "||"
    private const val ITEM_SEP = ";;"
    private const val FIELD_SEP = "::"

    val presetLocations = listOf(
        TransactionLocationData(
            placeName = "Kopi Kenangan • Grand Indonesia",
            address = "Jl. M.H. Thamrin No.1, Menteng, Jakarta Pusat",
            latitude = -6.1952,
            longitude = 106.8213
        ),
        TransactionLocationData(
            placeName = "Indomaret Point • Sudirman",
            address = "Jl. Jend. Sudirman Kav. 52, Senayan, Jakarta Selatan",
            latitude = -6.2246,
            longitude = 106.8096
        ),
        TransactionLocationData(
            placeName = "SPBU Shell • Gatot Subroto",
            address = "Jl. Gatot Subroto Kav. 38, Kuningan, Jakarta Selatan",
            latitude = -6.2381,
            longitude = 106.8254
        ),
        TransactionLocationData(
            placeName = "Ranch Market • Pondok Indah",
            address = "Jl. Metro Pondok Indah, Kebayoran Lama, Jakarta Selatan",
            latitude = -6.2655,
            longitude = 106.7843
        ),
        TransactionLocationData(
            placeName = "Kawasan Kuliner SCBD",
            address = "Jl. Jend. Sudirman Kav. 52-53, SCBD, Jakarta Selatan",
            latitude = -6.2271,
            longitude = 106.8086
        ),
        TransactionLocationData(
            placeName = "Central Park Mall • Tanjung Duren",
            address = "Jl. Letjen S. Parman Kav. 28, Jakarta Barat",
            latitude = -6.1774,
            longitude = 106.7907
        )
    )

    private fun sanitize(input: String): String {
        return input
            .replace(SECTION_SEP, " ")
            .replace(ITEM_SEP, ",")
            .replace(FIELD_SEP, " - ")
            .trim()
    }

    fun encode(
        items: List<PurchasedItem>,
        location: TransactionLocationData?,
        plainNote: String
    ): String {
        val cleanItems = items.filter { it.name.isNotBlank() }
        val cleanNote = sanitize(plainNote)
        if (cleanItems.isEmpty() && location == null) {
            return cleanNote
        }

        val locPart = if (location != null && location.placeName.isNotBlank()) {
            listOf(
                sanitize(location.placeName),
                sanitize(location.address),
                location.latitude.toString(),
                location.longitude.toString()
            ).joinToString(FIELD_SEP)
        } else {
            ""
        }

        val itemsPart = cleanItems.joinToString(ITEM_SEP) { item ->
            listOf(
                sanitize(item.name),
                item.qty.coerceAtLeast(1).toString(),
                item.unitPrice.coerceAtLeast(0L).toString()
            ).joinToString(FIELD_SEP)
        }

        return "$META_PREFIX$locPart$SECTION_SEP$itemsPart$SECTION_SEP$cleanNote"
    }

    fun resolve(transaction: Transaction): TransactionRichMeta {
        return decodeOrDefault(
            rawNote = transaction.note,
            title = transaction.title,
            amount = transaction.amount,
            categoryId = transaction.categoryId,
            type = transaction.type
        )
    }

    fun decodeOrDefault(
        rawNote: String,
        title: String,
        amount: Long,
        categoryId: String?,
        type: TransactionType
    ): TransactionRichMeta {
        val isTransfer = categoryId.equals("transfer", ignoreCase = true)
        if (rawNote.startsWith(META_PREFIX)) {
            val payload = rawNote.removePrefix(META_PREFIX)
            val sections = payload.split(SECTION_SEP)
            val locRaw = sections.getOrNull(0).orEmpty()
            val itemsRaw = sections.getOrNull(1).orEmpty()
            val plainNote = sections.getOrNull(2).orEmpty()

            val location = if (locRaw.isNotBlank()) {
                val lp = locRaw.split(FIELD_SEP)
                val pName = lp.getOrNull(0).orEmpty()
                val addr = lp.getOrNull(1).orEmpty()
                val lat = lp.getOrNull(2)?.toDoubleOrNull() ?: -6.2088
                val lng = lp.getOrNull(3)?.toDoubleOrNull() ?: 106.8456
                if (pName.isNotBlank()) {
                    TransactionLocationData(pName, addr, lat, lng)
                } else null
            } else null

            val items = if (itemsRaw.isNotBlank()) {
                itemsRaw.split(ITEM_SEP).mapNotNull { entry ->
                    val ip = entry.split(FIELD_SEP)
                    val name = ip.getOrNull(0).orEmpty().trim()
                    val qty = ip.getOrNull(1)?.toIntOrNull() ?: 1
                    val price = ip.getOrNull(2)?.toLongOrNull() ?: 0L
                    if (name.isNotBlank()) PurchasedItem(name, qty, price) else null
                }
            } else emptyList()

            return TransactionRichMeta(
                items = items,
                location = if (isTransfer) null else (location ?: inferLocation(title, categoryId, type)),
                plainNote = plainNote
            )
        }

        if (isTransfer) {
            return TransactionRichMeta(
                items = emptyList(),
                location = null,
                plainNote = rawNote
            )
        }

        val inferredLoc = inferLocation(title, categoryId, type)
        val inferredItems = inferItems(title, amount, categoryId, type)
        return TransactionRichMeta(
            items = inferredItems,
            location = inferredLoc,
            plainNote = rawNote
        )
    }

    fun inferLocation(
        title: String,
        categoryId: String?,
        type: TransactionType
    ): TransactionLocationData {
        val lower = title.lowercase(Locale.ROOT)
        return when {
            lower.contains("kopi") || lower.contains("coffee") || lower.contains("cafe") ->
                presetLocations[0]
            lower.contains("indomaret") || lower.contains("alfamart") || lower.contains("mart") ->
                presetLocations[1]
            lower.contains("shell") || lower.contains("bensin") || lower.contains("pertamax") || lower.contains("fuel") ->
                presetLocations[2]
            lower.contains("supermarket") || lower.contains("belanja") || categoryId == "shopping" ->
                presetLocations[3]
            lower.contains("makan") || lower.contains("lunch") || lower.contains("dinner") || categoryId == "food" ->
                presetLocations[4]
            type == TransactionType.INCOME ->
                TransactionLocationData(
                    placeName = "Gedung Menara BCA • Thamrin",
                    address = "Jl. M.H. Thamrin No.1, Menteng, Jakarta Pusat",
                    latitude = -6.1950,
                    longitude = 106.8227
                )
            else -> presetLocations[5]
        }
    }

    fun inferItems(
        title: String,
        amount: Long,
        categoryId: String?,
        type: TransactionType
    ): List<PurchasedItem> {
        if (amount <= 0L || type == TransactionType.INCOME) return emptyList()
        val lower = title.lowercase(Locale.ROOT)
        return when {
            lower.contains("makan") && lower.contains("kopi") && amount >= 25_000L -> {
                val coffeePrice = (amount * 35 / 100 / 500) * 500
                val foodPrice = amount - coffeePrice
                listOf(
                    PurchasedItem("Paket Makan Siang", 1, foodPrice),
                    PurchasedItem("Es Kopi Susu", 1, coffeePrice)
                )
            }
            lower.contains("kopi") && amount >= 40_000L -> {
                val first = (amount * 55 / 100 / 500) * 500
                val second = amount - first
                listOf(
                    PurchasedItem("Es Kopi Kenangan Mantan (L)", 1, first),
                    PurchasedItem("Butter Croissant", 1, second)
                )
            }
            lower.contains("bensin") || lower.contains("pertamax") || lower.contains("shell") -> {
                listOf(
                    PurchasedItem(title.ifBlank { "Pengisian BBM" }, 1, amount)
                )
            }
            lower.contains("belanja") || lower.contains("supermarket") || lower.contains("indomaret") -> {
                if (amount >= 60_000L) {
                    val p1 = (amount * 40 / 100 / 500) * 500
                    val p2 = (amount * 35 / 100 / 500) * 500
                    val p3 = amount - p1 - p2
                    listOf(
                        PurchasedItem("Beras & Kebutuhan Pokok", 1, p1),
                        PurchasedItem("Susu Segar & Minuman", 1, p2),
                        PurchasedItem("Roti & Camilan", 1, p3)
                    )
                } else {
                    listOf(PurchasedItem(title.ifBlank { "Item Belanja" }, 1, amount))
                }
            }
            else -> listOf(
                PurchasedItem(
                    name = title.ifBlank { "Item Transaksi" },
                    qty = 1,
                    unitPrice = amount
                )
            )
        }
    }
}

/**
 * TransactionDetailBottomSheet:
 * Single unified component used for BOTH viewing Transaction Details and Confirming
 * newly detected Transactions (from Voice or Receipt Camera):
 * - Single clean Pill badge for Pengeluaran / Pemasukan (consistent across Detail & Confirmation).
 * - Zero-overlap 2-column rows for Kategori, Sumber Dana, and Waktu Transaksi.
 * - Universal Itemized Breakdown (`Rincian Barang / Pesanan`) showing purchased items/menu in straight aligned rows.
 * - Payment Location (`Lokasi Pembayaran`) with interactive Maps preview canvas, address, and Google Maps link.
 * - Full-width Note container (`Catatan`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailBottomSheet(
    transaction: Transaction,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismiss: () -> Unit,
    onDeleteClick: (Transaction) -> Unit = {},
    onEditClick: ((Transaction) -> Unit)? = null,
    isConfirmationMode: Boolean = false,
    sheetTitle: String? = null,
    onConfirmClick: ((Transaction) -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTransfer = transaction.categoryId.equals("transfer", ignoreCase = true)

    var activeType by remember(transaction.type) { mutableStateOf(transaction.type) }
    val activeCategoryId = remember(transaction.categoryId, activeType) {
        if (isTransfer) {
            "transfer"
        } else {
            val found = Category.findCategory(transaction.categoryId, activeType)
            found.id
        }
    }
    val effectiveTransaction = remember(transaction, activeType, activeCategoryId) {
        transaction.copy(
            type = activeType,
            categoryId = activeCategoryId
        )
    }

    val isIncome = effectiveTransaction.type == TransactionType.INCOME
    val incomeColor = MoneyTrackerTheme.colors.income
    val category = Category.findCategory(effectiveTransaction.categoryId, effectiveTransaction.type)
    val richMeta = remember(effectiveTransaction) {
        TransactionRichMetaCodec.resolve(effectiveTransaction)
    }

    val transferInfo = if (isTransfer) {
        TransactionWalletMeta.resolveTransfer(context, effectiveTransaction)
    } else null

    val walletInfo = if (!isTransfer) {
        TransactionWalletMeta.resolveNormal(context, effectiveTransaction)
    } else null

    val accentColor = when {
        isTransfer -> MaterialTheme.colorScheme.primary
        isIncome -> incomeColor
        else -> MaterialTheme.colorScheme.error
    }

    val resolvedTitle = sheetTitle ?: if (isConfirmationMode) {
        "Konfirmasi Transaksi"
    } else {
        stringResource(R.string.action_detail)
    }

    StandardModalBottomSheet(
        onDismissRequest = onDismiss,
        title = resolvedTitle,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        actions = {
            if (onRetryClick != null) {
                ModalTopBarButton(
                    icon = Icons.Outlined.Refresh,
                    onClick = { dismiss { onRetryClick() } },
                    contentDescription = stringResource(R.string.voice_action_retry)
                )
            }
        },
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        dismiss {
                            if (onEditClick != null) {
                                onEditClick(effectiveTransaction)
                            } else {
                                AddTransactionActivity.startEdit(context, effectiveTransaction)
                            }
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.90f),
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
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConfirmationMode) stringResource(R.string.voice_action_edit_continue) else "Edit",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isConfirmationMode && onConfirmClick != null) {
                    Button(
                        onClick = { dismiss { onConfirmClick(effectiveTransaction) } },
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
                } else {
                    Button(
                        onClick = { dismiss { onDeleteClick(effectiveTransaction) } },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_delete),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            }
        }
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Single Clean Pill Badge (Pengeluaran / Pemasukan / Transfer) without outer background
            if (isTransfer) {
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
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Transfer Antar Rekening",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = accentColor
                        )
                    }
                }
            } else {
                TransactionTypePillBar(
                    selectedType = effectiveTransaction.type,
                    interactive = isConfirmationMode,
                    onSelectType = { newType -> activeType = newType }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Large Formatted Amount
            val prefix = when {
                isTransfer -> "⇄ "
                isIncome -> "+ "
                else -> "- "
            }
            Text(
                text = prefix + CurrencyFormatter.formatRupiah(effectiveTransaction.amount),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = accentColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Transaction Title (Multi-line safe, centered)
            Text(
                text = effectiveTransaction.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Main Breakdown Card Container (Kategori, Sumber Dana, Waktu Transaksi)
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
                    // Row 1: Kategori (Zero-overlap weighted row)
                    val catColor = getCategoryColor(category.id)
                    DetailRowWithRightBadge(
                        leadIcon = Icons.Outlined.Category,
                        label = stringResource(R.string.detail_label_category),
                        rightBadge = {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(catColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(category.iconKey),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        },
                        value = stringResource(category.nameRes)
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                        thickness = 0.8.dp
                    )

                    if (isTransfer && transferInfo != null) {
                        DetailWalletStackedRightRow(
                            leadIcon = Icons.Outlined.AccountBalanceWallet,
                            label = "Dari Rekening",
                            topWalletInfo = transferInfo.fromTopWalletInfo,
                            providerKey = transferInfo.fromProviderKey,
                            bankName = transferInfo.fromBankWithCustom
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 0.8.dp
                        )

                        DetailWalletStackedRightRow(
                            leadIcon = Icons.Outlined.AccountBalanceWallet,
                            label = "Ke Rekening",
                            topWalletInfo = transferInfo.toTopWalletInfo,
                            providerKey = transferInfo.toProviderKey,
                            bankName = transferInfo.toBankWithCustom
                        )
                    } else if (walletInfo != null) {
                        DetailWalletStackedRightRow(
                            leadIcon = Icons.Outlined.AccountBalanceWallet,
                            label = if (isIncome) "Rekening Tujuan" else stringResource(R.string.detail_label_payment_source),
                            topWalletInfo = walletInfo.topWalletInfoLabel,
                            providerKey = walletInfo.providerKey,
                            bankName = walletInfo.bankWithCustomLabel
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                        thickness = 0.8.dp
                    )

                    // Row 3: Waktu Transaksi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailLeadLabel(
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
                                text = DateFormatter.formatTime(effectiveTransaction.dateMillis),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = DateFormatter.formatDateOnly(effectiveTransaction.dateMillis),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Row 4: Universal Purchased Items / Ordered Menu Breakdown (Straight Aligned Rows)
                    if (richMeta.items.isNotEmpty() && !isTransfer) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 0.8.dp
                        )

                        val totalQty = richMeta.items.sumOf { it.qty.coerceAtLeast(1) }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DetailLeadLabel(
                                    leadIcon = Icons.Outlined.ShoppingBag,
                                    label = "Rincian Barang / Pesanan"
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$totalQty Item",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.42f),
                                border = BorderStroke(
                                    width = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    richMeta.items.forEachIndexed { idx, item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
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
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        }

                                        if (idx < richMeta.items.lastIndex) {
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

                    // Row 5: Lokasi Pembayaran dengan Data Maps
                    if (richMeta.location != null && !isTransfer) {
                        val loc = richMeta.location
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 0.8.dp
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DetailLeadLabel(
                                    leadIcon = Icons.Outlined.LocationOn,
                                    label = "Lokasi Pembayaran"
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .clickable {
                                            val uri = Uri.parse(
                                                "geo:${loc.latitude},${loc.longitude}?q=${Uri.encode(loc.placeName)}"
                                            )
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Buka Maps",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            TransactionLocationMapCard(
                                location = loc,
                                onClick = {
                                    val uri = Uri.parse(
                                        "geo:${loc.latitude},${loc.longitude}?q=${Uri.encode(loc.placeName)}"
                                    )
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    }
                                }
                            )
                        }
                    }

                    // Row 6: Catatan Tambahan (Full-Width Vertical Container)
                    if (richMeta.plainNote.isNotBlank()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                            thickness = 0.8.dp
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DetailLeadLabel(
                                leadIcon = Icons.Outlined.Description,
                                label = stringResource(R.string.detail_label_note)
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
                                border = BorderStroke(
                                    width = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = richMeta.plainNote,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
 * Single Clean Pill Badge (1 pill only, without outer container background)
 * used consistently in both Detail and Confirmation modals.
 */
@Composable
fun TransactionTypePillBar(
    selectedType: TransactionType,
    interactive: Boolean = false,
    onSelectType: ((TransactionType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isIncome = selectedType == TransactionType.INCOME
    val activeColor = if (isIncome) MoneyTrackerTheme.colors.income else MaterialTheme.colorScheme.error
    val icon = if (isIncome) {
        Icons.AutoMirrored.Filled.TrendingUp
    } else {
        Icons.AutoMirrored.Filled.TrendingDown
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(activeColor.copy(alpha = 0.14f))
            .border(1.dp, activeColor.copy(alpha = 0.34f), CircleShape)
            .let { base ->
                if (interactive && onSelectType != null) {
                    base.clickable {
                        val nextType = if (isIncome) TransactionType.EXPENSE else TransactionType.INCOME
                        onSelectType(nextType)
                    }
                } else {
                    base
                }
            }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = activeColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = stringResource(selectedType.labelRes),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = activeColor
            )
        }
    }
}

/**
 * Reusable Payment Location & Maps Card (Visual Mini-Map Canvas + Place Name + Street Address + Coordinates).
 * Shared between `TransactionDetailBottomSheet` and full-screen `AddTransactionScreen`.
 */
@Composable
fun TransactionLocationMapCard(
    location: TransactionLocationData,
    onClick: (() -> Unit)? = null,
    mapHeight: Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    val isDark = MoneyTrackerTheme.colors.isDark
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 0.8.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .let { base ->
                if (onClick != null) base.clickable { onClick() } else base
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Visual Vector Mini-Map Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val mapBg = if (isDark) Color(0xFF161F30) else Color(0xFFEAEFF7)
                    val blockColor = if (isDark) Color(0xFF1E293B) else Color(0xFFDDE5F0)
                    val parkColor = if (isDark) Color(0xFF15322B) else Color(0xFFD1FAE5)
                    val waterColor = if (isDark) Color(0xFF132A4A) else Color(0xFFBAE6FD)
                    val minorRoadColor = if (isDark) Color(0xFF2B3950) else Color(0xFFFFFFFF)
                    val majorRoadColor = if (isDark) Color(0xFF3B4F6B) else Color(0xFFFDE68A)

                    drawRect(color = mapBg)

                    // Deterministic layout shift based on lat/lng so each place has a unique map look
                    val seed = ((abs(location.latitude * 1000) + abs(location.longitude * 1000)).toInt() % 20) / 20f

                    // Park zone
                    drawRoundRect(
                        color = parkColor,
                        topLeft = Offset(w * (0.06f + seed * 0.05f), h * 0.12f),
                        size = Size(w * 0.28f, h * 0.46f),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )

                    // Water / canal feature
                    val riverPath = Path().apply {
                        moveTo(w * 0.72f, 0f)
                        cubicTo(
                            w * 0.66f, h * 0.35f,
                            w * 0.84f, h * 0.65f,
                            w * 0.76f, h
                        )
                    }
                    drawPath(
                        path = riverPath,
                        color = waterColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // City blocks
                    drawRoundRect(
                        color = blockColor,
                        topLeft = Offset(w * 0.40f, h * 0.10f),
                        size = Size(w * 0.24f, h * 0.30f),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    drawRoundRect(
                        color = blockColor,
                        topLeft = Offset(w * 0.12f, h * 0.66f),
                        size = Size(w * 0.36f, h * 0.24f),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Minor street grid
                    val minorW = 3.5.dp.toPx()
                    drawLine(minorRoadColor, Offset(0f, h * 0.32f), Offset(w, h * 0.28f), minorW)
                    drawLine(minorRoadColor, Offset(0f, h * 0.74f), Offset(w, h * 0.78f), minorW)
                    drawLine(minorRoadColor, Offset(w * 0.26f, 0f), Offset(w * 0.22f, h), minorW)
                    drawLine(minorRoadColor, Offset(w * 0.58f, 0f), Offset(w * 0.62f, h), minorW)

                    // Major arterial road passing near center
                    val majorW = 6.dp.toPx()
                    drawLine(majorRoadColor, Offset(0f, h * 0.54f), Offset(w, h * 0.46f), majorW)
                    drawLine(majorRoadColor, Offset(w * 0.44f, 0f), Offset(w * 0.50f, h), majorW)

                    // Center GPS accuracy halo
                    val center = Offset(w * 0.5f, h * 0.5f)
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.20f),
                        radius = 22.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = errorColor.copy(alpha = 0.25f),
                        radius = 11.dp.toPx(),
                        center = center
                    )
                }

                // Center Map Pin Icon
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = errorColor,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                        .padding(bottom = 6.dp)
                )

                // Coordinates Badge in bottom-right of map
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.58f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = location.formattedCoordinates,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // 2. Place Name & Street Address (Multi-line safe, zero overlap)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = location.placeName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (location.address.isNotBlank()) {
                    Text(
                        text = location.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Convenience wrapper around `TransactionDetailBottomSheet` for confirming a newly detected
 * transaction (Voice or Receipt Camera) using the exact same layout, Pillbar, Itemized Breakdown, and Maps Card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionConfirmationBottomSheet(
    title: String,
    amount: Long,
    type: TransactionType,
    categoryId: String,
    note: String = "",
    onDismiss: () -> Unit,
    onRetryClick: (() -> Unit)? = null,
    onEditDetails: (Transaction) -> Unit,
    onConfirmSave: (Transaction) -> Unit
) {
    val context = LocalContext.current
    WalletStateHolder.ensureInitialized(context)
    val primaryWallet = WalletStateHolder.wallets.firstOrNull()
    val category = Category.findCategory(categoryId, type)
    val localizedCatName = stringResource(category.nameRes)

    val previewTransaction = remember(title, amount, type, categoryId, note, primaryWallet, localizedCatName) {
        val encodedCategoryName = if (primaryWallet != null) {
            TransactionWalletMeta.encodeNormal(
                wallet = primaryWallet,
                walletDisplayName = primaryWallet.bankWithCustomBracket(context),
                localizedCategoryName = localizedCatName
            )
        } else {
            localizedCatName
        }
        Transaction(
            id = 0L,
            title = title,
            amount = amount,
            type = type,
            categoryId = categoryId,
            categoryName = encodedCategoryName,
            dateMillis = System.currentTimeMillis(),
            note = note
        )
    }

    TransactionDetailBottomSheet(
        transaction = previewTransaction,
        onDismiss = onDismiss,
        isConfirmationMode = true,
        onRetryClick = onRetryClick,
        onEditClick = { updatedTx -> onEditDetails(updatedTx) },
        onConfirmClick = { updatedTx -> onConfirmSave(updatedTx) }
    )
}

@Composable
private fun DetailLeadLabel(
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
private fun DetailRowWithRightBadge(
    leadIcon: ImageVector,
    label: String,
    rightBadge: @Composable () -> Unit,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DetailLeadLabel(
            leadIcon = leadIcon,
            label = label
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            rightBadge()
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailWalletStackedRightRow(
    leadIcon: ImageVector,
    label: String,
    topWalletInfo: String,
    providerKey: String,
    bankName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DetailLeadLabel(
            leadIcon = leadIcon,
            label = label
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = topWalletInfo,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
            ) {
                FinLogoCircleBadge(
                    idOrName = providerKey,
                    size = 19.dp,
                    borderWidth = 0.8.dp,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Text(
                    text = bankName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
