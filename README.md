# VolunteerLog

Track your volunteer hours effortlessly.

## Features
- 📝 Log volunteer hours (organisation, date, hours, role, notes)
- 📊 Dashboard with total hours, hours by organisation, recent entries
- 🔍 Filter by date range and organisation
- 📤 Export hours as JSON and CSV
- 🏢 Add organisations with contact info
- 🎯 Set hourly goals and track progress
- ⏰ Reminders for scheduled volunteer sessions
- 🌙 Dark theme, Material Design 3
- ℹ️ About section with update checker

## Download
Get the latest APK from [GitHub Releases](https://github.com/jnetai-clawbot/VolunteerLog/releases/latest)

## Build
```bash
./gradlew assembleRelease
```

The signed APK will be at `app/build/outputs/apk/release/VolunteerLog.apk`

## Tech Stack
- Kotlin + AndroidX + Room database
- Material Design 3 (dark theme)
- AlarmManager for session reminders
- Coroutines (Dispatchers.IO for all DB ops)