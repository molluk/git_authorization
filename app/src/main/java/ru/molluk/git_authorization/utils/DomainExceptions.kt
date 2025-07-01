package ru.molluk.git_authorization.utils

import java.io.IOException

sealed class DomainException(override val message: String?, override val cause: Throwable? = null) :
    IOException(message, cause) {

    /**
     * Ошибка, связанная с отсутствием или проблемами интернет-соединения.
     */
    data class NoInternet(val customMessage: String = "Нет подключения к интернету. Проверьте соединение.") :
        DomainException(customMessage)

    /**
     * Ошибка авторизации (например, HTTP 401).
     * @param specificMessage Дополнительное сообщение, возможно, от сервера.
     */
    data class Unauthorized(
        val specificMessage: String? = null,
        override val cause: Throwable? = null
    ) :
        DomainException(specificMessage ?: "Ошибка авторизации. Пожалуйста, войдите снова.", cause)

    /**
     * Доступ запрещен (например, HTTP 403 - нет прав).
     * @param details Детали ошибки, возможно, от сервера.
     */
    data class Forbidden(val details: String? = null, override val cause: Throwable? = null) :
        DomainException(details ?: "Доступ запрещен.", cause)

    /**
     * Превышен лимит запросов к API (часто HTTP 403 с определенным телом).
     * @param retryAfterSeconds Информация о том, когда можно попробовать снова (если доступна).
     */
    data class ApiRateLimitExceeded(
        val retryAfterSeconds: Int? = null,
        override val cause: Throwable? = null
    ) :
        DomainException(
            "Превышен лимит запросов к API.${if (retryAfterSeconds != null) " Попробуйте через $retryAfterSeconds сек." else ""}",
            cause
        )

    /**
     * Запрошенный ресурс не найден (например, HTTP 404).
     */
    data class NotFound(val resourceName: String? = null, override val cause: Throwable? = null) :
        DomainException(
            if (resourceName != null) "Ресурс '$resourceName' не найден." else "Запрошенный ресурс не найден.",
            cause
        )

    /**
     * Ошибка на стороне сервера (например, HTTP 5xx).
     * @param statusCode HTTP-статус код ошибки.
     */
    data class ServerError(val statusCode: Int, override val cause: Throwable? = null) :
        DomainException(
            "Ошибка на сервере (код: $statusCode). Пожалуйста, попробуйте позже.",
            cause
        )

    /**
     * Общая ошибка сети, не классифицированная выше (например, таймауты, проблемы с SSL и т.д.).
     */
    data class GenericNetworkError(
        val customMessage: String = "Произошла сетевая ошибка. Пожалуйста, попробуйте позже.",
        override val cause: Throwable? = null
    ) :
        DomainException(customMessage, cause)

    /**
     * Неизвестная или непредвиденная ошибка.
     */
    data class Unknown(
        val customMessage: String = "Произошла неизвестная ошибка.",
        override val cause: Throwable? = null
    ) :
        DomainException(customMessage, cause)
}