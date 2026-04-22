package com.jnetai.volunteerlog.data.dao

import androidx.room.*
import com.jnetai.volunteerlog.data.entity.Organisation
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganisationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(org: Organisation): Long

    @Update
    suspend fun update(org: Organisation)

    @Delete
    suspend fun delete(org: Organisation)

    @Query("SELECT * FROM Organisation ORDER BY name ASC")
    fun getAll(): Flow<List<Organisation>>

    @Query("SELECT * FROM Organisation ORDER BY name ASC")
    suspend fun getAllOnce(): List<Organisation>

    @Query("SELECT * FROM Organisation WHERE id = :id")
    suspend fun getById(id: Long): Organisation?

    @Query("SELECT * FROM Organisation WHERE name = :name")
    suspend fun getByName(name: String): Organisation?

    @Query("DELETE FROM Organisation WHERE id = :id")
    suspend fun deleteById(id: Long)
}