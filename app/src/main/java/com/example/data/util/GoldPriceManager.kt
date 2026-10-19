package com.example.data.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GoldPriceManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _currentPrice = MutableStateFlow(
        prefs.getFloat(KEY_PRICE, DEFAULT_GOLD_PRICE.toFloat()).toDouble()
    )
    val currentPrice: StateFlow<Double> = _currentPrice.asStateFlow()

    private val _previousPrice = MutableStateFlow(
        prefs.getFloat(KEY_PREVIOUS_PRICE, (DEFAULT_GOLD_PRICE - 15_000.0).toFloat()).toDouble()
    )
    val previousPrice: StateFlow<Double> = _previousPrice.asStateFlow()

    private val _priceChange = MutableStateFlow(
        _currentPrice.value - _previousPrice.value
    )
    val priceChange: StateFlow<Double> = _priceChange.asStateFlow()

    private val _priceChangePercent = MutableStateFlow(
        if (_previousPrice.value > 0) ((_currentPrice.value - _previousPrice.value) / _previousPrice.value) * 100.0 else 0.0
    )
    val priceChangePercent: StateFlow<Double> = _priceChangePercent.asStateFlow()

    private val _lastUpdated = MutableStateFlow(
        prefs.getString(KEY_DATE, getNowDateTimeFormatted()) ?: getNowDateTimeFormatted()
    )
    val lastUpdated: StateFlow<String> = _lastUpdated.asStateFlow()

    private val _sourceName = MutableStateFlow(
        prefs.getString(KEY_SOURCE, "Logam Mulia (Antam)") ?: "Logam Mulia (Antam)"
    )
    val sourceName: StateFlow<String> = _sourceName.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    companion object {
        private const val PREFS_NAME = "dompetku_gold_prefs"
        private const val KEY_PRICE = "key_gold_price_per_gram"
        private const val KEY_PREVIOUS_PRICE = "key_gold_previous_price"
        private const val KEY_DATE = "key_gold_last_updated"
        private const val KEY_SOURCE = "key_gold_source"
        const val DEFAULT_GOLD_PRICE = 2_580_000.0 // Acuan harga Antam / Logam Mulia Indonesia

        @Volatile
        private var INSTANCE: GoldPriceManager? = null

        fun getInstance(context: Context): GoldPriceManager {
            return INSTANCE ?: synchronized(this) {
                val instance = GoldPriceManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        fun getNowDateTimeFormatted(): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
            return sdf.format(Date()) + " WIB"
        }
    }

    suspend fun refreshGoldPrice(): Boolean = withContext(Dispatchers.IO) {
        _isRefreshing.value = true
        try {
            // Primary source: Logam Mulia (Antam)
            val primarySuccess = fetchFromEndpoint("https://logam-mulia-api.iamutaki.workers.dev/api/prices/logammulia")
            if (primarySuccess) {
                _isRefreshing.value = false
                return@withContext true
            }

            // Fallback source: HargaEmas.org
            val fallbackSuccess = fetchFromEndpoint("https://logam-mulia-api.iamutaki.workers.dev/api/prices/hargaemas-org")
            if (fallbackSuccess) {
                _isRefreshing.value = false
                return@withContext true
            }
        } catch (_: Exception) {
            // Ignore network exceptions, keep cached price
        } finally {
            _isRefreshing.value = false
        }
        return@withContext false
    }

    private fun fetchFromEndpoint(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "DompetKu-Android/1.0")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return false

            val bodyString = response.body?.string() ?: return false
            val json = JSONObject(bodyString)
            if (!json.optBoolean("success", true)) return false

            val dataArray = json.optJSONArray("data") ?: return false
            var bestPrice = 0.0
            var recordedDate = ""
            var displayName = "Logam Mulia (Antam)"

            for (i in 0 until dataArray.length()) {
                val item = dataArray.optJSONObject(i) ?: continue
                val material = item.optString("material", "")
                val weight = item.optDouble("weight", 0.0)
                val unit = item.optString("weightUnit", "")
                val sellPrice = item.optDouble("sellPrice", 0.0)

                if (material.equals("gold", ignoreCase = true) && sellPrice > 0) {
                    if (weight == 1.0 && (unit.equals("gr", ignoreCase = true) || unit.equals("gram", ignoreCase = true))) {
                        bestPrice = sellPrice
                        recordedDate = item.optString("recordedDate", "")
                        displayName = item.optString("displayName", "Logam Mulia (Antam)")
                        break
                    } else if (weight == 0.5 && bestPrice == 0.0) {
                        // Estimate 1 gram from 0.5 gram
                        bestPrice = sellPrice * 2.0
                        recordedDate = item.optString("recordedDate", "")
                        displayName = item.optString("displayName", "Logam Mulia (Antam)")
                    }
                }
            }

            if (bestPrice > 500_000.0) { // Valid reasonable Indonesian gold price per gram
                savePrice(bestPrice, recordedDate, displayName)
                return true
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    fun setCustomPrice(price: Double, customSource: String = "Acuan Kustom (Pengguna)") {
        if (price > 100_000.0) {
            savePrice(price, "", customSource)
        }
    }

    private fun savePrice(newPrice: Double, recordedDate: String, source: String) {
        val oldPrice = _currentPrice.value
        val prevPrice = if (oldPrice > 0 && oldPrice != newPrice) oldPrice else _previousPrice.value

        val displayDate = if (recordedDate.isNotBlank()) {
            try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsed = inputSdf.parse(recordedDate)
                if (parsed != null) {
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(parsed)
                    val timeStr = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID")).format(Date())
                    "$dateStr, $timeStr WIB"
                } else recordedDate
            } catch (_: Exception) {
                getNowDateTimeFormatted()
            }
        } else getNowDateTimeFormatted()

        prefs.edit()
            .putFloat(KEY_PRICE, newPrice.toFloat())
            .putFloat(KEY_PREVIOUS_PRICE, prevPrice.toFloat())
            .putString(KEY_DATE, displayDate)
            .putString(KEY_SOURCE, source)
            .apply()

        _previousPrice.value = prevPrice
        _currentPrice.value = newPrice
        _priceChange.value = newPrice - prevPrice
        _priceChangePercent.value = if (prevPrice > 0) ((newPrice - prevPrice) / prevPrice) * 100.0 else 0.0
        _lastUpdated.value = displayDate
        _sourceName.value = source
    }
}
