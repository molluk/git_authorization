package ru.molluk.git_authorization.data.repository

import ru.molluk.git_authorization.data.model.dto.ReposResponse
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.remote.api.GitHubApiService
import javax.inject.Inject

class GitHubRepositoryImpl @Inject constructor(
    private val apiService: GitHubApiService
) : GitHubRepository {
    override suspend fun getUser(token: String?) = if (token.isNullOrEmpty()) {
        apiService.getUser()
    } else {
        apiService.getUser("Bearer $token")
    }

    override suspend fun getUserWithoutToken(name: String) = apiService.getUserWithoutToken(name)

    override suspend fun getRepos(page: Int?) = apiService.getRepos(page)

    override suspend fun getReposWithoutToken(name: String, page: Int?) =
        apiService.getReposWithoutToken(name, page)
}

interface GitHubRepository {
    suspend fun getUser(token: String? = null): UserResponse
    suspend fun getUserWithoutToken(name: String): UserResponse
    suspend fun getRepos(page: Int?): List<ReposResponse>
    suspend fun getReposWithoutToken(name: String, page: Int?): List<ReposResponse>
}