package ru.molluk.git_authorization.data.repository

import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.remote.api.GitHubApiService
import javax.inject.Inject

class GitHubRepositoryImpl @Inject constructor(
    private val apiService: GitHubApiService
): GitHubRepository {
    override suspend fun getUser(token: String) = apiService.getUser(token)

    override suspend fun getUserWithoutToken(name: String) = apiService.getUserWithoutToken(name)
}

interface GitHubRepository {
    suspend fun getUser(token: String): UserResponse
    suspend fun getUserWithoutToken(name: String): UserResponse
}