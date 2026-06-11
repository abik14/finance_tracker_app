package com.example.financetracker.data.database

import androidx.room.*
import com.example.financetracker.data.model.Category
import com.example.financetracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category)

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY type, name")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    // NEW: Get only custom categories (user-created)
    @Query("SELECT * FROM categories WHERE isDefault = 0 ORDER BY name")
    fun getCustomCategories(): Flow<List<Category>>

    // NEW: Check if category name already exists
    @Query("SELECT COUNT(*) FROM categories WHERE name = :name AND type = :type")
    suspend fun getCategoryCountByName(name: String, type: TransactionType): Int
}