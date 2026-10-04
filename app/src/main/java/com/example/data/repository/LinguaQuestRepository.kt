package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LinguaQuestRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("linguaquest_user_prefs", Context.MODE_PRIVATE)

    private val _selectedLanguage = MutableStateFlow(
        CourseData.supportedLanguages.firstOrNull { it.id == prefs.getString("selected_lang", "es") }
            ?: CourseData.supportedLanguages[0]
    )
    val selectedLanguage: StateFlow<SupportedLanguage> = _selectedLanguage.asStateFlow()

    private val _currentLevel = MutableStateFlow(
        try {
            CefrLevel.valueOf(prefs.getString("current_level", CefrLevel.A1.name) ?: CefrLevel.A1.name)
        } catch (e: Exception) {
            CefrLevel.A1
        }
    )
    val currentLevel: StateFlow<CefrLevel> = _currentLevel.asStateFlow()

    // Real learner progress: Starts at 0 for real genuine users
    private val _totalXp = MutableStateFlow(prefs.getInt("total_xp", 0))
    val totalXp: StateFlow<Int> = _totalXp.asStateFlow()

    private val _dailyXp = MutableStateFlow(prefs.getInt("daily_xp", 0))
    val dailyXp: StateFlow<Int> = _dailyXp.asStateFlow()

    private val _dailyGoalXp = MutableStateFlow(prefs.getInt("daily_goal_xp", 50))
    val dailyGoalXp: StateFlow<Int> = _dailyGoalXp.asStateFlow()

    private val _streakDays = MutableStateFlow(prefs.getInt("streak_days", 0))
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _completedLessons = MutableStateFlow(
        prefs.getStringSet("completed_lessons", emptySet()) ?: emptySet()
    )
    val completedLessons: StateFlow<Set<String>> = _completedLessons.asStateFlow()

    private val _isPremium = MutableStateFlow(prefs.getBoolean("is_premium", false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _speechSpeed = MutableStateFlow(prefs.getFloat("speech_speed", 1.0f))
    val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()

    // Real earned certificates only (empty if user has not passed an exam yet)
    private val _userCertificates = MutableStateFlow<List<Certificate>>(loadCertificates())
    val userCertificates: StateFlow<List<Certificate>> = _userCertificates.asStateFlow()

    private val _savedVocabulary = MutableStateFlow<List<VocabularyWord>>(loadSavedVocabulary())
    val savedVocabulary: StateFlow<List<VocabularyWord>> = _savedVocabulary.asStateFlow()

    // Real Monetization & Admin Ledger State (Strictly real transactions only)
    private val _paymentRequests = MutableStateFlow<List<PaymentRequest>>(loadPaymentRequests())
    val paymentRequests: StateFlow<List<PaymentRequest>> = _paymentRequests.asStateFlow()

    // Real Registered Users only (Strictly real accounts created in the app or synced from database)
    private val _registeredUsers = MutableStateFlow<List<RegisteredUser>>(loadRegisteredUsers())
    val registeredUsers: StateFlow<List<RegisteredUser>> = _registeredUsers.asStateFlow()

    // Revenue calculations (live computed strictly from approved real payments)
    private val _dailyRevenueDzd = MutableStateFlow(0)
    val dailyRevenueDzd: StateFlow<Int> = _dailyRevenueDzd.asStateFlow()

    private val _dailyRevenueUsdt = MutableStateFlow(0.0)
    val dailyRevenueUsdt: StateFlow<Double> = _dailyRevenueUsdt.asStateFlow()

    private val _monthlyRevenueDzd = MutableStateFlow(0)
    val monthlyRevenueDzd: StateFlow<Int> = _monthlyRevenueDzd.asStateFlow()

    private val _monthlyRevenueUsdt = MutableStateFlow(0.0)
    val monthlyRevenueUsdt: StateFlow<Double> = _monthlyRevenueUsdt.asStateFlow()

    private val _totalRevenueDzd = MutableStateFlow(0)
    val totalRevenueDzd: StateFlow<Int> = _totalRevenueDzd.asStateFlow()

    private val _totalRevenueUsdt = MutableStateFlow(0.0)
    val totalRevenueUsdt: StateFlow<Double> = _totalRevenueUsdt.asStateFlow()

    // AI Messages
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                id = "ai_welcome",
                isUser = false,
                message = "¡Hola! Soy tu tutor de idiomas con inteligencia artificial de LinguaQuest. ¿Sobre qué te gustaría conversar hoy?",
                translation = "Hello! I am your LinguaQuest AI language tutor. What would you like to talk about today?",
                grammarTips = "Consejo: Puedes preguntarme sobre gramática, practicar situaciones de viaje o pedirme que corrija tus frases."
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    init {
        // Sanitize and purge any old legacy seed/mock data from SharedPreferences
        val storedUsers = prefs.getString("persisted_users", null)
        if (storedUsers != null && (storedUsers.contains("pay_seed") || storedUsers.contains("Karim Benali") || storedUsers.contains("Sofia Martinez") || storedUsers.contains("Yacine Belkacem"))) {
            prefs.edit().remove("persisted_users").apply()
            _registeredUsers.value = emptyList()
        }

        val storedPayments = prefs.getString("persisted_payments", null)
        if (storedPayments != null && storedPayments.contains("pay_seed")) {
            prefs.edit().remove("persisted_payments").apply()
            _paymentRequests.value = emptyList()
        }

        val storedCerts = prefs.getStringSet("saved_certs", null)
        if (storedCerts != null && storedCerts.any { it.contains("cert_a1_demo") }) {
            val sanitized = storedCerts.filterNot { it.contains("cert_a1_demo") }.toSet()
            prefs.edit().putStringSet("saved_certs", sanitized).apply()
            _userCertificates.value = sanitized.mapNotNull { parseCertificate(it) }
        }

        // Sanitize legacy default values if present
        if (prefs.getInt("total_xp", -1) == 120 && prefs.getInt("daily_xp", -1) == 35) {
            prefs.edit()
                .putInt("total_xp", 0)
                .putInt("daily_xp", 0)
                .putInt("streak_days", 0)
                .remove("completed_lessons")
                .apply()
            _totalXp.value = 0
            _dailyXp.value = 0
            _streakDays.value = 0
            _completedLessons.value = emptySet()
        }

        recalculateLedger()
    }

    // Access control: Only Level A1 is free!
    fun isLevelUnlocked(level: CefrLevel): Boolean {
        return level == CefrLevel.A1 || _isPremium.value
    }

    fun selectLanguage(lang: SupportedLanguage) {
        _selectedLanguage.value = lang
        prefs.edit().putString("selected_lang", lang.id).apply()
    }

    fun selectLevel(level: CefrLevel) {
        _currentLevel.value = level
        prefs.edit().putString("current_level", level.name).apply()
    }

    fun addXp(amount: Int) {
        val newTotal = _totalXp.value + amount
        val newDaily = _dailyXp.value + amount
        _totalXp.value = newTotal
        _dailyXp.value = newDaily
        prefs.edit()
            .putInt("total_xp", newTotal)
            .putInt("daily_xp", newDaily)
            .apply()
    }

    fun completeLesson(lessonId: String, xpReward: Int) {
        val updated = _completedLessons.value.toMutableSet()
        updated.add(lessonId)
        _completedLessons.value = updated
        prefs.edit().putStringSet("completed_lessons", updated).apply()
        if (xpReward > 0) {
            addXp(xpReward)
        }
    }

    fun setPremium(premium: Boolean) {
        _isPremium.value = premium
        prefs.edit().putBoolean("is_premium", premium).apply()
    }

    fun setSpeechSpeed(speed: Float) {
        _speechSpeed.value = speed
        prefs.edit().putFloat("speech_speed", speed).apply()
    }

    fun saveCertificate(certificate: Certificate) {
        val updated = _userCertificates.value.toMutableList()
        updated.removeAll { it.id == certificate.id }
        updated.add(0, certificate)
        _userCertificates.value = updated
        persistCertificates(updated)
    }

    private fun persistCertificates(list: List<Certificate>) {
        val encoded = list.map { "${it.id}::${it.studentName}::${it.languageName}::${it.level.name}::${it.scorePercentage}::${it.issueDate}::${it.verificationCode}" }
        prefs.edit().putStringSet("saved_certs", encoded.toSet()).apply()
    }

    private fun parseCertificate(encoded: String): Certificate? {
        val parts = encoded.split("::")
        return if (parts.size >= 7) {
            Certificate(
                id = parts[0],
                studentName = parts[1],
                languageName = parts[2],
                level = try { CefrLevel.valueOf(parts[3]) } catch (e: Exception) { CefrLevel.A1 },
                scorePercentage = parts[4].toIntOrNull() ?: 70,
                issueDate = parts[5],
                verificationCode = parts[6]
            )
        } else null
    }

    private fun loadCertificates(): List<Certificate> {
        val set = prefs.getStringSet("saved_certs", null) ?: return emptyList()
        return set.mapNotNull { parseCertificate(it) }
    }

    private fun loadSavedVocabulary(): List<VocabularyWord> {
        return CourseData.sampleVocabulary
    }

    fun toggleMasteredWord(wordId: String) {
        _savedVocabulary.value = _savedVocabulary.value.map {
            if (it.id == wordId) it.copy(mastered = !it.mastered) else it
        }
    }

    fun toggleReviewWord(wordId: String) {
        _savedVocabulary.value = _savedVocabulary.value.map {
            if (it.id == wordId) it.copy(needsReview = !it.needsReview) else it
        }
    }

    // --- Real Payment Submission ---

    fun submitPayment(
        userName: String,
        userContact: String,
        plan: SubscriptionPlan,
        method: PaymentMethod,
        transactionRef: String,
        receiptNotes: String
    ): PaymentRequest {
        val request = PaymentRequest(
            id = "pay_${System.currentTimeMillis()}_${(100..999).random()}",
            userName = userName.trim(),
            userContact = userContact.trim(),
            plan = plan,
            method = method,
            amountDzd = plan.priceDzd,
            amountUsdt = plan.priceUsdt,
            transactionRef = transactionRef.trim(),
            receiptNotes = receiptNotes.trim(),
            timestamp = System.currentTimeMillis(),
            status = PaymentStatus.PENDING
        )

        val updated = _paymentRequests.value.toMutableList()
        updated.removeAll { it.id == request.id }
        updated.add(0, request)
        _paymentRequests.value = updated
        persistPaymentRequests(updated)

        // Ensure user is registered in the database list
        registerOrUpdateUser(
            userId = "usr_${System.currentTimeMillis()}",
            name = request.userName,
            contact = request.userContact,
            isVip = false
        )
        return request
    }

    // --- Admin Operations: Approve / Reject / Manual Activation / Delete User ---

    fun approvePayment(requestId: String) {
        val updated = _paymentRequests.value.map { req ->
            if (req.id == requestId) {
                req.copy(status = PaymentStatus.APPROVED)
            } else req
        }
        _paymentRequests.value = updated
        persistPaymentRequests(updated)

        // Unlock premium for current user session
        setPremium(true)

        // Mark user as active premium in registered list
        val targetReq = updated.firstOrNull { it.id == requestId }
        targetReq?.let { r ->
            _registeredUsers.value = _registeredUsers.value.map { u ->
                if (u.name.equals(r.userName, ignoreCase = true) || u.emailOrPhone.equals(r.userContact, ignoreCase = true)) {
                    u.copy(isPremium = true, premiumExpiryDate = "Active: ${r.plan.titleEn}")
                } else u
            }
            persistRegisteredUsers(_registeredUsers.value)
        }

        recalculateLedger()
    }

    fun rejectPayment(requestId: String) {
        val updated = _paymentRequests.value.map { req ->
            if (req.id == requestId) {
                req.copy(status = PaymentStatus.REJECTED)
            } else req
        }
        _paymentRequests.value = updated
        persistPaymentRequests(updated)
        recalculateLedger()
    }

    fun deleteUser(userId: String) {
        val updated = _registeredUsers.value.filterNot { it.id == userId }
        _registeredUsers.value = updated
        persistRegisteredUsers(updated)
    }

    fun manualAddUser(name: String, contact: String, level: CefrLevel, isVip: Boolean) {
        val newUser = RegisteredUser(
            id = "usr_${System.currentTimeMillis()}_${(100..999).random()}",
            name = name.trim(),
            emailOrPhone = contact.trim(),
            registrationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            currentLevel = level,
            isPremium = isVip,
            premiumExpiryDate = if (isVip) "Active VIP (Manual)" else "Free Tier",
            totalXp = 0
        )
        val updated = _registeredUsers.value.toMutableList()
        updated.removeAll { it.emailOrPhone.equals(contact.trim(), ignoreCase = true) }
        updated.add(0, newUser)
        _registeredUsers.value = updated
        persistRegisteredUsers(updated)
    }

    fun registerOrUpdateUser(
        userId: String = "usr_${System.currentTimeMillis()}",
        name: String,
        contact: String,
        isVip: Boolean = false,
        xp: Int = 0
    ) {
        if (name.isBlank() && contact.isBlank()) return
        val currentList = _registeredUsers.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.id == userId || (it.emailOrPhone.isNotBlank() && it.emailOrPhone.equals(contact, ignoreCase = true))
        }

        val updatedUser = if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            existing.copy(
                name = name.ifBlank { existing.name },
                emailOrPhone = contact.ifBlank { existing.emailOrPhone },
                isPremium = isVip || existing.isPremium,
                totalXp = maxOf(existing.totalXp, xp)
            )
        } else {
            RegisteredUser(
                id = userId,
                name = name.ifBlank { contact.substringBefore("@") },
                emailOrPhone = contact,
                registrationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                currentLevel = _currentLevel.value,
                isPremium = isVip,
                premiumExpiryDate = if (isVip) "Active VIP" else "Free Tier",
                totalXp = xp
            )
        }

        if (existingIndex >= 0) {
            currentList[existingIndex] = updatedUser
        } else {
            currentList.add(0, updatedUser)
        }
        _registeredUsers.value = currentList
        persistRegisteredUsers(currentList)
    }

    fun setRegisteredUsersFromCloud(remoteUsers: List<RegisteredUser>) {
        _registeredUsers.value = remoteUsers
        persistRegisteredUsers(remoteUsers)
    }

    fun setPaymentRequestsFromCloud(remotePayments: List<PaymentRequest>) {
        _paymentRequests.value = remotePayments
        persistPaymentRequests(remotePayments)
        recalculateLedger()
    }

    // Live financial recalculation: strictly calculated from actual approved payments
    private fun recalculateLedger() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val thirtyDaysMillis = 30L * oneDayMillis

        var dailyDzd = 0
        var dailyUsdt = 0.0
        var monthlyDzd = 0
        var monthlyUsdt = 0.0
        var totalDzd = 0
        var totalUsdt = 0.0

        for (req in _paymentRequests.value) {
            if (req.status != PaymentStatus.APPROVED) continue

            totalDzd += req.amountDzd
            totalUsdt += req.amountUsdt

            val age = now - req.timestamp
            if (age <= thirtyDaysMillis) {
                monthlyDzd += req.amountDzd
                monthlyUsdt += req.amountUsdt
            }
            if (age <= oneDayMillis) {
                dailyDzd += req.amountDzd
                dailyUsdt += req.amountUsdt
            }
        }

        _dailyRevenueDzd.value = dailyDzd
        _dailyRevenueUsdt.value = dailyUsdt
        _monthlyRevenueDzd.value = monthlyDzd
        _monthlyRevenueUsdt.value = monthlyUsdt
        _totalRevenueDzd.value = totalDzd
        _totalRevenueUsdt.value = totalUsdt
    }

    private fun persistPaymentRequests(list: List<PaymentRequest>) {
        val jsonArray = JSONArray()
        list.forEach { req ->
            val obj = JSONObject().apply {
                put("id", req.id)
                put("userName", req.userName)
                put("userContact", req.userContact)
                put("plan", req.plan.name)
                put("method", req.method.name)
                put("amountDzd", req.amountDzd)
                put("amountUsdt", req.amountUsdt)
                put("transactionRef", req.transactionRef)
                put("receiptNotes", req.receiptNotes)
                put("timestamp", req.timestamp)
                put("status", req.status.name)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("persisted_payments", jsonArray.toString()).apply()
    }

    private fun loadPaymentRequests(): List<PaymentRequest> {
        val raw = prefs.getString("persisted_payments", null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<PaymentRequest>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    PaymentRequest(
                        id = obj.getString("id"),
                        userName = obj.getString("userName"),
                        userContact = obj.getString("userContact"),
                        plan = SubscriptionPlan.valueOf(obj.getString("plan")),
                        method = PaymentMethod.valueOf(obj.getString("method")),
                        amountDzd = obj.getInt("amountDzd"),
                        amountUsdt = obj.getDouble("amountUsdt"),
                        transactionRef = obj.getString("transactionRef"),
                        receiptNotes = obj.optString("receiptNotes", ""),
                        timestamp = obj.getLong("timestamp"),
                        status = PaymentStatus.valueOf(obj.getString("status"))
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistRegisteredUsers(list: List<RegisteredUser>) {
        val jsonArray = JSONArray()
        list.forEach { u ->
            val obj = JSONObject().apply {
                put("id", u.id)
                put("name", u.name)
                put("emailOrPhone", u.emailOrPhone)
                put("registrationDate", u.registrationDate)
                put("currentLevel", u.currentLevel.name)
                put("isPremium", u.isPremium)
                put("premiumExpiryDate", u.premiumExpiryDate)
                put("totalXp", u.totalXp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("persisted_users", jsonArray.toString()).apply()
    }

    private fun loadRegisteredUsers(): List<RegisteredUser> {
        val raw = prefs.getString("persisted_users", null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<RegisteredUser>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RegisteredUser(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        emailOrPhone = obj.getString("emailOrPhone"),
                        registrationDate = obj.getString("registrationDate"),
                        currentLevel = try { CefrLevel.valueOf(obj.getString("currentLevel")) } catch (e: Exception) { CefrLevel.A1 },
                        isPremium = obj.getBoolean("isPremium"),
                        premiumExpiryDate = obj.getString("premiumExpiryDate"),
                        totalXp = obj.getInt("totalXp")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // AI Messages
    fun addAiMessage(msg: AiChatMessage) {
        val updated = _aiMessages.value.toMutableList()
        updated.add(msg)
        _aiMessages.value = updated
    }

    fun clearAiChat() {
        _aiMessages.value = emptyList()
    }

    fun getAchievements(): List<Achievement> {
        val xp = _totalXp.value
        val completedCount = _completedLessons.value.size
        val streak = _streakDays.value
        val certsCount = _userCertificates.value.size

        return listOf(
            Achievement("ach_1", "First Step", "Complete your first language lesson", "🌱", minOf(completedCount, 1), 1, completedCount >= 1, 50),
            Achievement("ach_2", "Polyglot Apprentice", "Reach Level A2 or higher", "🌟", if (_currentLevel.value.ordinal >= CefrLevel.A2.ordinal) 1 else 0, 1, _currentLevel.value.ordinal >= CefrLevel.A2.ordinal, 100),
            Achievement("ach_3", "Streak Flame", "Maintain a 7-day study streak", "🔥", minOf(streak, 7), 7, streak >= 7, 150),
            Achievement("ach_4", "Certified Scholar", "Earn an official LinguaQuest Certificate", "📜", minOf(certsCount, 1), 1, certsCount >= 1, 200),
            Achievement("ach_5", "XP Master 500", "Accumulate over 500 Total XP", "⚡", minOf(xp, 500), 500, xp >= 500, 250),
            Achievement("ach_6", "Fluent Speaker", "Practice 5 real-life conversation situations", "🗣️", 0, 5, false, 120)
        )
    }

    // Leaderboard strictly derived from genuine registered learners only! No mock profiles!
    fun getLeaderboard(currentUserId: String? = null): List<LeaderboardUser> {
        val flag = _selectedLanguage.value.flagEmoji

        // Take only genuinely registered users from database
        val realUsers = _registeredUsers.value.filter { it.totalXp > 0 || it.name.isNotBlank() }

        if (realUsers.isEmpty()) {
            return emptyList()
        }

        val mapped = realUsers.map { u ->
            LeaderboardUser(
                rank = 0,
                name = u.name,
                countryFlag = flag,
                xp = u.totalXp,
                avatarInitial = u.name.take(1).ifBlank { "U" }.uppercase(),
                isCurrentUser = (currentUserId != null && u.id == currentUserId)
            )
        }.toMutableList()

        mapped.sortByDescending { it.xp }
        return mapped.mapIndexed { index, user ->
            user.copy(rank = index + 1)
        }
    }
}
