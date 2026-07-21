package com.example.vaultflow.ui.screens

import androidx.camera.core.ExperimentalGetImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.compose.ui.tooling.preview.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vaultflow.data.model.Transaction
import com.example.vaultflow.data.model.TransactionType
import com.example.vaultflow.ui.components.TransactionItem
import com.example.vaultflow.ui.components.TransactionReceiptDialog
import com.example.vaultflow.ui.components.VaultBottomNavigation
import com.example.vaultflow.ui.theme.*
import com.example.vaultflow.ui.viewmodel.VaultViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.Executors

@Composable
fun DashboardScreen(
    userName: String = "User",
    viewModel: VaultViewModel = viewModel(),
    onNavigate: (String) -> Unit = {}
) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val aiNudge by viewModel.aiNudge.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val isProfileLoaded by viewModel.isProfileLoaded.collectAsState()
    
    val displayName = userProfile?.displayName ?: userName

    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showSendDialog by remember { mutableStateOf(false) }
    var showReceiveDialog by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    var currentTransferRecipient by remember { mutableStateOf("") }
    var currentTransferAmount by remember { mutableStateOf(0.0) }

    var selectedTransactionForReceipt by remember { mutableStateOf<Transaction?>(null) }

    var showSetupWizard by remember { mutableStateOf(false) }
    LaunchedEffect(bankAccounts, isProfileLoaded) {
        if (isProfileLoaded) {
            showSetupWizard = bankAccounts.isEmpty()
        }
    }

    val context = LocalContext.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanDialog = true
        }
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { title, amount, category, type ->
                viewModel.addTransaction(
                    Transaction(
                        title = title,
                        amount = amount,
                        category = category,
                        type = type
                    )
                )
                showAddTransactionDialog = false
            }
        )
    }

    if (showSetupWizard) {
        OnboardingSetupWizardOverlay(
            onDismiss = { showSetupWizard = false },
            onConfirm = { name, pin ->
                val updatedProfile = com.example.vaultflow.data.model.UserProfile(
                    uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    displayName = name,
                    email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: ""
                )
                viewModel.saveProfile(updatedProfile)
                
                // Create a default Bank Account with their set PIN instantly so they have a pin-authorized bank!
                viewModel.addBankAccount(
                    com.example.vaultflow.data.model.BankAccount(
                        bankName = "HDFC Bank",
                        accountHolder = name,
                        accountNumber = "**** 2839",
                        balance = 25000.0,
                        pin = pin
                    )
                )
                showSetupWizard = false
            },
            onNavigateToWallet = {
                onNavigate("wallet")
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { category, limit ->
                viewModel.setBudget(
                    com.example.vaultflow.data.model.Budget(
                        category = category,
                        limitAmount = limit
                    )
                )
                showAddBudgetDialog = false
            }
        )
    }

    if (showSendDialog) {
        SendDialog(
            onDismiss = { showSendDialog = false },
            onConfirm = { recipient, amount ->
                currentTransferRecipient = recipient
                currentTransferAmount = amount
                showSendDialog = false
                showPinDialog = true
            }
        )
    }

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Enter UPI Security PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("To authorize transfer of ₹${currentTransferAmount} to $currentTransferRecipient, please enter your 4-digit UPI PIN:")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                        label = { Text("Security PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    pinError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val activeAccount = bankAccounts.firstOrNull()
                        val correctPin = activeAccount?.pin ?: "1234"
                        
                        if (pinInput == correctPin) {
                            var isBalanceValid = true
                            if (activeAccount != null) {
                                val remainingBal = activeAccount.balance - currentTransferAmount
                                if (remainingBal < 0) {
                                    pinError = "Insufficient balance in account!"
                                    isBalanceValid = false
                                } else {
                                    viewModel.updateBankAccountBalance(activeAccount.id, remainingBal)
                                }
                            }
                            
                            if (isBalanceValid) {
                                viewModel.addTransaction(
                                    Transaction(
                                        title = "UPI to $currentTransferRecipient",
                                        amount = currentTransferAmount,
                                        category = "Transfer",
                                        type = TransactionType.EXPENSE
                                    )
                                )
                                showPinDialog = false
                            }
                        } else {
                            pinError = "Incorrect security PIN. Please try again!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReceiveDialog) {
        ReceiveDialog(
            onDismiss = { showReceiveDialog = false },
            onConfirm = { sender, amount ->
                viewModel.addTransaction(
                    Transaction(
                        title = "Received from $sender",
                        amount = amount,
                        category = "Transfer",
                        type = TransactionType.INCOME
                    )
                )
                showReceiveDialog = false
            }
        )
    }

    if (showScanDialog) {
        QRScannerDialog(
            onDismiss = { showScanDialog = false },
            onResult = { result ->
                viewModel.addTransaction(
                    Transaction(
                        title = "QR Payment",
                        amount = 100.0,
                        category = "QR Scan",
                        type = TransactionType.EXPENSE
                    )
                )
                showScanDialog = false
            }
        )
    }

    if (selectedTransactionForReceipt != null) {
        TransactionReceiptDialog(
            transaction = selectedTransactionForReceipt!!,
            onDismiss = { selectedTransactionForReceipt = null }
        )
    }

    Scaffold(
        bottomBar = { VaultBottomNavigation(currentRoute = "dashboard", onNavigate = onNavigate) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { showAddBudgetDialog = true },
                    containerColor = VaultSecondary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Add Budget")
                }
                FloatingActionButton(
                    onClick = { showAddTransactionDialog = true },
                    containerColor = VaultPrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
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
                Spacer(modifier = Modifier.height(20.dp))
                DashboardHeader(displayName)
                Spacer(modifier = Modifier.height(24.dp))
                NetWorthCard(totalBalance, savingsGoals)
                Spacer(modifier = Modifier.height(24.dp))
                BalanceCard(totalBalance)
                Spacer(modifier = Modifier.height(24.dp))
                QuickActions(
                    onSend = { showSendDialog = true },
                    onReceive = { showReceiveDialog = true },
                    onBudget = { showAddBudgetDialog = true },
                    onScan = { 
                        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            showScanDialog = true
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                FinancialHealthCard()
                Spacer(modifier = Modifier.height(24.dp))
                AIInsightCard(aiNudge)
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontWeight = FontWeight.Bold,
                        color = VaultTextDark
                    )
                    TextButton(onClick = { onNavigate("transactions") }) {
                        Text("View All", color = VaultPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No recent transactions",
                        color = VaultTextLight,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            } else {
                items(transactions.take(5)) { transaction ->
                    TransactionItem(transaction, onClick = { selectedTransactionForReceipt = transaction })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                AIInsightCard(aiNudge, onClick = { onNavigate("ai_coach") })
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun DashboardHeader(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, $userName 👋",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VaultTextDark
            )
            Text(
                text = "Good Morning!",
                fontSize = 14.sp,
                color = VaultTextLight
            )
        }
        IconButton(
            onClick = { },
            modifier = Modifier.background(Color.White, CircleShape)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = VaultTextDark)
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VaultPrimary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(VaultPrimary, VaultSecondary)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total Balance", color = Color.White.copy(alpha = 0.8f))
                    Icon(Icons.Default.Visibility, contentDescription = "Show", tint = Color.White)
                }
                Text(
                    text = currencyFormatter.format(balance),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = VaultIncome,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Dynamic balance from cloud",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActions(
    onSend: () -> Unit = {},
    onReceive: () -> Unit = {},
    onBudget: () -> Unit = {},
    onScan: () -> Unit = {}
) {
    Column {
        Text(
            text = "Quick Actions",
            fontWeight = FontWeight.Bold,
            color = VaultTextDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionItem(Icons.Default.ArrowUpward, "Send", onSend)
            QuickActionItem(Icons.Default.ArrowDownward, "Receive", onReceive)
            QuickActionItem(Icons.Default.AccountBalanceWallet, "Budget", onBudget)
            QuickActionItem(Icons.Default.QrCodeScanner, "Scan", onScan)
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = VaultPrimary)
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = VaultTextDark,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = limit,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) limit = it },
                    label = { Text("Limit Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limitVal = limit.toDoubleOrNull() ?: 0.0
                    if (category.isNotBlank() && limitVal > 0) {
                        onConfirm(category, limitVal)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AIInsightCard(nudge: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VaultPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Smart Nudge",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VaultPrimary
                )
                Text(
                    text = nudge,
                    fontSize = 12.sp,
                    color = VaultTextDark
                )
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, TransactionType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE })
                        Text("Expense")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME })
                        Text("Income")
                    }
                }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amountVal > 0) {
                        onConfirm(title, amountVal, category, type)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SmartScanDialog(viewModel: VaultViewModel, onDismiss: () -> Unit) {
    var receiptText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<Transaction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Receipt Scan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Paste receipt text below for Gemini to analyze:", fontSize = 12.sp, color = VaultTextLight)
                OutlinedTextField(
                    value = receiptText,
                    onValueChange = { receiptText = it },
                    placeholder = { Text("e.g. Starbucks Coffee $12.50") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                analysisResult?.let {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Gemini detected:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${it.title} - ₹${it.amount}", color = VaultIncome)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (analysisResult != null) {
                        viewModel.addTransaction(analysisResult!!)
                        onDismiss()
                    } else if (receiptText.isNotBlank()) {
                        isAnalyzing = true
                        // Simulation of Gemini parsing
                        analysisResult = Transaction(title = receiptText.split(" ")[0], amount = receiptText.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0)
                        isAnalyzing = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text(if (analysisResult != null) "Confirm" else "Analyze")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun NetWorthCard(balance: Double, goals: List<com.example.vaultflow.data.model.SavingsGoal>) {
    val totalSavings = goals.sumOf { it.currentAmount }
    val netWorth = balance + totalSavings
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    var isVisible by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Estimated Net Worth", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                IconButton(onClick = { isVisible = !isVisible }) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Balance",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Text(
                text = if (isVisible) currencyFormatter.format(netWorth) else "••••••••",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (isVisible) "Liquid: ${currencyFormatter.format(balance)}" else "Liquid: ••••••••",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
                Text(
                    text = if (isVisible) "Saved: ${currencyFormatter.format(totalSavings)}" else "Saved: ••••••••",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun FinancialHealthCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Financial Health Score", fontWeight = FontWeight.Bold, color = VaultTextDark)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    CircularProgressIndicator(
                        progress = { 0.85f },
                        modifier = Modifier.fillMaxSize(),
                        color = VaultIncome,
                        strokeWidth = 6.dp
                    )
                    Text("85", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text("Excellent!", fontWeight = FontWeight.Bold, color = VaultIncome)
                    Text("You saved 15% more than last month.", fontSize = 12.sp, color = VaultTextLight)
                }
            }
        }
    }
}

@Composable
fun SendDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var recipient by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var errorState by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("UPI Transfer - Send Money", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Transfer simulated funds securely to any UPI ID or recipient name.",
                    fontSize = 12.sp,
                    color = VaultTextLight
                )

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    placeholder = { Text("username@upi or name") },
                    label = { Text("Recipient Name/UPI ID") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = "UPI ID")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                    placeholder = { Text("0.00") },
                    label = { Text("Amount (₹)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocalAtm, contentDescription = "Amount")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorState?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    if (recipient.isBlank()) {
                        errorState = "Please enter a recipient UPI ID or Name"
                        return@Button
                    }
                    if (amountVal <= 0.0) {
                        errorState = "Please enter a valid transfer amount"
                        return@Button
                    }
                    onConfirm(recipient, amountVal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Verify & Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReceiveDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var senderName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receive Money", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = senderName, onValueChange = { senderName = it }, label = { Text("Sender Name") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(120.dp), tint = VaultPrimary)
                Text("Scan QR or enter manually above", color = VaultTextLight, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                if (senderName.isNotBlank() && amountVal > 0) {
                    onConfirm(senderName, amountVal)
                }
            }) {
                Text("Receive")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerDialog(onDismiss: () -> Unit, onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan QR Code", fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val executor = ContextCompat.getMainExecutor(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = CameraPreview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val scanner = BarcodeScanning.getClient()
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                            scanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    if (barcodes.isNotEmpty()) {
                                                        barcodes[0].rawValue?.let { value ->
                                                            onResult(value)
                                                        }
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        }
                                    }
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("Scanner", "Binding failed", e)
                            }
                        }, executor)
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun OnboardingSetupWizardOverlay(
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, mpin: String) -> Unit,
    onNavigateToWallet: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var mpin by remember { mutableStateOf("") }
    var confirmMpin by remember { mutableStateOf("") }
    var currentSlide by remember { mutableStateOf(1) }
    var errorState by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {}, // Force setup completion
        title = {
            Text(
                text = if (currentSlide == 1) "Initial App Setup" else "Bank Link Setup",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currentSlide == 1) {
                    Text("Welcome to VaultFlow! Let's set up your profile name and secure 4-digit login MPIN:")
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mpin,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) mpin = it },
                        label = { Text("Create 4-Digit MPIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmMpin,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) confirmMpin = it },
                        label = { Text("Confirm 4-Digit MPIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = VaultIncome,
                        modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally)
                    )
                    Text(
                        "Successfully Created!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = VaultIncome,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        "To unlock the full potential of your AI Copilot, please link your bank account now.",
                        textAlign = TextAlign.Center
                    )
                }

                errorState?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentSlide == 1) {
                        if (fullName.isBlank()) {
                            errorState = "Please enter your full name"
                            return@Button
                        }
                        if (mpin.length < 4) {
                            errorState = "Please set a secure 4-digit MPIN"
                            return@Button
                        }
                        if (mpin != confirmMpin) {
                            errorState = "Pins do not match. Please verify!"
                            return@Button
                        }
                        
                        onConfirm(fullName, mpin)
                        currentSlide = 2
                        errorState = null
                    } else {
                        onDismiss()
                        onNavigateToWallet()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text(if (currentSlide == 1) "Save & Continue" else "Link Bank Account")
            }
        }
    )
}

@Preview
@Composable
fun DashboardPreview() {
    VaultFlowTheme {
        DashboardScreen("Prathiksha")
    }
}

