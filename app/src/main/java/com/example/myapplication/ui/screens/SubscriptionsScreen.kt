package com.example.vaultflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vaultflow.data.model.Subscription
import com.example.vaultflow.ui.components.VaultBottomNavigation
import com.example.vaultflow.ui.theme.*
import com.example.vaultflow.ui.viewmodel.VaultViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    viewModel: VaultViewModel = viewModel(),
    onNavigate: (String) -> Unit = {}
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddSubscriptionDialog(
            bankAccounts = bankAccounts,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, cycle, category ->
                viewModel.addSubscription(
                    Subscription(
                        name = name,
                        amount = amount,
                        billingCycle = cycle,
                        category = category
                    )
                )
                viewModel.addTransaction(
                    com.example.vaultflow.data.model.Transaction(
                        title = "Subscription: $name",
                        amount = amount,
                        category = category,
                        type = com.example.vaultflow.data.model.TransactionType.EXPENSE
                    )
                )
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Subscriptions", fontWeight = FontWeight.Bold, color = VaultTextDark) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VaultBackgroundLight)
            )
        },
        bottomBar = { VaultBottomNavigation(currentRoute = "subscriptions", onNavigate = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = VaultPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subscription")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VaultBackgroundLight)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SubscriptionSummary(subscriptions)
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (subscriptions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No subscriptions tracked yet.", color = VaultTextLight)
                    }
                }
            } else {
                val ottServices = subscriptions.filter { it.category.equals("OTT", true) }
                val wifiServices = subscriptions.filter { it.category.equals("WIFI", true) }
                val mobileServices = subscriptions.filter { it.category.equals("MOBILE", true) }
                val otherServices = subscriptions.filter { 
                    !it.category.equals("OTT", true) && 
                    !it.category.equals("WIFI", true) && 
                    !it.category.equals("MOBILE", true)
                }

                // 1. OTT Services
                if (ottServices.isNotEmpty()) {
                    item {
                        Text(
                            text = "🍿 OTT Services",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = VaultTextDark,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(ottServices) { SubscriptionItem(it) }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }

                // 2. Wi-Fi & Broadband Services
                if (wifiServices.isNotEmpty()) {
                    item {
                        Text(
                            text = "🌐 Wi-Fi & Broadband",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = VaultTextDark,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(wifiServices) { SubscriptionItem(it) }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }

                // 3. Mobile Recharges
                if (mobileServices.isNotEmpty()) {
                    item {
                        Text(
                            text = "📱 Mobile Recharges",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = VaultTextDark,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(mobileServices) { SubscriptionItem(it) }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }

                // 4. Other Services
                if (otherServices.isNotEmpty()) {
                    item {
                        Text(
                            text = "⚙️ Other Services",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = VaultTextDark,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(otherServices) { SubscriptionItem(it) }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@Composable
fun SubscriptionSummary(subs: List<Subscription>) {
    val totalMonthly = subs.sumOf { if (it.billingCycle == "Monthly") it.amount else it.amount / 12 }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VaultSurface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Monthly recurring expense", color = VaultTextLight, fontSize = 14.sp)
            Text(currencyFormatter.format(totalMonthly), color = VaultPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SubscriptionItem(sub: Subscription) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = VaultPrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Subscriptions, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sub.name, fontWeight = FontWeight.Bold, color = VaultTextDark)
                Text(sub.billingCycle, fontSize = 12.sp, color = VaultTextLight)
            }
            Text(currencyFormatter.format(sub.amount), fontWeight = FontWeight.Bold, color = VaultExpense)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSubscriptionDialog(
    bankAccounts: List<com.example.vaultflow.data.model.BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, cycle: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf("Monthly") }
    var category by remember { mutableStateOf("OTT") }
    var errorState by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subscription", fontWeight = FontWeight.Bold, color = VaultTextDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Category:", fontSize = 12.sp, color = VaultTextLight)
                
                // FlowRow wrapping the category radio buttons beautifully
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = category == "OTT", onClick = { category = "OTT" })
                        Text("🍿 OTT", color = VaultTextDark, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = category == "WIFI", onClick = { category = "WIFI" })
                        Text("🌐 Wi-Fi", color = VaultTextDark, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = category == "MOBILE", onClick = { category = "MOBILE" })
                        Text("📱 Mobile", color = VaultTextDark, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = category == "Other", onClick = { category = "Other" })
                        Text("⚙️ Other", color = VaultTextDark, fontSize = 12.sp)
                    }
                }

                Text("Billing Cycle:", fontSize = 12.sp, color = VaultTextLight)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = cycle == "Monthly", onClick = { cycle = "Monthly" })
                    Text("Monthly", color = VaultTextDark, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    RadioButton(selected = cycle == "Yearly", onClick = { cycle = "Yearly" })
                    Text("Yearly", color = VaultTextDark, fontSize = 14.sp)
                }

                errorState?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorState = "Please enter service name"
                        return@Button
                    }
                    if (amountVal == null || amountVal <= 0.0) {
                        errorState = "Please enter a valid billing amount"
                        return@Button
                    }
                    
                    val activeAccount = bankAccounts.firstOrNull()
                    val totalAvailable = activeAccount?.balance ?: 0.0
                    if (amountVal > totalAvailable) {
                        errorState = "Insufficient balance in your bank account!"
                        return@Button
                    }
                    
                    onConfirm(name, amountVal, cycle, category)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
