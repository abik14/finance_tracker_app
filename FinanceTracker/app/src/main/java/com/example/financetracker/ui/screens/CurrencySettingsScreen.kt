package com.example.financetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.data.model.CurrencyPosition
import com.example.financetracker.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    
    var symbol by remember(currencyConfig) { mutableStateOf(currencyConfig.symbol) }
    var position by remember(currencyConfig) { mutableStateOf(currencyConfig.position) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currency Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.updateCurrencyConfig(symbol, position)
                        onBack()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Currency Symbol", fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    placeholder = { Text("e.g. €") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Symbol Position", fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = position == CurrencyPosition.LEFT,
                        onClick = { position = CurrencyPosition.LEFT },
                        label = { Text("Left ($symbol 100)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = position == CurrencyPosition.RIGHT,
                        onClick = { position = CurrencyPosition.RIGHT },
                        label = { Text("Right (100 $symbol)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}