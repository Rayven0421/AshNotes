package com.rayven0421.ashnotes.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.rayven0421.ashnotes.domain.model.AuthError
import com.rayven0421.ashnotes.domain.model.AuthResult
import com.rayven0421.ashnotes.domain.model.AuthUser
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            trySend(firebaseUser?.let { AuthUser(it.uid, it.email) })
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).awaitTask()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseExceptionToAuthError(e))
        }
    }

    override suspend fun register(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).awaitTask()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseExceptionToAuthError(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).awaitTask()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseExceptionToAuthError(e))
        }
    }

    override suspend fun signOut(): AuthResult {
        return try {
            firebaseAuth.signOut()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseExceptionToAuthError(e))
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
}

internal fun mapFirebaseExceptionToAuthError(e: Exception): AuthError {
    if (e is FirebaseNetworkException || e.message?.contains("network", ignoreCase = true) == true) {
        return AuthError.NETWORK_ERROR
    }
    if (e is FirebaseAuthInvalidCredentialsException) {
        return AuthError.INVALID_CREDENTIALS
    }
    if (e is FirebaseAuthInvalidUserException) {
        return AuthError.USER_NOT_FOUND
    }
    if (e is FirebaseAuthUserCollisionException) {
        return AuthError.EMAIL_ALREADY_IN_USE
    }
    if (e is FirebaseAuthWeakPasswordException) {
        return AuthError.WEAK_PASSWORD
    }
    if (e is FirebaseAuthException) {
        return mapErrorCodeToAuthError(e.errorCode)
    }
    return AuthError.UNKNOWN
}

internal fun mapErrorCodeToAuthError(errorCode: String?): AuthError {
    return when (errorCode) {
        "ERROR_INVALID_EMAIL", "invalid-email" -> AuthError.INVALID_EMAIL
        "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIALS", "invalid-credential", "wrong-password" -> AuthError.INVALID_CREDENTIALS
        "ERROR_USER_NOT_FOUND", "user-not-found" -> AuthError.USER_NOT_FOUND
        "ERROR_EMAIL_ALREADY_IN_USE", "email-already-in-use" -> AuthError.EMAIL_ALREADY_IN_USE
        "ERROR_WEAK_PASSWORD", "weak-password" -> AuthError.WEAK_PASSWORD
        "ERROR_TOO_MANY_REQUESTS", "too-many-requests" -> AuthError.TOO_MANY_REQUESTS
        "ERROR_NETWORK_REQUEST_FAILED", "network-request-failed" -> AuthError.NETWORK_ERROR
        else -> AuthError.UNKNOWN
    }
}
