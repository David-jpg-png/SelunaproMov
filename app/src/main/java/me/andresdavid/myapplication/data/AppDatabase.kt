package me.andresdavid.myapplication.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "AppDatabase"
        private const val DATABASE_NAME = "seluna_local_database.db"
        private const val DATABASE_VERSION = 1
        private const val DEFAULT_TEST_UID = "test1_default_uid"

        // Users table
        private const val TABLE_USERS = "users"
        private const val COL_USER_UID = "uid"
        private const val COL_USER_NAME = "name"
        private const val COL_USER_EMAIL = "email"
        private const val COL_USER_GOAL = "goal"
        private const val COL_USER_APPROACH = "approach"
        private const val COL_USER_REGULARITY = "regularity"
        private const val COL_USER_CONTRACEPTIVE = "contraceptive"
        private const val COL_USER_PERIOD_DAYS = "period_days"
        private const val COL_USER_CONDITIONS = "conditions"
        private const val COL_USER_PRIORITIES = "priorities"
        private const val COL_USER_NOTIFICATIONS = "notifications_enabled"

        // Daily Logs table
        private const val TABLE_LOGS = "daily_logs"
        private const val COL_LOG_ID = "id"
        private const val COL_LOG_USER_UID = "user_uid"
        private const val COL_LOG_DATE = "date"
        private const val COL_LOG_FLOW = "flow_level"
        private const val COL_LOG_SYMPTOMS = "symptoms"
        private const val COL_LOG_MOODS = "moods"
        private const val COL_LOG_MEDICATION = "medication_taken"
        private const val COL_LOG_NOTES = "notes"
        private const val COL_LOG_TIMESTAMP = "timestamp"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_UID TEXT PRIMARY KEY,
                $COL_USER_NAME TEXT,
                $COL_USER_EMAIL TEXT,
                $COL_USER_GOAL TEXT,
                $COL_USER_APPROACH TEXT,
                $COL_USER_REGULARITY TEXT,
                $COL_USER_CONTRACEPTIVE TEXT,
                $COL_USER_PERIOD_DAYS INTEGER,
                $COL_USER_CONDITIONS TEXT,
                $COL_USER_PRIORITIES TEXT,
                $COL_USER_NOTIFICATIONS INTEGER
            )
        """.trimIndent()

        val createLogsTable = """
            CREATE TABLE $TABLE_LOGS (
                $COL_LOG_ID TEXT PRIMARY KEY,
                $COL_LOG_USER_UID TEXT,
                $COL_LOG_DATE TEXT,
                $COL_LOG_FLOW TEXT,
                $COL_LOG_SYMPTOMS TEXT,
                $COL_LOG_MOODS TEXT,
                $COL_LOG_MEDICATION INTEGER,
                $COL_LOG_NOTES TEXT,
                $COL_LOG_TIMESTAMP INTEGER
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createLogsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        onCreate(db)
    }

    // Insert or Update User Profile in Database
    fun saveUserProfile(profile: UserProfile) {
        val targetUid = if (profile.uid.isNotBlank()) profile.uid else DEFAULT_TEST_UID
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COL_USER_UID, targetUid)
                put(COL_USER_NAME, profile.name)
                put(COL_USER_EMAIL, profile.email)
                put(COL_USER_GOAL, profile.goal)
                put(COL_USER_APPROACH, profile.approach)
                put(COL_USER_REGULARITY, profile.regularity)
                put(COL_USER_CONTRACEPTIVE, profile.contraceptive)
                put(COL_USER_PERIOD_DAYS, profile.periodDays)
                put(COL_USER_CONDITIONS, profile.conditions.joinToString(";;;"))
                put(COL_USER_PRIORITIES, profile.priorities.joinToString(";;;"))
                put(COL_USER_NOTIFICATIONS, if (profile.notificationsEnabled) 1 else 0)
            }
            db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)

            // Also keep default test UID in sync if different
            if (targetUid != DEFAULT_TEST_UID) {
                val fallbackValues = ContentValues(values).apply { put(COL_USER_UID, DEFAULT_TEST_UID) }
                db.insertWithOnConflict(TABLE_USERS, null, fallbackValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile to database", e)
        }
    }

    // Retrieve User Profile from Database
    fun getUserProfile(uid: String): UserProfile? {
        val targetUid = if (uid.isNotBlank()) uid else DEFAULT_TEST_UID
        try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_USERS,
                null,
                "$COL_USER_UID = ?",
                arrayOf(targetUid),
                null,
                null,
                null
            )
            cursor.use { c ->
                if (c.moveToFirst()) {
                    val name = c.getString(c.getColumnIndexOrThrow(COL_USER_NAME)) ?: ""
                    val email = c.getString(c.getColumnIndexOrThrow(COL_USER_EMAIL)) ?: ""
                    val goal = c.getString(c.getColumnIndexOrThrow(COL_USER_GOAL)) ?: ""
                    val approach = c.getString(c.getColumnIndexOrThrow(COL_USER_APPROACH)) ?: "TRACK_CYCLE"
                    val regularity = c.getString(c.getColumnIndexOrThrow(COL_USER_REGULARITY)) ?: "REGULAR"
                    val contraceptive = c.getString(c.getColumnIndexOrThrow(COL_USER_CONTRACEPTIVE)) ?: "NONE"
                    val periodDays = c.getInt(c.getColumnIndexOrThrow(COL_USER_PERIOD_DAYS))
                    val condStr = c.getString(c.getColumnIndexOrThrow(COL_USER_CONDITIONS)) ?: ""
                    val conditions = if (condStr.isNotBlank()) condStr.split(";;;") else emptyList()
                    val prioStr = c.getString(c.getColumnIndexOrThrow(COL_USER_PRIORITIES)) ?: ""
                    val priorities = if (prioStr.isNotBlank()) prioStr.split(";;;") else emptyList()
                    val notificationsEnabled = c.getInt(c.getColumnIndexOrThrow(COL_USER_NOTIFICATIONS)) == 1

                    return UserProfile(
                        uid = targetUid,
                        name = if (name.isNotBlank()) name else "Test 1",
                        email = if (email.isNotBlank()) email else "test1@seluna.com",
                        goal = if (goal.isNotBlank()) goal else "Conocer y registrar mi ciclo menstrual",
                        approach = approach,
                        regularity = regularity,
                        contraceptive = contraceptive,
                        periodDays = if (periodDays > 0) periodDays else 5,
                        conditions = if (conditions.isNotEmpty()) conditions else listOf("Cólicos menstruales", "Sensibilidad hormonal"),
                        priorities = if (priorities.isNotEmpty()) priorities else listOf("Alivio de dolor", "Seguimiento de ánimo", "Nutrición"),
                        notificationsEnabled = notificationsEnabled
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile from database", e)
        }

        // Fallback to default test UID if specific targetUid wasn't found yet
        if (targetUid != DEFAULT_TEST_UID) {
            return getUserProfile(DEFAULT_TEST_UID)
        }

        return null
    }

    // Insert or Update Daily Log in Database
    fun saveDailyLog(userUid: String, log: DailyLogItem) {
        val targetUid = if (userUid.isNotBlank()) userUid else DEFAULT_TEST_UID
        if (log.id.isBlank()) return
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COL_LOG_ID, log.id)
                put(COL_LOG_USER_UID, targetUid)
                put(COL_LOG_DATE, log.date)
                put(COL_LOG_FLOW, log.flowLevel)
                put(COL_LOG_SYMPTOMS, log.symptoms.joinToString(";;;"))
                put(COL_LOG_MOODS, log.moods.joinToString(";;;"))
                put(COL_LOG_MEDICATION, if (log.medicationTaken) 1 else 0)
                put(COL_LOG_NOTES, log.notes)
                put(COL_LOG_TIMESTAMP, log.timestamp)
            }
            db.insertWithOnConflict(TABLE_LOGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)

            if (targetUid != DEFAULT_TEST_UID) {
                val fallbackValues = ContentValues(values).apply { put(COL_LOG_USER_UID, DEFAULT_TEST_UID) }
                db.insertWithOnConflict(TABLE_LOGS, null, fallbackValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving log to database", e)
        }
    }

    // Retrieve all Daily Logs for a user from Database
    fun getDailyLogs(userUid: String): List<DailyLogItem> {
        val targetUid = if (userUid.isNotBlank()) userUid else DEFAULT_TEST_UID
        val list = mutableListOf<DailyLogItem>()
        try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_LOGS,
                null,
                "$COL_LOG_USER_UID = ?",
                arrayOf(targetUid),
                null,
                null,
                "$COL_LOG_DATE DESC"
            )
            cursor.use { c ->
                while (c.moveToNext()) {
                    val id = c.getString(c.getColumnIndexOrThrow(COL_LOG_ID)) ?: ""
                    val date = c.getString(c.getColumnIndexOrThrow(COL_LOG_DATE)) ?: ""
                    val flowLevel = c.getString(c.getColumnIndexOrThrow(COL_LOG_FLOW)) ?: ""
                    val sympStr = c.getString(c.getColumnIndexOrThrow(COL_LOG_SYMPTOMS)) ?: ""
                    val symptoms = if (sympStr.isNotBlank()) sympStr.split(";;;") else emptyList()
                    val moodStr = c.getString(c.getColumnIndexOrThrow(COL_LOG_MOODS)) ?: ""
                    val moods = if (moodStr.isNotBlank()) moodStr.split(";;;") else emptyList()
                    val medicationTaken = c.getInt(c.getColumnIndexOrThrow(COL_LOG_MEDICATION)) == 1
                    val notes = c.getString(c.getColumnIndexOrThrow(COL_LOG_NOTES)) ?: ""
                    val timestamp = c.getLong(c.getColumnIndexOrThrow(COL_LOG_TIMESTAMP))

                    list.add(
                        DailyLogItem(
                            id = id,
                            date = date,
                            flowLevel = flowLevel,
                            symptoms = symptoms,
                            moods = moods,
                            medicationTaken = medicationTaken,
                            notes = notes,
                            timestamp = timestamp
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting logs from database", e)
        }

        if (list.isEmpty() && targetUid != DEFAULT_TEST_UID) {
            return getDailyLogs(DEFAULT_TEST_UID)
        }

        return list
    }

    // Populate default database seed records if database is empty for user
    fun seedIfEmpty(uid: String, email: String = "", name: String = "") {
        val targetUid = if (uid.isNotBlank()) uid else DEFAULT_TEST_UID
        val existingProfile = getUserProfile(targetUid)
        if (existingProfile == null || existingProfile.name.isBlank() || existingProfile.name == "Usuario") {
            val displayName = if (name.isNotBlank() && name != "Usuario") name else "Test 1"
            val defaultProfile = UserProfile(
                uid = targetUid,
                name = displayName,
                email = if (email.isNotBlank()) email else "test1@seluna.com",
                goal = "Conocer y registrar mi ciclo menstrual",
                approach = "TRACK_CYCLE",
                regularity = "REGULAR",
                contraceptive = "PILLS",
                periodDays = 5,
                conditions = listOf("Cólicos menstruales", "Sensibilidad hormonal"),
                priorities = listOf("Alivio de dolor", "Seguimiento de ánimo", "Nutrición"),
                notificationsEnabled = true
            )
            saveUserProfile(defaultProfile)
        }

        val existingLogs = getDailyLogs(targetUid)
        if (existingLogs.isEmpty()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()

            val todayStr = dateFormat.format(cal.time)

            cal.add(Calendar.DAY_OF_MONTH, -1)
            val yesterdayStr = dateFormat.format(cal.time)

            cal.add(Calendar.DAY_OF_MONTH, -1)
            val dayBeforeYesterdayStr = dateFormat.format(cal.time)

            cal.add(Calendar.DAY_OF_MONTH, -1)
            val threeDaysAgoStr = dateFormat.format(cal.time)

            val seedLogs = listOf(
                DailyLogItem(
                    id = todayStr,
                    date = todayStr,
                    flowLevel = "Medio",
                    symptoms = listOf("Cólicos", "Fatiga", "Sensibilidad mamaria"),
                    moods = listOf("Tranquila", "Sensible"),
                    medicationTaken = true,
                    notes = "Registro diario de test1 - Base de datos local Seluna.",
                    timestamp = System.currentTimeMillis()
                ),
                DailyLogItem(
                    id = yesterdayStr,
                    date = yesterdayStr,
                    flowLevel = "Abundante",
                    symptoms = listOf("Cólicos", "Dolor de cabeza", "Hinchazón"),
                    moods = listOf("Irritable", "Sensible"),
                    medicationTaken = true,
                    notes = "Día de mayor flujo, descanso e hidratación.",
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                DailyLogItem(
                    id = dayBeforeYesterdayStr,
                    date = dayBeforeYesterdayStr,
                    flowLevel = "Ligero",
                    symptoms = listOf("Fatiga", "Antojos"),
                    moods = listOf("Tranquila"),
                    medicationTaken = true,
                    notes = "Flujo ligero, nivel de energía estable.",
                    timestamp = System.currentTimeMillis() - 172800000L
                ),
                DailyLogItem(
                    id = threeDaysAgoStr,
                    date = threeDaysAgoStr,
                    flowLevel = "Ninguno",
                    symptoms = listOf("Acné"),
                    moods = listOf("Energética"),
                    medicationTaken = false,
                    notes = "Inicio de registro previo.",
                    timestamp = System.currentTimeMillis() - 259200000L
                )
            )

            seedLogs.forEach { log ->
                saveDailyLog(targetUid, log)
            }
        }
    }
}
