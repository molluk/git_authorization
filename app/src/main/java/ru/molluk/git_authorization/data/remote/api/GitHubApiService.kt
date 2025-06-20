package ru.molluk.git_authorization.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import ru.molluk.git_authorization.data.model.dto.UserResponse

interface GitHubApiService {
    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): UserResponse

    @GET("users/{username}")
    suspend fun getUserWithoutToken(@Path("username") username: String): UserResponse
}