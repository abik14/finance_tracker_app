package com.example.financetracker.data.repository

import com.example.financetracker.data.database.*
import com.example.financetracker.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.*

class FinanceRepository(private val database: FinanceDatabase) {

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()

    // Transaction operations
    suspend fun addTransaction(transaction: Transaction) = transactionDao.insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.delete(transaction)
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    // Summary operations
    fun getTotalIncome(startDate: Date, endDate: Date): Flow<Double?> =
        transactionDao.getTotalIncome(startDate, endDate)

    fun getTotalExpenses(startDate: Date, endDate: Date): Flow<Double?> =
        transactionDao.getTotalExpenses(startDate, endDate)

    // Category operations
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)
    fun getCustomCategories(): Flow<List<Category>> = categoryDao.getCustomCategories()

    suspend fun addCategory(category: Category): Boolean {
        // Check if category name already exists
        val count = categoryDao.getCategoryCountByName(category.name, category.type)
        return if (count == 0) {
            categoryDao.insert(category)
            true
        } else {
            false  // Category already exists
        }
    }

    suspend fun deleteCategory(category: Category): Boolean {
        // Only allow deleting custom categories
        if (category.isDefault) return false

        // Check if there are transactions using this category
        // If yes, we should handle them (e.g., reassign or prevent deletion)
        // For now, we'll just delete it
        categoryDao.delete(category)
        return true
    }

    // Budget operations
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(month, year)
    suspend fun setBudget(budget: Budget) = budgetDao.insert(budget)
}