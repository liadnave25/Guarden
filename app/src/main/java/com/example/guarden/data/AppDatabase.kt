package com.example.guarden.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.guarden.model.Plant

@Database(entities = [Plant::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
}