package ru.molluk.git_authorization.data.auth

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import ru.molluk.git_authorization.data.local.dao.ProfileDao
import androidx.core.content.edit
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import android.util.Base64

class TokenManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileDao: ProfileDao
) : TokenManager {

    private val appStatePrefs =
        context.getSharedPreferences("app_auth_state_prefs", Context.MODE_PRIVATE)

    @Volatile
    private var temporaryToken: String? = null


    companion object {
        const val KEY_CURRENT_PROFILE_ID = "current_profile_id"
        const val KEY_CURRENT_PROFILE_HAS_TOKEN = "current_profile_has_token"

        const val KEYSET_NAME = "master_token_keyset"
        const val PREFERENCE_FILE_NAME_FOR_KEYSET = "master_token_keyset_preferences"
        const val MASTER_KEY_URI = "android-keystore://_androidx_security_master_key_"
    }

    private val keysetHandle: KeysetHandle = try {
        AndroidKeysetManager.Builder()
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE_NAME_FOR_KEYSET)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
    } catch (e: Exception) {
        Log.e(
            this.javaClass.simpleName,
            "Не удалось инициализировать KeysetHandle",
            e
        )
        throw RuntimeException(
            "Критическая ошибка: не удалось инициализировать KeysetHandle (IOException)",
            e
        )
    }

    private val crypto: Aead = try {
        keysetHandle.getPrimitive(com.google.crypto.tink.RegistryConfiguration.get(), Aead::class.java)
    } catch (e: GeneralSecurityException) {
        Log.e(this.javaClass.simpleName, "Не удалось получить Aead primitive из KeysetHandle", e)
        throw RuntimeException("Критическая ошибка: не удалось получить Aead primitive", e)
    }

    override suspend fun setTemporaryToken(token: String?) {
        temporaryToken = token
    }

    override suspend fun getTemporaryToken() = temporaryToken

    override suspend fun setActiveProfile(id: String, hasToken: Boolean) {
        appStatePrefs.edit {
            putString(KEY_CURRENT_PROFILE_ID, id)
            putBoolean(KEY_CURRENT_PROFILE_HAS_TOKEN, hasToken)
        }
    }

    override suspend fun getActiveToken(): String? {
        val activeId = appStatePrefs.getString(KEY_CURRENT_PROFILE_ID, null) ?: return null
        val profileHasToken = appStatePrefs.getBoolean(KEY_CURRENT_PROFILE_HAS_TOKEN, false)

        if (!profileHasToken) return null

        return profileDao.getProfile(activeId)
            ?.accessToken
            ?.let { getDecryptedToken(it) }
    }

    override suspend fun clearActiveToken() {
        appStatePrefs.edit {
            remove(KEY_CURRENT_PROFILE_ID)
            remove(KEY_CURRENT_PROFILE_HAS_TOKEN)
        }
    }

    override suspend fun isActiveProfileAuthenticatable(): Boolean {
        return appStatePrefs.getBoolean(KEY_CURRENT_PROFILE_HAS_TOKEN, false)
    }

    override fun getEncryptedToken(token: String?): String? {
        if (token.isNullOrEmpty()) return null
        return try {
            val encryptedBytes = crypto.encrypt(token.toByteArray(StandardCharsets.UTF_8), null)
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Ошибка шифрования токена", e)
            null
        }
    }

    override fun getDecryptedToken(token: String?): String? {
        if (token.isNullOrEmpty()) {
            Log.w(this.javaClass.simpleName, "Попытка дешифровать пустой или null токен.")
            return null
        }

        return try {
            val encryptedBytesFromBase64 = Base64.decode(token, Base64.DEFAULT)
            val decryptedBytes = crypto.decrypt(encryptedBytesFromBase64, null)
            val decryptedString = String(decryptedBytes, StandardCharsets.UTF_8)
            decryptedString.trim().replace("\n", "").replace("\r", "")
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Ошибка при дешифровке токена.", e)
            null
        }
    }
}

interface TokenManager {
    suspend fun setActiveProfile(id: String, hasToken: Boolean)

    /**
     * Получает дешифрованный активный токен
     */
    suspend fun getActiveToken(): String?

    /**
     * Получаем зашифрованный токен
     */
    fun getEncryptedToken(token: String?): String?

    /**
     * Получаем дешифрованный токен
     */
    fun getDecryptedToken(token: String?): String?

    /**
     * Очистка
     */
    suspend fun clearActiveToken()

    /**
     * Профиль без токена
     */
    suspend fun isActiveProfileAuthenticatable(): Boolean

    /**
     * Временный токен для авторизации
     */
    suspend fun setTemporaryToken(token: String?)

    /**
     * Получить временный токен
     */
    suspend fun getTemporaryToken(): String?
}