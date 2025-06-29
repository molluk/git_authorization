package ru.molluk.git_authorization.data.remote.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.molluk.git_authorization.utils.DomainException
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandlingInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            val response = chain.proceed(request)

            if (!response.isSuccessful) {
                val errorBodyString = response.body?.string()
                response.body?.close()

                throw when (response.code) {
                    401 -> DomainException.Unauthorized(specificMessage = "Пользователь не авторизован или токен недействителен", cause = IOException("HTTP 401"))
                    403 -> {
                        if (errorBodyString?.contains("API rate limit exceeded", ignoreCase = true) == true) {
                            DomainException.ApiRateLimitExceeded(cause = IOException("HTTP 403 Rate Limit"))
                        } else {
                            DomainException.Forbidden(details = errorBodyString ?: "Доступ запрещен", cause = IOException("HTTP 403 Forbidden"))
                        }
                    }
                    404 -> DomainException.NotFound(cause = IOException("HTTP 404 Not Found"))
                    in 500..599 -> DomainException.ServerError(response.code, cause = IOException("HTTP ${response.code}"))
                    else -> DomainException.Unknown("Необработанная HTTP ошибка: ${response.code}", cause = IOException("HTTP ${response.code}"))
                }
            }
            return response
        } catch (e: Exception) {
            if (e is DomainException) throw e

            throw when (e) {
                is UnknownHostException -> DomainException.NoInternet("Не удалось подключиться к серверу. Проверьте интернет-соединение.")
                is java.net.SocketTimeoutException -> DomainException.GenericNetworkError("Таймаут соединения. Пожалуйста, попробуйте позже.", e)
                is IOException -> DomainException.GenericNetworkError("Произошла ошибка сети.", e)
                else -> DomainException.Unknown("Произошла непредвиденная ошибка.", e)
            }
        }
    }
}