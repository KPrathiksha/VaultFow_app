package com.example.vaultflow.data.model

data class SavingsChallenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val rewardPoints: Int = 0,
    val isCompleted: Boolean = false
)
