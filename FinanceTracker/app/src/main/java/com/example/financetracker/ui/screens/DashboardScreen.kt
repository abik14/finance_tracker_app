package com.example.financetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.R
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.CurrencyConfig
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import com.example.financetracker.ui.components.NoteCard
import com.example.financetracker.ui.viewmodel.FinanceViewModel
import com.example.financetracker.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onViewAllExpenses: () -> Unit,
    onExtractFromFile: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    val transactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val currencyConfig by viewModel.currencyConfig.collectAsStateWithLifecycle()
    
    var fabExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (fabExpanded) 45f else 0f)

    Box(modifier = Modifier.fillMaxSize()) {
        // Full background image - made more opaque to see the duck better
        Image(
            painter = painterResource(id = R.drawable.background_with_duck),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1.0f 
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Custom Beige Header from sketch
                Surface(
                    color = Color(0xFFF3D9B1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .shadow(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_foreground),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 12.dp)
                            )
                            Text(
                                text = "Finance tracker",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        
                        // Sticker-style settings button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable { onNavigateToSettings() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚙️", fontSize = 28.sp)
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = Color(0xFFF3D9B1),
                    contentColor = Color.Black
                ) {
                    Text("+", fontSize = 24.sp, modifier = Modifier.rotate(rotation))
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                // Summary Section (Income/Expense small notes)
                item {
                }

                // Note 1: Category Breakdown (Pie Chart)
                item {
                    val dashboardBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle(initialValue = emptyMap())
                    NoteCard(
                        title = "Monthly Review",
                        action = {
                            TextButton(onClick = onNavigateToStatistics, contentPadding = PaddingValues(0.dp)) {
                                Text("Show More ›", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    ) {
                        if (dashboardBreakdown.isNotEmpty()) {
                            CategoryReviewSection(dashboardBreakdown, currencyConfig)
                        } else {
                            Text("No expenses yet this month.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                }

                // Note 2: Recent Transactions - Smaller and shifted to the right
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        NoteCard(
                            modifier = Modifier.fillMaxWidth(0.7f),
                            title = "Recents",
                            action = {
                                TextButton(onClick = onViewAllExpenses, contentPadding = PaddingValues(0.dp)) {
                                    Text("Show All", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        ) {
                            if (transactions.isEmpty()) {
                                Text("No transactions this month", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                            } else {
                                transactions.take(3).forEach { transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        currencyConfig = currencyConfig
                                    )
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB Menu Overlay
        if (fabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { fabExpanded = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionRow("Add Manually", "✏️") { fabExpanded = false; onAddTransaction() }
                    ActionRow("Extract from File", "📁") { fabExpanded = false; onExtractFromFile() }
                }
            }
        }
    }
}

@Composable
fun ActionRow(label: String, icon: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.width(12.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = Color(0xFFF3D9B1)
        ) {
            Text(icon)
        }
    }
}

@Composable
fun CategoryReviewSection(breakdown: Map<Category, Double>, currencyConfig: CurrencyConfig) {
    val topCategories = breakdown.entries.sortedByDescending { it.value }.take(4)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SimplePieChart(
            data = breakdown,
            modifier = Modifier.size(110.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f).padding(start = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            topCategories.forEach { (category, amount) ->
                CategoryLegendItem(category, amount, currencyConfig)
            }
        }
    }
}

@Composable
fun CategoryLegendItem(category: Category, amount: Double, currencyConfig: CurrencyConfig) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color(android.graphics.Color.parseColor(category.color)), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.name,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            color = Color.DarkGray
        )
        Text(
            text = currencyConfig.format(amount),
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    currencyConfig: CurrencyConfig,
    onDelete: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (transaction.type == TransactionType.INCOME) "💰 Income" else "💸 Expense",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Text(
                text = currencyConfig.format(transaction.amount),
                color = if (transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).padding(start = 8.dp)) {
                    Text("🗑️", fontSize = 14.sp)
                }
            }
        }
        Text(
            text = DateUtils.formatDate(transaction.date),
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun SimplePieChart(data: Map<Category, Double>, modifier: Modifier) {
    val total = data.values.sum()
    Canvas(modifier = modifier) {
        var startAngle = 0f
        data.forEach { (category, amount) ->
            val sweepAngle = (amount / total).toFloat() * 360f
            drawArc(
                color = Color(android.graphics.Color.parseColor(category.color)),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun AllCategoriesBreakdown(breakdown: Map<Category, Double>, currencyConfig: CurrencyConfig) {
    // Sort categories alphabetically by name
    val sortedBreakdown = breakdown.entries.sortedBy { it.key.name }

    NoteCard(title = "Category Breakdown") {
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sortedBreakdown.forEach { (category, amount) ->
                CategoryBreakdownItem(category, amount, currencyConfig)
                if (category != sortedBreakdown.last().key) {
                    HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownItem(category: Category, amount: Double, currencyConfig: CurrencyConfig) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(android.graphics.Color.parseColor(category.color)), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "${category.icon} ${category.name}", fontSize = 14.sp, color = Color.Black)
        }
        Text(
            text = currencyConfig.format(amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
