package com.example.foodieshaven

data class Food(
    val id: String = "",
    val categoryId: String = "",
    val title: String = "",
    val imageName: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val bestFood: Boolean = false
)
