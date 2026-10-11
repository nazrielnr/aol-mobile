package com.aol.moneytracker.presentation.screens.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.components.CategorySelector
import com.aol.moneytracker.presentation.components.SegmentedTypeSwitch
import com.aol.moneytracker.presentation.components.StandardModalBottomSheet
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter

/**
 * Compact, Clean & Modern Add Transaction Modal powered by global StandardModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    initialTitle: String = "",
    initialAmount: Long = 0L,
    initialType: TransactionType = TransactionType.EXPENSE,
    initialCategoryId: String? = null
) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(initialType) }
    var rawAmount by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "") }
    var title by remember { mutableStateOf(initialTitle) }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember {
        mutableStateOf(
            initialCategoryId ?: if (type == TransactionType.EXPENSE) Category.EXPENSE_CATEGORIES.first().id
            else Category.INCOME_CATEGORIES.first().id
        )
    }

    val parsedAmount = CurrencyFormatter.parseAmount(rawAmount)
    val isValid = parsedAmount > 0 && title.isNotBlank()

    StandardModalBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.add_tx_title),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
                        .height(50.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Button(
                    onClick = {
                        if (isValid) {
                            val selectedCat = Category.findCategory(selectedCategoryId, type)
                            val transaction = Transaction(
                                title = title.trim(),
                                amount = parsedAmount,
                                type = type,
                                categoryId = selectedCategoryId,
                                categoryName = context.getString(selectedCat.nameRes),
                                dateMillis = System.currentTimeMillis(),
                                note = note.trim()
                            )
                            dismiss { onSave(transaction) }
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .weight(1.35f)
                        .height(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.add_tx_save_button),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
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
            // Segmented Type Switch (Pengeluaran vs Pemasukan)
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

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Prominent Amount Card with Quick Increments (+10k, +50k, etc.)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.add_tx_amount_label),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = if (rawAmount.isEmpty()) "" else CurrencyFormatter.formatRupiah(parsedAmount),
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            rawAmount = digits
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.add_tx_amount_placeholder),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            color = if (type == TransactionType.EXPENSE) MaterialTheme.colorScheme.onSurface else MoneyTrackerTheme.colors.income
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Nominal Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                    ) {
                        QuickAmountChip(label = stringResource(R.string.chip_plus_10k)) {
                            val cur = rawAmount.toLongOrNull() ?: 0L
                            rawAmount = (cur + 10_000L).toString()
                        }
                        QuickAmountChip(label = stringResource(R.string.chip_plus_20k)) {
                            val cur = rawAmount.toLongOrNull() ?: 0L
                            rawAmount = (cur + 20_000L).toString()
                        }
                        QuickAmountChip(label = stringResource(R.string.chip_plus_50k)) {
                            val cur = rawAmount.toLongOrNull() ?: 0L
                            rawAmount = (cur + 50_000L).toString()
                        }
                        QuickAmountChip(label = stringResource(R.string.chip_plus_100k)) {
                            val cur = rawAmount.toLongOrNull() ?: 0L
                            rawAmount = (cur + 100_000L).toString()
                        }
                        if (rawAmount.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .height(30.dp)
                                    .clickable { rawAmount = "" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                        contentDescription = stringResource(R.string.action_reset),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Judul / Keterangan Transaksi
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.add_tx_field_title_label)) },
                placeholder = {
                    Text(
                        stringResource(
                            if (type == TransactionType.EXPENSE) R.string.add_tx_field_title_placeholder_expense
                            else R.string.add_tx_field_title_placeholder_income
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Kategori Transaksi
            Text(
                text = stringResource(R.string.add_tx_category_label),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            CategorySelector(
                type = type,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { category ->
                    selectedCategoryId = category.id
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Catatan Opsional
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.add_tx_note_label)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun QuickAmountChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .height(30.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
