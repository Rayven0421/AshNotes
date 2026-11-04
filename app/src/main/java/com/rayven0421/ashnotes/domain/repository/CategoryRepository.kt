package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun createCategory(category: Category): String
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: String)
}
