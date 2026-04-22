package com.jnetai.volunteerlog

import android.app.Application
import com.jnetai.volunteerlog.data.AppDatabase

class VolunteerLogApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}