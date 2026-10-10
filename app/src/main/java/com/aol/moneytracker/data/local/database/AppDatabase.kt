package com.aol.moneytracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aol.moneytracker.data.local.dao.TransactionDao
import com.aol.moneytracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance: AppDatabase? = null
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "money_tracker.db"
                )
                    .addCallback(SeedDataCallback(scope) { instance ?: INSTANCE })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedDataCallback(
            private val scope: CoroutineScope,
            private val databaseProvider: () -> AppDatabase?
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    databaseProvider()?.transactionDao()?.let { dao ->
                        populateInitialData(dao)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                scope.launch(Dispatchers.IO) {
                    databaseProvider()?.transactionDao()?.let { dao ->
                        if (dao.getCount() < 15) {
                            populateInitialData(dao)
                        }
                    }
                }
            }

            private suspend fun populateInitialData(dao: TransactionDao) {
                val currentTime = System.currentTimeMillis()
                val oneHour = 3600_000L
                val oneDay = 86400_000L

                val seedList = listOf(
                    // Hari Ini
                    TransactionEntity(
                        title = "Isi Bahan Bakar Mobil",
                        amount = 250000L,
                        type = "EXPENSE",
                        categoryId = "transport",
                        categoryName = "Transportasi",
                        dateMillis = currentTime - (2 * oneHour),
                        note = "Pertamax full tank"
                    ),
                    TransactionEntity(
                        title = "Makan Siang & Kopi Artisan",
                        amount = 55000L,
                        type = "EXPENSE",
                        categoryId = "food",
                        categoryName = "Makanan & Minuman",
                        dateMillis = currentTime - (5 * oneHour),
                        note = "Makan siang bersama teman tim"
                    ),
                    TransactionEntity(
                        title = "Tagihan Internet & Listrik",
                        amount = 420000L,
                        type = "EXPENSE",
                        categoryId = "bills",
                        categoryName = "Tagihan & Utilitas",
                        dateMillis = currentTime - (10 * oneHour),
                        note = "Pembayaran rutin bulanan"
                    ),

                    // Kemarin
                    TransactionEntity(
                        title = "Belanja Mingguan Supermarket",
                        amount = 450000L,
                        type = "EXPENSE",
                        categoryId = "shopping",
                        categoryName = "Belanja",
                        dateMillis = currentTime - (1 * oneDay) - (4 * oneHour),
                        note = "Bahan makanan & kebutuhan rumah"
                    ),
                    TransactionEntity(
                        title = "Tiket Bioskop & Snack",
                        amount = 120000L,
                        type = "EXPENSE",
                        categoryId = "entertainment",
                        categoryName = "Hiburan",
                        dateMillis = currentTime - (1 * oneDay) - (6 * oneHour),
                        note = "Nonton film akhir pekan"
                    ),
                    TransactionEntity(
                        title = "Makan Malam Seafood",
                        amount = 175000L,
                        type = "EXPENSE",
                        categoryId = "food",
                        categoryName = "Makanan & Minuman",
                        dateMillis = currentTime - (1 * oneDay) - (8 * oneHour),
                        note = "Makan malam keluarga"
                    ),

                    // 2 Hari Lalu
                    TransactionEntity(
                        title = "Proyek Mobile App Freelance",
                        amount = 3000000L,
                        type = "INCOME",
                        categoryId = "freelance",
                        categoryName = "Proyek Lepas",
                        dateMillis = currentTime - (2 * oneDay),
                        note = "Pembayaran termin 1"
                    ),
                    TransactionEntity(
                        title = "Service Motor & Ganti Oli",
                        amount = 210000L,
                        type = "EXPENSE",
                        categoryId = "transport",
                        categoryName = "Transportasi",
                        dateMillis = currentTime - (2 * oneDay) - (3 * oneHour),
                        note = "Service rutin bulanan"
                    ),
                    TransactionEntity(
                        title = "Langganan Spotify & Cloud",
                        amount = 85000L,
                        type = "EXPENSE",
                        categoryId = "bills",
                        categoryName = "Tagihan & Utilitas",
                        dateMillis = currentTime - (2 * oneDay) - (7 * oneHour),
                        note = "Perpanjangan langganan otomatis"
                    ),

                    // 3 Hari Lalu
                    TransactionEntity(
                        title = "Gaji Pokok Oktober",
                        amount = 8500000L,
                        type = "INCOME",
                        categoryId = "salary",
                        categoryName = "Gaji Pokok",
                        dateMillis = currentTime - (3 * oneDay),
                        note = "Transfer kantor bulanan"
                    ),
                    TransactionEntity(
                        title = "Belanja Perlengkapan Rumah",
                        amount = 320000L,
                        type = "EXPENSE",
                        categoryId = "shopping",
                        categoryName = "Belanja",
                        dateMillis = currentTime - (3 * oneDay) - (2 * oneHour),
                        note = "Perabot dapur & sabun"
                    ),
                    TransactionEntity(
                        title = "Kopi Sore & Pastry",
                        amount = 48000L,
                        type = "EXPENSE",
                        categoryId = "food",
                        categoryName = "Makanan & Minuman",
                        dateMillis = currentTime - (3 * oneDay) - (5 * oneHour),
                        note = "Coffee break santai"
                    ),

                    // 4 Hari Lalu
                    TransactionEntity(
                        title = "Bonus Kinerja Kuartal",
                        amount = 2000000L,
                        type = "INCOME",
                        categoryId = "bonus",
                        categoryName = "Bonus & THR",
                        dateMillis = currentTime - (4 * oneDay),
                        note = "Reward pencapaian sprint"
                    ),
                    TransactionEntity(
                        title = "Check-up & Vitamin Kesehatan",
                        amount = 280000L,
                        type = "EXPENSE",
                        categoryId = "health",
                        categoryName = "Kesehatan",
                        dateMillis = currentTime - (4 * oneDay) - (4 * oneHour),
                        note = "Suplemen dan vitamin imun"
                    ),
                    TransactionEntity(
                        title = "Beli Buku Pemrograman Kotlin",
                        amount = 185000L,
                        type = "EXPENSE",
                        categoryId = "education",
                        categoryName = "Pendidikan",
                        dateMillis = currentTime - (4 * oneDay) - (7 * oneHour),
                        note = "Upgrade skill Compose"
                    ),

                    // 5 Hari Lalu
                    TransactionEntity(
                        title = "Beli Makanan & Pasir Kucing",
                        amount = 145000L,
                        type = "EXPENSE",
                        categoryId = "shopping",
                        categoryName = "Belanja",
                        dateMillis = currentTime - (5 * oneDay) - (3 * oneHour),
                        note = "Keperluan pet shop"
                    ),
                    TransactionEntity(
                        title = "Tagihan Air PDAM",
                        amount = 85000L,
                        type = "EXPENSE",
                        categoryId = "bills",
                        categoryName = "Tagihan & Utilitas",
                        dateMillis = currentTime - (5 * oneDay) - (6 * oneHour),
                        note = "Pemakaian bulan September"
                    ),
                    TransactionEntity(
                        title = "Makan Siang Bakso Malang",
                        amount = 35000L,
                        type = "EXPENSE",
                        categoryId = "food",
                        categoryName = "Makanan & Minuman",
                        dateMillis = currentTime - (5 * oneDay) - (8 * oneHour),
                        note = "Makan siang murah meriah"
                    ),

                    // 6 Hari Lalu
                    TransactionEntity(
                        title = "Dividen Investasi Reksadana",
                        amount = 650000L,
                        type = "INCOME",
                        categoryId = "investment",
                        categoryName = "Investasi",
                        dateMillis = currentTime - (6 * oneDay),
                        note = "Pembagian dividen pasar uang"
                    ),
                    TransactionEntity(
                        title = "Belanja Online Gadget Aksesoris",
                        amount = 310000L,
                        type = "EXPENSE",
                        categoryId = "shopping",
                        categoryName = "Belanja",
                        dateMillis = currentTime - (6 * oneDay) - (4 * oneHour),
                        note = "Kabel USB-C & charger"
                    ),
                    TransactionEntity(
                        title = "Laundry Pakaian Mingguan",
                        amount = 60000L,
                        type = "EXPENSE",
                        categoryId = "bills",
                        categoryName = "Tagihan & Utilitas",
                        dateMillis = currentTime - (6 * oneDay) - (7 * oneHour),
                        note = "Laundry kiloan kilat"
                    )
                )
                dao.insertAll(seedList)
            }
        }
    }
}
