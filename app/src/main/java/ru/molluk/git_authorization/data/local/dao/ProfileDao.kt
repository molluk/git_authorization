package ru.molluk.git_authorization.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ru.molluk.git_authorization.data.local.entity.UserProfile

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfile(id: String): UserProfile?

    @Query("SELECT * FROM user_profiles ORDER BY isActive DESC, login ASC")
    suspend fun getAllProfiles(): List<UserProfile>

    @Query("UPDATE user_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE user_profiles SET isActive = 1 WHERE id = :id")
    suspend fun activateProfile(id: String)

    @Transaction
    suspend fun setProfileAsActive(id: String) {
        deactivateAllProfiles()
        activateProfile(id)
    }

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String): Int

    @Delete
    suspend fun deleteProfile(profile: UserProfile)
}