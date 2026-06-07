package com.appforge.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_instances")
data class AppInstance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dbFilePath: String,
    val templateId: Int,
    val iconUri: String? = null,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
