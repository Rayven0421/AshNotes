package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudgets(workspaceId: String): Flow<List<Budget>>
    fun observeBudgetById(id: String): Flow<Budget?>
    suspend fun getBudgetById(id: String): Budget?
    suspend fun createBudget(budget: Budget): String
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(id: String)
    fun observeTotalSpent(budgetId: String): Flow<Long>
    suspend fun getTotalSpent(budgetId: String): Long
}
