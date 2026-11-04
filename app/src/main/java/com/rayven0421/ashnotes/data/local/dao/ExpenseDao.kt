package com.rayven0421.ashnotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rayven0421.ashnotes.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE budgetId = :budgetId AND deletedAt IS NULL ORDER BY date DESC")
    fun observeExpenses(budgetId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id AND deletedAt IS NULL")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM expenses WHERE budgetId = :budgetId AND deletedAt IS NULL")
    fun observeTotalSpent(budgetId: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM expenses WHERE budgetId = :budgetId AND deletedAt IS NULL")
    suspend fun getTotalSpent(budgetId: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteExpense(id: String, deletedAt: Long = System.currentTimeMillis())
}
