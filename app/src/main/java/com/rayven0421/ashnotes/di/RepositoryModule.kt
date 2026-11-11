package com.rayven0421.ashnotes.di

import com.rayven0421.ashnotes.data.repository.AuthRepositoryImpl
import com.rayven0421.ashnotes.data.repository.BudgetRepositoryImpl
import com.rayven0421.ashnotes.data.repository.CategoryRepositoryImpl
import com.rayven0421.ashnotes.data.repository.ExpenseRepositoryImpl
import com.rayven0421.ashnotes.data.repository.NoteRepositoryImpl
import com.rayven0421.ashnotes.data.repository.SessionRepositoryImpl
import com.rayven0421.ashnotes.data.repository.UserPreferencesRepositoryImpl
import com.rayven0421.ashnotes.data.repository.WorkspaceRepositoryImpl
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import com.rayven0421.ashnotes.domain.repository.BudgetRepository
import com.rayven0421.ashnotes.domain.repository.CategoryRepository
import com.rayven0421.ashnotes.domain.repository.ExpenseRepository
import com.rayven0421.ashnotes.domain.repository.NoteRepository
import com.rayven0421.ashnotes.domain.repository.SessionRepository
import com.rayven0421.ashnotes.domain.repository.UserPreferencesRepository
import com.rayven0421.ashnotes.domain.repository.WorkspaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkspaceRepository(
        impl: WorkspaceRepositoryImpl
    ): WorkspaceRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository
}
