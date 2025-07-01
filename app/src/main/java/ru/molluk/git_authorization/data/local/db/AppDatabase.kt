package ru.molluk.git_authorization.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.molluk.git_authorization.data.local.entity.UserProfile
import ru.molluk.git_authorization.data.local.dao.ProfileDao

@Database(
    entities = [UserProfile::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}