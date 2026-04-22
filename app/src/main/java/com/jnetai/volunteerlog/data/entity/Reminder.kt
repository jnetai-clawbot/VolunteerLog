package com.jnetai.volunteerlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val organisationName: String = "",
    val dateTime: Long, // epoch millis
    val created: Boolean = false // whether alarm has been set
)