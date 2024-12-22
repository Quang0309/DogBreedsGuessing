package com.example.dogbreedsguessing.data.model

import com.google.gson.annotations.SerializedName

data class DogImagesResponse(
    @SerializedName("message")
    val message: List<String>, // List of image URLs
    val status: String         // Status of the response
)
