package com.example.dogbreedsguessing.ui.guessing

data class GameUiState(
    val currentDogBreed: String = "",
    val currentDogBreedImageUrl: String = "",
    val score: Int = 0,
    val isGuessedWordWrong: Boolean = false,
    val isGameOver: Boolean = false
)