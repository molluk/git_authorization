package ru.molluk.git_authorization.domain.usecase.home

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.molluk.git_authorization.data.remote.network.NetworkSpeedMonitor
import javax.inject.Inject

class NetworkSpeedUseCase @Inject constructor(
    private val networkSpeedMonitor: NetworkSpeedMonitor
) {
    operator fun invoke(): Flow<String> {
        return networkSpeedMonitor.speedKbps.map { kbps ->
            when {
                kbps < 0 -> "Н/д"
                kbps > 1000 -> "~${"%.4f".format(kbps / 1024f)} МБ/с"
                else -> "~${"%.4f".format(kbps)} КБ/с"
            }
        }
    }
}