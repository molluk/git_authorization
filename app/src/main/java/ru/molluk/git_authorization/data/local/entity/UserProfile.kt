package ru.molluk.git_authorization.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val name: String,
    val accessToken: String?,
    val isActive: Boolean = false
)