package com.example.vaultflow.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vaultflow.data.model.BankAccount
import com.example.vaultflow.data.model.SavingsGoal
import com.example.vaultflow.data.model.Transaction
import com.example.vaultflow.data.model.TransactionType
import com.example.vaultflow.ui.components.VaultBottomNavigation
import com.example.vaultflow.ui.theme.*
import com.example.vaultflow.ui.viewmodel.VaultViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    viewModel: VaultViewModel = viewModel(),
    onNavigate: (String) -> Unit = {}
) {
    val goals by viewModel.savingsGoals.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showContributeDialog by remember { mutableStateOf(false) }
    var selectedGoalForContribution by remember { mutableStateOf<SavingsGoal?>(null) }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            bankAccounts = bankAccounts,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, target, current ->
                viewModel.addSavingsGoal(
                    SavingsGoal(
                        title = title,
                        targetAmount = target,
                        currentAmount = current
                    )
                )
                if (current > 0.0) {
                    viewModel.addTransaction(
                        com.example.vaultflow.data.model.Transaction(
                            title = "Initial Saved: $title",
                            amount = current,
                            category = "Savings",
                            type = com.example.vaultflow.data.model.TransactionType.EXPENSE
                        )
                    )
                }
                showAddGoalDialog = false
            }
        )
    }

    if (showContributeDialog && selectedGoalForContribution != null) {
        AddMoneyToGoalDialog(
            goalTitle = selectedGoalForContribution!!.title,
            bankAccounts = bankAccounts,
            onDismiss = { showContributeDialog = false },
            onConfirm = { amountToSave ->
                val activeGoal = selectedGoalForContribution!!
                val activeAccount = bankAccounts.firstOrNull()

                // 1. Deduct balance from Bank Account in Firestore (if connected)
                if (activeAccount != null) {
                    val remainingBal = activeAccount.balance - amountToSave
                    viewModel.updateBankAccountBalance(activeAccount.id, remainingBal)
                }

                // 2. Increase Savings Goal progress in Firestore
                val newProgress = activeGoal.currentAmount + amountToSave
                viewModel.updateSavingsGoalProgress(activeGoal.id, newProgress)

                // 3. Register transaction Expense to timeline & history.json
                viewModel.addTransaction(
                    Transaction(
                        title = "Saved for ${activeGoal.title}",
                        amount = amountToSave,
                        category = "Savings",
                        type = TransactionType.EXPENSE
                    )
                )

                showContributeDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Savings Goals", fontWeight = FontWeight.Bold, color = VaultTextDark) },
                actions = {
                    TextButton(onClick = { showAddGoalDialog = true }) {
                        Text("+ Add Goal", color = VaultPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VaultBackgroundLight)
            )
        },
        bottomBar = { VaultBottomNavigation(currentRoute = "savings", onNavigate = onNavigate) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VaultBackgroundLight)
                .padding(horizontal = 20.dp)
        ) {
            if (goals.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No savings goals yet. Start saving today!", color = VaultTextLight)
                    }
                }
            } else {
                items(goals) { goal ->
                    SavingGoalItem(
                        goal = goal,
                        onContribute = {
                            selectedGoalForContribution = goal
                            showContributeDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun SavingGoalItem(
    goal: SavingsGoal,
    onContribute: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VaultSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = VaultPrimary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = VaultPrimary)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = goal.title, fontWeight = FontWeight.Bold, color = VaultTextDark)
                        Text(text = "${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VaultPrimary)
                    }
                    Text(
                        text = "${currencyFormatter.format(goal.currentAmount)} / ${currencyFormatter.format(goal.targetAmount)}",
                        fontSize = 12.sp, 
                        color = VaultTextLight, 
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = VaultPrimary,
                        trackColor = VaultBackgroundLight,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Money button to contribute directly to goals!
            Button(
                onClick = onContribute,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VaultPrimary.copy(alpha = 0.1f),
                    contentColor = VaultPrimary
                ),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Contribute", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Money (Contribute)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddMoneyToGoalDialog(
    goalTitle: String,
    bankAccounts: List<BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var errorState by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contribute to $goalTitle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Transfer simulated funds from your active bank account directly into your savings goal.",
                    fontSize = 12.sp,
                    color = VaultTextLight
                )

                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) rawAmount = it },
                    label = { Text("Contribution Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                    label = { Text("Enter 4-Digit Security PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                        errorState = "Please enter a valid contribution amount"
                        return@Button
                    }

                    val activeAccount = bankAccounts.firstOrNull()
                    val correctPin = activeAccount?.pin ?: "1234"

                    if (activeAccount != null && activeAccount.balance < amountVal) {
                        errorState = "Insufficient balance in linked bank account!"
                        return@Button
                    }

                    if (pinInput == correctPin) {
                        onConfirm(amountVal)
                    } else {
                        errorState = "Incorrect security PIN. Please try again!"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
            ) {
                Text("Confirm Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddSavingsGoalDialog(
    bankAccounts: List<com.example.vaultflow.data.model.BankAccount>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var errorState by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) target = it },
                    label = { Text("Target Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = current,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) current = it },
                    label = { Text("Starting Progress") },
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
                    val targetVal = target.toDoubleOrNull() ?: 0.0
                    val currentVal = current.toDoubleOrNull() ?: 0.0
                    
                    if (title.isBlank()) {
                        errorState = "Please enter goal title"
                        return@Button
                    }
                    if (targetVal <= 0.0) {
                        errorState = "Please enter a valid target amount"
                        return@Button
                    }
                    
                    val activeAccount = bankAccounts.firstOrNull()
                    val totalAvailable = activeAccount?.balance ?: 0.0
                    if (currentVal > totalAvailable) {
                        errorState = "Starting progress exceeds your bank balance!"
                        return@Button
                    }
                    
                    onConfirm(title, targetVal, currentVal)
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

@Preview
@Composable
fun SavingsPreview() {
    VaultFlowTheme {
        SavingsScreen()
    }
}
