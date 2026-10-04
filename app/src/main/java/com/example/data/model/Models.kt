package com.example.data.model

enum class CefrLevel(val code: String, val title: String, val description: String) {
    A1("A1", "Beginner", "Basic everyday expressions & simple phrases"),
    A2("A2", "Elementary", "Routine situations & direct exchanges of information"),
    B1("B1", "Intermediate", "Work, school, leisure & travel topics with spontaneity"),
    B2("B2", "Upper Intermediate", "Complex ideas, technical discussions & fluent interactions"),
    C1("C1", "Advanced", "Demanding texts, flexible language for social & professional use"),
    C2("C2", "Mastery", "Effortless comprehension, native-level precision & nuances")
}

data class SupportedLanguage(
    val id: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String,
    val localeTag: String,
    val totalCourses: Int = 120
)

enum class SkillType(val title: String) {
    VOCABULARY("Vocabulary"),
    GRAMMAR("Grammar"),
    READING("Reading"),
    LISTENING("Listening"),
    WRITING("Writing"),
    SPEAKING("Speaking"),
    PRONUNCIATION("Pronunciation"),
    CONVERSATION("Conversation")
}

enum class ExerciseType {
    MULTIPLE_CHOICE,
    FILL_IN_BLANK,
    SENTENCE_BUILDER,
    LISTENING_CHOICE,
    PRONUNCIATION_SPEAK
}

data class Exercise(
    val id: String,
    val type: ExerciseType,
    val question: String,
    val instruction: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val audioPhrase: String = "",
    val translation: String = "",
    val explanation: String = ""
)

data class Lesson(
    val id: String,
    val level: CefrLevel,
    val unitNumber: Int,
    val title: String,
    val description: String,
    val skillType: SkillType,
    val xpReward: Int = 20,
    val exercises: List<Exercise> = emptyList()
)

data class DialogueLine(
    val speakerName: String,
    val isUser: Boolean,
    val text: String,
    val translation: String,
    val audioText: String = text
)

data class ConversationTopic(
    val id: String,
    val title: String,
    val situation: String,
    val iconEmoji: String,
    val level: CefrLevel,
    val description: String,
    val dialogues: List<DialogueLine>
)

data class VocabularyWord(
    val id: String,
    val word: String,
    val translation: String,
    val phonetic: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val level: CefrLevel,
    val mastered: Boolean = false,
    val needsReview: Boolean = false
)

data class PlacementQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val targetLevel: CefrLevel,
    val skill: String
)

data class ExamQuestion(
    val id: String,
    val section: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val audioPrompt: String? = null
)

data class ExamResult(
    val level: CefrLevel,
    val languageId: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val passed: Boolean,
    val dateCompleted: Long,
    val skillBreakdown: Map<String, Int>
)

data class Certificate(
    val id: String,
    val studentName: String,
    val languageName: String,
    val level: CefrLevel,
    val scorePercentage: Int,
    val issueDate: String,
    val verificationCode: String
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val progress: Int,
    val maxProgress: Int,
    val unlocked: Boolean,
    val xpReward: Int
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val countryFlag: String,
    val xp: Int,
    val avatarInitial: String,
    val isCurrentUser: Boolean = false
)
