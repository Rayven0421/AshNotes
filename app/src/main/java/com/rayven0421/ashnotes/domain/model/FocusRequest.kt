package com.rayven0421.ashnotes.domain.model

data class FocusRequest(
    val blockId: String,
    val cursor: Int,
    val token: Long = System.currentTimeMillis()
)
