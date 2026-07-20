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
                ProfileMenuItemModern(Icons.Default.AutoAwesome, "AI Coach Settings", "Configure your financial assistant")
            }
            
            item {
                ProfileSectionHeader("App Preferences")
                ProfileMenuItemModern(Icons.Default.Security, "Privacy & Security", "Fingerprint & PIN")
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
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

@Preview
@Composable
fun ProfilePreview() {
    VaultFlowTheme {
        ProfileScreen()
    }
}
