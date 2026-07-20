package com.example.vaultflow.data.model

import com.google.firebase.Timestamp

data class Subscription(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val billingCycle: String = "Monthly", // Monthly, Yearly
    val nextBillingDate: Timestamp = Timestamp.now(),
    val category: String = "Entertainment"
)
