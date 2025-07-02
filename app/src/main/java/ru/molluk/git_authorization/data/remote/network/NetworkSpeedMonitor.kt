package ru.molluk.git_authorization.data.remote.network

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Монитор скорости сети на основе измерения времени ответов от OkHttp.
 */
class NetworkSpeedMonitor : Interceptor {

    companion object {
        private const val SAMPLE_SIZE = 10
    }

    private val recentSpeeds = ArrayDeque<Double>()
    private val _speedKbps = MutableStateFlow<Double>(-1.0)
    val speedKbps: StateFlow<Double> get() = _speedKbps

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()
        val response: Response

        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            throw e
        }

        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000.0 // мс

        Log.d(this.javaClass.simpleName, "${request.method} ${request.url} занял ${durationMs}ms")

        val contentLength = response.body?.contentLength()?.takeIf { it > 0 } ?: 1L
        val speedKBps = (contentLength.toDouble() / 1024.0) / (durationMs / 1000.0) // KB/s

        Log.d(this.javaClass.simpleName, "${request.method} ${request.url} скорость ${"%.4f".format(speedKBps)} KB/s")

        synchronized(this) {
            if (recentSpeeds.size >= SAMPLE_SIZE) recentSpeeds.removeFirst()
            recentSpeeds.addLast(speedKBps)
            val averageSpeed = recentSpeeds.average()
            _speedKbps.value = averageSpeed
        }

        return response
    }
}
