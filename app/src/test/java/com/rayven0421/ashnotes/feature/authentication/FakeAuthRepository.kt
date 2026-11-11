package com.rayven0421.ashnotes.feature.authentication

import com.rayven0421.ashnotes.domain.model.AuthError
import com.rayven0421.ashnotes.domain.model.AuthResult
import com.rayven0421.ashnotes.domain.model.AuthUser
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    var errorToReturn: AuthError? = null

    override suspend fun signIn(email: String, password: String): AuthResult {
        val err = errorToReturn
        return if (err != null) {
            AuthResult.Error(err)
        } else {
            _currentUser.value = AuthUser(uid = "test_uid", email = email)
            AuthResult.Success
        }
    }

    override suspend fun register(email: String, password: String): AuthResult {
        val err = errorToReturn
        return if (err != null) {
            AuthResult.Error(err)
        } else {
            _currentUser.value = AuthUser(uid = "test_uid", email = email)
            AuthResult.Success
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        val err = errorToReturn
        return if (err != null) {
            AuthResult.Error(err)
        } else {
            _currentUser.value = AuthUser(uid = "google_uid", email = "google@example.com")
            AuthResult.Success
        }
    }

    override suspend fun signOut(): AuthResult {
        _currentUser.value = null
        return AuthResult.Success
    }
}
