package com.example.foodieshaven

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",  // Storing password (not recommended)
    val mobile: String = "",
    val address: String = "",
    val imageName: String = ""
)
