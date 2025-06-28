package ru.molluk.git_authorization.utils

import android.content.Context
import android.util.Patterns
import android.view.View
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