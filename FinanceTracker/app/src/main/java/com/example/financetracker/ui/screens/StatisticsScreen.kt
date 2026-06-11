package com.example.financetracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.R
import com.example.financetracker.ui.viewmodel.FinanceViewModel
import com.example.financetracker.ui.viewmodel.Period

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle(initialValue = emptyMap())
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val monthOffset by viewModel.monthOffset.collectAsStateWithLifecycle()
    
    val periodLabel = viewModel.getFormattedPeriodLabel(selectedPeriod, monthOffset)

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background_with_duck),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Monthly Review") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Text("←", fontSize = 24.sp)
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Period Selector Tabs
                ScrollableTabRow(
                    selectedTabIndex = when(selectedPeriod) {
                        is Period.CurrentMonth -> 0
                        is Period.Last3Months -> 1
                        is Period.Last6Months -> 2
                        is Period.LastYear -> 3
                        else -> 0
                    },
                    edgePadding = 0.dp,
                    containerColor = Color.White.copy(alpha = 0.5f)
                ) {
                    Tab(selected = selectedPeriod is Period.CurrentMonth, onClick = { viewModel.setPeriod(Period.CurrentMonth) }) {
                        Text("1 Month", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedPeriod is Period.Last3Months, onClick = { viewModel.setPeriod(Period.Last3Months) }) {
                        Text("3 Months", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedPeriod is Period.Last6Months, onClick = { viewModel.setPeriod(Period.Last6Months) }) {
                        Text("6 Months", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedPeriod is Period.LastYear, onClick = { viewModel.setPeriod(Period.LastYear) }) {
                        Text("1 Year", modifier = Modifier.padding(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.updateMonthOffset(-1) }) {
                        Text("‹", fontSize = 32.sp)
                    }
                    Text(
                        text = periodLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = { viewModel.updateMonthOffset(1) }) {
                        Text("›", fontSize = 32.sp)
                    }
                }

                // Explicit Date Range Display
                Text(
                    text = viewModel.getFormattedDateRange(selectedPeriod, monthOffset),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (categoryBreakdown.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            CategoryReviewSection(categoryBreakdown, currencyConfig)
                        }
                        item {
                            AllCategoriesBreakdown(categoryBreakdown, currencyConfig)
                        }
                    }
                }
            }
        }
    }
}