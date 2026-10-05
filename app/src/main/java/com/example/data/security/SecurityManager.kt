package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SecurityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "dompetku_security_prefs"
        private const val KEY_LOCK_ENABLED = "is_lock_enabled"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_AMOUNT_HIDDEN = "is_amount_hidden"
        private const val SALT = "DompetKu_Fintech_Salt_2026"

        @Volatile
        private var INSTANCE: SecurityManager? = null

        fun getInstance(context: Context): SecurityManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SecurityManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        fun hashPin(pin: String): String {
            val input = "$pin$SALT"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }

    var isLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ENABLED, false)
        private set(value) = prefs.edit().putBoolean(KEY_LOCK_ENABLED, value).apply()

    var isAmountHidden: Boolean
        get() = prefs.getBoolean(KEY_AMOUNT_HIDDEN, false)
        set(value) = prefs.edit().putBoolean(KEY_AMOUNT_HIDDEN, value).apply()

    private var pinHash: String?
        get() = prefs.getString(KEY_PIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    fun hasPinSet(): Boolean {
        return !pinHash.isNullOrBlank() && isLockEnabled
    }

    fun setPin(newPin: String) {
        pinHash = hashPin(newPin)
        isLockEnabled = true
    }

    fun verifyPin(enteredPin: String): Boolean {
        val currentHash = pinHash ?: return false
        return hashPin(enteredPin) == currentHash
    }

    fun disableLock(currentPin: String): Boolean {
        if (verifyPin(currentPin)) {
            isLockEnabled = false
            pinHash = null
            return true
        }
        return false
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (verifyPin(oldPin)) {
            setPin(newPin)
            return true
        }
        return false
    }
}
