package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e("SpeechManager", "TTS initialization failed status: $status")
        }
    }

    fun speak(text: String, localeTag: String = "es-ES", speechRate: Float = 1.0f) {
        if (!isInitialized || tts == null || text.isBlank()) return

        try {
            val locale = Locale.forLanguageTag(localeTag)
            tts?.language = locale
            tts?.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("SpeechManager", "Failed to speak: ${e.message}")
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        fun evaluatePronunciation(spoken: String, expected: String): Int {
            if (expected.isBlank()) return 0
            if (spoken.isBlank()) return 0

            val cleanSpoken = cleanText(spoken)
            val cleanExpected = cleanText(expected)

            if (cleanSpoken == cleanExpected) return 100

            val distance = levenshteinDistance(cleanSpoken, cleanExpected)
            val maxLen = maxOf(cleanSpoken.length, cleanExpected.length)
            if (maxLen == 0) return 100

            val similarity = ((1.0 - (distance.toDouble() / maxLen.toDouble())) * 100).toInt()
            return similarity.coerceIn(0, 100)
        }

        fun getPronunciationFeedback(score: Int): Pair<String, String> {
            return when {
                score >= 90 -> Pair("Exceptional Clarity! 🎉", "Native-like articulation and pitch accuracy.")
                score >= 75 -> Pair("Very Good! 👏", "Clear pronunciation with minor accent variation.")
                score >= 50 -> Pair("Keep Practicing! 💪", "Understood, but try repeating for better cadence.")
                else -> Pair("Listen Again 🎧", "Tap the speaker icon to hear the native rhythm and try again.")
            }
        }

        private fun cleanText(text: String): String {
            return text.lowercase(Locale.ROOT)
                .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), "")
                .trim()
                .replace(Regex("\\s+"), " ")
        }

        private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
            val lhsLength = lhs.length
            val rhsLength = rhs.length

            var cost = Array(lhsLength + 1) { it }
            var newCost = Array(lhsLength + 1) { 0 }

            for (i in 1..rhsLength) {
                newCost[0] = i
                for (j in 1..lhsLength) {
                    val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                    val costReplace = cost[j - 1] + match
                    val costInsert = cost[j] + 1
                    val costDelete = newCost[j - 1] + 1
                    newCost[j] = minOf(costInsert, costDelete, costReplace)
                }
                val swap = cost
                cost = newCost
                newCost = swap
            }
            return cost[lhsLength]
        }
    }
}
