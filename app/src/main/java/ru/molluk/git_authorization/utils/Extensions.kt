package ru.molluk.git_authorization.utils

import android.util.Patterns
import android.view.View

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