package com.example.vaultflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vaultflow.data.model.UserProfile
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.vaultflow.ui.components.VaultBottomNavigation
import com.example.vaultflow.ui.theme.*
import com.example.vaultflow.ui.viewmodel.VaultViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.*

@Composable
fun ProfileScreen(
    viewModel: VaultViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userProfile by viewModel.userProfile.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showApiDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("vaultflow_prefs", android.content.Context.MODE_PRIVATE) }
    var isAppLocked by remember { mutableStateOf(prefs.getBoolean("is_app_locked", false)) }
    
    val profile = userProfile ?: UserProfile(
        uid = user?.uid ?: "",
        displayName = user?.displayName ?: "User",
        email = user?.email ?: ""
    )

    if (showEditDialog) {
        EditProfileDialog(
            currentProfile = profile,
            onDismiss = { showEditDialog = false },
            onConfirm = { updated ->
                viewModel.saveProfile(updated)
                showEditDialog = false
            }
        )
    }

    if (showApiDialog) {
        ConfigureApiDialog(
            onDismiss = { showApiDialog = false },
            onConfirm = { customKey, customBaseUrl, bestModel ->
                prefs.edit()
                    .putString("gemini_api_key", customKey)
                    .putString("gemini_base_url", customBaseUrl)
                    .putString("gemini_model_name", bestModel)
                    .apply()
                viewModel.updateAiApiKey(customKey, customBaseUrl, bestModel)
                showApiDialog = false
            }
        )
    }

    if (showSecurityDialog) {
        SecuritySettingsDialog(
            currentLockState = isAppLocked,
            onDismiss = { showSecurityDialog = false },
            onConfirm = { newState ->
                prefs.edit().putBoolean("is_app_locked", newState).apply()
                isAppLocked = newState
                showSecurityDialog = false
            }
        )
    }

    Scaffold(
        bottomBar = { VaultBottomNavigation(currentRoute = "profile", onNavigate = onNavigate) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VaultBackgroundLight)
        ) {
            item {
                ModernProfileHeader(profile, onEditClick = { showEditDialog = true })
            }
            
            item {
                FinancialQuickSummary(totalBalance, savingsGoals.size)
            }
            
            item {
                ProfileSectionHeader("Account Settings")
                ProfileMenuItemModern(Icons.Default.Person, "Personal Information", "Manage your personal data", onClick = { showEditDialog = true })
                ProfileMenuItemModern(Icons.Default.AccountBalance, "Bank Accounts", "Linked with Plaid", onClick = { onNavigate("wallet") })
                ProfileMenuItemModern(Icons.Default.History, "Transaction History", "View full logs", onClick = { onNavigate("transactions") })
            }
            
            item {
                ProfileSectionHeader("Financial Tools")
                ProfileMenuItemModern(Icons.Default.AccountBalanceWallet, "My Wallet", "Stored cards & UPI", onClick = { onNavigate("wallet") })
                ProfileMenuItemModern(Icons.Default.PieChart, "Budget Planner", "Manage limits", onClick = { onNavigate("insights") })
                ProfileMenuItemModern(Icons.Default.Subscriptions, "Subscriptions", "Manage recurring payments", onClick = { onNavigate("subscriptions") })
                ProfileMenuItemModern(Icons.Default.Savings, "Savings Goals", "View active targets", onClick = { onNavigate("savings") })
                ProfileMenuItemModern(Icons.Default.AutoAwesome, "AI Coach Settings", "Configure your financial assistant", onClick = { showApiDialog = true })
            }
            
            item {
                ProfileSectionHeader("App Preferences")
                ProfileMenuItemModern(Icons.Default.Security, "Privacy & Security", if (isAppLocked) "App Lock: Enabled" else "App Lock: Disabled", onClick = { showSecurityDialog = true })
                ProfileMenuItemModern(Icons.Default.Notifications, "Notifications", "Alerts & Nudges")
                ProfileMenuItemModern(Icons.AutoMirrored.Filled.Help, "Help & Support", "FAQs & Contact Us")
                ProfileMenuItemModern(Icons.Default.Info, "About VaultFlow", "Version 2.1.0")
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                LogoutButton(onLogout)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ModernProfileHeader(profile: UserProfile, onEditClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(VaultPrimary, VaultSecondary)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = profile.displayName.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = VaultPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = profile.displayName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (profile.jobTitle.isNotBlank()) profile.jobTitle else "VaultFlow Member",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Edit Profile", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FinancialQuickSummary(balance: Double, goalsCount: Int) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .offset(y = (-30).dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VaultSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Balance", color = VaultTextLight, fontSize = 12.sp)
                Text(currencyFormatter.format(balance), fontWeight = FontWeight.Bold, color = VaultPrimary, fontSize = 16.sp)
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VaultSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Active Goals", color = VaultTextLight, fontSize = 12.sp)
                Text("$goalsCount Goals", fontWeight = FontWeight.Bold, color = VaultIncome, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        color = VaultPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
}

@Composable
fun ProfileMenuItemModern(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = VaultPrimary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, color = VaultTextDark, fontSize = 16.sp)
            Text(text = subtitle, color = VaultTextLight, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VaultTextLight.copy(alpha = 0.5f))
    }
}

