package ru.molluk.git_authorization.data.repository

import ru.molluk.git_authorization.data.remote.api.OctocatApiService
import javax.inject.Inject

class OctocatRepositoryImpl @Inject constructor(
    private val apiService: OctocatApiService
): OctocatRepository {
    override suspend fun getOctocat(): String {
        return apiService.getOctocat()
    }
}

interface OctocatRepository {
    suspend fun getOctocat(): String
}