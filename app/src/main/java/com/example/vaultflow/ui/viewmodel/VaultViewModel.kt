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

class VaultViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val geminiRepository = GeminiRepository("AIzaSyCwBww3l4dMGwK_0QAYLXoTNBzM2whACE4")

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance

    private val _aiNudge = MutableStateFlow("Loading smart insights...")
    val aiNudge: StateFlow<String> = _aiNudge

    private val _chatMessages = MutableStateFlow<List<com.example.vaultflow.ui.screens.ChatMessage>>(
        listOf(com.example.vaultflow.ui.screens.ChatMessage("Hello! I'm your VaultFlow AI Coach. How can I help you manage your finances today?", false))
    )
    val chatMessages: StateFlow<List<com.example.vaultflow.ui.screens.ChatMessage>> = _chatMessages

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getTransactions().collectLatest {
                _transactions.value = it
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
            }
        }
        viewModelScope.launch {
            val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "")
            
            userDoc.addSnapshotListener { snapshot, _ ->
                _totalBalance.value = snapshot?.getDouble("totalBalance") ?: 0.0
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

    fun sendMessage(text: String) {
        val userMsg = com.example.vaultflow.ui.screens.ChatMessage(text, true)
        _chatMessages.value = _chatMessages.value + userMsg
        
        viewModelScope.launch {
            _isTyping.value = true
            try {
                val response = geminiRepository.getGeneralResponse(text) ?: "I'm having trouble connecting to my financial brain. Please check your internet!"
                _chatMessages.value = _chatMessages.value + com.example.vaultflow.ui.screens.ChatMessage(response, false)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + com.example.vaultflow.ui.screens.ChatMessage("Oops, something went wrong. Let's try that again!", false)
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
}
