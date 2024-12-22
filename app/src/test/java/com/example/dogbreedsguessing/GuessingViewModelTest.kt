package com.example.dogbreedsguessing

import app.cash.turbine.test
import com.example.dogbreedsguessing.domain.FetchDogBreedsUseCase
import com.example.dogbreedsguessing.domain.FetchDogBreedsUseCase.DogBreedInfo
import com.example.dogbreedsguessing.domain.FetchDogBreedsUseCase.Result
import com.example.dogbreedsguessing.ui.guessing.GameUiState
import com.example.dogbreedsguessing.ui.guessing.GuessingViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class GuessingViewModelTest {

    private val fetchDogBreedsUseCase: FetchDogBreedsUseCase = mockk()
    private lateinit var viewModel: GuessingViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData success updatesUiState`() = runTest {
        // Given
        val dogBreedInfoList = listOf(
            DogBreedInfo("chihuahua", "url1"),
            DogBreedInfo("golden", "url2")
        )
        coEvery { fetchDogBreedsUseCase.invoke(breadCount = any(), imagePerBreed = any()) } returns Result.Success(dogBreedInfoList)
        viewModel = GuessingViewModel(fetchDogBreedsUseCase)
        // Then
        viewModel.loadData()

        // When
        viewModel.uiState.test {
            val initialUiState = awaitItem()
            assert(initialUiState == GameUiState())
            val uiState = awaitItem()
            assertEquals("chihuahua", uiState.currentDogBreed)
            assertEquals("url1", uiState.currentDogBreedImageUrl)
            assertEquals(0, uiState.score)
            assertEquals(false, uiState.isGameOver)
        }
    }

    @Test
    fun `given checkUserGuess when correctGuess then updatesUiState`() = runTest {

        val dogBreedInfoList = listOf(
            DogBreedInfo("breed1", "url1"),
            DogBreedInfo("breed2", "url2")
        )
        coEvery { fetchDogBreedsUseCase.invoke(breadCount = any(), imagePerBreed = any()) } returns Result.Success(dogBreedInfoList)
        viewModel = GuessingViewModel(fetchDogBreedsUseCase)
        viewModel.loadData() // Initialize with data
        viewModel.updateUserGuess("breed1") // Set user guess

        viewModel.uiState.test {
            val initialUiState = awaitItem()
            assert(initialUiState == GameUiState())

            val uiState = awaitItem()
            assertEquals("breed1", uiState.currentDogBreed)
            viewModel.checkUserGuess()

            val secondUiState = awaitItem()
            assertEquals("breed2", secondUiState.currentDogBreed) // Moved to next breed
            assertEquals("url2", secondUiState.currentDogBreedImageUrl)
            assertEquals(1, secondUiState.score) // Score incremented
            assertEquals(false, secondUiState.isGameOver)
        }
    }

    @Test
    fun `given checkUserGuess when incorrectGuess then updatesUiState`() = runTest {
        // Arrange
        val dogBreedInfoList = listOf(
            DogBreedInfo("breed1", "url1")
        )
        coEvery { fetchDogBreedsUseCase.invoke() } returns Result.Success(dogBreedInfoList)
        viewModel = GuessingViewModel(fetchDogBreedsUseCase)
        viewModel.loadData() // Initialize with data
        viewModel.updateUserGuess("wrongGuess") // Set incorrect guess

        viewModel.uiState.test {
            val initialUiState = awaitItem()
            assert(initialUiState == GameUiState())

            val uiState = awaitItem()
            viewModel.checkUserGuess()

            val errorState = awaitItem()
            assertEquals(0, errorState.score) // Score incremented
            assertEquals(true, errorState.isGuessedWordWrong)
        }
    }

    @Test
    fun `given user guess all the breeds then game over`() = runTest {

        val dogBreedInfoList = listOf(
            DogBreedInfo("breed1", "url1")
        )
        coEvery { fetchDogBreedsUseCase.invoke(breadCount = any(), imagePerBreed = any()) } returns Result.Success(dogBreedInfoList)
        viewModel = GuessingViewModel(fetchDogBreedsUseCase)
        viewModel.loadData() // Initialize with data
        viewModel.updateUserGuess("breed1") // Set user guess

        viewModel.uiState.test {
            val initialUiState = awaitItem()
            assert(initialUiState == GameUiState())

            val uiState = awaitItem()
            assertEquals("breed1", uiState.currentDogBreed)
            viewModel.checkUserGuess()

            val gameOverUiState = awaitItem()
            assertEquals(true, gameOverUiState.isGameOver)
            assertEquals(1, gameOverUiState.score)
        }
    }

}