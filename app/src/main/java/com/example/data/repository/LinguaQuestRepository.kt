package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class LinguaQuestRepository(private val context: Context) {

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

    private val _totalXp = MutableStateFlow(prefs.getInt("total_xp", 120))
    val totalXp: StateFlow<Int> = _totalXp.asStateFlow()

    private val _dailyXp = MutableStateFlow(prefs.getInt("daily_xp", 35))
    val dailyXp: StateFlow<Int> = _dailyXp.asStateFlow()

    private val _dailyGoalXp = MutableStateFlow(prefs.getInt("daily_goal_xp", 50))
    val dailyGoalXp: StateFlow<Int> = _dailyGoalXp.asStateFlow()

    private val _streakDays = MutableStateFlow(prefs.getInt("streak_days", 4))
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _completedLessons = MutableStateFlow(
        prefs.getStringSet("completed_lessons", setOf("es_a1_1")) ?: setOf("es_a1_1")
    )
    val completedLessons: StateFlow<Set<String>> = _completedLessons.asStateFlow()

    private val _isPremium = MutableStateFlow(prefs.getBoolean("is_premium", false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _speechSpeed = MutableStateFlow(prefs.getFloat("speech_speed", 1.0f))
    val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()

    private val _userCertificates = MutableStateFlow<List<Certificate>>(loadCertificates())
    val userCertificates: StateFlow<List<Certificate>> = _userCertificates.asStateFlow()

    private val _savedVocabulary = MutableStateFlow<List<VocabularyWord>>(CourseData.sampleVocabulary)
    val savedVocabulary: StateFlow<List<VocabularyWord>> = _savedVocabulary.asStateFlow()

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
        addXp(xpReward)
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

    private fun loadCertificates(): List<Certificate> {
        val set = prefs.getStringSet("saved_certs", null) ?: return listOf(
            Certificate(
                id = "cert_a1_demo",
                studentName = "Ahmed Becetti",
                languageName = "Spanish",
                level = CefrLevel.A1,
                scorePercentage = 95,
                issueDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                verificationCode = "LQ-ES-A1-94827"
            )
        )
        return set.mapNotNull {
            val parts = it.split("::")
            if (parts.size >= 7) {
                Certificate(
                    id = parts[0],
                    studentName = parts[1],
                    languageName = parts[2],
                    level = CefrLevel.valueOf(parts[3]),
                    scorePercentage = parts[4].toIntOrNull() ?: 90,
                    issueDate = parts[5],
                    verificationCode = parts[6]
                )
            } else null
        }
    }

    fun toggleMasteredWord(wordId: String) {
        _savedVocabulary.value = _savedVocabulary.value.map {
            if (it.id == wordId) it.copy(mastered = !it.mastered, needsReview = false) else it
        }
    }

    fun toggleReviewWord(wordId: String) {
        _savedVocabulary.value = _savedVocabulary.value.map {
            if (it.id == wordId) it.copy(needsReview = !it.needsReview) else it
        }
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
            Achievement("ach_6", "Fluent Speaker", "Practice 5 real-life conversation situations", "🗣️", 3, 5, false, 120)
        )
    }

    fun getLeaderboard(): List<LeaderboardUser> {
        val currentXp = _totalXp.value
        return listOf(
            LeaderboardUser(1, "Elena Rostova", "🇪🇸", 1420, "E"),
            LeaderboardUser(2, "Ahmed Becetti", "🇩🇿", maxOf(currentXp, 980), "A", isCurrentUser = true),
            LeaderboardUser(3, "Kenji Tanaka", "🇯🇵", 920, "K"),
            LeaderboardUser(4, "Camille Laurent", "🇫🇷", 840, "C"),
            LeaderboardUser(5, "Mateo Silva", "🇧🇷", 710, "M"),
            LeaderboardUser(6, "Fatima Zahra", "🇸🇦", 650, "F")
        )
    }
}
