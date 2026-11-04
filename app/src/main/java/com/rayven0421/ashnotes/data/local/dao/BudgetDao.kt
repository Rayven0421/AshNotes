package com.rayven0421.ashnotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rayven0421.ashnotes.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE workspaceId = :workspaceId AND deletedAt IS NULL ORDER BY startDate DESC")
    fun observeBudgets(workspaceId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id AND deletedAt IS NULL")
    fun observeBudgetById(id: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE id = :id AND deletedAt IS NULL")
    suspend fun getBudgetById(id: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("UPDATE budgets SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteBudget(id: String, deletedAt: Long = System.currentTimeMillis())
}
