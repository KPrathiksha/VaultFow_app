package com.example.vaultflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vaultflow.data.model.BankAccount
import com.example.vaultflow.data.model.LinkedBank
import com.example.vaultflow.data.model.Transaction
import com.example.vaultflow.data.model.TransactionType
import com.example.vaultflow.ui.components.VaultBottomNavigation
import com.example.vaultflow.ui.theme.*
import com.example.vaultflow.ui.viewmodel.VaultViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: VaultViewModel = viewModel(),
    onLinkBank: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val linkedBanks by viewModel.linkedBanks.collectAsState()

    var showLoadMoneyDialog by remember { mutableStateOf(false) }
    var selectedAccountForLoad by remember { mutableStateOf<BankAccount?>(null) }
    var showPlaidSimDialog by remember { mutableStateOf(false) }

    val user = FirebaseAuth.getInstance().currentUser
    val defaultHolder = user?.displayName ?: "User Member"

    if (showLoadMoneyDialog && selectedAccountForLoad != null) {
        LoadMoneyDialog(
            bankName = selectedAccountForLoad!!.bankName,
            onDismiss = { showLoadMoneyDialog = false },
            onConfirm = { loadAmount ->
                val activeAcc = selectedAccountForLoad!!
                // Add Income transaction to history (which atomically updates bank balance in Firestore!)
                viewModel.addTransaction(
                    Transaction(
                        title = "Loaded to ${activeAcc.bankName}",
                        amount = loadAmount,
                        category = "Deposit",
                        type = TransactionType.INCOME
                    )
                )
                showLoadMoneyDialog = false
            }
        )
    }

    if (showPlaidSimDialog) {
        PlaidSimDialog(
            onDismiss = { showPlaidSimDialog = false },
            onConfirm = { bankName, accountName, balance, accNum ->
                viewModel.addLinkedBank(
                    LinkedBank(
                        bankName = bankName,
                        accountName = accountName,
                        balance = balance,
                        accountNumber = accNum
                    )
                )
                showPlaidSimDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Wallet", fontWeight = FontWeight.Bold, color = VaultTextDark) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VaultBackgroundLight)
            )
        },
        bottomBar = { VaultBottomNavigation(currentRoute = "wallet", onNavigate = onNavigate) }
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
                
                // Plaid Real-Time Banking Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (linkedBanks.isEmpty()) {
                            // No banks linked yet: Show Link Button
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(48.dp), tint = VaultPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Connect Your Bank", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = VaultTextDark)
                            Text(
                                "Sync your transactions automatically in real-time using Plaid",
                                textAlign = TextAlign.Center,
                                color = VaultTextLight,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Button(
                                onClick = { showPlaidSimDialog = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                            ) {
                                Text("Link Account with Plaid", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Real Connected Banks List
                            Text(
                                text = "Connected Bank Accounts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = VaultTextDark,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            linkedBanks.forEach { bank ->
                                val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .border(1.dp, VaultPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .background(VaultBackgroundLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = bank.bankName, fontWeight = FontWeight.Bold, color = VaultTextDark, fontSize = 16.sp)
                                        Text(text = "${bank.accountName} (${bank.accountNumber})", color = VaultTextLight, fontSize = 12.sp)
                                    }
                                    Text(
                                        text = currencyFormatter.format(bank.balance),
                                        fontWeight = FontWeight.Bold,
                                        color = VaultIncome,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showPlaidSimDialog = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Bank")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Link Another Bank", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Saved Cards Section Header (Simplified - Natively no manual multi-add to protect onboarding flows!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Bank Accounts", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VaultTextDark)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Real-Time saved bank accounts from Firestore
            if (bankAccounts.isEmpty()) {
                item {
                    // Fallback to beautiful mock card if they have not added a real account yet
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SAVINGS ACCOUNT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("₹0.00", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(defaultHolder.uppercase(), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text("**** 9820", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                items(bankAccounts) { acc ->
                    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                    
                    // Sleek gradient color matching the bank
                    val cardGradient = when {
                        acc.bankName.contains("HDFC", true) -> Brush.linearGradient(colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
                        acc.bankName.contains("SBI", true) -> Brush.linearGradient(colors = listOf(Color(0xFF0369A1), Color(0xFF0EA5E9)))
                        acc.bankName.contains("Chase", true) -> Brush.linearGradient(colors = listOf(Color(0xFF3730A3), Color(0xFF6366F1)))
                        else -> Brush.linearGradient(colors = listOf(Color(0xFF1F2937), Color(0xFF4B5563)))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(cardGradient)
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(acc.bankName.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(currencyFormatter.format(acc.balance), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(acc.accountHolder.uppercase(), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Text(acc.accountNumber, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Beautiful, functional "Load Money" button inside the Bank Account Card!
                                Button(
                                    onClick = {
                                        selectedAccountForLoad = acc
                                        showLoadMoneyDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.25f),
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Load", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Load Money (Deposit)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadMoneyDialog(
    bankName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var rawAmount by remember { mutableStateOf("5000") }
    var errorState by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load Money - $bankName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Deposit simulated funds directly into your active account:",
                    fontSize = 12.sp,
                    color = VaultTextLight
                )
                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) rawAmount = it },
                    label = { Text("Amount to Load (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                errorState?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = rawAmount.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0) {
                        errorState = "Please enter a valid deposit amount"
                        return@Button
                    }
                    onConfirm(amountVal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Load Money")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PlaidSimDialog(
    onDismiss: () -> Unit,
    onConfirm: (bankName: String, accountName: String, balance: Double, accNum: String) -> Unit
) {
    val banks = listOf(
        Pair("HDFC Bank", "ins_hdfc"),
        Pair("State Bank of India", "ins_sbi"),
        Pair("ICICI Bank", "ins_icici"),
        Pair("Chase Bank", "ins_chase"),
        Pair("Bank of America", "ins_boa")
    )
    var selectedBank by remember { mutableStateOf(banks[0].first) }
    var rawBalance by remember { mutableStateOf("45000") }
    var errorState by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plaid Sandbox - Link Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select your banking institution:",
                    fontSize = 12.sp,
                    color = VaultTextLight
                )

                // Simple Bank Institution list selectors
                banks.forEach { (bankName, _) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBank = bankName }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedBank == bankName),
                            onClick = { selectedBank = bankName }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = bankName, color = VaultTextDark, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawBalance,
                    onValueChange = { if (it.all { char -> char.isDigit() }) rawBalance = it },
                    label = { Text("Simulated Account Balance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                errorState?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = rawBalance.toDoubleOrNull()
                    if (bal == null || bal < 0) {
                        errorState = "Please enter a valid balance"
                        return@Button
                    }
                    val randomLastFour = String.format("%04d", Random().nextInt(10000))
                    onConfirm(selectedBank, "Savings Account", bal, "**** $randomLastFour")
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Link Bank")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
