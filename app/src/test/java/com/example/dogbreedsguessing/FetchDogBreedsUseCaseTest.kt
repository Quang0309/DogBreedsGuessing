package com.example.dogbreedsguessing

import com.example.dogbreedsguessing.data.RemoteDogService
import com.example.dogbreedsguessing.data.model.DogBreedsResponse
import com.example.dogbreedsguessing.data.model.DogImagesResponse
import com.example.dogbreedsguessing.domain.FetchDogBreedsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FetchDogBreedsUseCaseTest {

    private val remoteDogService: RemoteDogService = mockk()
    private lateinit var fetchDogBreedsUseCase: FetchDogBreedsUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fetchDogBreedsUseCase = FetchDogBreedsUseCase(remoteDogService, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke network success should return list of dog breed info`() = runTest {
        // Given
        val mockDogBreedsResponse = DogBreedsResponse(
            message = mapOf(
                "alaska" to listOf("small alaska", "big alaska"),
                "bulldog" to listOf("bulldog1, bulldog2")
            ),
            status = "success"
        )
        coEvery { remoteDogService.fetchAllBreeds() } returns mockDogBreedsResponse

        val mockImagesByBreedsResponse = DogImagesResponse(
            message = listOf("url1", "url2"),
            status = "success"
        )
        coEvery { remoteDogService.fetchImagesByBreed(any(), any()) } returns mockImagesByBreedsResponse
        // When
        val actualDogBreeds = fetchDogBreedsUseCase()
        val expectedResult = FetchDogBreedsUseCase.Result.Success(
            breedInfoList = listOf(
                FetchDogBreedsUseCase.DogBreedInfo("alaska", "url1"),
                FetchDogBreedsUseCase.DogBreedInfo("alaska", "url2"),
                FetchDogBreedsUseCase.DogBreedInfo("bulldog", "url1"),
                FetchDogBreedsUseCase.DogBreedInfo("bulldog", "url2")
            )
        )

        // Then
        assert(actualDogBreeds is FetchDogBreedsUseCase.Result.Success)
        actualDogBreeds as FetchDogBreedsUseCase.Result.Success
        Assert.assertEquals(expectedResult.breedInfoList.size, actualDogBreeds.breedInfoList.size)
        expectedResult.breedInfoList.forEach {
            Assert.assertTrue(actualDogBreeds.breedInfoList.contains(it))
        }
    }

    @Test
    fun `invoke network fail in get dog breed should return error`() = runTest {
        // Given
        coEvery { remoteDogService.fetchAllBreeds() } throws Exception()

        val mockImagesByBreedsResponse = DogImagesResponse(
            message = listOf("url1", "url2"),
            status = "success"
        )
        coEvery { remoteDogService.fetchImagesByBreed(any(), any()) } returns mockImagesByBreedsResponse
        // When
        val actualDogBreeds = fetchDogBreedsUseCase()

        // Then
        assert(actualDogBreeds is FetchDogBreedsUseCase.Result.Failed)
    }

    @Test
    fun `invoke network fail in get dog images should return error`() = runTest {
        // Given
        val mockDogBreedsResponse = DogBreedsResponse(
            message = mapOf(
                "alaska" to listOf("small alaska", "big alaska"),
                "bulldog" to listOf("bulldog1, bulldog2")
            ),
            status = "success"
        )
        coEvery { remoteDogService.fetchAllBreeds() } returns mockDogBreedsResponse

        coEvery { remoteDogService.fetchImagesByBreed(any(), any()) } throws Exception()
        // When
        val actualDogBreeds = fetchDogBreedsUseCase()

        // Then
        assert(actualDogBreeds is FetchDogBreedsUseCase.Result.Failed)
    }
}