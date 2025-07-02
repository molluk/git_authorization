package ru.molluk.git_authorization.data.repository

import ru.molluk.git_authorization.data.model.dto.ReposResponse
import ru.molluk.git_authorization.data.model.dto.UserResponse
import ru.molluk.git_authorization.data.remote.api.GitHubApiService
import javax.inject.Inject

class GitHubRepositoryImpl @Inject constructor(
    private val apiService: GitHubApiService
) : GitHubRepository {

    override suspend fun getUserDataForToken(): UserResponse {
        return apiService.getUserAuthorized()
    }

    override suspend fun getCurrentAuthenticatedUser(): UserResponse {
        return apiService.getUserAuthorized()
    }

    override suspend fun getCurrentAuthenticatedUserRepos(page: Int?): List<ReposResponse> {
        return apiService.getReposAuthorized(page = page)
    }

    override suspend fun getUserByUsername(username: String): UserResponse {
        return apiService.getUserByUsername(username)
    }

    override suspend fun getUserReposByUsername(username: String, page: Int?): List<ReposResponse> {
        return apiService.getUserReposByUsername(username, page)
    }
}

interface GitHubRepository {
    suspend fun getUserDataForToken(): UserResponse
    suspend fun getCurrentAuthenticatedUser(): UserResponse
    suspend fun getCurrentAuthenticatedUserRepos(page: Int?): List<ReposResponse>
    suspend fun getUserByUsername(username: String): UserResponse
    suspend fun getUserReposByUsername(username: String, page: Int?): List<ReposResponse>
}