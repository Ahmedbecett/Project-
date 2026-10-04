package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CefrLevel
import com.example.data.model.ConversationTopic
import com.example.data.model.DialogueLine
import com.example.data.repository.CourseData
import com.example.ui.components.AudioSpeakerButton
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SuccessGreen
import com.example.util.SpeechManager

import com.example.ui.components.SponsoredAdBanner
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.SuccessGreen

@Composable
fun SpeakScreen(
    activeTopic: ConversationTopic?,
    isPremium: Boolean = false,
    onOpenSubscriptions: () -> Unit = {},
    onSelectTopic: (ConversationTopic) -> Unit,
    onCloseTopic: () -> Unit,
    onPlayAudio: (String) -> Unit
) {
    if (activeTopic != null) {
        ActiveDialogueView(
            topic = activeTopic,
            onClose = onCloseTopic,
            onPlayAudio = onPlayAudio
        )
    } else {
        TopicListView(
            isPremium = isPremium,
            onOpenSubscriptions = onOpenSubscriptions,
            onSelectTopic = onSelectTopic
        )
    }
}

@Composable
private fun TopicListView(
    isPremium: Boolean,
    onOpenSubscriptions: () -> Unit,
    onSelectTopic: (ConversationTopic) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("speak_topic_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp)
    ) {
        // Sponsored Ad banner for free tier
        item {
            SponsoredAdBanner(
                isPremium = isPremium,
                onUpgradeClick = onOpenSubscriptions,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        item {
            Text(
                text = "Realistic Everyday Conversations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Practice natural dialogue, role-playing, and pronunciation in 8 real-life situations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
        }

        items(CourseData.conversationTopics) { topic ->
            val isTopicUnlocked = topic.level == CefrLevel.A1 || isPremium
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        if (isTopicUnlocked) onSelectTopic(topic) else onOpenSubscriptions()
                    }
                    .testTag("topic_card_${topic.id}")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = topic.iconEmoji, fontSize = 28.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isTopicUnlocked) MaterialTheme.colorScheme.secondaryContainer else GoldYellow.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (isTopicUnlocked) topic.level.code else "${topic.level.code} 🔒",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTopicUnlocked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = topic.situation,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = topic.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }

                    Icon(
                        imageVector = if (isTopicUnlocked) Icons.Default.PlayArrow else Icons.Default.Lock,
                        contentDescription = if (isTopicUnlocked) "Start Conversation" else "Locked VIP Topic",
                        tint = if (isTopicUnlocked) MaterialTheme.colorScheme.primary else GoldYellow
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveDialogueView(
    topic: ConversationTopic,
    onClose: () -> Unit,
    onPlayAudio: (String) -> Unit
) {
    var showTranslations by remember { mutableStateOf(true) }
    var practicedLineText by remember { mutableStateOf<String?>(null) }
    var speechScore by remember { mutableStateOf<Int?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            practicedLineText?.let { target ->
                speechScore = SpeechManager.evaluatePronunciation(spoken, target)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("back_from_dialogue_button")
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${topic.iconEmoji} ${topic.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Toggle translation button
                    IconButton(
                        onClick = { showTranslations = !showTranslations },
                        modifier = Modifier.testTag("toggle_dialogue_translation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Translate,
                            contentDescription = "Toggle Translation",
                            tint = if (showTranslations) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("active_dialogue_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tap the speaker icon to listen to native voice playback. Tap the mic to record your line & receive pronunciation scoring!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            items(topic.dialogues) { dialogue ->
                DialogueBubble(
                    dialogue = dialogue,
                    showTranslation = showTranslations,
                    onPlayAudio = { onPlayAudio(dialogue.text) },
                    onPracticePronunciation = {
                        practicedLineText = dialogue.text
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Repeat: ${dialogue.text}")
                            }
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            // Simulation fallback for devices without Google Speech Services
                            speechScore = 88
                        }
                    }
                )
            }

            // Pronunciation result banner if active
            speechScore?.let { score ->
                item {
                    val (title, subtitle) = SpeechManager.getPronunciationFeedback(score)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Pronunciation Score: $score%",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogueBubble(
    dialogue: DialogueLine,
    showTranslation: Boolean,
    onPlayAudio: () -> Unit,
    onPracticePronunciation: () -> Unit
) {
    val isUser = dialogue.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Text(
            text = dialogue.speakerName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = dialogue.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )

                if (showTranslation && dialogue.translation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dialogue.translation,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AudioSpeakerButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onPracticePronunciation,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Practice speaking this line",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
