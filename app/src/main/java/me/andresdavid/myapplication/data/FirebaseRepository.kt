package me.andresdavid.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

data class DailyLogItem(
    val id: String = "",
    val date: String = "",
    val flowLevel: String = "",
    val symptoms: List<String> = emptyList(),
    val moods: List<String> = emptyList(),
    val medicationTaken: Boolean = false,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val goal: String = "",
    val approach: String = "TRACK_CYCLE",
    val regularity: String = "REGULAR",
    val contraceptive: String = "NONE",
    val periodDays: Int = 5,
    val conditions: List<String> = emptyList(),
    val priorities: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true
)

object FirebaseRepository {
    private const val TAG = "FirebaseRepository"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var prefs: SharedPreferences? = null
    private var appDb: AppDatabase? = null

    private var cachedUserProfile: UserProfile? = null
    private var cachedDailyLogs: MutableList<DailyLogItem>? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("seluna_local_cache", Context.MODE_PRIVATE)
        appDb = AppDatabase.getInstance(context)

        val uid = currentUserId
        if (!uid.isNullOrBlank()) {
            val authUser = auth.currentUser
            val email = authUser?.email ?: ""
            val name = authUser?.displayName ?: "Test 1"
            appDb?.seedIfEmpty(uid, email, name)
        }

        cachedUserProfile = loadProfileFromPrefs()
        cachedDailyLogs = loadLogsFromPrefs()
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserName: String
        get() {
            val cachedName = cachedUserProfile?.name
            if (!cachedName.isNullOrBlank()) return cachedName

            val authName = auth.currentUser?.displayName
            if (!authName.isNullOrBlank()) return authName

            val emailName = auth.currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            if (!emailName.isNullOrBlank()) return emailName

            return "Usuario Test"
        }

    private fun saveProfileToPrefs(profile: UserProfile) {
        appDb?.saveUserProfile(profile)

        val p = prefs ?: return
        p.edit().apply {
            putString("uid_${profile.uid}", profile.uid)
            putString("name_${profile.uid}", profile.name)
            putString("email_${profile.uid}", profile.email)
            putString("goal_${profile.uid}", profile.goal)
            putString("approach_${profile.uid}", profile.approach)
            putString("regularity_${profile.uid}", profile.regularity)
            putString("contraceptive_${profile.uid}", profile.contraceptive)
            putInt("periodDays_${profile.uid}", profile.periodDays)
            putString("conditions_${profile.uid}", profile.conditions.joinToString(";;;"))
            putString("priorities_${profile.uid}", profile.priorities.joinToString(";;;"))
            putBoolean("notificationsEnabled_${profile.uid}", profile.notificationsEnabled)
            apply()
        }
    }

    private fun loadProfileFromPrefs(): UserProfile? {
        val uid = currentUserId ?: return null

        val dbProfile = appDb?.getUserProfile(uid)
        if (dbProfile != null && dbProfile.name.isNotBlank()) {
            return dbProfile
        }

        val p = prefs ?: return null
        val savedUid = p.getString("uid_$uid", "") ?: ""
        if (savedUid.isBlank()) return null
        val name = p.getString("name_$uid", "") ?: ""
        val email = p.getString("email_$uid", "") ?: ""
        val goal = p.getString("goal_$uid", "") ?: ""
        val approach = p.getString("approach_$uid", "TRACK_CYCLE") ?: "TRACK_CYCLE"
        val regularity = p.getString("regularity_$uid", "REGULAR") ?: "REGULAR"
        val contraceptive = p.getString("contraceptive_$uid", "NONE") ?: "NONE"
        val periodDays = p.getInt("periodDays_$uid", 5)
        val conditionsStr = p.getString("conditions_$uid", "") ?: ""
        val conditions = if (conditionsStr.isNotBlank()) conditionsStr.split(";;;") else emptyList()
        val prioritiesStr = p.getString("priorities_$uid", "") ?: ""
        val priorities = if (prioritiesStr.isNotBlank()) prioritiesStr.split(";;;") else emptyList()
        val notificationsEnabled = p.getBoolean("notificationsEnabled_$uid", true)

        val profile = UserProfile(
            uid = uid,
            name = name,
            email = email,
            goal = goal,
            approach = approach,
            regularity = regularity,
            contraceptive = contraceptive,
            periodDays = periodDays,
            conditions = conditions,
            priorities = priorities,
            notificationsEnabled = notificationsEnabled
        )

        appDb?.saveUserProfile(profile)
        return profile
    }

