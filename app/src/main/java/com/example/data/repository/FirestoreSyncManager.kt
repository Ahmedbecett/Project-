package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class FirestoreSyncManager(private val context: Context) {

    private val tag = "FirestoreSyncManager"
    private var firestore: FirebaseFirestore? = null
    private var userDocListener: ListenerRegistration? = null
    private var paymentsListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _firestorePayments = MutableStateFlow<List<PaymentRequest>>(emptyList())
    val firestorePayments: StateFlow<List<PaymentRequest>> = _firestorePayments.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "Firestore not initialized or running in offline mode: ${e.message}")
        }
    }

    fun startListeningToUser(
        userId: String,
        onRemoteUserLoaded: (isPremium: Boolean, totalXp: Int, currentLevel: CefrLevel?, completedLessons: Set<String>) -> Unit
    ) {
        if (userId.isBlank()) return
        userDocListener?.remove()

        try {
            val db = firestore ?: return
            userDocListener = db.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Error listening to user document: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val isPremium = snapshot.getBoolean("isPremium") ?: false
                        val totalXp = snapshot.getLong("totalXp")?.toInt() ?: 0
                        val levelCode = snapshot.getString("currentLevel")
                        val level = levelCode?.let { code ->
                            CefrLevel.values().find { it.code.equals(code, ignoreCase = true) }
                        }
                        @Suppress("UNCHECKED_CAST")
                        val lessonsList = snapshot.get("completedLessons") as? List<String> ?: emptyList()

                        onRemoteUserLoaded(isPremium, totalXp, level, lessonsList.toSet())
                        _lastSyncTimestamp.value = System.currentTimeMillis()
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to listen to user: ${e.message}", e)
        }
    }

    fun stopListeningToUser() {
        userDocListener?.remove()
        userDocListener = null
    }

    fun startListeningToPayments(onPaymentsUpdated: (List<PaymentRequest>) -> Unit) {
        paymentsListener?.remove()
        try {
            val db = firestore ?: return
            paymentsListener = db.collection("payments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Error listening to payments: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = mutableListOf<PaymentRequest>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val userName = doc.getString("userName") ?: "Learner"
                                val userContact = doc.getString("userContact") ?: ""
                                val planId = doc.getString("planId") ?: SubscriptionPlan.MONTHLY_1.id
                                val plan = SubscriptionPlan.values().find { it.id == planId } ?: SubscriptionPlan.MONTHLY_1
                                val methodStr = doc.getString("method") ?: PaymentMethod.BARIDIMOB.name
                                val method = try {
                                    PaymentMethod.valueOf(methodStr)
                                } catch (e: Exception) {
                                    PaymentMethod.BARIDIMOB
                                }
                                val amountDzd = doc.getLong("amountDzd")?.toInt() ?: plan.priceDzd
                                val amountUsdt = doc.getDouble("amountUsdt") ?: plan.priceUsdt
                                val ref = doc.getString("transactionRef") ?: ""
                                val notes = doc.getString("receiptNotes") ?: ""
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                val statusStr = doc.getString("status") ?: PaymentStatus.PENDING.name
                                val status = try {
                                    PaymentStatus.valueOf(statusStr)
                                } catch (e: Exception) {
                                    PaymentStatus.PENDING
                                }

                                list.add(
                                    PaymentRequest(
                                        id = id,
                                        userName = userName,
                                        userContact = userContact,
                                        plan = plan,
                                        method = method,
                                        amountDzd = amountDzd,
                                        amountUsdt = amountUsdt,
                                        transactionRef = ref,
                                        receiptNotes = notes,
                                        timestamp = timestamp,
                                        status = status
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e(tag, "Error parsing payment document ${doc.id}: ${e.message}")
                            }
                        }
                        // Sort by newest first
                        list.sortByDescending { it.timestamp }
                        _firestorePayments.value = list
                        onPaymentsUpdated(list)
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to listen to payments: ${e.message}", e)
        }
    }

    fun syncUserProfile(
        userId: String,
        userName: String,
        email: String,
        currentLanguageId: String,
        currentLevel: CefrLevel,
        totalXp: Int,
        streakDays: Int,
        completedLessons: Set<String>,
        isPremium: Boolean
    ) {
        if (userId.isBlank()) return
        scope.launch {
            try {
                _isCloudSyncing.value = true
                val db = firestore ?: return@launch
                val data = hashMapOf(
                    "userId" to userId,
                    "userName" to userName,
                    "email" to email,
                    "languageId" to currentLanguageId,
                    "currentLevel" to currentLevel.code,
                    "totalXp" to totalXp,
                    "streakDays" to streakDays,
                    "completedLessons" to completedLessons.toList(),
                    "isPremium" to isPremium,
                    "lastActive" to System.currentTimeMillis(),
                    "lastActiveFormatted" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                )
                db.collection("users").document(userId)
                    .set(data, SetOptions.merge())
                    .await()
                _lastSyncTimestamp.value = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.w(tag, "Sync user profile failed: ${e.message}")
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun uploadPaymentRequest(payment: PaymentRequest, userId: String = "") {
        scope.launch {
            try {
                val db = firestore ?: return@launch
                val data = hashMapOf(
                    "id" to payment.id,
                    "userId" to userId,
                    "userName" to payment.userName,
                    "userContact" to payment.userContact,
                    "planId" to payment.plan.id,
                    "planTitle" to payment.plan.titleAr,
                    "method" to payment.method.name,
                    "amountDzd" to payment.amountDzd,
                    "amountUsdt" to payment.amountUsdt,
                    "transactionRef" to payment.transactionRef,
                    "receiptNotes" to payment.receiptNotes,
                    "timestamp" to payment.timestamp,
                    "status" to payment.status.name,
                    "dateFormatted" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(payment.timestamp))
                )
                db.collection("payments").document(payment.id)
                    .set(data, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(tag, "Failed to upload payment request: ${e.message}")
            }
        }
    }

    fun updatePaymentStatus(paymentId: String, newStatus: PaymentStatus, targetUserId: String? = null) {
        scope.launch {
            try {
                val db = firestore ?: return@launch
                db.collection("payments").document(paymentId)
                    .update("status", newStatus.name)
                    .await()

                if (newStatus == PaymentStatus.APPROVED && !targetUserId.isNullOrBlank()) {
                    db.collection("users").document(targetUserId)
                        .update(
                            mapOf(
                                "isPremium" to true,
                                "premiumUpgradedAt" to System.currentTimeMillis()
                            )
                        ).await()
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to update payment status: ${e.message}")
            }
        }
    }

    fun cleanup() {
        userDocListener?.remove()
        paymentsListener?.remove()
    }
}
