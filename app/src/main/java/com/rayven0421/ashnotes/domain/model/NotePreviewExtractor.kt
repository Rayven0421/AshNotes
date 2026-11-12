package com.rayven0421.ashnotes.domain.model

import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
}

fun extractNotePreview(contentJson: String): String {
    if (contentJson.isBlank()) return ""
    return try {
        val document = json.decodeFromString(Document.serializer(), contentJson)
        val textBlock = document.blocks.firstNotNullOfOrNull { block ->
            when (block) {
                is TextBlock -> {
                    val text = block.text
                    if (text.isNotBlank()) {
                        text
                    } else if (block is Block.Checklist) {
                        block.items.firstOrNull { it.text.isNotBlank() }?.text
                    } else {
                        null
                    }
                }
                else -> null
            }
        } ?: return ""

        val collapsed = textBlock.replace(Regex("\\s+"), " ").trim()
        if (collapsed.length > 120) {
            collapsed.take(120)
        } else {
            collapsed
        }
    } catch (_: Throwable) {
        ""
    }
}
