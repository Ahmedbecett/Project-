package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CourseData
import com.example.data.repository.LinguaQuestRepository
import com.example.util.SpeechManager
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
    SUBSCRIPTIONS
}

class LinguaQuestViewModel(application: Application) : AndroidViewModel(application) {

    val repository = LinguaQuestRepository(application)
    val speechManager = SpeechManager(application)

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
    }

    fun selectLevel(level: CefrLevel) {
        repository.selectLevel(level)
    }

    fun startLesson(lesson: Lesson) {
        _activeLesson.value = lesson
        navigateTo(AppScreen.LESSON_PLAYER)
    }

    fun completeCurrentLesson(xpReward: Int) {
        _activeLesson.value?.let { lesson ->
            repository.completeLesson(lesson.id, xpReward)
        }
    }

    fun startPlacementTest() {
        navigateTo(AppScreen.PLACEMENT_TEST)
    }

    fun applyPlacementResult(level: CefrLevel) {
        repository.selectLevel(level)
        repository.addXp(50)
        navigateTo(AppScreen.MAIN_LEARN)
    }

    fun startExam(level: CefrLevel) {
        _activeExamLevel.value = level
        navigateTo(AppScreen.EXAM_PLAYER)
    }

    fun completeExam(level: CefrLevel, scorePercent: Int, studentName: String) {
        if (scorePercent >= 70) {
            val cert = Certificate(
                id = "cert_${level.name.lowercase()}_${System.currentTimeMillis()}",
                studentName = studentName.ifBlank { "LinguaQuest Scholar" },
                languageName = selectedLanguage.value.name,
                level = level,
                scorePercentage = scorePercent,
                issueDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                verificationCode = "LQ-${selectedLanguage.value.id.uppercase()}-${level.code}-${(10000..99999).random()}"
            )
            repository.saveCertificate(cert)
            repository.addXp(100)
            _activeCertificate.value = cert
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

    fun upgradeToPremium() {
        repository.setPremium(true)
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
    }
}
