package ru.zuevs5115.deadlinedaemon.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun save(user: UserCredentials)

    @Query("DELETE FROM user_credentials WHERE username = :username AND password = :password")
    suspend fun delete(username: String, password: String): Int

    @Query("SELECT * FROM user_credentials")
    suspend fun getAll(): List<UserCredentials>
}