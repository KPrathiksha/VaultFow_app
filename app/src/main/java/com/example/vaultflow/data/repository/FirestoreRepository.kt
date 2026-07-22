package com.example.vaultflow.data.repository

import com.example.vaultflow.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    private fun getUserDoc() = userId?.let { firestore.collection("users").document(it) }

    fun getUserProfile(): Flow<UserProfile?> {
        val userDoc = getUserDoc() ?: return flowOf(null)
        return callbackFlow {
            val subscription = userDoc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreRepository", "UserProfile listener error: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(UserProfile::class.java)
                trySend(profile)
            }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        try {
            val userDoc = getUserDoc() ?: return
            userDoc.set(profile, SetOptions.merge()).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "saveUserProfile failed: ${e.message}")
        }
    }

    fun getTransactions(): Flow<List<Transaction>> {
        val userDoc = getUserDoc() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = userDoc.collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreRepository", "Transactions listener error: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val transactions = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            val id = doc.id
                            val title = doc.getString("title") ?: ""
                            val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
                            val category = doc.getString("category") ?: "General"
                            
                            val typeStr = doc.getString("type") ?: "EXPENSE"
                            val type = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
                            
                            val dateObj = doc.get("date")
                            val date = when (dateObj) {
                                is com.google.firebase.Timestamp -> dateObj
                                else -> com.google.firebase.Timestamp.now()
                            }
                            
                            Transaction(id, title, amount, category, type, date)
                        } catch (e: Exception) {
                            android.util.Log.e("FirestoreRepository", "Manual Transaction parse failed: ${e.message}")
                            null
                        }
                    } ?: emptyList()
                    trySend(transactions)
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        try {
            val userDoc = getUserDoc() ?: return
            
            // Query their bank accounts before launching the transaction
            val bankAccountsSnapshot = userDoc.collection("bank_accounts").get().await()
            val firstAccountDoc = bankAccountsSnapshot.documents.firstOrNull()

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
                
                // Also update the balance of their active bank account document in Firestore!
                if (firstAccountDoc != null) {
                    val accRef = firstAccountDoc.reference
                    val currentAccBalance = firstAccountDoc.getDouble("balance") ?: 0.0
                    val newAccBalance = if (transaction.type == TransactionType.INCOME) {
                        currentAccBalance + transaction.amount
                    } else {
                        currentAccBalance - transaction.amount
                    }
                    fireTransaction.update(accRef, "balance", newAccBalance)
                }

                val transactionRef = userDoc.collection("transactions").document()
                fireTransaction.set(transactionRef, transaction.copy(id = transactionRef.id))
            }.await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "addTransaction failed: ${e.message}")
        }
    }

    fun getSubscriptions(): Flow<List<Subscription>> {
        val userDoc = getUserDoc() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = userDoc.collection("subscriptions")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreRepository", "Subscriptions listener error: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val subs = snapshot?.toObjects(Subscription::class.java) ?: emptyList()
                    trySend(subs)
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun addSubscription(sub: Subscription) {
        try {
            val userDoc = getUserDoc() ?: return
            userDoc.collection("subscriptions").add(sub).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "addSubscription failed: ${e.message}")
        }
    }

    fun getSavingsGoals(): Flow<List<SavingsGoal>> {
        val userDoc = getUserDoc() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = userDoc.collection("savings_goals")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreRepository", "SavingsGoals listener error: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val goals = snapshot?.toObjects(SavingsGoal::class.java) ?: emptyList()
                    trySend(goals)
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun addSavingsGoal(goal: SavingsGoal) {
        try {
            val userDoc = getUserDoc() ?: return
            val goalRef = userDoc.collection("savings_goals").document()
            goalRef.set(goal.copy(id = goalRef.id)).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "addSavingsGoal failed: ${e.message}")
        }
    }

    suspend fun updateSavingsGoalProgress(goalId: String, newCurrentAmount: Double) {
        try {
            val userDoc = getUserDoc() ?: return
            userDoc.collection("savings_goals").document(goalId).update("currentAmount", newCurrentAmount).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "updateSavingsGoalProgress failed: ${e.message}")
        }
    }

    fun getBudgets(): Flow<List<Budget>> {
        val userDoc = getUserDoc() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = userDoc.collection("budgets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreRepository", "Budgets listener error: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val budgets = snapshot?.toObjects(Budget::class.java) ?: emptyList()
                    trySend(budgets)
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun setBudget(budget: Budget) {
        try {
            val userDoc = getUserDoc() ?: return
            userDoc.collection("budgets").document(budget.category).set(budget).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "setBudget failed: ${e.message}")
        }
    }
    
    suspend fun getUserBalance(): Double {
        return try {
            val userDoc = getUserDoc() ?: return 0.0
            val snapshot = userDoc.get().await()
            snapshot.getDouble("totalBalance") ?: 0.0
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "getUserBalance failed: ${e.message}")
            0.0
        }
    }

    fun getBankAccounts(): Flow<List<BankAccount>> {
        val userDoc = getUserDoc() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = userDoc.collection("bank_accounts")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreRepository", "BankAccounts listener error: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val accounts = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            BankAccount(
                                id = doc.id,
                                bankName = doc.getString("bankName") ?: "",
                                accountHolder = doc.getString("accountHolder") ?: "",
                                accountNumber = doc.getString("accountNumber") ?: "",
                                balance = (doc.get("balance") as? Number)?.toDouble() ?: 0.0,
                                pin = doc.getString("pin") ?: ""
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("FirestoreRepository", "Manual BankAccount parse failed: ${e.message}")
                            null
                        }
                    } ?: emptyList()

                    val decryptedAccounts = accounts.map { acc ->
                        acc.copy(
                            accountNumber = com.example.vaultflow.util.CryptoHelper.decrypt(acc.accountNumber),
                            accountHolder = com.example.vaultflow.util.CryptoHelper.decrypt(acc.accountHolder),
                            pin = com.example.vaultflow.util.CryptoHelper.decrypt(acc.pin)
                        )
                    }
                    trySend(decryptedAccounts)
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun addBankAccount(account: BankAccount) {
        try {
            val userDoc = getUserDoc() ?: return
            val accRef = userDoc.collection("bank_accounts").document()
            val encryptedAccount = account.copy(
                id = accRef.id,
                accountNumber = com.example.vaultflow.util.CryptoHelper.encrypt(account.accountNumber),
                accountHolder = com.example.vaultflow.util.CryptoHelper.encrypt(account.accountHolder),
                pin = com.example.vaultflow.util.CryptoHelper.encrypt(account.pin)
            )
            accRef.set(encryptedAccount).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "addBankAccount failed: ${e.message}")
        }
    }

    suspend fun updateBankAccountBalance(accountId: String, newBalance: Double) {
        try {
            val userDoc = getUserDoc() ?: return
            userDoc.collection("bank_accounts").document(accountId).update("balance", newBalance).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "updateBankAccountBalance failed: ${e.message}")
        }
    }

    fun getLinkedBanks(): Flow<List<LinkedBank>> {
        val userDoc = getUserDoc() ?: return flowOf(emptyList())
        return callbackFlow {
            val subscription = userDoc.collection("linked_banks")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreRepository", "LinkedBanks listener error: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val banks = snapshot?.toObjects(LinkedBank::class.java) ?: emptyList()
                    trySend(banks)
                }
            awaitClose { subscription.remove() }
        }
    }

    suspend fun addLinkedBank(bank: LinkedBank) {
        try {
            val userDoc = getUserDoc() ?: return
            val bankRef = userDoc.collection("linked_banks").document()
            bankRef.set(bank.copy(id = bankRef.id)).await()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreRepository", "addLinkedBank failed: ${e.message}")
        }
    }
}
