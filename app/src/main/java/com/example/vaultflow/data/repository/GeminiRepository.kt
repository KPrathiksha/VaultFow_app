package com.example.vaultflow.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository(apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun getSmartNudge(transactions: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(
                content {
                    text("Analyze these financial transactions and provide a short, helpful saving nudge (max 20 words): $transactions")
                }
            )
            response.text
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGeneralResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("Gemini", "Sending prompt to Gemini: $prompt")
            val response = generativeModel.generateContent(
                content {
                    text("You are VaultFlow AI, a professional financial coach. Answer this user query concisely and helpfuly: $prompt")
                }
            )
            android.util.Log.d("Gemini", "Received response from Gemini: ${response.text}")
            response.text
        } catch (e: Exception) {
            android.util.Log.e("Gemini", "Error calling Gemini API", e)
            null
        }
    }
}
