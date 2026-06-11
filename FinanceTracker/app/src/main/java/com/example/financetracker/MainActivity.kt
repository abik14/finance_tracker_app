package com.example.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.data.database.FinanceDatabase
import com.example.financetracker.ui.screens.*
import com.example.financetracker.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy {
        FinanceDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinanceTrackerApp(
                        database = database
                    )
                }
            }
        }
    }
}

@Composable
fun FinanceTrackerApp(database: FinanceDatabase) {
    var currentScreen by remember { mutableStateOf("dashboard") }

    val viewModel: FinanceViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FinanceViewModel(database) as T
            }
        }
    )

    when (currentScreen) {
        "dashboard" -> DashboardScreen(
            viewModel = viewModel,
            onAddTransaction = { currentScreen = "add" },
            onNavigateToSettings = { currentScreen = "settings" },
            onViewAllExpenses = { currentScreen = "expenses" },
            onExtractFromFile = { currentScreen = "extract" },
            onNavigateToStatistics = { currentScreen = "statistics" }
        )
        "add" -> AddTransactionScreen(
            viewModel = viewModel,
            onTransactionAdded = { currentScreen = "dashboard" }
        )
        "settings" -> SettingsScreen(
            onBack = { currentScreen = "dashboard" },
            onNavigateToCurrency = { currentScreen = "currency_settings" },
            onNavigateToCategories = { currentScreen = "categories" },
            onNavigateToConnection = {currentScreen = "connection"}

        )
        "currency_settings" -> CurrencySettingsScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "settings" }
        )
        "categories" -> CategoriesScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "settings" }
        )
        "expenses" -> ExpensesScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "dashboard" }
        )
        "extract" -> ExtractFromFileScreen(
            onBack = { currentScreen = "dashboard" }
        )
        "statistics" -> StatisticsScreen(
            viewModel = viewModel,
            onBack = { currentScreen = "dashboard" }
        )
        "connection" -> ConnectionScreen(
            onBack = { currentScreen = "settings" },
        )
    }
}