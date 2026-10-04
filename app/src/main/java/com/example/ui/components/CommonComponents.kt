package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CefrLevel
import com.example.data.model.SupportedLanguage
import com.example.data.repository.CourseData
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@Composable
fun LinguaTopAppBar(
    currentLanguage: SupportedLanguage,
    currentLevel: CefrLevel,
    totalXp: Int,
    streakDays: Int,
    isPremium: Boolean,
    onLanguageSelected: (SupportedLanguage) -> Unit,
    onPremiumClick: () -> Unit,
    onContactClick: () -> Unit
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Language Picker Chip
            Box {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clickable { showLangMenu = true }
                        .testTag("language_picker_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currentLanguage.flagEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentLanguage.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select language",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showLangMenu,
                    onDismissRequest = { showLangMenu = false }
                ) {
                    CourseData.supportedLanguages.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = lang.flagEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = lang.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = lang.nativeName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onLanguageSelected(lang)
                                showLangMenu = false
                            },
                            modifier = Modifier.testTag("lang_option_${lang.id}")
                        )
                    }
                }
            }

            // Gamification Chips: Streak, XP & Contact
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFECE5)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streakDays",
                            fontWeight = FontWeight.Bold,
                            color = StreakOrange,
                            fontSize = 13.sp
                        )
                    }
                }

                // XP Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalXp",
                            fontWeight = FontWeight.Bold,
                            color = GoldYellow,
                            fontSize = 13.sp
                        )
                    }
                }

                // Premium Badge or Upgrade Icon
                IconButton(
                    onClick = onPremiumClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("premium_upgrade_button")
                ) {
                    Icon(
                        imageVector = if (isPremium) Icons.Default.Stars else Icons.Outlined.WorkspacePremium,
                        contentDescription = "Premium Subscription",
                        tint = if (isPremium) GoldYellow else MaterialTheme.colorScheme.primary
                    )
                }

                // Contact Developer quick button
                IconButton(
                    onClick = onContactClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("top_contact_dev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContactSupport,
                        contentDescription = "Contact Developer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LinguaBottomNavBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        tonalElevation = 6.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("main_bottom_nav_bar")
    ) {
        val navItems = listOf(
            Triple(AppScreen.MAIN_LEARN, "Learn", Icons.Default.School),
            Triple(AppScreen.MAIN_SPEAK, "Speak", Icons.Default.RecordVoiceOver),
            Triple(AppScreen.MAIN_EXAMS, "Exams", Icons.Default.Verified),
            Triple(AppScreen.MAIN_VOCAB, "Vocab", Icons.Default.MenuBook),
            Triple(AppScreen.MAIN_PROFILE, "Profile", Icons.Default.Person)
        )

        navItems.forEach { (screen, label, icon) ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}

@Composable
fun LevelSelectorPills(
    selectedLevel: CefrLevel,
    onLevelSelected: (CefrLevel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CefrLevel.values().forEach { level ->
            val isSelected = level == selectedLevel
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                label = "pill_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "pill_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onLevelSelected(level) }
                    .padding(vertical = 8.dp)
                    .testTag("level_pill_${level.code}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.code,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun AudioSpeakerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    contentDescription: String = "Listen Pronunciation"
) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isSpeaking) GoldYellow else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isSpeaking) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
            .size(44.dp)
            .testTag("audio_tts_button")
    ) {
        Icon(
            imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Outlined.VolumeUp,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp)
        )
    }
}
