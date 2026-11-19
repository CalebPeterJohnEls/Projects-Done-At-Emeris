package com.example.funinactionproto

import com.example.funinactionproto.pose.Achievement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

// Represents one player's leaderboard entry.
data class LeaderboardEntry(
    val id: String,
    val nickname: String,
    val averageLevel: Int
)

object LeaderboardRepository {

    private const val COLLECTION = "scores"

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    /**
     * Submits the player's overall achievement level to Firestore.
     */
    fun submit(
        displayName: String,
        achievement: Achievement,
        onDone: (Result<String>) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            write(displayName, achievement, onDone)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    write(displayName, achievement, onDone)
                }
                .addOnFailureListener {
                    onDone(Result.failure(it))
                }
        }
    }

    private fun write(
        displayName: String,
        achievement: Achievement,
        onDone: (Result<String>) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            onDone(Result.failure(Exception("User authentication failed.")))
            return
        }

        val data = hashMapOf<String, Any>(
            "displayName" to displayName,
            "score" to achievement.level.level,
            "updatedAt" to FieldValue.serverTimestamp(),
            "userID" to userId
        )

        db.collection(COLLECTION)
            .add(data)
            .addOnSuccessListener {
                onDone(Result.success(it.id))
            }
            .addOnFailureListener {
                onDone(Result.failure(it))
            }
    }

    /**
     * Listens for the highest-scoring players.
     */
    fun listenTop(
        limit: Long,
        onUpdate: (List<LeaderboardEntry>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {

        return db.collection(COLLECTION)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val entries: List<LeaderboardEntry> =
                    snapshot?.documents?.mapNotNull { document ->
                        val name = document.getString("displayName")
                            ?: return@mapNotNull null

                        val score = document.getLong("score")?.toInt()
                            ?: return@mapNotNull null

                        LeaderboardEntry(
                            id = document.id,
                            nickname = name,
                            averageLevel = score
                        )
                    } ?: emptyList()

                onUpdate(entries)
            }
    }
}