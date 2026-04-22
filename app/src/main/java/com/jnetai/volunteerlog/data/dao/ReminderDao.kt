package com.jnetai.volunteerlog.data.dao

import androidx.room.*
import com.jnetai.volunteerlog.data.entity.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM Reminder ORDER BY dateTime ASC")
    fun getAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM Reminder WHERE dateTime >= :now ORDER BY dateTime ASC")
    fun getUpcoming(now: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM Reminder WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Query("DELETE FROM Reminder WHERE id = :id")
    suspend fun deleteById(id: Long)
}