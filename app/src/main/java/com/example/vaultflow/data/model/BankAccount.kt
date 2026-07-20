package com.example.vaultflow.data.model

data class BankAccount(
    val id: String = "",
    val bankName: String = "",
    val accountHolder: String = "",
    val accountNumber: String = "",
    val balance: Double = 0.0,
    val pin: String = "" // 4-digit security PIN for UPI transfers
)
