package com.rayven0421.ashnotes.data.repository

import com.rayven0421.ashnotes.data.local.dao.BudgetDao
import com.rayven0421.ashnotes.data.local.dao.ExpenseDao
import com.rayven0421.ashnotes.data.mapper.toDomain
import com.rayven0421.ashnotes.data.mapper.toEntity
import com.rayven0421.ashnotes.domain.model.Budget
import com.rayven0421.ashnotes.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao
) : BudgetRepository {

    override fun observeBudgets(workspaceId: String): Flow<List<Budget>> {
        return budgetDao.observeBudgets(workspaceId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeBudgetById(id: String): Flow<Budget?> {
        return budgetDao.observeBudgetById(id).map { it?.toDomain() }
    }

    override suspend fun getBudgetById(id: String): Budget? {
        return budgetDao.getBudgetById(id)?.toDomain()
    }

    override suspend fun createBudget(budget: Budget): String {
        val entity = budget.toEntity()
        budgetDao.insertBudget(entity)
        return entity.id
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(id: String) {
        budgetDao.softDeleteBudget(id)
    }

    override fun observeTotalSpent(budgetId: String): Flow<Long> {
        return expenseDao.observeTotalSpent(budgetId)
    }

    override suspend fun getTotalSpent(budgetId: String): Long {
        return expenseDao.getTotalSpent(budgetId)
    }
}
