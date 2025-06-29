package ru.molluk.git_authorization.ui.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import com.google.crypto.tink.aead.AeadConfig
import dagger.hilt.android.HiltAndroidApp
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.data.auth.TokenManagerImpl
import ru.molluk.git_authorization.utils.NetworkMonitor
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private var previousNetworkState: Boolean? = null

    private val networkStateObserver = Observer<Boolean> { isConnected ->
        val currentState = isConnected
        val previousState = previousNetworkState

        if (previousState != null) {
            if (previousState == true && currentState == false) {
                showNetworkLostNotification()
            } else if (previousState == false && currentState == true) {
                hideNetworkLostNotification()
                showNetworkRestoredNotification()
            }
        } else {
            if (currentState == false) {
                showNetworkLostNotification()
            } else {
                hideNetworkLostNotification()
                hideNetworkRestoredNotification()
            }
        }
        previousNetworkState = currentState
    }

    override fun onCreate() {
        super.onCreate()

        initKeyStoreAndTink()

        createNotificationChannel()
        startGlobalNetworkMonitoring()
    }

    private fun initKeyStoreAndTink() {
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
                Log.i(
                    this.javaClass.simpleName,
                    "Ключ '${TokenManagerImpl.MASTER_KEY_URI}' сгенерирован в AndroidKeyStore."
                )
            } else {
                Log.i(
                    this.javaClass.simpleName,
                    "Ключ '${TokenManagerImpl.MASTER_KEY_URI}' уже существует в AndroidKeyStore."
                )
            }
        } catch (e: Exception) {
            Log.e(
                this.javaClass.simpleName,
                "Ошибка работы с AndroidKeyStore для ключа '${TokenManagerImpl.MASTER_KEY_URI}'",
                e
            )
        }
        try {
            AeadConfig.register()
            Log.i(this.javaClass.simpleName, "Tink AeadConfig зарегестрирован.")
        } catch (e: GeneralSecurityException) {
            Log.e(this.javaClass.simpleName, "Ошибка регистрации Tink AeadConfig", e)
        }
    }

    private fun startGlobalNetworkMonitoring() {
        networkMonitor.observeForever(networkStateObserver)
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.notification_channel_name_network)
        val descriptionText = getString(R.string.notification_channel_description_network)

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NETWORK_STATUS_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNetworkLostNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        hideNetworkRestoredNotification()

        val builder = NotificationCompat.Builder(this, NETWORK_STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_no_internet)
            .setContentTitle(getString(R.string.notification_title_network_lost))
            .setContentText(getString(R.string.notification_text_network_lost))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setAutoCancel(true)


        with(NotificationManagerCompat.from(this)) {
            notify(NETWORK_LOST_NOTIFICATION_ID, builder.build())
        }
    }

    private fun showNetworkRestoredNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        hideNetworkLostNotification()

        val builder = NotificationCompat.Builder(this, NETWORK_STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_internet_restored)
            .setContentTitle(getString(R.string.notification_title_network_restored))
            .setContentText(getString(R.string.notification_text_network_restored))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        // .setTimeoutAfter(5000)

        with(NotificationManagerCompat.from(this)) {
            notify(NETWORK_RESTORED_NOTIFICATION_ID, builder.build())
        }
    }

    private fun hideNetworkLostNotification() {
        with(NotificationManagerCompat.from(this)) {
            cancel(NETWORK_LOST_NOTIFICATION_ID)
        }
    }

    private fun hideNetworkRestoredNotification() {
        with(NotificationManagerCompat.from(this)) {
            cancel(NETWORK_RESTORED_NOTIFICATION_ID)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.removeObserver(networkStateObserver)
    }

    companion object {
        const val NETWORK_STATUS_CHANNEL_ID = "network_status_channel"
        const val NETWORK_LOST_NOTIFICATION_ID = 1001
        const val NETWORK_RESTORED_NOTIFICATION_ID = 1002
    }
}