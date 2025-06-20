package ru.molluk.git_authorization.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.molluk.git_authorization.data.local.entity.UserProfile

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfile(id: String): UserProfile?

    @Query("SELECT * FROM user_profiles")
    suspend fun getAllProfiles(): List<UserProfile>

    @Query("SELECT * FROM user_profiles")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    @Query("UPDATE user_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE user_profiles SET isActive = 1 WHERE id = :id")
    suspend fun activateProfile(id: String)
}