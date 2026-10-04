package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CourseData
import com.example.data.repository.FirestoreSyncManager
import com.example.data.repository.LinguaQuestRepository
import com.example.util.AuthManager
import com.example.util.GeminiAiService
import com.example.util.SpeechManager
import com.example.util.UserAuthProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    WELCOME,
    MAIN_LEARN,
    MAIN_SPEAK,
    MAIN_EXAMS,
    MAIN_VOCAB,
    MAIN_PROFILE,
    LESSON_PLAYER,
    PLACEMENT_TEST,
    EXAM_PLAYER,
    CERTIFICATE_VIEW,
    CONTACT_DEVELOPER,
    HELP_FAQ,
    SUBSCRIPTIONS,
    AI_TUTOR,
    ADMIN_DASHBOARD
}

class LinguaQuestViewModel(application: Application) : AndroidViewModel(application) {

    val repository = LinguaQuestRepository(application)
    val speechManager = SpeechManager(application)
    val authManager = AuthManager(application)
    val firestoreSyncManager = FirestoreSyncManager(application)
    private val geminiAiService = GeminiAiService()

    private val _currentScreen = MutableStateFlow(AppScreen.WELCOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val screenStack = mutableListOf<AppScreen>()

    val selectedLanguage = repository.selectedLanguage
    val currentLevel = repository.currentLevel
    val totalXp = repository.totalXp
    val dailyXp = repository.dailyXp
    val dailyGoalXp = repository.dailyGoalXp
    val streakDays = repository.streakDays
    val completedLessons = repository.completedLessons
    val isPremium = repository.isPremium
    val speechSpeed = repository.speechSpeed
    val userCertificates = repository.userCertificates
    val savedVocabulary = repository.savedVocabulary

    // Auth & Cloud Sync State
    val currentUser: StateFlow<UserAuthProfile?> = authManager.currentUser
    val isAuthLoading: StateFlow<Boolean> = authManager.isLoading
    val authError: StateFlow<String?> = authManager.authError
    val isCloudSyncing: StateFlow<Boolean> = firestoreSyncManager.isCloudSyncing
    val lastSyncTimestamp: StateFlow<Long?> = firestoreSyncManager.lastSyncTimestamp

    // Admin & Monetization State
    val paymentRequests = repository.paymentRequests
    val registeredUsers = repository.registeredUsers
    val dailyRevenueDzd = repository.dailyRevenueDzd
    val dailyRevenueUsdt = repository.dailyRevenueUsdt
    val monthlyRevenueDzd = repository.monthlyRevenueDzd
    val monthlyRevenueUsdt = repository.monthlyRevenueUsdt
    val totalRevenueDzd = repository.totalRevenueDzd
    val totalRevenueUsdt = repository.totalRevenueUsdt

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    // AI Tutor State
    val aiMessages = repository.aiMessages
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Active screen entities
    private val _activeLesson = MutableStateFlow<Lesson?>(null)
    val activeLesson: StateFlow<Lesson?> = _activeLesson.asStateFlow()

    private val _activeExamLevel = MutableStateFlow<CefrLevel?>(null)
    val activeExamLevel: StateFlow<CefrLevel?> = _activeExamLevel.asStateFlow()

    private val _activeCertificate = MutableStateFlow<Certificate?>(null)
    val activeCertificate: StateFlow<Certificate?> = _activeCertificate.asStateFlow()

    private val _activeConversation = MutableStateFlow<ConversationTopic?>(null)
    val activeConversation: StateFlow<ConversationTopic?> = _activeConversation.asStateFlow()

    // Pronunciation interactive state
    private val _lastPronunciationScore = MutableStateFlow<Int?>(null)
    val lastPronunciationScore: StateFlow<Int?> = _lastPronunciationScore.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        // Observe auth user and initiate real-time Firestore sync
        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                if (user != null) {
                    firestoreSyncManager.startListeningToUser(user.uid) { isPremRemote, xpRemote, lvlRemote, remoteLessons ->
                        if (isPremRemote) {
                            repository.setPremium(true)
                        }
                        if (xpRemote > repository.totalXp.value) {
                            repository.addXp(xpRemote - repository.totalXp.value)
                        }
                        lvlRemote?.let { repository.selectLevel(it) }
                        remoteLessons.forEach { lessonId ->
                            repository.completeLesson(lessonId, 0)
                        }
                    }
                    syncToCloudNow()
                } else {
                    firestoreSyncManager.stopListeningToUser()
                }
            }
        }

        // Listen to payment updates from cloud
        firestoreSyncManager.startListeningToPayments { remoteList ->
            for (p in remoteList) {
                val exists = repository.paymentRequests.value.any { it.id == p.id }
                if (!exists) {
                    repository.submitPayment(
                        userName = p.userName,
                        userContact = p.userContact,
                        plan = p.plan,
                        method = p.method,
                        transactionRef = p.transactionRef,
                        receiptNotes = p.receiptNotes
                    )
                }
            }
        }
    }

    fun syncToCloudNow() {
        val user = authManager.currentUser.value ?: return
        firestoreSyncManager.syncUserProfile(
            userId = user.uid,
            userName = user.displayName,
            email = user.email,
            currentLanguageId = selectedLanguage.value.id,
            currentLevel = currentLevel.value,
            totalXp = totalXp.value,
            streakDays = streakDays.value,
            completedLessons = completedLessons.value,
            isPremium = isPremium.value
        )
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authManager.signInWithGoogle()
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            authManager.signInWithEmail(email, pass)
        }
    }

    fun registerWithEmail(name: String, email: String, pass: String) {
        viewModelScope.launch {
            authManager.registerWithEmail(name, email, pass)
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            authManager.signInAnonymously()
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    fun clearAuthError() {
        authManager.clearError()
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            val previous = screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = previous
            return true
        }
        if (_currentScreen.value != AppScreen.MAIN_LEARN) {
            _currentScreen.value = AppScreen.MAIN_LEARN
            return true
        }
        return false
    }

    fun selectLanguage(lang: SupportedLanguage) {
        repository.selectLanguage(lang)
        syncToCloudNow()
    }

    fun isLevelUnlocked(level: CefrLevel): Boolean {
        return repository.isLevelUnlocked(level)
    }

    fun selectLevel(level: CefrLevel) {
        repository.selectLevel(level)
        syncToCloudNow()
    }

    fun startLesson(lesson: Lesson) {
        if (isLevelUnlocked(lesson.level)) {
            _activeLesson.value = lesson
            navigateTo(AppScreen.LESSON_PLAYER)
        } else {
            navigateTo(AppScreen.SUBSCRIPTIONS)
        }
    }

    fun completeCurrentLesson(xpReward: Int) {
        _activeLesson.value?.let { lesson ->
            repository.completeLesson(lesson.id, xpReward)
            syncToCloudNow()
        }
    }

    fun startPlacementTest() {
        navigateTo(AppScreen.PLACEMENT_TEST)
    }

    fun applyPlacementResult(level: CefrLevel) {
        if (isLevelUnlocked(level)) {
            repository.selectLevel(level)
        } else {
            repository.selectLevel(CefrLevel.A1)
        }
        repository.addXp(50)
        syncToCloudNow()
        navigateTo(AppScreen.MAIN_LEARN)
    }

    fun startExam(level: CefrLevel) {
        if (isLevelUnlocked(level)) {
            _activeExamLevel.value = level
            navigateTo(AppScreen.EXAM_PLAYER)
        } else {
            navigateTo(AppScreen.SUBSCRIPTIONS)
        }
    }

    fun completeExam(level: CefrLevel, scorePercent: Int, studentName: String) {
        if (scorePercent >= 70) {
            val cert = Certificate(
                id = "cert_${level.name.lowercase()}_${System.currentTimeMillis()}",
                studentName = studentName.ifBlank {
                    authManager.currentUser.value?.displayName ?: "LinguaQuest Scholar"
                },
                languageName = selectedLanguage.value.name,
                level = level,
                scorePercentage = scorePercent,
                issueDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                verificationCode = "LQ-${selectedLanguage.value.id.uppercase()}-${level.code}-${(10000..99999).random()}"
            )
            repository.saveCertificate(cert)
            repository.addXp(100)
            _activeCertificate.value = cert
            syncToCloudNow()
        }
    }

    fun viewCertificate(cert: Certificate) {
        _activeCertificate.value = cert
        navigateTo(AppScreen.CERTIFICATE_VIEW)
    }

    fun startConversation(topic: ConversationTopic?) {
        _activeConversation.value = topic
    }

    fun closeConversation() {
        _activeConversation.value = null
    }

    fun playAudio(phrase: String) {
        speechManager.speak(phrase, selectedLanguage.value.localeTag, speechSpeed.value)
    }

    fun evaluatePronunciation(spoken: String, expected: String) {
        val score = SpeechManager.evaluatePronunciation(spoken, expected)
        _lastPronunciationScore.value = score
    }

    fun clearPronunciationScore() {
        _lastPronunciationScore.value = null
    }

    // AI Tutor Chat
    fun sendAiTutorMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = AiChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            message = text.trim()
        )
        repository.addAiMessage(userMsg)

        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                val aiReply = geminiAiService.getAiTutorResponse(
                    userMessage = text,
                    targetLanguage = selectedLanguage.value,
                    level = currentLevel.value,
                    history = repository.aiMessages.value
                )
                repository.addAiMessage(aiReply)
                playAudio(aiReply.message)
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    // Monetization Payments
    fun submitPayment(
        userName: String,
        userContact: String,
        plan: SubscriptionPlan,
        method: PaymentMethod,
        transactionRef: String,
        receiptNotes: String
    ): PaymentRequest {
        val req = repository.submitPayment(userName, userContact, plan, method, transactionRef, receiptNotes)
        firestoreSyncManager.uploadPaymentRequest(req, authManager.currentUser.value?.uid ?: "")
        return req
    }

    // Admin Operations
    fun verifyAdminPin(pin: String): Boolean {
        val isValid = pin.trim() == "2026" || pin.trim() == "0024"
        _isAdminAuthenticated.value = isValid
        return isValid
    }

    fun lockAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun approvePayment(requestId: String) {
        repository.approvePayment(requestId)
        val p = repository.paymentRequests.value.find { it.id == requestId }
        val targetUser = repository.registeredUsers.value.find { it.emailOrPhone == p?.userContact }
        firestoreSyncManager.updatePaymentStatus(requestId, PaymentStatus.APPROVED, targetUser?.id)
    }

    fun rejectPayment(requestId: String) {
        repository.rejectPayment(requestId)
        firestoreSyncManager.updatePaymentStatus(requestId, PaymentStatus.REJECTED)
    }

    fun manualAddUser(name: String, contact: String, level: CefrLevel, isVip: Boolean) {
        repository.manualAddUser(name, contact, level, isVip)
    }

    fun upgradeToPremium() {
        repository.setPremium(true)
        syncToCloudNow()
    }

    fun setSpeechSpeed(speed: Float) {
        repository.setSpeechSpeed(speed)
    }

    fun toggleMasteredWord(wordId: String) {
        repository.toggleMasteredWord(wordId)
    }

    fun toggleReviewWord(wordId: String) {
        repository.toggleReviewWord(wordId)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
        firestoreSyncManager.cleanup()
    }
}
