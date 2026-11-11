package com.rayven0421.ashnotes.feature.authentication

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun isValidEmail_returnsTrueForValidEmails() {
        assertTrue(isValidEmail("user@example.com"))
        assertTrue(isValidEmail("test.user+123@domain.co.uk"))
    }

    @Test
    fun isValidEmail_returnsFalseForInvalidEmails() {
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("notanemail"))
        assertFalse(isValidEmail("user@"))
        assertFalse(isValidEmail("@domain.com"))
        assertFalse(isValidEmail("user@domain"))
    }
}
