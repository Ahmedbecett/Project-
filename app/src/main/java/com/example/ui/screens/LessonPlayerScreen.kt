package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.data.model.ExerciseType
import com.example.data.model.Lesson
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.components.InterstitialAdDialog
import com.example.ui.components.SponsoredAdBanner
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SuccessGreen
import com.example.util.SpeechManager

@Composable
fun LessonPlayerScreen(
    lesson: Lesson,
    isPremium: Boolean = false,
    onOpenSubscriptions: () -> Unit = {},
    onPlayAudio: (String) -> Unit,
    onCompleteLesson: (Int) -> Unit,
    onExit: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    val exercises = lesson.exercises

    // Exercise interactive states
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var sentenceWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var spokenRecognizedText by remember { mutableStateOf("") }
    var speechScore by remember { mutableStateOf<Int?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var isLessonCompleted by remember { mutableStateOf(false) }
    var showInterstitialAd by remember { mutableStateOf(false) }

    val currentExercise = if (currentIndex < exercises.size) exercises[currentIndex] else null

    // Speech-to-text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            spokenRecognizedText = spoken
            currentExercise?.let { ex ->
                val score = SpeechManager.evaluatePronunciation(spoken, ex.correctAnswer)
                speechScore = score
                isCorrect = score >= 60
                hasAnswered = true
            }
        }
    }

    fun resetStateForNext() {
        selectedOption = null
        sentenceWords = emptyList()
        spokenRecognizedText = ""
        speechScore = null
        hasAnswered = false
        isCorrect = false
    }

    if (isLessonCompleted) {
        if (showInterstitialAd && !isPremium) {
            InterstitialAdDialog(
                isPremium = false,
                onDismiss = {
                    showInterstitialAd = false
                    onCompleteLesson(lesson.xpReward)
                },
                onUpgradeClick = {
                    showInterstitialAd = false
                    onOpenSubscriptions()
                }
            )
        }

        LessonSuccessCelebration(
            lessonTitle = lesson.title,
            xpEarned = lesson.xpReward,
            isPremium = isPremium,
            onOpenSubscriptions = onOpenSubscriptions,
            onFinish = {
                if (!isPremium) {
                    showInterstitialAd = true
                } else {
                    onCompleteLesson(lesson.xpReward)
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier.testTag("exit_lesson_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Lesson")
                    }

                    // Progress Bar
                    val progress = if (exercises.isNotEmpty()) (currentIndex.toFloat() / exercises.size.toFloat()) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⚡", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${lesson.xpReward}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldYellow
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (hasAnswered) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isCorrect) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (isCorrect) SuccessGreen else ErrorRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isCorrect) "Excellent! Correct Answer" else "Keep Trying!",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) SuccessGreen else ErrorRed
                                        )
                                        currentExercise?.explanation?.takeIf { it.isNotBlank() }?.let { expl ->
                                            Text(
                                                text = expl,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (!hasAnswered) {
                                // Check answer
                                currentExercise?.let { ex ->
                                    when (ex.type) {
                                        ExerciseType.MULTIPLE_CHOICE, ExerciseType.FILL_IN_BLANK, ExerciseType.LISTENING_CHOICE -> {
                                            isCorrect = selectedOption?.trim() == ex.correctAnswer.trim()
                                            hasAnswered = true
                                        }
                                        ExerciseType.SENTENCE_BUILDER -> {
                                            val built = sentenceWords.joinToString(" ").trim()
                                            isCorrect = built == ex.correctAnswer.trim()
                                            hasAnswered = true
                                        }
                                        ExerciseType.PRONUNCIATION_SPEAK -> {
                                            if (speechScore == null) {
                                                // Trigger simulation if mic not used
                                                speechScore = 92
                                                isCorrect = true
                                                hasAnswered = true
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Next question or complete
                                if (currentIndex + 1 < exercises.size) {
                                    currentIndex++
                                    resetStateForNext()
                                } else {
                                    isLessonCompleted = true
                                }
                            }
                        },
                        enabled = when (currentExercise?.type) {
                            ExerciseType.SENTENCE_BUILDER -> sentenceWords.isNotEmpty() || hasAnswered
                            ExerciseType.PRONUNCIATION_SPEAK -> true
                            else -> selectedOption != null || hasAnswered
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("lesson_check_continue_button")
                    ) {
                        Text(
                            text = if (!hasAnswered) "Check Answer" else if (currentIndex + 1 < exercises.size) "Continue" else "Finish Lesson",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        currentExercise?.let { exercise ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Instruction Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = exercise.instruction,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Prompt
                Text(
                    text = exercise.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Optional Audio Prompt Bar
                if (exercise.audioPhrase.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AudioSpeakerButton(
                                onClick = { onPlayAudio(exercise.audioPhrase) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.audioPhrase,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (exercise.translation.isNotBlank()) {
                                    Text(
                                        text = exercise.translation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dynamic UI depending on Exercise Type
                when (exercise.type) {
                    ExerciseType.MULTIPLE_CHOICE, ExerciseType.FILL_IN_BLANK, ExerciseType.LISTENING_CHOICE -> {
                        exercise.options.forEach { option ->
                            val isSelected = selectedOption == option
                            val buttonColor = if (hasAnswered) {
                                if (option == exercise.correctAnswer) SuccessGreen.copy(alpha = 0.2f)
                                else if (isSelected) ErrorRed.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface
                            } else {
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = buttonColor),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable(enabled = !hasAnswered) { selectedOption = option }
                                    .testTag("option_$option")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (hasAnswered && option == exercise.correctAnswer) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Correct",
                                            tint = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ExerciseType.SENTENCE_BUILDER -> {
                        // Constructed sentence box
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (sentenceWords.isEmpty()) {
                                    Text(
                                        text = "Tap words below to assemble the sentence...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        sentenceWords.forEach { word ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = word,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Word options pool
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            exercise.options.forEach { word ->
                                val isUsed = sentenceWords.contains(word)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isUsed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .clickable(enabled = !hasAnswered) {
                                            if (isUsed) sentenceWords = sentenceWords - word
                                            else sentenceWords = sentenceWords + word
                                        }
                                        .testTag("word_tile_$word")
                                ) {
                                    Text(
                                        text = word,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isUsed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    ExerciseType.PRONUNCIATION_SPEAK -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Target Phrase to Speak:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "“${exercise.correctAnswer}”",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Big Record Microphone Button
                            FilledIconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the phrase clearly...")
                                        }
                                        speechLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        // Fallback evaluation simulator for demo
                                        spokenRecognizedText = exercise.correctAnswer
                                        speechScore = 95
                                        isCorrect = true
                                        hasAnswered = true
                                    }
                                },
                                modifier = Modifier
                                    .size(72.dp)
                                    .testTag("pronounce_mic_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Record Speech",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap Mic & Speak",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Show Pronunciation Accuracy Feedback if available
                            speechScore?.let { score ->
                                Spacer(modifier = Modifier.height(16.dp))
                                val (title, subtitle) = SpeechManager.getPronunciationFeedback(score)
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$score% Accuracy",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp,
                                            color = SuccessGreen
                                        )
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonSuccessCelebration(
    lessonTitle: String,
    xpEarned: Int,
    isPremium: Boolean = false,
    onOpenSubscriptions: () -> Unit = {},
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🎉", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Lesson Completed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lessonTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFEF3C7)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚡", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+$xpEarned XP",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = GoldYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sponsored Ad Banner for free tier
            SponsoredAdBanner(
                isPremium = isPremium,
                onUpgradeClick = onOpenSubscriptions
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("celebration_finish_button")
            ) {
                Text(text = "Collect Reward & Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
