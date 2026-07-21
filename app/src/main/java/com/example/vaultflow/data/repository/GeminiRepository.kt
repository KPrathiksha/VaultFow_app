package com.example.vaultflow.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject

class GeminiRepository(private val apiKey: String, private val baseUrl: String = "") {
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-flash-latest",
        apiKey = apiKey
    )

    suspend fun getSmartNudge(transactions: String): String? = withContext(Dispatchers.IO) {
        val prompt = "Analyze these financial transactions and provide a short, helpful saving nudge (max 20 words): $transactions"
        if (baseUrl.isNotBlank()) {
            callGeminiViaOkHttp(prompt)
        } else {
            try {
                val response = generativeModel.generateContent(
                    content { text(prompt) }
                )
                response.text
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getGeneralResponse(prompt: String): String? = withContext(Dispatchers.IO) {
        val styledPrompt = "You are VaultFlow AI, a professional financial coach. Answer this user query concisely and helpfuly: $prompt"
        if (baseUrl.isNotBlank()) {
            callGeminiViaOkHttp(styledPrompt)
        } else {
            try {
                android.util.Log.d("Gemini", "Sending prompt to Gemini SDK: $styledPrompt")
                val response = generativeModel.generateContent(
                    content { text(styledPrompt) }
                )
                response.text
            } catch (e: Exception) {
                android.util.Log.e("Gemini", "Error calling Gemini SDK API", e)
                null
            }
        }
    }

    private fun callGeminiViaOkHttp(prompt: String): String? {
        try {
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            
            val cleanPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n")
            val jsonPayload = """
                {
                    "contents": [{
                        "parts": [{
                            "text": "$cleanPrompt"
                        }]
                    }]
                }
            """.trimIndent()
            
            val url = if (baseUrl.endsWith("/")) {
                "${baseUrl}v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            } else {
                "$baseUrl/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            }
            
            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toRequestBody(mediaType))
                .build()
                
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody)
                    val candidates = json.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.getJSONObject("content")
                    val parts = contentObj.getJSONArray("parts")
                    return parts.getJSONObject(0).getString("text")
                } else {
                    android.util.Log.e("Gemini", "OkHttp custom proxy call failed with status: ${response.code}")
                    return null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Gemini", "OkHttp custom proxy call exception: ${e.message}", e)
            return null
        }
    }
}
