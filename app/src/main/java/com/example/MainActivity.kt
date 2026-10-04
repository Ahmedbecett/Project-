package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.LinguaBottomNavBar
import com.example.ui.components.LinguaTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.LinguaQuestTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.LinguaQuestViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LinguaQuestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkThemeSystem = isSystemInDarkTheme()
            val isDarkModeUser by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            val currentLevel by viewModel.currentLevel.collectAsState()
            val totalXp by viewModel.totalXp.collectAsState()
            val dailyXp by viewModel.dailyXp.collectAsState()
            val dailyGoalXp by viewModel.dailyGoalXp.collectAsState()
            val streakDays by viewModel.streakDays.collectAsState()
            val completedLessons by viewModel.completedLessons.collectAsState()
            val isPremium by viewModel.isPremium.collectAsState()
            val speechSpeed by viewModel.speechSpeed.collectAsState()
            val userCertificates by viewModel.userCertificates.collectAsState()
            val savedVocabulary by viewModel.savedVocabulary.collectAsState()
            val activeLesson by viewModel.activeLesson.collectAsState()
            val activeExamLevel by viewModel.activeExamLevel.collectAsState()
            val activeCertificate by viewModel.activeCertificate.collectAsState()
            val activeConversation by viewModel.activeConversation.collectAsState()

            // Admin & AI State
            val paymentRequests by viewModel.paymentRequests.collectAsState()
            val registeredUsers by viewModel.registeredUsers.collectAsState()
            val dailyRevenueDzd by viewModel.dailyRevenueDzd.collectAsState()
            val dailyRevenueUsdt by viewModel.dailyRevenueUsdt.collectAsState()
            val monthlyRevenueDzd by viewModel.monthlyRevenueDzd.collectAsState()
            val monthlyRevenueUsdt by viewModel.monthlyRevenueUsdt.collectAsState()
            val totalRevenueDzd by viewModel.totalRevenueDzd.collectAsState()
            val totalRevenueUsdt by viewModel.totalRevenueUsdt.collectAsState()
            val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()
            val aiMessages by viewModel.aiMessages.collectAsState()
            val isAiThinking by viewModel.isAiThinking.collectAsState()

            LinguaQuestTheme(darkTheme = isDarkModeUser || isDarkThemeSystem) {
                // Handle system back button for all sub-screens
                BackHandler(enabled = currentScreen != AppScreen.MAIN_LEARN) {
                    viewModel.navigateBack()
                }

                val isMainTabScreen = when (currentScreen) {
                    AppScreen.MAIN_LEARN,
                    AppScreen.MAIN_SPEAK,
                    AppScreen.MAIN_EXAMS,
                    AppScreen.MAIN_VOCAB,
                    AppScreen.MAIN_PROFILE -> true
                    else -> false
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (isMainTabScreen) {
                            LinguaTopAppBar(
                                currentLanguage = selectedLanguage,
                                currentLevel = currentLevel,
                                totalXp = totalXp,
                                streakDays = streakDays,
                                isPremium = isPremium,
                                onLanguageSelected = { viewModel.selectLanguage(it) },
                                onPremiumClick = { viewModel.navigateTo(AppScreen.SUBSCRIPTIONS) },
                                onContactClick = { viewModel.navigateTo(AppScreen.CONTACT_DEVELOPER) }
                            )
                        }
                    },
                    bottomBar = {
                        if (isMainTabScreen) {
                            LinguaBottomNavBar(
                                currentScreen = currentScreen,
                                onTabSelected = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.WELCOME -> {
                                WelcomeScreen(
                                    selectedLanguage = selectedLanguage,
                                    onLanguageSelected = { viewModel.selectLanguage(it) },
                                    onStartLearning = { viewModel.navigateTo(AppScreen.MAIN_LEARN) },
                                    onTakePlacementTest = { viewModel.startPlacementTest() }
                                )
                            }

                            AppScreen.MAIN_LEARN -> {
                                LearnScreen(
                                    currentLanguage = selectedLanguage,
                                    currentLevel = currentLevel,
                                    completedLessons = completedLessons,
                                    dailyXp = dailyXp,
                                    dailyGoalXp = dailyGoalXp,
                                    isLevelUnlocked = { level -> viewModel.isLevelUnlocked(level) },
                                    onLevelSelected = { viewModel.selectLevel(it) },
                                    onLessonClick = { lesson -> viewModel.startLesson(lesson) },
                                    onTakePlacementTest = { viewModel.startPlacementTest() },
                                    onStartLevelExam = { level -> viewModel.startExam(level) },
                                    onOpenAiTutor = { viewModel.navigateTo(AppScreen.AI_TUTOR) },
                                    onOpenSubscriptions = { viewModel.navigateTo(AppScreen.SUBSCRIPTIONS) }
                                )
                            }

                            AppScreen.MAIN_SPEAK -> {
                                SpeakScreen(
                                    activeTopic = activeConversation,
                                    onSelectTopic = { topic -> viewModel.startConversation(topic) },
                                    onCloseTopic = { viewModel.closeConversation() },
                                    onPlayAudio = { phrase -> viewModel.playAudio(phrase) }
                                )
                            }

                            AppScreen.MAIN_EXAMS -> {
                                ExamsScreen(
                                    currentLanguage = selectedLanguage,
                                    userCertificates = userCertificates,
                                    onStartExam = { level -> viewModel.startExam(level) },
                                    onViewCertificate = { cert -> viewModel.viewCertificate(cert) },
                                    onStartPlacementTest = { viewModel.startPlacementTest() }
                                )
                            }

                            AppScreen.MAIN_VOCAB -> {
                                VocabularyScreen(
                                    vocabularyList = savedVocabulary,
                                    onPlayAudio = { phrase -> viewModel.playAudio(phrase) },
                                    onToggleMastered = { wordId -> viewModel.toggleMasteredWord(wordId) },
                                    onToggleReview = { wordId -> viewModel.toggleReviewWord(wordId) }
                                )
                            }

                            AppScreen.MAIN_PROFILE -> {
                                ProfileDashboardScreen(
                                    currentLanguage = selectedLanguage,
                                    currentLevel = currentLevel,
                                    totalXp = totalXp,
                                    streakDays = streakDays,
                                    completedLessonsCount = completedLessons.size,
                                    vocabularyCount = savedVocabulary.size,
                                    certificatesCount = userCertificates.size,
                                    isPremium = isPremium,
                                    isDarkMode = isDarkModeUser,
                                    speechSpeed = speechSpeed,
                                    achievements = viewModel.repository.getAchievements(),
                                    leaderboard = viewModel.repository.getLeaderboard(),
                                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                                    onSpeechSpeedChange = { speed -> viewModel.setSpeechSpeed(speed) },
                                    onNavigateToContact = { viewModel.navigateTo(AppScreen.CONTACT_DEVELOPER) },
                                    onNavigateToHelp = { viewModel.navigateTo(AppScreen.HELP_FAQ) },
                                    onNavigateToSubscriptions = { viewModel.navigateTo(AppScreen.SUBSCRIPTIONS) },
                                    onNavigateToAiTutor = { viewModel.navigateTo(AppScreen.AI_TUTOR) },
                                    onNavigateToAdmin = { viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD) }
                                )
                            }

                            AppScreen.LESSON_PLAYER -> {
                                activeLesson?.let { lesson ->
                                    LessonPlayerScreen(
                                        lesson = lesson,
                                        onPlayAudio = { phrase -> viewModel.playAudio(phrase) },
                                        onCompleteLesson = { xp ->
                                            viewModel.completeCurrentLesson(xp)
                                            viewModel.navigateBack()
                                        },
                                        onExit = { viewModel.navigateBack() }
                                    )
                                } ?: run {
                                    viewModel.navigateBack()
                                }
                            }

                            AppScreen.PLACEMENT_TEST -> {
                                PlacementTestScreen(
                                    onCompletePlacement = { level ->
                                        viewModel.applyPlacementResult(level)
                                    },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            AppScreen.EXAM_PLAYER -> {
                                (activeExamLevel ?: currentLevel).let { level ->
                                    ExamPlayerScreen(
                                        level = level,
                                        onPlayAudio = { phrase -> viewModel.playAudio(phrase) },
                                        onExamFinished = { score, name ->
                                            viewModel.completeExam(level, score, name)
                                            if (score >= 70) {
                                                viewModel.navigateTo(AppScreen.CERTIFICATE_VIEW)
                                            } else {
                                                viewModel.navigateBack()
                                            }
                                        },
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                            }

                            AppScreen.CERTIFICATE_VIEW -> {
                                activeCertificate?.let { cert ->
                                    CertificateScreen(
                                        certificate = cert,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                } ?: run {
                                    viewModel.navigateBack()
                                }
                            }

                            AppScreen.CONTACT_DEVELOPER -> {
                                ContactDeveloperScreen(
                                    onBack = { viewModel.navigateBack() },
                                    onNavigateToHelp = { viewModel.navigateTo(AppScreen.HELP_FAQ) },
                                    onNavigateToSubscriptions = { viewModel.navigateTo(AppScreen.SUBSCRIPTIONS) }
                                )
                            }

                            AppScreen.HELP_FAQ -> {
                                HelpScreen(
                                    onBack = { viewModel.navigateBack() },
                                    onContactDeveloper = { viewModel.navigateTo(AppScreen.CONTACT_DEVELOPER) }
                                )
                            }

                            AppScreen.SUBSCRIPTIONS -> {
                                SubscriptionScreen(
                                    isPremium = isPremium,
                                    onSubmitPayment = { name, contact, plan, method, ref, notes ->
                                        viewModel.submitPayment(name, contact, plan, method, ref, notes)
                                    },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            AppScreen.AI_TUTOR -> {
                                AiTutorScreen(
                                    currentLanguage = selectedLanguage,
                                    currentLevel = currentLevel,
                                    isPremium = isPremium,
                                    messages = aiMessages,
                                    isThinking = isAiThinking,
                                    onSendMessage = { text -> viewModel.sendAiTutorMessage(text) },
                                    onPlayAudio = { phrase -> viewModel.playAudio(phrase) },
                                    onUpgradeClick = { viewModel.navigateTo(AppScreen.SUBSCRIPTIONS) },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }

                            AppScreen.ADMIN_DASHBOARD -> {
                                AdminDashboardScreen(
                                    isAuthenticated = isAdminAuthenticated,
                                    paymentRequests = paymentRequests,
                                    registeredUsers = registeredUsers,
                                    dailyRevenueDzd = dailyRevenueDzd,
                                    dailyRevenueUsdt = dailyRevenueUsdt,
                                    monthlyRevenueDzd = monthlyRevenueDzd,
                                    monthlyRevenueUsdt = monthlyRevenueUsdt,
                                    totalRevenueDzd = totalRevenueDzd,
                                    totalRevenueUsdt = totalRevenueUsdt,
                                    onVerifyPin = { pin -> viewModel.verifyAdminPin(pin) },
                                    onLockAdmin = { viewModel.lockAdmin() },
                                    onApprovePayment = { reqId -> viewModel.approvePayment(reqId) },
                                    onRejectPayment = { reqId -> viewModel.rejectPayment(reqId) },
                                    onManualAddUser = { name, contact, level, isVip ->
                                        viewModel.manualAddUser(name, contact, level, isVip)
                                    },
                                    onBack = { viewModel.navigateBack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
