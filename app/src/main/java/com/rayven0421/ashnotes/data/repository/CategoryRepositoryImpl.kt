package com.rayven0421.ashnotes.data.repository

import com.rayven0421.ashnotes.data.local.dao.CategoryDao
import com.rayven0421.ashnotes.data.mapper.*
import com.rayven0421.ashnotes.domain.model.Category
import com.rayven0421.ashnotes.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> {
        return categoryDao.observeCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: String): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun createCategory(category: Category): String {
        val entity = category.toEntity()
        categoryDao.insertCategory(entity)
        return entity.id
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(id: String) {
        categoryDao.softDeleteCategory(id)
    }
}
