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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import com.aol.moneytracker.presentation.components.FinCategory
import com.aol.moneytracker.presentation.components.FinInstitution
import com.aol.moneytracker.presentation.components.FinInstitutionSelectorSheet
import com.aol.moneytracker.presentation.components.FinInstitutionSelectorTrigger
import com.aol.moneytracker.presentation.components.FinLogoCircleBadge
import com.aol.moneytracker.presentation.components.FinLogoMapping
import com.aol.moneytracker.presentation.components.StandardModalBottomSheet
import com.aol.moneytracker.presentation.components.UniversalSubScreenScaffold
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter

@Composable
fun AddWalletScreen(
    initialWallet: WalletItem? = null,
    canDelete: Boolean = false,
    onBackClick: () -> Unit,
    onSaveWallet: (WalletItem) -> Unit,
    onDeleteWallet: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val appColors = MoneyTrackerTheme.colors
    val isEditMode = initialWallet != null

    val presetCategories = remember {
        listOf(
            "Rekening Utama",
            "Tabungan",
            "Kebutuhan Harian",
            "Dana Darurat",
            "Gaji & Bisnis"
        )
    }

    val initialInstitution = remember(initialWallet) {
        if (initialWallet != null) {
            FinLogoMapping.resolveInstitution(initialWallet.providerKey)
        } else {
            FinLogoMapping.banks.first()
        }
    }

    var selectedInstitution by remember(initialWallet) { mutableStateOf(initialInstitution) }
    var activeCategoryTab by remember(initialWallet) { mutableStateOf(initialInstitution.category) }
    var isInstitutionSelectorOpen by remember { mutableStateOf(false) }
    var isAccountCategorySheetOpen by remember { mutableStateOf(false) }

    // Custom Wallet Name (Optional: when left blank, defaults to the official Bank/E-Wallet name)
    var walletName by remember(initialWallet) {
        mutableStateOf(initialWallet?.customLabelOrNull(context) ?: "")
    }

    val initialCategoryString = remember(initialWallet) {
        if (initialWallet != null) {
            initialWallet.customType ?: context.getString(initialWallet.typeRes)
        } else {
            presetCategories.first()
        }
    }

    var isCustomCategory by remember(initialWallet) {
        mutableStateOf(initialCategoryString !in presetCategories)
    }
    var selectedPresetCategory by remember(initialWallet) {
        mutableStateOf(
            if (initialCategoryString in presetCategories) initialCategoryString else presetCategories.first()
        )
    }
    var customCategoryText by remember(initialWallet) {
        mutableStateOf(
            if (initialCategoryString !in presetCategories) initialCategoryString else ""
        )
    }

    val resolvedCategory = if (isCustomCategory) {
        customCategoryText.trim().ifBlank { "Kustom" }
    } else {
        selectedPresetCategory
    }

    val officialBankName = resolveOfficialBankName(selectedInstitution.id, selectedInstitution.displayName)
    val customWalletName = walletName.trim().takeIf {
        it.isNotBlank() &&
            !it.equals(officialBankName, ignoreCase = true) &&
            !it.equals(selectedInstitution.displayName, ignoreCase = true)
    }

    var rawBalance by remember(initialWallet) {
        mutableStateOf(initialWallet?.balance?.toString() ?: "")
    }
    var showCategoryOnCard by remember(initialWallet) {
        mutableStateOf(initialWallet?.showCategoryOnCard ?: true)
    }
    var isBalanceHidden by remember(initialWallet) {
        mutableStateOf(initialWallet?.isBalanceHidden ?: false)
    }

    val parsedBalance = CurrencyFormatter.parseAmount(rawBalance)
    val listState = rememberLazyListState()

    UniversalSubScreenScaffold(
        title = if (isEditMode) {
            "Konfigurasi Wallet"
        } else {
            stringResource(R.string.wallet_new_account_title)
        },
        onBackClick = onBackClick,
        isScrolledOverride = listState.canScrollBackward,
        bottomBar = { navigationBarHeight ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .padding(bottom = navigationBarHeight),
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
                        val updatedGradient = FinLogoMapping.resolveCardGradient(
                            context,
                            selectedInstitution.id,
                            appColors.isDark
                        )
                        if (initialWallet != null) {
                            onSaveWallet(
                                initialWallet.copy(
                                    providerKey = selectedInstitution.id,
                                    typeRes = selectedInstitution.category.labelRes,
                                    gradientColors = updatedGradient,
                                    icon = selectedInstitution.fallbackIcon,
                                    balance = parsedBalance,
                                    customName = customWalletName,
                                    customType = resolvedCategory,
                                    showCategoryOnCard = showCategoryOnCard,
                                    isBalanceHidden = isBalanceHidden
                                )
                            )
                        } else {
                            onSaveWallet(
                                WalletItem(
                                    id = "${selectedInstitution.id}_${System.currentTimeMillis()}",
                                    providerKey = selectedInstitution.id,
                                    nameRes = R.string.wallet_bca_name,
                                    accountNumberRes = R.string.wallet_bca_number,
                                    balance = parsedBalance,
                                    typeRes = selectedInstitution.category.labelRes,
                                    gradientColors = updatedGradient,
                                    icon = selectedInstitution.fallbackIcon,
                                    customName = customWalletName,
                                    customType = resolvedCategory,
                                    showCategoryOnCard = showCategoryOnCard,
                                    isBalanceHidden = isBalanceHidden
                                )
                            )
                        }
                    },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Live Wallet Card Preview at Top
            item {
                LiveWalletPreviewCard(
                    institution = selectedInstitution,
                    customWalletName = customWalletName,
                    bankName = officialBankName,
                    accountType = resolvedCategory,
                    balance = parsedBalance,
                    showCategoryOnCard = showCategoryOnCard,
                    isBalanceHidden = isBalanceHidden,
                    onToggleBalanceVisibility = { isBalanceHidden = !isBalanceHidden }
                )
            }

            // 2. Section: Institusi & Identitas Wallet
            item {
                AddWalletSectionTitle(stringResource(R.string.wallet_new_account_section_institution))
                Spacer(modifier = Modifier.height(10.dp))
                AddWalletGroupCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        AddWalletCardRowHeader(
                            icon = Icons.Outlined.AccountBalance,
                            title = stringResource(R.string.wallet_config_provider_label)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Pills: Bank | E-Wallet | Tunai
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val tabs = listOf(
                                Triple(
                                    FinCategory.BANK,
                                    stringResource(R.string.wallet_category_tab_bank, FinLogoMapping.banks.size),
                                    Icons.Outlined.AccountBalance
                                ),
                                Triple(
                                    FinCategory.EWALLET,
                                    stringResource(R.string.wallet_category_tab_ewallet, FinLogoMapping.eWallets.size),
                                    Icons.Outlined.Payments
                                ),
                                Triple(
                                    FinCategory.CASH,
                                    stringResource(R.string.wallet_category_tab_cash),
                                    Icons.Outlined.AccountBalanceWallet
                                )
                            )
                            tabs.forEach { (category, label, tabIcon) ->
                                CategorySelectorChip(
                                    label = label,
                                    icon = tabIcon,
                                    isSelected = activeCategoryTab == category,
                                    onClick = {
                                        activeCategoryTab = category
                                        if (category == FinCategory.CASH) {
                                            selectedInstitution = FinLogoMapping.cashInstitution
                                        } else {
                                            val firstInCategory = FinLogoMapping.byCategory(category).firstOrNull()
                                            if (firstInCategory != null && selectedInstitution.category != category) {
                                                selectedInstitution = firstInCategory
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Selector Trigger ONLY for Bank and E-Wallet (Tunai has NO selector because it is fixed)
                        if (activeCategoryTab != FinCategory.CASH) {
                            Spacer(modifier = Modifier.height(12.dp))
                            FinInstitutionSelectorTrigger(
                                selectedInstitution = selectedInstitution,
                                onClick = { isInstitutionSelectorOpen = true }
                            )
                        }
                    }

                    AddWalletGroupDivider()

                    // Nama Wallet / Akun (Opsional)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        AddWalletCardRowHeader(
                            icon = Icons.Outlined.Edit,
                            title = "Nama Custom Wallet (Opsional)"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = walletName,
                            onValueChange = { walletName = it },
                            placeholder = {
                                Text(
                                    text = "Kosongkan untuk pakai nama $officialBankName",
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

                    AddWalletGroupDivider()

                    // Kategori Akun Row (Selector Trigger + Optional Custom Input — NO PILLS)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        AddWalletCardRowHeader(
                            icon = Icons.Outlined.Category,
                            title = stringResource(R.string.wallet_config_type_label)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            onClick = { isAccountCategorySheetOpen = true },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.48f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Text(
                                        text = if (isCustomCategory) "Kategori Kustom" else "Kategori Terpilih",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = resolvedCategory,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
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

                        if (isCustomCategory) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = customCategoryText,
                                onValueChange = { customCategoryText = it },
                                placeholder = {
                                    Text(
                                        text = "Ketik kategori kustom (mis. Dana Liburan, Kos)",
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
            }

            // 3. Section: Saldo & Identitas Rekening
            item {
                AddWalletSectionTitle(stringResource(R.string.wallet_new_account_section_details))
                Spacer(modifier = Modifier.height(10.dp))
                AddWalletGroupCard {
                    // Row 1: Saldo Awal / Saat Ini (Rp) + Quick Nominal Chips
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        AddWalletCardRowHeader(
                            icon = Icons.Outlined.AccountBalanceWallet,
                            title = if (isEditMode) {
                                stringResource(R.string.wallet_config_balance_label)
                            } else {
                                stringResource(R.string.wallet_new_account_initial_balance)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = rawBalance,
                            onValueChange = { rawBalance = it.filter { ch -> ch.isDigit() } },
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Balance Chips
                        val quickAmounts = listOf(
                            "+100rb" to 100_000L,
                            "+500rb" to 500_000L,
                            "+1jt" to 1_000_000L,
                            "+5jt" to 5_000_000L
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickAmounts.forEach { (label, delta) ->
                                Surface(
                                    onClick = {
                                        val next = (parsedBalance + delta).coerceAtMost(999_999_999_999L)
                                        rawBalance = next.toString()
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
                }
            }

            // 4. Section: Pengaturan Tampilan Wallet (Toggle for Category on Card + List)
            item {
                AddWalletSectionTitle("Pengaturan Tampilan Wallet")
                Spacer(modifier = Modifier.height(10.dp))
                AddWalletGroupCard {
                    AddWalletSwitchRow(
                        icon = Icons.Outlined.Category,
                        title = "Tampilkan Kategori Akun",
                        subtitle = "Perlihatkan kategori di kartu wallet dan daftar rekening",
                        checked = showCategoryOnCard,
                        onCheckedChange = { showCategoryOnCard = it }
                    )
                }
            }

            // 5. Optional Delete Account Row in Edit Mode
            if (isEditMode && canDelete && initialWallet != null && onDeleteWallet != null) {
                item {
                    Surface(
                        onClick = { onDeleteWallet(initialWallet.id) },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hapus Akun Ini",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Hapus rekening/dompet ini dari daftar akun Anda",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom Modal Selector for Bank or E-Wallet (built on Amaya's StandardModalBottomSheet)
    if (isInstitutionSelectorOpen && activeCategoryTab != FinCategory.CASH) {
        FinInstitutionSelectorSheet(
            category = activeCategoryTab,
            selectedInstitutionId = selectedInstitution.id,
            onSelect = { inst ->
                selectedInstitution = inst
            },
            onDismiss = { isInstitutionSelectorOpen = false }
        )
    }

    // Bottom Modal Selector for Kategori Akun
    if (isAccountCategorySheetOpen) {
        StandardModalBottomSheet(
            onDismissRequest = { isAccountCategorySheetOpen = false },
            title = stringResource(R.string.wallet_config_type_label),
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
                    val allCategoryChoices = presetCategories + "Kustom (Ketik Sendiri)"
                    allCategoryChoices.forEachIndexed { index, option ->
                        val isCustomOption = index == allCategoryChoices.lastIndex
                        val isSelected = if (isCustomOption) {
                            isCustomCategory
                        } else {
                            !isCustomCategory && selectedPresetCategory == option
                        }
                        Surface(
                            onClick = {
                                if (isCustomOption) {
                                    isCustomCategory = true
                                } else {
                                    isCustomCategory = false
                                    selectedPresetCategory = option
                                }
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
                                    .padding(horizontal = 18.dp, vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
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
                        if (index < allCategoryChoices.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                thickness = 0.6.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddWalletCardRowHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
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
private fun LiveWalletPreviewCard(
    institution: FinInstitution,
    customWalletName: String?,
    bankName: String,
    accountType: String,
    balance: Long,
    showCategoryOnCard: Boolean,
    isBalanceHidden: Boolean,
    onToggleBalanceVisibility: () -> Unit
) {
    val context = LocalContext.current
    val appColors = MoneyTrackerTheme.colors
    val onWalletCard = appColors.onWalletCard
    val cardGradient = remember(institution.id, appColors.isDark) {
        FinLogoMapping.resolveCardGradient(context, institution.id, appColors.isDark)
    }
    val displayedBalance = if (isBalanceHidden) "Rp ••••••••" else CurrencyFormatter.formatRupiah(balance)
    val mainTitle = customWalletName ?: bankName

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, onWalletCard.copy(alpha = 0.14f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(158.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = cardGradient))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
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
                        if (customWalletName != null) {
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
                        idOrName = institution.id,
                        size = 42.dp,
                        borderWidth = 1.2.dp,
                        borderColor = onWalletCard.copy(alpha = 0.35f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (showCategoryOnCard && accountType.isNotBlank()) {
                            Text(
                                text = accountType,
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
                            text = displayedBalance,
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
                        color = onWalletCard.copy(alpha = 0.16f),
                        border = BorderStroke(0.8.dp, onWalletCard.copy(alpha = 0.25f)),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isBalanceHidden) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (isBalanceHidden) {
                                    "Tampilkan saldo"
                                } else {
                                    "Sembunyikan saldo"
                                },
                                tint = onWalletCard,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySelectorChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
        ),
        modifier = modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddWalletSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
private fun AddWalletGroupCard(
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
private fun AddWalletGroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.8.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    )
}

@Composable
private fun AddWalletSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            modifier = Modifier.scale(0.85f)
        )
    }
}
