package me.andresdavid.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    val goal: String = ""
)

object FirebaseRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun signUp(email: String, pass: String, name: String, goal: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("User UID is null")
            val userProfile = UserProfile(uid = uid, name = name, email = email, goal = goal)
            db.collection("users").document(uid).set(userProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val uid = currentUserId ?: return null
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveDailyLog(log: DailyLogItem): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val logId = if (log.id.isNotEmpty()) log.id else log.date.ifEmpty { System.currentTimeMillis().toString() }
            val logToSave = log.copy(id = logId)
            db.collection("users")
                .document(uid)
                .collection("daily_logs")
                .document(logId)
                .set(logToSave)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyLogs(): List<DailyLogItem> {
        val uid = currentUserId ?: return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("daily_logs")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(DailyLogItem::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
