package com.rayven0421.ashnotes.domain.repository

import com.rayven0421.ashnotes.domain.model.AuthResult
import com.rayven0421.ashnotes.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun register(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut(): AuthResult
}
