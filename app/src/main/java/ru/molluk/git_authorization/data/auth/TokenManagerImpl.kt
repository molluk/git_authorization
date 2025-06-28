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
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import android.util.Base64

class TokenManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileDao: ProfileDao
) : TokenManager {

    private val appStatePrefs =
        context.getSharedPreferences("app_auth_state_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_CURRENT_PROFILE_ID = "current_profile_id"

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
    } catch (e: GeneralSecurityException) {
        Log.e(
            this.javaClass.simpleName,
            "Не удалось инициализировать KeysetHandle с AndroidKeysetManager",
            e
        )
        throw RuntimeException(
            "Критическая ошибка: не удалось инициализировать KeysetHandle для шифрования",
            e
        )
    } catch (e: IOException) {
        Log.e(
            this.javaClass.simpleName,
            "Не удалось инициализировать KeysetHandle (IOException)",
            e
        )
        throw RuntimeException(
            "Критическая ошибка: не удалось инициализировать KeysetHandle (IOException)",
            e
        )
    }

    private val crypto: Aead = try {
        keysetHandle.getPrimitive(Aead::class.java)
    } catch (e: GeneralSecurityException) {
        Log.e(this.javaClass.simpleName, "Не удалось получить Aead primitive из KeysetHandle", e)
        throw RuntimeException("Критическая ошибка: не удалось получить Aead primitive", e)
    }

    override suspend fun setActiveProfile(id: String) {
        appStatePrefs.edit { putString(KEY_CURRENT_PROFILE_ID, id) }
    }

    override suspend fun getActiveToken(): String? {
        val activeId = appStatePrefs.getString(KEY_CURRENT_PROFILE_ID, null) ?: return null
        val activeProfile = profileDao.getProfile(activeId) ?: return null
        val encryptedTokenBase64 = activeProfile.accessToken ?: return null
        return getDecryptedToken(encryptedTokenBase64)
    }

    override fun getEncryptedToken(token: String?): String {
        if (token.isNullOrEmpty()) return ""
        return try {
            val encryptedBytes = crypto.encrypt(token.toByteArray(StandardCharsets.UTF_8), null)
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Ошибка шифрования токена", e)
            ""
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
        } catch (e: GeneralSecurityException) {
            Log.e(
                this.javaClass.simpleName,
                "Ошибка дешифровки токена ${e.javaClass.simpleName}. Возможно, ключ изменился или данные повреждены.",
                e
            )
            null
        } catch (e: IllegalArgumentException) {
            Log.e(
                this.javaClass.simpleName,
                "Ошибка дешифровки токена ${e.javaClass.simpleName}. Возможно, неверный формат Base64.",
                e
            )
            null
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Ошибка при дешифровке токена.", e)
            null
        }
    }
}

interface TokenManager {
    suspend fun setActiveProfile(id: String)

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
}