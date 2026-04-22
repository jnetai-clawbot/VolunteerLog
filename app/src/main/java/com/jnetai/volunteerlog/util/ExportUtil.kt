package com.jnetai.volunteerlog.util

import com.jnetai.volunteerlog.data.entity.VolunteerEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ExportUtil {

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.UK)

    fun entriesToJson(entries: List<VolunteerEntry>): String {
        val exportList = entries.map { entry ->
            mapOf(
                "id" to entry.id,
                "organisation" to entry.organisationName,
                "date" to dateFormat.format(Date(entry.date)),
                "hours" to entry.hours,
                "role" to entry.role,
                "notes" to entry.notes
            )
        }
        return gson.toJson(exportList)
    }

    fun entriesToCsv(entries: List<VolunteerEntry>): String {
        val sb = StringBuilder()
        sb.appendLine("ID,Organisation,Date,Hours,Role,Notes")
        for (entry in entries) {
            sb.appendLine("${entry.id},\"${entry.organisationName}\",${dateFormat.format(Date(entry.date))},${entry.hours},\"${entry.role}\",\"${entry.notes.replace("\"", "\"\"")}\"")
        }
        return sb.toString()
    }

    fun saveToFile(dir: File, filename: String, content: String): File {
        val file = File(dir, filename)
        file.writeText(content)
        return file
    }
}