package ru.molluk.git_authorization.utils

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
import ru.molluk.git_authorization.R
import java.time.Instant
import java.time.format.DateTimeFormatter

fun View.fadeVisibility(
    targetVisibility: Int,
    duration: Long = 500L,
    endAlpha: Float = if (targetVisibility == View.VISIBLE) 1f else 0f,
    onEnd: (() -> Unit)? = null
) {
    if (targetVisibility == View.VISIBLE) {
        this.alpha = 0f
        this.visibility = View.VISIBLE
        this.animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction {
                onEnd?.invoke()
            }
    } else {
        this.animate()
            .alpha(endAlpha)
            .setDuration(duration)
            .withEndAction {
                this.visibility = targetVisibility
                onEnd?.invoke()
            }
    }
}

fun String.formatToDateYMD(): String {
    val instant = Instant.parse(this)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return formatter.format(instant.atZone(java.time.ZoneId.systemDefault()))
}

inline fun <reified T : Parcelable> Bundle.getParcelableFromBundle(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelable(key) as? T
    }
}

fun AppCompatActivity.observeNetworkStatusAndShowSnackbar(
    networkStateSource: LiveData<Boolean>,
    rootView: View,
    lifecycleOwner: LifecycleOwner = this
) {
    var currentSnackbar: Snackbar? = null
    var previousNetworkStateForSnackbar: Boolean? = null

    networkStateSource.observe(lifecycleOwner) { isConnected ->
        val currentState = isConnected
        val previousState = previousNetworkStateForSnackbar

        if (previousState != null) {
            if (previousState == true && currentState == false) {
                currentSnackbar?.dismiss()
                currentSnackbar = Snackbar.make(rootView, this.getString(R.string.notification_title_network_lost),
                    Snackbar.LENGTH_INDEFINITE).apply {
                    setBackgroundTint(ContextCompat.getColor(rootView.context, R.color.error_default))
                    setTextColor(ContextCompat.getColor(rootView.context, R.color.white))
                    setAction("СКРЫТЬ") {}
                }
                currentSnackbar?.show()
            } else if (previousState == false && currentState == true) {
                currentSnackbar?.dismiss()
                currentSnackbar = Snackbar.make(rootView, this.getString(R.string.notification_title_network_restored),
                    Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(rootView.context, R.color.element_active))
                    setTextColor(ContextCompat.getColor(rootView.context, R.color.white))
                }
                currentSnackbar?.show()
            }
        } else {
            if (currentState == false) {
                currentSnackbar?.dismiss()
                currentSnackbar = Snackbar.make(rootView, this.getString(R.string.notification_title_network_lost),
                    Snackbar.LENGTH_INDEFINITE).apply {
                    setBackgroundTint(ContextCompat.getColor(rootView.context, R.color.error_default))
                    setTextColor(ContextCompat.getColor(rootView.context, R.color.white))
                    setAction("СКРЫТЬ") {}
                }
                currentSnackbar.show()
            } else {
                currentSnackbar?.dismiss()
            }
        }
        previousNetworkStateForSnackbar = currentState
    }
}

fun parseDomainException(e: DomainException): String {
    return when (e) {
        is DomainException.NoInternet -> e.customMessage
        is DomainException.Unauthorized -> e.message ?: "Ошибка авторизации."
        is DomainException.Forbidden -> e.message ?: "Доступ запрещен."
        is DomainException.ApiRateLimitExceeded -> e.message ?: "Превышен лимит запросов."
        is DomainException.NotFound -> e.message ?: "Не найдено."
        is DomainException.ServerError -> e.message ?: "Ошибка сервера."
        is DomainException.GenericNetworkError -> e.customMessage
        is DomainException.Unknown -> e.customMessage
    }
}