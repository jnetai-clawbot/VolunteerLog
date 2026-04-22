package com.jnetai.volunteerlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jnetai.volunteerlog.data.dao.*
import com.jnetai.volunteerlog.data.entity.*

@Database(
    entities = [VolunteerEntry::class, Organisation::class, Goal::class, Reminder::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun volunteerEntryDao(): VolunteerEntryDao
    abstract fun organisationDao(): OrganisationDao
    abstract fun goalDao(): GoalDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "volunteerlog.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}