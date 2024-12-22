package com.example.dogbreedsguessing.ui.guessing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogbreedsguessing.domain.FetchDogBreedsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuessingViewModel @Inject constructor(
    private val fetchDogBreedsUseCase: FetchDogBreedsUseCase
) : ViewModel() {

    companion object {
        private const val SCORE_INCREASE = 1
        private const val TAG = "GuessingViewModel"
    }

    // Game UI state
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    var userGuess by mutableStateOf("")
        private set

    private var dogBreedInfoList = mutableListOf<FetchDogBreedsUseCase.DogBreedInfo>()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            when (val result = fetchDogBreedsUseCase.invoke()) {
                FetchDogBreedsUseCase.Result.Failed -> {
                    /*add toast or display error state*/
                }

                is FetchDogBreedsUseCase.Result.Success -> {
                    dogBreedInfoList.clear()
                    dogBreedInfoList.addAll(result.breedInfoList)
                    updateGameState(0)
                }
            }
        }
    }

    /*
     * Update the user's guess
     */
    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    /*
     * Checks if the user's guess is correct.
     * Increases the score accordingly.
     */
    fun checkUserGuess() {
        val currentWord = _uiState.value.currentDogBreed.trim()
        if (userGuess.trim().equals(currentWord, ignoreCase = true)) {
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            dogBreedInfoList.removeAt(0)
            updateGameState(updatedScore)
        } else {
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    /*
     * Pick the next dog breed to display on the screen
     * if there are no next dog breeds, set isGameOver to true
     */
    private fun updateGameState(updatedScore: Int) {
        if (dogBreedInfoList.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    isGameOver = true,
                    score = updatedScore
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentDogBreed = dogBreedInfoList.firstOrNull()?.name ?: "",
                    currentDogBreedImageUrl = dogBreedInfoList.firstOrNull()?.image ?: "",
                    score = updatedScore,
                    isGameOver = false
                )
            }
        }
    }
}