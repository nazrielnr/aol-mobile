package com.aol.moneytracker

import android.app.Application
import com.aol.moneytracker.data.local.database.AppDatabase
import com.aol.moneytracker.data.repository.TransactionRepositoryImpl
import com.aol.moneytracker.domain.repository.TransactionRepository
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MoneyTrackerApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        LanguageManager.init(this)
        ThemeManager.init(this)
    }

    val database by lazy {
        AppDatabase.getInstance(this, applicationScope)
    }

    val repository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao())
    }
}
