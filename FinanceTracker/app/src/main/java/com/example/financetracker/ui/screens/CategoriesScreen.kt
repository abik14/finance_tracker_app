package com.example.financetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.TransactionType
import com.example.financetracker.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val expenseCategories by viewModel.getCategoriesByType(TransactionType.EXPENSE)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val incomeCategories by viewModel.getCategoriesByType(TransactionType.INCOME)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Text("➕", fontSize = 20.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row for Expense/Income
            TabRow(
                selectedTabIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    text = { Text("Expenses 💸") }
                )
                Tab(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    text = { Text("Income 💰") }
                )
            }

            // Category list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onDelete = {
                            if (!category.isDefault) {
                                viewModel.deleteCategory(category)
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddDialog) {
        AddCategoryDialog(
            type = selectedType,
            onDismiss = { showAddDialog = false },
            onAdd = { name, icon, color ->
                viewModel.addCategory(
                    name = name,
                    icon = icon,
                    color = color,
                    type = selectedType
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = category.icon, fontSize = 24.sp)
                Column {
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    if (category.isDefault) {
                        Text(
                            text = "Default",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (!category.isDefault) {
                IconButton(onClick = onDelete) {
                    Text("🗑️", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("📌") }
    var selectedColor by remember { mutableStateOf("#9C27B0") }

    val colorOptions = listOf(
        "#FF5722" to "🍊", "#2196F3" to "💙", "#9C27B0" to "💜",
        "#4CAF50" to "💚", "#F44336" to "❤️", "#FF9800" to "🧡",
        "#00BCD4" to "💎", "#795548" to "🤎", "#607D8B" to "🩶"
    )

    val iconOptions = listOf(
        "🍔", "🚗", "🛍️", "🎬", "💡", "🏥", "📚", "✈️", "🎮",
        "☕", "🍺", "🎵", "🏋️", "💅", "🐶", "🌱", "🎓", "💝"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add ${if (type == TransactionType.EXPENSE) "Expense" else "Income"} Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon picker
                Text("Choose Icon", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    iconOptions.take(8).forEach { optionIcon ->
                        FilterChip(
                            selected = icon == optionIcon,
                            onClick = { icon = optionIcon },
                            label = { Text(optionIcon) }
                        )
                    }
                }

                // Color picker
                Text("Choose Color", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorOptions.forEach { (colorHex, colorIcon) ->
                        FilterChip(
                            selected = selectedColor == colorHex,
                            onClick = { selectedColor = colorHex },
                            label = { Text(colorIcon) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(android.graphics.Color.parseColor(colorHex)).copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { if (name.isNotBlank()) onAdd(name, icon, selectedColor) },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}