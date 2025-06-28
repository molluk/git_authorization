package ru.molluk.git_authorization.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val login: String,
    val avatarUrl: String,
    @ColumnInfo(defaultValue = "UNKNOWN_DATE")
    val createdAt: String,
    val accessToken: String?,
    val isActive: Boolean = false
) : Parcelable