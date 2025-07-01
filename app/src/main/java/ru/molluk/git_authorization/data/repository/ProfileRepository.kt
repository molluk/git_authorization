package ru.molluk.git_authorization.data.repository

import ru.molluk.git_authorization.data.local.dao.ProfileDao
import ru.molluk.git_authorization.data.local.entity.UserProfile
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    suspend fun getActiveProfile(): UserProfile? {
        return profileDao.getAllProfiles().find { it.isActive }
    }

    suspend fun setActiveProfile(profile: UserProfile) {
        profileDao.setProfileAsActive(profile.id)
    }

    suspend fun getAllProfiles(): List<UserProfile> {
        return profileDao.getAllProfiles()
    }

    suspend fun saveProfile(profile: UserProfile) {
        profileDao.saveProfile(profile)
    }

    suspend fun deactivateAllProfiles() {
        profileDao.deactivateAllProfiles()
    }

    suspend fun deleteProfile(profile: UserProfile) {
        profileDao.deleteProfile(profile)
    }
}