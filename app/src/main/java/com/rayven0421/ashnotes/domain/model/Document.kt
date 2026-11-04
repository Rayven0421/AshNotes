package com.rayven0421.ashnotes.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val version: Int = CURRENT_VERSION,
    val blocks: List<Block> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}
