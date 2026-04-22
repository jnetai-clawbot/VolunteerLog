package com.jnetai.volunteerlog.data.dao

import androidx.room.*
import com.jnetai.volunteerlog.data.entity.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("SELECT * FROM Goal ORDER BY organisationName ASC")
    fun getAll(): Flow<List<Goal>>

    @Query("SELECT * FROM Goal WHERE organisationName = '' LIMIT 1")
    fun getGlobalGoal(): Flow<Goal?>

    @Query("SELECT * FROM Goal WHERE organisationName = :orgName LIMIT 1")
    fun getGoalForOrganisation(orgName: String): Flow<Goal?>

    @Query("DELETE FROM Goal WHERE id = :id")
    suspend fun deleteById(id: Long)
}