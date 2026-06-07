package com.appforge.data.local

import androidx.room.*
import com.appforge.domain.model.AppInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInstanceDao {
    @Query("SELECT * FROM app_instances ORDER BY createdAt DESC")
    fun getAllInstances(): Flow<List<AppInstance>>

    @Query("SELECT * FROM app_instances WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveInstance(): AppInstance?

    @Query("SELECT * FROM app_instances WHERE id = :id")
    suspend fun getInstanceById(id: Long): AppInstance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: AppInstance): Long

    @Update
    suspend fun updateInstance(instance: AppInstance)

    @Delete
    suspend fun deleteInstance(instance: AppInstance)

    @Query("UPDATE app_instances SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE app_instances SET isActive = 1 WHERE id = :id")
    suspend fun activateInstance(id: Long)
}
