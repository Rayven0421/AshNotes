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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var sessionRepository: FakeSessionRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        sessionRepository = FakeSessionRepository()
        viewModel = RegisterViewModel(authRepository, sessionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun register_passwordsMismatch_setsConfirmPasswordError() = runTest {
        viewModel.updateEmail("user@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateConfirmPassword("different456")
        viewModel.register {}

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val formData = (state as UiState.Success).data
        assertEquals(R.string.auth_error_passwords_dont_match, formData.confirmPasswordErrorResId)
        assertFalse(formData.isSubmitting)
    }

    @Test
    fun register_success_updatesSessionModeAndTriggersCallback() = runTest {
        var callbackCalled = false
        viewModel.updateEmail("user@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateConfirmPassword("password123")
        viewModel.register { callbackCalled = true }

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
    fun register_failure_setsGeneralErrorResId() = runTest {
        authRepository.errorToReturn = AuthError.EMAIL_ALREADY_IN_USE
        var callbackCalled = false
        viewModel.updateEmail("existing@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateConfirmPassword("password123")
        viewModel.register { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val formData = (state as UiState.Success).data
        assertFalse(formData.isSubmitting)
        assertEquals(R.string.auth_error_email_already_in_use, formData.generalErrorResId)
        assertFalse(callbackCalled)
    }
}
