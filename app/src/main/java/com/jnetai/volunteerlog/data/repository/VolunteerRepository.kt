package com.jnetai.volunteerlog.data.repository

import com.jnetai.volunteerlog.data.dao.*
import com.jnetai.volunteerlog.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VolunteerRepository(
    private val entryDao: VolunteerEntryDao,
    private val orgDao: OrganisationDao,
    private val goalDao: GoalDao,
    private val reminderDao: ReminderDao
) {
    // Entries
    fun getAllEntries(): Flow<List<VolunteerEntry>> = entryDao.getAll()
    fun getTotalHours(): Flow<Double?> = entryDao.getTotalHours()
    fun getHoursByOrganisation(): Flow<List<OrgHours>> = entryDao.getHoursByOrganisation()
    fun getHoursForOrganisation(orgName: String): Flow<Double?> = entryDao.getHoursForOrganisation(orgName)

    suspend fun insertEntry(entry: VolunteerEntry) = withContext(Dispatchers.IO) { entryDao.insert(entry) }
    suspend fun updateEntry(entry: VolunteerEntry) = withContext(Dispatchers.IO) { entryDao.update(entry) }
    suspend fun deleteEntry(entry: VolunteerEntry) = withContext(Dispatchers.IO) { entryDao.delete(entry) }
    suspend fun deleteEntryById(id: Long) = withContext(Dispatchers.IO) { entryDao.deleteById(id) }

    fun getByDateRange(from: Long, to: Long): Flow<List<VolunteerEntry>> = entryDao.getByDateRange(from, to)
    fun getByOrganisation(orgName: String): Flow<List<VolunteerEntry>> = entryDao.getByOrganisation(orgName)
    fun getByOrgAndDateRange(orgName: String, from: Long, to: Long): Flow<List<VolunteerEntry>> = entryDao.getByOrgAndDateRange(orgName, from, to)

    suspend fun getAllEntriesOnce(): List<VolunteerEntry> = withContext(Dispatchers.IO) { entryDao.getAllOnce() }

    // Organisations
    fun getAllOrganisations(): Flow<List<Organisation>> = orgDao.getAll()
    suspend fun getAllOrganisationsOnce(): List<Organisation> = withContext(Dispatchers.IO) { orgDao.getAllOnce() }
    suspend fun insertOrganisation(org: Organisation) = withContext(Dispatchers.IO) { orgDao.insert(org) }
    suspend fun updateOrganisation(org: Organisation) = withContext(Dispatchers.IO) { orgDao.update(org) }
    suspend fun deleteOrganisation(org: Organisation) = withContext(Dispatchers.IO) { orgDao.delete(org) }
    suspend fun deleteOrganisationById(id: Long) = withContext(Dispatchers.IO) { orgDao.deleteById(id) }

    // Goals
    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAll()
    fun getGlobalGoal(): Flow<Goal?> = goalDao.getGlobalGoal()
    fun getGoalForOrganisation(orgName: String): Flow<Goal?> = goalDao.getGoalForOrganisation(orgName)
    suspend fun insertGoal(goal: Goal) = withContext(Dispatchers.IO) { goalDao.insert(goal) }
    suspend fun updateGoal(goal: Goal) = withContext(Dispatchers.IO) { goalDao.update(goal) }
    suspend fun deleteGoal(goal: Goal) = withContext(Dispatchers.IO) { goalDao.delete(goal) }
    suspend fun deleteGoalById(id: Long) = withContext(Dispatchers.IO) { goalDao.deleteById(id) }

    // Reminders
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAll()
    fun getUpcomingReminders(now: Long): Flow<List<Reminder>> = reminderDao.getUpcoming(now)
    suspend fun insertReminder(reminder: Reminder) = withContext(Dispatchers.IO) { reminderDao.insert(reminder) }
    suspend fun updateReminder(reminder: Reminder) = withContext(Dispatchers.IO) { reminderDao.update(reminder) }
    suspend fun deleteReminder(reminder: Reminder) = withContext(Dispatchers.IO) { reminderDao.delete(reminder) }
    suspend fun deleteReminderById(id: Long) = withContext(Dispatchers.IO) { reminderDao.deleteById(id) }
    suspend fun getReminderById(id: Long): Reminder? = withContext(Dispatchers.IO) { reminderDao.getById(id) }
}