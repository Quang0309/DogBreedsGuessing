package com.example.dogbreedsguessing.data

import com.example.dogbreedsguessing.data.model.DogBreedsResponse
import com.example.dogbreedsguessing.data.model.DogImagesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface RemoteDogService {

    @GET("/api/breeds/list/all")
    suspend fun fetchAllBreeds(): DogBreedsResponse

    @GET("/api/breed/{type}/images/random/{count}")
    suspend fun fetchImagesByBreed(
        @Path("type") breedType: String,
        @Path("count") count: Int = 2
    ): DogImagesResponse
}