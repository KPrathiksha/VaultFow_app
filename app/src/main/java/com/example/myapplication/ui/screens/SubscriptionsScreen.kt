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
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, cycle ->
                viewModel.addSubscription(Subscription(name = name, amount = amount, billingCycle = cycle))
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Subscriptions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VaultBackgroundLight)
            )
        },
        bottomBar = { VaultBottomNavigation(currentRoute = "subscriptions", onNavigate = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = VaultPrimary, contentColor = Color.White) {
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
                Text("Your Active Subscriptions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VaultTextDark)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (subscriptions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No subscriptions tracked yet.", color = VaultTextLight)
                    }
                }
            } else {
                items(subscriptions) { SubscriptionItem(it) }
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Monthly recurring expense", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(currencyFormatter.format(totalMonthly), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SubscriptionItem(sub: Subscription) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = VaultPrimary.copy(alpha = 0.1f)) {
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

@Composable
fun AddSubscriptionDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf("Monthly") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subscription") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                // Simple cycle selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = cycle == "Monthly", onClick = { cycle = "Monthly" })
                    Text("Monthly")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = cycle == "Yearly", onClick = { cycle = "Yearly" })
                    Text("Yearly")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, amount.toDoubleOrNull() ?: 0.0, cycle) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
