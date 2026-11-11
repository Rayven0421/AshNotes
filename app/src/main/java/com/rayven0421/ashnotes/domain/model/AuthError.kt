package com.rayven0421.ashnotes.domain.model

enum class AuthError {
    INVALID_CREDENTIALS,
    USER_NOT_FOUND,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    NETWORK_ERROR,
    TOO_MANY_REQUESTS,
    UNKNOWN
}
