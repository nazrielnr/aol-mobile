package com.aol.moneytracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.screens.add.AddTransactionScreen
import com.aol.moneytracker.presentation.screens.wallet.TransactionWalletMeta
import com.aol.moneytracker.presentation.screens.wallet.WalletStateHolder
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.ProvideAppLanguage
import com.aol.moneytracker.util.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddTransactionActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WalletStateHolder.ensureInitialized(this)

        val isEditMode = intent?.getBooleanExtra(EXTRA_IS_EDIT, false) == true
        val txId = intent?.getLongExtra(EXTRA_TX_ID, 0L) ?: 0L
        val initialTitle = intent?.getStringExtra(EXTRA_TITLE).orEmpty()
        val initialAmount = intent?.getLongExtra(EXTRA_AMOUNT, 0L) ?: 0L
        val typeName = intent?.getStringExtra(EXTRA_TYPE) ?: TransactionType.EXPENSE.name
        val initialType = runCatching { TransactionType.valueOf(typeName) }.getOrDefault(TransactionType.EXPENSE)
        val initialCategoryId = intent?.getStringExtra(EXTRA_CATEGORY_ID)
        val initialCategoryName = intent?.getStringExtra(EXTRA_CATEGORY_NAME).orEmpty()
        val initialDateMillis = intent?.getLongExtra(EXTRA_DATE_MILLIS, System.currentTimeMillis())
            ?: System.currentTimeMillis()
        val initialNote = intent?.getStringExtra(EXTRA_NOTE).orEmpty()

        val oldTransaction = if (isEditMode) {
            Transaction(
                id = txId,
                title = initialTitle,
                amount = initialAmount,
                type = initialType,
                categoryId = initialCategoryId.orEmpty(),
                categoryName = initialCategoryName,
                dateMillis = initialDateMillis,
                note = initialNote
            )
        } else null

        val isTransfer = initialCategoryId.equals("transfer", ignoreCase = true)
        val resolvedWalletId = if (oldTransaction != null) {
            if (isTransfer) {
                TransactionWalletMeta.resolveTransfer(this, oldTransaction).fromWalletId
            } else {
                TransactionWalletMeta.resolveNormal(this, oldTransaction).walletId
            }
        } else null

        val resolvedToWalletId = if (oldTransaction != null && isTransfer) {
            TransactionWalletMeta.resolveTransfer(this, oldTransaction).toWalletId
        } else null

        val app = applicationContext as MoneyTrackerApp

        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            ProvideAppLanguage {
                MoneyTrackerTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AddTransactionScreen(
                            wallets = WalletStateHolder.wallets,
                            isEditMode = isEditMode,
                            editingTransactionId = txId,
                            initialTitle = initialTitle,
                            initialAmount = initialAmount,
                            initialType = initialType,
                            initialCategoryId = initialCategoryId,
                            initialWalletId = resolvedWalletId,
                            initialToWalletId = resolvedToWalletId,
                            initialDateMillis = initialDateMillis,
                            initialNote = initialNote,
                            onBackClick = { finish() },
                            onSave = { savedTransaction ->
                                if (isEditMode && oldTransaction != null) {
                                    WalletStateHolder.updateTransactionEffect(
                                        context = this@AddTransactionActivity,
                                        oldTransaction = oldTransaction,
                                        newTransaction = savedTransaction
                                    )
                                    app.applicationScope.launch(Dispatchers.IO) {
                                        app.repository.updateTransaction(savedTransaction)
                                    }
                                } else {
                                    WalletStateHolder.applyTransactionEffect(
                                        context = this@AddTransactionActivity,
                                        transaction = savedTransaction
                                    )
                                    app.applicationScope.launch(Dispatchers.IO) {
                                        app.repository.insertTransaction(savedTransaction)
                                    }
                                }
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_IS_EDIT = "extra_is_edit"
        private const val EXTRA_TX_ID = "extra_tx_id"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_AMOUNT = "extra_amount"
        private const val EXTRA_TYPE = "extra_type"
        private const val EXTRA_CATEGORY_ID = "extra_category_id"
        private const val EXTRA_CATEGORY_NAME = "extra_category_name"
        private const val EXTRA_DATE_MILLIS = "extra_date_millis"
        private const val EXTRA_NOTE = "extra_note"

        fun startCreate(
            context: Context,
            initialTitle: String = "",
            initialAmount: Long = 0L,
            initialType: TransactionType = TransactionType.EXPENSE,
            initialCategoryId: String? = null,
            initialNote: String = ""
        ) {
            val intent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra(EXTRA_IS_EDIT, false)
                putExtra(EXTRA_TITLE, initialTitle)
                putExtra(EXTRA_AMOUNT, initialAmount)
                putExtra(EXTRA_TYPE, initialType.name)
                putExtra(EXTRA_CATEGORY_ID, initialCategoryId)
                putExtra(EXTRA_DATE_MILLIS, System.currentTimeMillis())
                putExtra(EXTRA_NOTE, initialNote)
            }
            context.startActivity(intent)
        }

        fun startEdit(context: Context, transaction: Transaction) {
            val intent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra(EXTRA_IS_EDIT, true)
                putExtra(EXTRA_TX_ID, transaction.id)
                putExtra(EXTRA_TITLE, transaction.title)
                putExtra(EXTRA_AMOUNT, transaction.amount)
                putExtra(EXTRA_TYPE, transaction.type.name)
                putExtra(EXTRA_CATEGORY_ID, transaction.categoryId)
                putExtra(EXTRA_CATEGORY_NAME, transaction.categoryName)
                putExtra(EXTRA_DATE_MILLIS, transaction.dateMillis)
                putExtra(EXTRA_NOTE, transaction.note)
            }
            context.startActivity(intent)
        }
    }
}
