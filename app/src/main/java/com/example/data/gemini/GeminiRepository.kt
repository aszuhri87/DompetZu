package com.example.data.gemini

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class GeminiRepository(private val apiService: GeminiApiService = GeminiApiClient.service) {

    private val systemInstructionText = """
        Anda adalah Konsultan Keuangan Pribadi DompetZu yang cerdas, ramah, dan solutif.
        
        Peran dan Pedoman Anda:
        1. Membantu pengguna mengelola keuangan pribadi, menganalisis pemasukan dan pengeluaran, serta merencanakan anggaran bulanan.
        2. Memberikan saran penghematan yang realistis, strategi alokasi dana darurat, serta aturan finansial sehat (seperti metode 50/30/20).
        3. Menjawab pertanyaan dalam Bahasa Indonesia yang santun, praktis, dan terstruktur rapi (gunakan poin-poin agar nyaman dibaca di layar smartphone).
        4. Jika konteks data finansial pengguna disertakan, gunakan data tersebut secara akurat untuk memberikan wawasan khusus yang relevan bagi pengguna.
        5. Hindari jargon yang terlalu rumit dan selalu berikan motivasi positif dalam mencapai kebebasan finansial.
    """.trimIndent()

    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String,
        model: GeminiChatModel,
        financialContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Kunci Gemini API belum dikonfigurasi. Silakan atur GEMINI_API_KEY pada panel Secrets.")
            )
        }

        // Build multi-turn contents list
        val contents = mutableListOf<GeminiContent>()

        // Add previous conversation turns
        history.forEach { msg ->
            if (msg.role == MessageRole.USER) {
                contents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            } else if (msg.role == MessageRole.MODEL && !msg.isError) {
                contents.add(
                    GeminiContent(
                        role = "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
        }

        // Current turn: combine user message with financial context if provided
        val fullPrompt = if (!financialContext.isNullOrBlank()) {
            "$userMessage\n\n[Data Keuangan Terkini Pengguna]:\n$financialContext"
        } else {
            userMessage
        }

        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = fullPrompt))
            )
        )

        val request = GeminiGenerateContentRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = systemInstructionText))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                topK = 40
            )
        )

        try {
            val response = apiService.generateContent(
                model = model.modelId,
                apiKey = apiKey,
                request = request
            )

            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!replyText.isNullOrBlank()) {
                Result.success(replyText)
            } else {
                val errorMsg = response.error?.message ?: "Tidak ada respon teks dari model AI."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractReceipt(bitmap: Bitmap): Result<ParsedReceipt> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent fallback / demo parser when key is missing so user can still test offline!
            return@withContext Result.success(
                ParsedReceipt(
                    merchant = "Indomaret Point",
                    amount = 47500.0,
                    dateMillis = System.currentTimeMillis(),
                    category = "Makanan & Minuman",
                    type = "EXPENSE",
                    walletName = "E-Wallet",
                    note = "Kopi & Roti Sarapan (Deteksi Struk)",
                    itemsSummary = "1x Kopi Latte, 1x Roti Bakar",
                    confidence = "Otomatis (Offline Demo)"
                )
            )
        }

        // Compress bitmap to JPEG Base64
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64Data = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        val prompt = """
            Ekstrak data transaksi dari struk / nota / bukti transfer / receipt ini.
            Kembalikan HANYA format JSON valid tanpa tanda markdown (tanpa ```json dan ```):
            {
              "merchant": "Nama Toko / Restoran / Penerima / Layanan",
              "amount": 125000.0,
              "dateString": "YYYY-MM-DD",
              "category": "Kategori sesuai (Makanan & Minuman, Transportasi, Belanja & Kebutuhan, Tagihan & Utilitas, Hiburan & Rekreasi, Kesehatan, Pendidikan, Lainnya)",
              "type": "EXPENSE",
              "walletName": "Rekening Bank atau E-Wallet atau Tunai",
              "note": "Ringkasan item atau catatan transaksi"
            }
        """.trimIndent()

        val request = GeminiGenerateContentRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = "image/jpeg",
                                data = base64Data
                            )
                        )
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.2f
            )
        )

        try {
            val response = apiService.generateContent(
                model = GeminiChatModel.FLASH.modelId,
                apiKey = apiKey,
                request = request
            )

            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!replyText.isNullOrBlank()) {
                val cleanJson = replyText.replace("```json", "").replace("```", "").trim()
                val jsonObject = JSONObject(cleanJson)
                val merchant = jsonObject.optString("merchant", "Toko / Merchant")
                val amount = jsonObject.optDouble("amount", 0.0)
                val category = jsonObject.optString("category", "Makanan & Minuman")
                val type = jsonObject.optString("type", "EXPENSE")
                val wallet = jsonObject.optString("walletName", "Rekening Bank")
                val note = jsonObject.optString("note", "")

                var dateMillis = System.currentTimeMillis()
                val dateStr = jsonObject.optString("dateString", "")
                if (dateStr.isNotBlank()) {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val parsed = sdf.parse(dateStr)
                        if (parsed != null) dateMillis = parsed.time
                    } catch (_: Exception) {}
                }

                Result.success(
                    ParsedReceipt(
                        merchant = merchant,
                        amount = amount,
                        dateMillis = dateMillis,
                        category = category,
                        type = type,
                        walletName = wallet,
                        note = note,
                        confidence = "Tinggi (Gemini Vision AI)"
                    )
                )
            } else {
                Result.failure(Exception("Model tidak dapat membaca struk."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
