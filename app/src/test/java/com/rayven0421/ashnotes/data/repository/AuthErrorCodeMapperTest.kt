package com.rayven0421.ashnotes.data.repository

import com.rayven0421.ashnotes.domain.model.AuthError
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorCodeMapperTest {

    @Test
    fun mapErrorCodeToAuthError_mapsKnownCodesCorrectly() {
        assertEquals(AuthError.INVALID_EMAIL, mapErrorCodeToAuthError("invalid-email"))
        assertEquals(AuthError.INVALID_EMAIL, mapErrorCodeToAuthError("ERROR_INVALID_EMAIL"))
        assertEquals(AuthError.INVALID_CREDENTIALS, mapErrorCodeToAuthError("wrong-password"))
        assertEquals(AuthError.INVALID_CREDENTIALS, mapErrorCodeToAuthError("ERROR_WRONG_PASSWORD"))
        assertEquals(AuthError.USER_NOT_FOUND, mapErrorCodeToAuthError("user-not-found"))
        assertEquals(AuthError.EMAIL_ALREADY_IN_USE, mapErrorCodeToAuthError("email-already-in-use"))
        assertEquals(AuthError.WEAK_PASSWORD, mapErrorCodeToAuthError("weak-password"))
        assertEquals(AuthError.TOO_MANY_REQUESTS, mapErrorCodeToAuthError("too-many-requests"))
        assertEquals(AuthError.NETWORK_ERROR, mapErrorCodeToAuthError("network-request-failed"))
    }

    @Test
    fun mapErrorCodeToAuthError_mapsUnknownCodeToUnknown() {
        assertEquals(AuthError.UNKNOWN, mapErrorCodeToAuthError("some_random_error_code"))
        assertEquals(AuthError.UNKNOWN, mapErrorCodeToAuthError(null))
    }
}
