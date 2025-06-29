package ru.molluk.git_authorization.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.molluk.git_authorization.data.auth.TokenManager
import ru.molluk.git_authorization.data.auth.TokenManagerImpl
import ru.molluk.git_authorization.data.local.db.AppDatabase
import ru.molluk.git_authorization.data.local.dao.ProfileDao
import ru.molluk.git_authorization.data.remote.api.GitHubApiService
import ru.molluk.git_authorization.data.remote.api.OctocatApiService
import ru.molluk.git_authorization.data.repository.GitHubRepository
import ru.molluk.git_authorization.data.repository.GitHubRepositoryImpl
import ru.molluk.git_authorization.data.repository.OctocatRepository
import ru.molluk.git_authorization.data.repository.OctocatRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(db: AppDatabase): ProfileDao {
        return db.profileDao()
    }

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context, profileDao: ProfileDao): TokenManager {
        return TokenManagerImpl(context, profileDao)
    }

    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideOctocatRepository(apiService: OctocatApiService): OctocatRepository {
        return OctocatRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideGitHubGitHubRepository(apiService: GitHubApiService): GitHubRepository {
        return GitHubRepositoryImpl(apiService)
    }
}