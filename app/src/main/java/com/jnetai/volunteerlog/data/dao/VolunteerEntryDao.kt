package com.jnetai.volunteerlog.data.dao

import androidx.room.*
import com.jnetai.volunteerlog.data.entity.VolunteerEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface VolunteerEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: VolunteerEntry): Long

    @Update
    suspend fun update(entry: VolunteerEntry)

    @Delete
    suspend fun delete(entry: VolunteerEntry)

    @Query("SELECT * FROM VolunteerEntry ORDER BY date DESC")
    fun getAll(): Flow<List<VolunteerEntry>>

    @Query("SELECT * FROM VolunteerEntry ORDER BY date DESC")
    suspend fun getAllOnce(): List<VolunteerEntry>

    @Query("SELECT * FROM VolunteerEntry WHERE id = :id")
    suspend fun getById(id: Long): VolunteerEntry?

    @Query("SELECT SUM(hours) FROM VolunteerEntry")
    fun getTotalHours(): Flow<Double?>

    @Query("SELECT organisationName, SUM(hours) as hours FROM VolunteerEntry GROUP BY organisationName ORDER BY hours DESC")
    fun getHoursByOrganisation(): Flow<List<OrgHours>>

    @Query("SELECT * FROM VolunteerEntry WHERE date >= :fromDate AND date <= :toDate ORDER BY date DESC")
    fun getByDateRange(fromDate: Long, toDate: Long): Flow<List<VolunteerEntry>>

    @Query("SELECT * FROM VolunteerEntry WHERE organisationName = :orgName ORDER BY date DESC")
    fun getByOrganisation(orgName: String): Flow<List<VolunteerEntry>>

    @Query("SELECT * FROM VolunteerEntry WHERE organisationName = :orgName AND date >= :fromDate AND date <= :toDate ORDER BY date DESC")
    fun getByOrgAndDateRange(orgName: String, fromDate: Long, toDate: Long): Flow<List<VolunteerEntry>>

    @Query("SELECT SUM(hours) FROM VolunteerEntry WHERE organisationName = :orgName")
    fun getHoursForOrganisation(orgName: String): Flow<Double?>

    @Query("DELETE FROM VolunteerEntry WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class OrgHours(
    val organisationName: String,
    val hours: Double
)