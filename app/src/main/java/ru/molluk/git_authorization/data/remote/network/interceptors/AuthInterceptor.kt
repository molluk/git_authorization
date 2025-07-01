package ru.molluk.git_authorization.data.remote.network.interceptors

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.molluk.git_authorization.data.auth.TokenManager

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        runBlocking {
            val token = tokenManager.getActiveToken() ?: tokenManager.getTemporaryToken()
            token?.let {
                builder.addHeader("Authorization", "Bearer $it")
            }
        }

        return chain.proceed(builder.build())
    }
}