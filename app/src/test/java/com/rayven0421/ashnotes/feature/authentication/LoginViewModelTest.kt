package com.rayven0421.ashnotes.feature.authentication

import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.UiState
import com.rayven0421.ashnotes.domain.model.AuthError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var sessionRepository: FakeSessionRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        sessionRepository = FakeSessionRepository()
        viewModel = LoginViewModel(authRepository, sessionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun signIn_validationError_setsEmailAndPasswordErrors() = runTest {
        viewModel.updateEmail("invalid")
        viewModel.updatePassword("123")
        viewModel.signIn {}

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val formData = (state as UiState.Success).data
        assertEquals(R.string.auth_error_invalid_email, formData.emailErrorResId)
        assertEquals(R.string.auth_error_password_too_short, formData.passwordErrorResId)
        assertFalse(formData.isSubmitting)
    }

    @Test
    fun signIn_success_updatesSessionModeAndTriggersCallback() = runTest {
        var callbackCalled = false
        viewModel.updateEmail("user@example.com")
        viewModel.updatePassword("password123")
        viewModel.signIn { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val formData = (state as UiState.Success).data
        assertFalse(formData.isSubmitting)
        assertNull(formData.generalErrorResId)
        assertFalse(sessionRepository.isLocalMode.value)
        assertTrue(callbackCalled)
    }

    @Test
    fun signIn_failure_setsGeneralErrorResId() = runTest {
        authRepository.errorToReturn = AuthError.INVALID_CREDENTIALS
        var callbackCalled = false
        viewModel.updateEmail("user@example.com")
        viewModel.updatePassword("wrongpassword")
        viewModel.signIn { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val formData = (state as UiState.Success).data
        assertFalse(formData.isSubmitting)
        assertEquals(R.string.auth_error_invalid_credentials, formData.generalErrorResId)
        assertFalse(callbackCalled)
    }

    @Test
    fun signInWithGoogle_success_updatesSessionModeAndTriggersCallback() = runTest {
        var callbackCalled = false
        viewModel.signInWithGoogle("fake_google_id_token") { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val formData = (state as UiState.Success).data
        assertFalse(formData.isSubmitting)
        assertNull(formData.generalErrorResId)
        assertFalse(sessionRepository.isLocalMode.value)
        assertTrue(callbackCalled)
    }

    @Test
    fun signInWithGoogle_failure_setsGeneralErrorResId() = runTest {
        authRepository.errorToReturn = AuthError.NETWORK_ERROR
        var callbackCalled = false
        viewModel.signInWithGoogle("fake_google_id_token") { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val formData = (state as UiState.Success).data
        assertFalse(formData.isSubmitting)
        assertEquals(R.string.auth_error_network, formData.generalErrorResId)
        assertFalse(callbackCalled)
    }

    @Test
    fun continueWithoutAccount_setsLocalModeTrueAndTriggersCallback() = runTest {
        var callbackCalled = false
        viewModel.continueWithoutAccount { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(sessionRepository.isLocalMode.value)
        assertTrue(callbackCalled)
    }
}