@Composable
fun LogoutButton(onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VaultExpense.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = VaultExpense)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Account", color = VaultExpense, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.displayName) }
    var job by remember { mutableStateOf(currentProfile.jobTitle) }
    var phone by remember { mutableStateOf(currentProfile.phoneNumber) }
    var location by remember { mutableStateOf(currentProfile.location) }
    var bio by remember { mutableStateOf(currentProfile.bio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = job, onValueChange = { job = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentProfile.copy(displayName = name, jobTitle = job, phoneNumber = phone, location = location, bio = bio)) },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ConfigureApiDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("vaultflow_prefs", android.content.Context.MODE_PRIVATE) }
    
    val savedKey = prefs.getString("gemini_api_key", "") ?: ""
    val savedUrl = prefs.getString("gemini_base_url", "") ?: ""
    val initialInput = if (savedUrl.isNotBlank()) savedUrl else savedKey

    var apiInput by remember { mutableStateOf(initialInput) }
    var keyVisible by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var feedbackState by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val viewModel: VaultViewModel = viewModel()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure AI API Key", fontWeight = FontWeight.Bold, color = VaultTextDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure your personal Google Gemini API Key or Custom Proxy Base URL to power your AI Coach natively.",
                    fontSize = 12.sp,
                    color = VaultTextLight
                )

                OutlinedTextField(
                    value = apiInput,
                    onValueChange = { apiInput = it },
                    label = { Text("Gemini API Key or Proxy URL") },
                    placeholder = { Text("Enter AIzaSy... or https://...") },
                    singleLine = true,
                    enabled = !isValidating && !isSuccess,
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (keyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { keyVisible = !keyVisible }, enabled = !isValidating && !isSuccess) {
                            Icon(imageVector = image, contentDescription = "Toggle visibility")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isValidating) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = VaultPrimary)
                        Text("Verifying API Credentials...", fontSize = 12.sp, color = VaultTextLight)
                    }
                }

                feedbackState?.let {
                    Text(
                        text = it,
                        color = if (isSuccess) VaultIncome else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedInput = apiInput.trim()
                    if (trimmedInput.isBlank()) {
                        feedbackState = "Please enter an API Key or Proxy URL"
                        return@Button
                    }
                    
                    if (isSuccess) {
                        onDismiss()
                        return@Button
                    }

                    coroutineScope.launch {
                        isValidating = true
                        feedbackState = null
                        
                        val isUrl = trimmedInput.startsWith("http://", true) || trimmedInput.startsWith("https://", true)
                        val testKey = if (isUrl) "dummy_key_for_proxy" else trimmedInput
                        val testBaseUrl = if (isUrl) trimmedInput else ""

                        // Execute on-device validation with custom credentials
                        val isValid = viewModel.validateGeminiKey(testKey, testBaseUrl)
                        
                        if (isValid) {
                            val bestModel = viewModel.fetchBestModel(testKey, testBaseUrl)
                            isValidating = false
                            isSuccess = true
                            feedbackState = "Added successfully! Active Model: $bestModel"
                            onConfirm(testKey, testBaseUrl, bestModel)
                        } else {
                            isValidating = false
                            isSuccess = false
                            feedbackState = "Invalid API setup. Please verify credentials!"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSuccess) VaultIncome else VaultPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isSuccess) "Close" else "Verify & Add")
            }
        },
        dismissButton = {
            if (!isSuccess) {
                TextButton(onClick = onDismiss, enabled = !isValidating) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun SecuritySettingsDialog(
    currentLockState: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var isToggled by remember { mutableStateOf(currentLockState) }
    var pinInput by remember { mutableStateOf("") }
    var errorState by remember { mutableStateOf<String?>(null) }
    
    val viewModel: VaultViewModel = viewModel()
    val bankAccounts by viewModel.bankAccounts.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy & Security", fontWeight = FontWeight.Bold, color = VaultTextDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enabling App Lock will require entering your secure 4-digit MPIN on startup to unlock the application.",
                    fontSize = 12.sp,
                    color = VaultTextLight
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable MPIN App Lock", fontWeight = FontWeight.SemiBold, color = VaultTextDark)
                    Switch(
                        checked = isToggled,
                        onCheckedChange = { isToggled = it }
                    )
                }

                if (isToggled != currentLockState) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Please enter your 4-digit Security PIN to authorize change:", fontSize = 12.sp, color = VaultTextLight)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                        label = { Text("Security PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorState?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isToggled == currentLockState) {
                        onDismiss()
                        return@Button
                    }
                    
                    val activeAccount = bankAccounts.firstOrNull()
                    val correctPin = activeAccount?.pin ?: "1234"

                    if (pinInput == correctPin) {
                        onConfirm(isToggled)
                    } else {
                        errorState = "Incorrect security PIN. Please try again!"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Confirm Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview
@Composable
fun ProfilePreview() {
    VaultFlowTheme {
        ProfileScreen()
    }
}
