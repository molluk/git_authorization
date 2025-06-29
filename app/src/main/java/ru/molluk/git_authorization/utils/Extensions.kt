package ru.molluk.git_authorization.utils

import android.content.Context
import android.util.Patterns
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

fun isValidEmail(email: String?): Boolean {
    return if (email.isNullOrEmpty()) {
        false
    } else {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

fun String.formatToDateYMD(): String {
    val instant = Instant.parse(this)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return formatter.format(instant.atZone(java.time.ZoneId.systemDefault()))
}

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

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