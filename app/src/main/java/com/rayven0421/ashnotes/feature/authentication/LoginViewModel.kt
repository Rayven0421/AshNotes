package com.rayven0421.ashnotes.feature.authentication

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.domain.model.AuthError
import com.rayven0421.ashnotes.domain.model.AuthResult
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import com.rayven0421.ashnotes.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginFormData(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    @StringRes val emailErrorResId: Int? = null,
    @StringRes val passwordErrorResId: Int? = null,
    @StringRes val generalErrorResId: Int? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<LoginFormData>>(UiState.Success(LoginFormData()))
    val uiState: StateFlow<UiState<LoginFormData>> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(email = email, emailErrorResId = null, generalErrorResId = null))
    }

    fun updatePassword(password: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(password = password, passwordErrorResId = null, generalErrorResId = null))
    }

    fun togglePasswordVisibility() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(isPasswordVisible = !current.isPasswordVisible))
    }

    fun signIn(onSuccess: () -> Unit) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        val email = current.email.trim()
        val password = current.password

        var hasError = false
        var emailErr: Int? = null
        var passwordErr: Int? = null

        if (!isValidEmail(email)) {
            emailErr = R.string.auth_error_invalid_email
            hasError = true
        }
        if (password.length < 6) {
            passwordErr = R.string.auth_error_password_too_short
            hasError = true
        }

        if (hasError) {
            _uiState.value = UiState.Success(current.copy(emailErrorResId = emailErr, passwordErrorResId = passwordErr))
            return
        }

        _uiState.value = UiState.Success(current.copy(isSubmitting = true, generalErrorResId = null))

        viewModelScope.launch {
            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = UiState.Success(current.copy(isSubmitting = false))
                    sessionRepository.setLocalMode(false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    val errRes = result.error.toStringResId()
                    _uiState.value = UiState.Success(current.copy(isSubmitting = false, generalErrorResId = errRes))
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(isSubmitting = true, generalErrorResId = null))

        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    _uiState.value = UiState.Success(current.copy(isSubmitting = false))
                    sessionRepository.setLocalMode(false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    val errRes = result.error.toStringResId()
                    _uiState.value = UiState.Success(current.copy(isSubmitting = false, generalErrorResId = errRes))
                }
            }
        }
    }

    fun setGeneralError(@StringRes errorResId: Int) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(generalErrorResId = errorResId, isSubmitting = false))
    }

    fun continueWithoutAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.setLocalMode(true)
            onSuccess()
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
}

fun AuthError.toStringResId(): Int {
    return when (this) {
        AuthError.INVALID_EMAIL -> R.string.auth_error_invalid_email
        AuthError.WEAK_PASSWORD -> R.string.auth_error_weak_password
        AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
        AuthError.USER_NOT_FOUND -> R.string.auth_error_user_not_found
        AuthError.EMAIL_ALREADY_IN_USE -> R.string.auth_error_email_already_in_use
        AuthError.NETWORK_ERROR -> R.string.auth_error_network
        AuthError.TOO_MANY_REQUESTS -> R.string.auth_error_too_many_requests
        AuthError.UNKNOWN -> R.string.auth_error_unknown
    }
}
