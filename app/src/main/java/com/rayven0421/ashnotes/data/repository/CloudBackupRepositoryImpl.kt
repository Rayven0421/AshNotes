package com.rayven0421.ashnotes.data.repository

import androidx.room.withTransaction
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.rayven0421.ashnotes.data.local.AshNotesDatabase
import com.rayven0421.ashnotes.data.local.dao.BudgetDao
import com.rayven0421.ashnotes.data.local.dao.CategoryDao
import com.rayven0421.ashnotes.data.local.dao.ExpenseDao
import com.rayven0421.ashnotes.data.local.dao.NoteDao
import com.rayven0421.ashnotes.data.local.dao.WorkspaceDao
import com.rayven0421.ashnotes.data.mapper.NoteSizeChecker
import com.rayven0421.ashnotes.data.mapper.RestoreMergeDecision
import com.rayven0421.ashnotes.data.mapper.budgetEntityFromMap
import com.rayven0421.ashnotes.data.mapper.categoryEntityFromMap
import com.rayven0421.ashnotes.data.mapper.expenseEntityFromMap
import com.rayven0421.ashnotes.data.mapper.noteEntityFromMap
import com.rayven0421.ashnotes.data.mapper.toMap
import com.rayven0421.ashnotes.data.mapper.workspaceEntityFromMap
import com.rayven0421.ashnotes.domain.model.CloudBackupError
import com.rayven0421.ashnotes.domain.model.CloudBackupResult
import com.rayven0421.ashnotes.domain.repository.CloudBackupRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudBackupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val database: AshNotesDatabase,
    private val workspaceDao: WorkspaceDao,
    private val noteDao: NoteDao,
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) : CloudBackupRepository {

    override suspend fun backup(): CloudBackupResult {
        val uid = firebaseAuth.currentUser?.uid
            ?: return CloudBackupResult.Error(CloudBackupError.NOT_SIGNED_IN)

        return try {
            val workspaces = workspaceDao.getAllWorkspacesIncludingDeleted()
            val categories = categoryDao.getAllCategoriesIncludingDeleted()
            val budgets = budgetDao.getAllBudgetsIncludingDeleted()
            val notes = noteDao.getAllNotesIncludingDeleted()
            val expenses = expenseDao.getAllExpensesIncludingDeleted()

            val validNotes = notes.filter { !NoteSizeChecker.isTooLarge(it.contentJson) }
            val skippedLargeCount = notes.size - validNotes.size

            val operations = mutableListOf<Pair<DocumentReference, Map<String, Any?>>>()

            workspaces.forEach { workspace ->
                val ref = firestore.collection("users").document(uid).collection("workspaces").document(workspace.id)
                operations.add(ref to workspace.toMap())
            }
            categories.forEach { category ->
                val ref = firestore.collection("users").document(uid).collection("categories").document(category.id)
                operations.add(ref to category.toMap())
            }
            budgets.forEach { budget ->
                val ref = firestore.collection("users").document(uid).collection("budgets").document(budget.id)
                operations.add(ref to budget.toMap())
            }
            validNotes.forEach { note ->
                val ref = firestore.collection("users").document(uid).collection("notes").document(note.id)
                operations.add(ref to note.toMap())
            }
            expenses.forEach { expense ->
                val ref = firestore.collection("users").document(uid).collection("expenses").document(expense.id)
                operations.add(ref to expense.toMap())
            }

            if (operations.isNotEmpty()) {
                val chunks = operations.chunked(400)
                for (chunk in chunks) {
                    val batch = firestore.batch()
                    for ((ref, map) in chunk) {
                        batch.set(ref, map)
                    }
                    withTimeout(15_000L) {
                        batch.commit().awaitTask()
                    }
                }
            }

            val uploadedCount = workspaces.size + categories.size + budgets.size + validNotes.size + expenses.size
            CloudBackupResult.Success(uploaded = uploadedCount, skippedLarge = skippedLargeCount)
        } catch (e: TimeoutCancellationException) {
            CloudBackupResult.Error(CloudBackupError.NETWORK)
        } catch (e: FirebaseFirestoreException) {
            CloudBackupResult.Error(mapFirestoreException(e))
        } catch (e: Exception) {
            CloudBackupResult.Error(mapGenericException(e))
        }
    }

    override suspend fun restore(): CloudBackupResult {
        val uid = firebaseAuth.currentUser?.uid
            ?: return CloudBackupResult.Error(CloudBackupError.NOT_SIGNED_IN)

        return try {
            val workspacesTask = firestore.collection("users").document(uid).collection("workspaces").get()
            val workspacesSnapshot = withTimeout(15_000L) { workspacesTask.awaitTask() }

            val categoriesTask = firestore.collection("users").document(uid).collection("categories").get()
            val categoriesSnapshot = withTimeout(15_000L) { categoriesTask.awaitTask() }

            val budgetsTask = firestore.collection("users").document(uid).collection("budgets").get()
            val budgetsSnapshot = withTimeout(15_000L) { budgetsTask.awaitTask() }

            val notesTask = firestore.collection("users").document(uid).collection("notes").get()
            val notesSnapshot = withTimeout(15_000L) { notesTask.awaitTask() }

            val expensesTask = firestore.collection("users").document(uid).collection("expenses").get()
            val expensesSnapshot = withTimeout(15_000L) { expensesTask.awaitTask() }

            val localWorkspaceIds = workspaceDao.getAllWorkspacesIncludingDeleted().map { it.id }.toSet()
            val localCategoryIds = categoryDao.getAllCategoriesIncludingDeleted().map { it.id }.toSet()
            val localBudgetIds = budgetDao.getAllBudgetsIncludingDeleted().map { it.id }.toSet()
            val localNoteIds = noteDao.getAllNotesIncludingDeleted().map { it.id }.toSet()
            val localExpenseIds = expenseDao.getAllExpensesIncludingDeleted().map { it.id }.toSet()

            val remoteWorkspaces = workspacesSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { workspaceEntityFromMap(it) }
            }
            val remoteCategories = categoriesSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { categoryEntityFromMap(it) }
            }
            val remoteBudgets = budgetsSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { budgetEntityFromMap(it) }
            }
            val remoteNotes = notesSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { noteEntityFromMap(it) }
            }
            val remoteExpenses = expensesSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { expenseEntityFromMap(it) }
            }

            val workspacesToInsert = RestoreMergeDecision.filterNewItems(remoteWorkspaces, localWorkspaceIds) { it.id }
            val categoriesToInsert = RestoreMergeDecision.filterNewItems(remoteCategories, localCategoryIds) { it.id }
            val budgetsToInsert = RestoreMergeDecision.filterNewItems(remoteBudgets, localBudgetIds) { it.id }
            val notesToInsert = RestoreMergeDecision.filterNewItems(remoteNotes, localNoteIds) { it.id }
            val expensesToInsert = RestoreMergeDecision.filterNewItems(remoteExpenses, localExpenseIds) { it.id }

            if (workspacesToInsert.isNotEmpty()) {
                database.withTransaction {
                    workspaceDao.insertWorkspaces(workspacesToInsert)
                }
            }
            if (categoriesToInsert.isNotEmpty()) {
                database.withTransaction {
                    categoryDao.insertCategories(categoriesToInsert)
                }
            }
            if (budgetsToInsert.isNotEmpty()) {
                database.withTransaction {
                    budgetDao.insertBudgets(budgetsToInsert)
                }
            }
            if (notesToInsert.isNotEmpty()) {
                database.withTransaction {
                    noteDao.insertNotes(notesToInsert)
                }
            }
            if (expensesToInsert.isNotEmpty()) {
                database.withTransaction {
                    expenseDao.insertExpenses(expensesToInsert)
                }
            }

            val totalAdded = workspacesToInsert.size + categoriesToInsert.size + budgetsToInsert.size + notesToInsert.size + expensesToInsert.size
            CloudBackupResult.Success(added = totalAdded)
        } catch (e: TimeoutCancellationException) {
            CloudBackupResult.Error(CloudBackupError.NETWORK)
        } catch (e: FirebaseFirestoreException) {
            CloudBackupResult.Error(mapFirestoreException(e))
        } catch (e: Exception) {
            CloudBackupResult.Error(mapGenericException(e))
        }
    }

    private fun mapFirestoreException(e: FirebaseFirestoreException): CloudBackupError {
        return when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> CloudBackupError.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.CANCELLED,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> CloudBackupError.NETWORK
            else -> CloudBackupError.UNKNOWN
        }
    }

    private fun mapGenericException(e: Exception): CloudBackupError {
        return if (e.message?.contains("network", ignoreCase = true) == true) {
            CloudBackupError.NETWORK
        } else {
            CloudBackupError.UNKNOWN
        }
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) continuation.resume(result)
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) continuation.resumeWithException(exception)
    }
}