    private fun saveLogsToPrefs(logs: List<DailyLogItem>) {
        val uid = currentUserId ?: return

        logs.forEach { log ->
            appDb?.saveDailyLog(uid, log)
        }

        val p = prefs ?: return
        val jsonArray = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject().apply {
                put("id", log.id)
                put("date", log.date)
                put("flowLevel", log.flowLevel)
                put("symptoms", JSONArray(log.symptoms))
                put("moods", JSONArray(log.moods))
                put("medicationTaken", log.medicationTaken)
                put("notes", log.notes)
                put("timestamp", log.timestamp)
            }
            jsonArray.put(obj)
        }
        p.edit().putString("saved_daily_logs_$uid", jsonArray.toString()).apply()
    }

    private fun loadLogsFromPrefs(): MutableList<DailyLogItem> {
        val uid = currentUserId ?: return mutableListOf()

        val dbLogs = appDb?.getDailyLogs(uid)
        if (!dbLogs.isNullOrEmpty()) {
            return dbLogs.toMutableList()
        }

        val p = prefs ?: return mutableListOf()
        val raw = p.getString("saved_daily_logs_$uid", "") ?: ""
        if (raw.isBlank()) return mutableListOf()
        val list = mutableListOf<DailyLogItem>()
        return try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val symptomsArr = obj.optJSONArray("symptoms")
                val symptomsList = mutableListOf<String>()
                if (symptomsArr != null) {
                    for (j in 0 until symptomsArr.length()) symptomsList.add(symptomsArr.getString(j))
                }
                val moodsArr = obj.optJSONArray("moods")
                val moodsList = mutableListOf<String>()
                if (moodsArr != null) {
                    for (j in 0 until moodsArr.length()) moodsList.add(moodsArr.getString(j))
                }
                val item = DailyLogItem(
                    id = obj.optString("id", ""),
                    date = obj.optString("date", ""),
                    flowLevel = obj.optString("flowLevel", ""),
                    symptoms = symptomsList,
                    moods = moodsList,
                    medicationTaken = obj.optBoolean("medicationTaken", false),
                    notes = obj.optString("notes", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
                list.add(item)
                appDb?.saveDailyLog(uid, item)
            }
            list
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    suspend fun signUp(email: String, pass: String, name: String, goal: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw Exception("User UID is null")
            val uid = user.uid

            runCatching {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            val userProfile = UserProfile(uid = uid, name = name, email = email, goal = goal)
            cachedUserProfile = userProfile
            saveProfileToPrefs(userProfile)

            // Try Cloud Write
            runCatching {
                db.collection("users").document(uid).set(userProfile).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error in signUp", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<Unit> {
        return try {
            cachedUserProfile = null
            cachedDailyLogs = null

            auth.signInWithEmailAndPassword(email, pass).await()

            val uid = currentUserId
            if (!uid.isNullOrBlank()) {
                appDb?.seedIfEmpty(uid, email, auth.currentUser?.displayName ?: "Test 1")
            }

            // Try Cloud Fetch first to get remote updates
            runCatching { fetchProfileFromCloud() }
            runCatching { fetchDailyLogsFromCloud() }

            getUserProfile()
            getDailyLogs()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error in signIn", e)
            Result.failure(e)
        }
    }

    suspend fun saveNaturalRhythm(
        approach: String,
        regularity: String,
        contraceptive: String,
        periodDays: Int,
        conditions: List<String>
    ): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val current = cachedUserProfile ?: loadProfileFromPrefs() ?: UserProfile(uid = uid, name = currentUserName)
            val updated = current.copy(
                uid = uid,
                approach = approach,
                regularity = regularity,
                contraceptive = contraceptive,
                periodDays = periodDays,
                conditions = conditions
            )
            cachedUserProfile = updated
            saveProfileToPrefs(updated)

            val updates = mapOf(
                "approach" to approach,
                "regularity" to regularity,
                "contraceptive" to contraceptive,
                "periodDays" to periodDays,
                "conditions" to conditions
            )

            // Push to Cloud Firestore
            runCatching {
                db.collection("users").document(uid).set(updates, SetOptions.merge()).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving natural rhythm", e)
            Result.failure(e)
        }
    }

    suspend fun savePersonalization(
        priorities: List<String>,
        notificationsEnabled: Boolean
    ): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val current = cachedUserProfile ?: loadProfileFromPrefs() ?: UserProfile(uid = uid, name = currentUserName)
            val updated = current.copy(
                uid = uid,
                priorities = priorities,
                notificationsEnabled = notificationsEnabled
            )
            cachedUserProfile = updated
            saveProfileToPrefs(updated)

            val updates = mapOf(
                "priorities" to priorities,
                "notificationsEnabled" to notificationsEnabled
            )

            // Push to Cloud Firestore
            runCatching {
                db.collection("users").document(uid).set(updates, SetOptions.merge()).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving personalization", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchProfileFromCloud(): UserProfile? {
        val uid = currentUserId ?: return null
        val authUser = auth.currentUser
        val email = authUser?.email ?: ""
        val derivedName = authUser?.displayName?.ifBlank { null }
            ?: email.substringBefore("@").takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
            ?: "Test 1"

        return try {
            val docSnapshot = db.collection("users").document(uid).get().await()

            if (docSnapshot != null && docSnapshot.exists()) {
                val profile = UserProfile(
                    uid = uid,
                    name = docSnapshot.getString("name") ?: derivedName,
                    email = docSnapshot.getString("email") ?: email,
                    goal = docSnapshot.getString("goal") ?: "Conocer y registrar mi ciclo menstrual",
                    approach = docSnapshot.getString("approach") ?: "TRACK_CYCLE",
                    regularity = docSnapshot.getString("regularity") ?: "REGULAR",
                    contraceptive = docSnapshot.getString("contraceptive") ?: "PILLS",
                    periodDays = (docSnapshot.get("periodDays") as? Number)?.toInt() ?: 5,
                    conditions = (docSnapshot.get("conditions") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Cólicos menstruales", "Sensibilidad hormonal"),
                    priorities = (docSnapshot.get("priorities") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Alivio de dolor", "Seguimiento de ánimo"),
                    notificationsEnabled = docSnapshot.getBoolean("notificationsEnabled") ?: true
                )
                cachedUserProfile = profile
                saveProfileToPrefs(profile)
                profile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile from cloud", e)
            null
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val uid = currentUserId ?: return null

        val authUser = auth.currentUser
        val email = authUser?.email ?: ""
        val name = authUser?.displayName ?: "Test 1"
        appDb?.seedIfEmpty(uid, email, name)

        if (cachedUserProfile != null && cachedUserProfile?.uid == uid && cachedUserProfile!!.name.isNotBlank() && cachedUserProfile!!.name != "Usuario") {
            return cachedUserProfile
        }

        // 1. Try Local SQLite Database first for instant offline/cache response
        val localProfile = loadProfileFromPrefs()
        if (localProfile != null && localProfile.uid == uid && localProfile.name.isNotBlank() && localProfile.name != "Usuario") {
            cachedUserProfile = localProfile
            // Auto-sync to Cloud Firestore in background
            runCatching {
                db.collection("users").document(uid).set(localProfile, SetOptions.merge())
            }
            return localProfile
        }

        // 2. Try Cloud Fetch
        val cloudProfile = fetchProfileFromCloud()
        if (cloudProfile != null && cloudProfile.name.isNotBlank()) {
            cachedUserProfile = cloudProfile
            saveProfileToPrefs(cloudProfile)
            return cloudProfile
        }

        // 3. Fallback
        val derivedName = authUser?.displayName?.ifBlank { null }
            ?: email.substringBefore("@").takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
            ?: "Test 1"

        val fallback = UserProfile(
            uid = uid,
            name = derivedName,
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
        cachedUserProfile = fallback
        saveProfileToPrefs(fallback)

        // Sync to Cloud Firestore
        runCatching {
            db.collection("users").document(uid).set(fallback, SetOptions.merge())
        }

        return fallback
    }

    suspend fun saveDailyLog(log: DailyLogItem): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val logId = if (log.id.isNotEmpty()) log.id else log.date.ifEmpty { System.currentTimeMillis().toString() }
            val logToSave = log.copy(id = logId)

            val currentLogs = cachedDailyLogs ?: loadLogsFromPrefs()
            currentLogs.removeAll { it.id == logId || it.date == logToSave.date }
            currentLogs.add(logToSave)
            cachedDailyLogs = currentLogs
            saveLogsToPrefs(currentLogs)

            // Push to Cloud Firestore
            runCatching {
                db.collection("users")
                    .document(uid)
                    .collection("daily_logs")
                    .document(logId)
                    .set(logToSave)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving daily log", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchDailyLogsFromCloud(): List<DailyLogItem> {
        val uid = currentUserId ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("daily_logs")
                .get()
                .await()

            val logsList = snapshot?.documents?.mapNotNull { doc ->
                if (!doc.exists()) null
                else DailyLogItem(
                    id = doc.id,
                    date = doc.getString("date") ?: doc.id,
                    flowLevel = doc.getString("flowLevel") ?: "",
                    symptoms = (doc.get("symptoms") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                    moods = (doc.get("moods") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                    medicationTaken = doc.getBoolean("medicationTaken") ?: false,
                    notes = doc.getString("notes") ?: "",
                    timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } ?: emptyList()

            if (logsList.isNotEmpty()) {
                cachedDailyLogs = logsList.toMutableList()
                saveLogsToPrefs(logsList)
            }
            logsList
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching daily logs from cloud", e)
            emptyList()
        }
    }

    suspend fun getDailyLogs(): List<DailyLogItem> {
        val uid = currentUserId ?: return emptyList()

        val authUser = auth.currentUser
        val email = authUser?.email ?: ""
        val name = authUser?.displayName ?: "Test 1"
        appDb?.seedIfEmpty(uid, email, name)

        if (cachedDailyLogs != null && cachedDailyLogs!!.isNotEmpty()) {
            return cachedDailyLogs!!
        }

        // 1. Try Local Database
        val localLogs = loadLogsFromPrefs()
        if (localLogs.isNotEmpty()) {
            cachedDailyLogs = localLogs
            // Auto-sync to Cloud Firestore in background
            runCatching {
                for (log in localLogs) {
                    db.collection("users")
                        .document(uid)
                        .collection("daily_logs")
                        .document(log.id)
                        .set(log, SetOptions.merge())
                }
            }
            return localLogs
        }

        // 2. Try Cloud Fetch
        val cloudLogs = fetchDailyLogsFromCloud()
        if (cloudLogs.isNotEmpty()) {
            cachedDailyLogs = cloudLogs.toMutableList()
            saveLogsToPrefs(cloudLogs)
            return cloudLogs
        }

        return emptyList()
    }

    fun signOut() {
        cachedUserProfile = null
        cachedDailyLogs = null
        prefs?.edit()?.clear()?.apply()
        auth.signOut()
    }
}
