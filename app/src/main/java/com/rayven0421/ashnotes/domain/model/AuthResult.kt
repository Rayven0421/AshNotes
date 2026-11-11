package com.rayven0421.ashnotes.domain.model

sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val error: AuthError) : AuthResult
}
