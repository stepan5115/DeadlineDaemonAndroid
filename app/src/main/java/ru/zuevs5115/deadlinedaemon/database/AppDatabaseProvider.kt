package ru.zuevs5115.deadlinedaemon.database

import android.content.Context
import androidx.room.Room

object AppDatabaseProvider {
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app-db"
            ).build()
        }
        return instance!!
    }
}