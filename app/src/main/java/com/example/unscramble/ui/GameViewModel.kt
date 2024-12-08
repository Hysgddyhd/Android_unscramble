package com.example.unscramble.ui

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.properties.Delegates

//stateflow data holder, reflect current state


class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState : StateFlow<GameUiState> = _uiState.asStateFlow()
    private var usedWords: MutableSet<String> = mutableSetOf()
    private lateinit var currentWord: String
    val SCORE_INCREASE = 20
    //user guess word
    var userGuess by mutableStateOf("")
        private set
    init {
        resetGame()
    }
    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            var updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else if (userGuess.equals("")){

        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)

            }
        }
         updateUserGuess("")
    }
    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            //continue to pick random word until one is not used
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
        }
        return shuffleCurrentWord(currentWord)
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        //scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun updateUserGuess(guessedWord: String) {
            userGuess = guessedWord
    }

    private fun updateGameState(updatedScore:Int) {
        if (usedWords.size == MAX_NO_OF_WORDS ){
            //last round in game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentWordCount = 10,
                    isGameOver = true,
                )

            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    isGuessedWordWrong = false,
                    currentWordCount = currentState.currentWordCount.inc(),
                    score = updatedScore,
                )
            }

        }
        Log.d("word_count",_uiState.value.currentWordCount.toString())
    }

    fun skipCurrentWord() {
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }


}