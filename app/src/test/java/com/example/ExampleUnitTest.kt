package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun pronunciation_evaluation_isAccurate() {
    val scoreExact = com.example.util.SpeechManager.evaluatePronunciation("Hola mucho gusto", "Hola, mucho gusto!")
    assertEquals(100, scoreExact)

    val scoreHigh = com.example.util.SpeechManager.evaluatePronunciation("Hola mucho gust", "Hola, mucho gusto!")
    assertTrue(scoreHigh >= 85)
  }
}
