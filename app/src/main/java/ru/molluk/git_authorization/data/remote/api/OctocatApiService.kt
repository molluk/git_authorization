package ru.molluk.git_authorization.data.remote.api

import retrofit2.http.GET

interface OctocatApiService {

    @GET("octocat")
    suspend fun getOctocat(): String
}