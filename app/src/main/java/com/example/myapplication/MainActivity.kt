package com.example.vaultflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vaultflow.ui.screens.*
import com.example.vaultflow.ui.theme.VaultFlowTheme
import com.example.vaultflow.ui.viewmodel.VaultViewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.plaid.link.OpenPlaidLink
import com.plaid.link.result.LinkSuccess

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            android.util.Log.d("FCM_TOKEN", "FCM Token: $token")
        }

        val linkLauncher = registerForActivityResult(OpenPlaidLink()) { result ->
            if (result is LinkSuccess) {
                // Handle success - result.publicToken
            }
        }

        setContent {
            VaultFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authClient = remember { GoogleAuthClient(this) }
                    var isLoggedIn by remember {
                        mutableStateOf(authClient.isUserSignedIn())
                    }
                    var userName by remember { 
                        mutableStateOf(authClient.getCurrentUser()?.displayName ?: "User") 
                    }

                    if (isLoggedIn) {
                        val navController = rememberNavController()
                        val vaultViewModel: VaultViewModel = viewModel()
                        
                        NavHost(navController = navController, startDestination = "dashboard") {
                            composable("dashboard") {
                                DashboardScreen(
                                    userName = userName,
                                    viewModel = vaultViewModel,
                                    onNavigate = { route -> navController.navigate(route) }
                                )
                            }
                            composable("savings") {
                                SavingsScreen(
                                    viewModel = vaultViewModel,
                                    onNavigate = { route -> navController.navigate(route) }
                                )
                            }
                            composable("ai_coach") {
                                AICoachScreen(
                                    viewModel = vaultViewModel,
                                    onNavigate = { route -> navController.navigate(route) }
                                )
                            }
                            composable("insights") {
                                InsightsScreen(
                                    viewModel = vaultViewModel,
                                    onNavigate = { route -> navController.navigate(route) }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    onLogout = {
                                        authClient.signOut()
                                        isLoggedIn = false
                                    },
                                    onNavigate = { route -> navController.navigate(route) }
                                )
                            }
                            composable("wallet") {
                                WalletScreen(
                                    onLinkBank = {
                                        // Plaid Link flow
                                        android.util.Log.d("Plaid", "Launch Plaid Link")
                                    },
                                    onNavigate = { route -> navController.navigate(route) }
                                )
                            }
                            composable("subscriptions") {
                                SubscriptionsScreen(
                                    viewModel = vaultViewModel,
                                    onNavigate = { route -> navController.navigate(route) }
                                )
                            }
                            composable("transactions") {
                                TransactionsScreen(
                                    viewModel = vaultViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            // Add other screens here
                        }
                    } else {
                        LoginScreen(
                            onLoginSuccess = { name ->
                                userName = name
                                isLoggedIn = true
                            }
                        )
                    }
                }
            }
        }
    }
}
