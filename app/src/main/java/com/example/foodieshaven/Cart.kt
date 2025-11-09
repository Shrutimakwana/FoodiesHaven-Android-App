package com.example.foodieshaven

data class Cart(
    val id: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val imageName: String = "",
    var quantity: Int = 1 // Default quantity set to 1
)
