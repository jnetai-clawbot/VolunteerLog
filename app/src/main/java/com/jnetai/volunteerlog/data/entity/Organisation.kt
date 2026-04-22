package com.jnetai.volunteerlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Organisation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contactPhone: String = "",
    val contactEmail: String = "",
    val contactInfo: String = ""
)