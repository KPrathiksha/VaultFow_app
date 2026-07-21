package com.example.vaultflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vaultflow.GoogleAuthClient
import com.example.vaultflow.ui.screens.*
import com.example.vaultflow.ui.theme.VaultFlowTheme
import com.example.vaultflow.ThemeConfig
import com.example.vaultflow.ui.theme.VaultIncome
import com.example.vaultflow.ui.theme.VaultPrimary
import com.example.vaultflow.ui.theme.VaultSecondary
import com.example.vaultflow.ui.viewmodel.VaultViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("vaultflow_prefs", android.content.Context.MODE_PRIVATE) }

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

                        // Check if App Lock is configured
                        val isAppLockEnabled = remember { prefs.getBoolean("is_app_locked", false) }
                        var isAppUnlocked by remember { mutableStateOf(!isAppLockEnabled) }

                        LaunchedEffect(Unit) {
                            val savedApiKey = prefs.getString("gemini_api_key", "") ?: ""
                            val savedBaseUrl = prefs.getString("gemini_base_url", "") ?: ""
                            val savedModelName = prefs.getString("gemini_model_name", "gemini-flash-latest") ?: "gemini-flash-latest"
                            if (savedApiKey.isNotBlank()) {
                                vaultViewModel.updateAiApiKey(savedApiKey, savedBaseUrl, savedModelName)
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            // Main App Flow
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
                                            vaultViewModel.clearData()
                                            isLoggedIn = false
                                        },
                                        onNavigate = { route -> navController.navigate(route) }
                                    )
                                }
                                composable("wallet") {
                                    WalletScreen(
                                        viewModel = vaultViewModel,
                                        onLinkBank = {
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
                            }

                            // Full-Screen MPIN Lock Screen Overlay (Animated fade out upon unlock!)
                            AnimatedVisibility(
                                visible = !isAppUnlocked,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                MpinLockScreen(
                                    viewModel = vaultViewModel,
                                    onUnlockSuccess = {
                                        isAppUnlocked = true
                                    }
                                )
                            }
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

@Composable
fun MpinLockScreen(
    viewModel: VaultViewModel,
    onUnlockSuccess: () -> Unit
) {
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var errorState by remember { mutableStateOf<String?>(null) }
    
    // Gradient matching Flutter's 0xFF6A1B9A to 0xFF9C4DCC
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6A1B9A), // Primary Purple
            Color(0xFF9C4DCC)  // Light Purple
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Translucent Shield Icon
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(width = 1.5.dp, color = Color.White.copy(alpha = 0.25f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "VaultFlow Secure",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Enter your 4-digit MPIN to unlock",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            // PIN Indicator Bullets
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isEntered = i < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isEntered) Color.White else Color.White.copy(alpha = 0.3f))
                            .border(width = 1.dp, color = Color.White, shape = CircleShape)
                    )
                }
            }

            errorState?.let {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Numeric Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("FP", "0", "DEL")
                )

                keys.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable {
                                        errorState = null
                                        when (key) {
                                            "DEL" -> {
                                                if (pinInput.isNotEmpty()) {
                                                    pinInput = pinInput.dropLast(1)
                                                }
                                            }
                                            "FP" -> {
                                                // Simulating Biometric System Prompt
                                                onUnlockSuccess()
                                            }
                                            else -> {
                                                if (pinInput.length < 4) {
                                                    pinInput += key
                                                    if (pinInput.length == 4) {
                                                        val activeAccount = bankAccounts.firstOrNull()
                                                        val correctPin = activeAccount?.pin ?: "1234"
                                                        if (pinInput == correctPin) {
                                                            onUnlockSuccess()
                                                        } else {
                                                            pinInput = ""
                                                            errorState = "Incorrect MPIN. Please try again!"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "DEL" -> Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
                                    "FP" -> Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = Color.White, modifier = Modifier.size(28.dp))
                                    else -> Text(text = key, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
