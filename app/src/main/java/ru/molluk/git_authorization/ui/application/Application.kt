package ru.molluk.git_authorization.ui.application

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.google.crypto.tink.aead.AeadConfig
import dagger.hilt.android.HiltAndroidApp
import ru.molluk.git_authorization.data.auth.TokenManagerImpl
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.KeyGenerator

@HiltAndroidApp
class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias(TokenManagerImpl.MASTER_KEY_URI)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
                )
                val builder = KeyGenParameterSpec.Builder(
                    TokenManagerImpl.MASTER_KEY_URI,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                // .setUserAuthenticationRequired(true) // требовать блокировку экрана
                // .setUserAuthenticationValidityDurationSeconds(10) // требовать аутентификацию пользователя

                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
                Log.i(this.javaClass.simpleName, "Ключ '${TokenManagerImpl.MASTER_KEY_URI}' сгенерирован в AndroidKeyStore.")
            } else {
                Log.i(this.javaClass.simpleName, "Ключ '${TokenManagerImpl.MASTER_KEY_URI}' уже существует в AndroidKeyStore.")
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Ошибка работы с AndroidKeyStore для ключа '${TokenManagerImpl.MASTER_KEY_URI}'", e)
        }
        try {
            AeadConfig.register()
            Log.i(this.javaClass.simpleName, "Tink AeadConfig зарегестрирован.")
        } catch (e: GeneralSecurityException) {
            Log.e(this.javaClass.simpleName, "Ошибка регистрации Tink AeadConfig", e)
        }
    }
}