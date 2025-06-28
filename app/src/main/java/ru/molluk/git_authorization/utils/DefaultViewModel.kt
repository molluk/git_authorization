package ru.molluk.git_authorization.utils

import androidx.lifecycle.ViewModel

open class DefaultViewModel: ViewModel() {

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

}