package com.jnetai.volunteerlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(indices = [Index(value = ["organisationName"])])
data class VolunteerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organisationName: String,
    val date: Long, // epoch millis
    val hours: Double,
    val role: String,
    val notes: String = ""
)