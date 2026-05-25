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

    // Selected provider
    fun getSelectedProvider(): LlmProvider {
        val name = prefs.getString(KEY_SELECTED_PROVIDER, null)
        return try {
            name?.let { LlmProvider.valueOf(it) } ?: LlmProvider.ANTHROPIC
        } catch (_: Exception) {
            LlmProvider.ANTHROPIC
        }
    }

    fun setSelectedProvider(provider: LlmProvider) {
        prefs.edit().putString(KEY_SELECTED_PROVIDER, provider.name).apply()
    }

    // Per-provider API keys
    fun getApiKey(provider: LlmProvider = getSelectedProvider()): String? =
        prefs.getString(keyFor(provider), null)

    fun setApiKey(key: String, provider: LlmProvider = getSelectedProvider()) {
        prefs.edit().putString(keyFor(provider), key).apply()
    }

    fun clearApiKey(provider: LlmProvider = getSelectedProvider()) {
        prefs.edit().remove(keyFor(provider)).apply()
    }

    fun hasApiKey(provider: LlmProvider = getSelectedProvider()): Boolean =
        !getApiKey(provider).isNullOrBlank()

    fun hasAnyApiKey(): Boolean =
        LlmProvider.entries.any { hasApiKey(it) }

    fun getConfiguredProviders(): List<LlmProvider> =
        LlmProvider.entries.filter { hasApiKey(it) }

    private fun keyFor(provider: LlmProvider): String = "api_key_${provider.name}"

    companion object {
        private const val KEY_SELECTED_PROVIDER = "selected_provider"
    }
}
