package ru.molluk.git_authorization.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.molluk.git_authorization.data.model.dto.ReposResponse
import ru.molluk.git_authorization.data.model.dto.UserResponse

interface GitHubApiService {

    /**
     * Для аутентифицированного пользователя (эндпоинт /user)
     */
    @GET("user")
    suspend fun getUserAuthorized(): UserResponse

    /**
     * Данные любого пользователя по username (эндпоинт /users/{username})
     */
    @GET("users/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): UserResponse

    /**
     * Репозитории аутентифицированного пользователя (эндпоинт /user/repos)
     */
    @GET("/user/repos")
    suspend fun getReposAuthorized(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int = 30
    ): List<ReposResponse>

    /**
     * Репозитории любого пользователя по username (эндпоинт /users/{username}/repos)
     */
    @GET("/users/{username}/repos")
    suspend fun getUserReposByUsername(
        @Path("username") username: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int = 30
    ): List<ReposResponse>
}