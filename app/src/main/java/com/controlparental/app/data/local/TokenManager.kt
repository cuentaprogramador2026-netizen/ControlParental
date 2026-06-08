package com.controlparental.app.data.local

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenManager de respaldo usando EncryptedSharedPreferences.
 * Útil para tokens de autenticación o fallback cuando DataStore no es suficiente.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "control_parental_secure_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(key: String, token: String) {
        prefs.edit().putString(key, token).apply()
    }

    fun getToken(key: String): String? = prefs.getString(key, null)

    fun saveBytes(key: String, data: ByteArray) {
        prefs.edit().putString(key, Base64.encodeToString(data, Base64.NO_WRAP)).apply()
    }

    fun getBytes(key: String): ByteArray? {
        return prefs.getString(key, null)?.let {
            try { Base64.decode(it, Base64.NO_WRAP) } catch (_: Exception) { null }
        }
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
