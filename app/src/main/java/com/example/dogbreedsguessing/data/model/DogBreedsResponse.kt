package com.example.dogbreedsguessing.data.model

import com.google.gson.annotations.SerializedName

data class DogBreedsResponse(
    @SerializedName("message")
    val message: Map<String, List<String>>, // { breadType : [subBreadTypes]}
    @SerializedName("status")
    val status: String // Status of the response
)