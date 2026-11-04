package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(budgetId: String): Flow<List<Expense>>
    suspend fun getExpenseById(id: String): Expense?
    suspend fun createExpense(expense: Expense): String
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
}
