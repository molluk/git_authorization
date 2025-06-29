package ru.molluk.git_authorization.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import ru.molluk.git_authorization.data.model.dto.ReposResponse
import ru.molluk.git_authorization.data.model.dto.UserResponse

interface GitHubApiService {
    @GET("user")
    suspend fun getUser(): UserResponse

    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): UserResponse

    @GET("users/{username}")
    suspend fun getUserWithoutToken(@Path("username") username: String): UserResponse

    @GET("/user/repos")
    suspend fun getRepos(@Query("page") page: Int? = null): List<ReposResponse>

    @GET("/users/{username}/repos")
    suspend fun getReposWithoutToken(@Path("username") username: String, @Query("page") page: Int? = null): List<ReposResponse>
}