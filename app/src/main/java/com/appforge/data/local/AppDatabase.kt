package com.appforge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.appforge.domain.model.AppInstance

@Database(entities = [AppInstance::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appInstanceDao(): AppInstanceDao
}
