package com.example.vaultflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaultflow.data.model.*
import com.example.vaultflow.data.repository.FirestoreRepository
import com.example.vaultflow.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.ai.client.generativeai.GenerativeModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class VaultViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private var geminiRepository = GeminiRepository("YOUR_GEMINI_API_KEY")

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions

    private val _bankAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts

    private val _linkedBanks = MutableStateFlow<List<LinkedBank>>(emptyList())
    val linkedBanks: StateFlow<List<LinkedBank>> = _linkedBanks

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _isProfileLoaded = MutableStateFlow(false)
    val isProfileLoaded: StateFlow<Boolean> = _isProfileLoaded

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance

    private val _aiNudge = MutableStateFlow("Loading smart insights...")
    val aiNudge: StateFlow<String> = _aiNudge

    private val _pendingAiAction = MutableStateFlow<PendingAiAction?>(null)
    val pendingAiAction: StateFlow<PendingAiAction?> = _pendingAiAction

    private val _chatMessages = MutableStateFlow<List<com.example.vaultflow.ui.screens.ChatMessage>>(
        listOf(com.example.vaultflow.ui.screens.ChatMessage("Hello! I'm your VaultFlow AI Coach. How can I help you manage your finances today?", false))
    )
    val chatMessages: StateFlow<List<com.example.vaultflow.ui.screens.ChatMessage>> = _chatMessages

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun clearData() {
        _transactions.value = emptyList()
        _savingsGoals.value = emptyList()
        _budgets.value = emptyList()
        _bankAccounts.value = emptyList()
        _userProfile.value = null
        _isProfileLoaded.value = false
        _totalBalance.value = 0.0
        _aiNudge.value = "Welcome to VaultFlow! Add your first transaction to get smart AI nudges."
        _chatMessages.value = listOf(com.example.vaultflow.ui.screens.ChatMessage("Hello! I'm your VaultFlow AI Coach. How can I help you manage your finances today?", false))
        loadData()
    }

    init {
        loadData()
    }

    private fun writeTransactionsToHistoryJson(list: List<Transaction>) {
        try {
            val array = org.json.JSONArray()
            list.forEach { tx ->
                val obj = org.json.JSONObject()
                obj.put("id", tx.id)
                obj.put("title", tx.title)
                obj.put("amount", tx.amount)
                obj.put("category", tx.category)
                obj.put("type", tx.type.name)
                obj.put("date", tx.date?.seconds ?: (System.currentTimeMillis() / 1000))
                array.put(obj)
            }
            val file = java.io.File("/data/data/com.example.vaultflow/files/history.json")
            file.parentFile?.let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
            file.writeText(array.toString(2), Charsets.UTF_8)
            android.util.Log.d("VaultViewModel", "Saved ${list.size} transactions to history.json")
        } catch (e: Exception) {
            android.util.Log.e("VaultViewModel", "Failed to write history.json: ${e.message}", e)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getTransactions().collectLatest {
                _transactions.value = it
                writeTransactionsToHistoryJson(it)
                updateAiNudge(it)
            }
        }
        viewModelScope.launch {
            repository.getSavingsGoals().collectLatest {
                _savingsGoals.value = it
            }
        }
        viewModelScope.launch {
            repository.getBudgets().collectLatest {
                _budgets.value = it
            }
        }
        viewModelScope.launch {
            repository.getSubscriptions().collectLatest {
                _subscriptions.value = it
            }
        }
        viewModelScope.launch {
            repository.getUserProfile().collectLatest {
                _userProfile.value = it
                _isProfileLoaded.value = true // Sync profile loaded status cleanly!
            }
        }
        viewModelScope.launch {
            repository.getBankAccounts().collectLatest {
                _bankAccounts.value = it
                // Dynamically sum up the balance of all bank accounts in real-time!
                _totalBalance.value = it.sumOf { acc -> acc.balance }
            }
        }
        viewModelScope.launch {
            repository.getLinkedBanks().collectLatest {
                _linkedBanks.value = it
            }
        }
    }

    private fun updateAiNudge(transactions: List<Transaction>) {
        if (transactions.isEmpty()) return
        viewModelScope.launch {
            val transText = transactions.take(10).joinToString { "${it.title}: ${it.amount}" }
            val nudge = geminiRepository.getSmartNudge(transText)
            if (nudge != null) {
                _aiNudge.value = nudge
            }
        }
    }

    fun clearPendingAiAction() {
        _pendingAiAction.value = null
    }

    fun confirmPendingAiAction() {
        val action = _pendingAiAction.value ?: return
        viewModelScope.launch {
            when (action) {
                is PendingAiAction.CreateSavingsGoal -> {
                    repository.addSavingsGoal(
                        com.example.vaultflow.data.model.SavingsGoal(
                            title = action.title,
                            targetAmount = action.amount,
                            currentAmount = 0.0
                        )
                    )
                    _chatMessages.value = _chatMessages.value + com.example.vaultflow.ui.screens.ChatMessage(
                        "Action Authorized! I have successfully created your '${action.title}' savings goal with a target of ₹${action.amount}! 🚀",
                        false
                    )
                }
                is PendingAiAction.CreateTransaction -> {
                    repository.addTransaction(
                        com.example.vaultflow.data.model.Transaction(
                            title = action.title,
                            amount = action.amount,
                            category = action.category,
                            type = action.type
                        )
                    )
                    _chatMessages.value = _chatMessages.value + com.example.vaultflow.ui.screens.ChatMessage(
                        "Action Authorized! I have successfully added your '${action.title}' transaction of ₹${action.amount} under '${action.category}'! 💸",
                        false
                    )
                }
            }
            _pendingAiAction.value = null
        }
    }

    fun sendMessage(text: String) {
        val userMsg = com.example.vaultflow.ui.screens.ChatMessage(text, true)
        _chatMessages.value = _chatMessages.value + userMsg
        
        viewModelScope.launch {
            _isTyping.value = true
            
            // Dynamic AI Write-Intent Parser!
            val lowerText = text.lowercase()
            if (lowerText.contains("saving") && (lowerText.contains("make") || lowerText.contains("create") || lowerText.contains("add"))) {
                val amountRegex = "\\d+".toRegex()
                val amount = amountRegex.find(lowerText)?.value?.toDoubleOrNull() ?: 250.0
                
                var title = "Medicine"
                if (lowerText.contains("medicine")) title = "Medicine"
                else if (lowerText.contains("travel")) title = "Travel"
                else if (lowerText.contains("education")) title = "Education"
                else if (lowerText.contains("bike")) title = "Bike"
                
                // Draft Action for User Confirmation Overlay!
                _pendingAiAction.value = PendingAiAction.CreateSavingsGoal(title, amount)
                
                val aiResponse = "I have prepared an AI Copilot action to create a savings goal for '$title' with a target of ₹$amount. Please click 'Confirm' on your screen to authorize and write it to your database! 🛡️"
                _chatMessages.value = _chatMessages.value + com.example.vaultflow.ui.screens.ChatMessage(aiResponse, false)
                _isTyping.value = false
                return@launch
            } else if (lowerText.contains("transaction") || lowerText.contains("spending") || lowerText.contains("add expense") || lowerText.contains("add income")) {
                val amountRegex = "\\d+".toRegex()
                val amount = amountRegex.find(lowerText)?.value?.toDoubleOrNull() ?: 100.0
                
                val isIncome = lowerText.contains("income") || lowerText.contains("salary") || lowerText.contains("deposit")
                val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                val category = if (isIncome) "Income" else "Entertainment"
                
                var title = "Coffee"
                if (lowerText.contains("coffee")) title = "Coffee"
                else if (lowerText.contains("food")) title = "Food"
                else if (lowerText.contains("rent")) title = "Rent"
                else if (lowerText.contains("salary")) title = "Salary"
                
                // Draft Action for User Confirmation Overlay!
                _pendingAiAction.value = PendingAiAction.CreateTransaction(title, amount, type, category)
                
                val aiResponse = "I have prepared an AI Copilot action to add a ${type.name.lowercase()} transaction for '$title' of ₹$amount. Please click 'Confirm' on your screen to authorize and write it to your database! 🛡️"
                _chatMessages.value = _chatMessages.value + com.example.vaultflow.ui.screens.ChatMessage(aiResponse, false)
                _isTyping.value = false
                return@launch
            }

            try {
                // Read the actual local history.json file for real-time AI context!
                val file = java.io.File("/data/data/com.example.vaultflow/files/history.json")
                val jsonHistoryText = if (file.exists()) {
                    file.readText(Charsets.UTF_8)
                } else {
                    "[]"
                }

                // Construct system prompt with local history context
                val promptWithContext = """
                    You are VaultFlow, an AI-powered financial copilot.
                    Below is the user's real financial transaction history, read dynamically from their local 'history.json' file:
                    $jsonHistoryText
                    
                    User's Question: $text
                    Please analyze their history.json data and provide a professional, personalized, and highly actionable response. Keep your reply concise, engaging, and clear.
                """.trimIndent()

                val response = geminiRepository.getGeneralResponse(promptWithContext) ?: "I'm having trouble connecting to my financial brain. Please check your internet!"
                _chatMessages.value = _chatMessages.value + com.example.vaultflow.ui.screens.ChatMessage(response, false)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + com.example.vaultflow.ui.screens.ChatMessage("Oops, I had trouble analyzing your local transaction file. Let's try again!", false)
            } finally {
                _isTyping.value = false
            }
        }
    }

    suspend fun getAiCoachResponse(prompt: String): String {
        return geminiRepository.getGeneralResponse(prompt) ?: "I'm sorry, I'm having trouble connecting to my financial brain. Please try again!"
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.addSavingsGoal(goal)
        }
    }

    fun updateSavingsGoalProgress(goalId: String, newCurrentAmount: Double) {
        viewModelScope.launch {
            repository.updateSavingsGoalProgress(goalId, newCurrentAmount)
        }
    }

    fun setBudget(budget: Budget) {
        viewModelScope.launch {
            repository.setBudget(budget)
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun addSubscription(sub: Subscription) {
        viewModelScope.launch {
            repository.addSubscription(sub)
        }
    }

    fun addBankAccount(account: BankAccount) {
        viewModelScope.launch {
            repository.addBankAccount(account)
        }
    }

    fun updateBankAccountBalance(accountId: String, newBalance: Double) {
        viewModelScope.launch {
            repository.updateBankAccountBalance(accountId, newBalance)
        }
    }

    fun addLinkedBank(bank: LinkedBank) {
        viewModelScope.launch {
            repository.addLinkedBank(bank)
        }
    }

    fun updateAiApiKey(newKey: String, newBaseUrl: String = "", newModelName: String = "gemini-flash-latest") {
        geminiRepository = GeminiRepository(newKey, newBaseUrl, newModelName)
        updateAiNudge(_transactions.value)
    }

    suspend fun fetchBestModel(key: String, baseUrl: String = ""): String = withContext(Dispatchers.IO) {
        val client = okhttp3.OkHttpClient()
        val hasKey = key.isNotBlank() && key != "none" && !key.startsWith("YOUR_GEMINI")
        val url = if (baseUrl.isNotBlank()) {
            val suffix = if (hasKey) "?key=$key" else ""
            if (baseUrl.endsWith("/")) "${baseUrl}v1beta/models$suffix" else "$baseUrl/v1beta/models$suffix"
        } else {
            "https://generativelanguage.googleapis.com/v1beta/models?key=$key"
        }
        
        try {
            val request = okhttp3.Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = org.json.JSONObject(body)
                    if (json.has("models")) {
                        val modelsArray = json.getJSONArray("models")
                        val modelNames = mutableListOf<String>()
                        for (i in 0 until modelsArray.length()) {
                            val modelObj = modelsArray.getJSONObject(i)
                            val name = modelObj.getString("name")
                            modelNames.add(name.substringAfter("models/"))
                        }
                        
                        when {
                            modelNames.contains("gemini-2.5-flash") -> "gemini-2.5-flash"
                            modelNames.contains("gemini-2.0-flash") -> "gemini-2.0-flash"
                            modelNames.contains("gemini-flash-latest") -> "gemini-flash-latest"
                            modelNames.contains("gemini-1.5-flash") -> "gemini-1.5-flash"
                            else -> modelNames.firstOrNull { it.contains("flash") || it.contains("pro") } ?: "gemini-flash-latest"
                        }
                    } else {
                        "gemini-flash-latest"
                    }
                } else {
                    "gemini-flash-latest"
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VaultViewModel", "Failed to fetch best model: ${e.message}")
            "gemini-flash-latest"
        }
    }

    suspend fun validateGeminiKey(testKey: String, testBaseUrl: String = ""): Boolean = withContext(Dispatchers.IO) {
        val hasKey = testKey.isNotBlank() && testKey != "none" && !testKey.startsWith("YOUR_GEMINI")
        if (testBaseUrl.isNotBlank()) {
            try {
                val client = okhttp3.OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val jsonPayload = """
                    {
                        "contents": [{
                            "parts": [{
                                "text": "Hi"
                            }]
                        }]
                    }
                """.trimIndent()
                val suffix = if (hasKey) "?key=$testKey" else ""
                val url = if (testBaseUrl.endsWith("/")) {
                    "${testBaseUrl}v1beta/models/gemini-1.5-flash:generateContent$suffix"
                } else {
                    "$testBaseUrl/v1beta/models/gemini-1.5-flash:generateContent$suffix"
                }

                val requestBody = jsonPayload.toRequestBody(mediaType)
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    response.isSuccessful
                }
            } catch (e: Exception) {
                android.util.Log.e("VaultViewModel", "Gemini key validation via custom proxy failed: ${e.message}")
                false
            }
        } else {
            try {
                val testModel = GenerativeModel(
                    modelName = "gemini-flash-latest",
                    apiKey = testKey
                )
                val response = testModel.generateContent(
                    com.google.ai.client.generativeai.type.content {
                        text("Hi")
                    }
                )
                response.text != null
            } catch (e: Exception) {
                android.util.Log.e("VaultViewModel", "Gemini Key Validation failed: ${e.message}")
                false
            }
        }
    }
}

sealed class PendingAiAction {
    data class CreateSavingsGoal(val title: String, val amount: Double) : PendingAiAction()
    data class CreateTransaction(val title: String, val amount: Double, val type: com.example.vaultflow.data.model.TransactionType, val category: String) : PendingAiAction()
}
