package com.rayven0421.ashnotes.feature.authentication

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.domain.model.AuthResult
import com.rayven0421.ashnotes.domain.repository.AuthRepository
import com.rayven0421.ashnotes.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterFormData(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    @StringRes val emailErrorResId: Int? = null,
    @StringRes val passwordErrorResId: Int? = null,
    @StringRes val confirmPasswordErrorResId: Int? = null,
    @StringRes val generalErrorResId: Int? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<RegisterFormData>>(UiState.Success(RegisterFormData()))
    val uiState: StateFlow<UiState<RegisterFormData>> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(email = email, emailErrorResId = null, generalErrorResId = null))
    }

    fun updatePassword(password: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(password = password, passwordErrorResId = null, generalErrorResId = null))
    }

    fun updateConfirmPassword(password: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(confirmPassword = password, confirmPasswordErrorResId = null, generalErrorResId = null))
    }

    fun togglePasswordVisibility() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(isPasswordVisible = !current.isPasswordVisible))
    }

    fun toggleConfirmPasswordVisibility() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(isConfirmPasswordVisible = !current.isConfirmPasswordVisible))
    }

    fun register(onSuccess: () -> Unit) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        val email = current.email.trim()
        val password = current.password
        val confirmPassword = current.confirmPassword

        var hasError = false
        var emailErr: Int? = null
        var passwordErr: Int? = null
        var confirmErr: Int? = null

        if (!isValidEmail(email)) {
            emailErr = R.string.auth_error_invalid_email
            hasError = true
        }
        if (password.length < 6) {
            passwordErr = R.string.auth_error_password_too_short
            hasError = true
        }
        if (password != confirmPassword) {
            confirmErr = R.string.auth_error_passwords_dont_match
            hasError = true
        }

        if (hasError) {
            _uiState.value = UiState.Success(
                current.copy(
                    emailErrorResId = emailErr,
                    passwordErrorResId = passwordErr,
                    confirmPasswordErrorResId = confirmErr
                )
            )
            return
        }

        _uiState.value = UiState.Success(current.copy(isSubmitting = true, generalErrorResId = null))

        viewModelScope.launch {
            when (val result = authRepository.register(email, password)) {
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
