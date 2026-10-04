package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.CourseData
import com.example.ui.components.LevelSelectorPills
import com.example.ui.components.SponsoredAdBanner
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SuccessGreen

@Composable
fun LearnScreen(
    currentLanguage: SupportedLanguage,
    currentLevel: CefrLevel,
    completedLessons: Set<String>,
    dailyXp: Int,
    dailyGoalXp: Int,
    isPremium: Boolean,
    isLevelUnlocked: (CefrLevel) -> Boolean,
    onLevelSelected: (CefrLevel) -> Unit,
    onLessonClick: (Lesson) -> Unit,
    onTakePlacementTest: () -> Unit,
    onStartLevelExam: (CefrLevel) -> Unit,
    onOpenAiTutor: () -> Unit,
    onOpenSubscriptions: () -> Unit
) {
    val lessons = CourseData.getLessonsForLevel(currentLanguage.id, currentLevel)
    val isCurrentLevelUnlocked = isLevelUnlocked(currentLevel)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("learn_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Level Tabs A1 - C2
        item {
            LevelSelectorPills(
                selectedLevel = currentLevel,
                isLevelUnlocked = isLevelUnlocked,
                onLevelSelected = onLevelSelected
            )
        }

        // Sponsored Ad banner for free tier
        item {
            SponsoredAdBanner(
                isPremium = isPremium,
                onUpgradeClick = onOpenSubscriptions,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // AI Language Tutor Banner Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpenAiTutor() }
                    .testTag("ai_tutor_banner_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PurpleAccent,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🤖", fontSize = 26.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مُعلّم الذكاء الاصطناعي (AI Tutor)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldYellow
                            ) {
                                Text(
                                    text = "Gemini",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "محادثة ذكية، تصحيح فوري للقواعد، ومحاكاة مواقف حقيقية بالصوت والنص.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Chat",
                        tint = PurpleAccent
                    )
                }
            }
        }

        // Daily Goal & Level Intro Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Level ${currentLevel.code}: ${currentLevel.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (currentLevel == CefrLevel.A1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SuccessGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "مجاني 100%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = currentLevel.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isCurrentLevelUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isCurrentLevelUnlocked) {
                                    Text(
                                        text = currentLevel.code,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = GoldYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Goal XP Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Goal: $dailyXp / $dailyGoalXp XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (dailyXp >= dailyGoalXp) "Goal Reached! 🎉" else "${dailyGoalXp - dailyXp} XP to go",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (dailyXp >= dailyGoalXp) SuccessGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (dailyXp.toFloat() / dailyGoalXp.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GoldYellow,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        // Locked Level Banner if user is on Free Tier and looking at A2-C2
        if (!isCurrentLevelUnlocked) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onOpenSubscriptions() }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "هذا المستوى مدفوع (${currentLevel.code}) - المستوى A1 فقط مجاني",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اشترك الآن عبر بريدي موب (002440629137) أو بينانس USDT لفتح جميع المستويات والذكاء الاصطناعي.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenSubscriptions,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("فتح الاشتراك (ابتداءً من 1,500 دج / 8 USDT)")
                        }
                    }
                }
            }
        }

        // Diagnostic Placement Test Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onTakePlacementTest() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎯", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اختبار تحديد المستوى (Placement Test)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "12 سؤالاً لتشخيص مستواك اللغوي بدقة وتحديد نقطة الانطلاق.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Placement Test",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Section Title: Units & Courses
        item {
            Text(
                text = "Course Curriculum",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Lessons List
        itemsIndexed(lessons) { index, lesson ->
            val isCompleted = completedLessons.contains(lesson.id)
            LessonCardItem(
                lesson = lesson,
                unitIndex = index + 1,
                isCompleted = isCompleted,
                isLocked = !isCurrentLevelUnlocked,
                onClick = {
                    if (isCurrentLevelUnlocked) {
                        onLessonClick(lesson)
                    } else {
                        onOpenSubscriptions()
                    }
                }
            )
        }

        // Level Final Exam Node
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable {
                        if (isCurrentLevelUnlocked) onStartLevelExam(currentLevel)
                        else onOpenSubscriptions()
                    }
                    .testTag("level_exam_card_${currentLevel.code}")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isCurrentLevelUnlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                                contentDescription = "Exam",
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Final Exam: ${currentLevel.code} Certificate",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = if (isCurrentLevelUnlocked)
                                "50 questions covering Grammar, Vocab, Reading & Listening. Pass to earn your digital certificate."
                            else "يتطلب اشتراكاً لإجراء الاختبار الرسمي والحصول على الشهادة المعتمدة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = {
                            if (isCurrentLevelUnlocked) onStartLevelExam(currentLevel)
                            else onOpenSubscriptions()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = if (isCurrentLevelUnlocked) "Exam" else "Unlock", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCardItem(
    lesson: Lesson,
    unitIndex: Int,
    isCompleted: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("lesson_card_${lesson.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isLocked) MaterialTheme.colorScheme.surfaceVariant
                else if (isCompleted) SuccessGreen
                else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = GoldYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White
                        )
                    } else {
                        Text(
                            text = "$unitIndex",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = lesson.skillType.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${lesson.xpReward} XP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldYellow
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.testTag("start_lesson_${lesson.id}")
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock
                    else if (isCompleted) Icons.Default.Replay
                    else Icons.Default.PlayCircle,
                    contentDescription = "Action",
                    tint = if (isLocked) GoldYellow
                    else if (isCompleted) SuccessGreen
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
