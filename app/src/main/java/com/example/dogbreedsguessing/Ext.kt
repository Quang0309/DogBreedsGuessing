package com.example.dogbreedsguessing

fun <T> List<T>.getRandomItems(count: Int): List<T> {
    return if (count > this.size) {
        this.shuffled() // Return the entire list if count exceeds the list size
    } else {
        this.shuffled().take(count)
    }
}