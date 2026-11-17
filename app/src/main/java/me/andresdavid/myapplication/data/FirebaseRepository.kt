package me.andresdavid.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject

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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var prefs: SharedPreferences? = null
    private var cachedUserProfile: UserProfile? = null
    private var cachedDailyLogs: MutableList<DailyLogItem>? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("seluna_local_cache", Context.MODE_PRIVATE)
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

            return "Usuario"
        }

    private fun saveProfileToPrefs(profile: UserProfile) {
        val p = prefs ?: return
        p.edit().apply {
            putString("uid", profile.uid)
            putString("name", profile.name)
            putString("email", profile.email)
            putString("goal", profile.goal)
            putString("approach", profile.approach)
            putString("regularity", profile.regularity)
            putString("contraceptive", profile.contraceptive)
            putInt("periodDays", profile.periodDays)
            putString("conditions", profile.conditions.joinToString(";;;"))
            putString("priorities", profile.priorities.joinToString(";;;"))
            putBoolean("notificationsEnabled", profile.notificationsEnabled)
            apply()
        }
    }

    private fun loadProfileFromPrefs(): UserProfile? {
        val p = prefs ?: return null
        val uid = p.getString("uid", "") ?: ""
        if (uid.isBlank()) return null
        val name = p.getString("name", "") ?: ""
        val email = p.getString("email", "") ?: ""
        val goal = p.getString("goal", "") ?: ""
        val approach = p.getString("approach", "TRACK_CYCLE") ?: "TRACK_CYCLE"
        val regularity = p.getString("regularity", "REGULAR") ?: "REGULAR"
        val contraceptive = p.getString("contraceptive", "NONE") ?: "NONE"
        val periodDays = p.getInt("periodDays", 5)
        val conditionsStr = p.getString("conditions", "") ?: ""
        val conditions = if (conditionsStr.isNotBlank()) conditionsStr.split(";;;") else emptyList()
        val prioritiesStr = p.getString("priorities", "") ?: ""
        val priorities = if (prioritiesStr.isNotBlank()) prioritiesStr.split(";;;") else emptyList()
        val notificationsEnabled = p.getBoolean("notificationsEnabled", true)

        return UserProfile(
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
    }

    private fun saveLogsToPrefs(logs: List<DailyLogItem>) {
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
        p.edit().putString("saved_daily_logs", jsonArray.toString()).apply()
    }

    private fun loadLogsFromPrefs(): MutableList<DailyLogItem> {
        val p = prefs ?: return mutableListOf()
        val raw = p.getString("saved_daily_logs", "") ?: ""
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
                list.add(
                    DailyLogItem(
                        id = obj.optString("id", ""),
                        date = obj.optString("date", ""),
                        flowLevel = obj.optString("flowLevel", ""),
                        symptoms = symptomsList,
                        moods = moodsList,
                        medicationTaken = obj.optBoolean("medicationTaken", false),
                        notes = obj.optString("notes", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
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

            withTimeoutOrNull(3000L) {
                db.collection("users").document(uid).set(userProfile).await()
            } ?: db.collection("users").document(uid).set(userProfile)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<Unit> {
        return try {
            cachedUserProfile = null
            cachedDailyLogs = null
            auth.signInWithEmailAndPassword(email, pass).await()
            getUserProfile()
            getDailyLogs()
            Result.success(Unit)
        } catch (e: Exception) {
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
            withTimeoutOrNull(3000L) {
                db.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
            } ?: db.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge())

            Result.success(Unit)
        } catch (e: Exception) {
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
                priorities = priorities,
                notificationsEnabled = notificationsEnabled
            )
            cachedUserProfile = updated
            saveProfileToPrefs(updated)

            val updates = mapOf(
                "priorities" to priorities,
                "notificationsEnabled" to notificationsEnabled
            )
            withTimeoutOrNull(3000L) {
                db.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
            } ?: db.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge())

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val uid = currentUserId ?: return null

        if (cachedUserProfile != null && cachedUserProfile?.uid == uid && cachedUserProfile!!.name.isNotBlank() && cachedUserProfile!!.name != "Usuario") {
            return cachedUserProfile
        }

        val localProfile = loadProfileFromPrefs()
        if (localProfile != null && localProfile.uid == uid && localProfile.name.isNotBlank()) {
            cachedUserProfile = localProfile
        }

        val authUser = auth.currentUser
        val email = authUser?.email ?: ""
        val derivedName = authUser?.displayName?.ifBlank { null }
            ?: email.substringBefore("@").takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
            ?: "Usuario"

        return try {
            val docSnapshot = withTimeoutOrNull(8000L) {
                db.collection("users").document(uid).get().await()
            }

            val profile = docSnapshot?.let { doc ->
                if (!doc.exists()) null
                else UserProfile(
                    uid = uid,
                    name = doc.getString("name") ?: derivedName,
                    email = doc.getString("email") ?: email,
                    goal = doc.getString("goal") ?: "",
                    approach = doc.getString("approach") ?: "TRACK_CYCLE",
                    regularity = doc.getString("regularity") ?: "REGULAR",
                    contraceptive = doc.getString("contraceptive") ?: "NONE",
                    periodDays = (doc.get("periodDays") as? Number)?.toInt() ?: 5,
                    conditions = (doc.get("conditions") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                    priorities = (doc.get("priorities") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                    notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true
                )
            }

            if (profile != null) {
                val finalProfile = profile.copy(
                    uid = uid,
                    name = profile.name.ifBlank { derivedName },
                    email = profile.email.ifBlank { email }
                )
                cachedUserProfile = finalProfile
                saveProfileToPrefs(finalProfile)
                finalProfile
            } else if (cachedUserProfile != null) {
                cachedUserProfile
            } else {
                val fallback = UserProfile(uid = uid, name = derivedName, email = email)
                cachedUserProfile = fallback
                saveProfileToPrefs(fallback)
                fallback
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (cachedUserProfile != null) {
                cachedUserProfile
            } else {
                val fallback = UserProfile(uid = uid, name = derivedName, email = email)
                cachedUserProfile = fallback
                saveProfileToPrefs(fallback)
                fallback
            }
        }
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

            withTimeoutOrNull(3000L) {
                db.collection("users")
                    .document(uid)
                    .collection("daily_logs")
                    .document(logId)
                    .set(logToSave)
                    .await()
            } ?: db.collection("users")
                .document(uid)
                .collection("daily_logs")
                .document(logId)
                .set(logToSave)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyLogs(): List<DailyLogItem> {
        val uid = currentUserId ?: return emptyList()

        if (cachedDailyLogs != null && cachedDailyLogs!!.isNotEmpty()) {
            return cachedDailyLogs!!
        }

        val localLogs = loadLogsFromPrefs()
        if (localLogs.isNotEmpty()) {
            cachedDailyLogs = localLogs
        }

        return try {
            val snapshot = withTimeoutOrNull(8000L) {
                db.collection("users")
                    .document(uid)
                    .collection("daily_logs")
                    .get()
                    .await()
            }

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

            cachedDailyLogs ?: logsList
        } catch (e: Exception) {
            e.printStackTrace()
            cachedDailyLogs ?: emptyList()
        }
    }

    fun signOut() {
        cachedUserProfile = null
        cachedDailyLogs = null
        prefs?.edit()?.clear()?.apply()
        auth.signOut()
    }
}
