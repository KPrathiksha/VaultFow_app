package com.example.vaultflow

import android.content.Context
import android.content.Intent
import com.example.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): AuthResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).await()
            AuthResult.Success(auth.currentUser?.displayName ?: "User")
        } catch (e: com.google.android.gms.common.api.ApiException) {
            android.util.Log.e("GoogleAuthClient", "Sign in failed with status code: ${e.statusCode}")
            AuthResult.Error("Sign in failed: ${e.statusCode}")
        } catch (e: Exception) {
            android.util.Log.e("GoogleAuthClient", "Sign in failed", e)
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore("@") ?: "User")
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            AuthResult.Error("User not found. Signup here")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAuthClient", "Email sign in failed", e)
            AuthResult.Error(e.message ?: "Authentication failed")
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): AuthResult {
        return try {
            val userResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = userResult.user
            if (user != null && displayName.isNotBlank()) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Save user profile document in Firestore collection
                try {
                    val firestoreRepo = com.example.vaultflow.data.repository.FirestoreRepository()
                    val profile = com.example.vaultflow.data.model.UserProfile(
                        uid = user.uid,
                        displayName = displayName,
                        email = email
                    )
                    firestoreRepo.saveUserProfile(profile)
                } catch (fsEx: Exception) {
                    android.util.Log.e("FirebaseAuthClient", "Firestore profile document save failed", fsEx)
                }
            }
            AuthResult.Success(auth.currentUser?.displayName ?: displayName.ifBlank { "User" })
        } catch (e: Exception) {
            android.util.Log.e("FirebaseAuthClient", "Email sign up failed", e)
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}

sealed class AuthResult {
    data class Success(val userName: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}