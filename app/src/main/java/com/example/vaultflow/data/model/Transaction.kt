package com.example.vaultflow.data.model

import com.google.firebase.Timestamp

data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "General",
    val type: TransactionType = TransactionType.EXPENSE,
    val date: Timestamp = Timestamp.now()
)

enum class TransactionType {
    INCOME, EXPENSE
}
