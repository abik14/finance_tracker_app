package com.example.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.database.FinanceDatabase
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.CurrencyConfig
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import com.example.financetracker.data.repository.FinanceRepository
import com.example.financetracker.utils.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class Period {
    object CurrentMonth : Period()
    object Last3Months : Period()
    object Last6Months : Period()
    object LastYear : Period()
    data class Custom(val start: Date, val end: Date) : Period()

    fun getDateRange(): Pair<Date, Date> {
        val end = Date()
        val calendar = Calendar.getInstance()
        return when (this) {
            is CurrentMonth -> DateUtils.getCurrentMonthStart() to DateUtils.getCurrentMonthEnd()
            is Last3Months -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.time to end
            }
            is Last6Months -> {
                calendar.add(Calendar.MONTH, -6)
                calendar.time to end
            }
            is LastYear -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.time to end
            }
            is Custom -> start to end
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModel(
    private val database: FinanceDatabase
) : ViewModel() {

    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val repository = FinanceRepository(database)

    // Data flows
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    // Date Range for filtering
    private val _selectedPeriod = MutableStateFlow<Period>(Period.CurrentMonth)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _monthOffset = MutableStateFlow(0)
    val monthOffset: StateFlow<Int> = _monthOffset.asStateFlow()

    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
        if (period != Period.CurrentMonth) {
            _monthOffset.value = 0 // Reset offset for multi-month periods
        }
    }

    fun updateMonthOffset(delta: Int) {
        _monthOffset.value += delta
    }

    // Filtered transactions based on selected period and offset
    val filteredTransactions: Flow<List<Transaction>> = combine(_selectedPeriod, _monthOffset) { period, offset ->
        val range = getRangeWithOffset(period, offset)
        transactionDao.getTransactionsBetweenDates(range.first, range.second)
    }.flatMapLatest { it }

    // Totals for filtered transactions
    val filteredIncome: Flow<Double> = combine(_selectedPeriod, _monthOffset) { period, offset ->
        val range = getRangeWithOffset(period, offset)
        transactionDao.getTotalIncome(range.first, range.second).map { it ?: 0.0 }
    }.flatMapLatest { it }

    val filteredExpenses: Flow<Double> = combine(_selectedPeriod, _monthOffset) { period, offset ->
        val range = getRangeWithOffset(period, offset)
        transactionDao.getTotalExpenses(range.first, range.second).map { it ?: 0.0 }
    }.flatMapLatest { it }

    private fun getRangeWithOffset(period: Period, offset: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        
        // Apply offset first (only relevant for CurrentMonth, but safe to apply)
        if (period == Period.CurrentMonth) {
            calendar.add(Calendar.MONTH, offset)
            val monthStart = calendar.clone() as Calendar
            monthStart.set(Calendar.DAY_OF_MONTH, 1)
            monthStart.set(Calendar.HOUR_OF_DAY, 0)
            monthStart.set(Calendar.MINUTE, 0)
            monthStart.set(Calendar.SECOND, 0)

            val monthEnd = calendar.clone() as Calendar
            monthEnd.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            monthEnd.set(Calendar.HOUR_OF_DAY, 23)
            monthEnd.set(Calendar.MINUTE, 59)
            monthEnd.set(Calendar.SECOND, 59)
            
            return monthStart.time to monthEnd.time
        } else {
            // For 3m, 6m, 1y, offset slides the whole window
            val baseRange = period.getDateRange()
            val startCal = Calendar.getInstance().apply { time = baseRange.first; add(Calendar.MONTH, offset) }
            val endCal = Calendar.getInstance().apply { time = baseRange.second; add(Calendar.MONTH, offset) }
            return startCal.time to endCal.time
        }
    }

    fun getFormattedPeriodLabel(period: Period, offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)
        return when (period) {
            is Period.CurrentMonth -> DateUtils.formatMonth(calendar.time)
            is Period.Last3Months -> "Last 3 Months"
            is Period.Last6Months -> "Last 6 Months"
            is Period.LastYear -> "Last Year"
            else -> "Custom"
        }
    }

    fun getFormattedDateRange(period: Period, offset: Int): String {
        val range = getRangeWithOffset(period, offset)
        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return "${df.format(range.first)} - ${df.format(range.second)}"
    }

    // Category Breakdown (Expenses only)
    val categoryBreakdown: Flow<Map<Category, Double>> = combine(filteredTransactions, allCategories) { transactions, categories ->
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val categoryMap = categories.associateBy { it.id }
        expenses.groupBy { it.categoryId }
            .mapNotNull { (catId, trans) ->
                val category = categoryMap[catId] ?: return@mapNotNull null
                category to trans.sumOf { it.amount }
            }.toMap()
    }

    // Currency configuration
    private val _currencyConfig = MutableStateFlow(CurrencyConfig())
    val currencyConfig: StateFlow<CurrencyConfig> = _currencyConfig.asStateFlow()

    fun updateCurrencyConfig(symbol: String, position: com.example.financetracker.data.model.CurrencyPosition) {
        _currencyConfig.value = CurrencyConfig(symbol, position)
    }

    // State flows for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Current month transactions
    val currentMonthTransactions: Flow<List<Transaction>> = transactionDao.getTransactionsBetweenDates(
        DateUtils.getCurrentMonthStart(),
        DateUtils.getCurrentMonthEnd()
    )

    // Monthly totals
    val monthlyIncome: Flow<Double> = transactionDao.getTotalIncome(
        DateUtils.getCurrentMonthStart(),
        DateUtils.getCurrentMonthEnd()
    ).map { it ?: 0.0 }

    val monthlyExpenses: Flow<Double> = transactionDao.getTotalExpenses(
        DateUtils.getCurrentMonthStart(),
        DateUtils.getCurrentMonthEnd()
    ).map { it ?: 0.0 }

    val monthlyBalance: Flow<Double> = combine(monthlyIncome, monthlyExpenses) { income, expenses ->
        income - expenses
    }

    // Add a transaction
    fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: Int,
        note: String?,
        date: Date = Date()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val transaction = Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    note = note,
                    date = date
                )
                transactionDao.insert(transaction)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add transaction"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete a transaction
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionDao.delete(transaction)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete transaction"
            }
        }
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }

    fun addCategory(name: String, icon: String, color: String, type: TransactionType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newCategory = Category(
                    name = name,
                    icon = icon,
                    color = color,
                    type = type,
                    isDefault = false,
                    isEditable = true
                )
                val success = repository.addCategory(newCategory)
                if (!success) {
                    _errorMessage.value = "Category '$name' already exists"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add category"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete category"
            }
        }
    }

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return repository.getCategoriesByType(type)
    }
}