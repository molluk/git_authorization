package ru.molluk.git_authorization.utils

import androidx.lifecycle.ViewModel

open class DefaultViewModel: ViewModel() {

    fun parseDomainException(e: DomainException): String {
        return ru.molluk.git_authorization.utils.parseDomainException(e)
    }
}