package com.jnetai.volunteerlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetHours: Int,
    val organisationName: String = "", // empty = global goal
    val createdDate: Long = System.currentTimeMillis()
)