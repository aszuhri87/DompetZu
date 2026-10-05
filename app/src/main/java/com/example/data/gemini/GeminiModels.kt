package com.example.data.gemini

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

enum class GeminiChatModel(
    val modelId: String,
    val displayName: String,
    val badge: String,
    val description: String
) {
    FLASH("gemini-3.5-flash", "Gemini 3.5 Flash", "Standar", "Cepat & akurat untuk percakapan umum"),
    PRO("gemini-3.1-pro-preview", "Gemini 3.1 Pro", "Analisis Tinggi", "Penalaran mendalam & strategi finansial kompleks"),
    LITE("gemini-3.1-flash-lite-preview", "Gemini 3.1 Flash Lite", "Kilat", "Respon instan untuk kalkulasi cepat & tips ringkas")
}

enum class MessageRole {
    USER, MODEL, SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = false,
    val isError: Boolean = false,
    val modelUsed: GeminiChatModel? = null
)

// --- Moshi DTOs for Gemini REST API ---

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = null, // "user" or "model"
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inline_data") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "data") val data: String
)

data class ParsedReceipt(
    val merchant: String,
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val category: String = "Makanan & Minuman",
    val type: String = "EXPENSE",
    val walletName: String = "Rekening Bank",
    val note: String = "",
    val itemsSummary: String = "",
    val confidence: String = "Tinggi"
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @Json(name = "error") val error: GeminiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorDetails(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: String? = null
)
