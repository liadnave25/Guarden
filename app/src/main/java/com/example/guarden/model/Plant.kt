package com.example.guarden.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val imageUri: String?,
    val wateringFrequency: Int,
    val lastWateringDate: Long = System.currentTimeMillis()
)