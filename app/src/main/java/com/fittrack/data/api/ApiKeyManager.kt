package com.fittrack.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ApiKeyManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "fittrack_api_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(): String? = prefs.getString(KEY_API_KEY, null)

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    fun hasApiKey(): Boolean = !getApiKey().isNullOrBlank()

    companion object {
        private const val KEY_API_KEY = "claude_api_key"
    }
}
