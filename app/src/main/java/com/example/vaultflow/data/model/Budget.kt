package com.example.vaultflow.data.model

data class Budget(
    val id: String = "",
    val category: String = "",
    val limitAmount: Double = 0.0,
    val spentAmount: Double = 0.0
)
