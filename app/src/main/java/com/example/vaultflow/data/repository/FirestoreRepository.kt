package com.example.vaultflow.data.repository

import com.example.vaultflow.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    private fun getUserDoc() = userId?.let { firestore.collection("users").document(it) }

    fun getUserProfile(): Flow<UserProfile?> = callbackFlow {
        val userDoc = getUserDoc() ?: return@callbackFlow
        val subscription = userDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val profile = snapshot?.toObject(UserProfile::class.java)
            trySend(profile)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val userDoc = getUserDoc() ?: return
        userDoc.set(profile, SetOptions.merge()).await()
    }

    fun getTransactions(): Flow<List<Transaction>> = callbackFlow {
        val userDoc = getUserDoc() ?: return@callbackFlow
        val subscription = userDoc.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
                trySend(transactions)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addTransaction(transaction: Transaction) {
        val userDoc = getUserDoc() ?: return
        firestore.runTransaction { fireTransaction ->
            val userSnapshot = try {
                fireTransaction.get(userDoc)
            } catch (e: Exception) {
                null
            }
            
            val currentBalance = if (userSnapshot?.exists() == true) {
                userSnapshot.getDouble("totalBalance") ?: 0.0
            } else {
                0.0
            }
            
            val newBalance = if (transaction.type == TransactionType.INCOME) {
                currentBalance + transaction.amount
            } else {
                currentBalance - transaction.amount
            }
            
            if (userSnapshot?.exists() == true) {
                fireTransaction.update(userDoc, "totalBalance", newBalance)
            } else {
                fireTransaction.set(userDoc, mapOf("totalBalance" to newBalance), SetOptions.merge())
            }
            
            val transactionRef = userDoc.collection("transactions").document()
            fireTransaction.set(transactionRef, transaction.copy(id = transactionRef.id))
        }.await()
    }

    fun getSubscriptions(): Flow<List<Subscription>> = callbackFlow {
        val userDoc = getUserDoc() ?: return@callbackFlow
        val subscription = userDoc.collection("subscriptions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val subs = snapshot?.toObjects(Subscription::class.java) ?: emptyList()
                trySend(subs)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addSubscription(sub: Subscription) {
        val userDoc = getUserDoc() ?: return
        userDoc.collection("subscriptions").add(sub).await()
    }

    fun getSavingsGoals(): Flow<List<SavingsGoal>> = callbackFlow {
        val userDoc = getUserDoc() ?: return@callbackFlow
        val subscription = userDoc.collection("savings_goals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val goals = snapshot?.toObjects(SavingsGoal::class.java) ?: emptyList()
                trySend(goals)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addSavingsGoal(goal: SavingsGoal) {
        val userDoc = getUserDoc() ?: return
        userDoc.collection("savings_goals").add(goal).await()
    }

    fun getBudgets(): Flow<List<Budget>> = callbackFlow {
        val userDoc = getUserDoc() ?: return@callbackFlow
        val subscription = userDoc.collection("budgets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val budgets = snapshot?.toObjects(Budget::class.java) ?: emptyList()
                trySend(budgets)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun setBudget(budget: Budget) {
        val userDoc = getUserDoc() ?: return
        userDoc.collection("budgets").document(budget.category).set(budget).await()
    }
    
    suspend fun getUserBalance(): Double {
        val userDoc = getUserDoc() ?: return 0.0
        val snapshot = userDoc.get().await()
        return snapshot.getDouble("totalBalance") ?: 0.0
    }
}
