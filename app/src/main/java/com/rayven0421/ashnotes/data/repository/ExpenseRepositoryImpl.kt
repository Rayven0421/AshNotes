package com.rayven0421.ashnotes.data.repository

import com.rayven0421.ashnotes.data.local.dao.ExpenseDao
import com.rayven0421.ashnotes.data.mapper.*
import com.rayven0421.ashnotes.domain.model.Expense
import com.rayven0421.ashnotes.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun observeExpenses(budgetId: String): Flow<List<Expense>> {
        return expenseDao.observeExpenses(budgetId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExpenseById(id: String): Expense? {
        return expenseDao.getExpenseById(id)?.toDomain()
    }

    override suspend fun createExpense(expense: Expense): String {
        val entity = expense.toEntity()
        expenseDao.insertExpense(entity)
        return entity.id
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(id: String) {
        expenseDao.softDeleteExpense(id)
    }
}
