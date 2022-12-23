package com.example.android.unscramble.ui.game

import android.text.Spannable
import android.text.SpannableString
import android.text.style.TtsSpan
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.*
import kotlin.random.Random

/**
 * ViewModel containing the app data and methods to process the data
 */
class GameViewModel(
    stateHandler: SavedStateHandle
) : ViewModel(){
    private val _score = stateHandler.getMutableStateFlow("score", 0)
    val score: StateFlow<Int>
        get() = _score.asStateFlow()

    private val _currentWordCount = stateHandler.getMutableStateFlow("currentWordCount",0)
    val currentWordCount: StateFlow<Int>
        get() = _currentWordCount.asStateFlow()

    private val _currentScrambledWord = stateHandler.getMutableStateFlow("currentScrambledWord", "")
    val currentScrambledWord: StateFlow<Spannable>
        get() = _currentScrambledWord.asStateFlow().map { scrambledWord ->
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
    private var wordsList: MutableList<String> = mutableListOf()
    private lateinit var currentWord: String

    init {
        getNextWord()
    }

    /*
    * Updates currentWord and currentScrambledWord with the next word.
    */
    private fun getNextWord() {
        currentWord = allWordsList.random(Random(Calendar.getInstance().timeInMillis))
        val tempWord = currentWord.toCharArray()
        tempWord.shuffle()

        while (String(tempWord).equals(currentWord, false)) {
            tempWord.shuffle()
        }
        if (wordsList.contains(currentWord)) {
            getNextWord()
        } else {
            _currentScrambledWord.value = String(tempWord)
            _currentWordCount.value = _currentWordCount.value.inc()
            wordsList.add(currentWord)
        }
    }

    /*
    * Re-initializes the game data to restart the game.
    */
    fun reinitializeData() {
        _score.value = 0
        _currentWordCount.value = 0
        wordsList.clear()
        getNextWord()
    }

    /*
    * Increases the game score if the player's word is correct.
    */
    private fun increaseScore() {
        _score.value = _score.value.plus(SCORE_INCREASE)
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
            getNextWord()
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
