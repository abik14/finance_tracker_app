package com.example.financetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.financetracker.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Category::class, Budget::class],
    version = 1,
    exportSchema = false
)
@androidx.room.TypeConverters(Converters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultCategories(database.categoryDao())
                }
            }
        }

        suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                Category(name = "Food & Dining", icon = "🍔", color = "#FF5722", type = TransactionType.EXPENSE, isDefault = true, isEditable = false),
                Category(name = "Transport", icon = "🚗", color = "#2196F3", type = TransactionType.EXPENSE, isDefault = true, isEditable = false),
                Category(name = "Shopping", icon = "🛍️", color = "#9C27B0", type = TransactionType.EXPENSE, isDefault = true, isEditable = false),
                Category(name = "Entertainment", icon = "🎬", color = "#FF9800", type = TransactionType.EXPENSE, isDefault = true, isEditable = false),
                Category(name = "Bills & Utilities", icon = "💡", color = "#F44336", type = TransactionType.EXPENSE, isDefault = true, isEditable = false),
                Category(name = "Healthcare", icon = "🏥", color = "#4CAF50", type = TransactionType.EXPENSE, isDefault = true, isEditable = false),
                Category(name = "Salary", icon = "💰", color = "#4CAF50", type = TransactionType.INCOME, isDefault = true, isEditable = false),
                Category(name = "Freelance", icon = "💻", color = "#2196F3", type = TransactionType.INCOME, isDefault = true, isEditable = false),
                Category(name = "Gift", icon = "🎁", color = "#FF9800", type = TransactionType.INCOME, isDefault = true, isEditable = false),
                Category(name = "Other", icon = "📦", color = "#9E9E9E", type = TransactionType.EXPENSE, isDefault = true, isEditable = false)
            )
            categoryDao.insertAll(defaultCategories)
        }
    }
}