package ru.zuevs5115.deadlinedaemon.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.zuevs5115.deadlinedaemon.entities.UserCredentials
import ru.zuevs5115.deadlinedaemon.entities.UserDao

@Database(entities = [UserCredentials::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}