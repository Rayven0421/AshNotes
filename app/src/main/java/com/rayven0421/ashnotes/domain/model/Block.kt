package com.rayven0421.ashnotes.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

@Serializable
enum class SpanStyle {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    CODE,
    HIGHLIGHT,
    LINK
}

@Serializable
data class TextSpan(
    val start: Int,
    val end: Int,
    val style: SpanStyle,
    val url: String? = null
)

@Serializable
data class ChecklistItem(
    val id: String,
    val text: String,
    val isChecked: Boolean = false,
    val spans: List<TextSpan> = emptyList()
)

@Serializable(with = BlockSerializer::class)
sealed interface Block {
    val id: String
    val position: Int
    val createdAt: Long
    val updatedAt: Long
    val metadata: Map<String, String>

    @Serializable
    @SerialName("paragraph")
    data class Paragraph(
        override val id: String,
        override val position: Int,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val metadata: Map<String, String> = emptyMap(),
        val text: String = "",
        val spans: List<TextSpan> = emptyList()
    ) : Block

    @Serializable
    @SerialName("heading")
    data class Heading(
        override val id: String,
        override val position: Int,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val metadata: Map<String, String> = emptyMap(),
        val text: String = "",
        val level: Int = 1,
        val spans: List<TextSpan> = emptyList()
    ) : Block

    @Serializable
    @SerialName("checklist")
    data class Checklist(
        override val id: String,
        override val position: Int,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val metadata: Map<String, String> = emptyMap(),
        val text: String = "",
        val isChecked: Boolean = false,
        val items: List<ChecklistItem> = emptyList(),
        val spans: List<TextSpan> = emptyList()
    ) : Block

    @Serializable
    data class UnknownBlock(
        override val id: String,
        override val position: Int,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val metadata: Map<String, String> = emptyMap(),
        val rawType: String,
        val rawContent: JsonObject = JsonObject(emptyMap())
    ) : Block
}

object BlockSerializer : KSerializer<Block> {
    override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor

    override fun deserialize(decoder: Decoder): Block {
        require(decoder is JsonDecoder) { "BlockSerializer only supports JSON decoding" }
        val jsonObject = decoder.decodeJsonElement().jsonObject
        val type = jsonObject["type"]?.jsonPrimitive?.content ?: "unknown"

        return when (type) {
            "paragraph" -> decoder.json.decodeFromJsonElement(Block.Paragraph.serializer(), jsonObject)
            "heading" -> decoder.json.decodeFromJsonElement(Block.Heading.serializer(), jsonObject)
            "checklist" -> decoder.json.decodeFromJsonElement(Block.Checklist.serializer(), jsonObject)
            else -> {
                val id = jsonObject["id"]?.jsonPrimitive?.content ?: ""
                val position = jsonObject["position"]?.jsonPrimitive?.int ?: 0
                val createdAt = jsonObject["createdAt"]?.jsonPrimitive?.long ?: 0L
                val updatedAt = jsonObject["updatedAt"]?.jsonPrimitive?.long ?: 0L
                val metadata = try {
                    jsonObject["metadata"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()
                } catch (_: Exception) {
                    emptyMap()
                }
                Block.UnknownBlock(
                    id = id,
                    position = position,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    metadata = metadata,
                    rawType = type,
                    rawContent = jsonObject
                )
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Block) {
        require(encoder is JsonEncoder) { "BlockSerializer only supports JSON encoding" }
        when (value) {
            is Block.Paragraph -> {
                val element = encoder.json.encodeToJsonElement(Block.Paragraph.serializer(), value).jsonObject
                val result = buildJsonObject {
                    put("type", JsonPrimitive("paragraph"))
                    element.forEach { (k, v) -> put(k, v) }
                }
                encoder.encodeJsonElement(result)
            }
            is Block.Heading -> {
                val element = encoder.json.encodeToJsonElement(Block.Heading.serializer(), value).jsonObject
                val result = buildJsonObject {
                    put("type", JsonPrimitive("heading"))
                    element.forEach { (k, v) -> put(k, v) }
                }
                encoder.encodeJsonElement(result)
            }
            is Block.Checklist -> {
                val element = encoder.json.encodeToJsonElement(Block.Checklist.serializer(), value).jsonObject
                val result = buildJsonObject {
                    put("type", JsonPrimitive("checklist"))
                    element.forEach { (k, v) -> put(k, v) }
                }
                encoder.encodeJsonElement(result)
            }
            is Block.UnknownBlock -> {
                val result = buildJsonObject {
                    value.rawContent.forEach { (k, v) ->
                        if (k != "type") put(k, v)
                    }
                    put("type", JsonPrimitive(value.rawType))
                }
                encoder.encodeJsonElement(result)
            }
        }
    }
}
