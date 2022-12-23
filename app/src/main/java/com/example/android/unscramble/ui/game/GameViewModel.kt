package com.example.android.unscramble.ui.game

import android.text.Spannable
import android.text.SpannableString
import android.text.style.TtsSpan
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.random.Random

/**
 * ViewModel containing the app data and methods to process the data
 */
class GameViewModel(
    private val stateHandler: SavedStateHandle
) : ViewModel(){
    private val _score = stateHandler.getMutableStateFlow("score", 0)
    val score: StateFlow<Int>
        get() = _score.asStateFlow()

    private val _currentWordCount = stateHandler.getMutableStateFlow("currentWordCount",0)
    val currentWordCount: StateFlow<Int>
        get() = _currentWordCount.asStateFlow()

    private val _currentScrambledWord = stateHandler.getMutableStateFlow("currentScrambledWord", "")
    val currentScrambledWord: StateFlow<Spannable>
        get() = _currentScrambledWord.asStateFlow()
            .onSubscription {
                if (currentWord.isEmpty()) {
                    nextWord()
                }
            }
            .map { scrambledWord ->
            SpannableString(scrambledWord).apply {
                setSpan(
                    TtsSpan.VerbatimBuilder(scrambledWord).build(),
                    0,
                    scrambledWord.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SpannableString(""))

    // List of words used in the game
    private var wordsList: List<String>
        get() = stateHandler["wordsList"] ?: emptyList()
        set(value) {
            stateHandler["wordsList"] = value
        }

    private var currentWord: String
        get() = stateHandler["currentWord"] ?: ""
        set(value) {
            val tempWord = value.toCharArray()
            do {
                tempWord.shuffle()
            } while (tempWord.equals(value))

            _currentScrambledWord.value = String(tempWord)
            _currentWordCount.value++
            wordsList = wordsList + currentWord

            stateHandler["currentWord"] = value
        }

    /*
    * Re-initializes the game data to restart the game.
    */
    fun reinitializeData() {
        _score.value = 0
        _currentWordCount.value = 0
        wordsList = emptyList()
        nextWord()
    }

    /*
    * Increases the game score if the player's word is correct.
    */
    private fun increaseScore() {
        _score.value += SCORE_INCREASE
    }

    /*
    * Returns true if the player word is correct.
    * Increases the score accordingly.
    */
    fun isUserWordCorrect(playerWord: String): Boolean {
        if (playerWord.equals(currentWord, true)) {
            increaseScore()
            return true
        }
        return false
    }

    /*
    * Returns true if the current word count is less than MAX_NO_OF_WORDS
    */
    fun nextWord(): Boolean {
        return if (_currentWordCount.value < MAX_NO_OF_WORDS) {
            var nextWord: String
            do {
                nextWord = allWordsList.random(Random(Calendar.getInstance().timeInMillis))
            } while (wordsList.contains(currentWord))
            currentWord = nextWord
            true
        } else false
    }
}

class SavedMutableStateFlow<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    initialValue: T,
) {
    private val state: StateFlow<T> = savedStateHandle.getStateFlow(key, initialValue)
    var value: T
        get() = state.value
        set(value) {
            savedStateHandle[key] = value
        }
    fun asStateFlow(): StateFlow<T> = state
}

fun <T> SavedStateHandle.getMutableStateFlow(key: String, initialValue: T): SavedMutableStateFlow<T> =
    SavedMutableStateFlow(this, key, initialValue)
