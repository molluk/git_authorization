package ru.molluk.git_authorization.data.remote.network.retrofit

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.remote.api.GitHubApiService
import ru.molluk.git_authorization.data.remote.api.OctocatApiService
import ru.molluk.git_authorization.data.remote.network.NetworkSpeedMonitor
import ru.molluk.git_authorization.data.remote.network.interceptors.AuthInterceptor
import ru.molluk.git_authorization.data.remote.network.interceptors.ErrorHandlingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.github.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorHandlingInterceptor,
        speedMonitor: NetworkSpeedMonitor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(speedMonitor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideOctocatApiService(retrofit: Retrofit): OctocatApiService {
        return retrofit.create(OctocatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGitHubApiService(retrofit: Retrofit): GitHubApiService {
        return retrofit.create(GitHubApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkSpeedMonitor(): NetworkSpeedMonitor = NetworkSpeedMonitor()
}