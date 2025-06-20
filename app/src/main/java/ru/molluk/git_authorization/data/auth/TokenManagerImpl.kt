package ru.molluk.git_authorization.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.molluk.git_authorization.data.local.dao.ProfileDao
import ru.molluk.git_authorization.data.local.entity.UserProfile
import androidx.core.content.edit

class TokenManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileDao: ProfileDao
) : TokenManager {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val currentProfileKey = "current_profile_id"

    override suspend fun saveProfile(profile: UserProfile) {
        profileDao.saveProfile(profile)
    }

    override fun getAllProfiles(): Flow<List<UserProfile>> {
        return profileDao.getAllProfilesFlow()
    }

    override suspend fun setActiveProfile(id: String) {
        profileDao.deactivateAllProfiles()
        profileDao.activateProfile(id)
        prefs.edit { putString(currentProfileKey, id) }
    }

    override suspend fun getActiveToken(): String? {
        val activeId = prefs.getString(currentProfileKey, null) ?: return null
        return profileDao.getProfile(activeId)?.accessToken
    }
}

interface TokenManager {
    suspend fun saveProfile(profile: UserProfile)
    fun getAllProfiles(): Flow<List<UserProfile>>
    suspend fun setActiveProfile(id: String)
    suspend fun getActiveToken(): String?
}