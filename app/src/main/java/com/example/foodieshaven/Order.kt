package com.example.foodieshaven

data class Order(
    val id: String = "",
    val foodName: String = "",
    val foodPrice: Double = 0.0,
    val imageName: String = "",
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,  // Total price is now part of the Order model
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val paymentMethod: String = "",
    val status: String? = "Pending"
)

