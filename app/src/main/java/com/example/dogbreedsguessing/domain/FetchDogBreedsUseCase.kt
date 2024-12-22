package com.example.dogbreedsguessing.domain

import com.example.dogbreedsguessing.data.RemoteDogService
import com.example.dogbreedsguessing.getRandomItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchDogBreedsUseCase @Inject constructor(
    private val apiService: RemoteDogService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    data class DogBreedInfo(
        val name: String,
        val image: String // images URL of this breed
    )

    sealed interface Result {
        data class Success(val breedInfoList: List<DogBreedInfo>): Result
        data object Failed: Result
    }

    suspend operator fun invoke(breadCount: Int = 20, imagePerBreed: Int = 2): Result {
        return withContext(ioDispatcher) {
            try {
                val selectedBreadList = getSelectedBreeds(breadCount)
                val breedInfoList = constructDogBreedInfoList(selectedBreadList, imagePerBreed)
                Result.Success(breedInfoList.shuffled())
            } catch (ex: Exception) {
                Result.Failed
            }
        }
    }

    private suspend fun constructDogBreedInfoList(breedList: List<String>, imagePerBreed: Int): List<DogBreedInfo> {
        val lock = Any()
        val result = mutableListOf<DogBreedInfo>()
        return coroutineScope {
            breedList.forEach { breedType ->
                launch {
                    val imagesByBreedResponse = apiService.fetchImagesByBreed(
                        breedType = breedType,
                        count = imagePerBreed
                    )
                    synchronized(lock) { // make sure it is thread-safe to write to mutableList
                        result.addAll(
                            imagesByBreedResponse.message.map { url ->
                                DogBreedInfo(name = breedType, image = url)
                            }
                        )
                    }
                }
            }
            result
        }
    }

    private suspend fun getSelectedBreeds(breedCount: Int = 20): List<String> {
        val fetchAllBreedsResponse = apiService.fetchAllBreeds()
        val breedList = fetchAllBreedsResponse.message.keys // todo: should catch this breedList because it wont change much
        return breedList.toList().getRandomItems(breedCount)
    }
}