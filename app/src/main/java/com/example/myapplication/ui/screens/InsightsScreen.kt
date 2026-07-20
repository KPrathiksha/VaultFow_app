package com.example.vaultflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vaultflow.data.model.TransactionType
import com.example.vaultflow.ui.components.VaultBottomNavigation
import com.example.vaultflow.ui.theme.*
import com.example.vaultflow.ui.viewmodel.VaultViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: VaultViewModel = viewModel(),
    onNavigate: (String) -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Insights", fontWeight = FontWeight.Bold, color = VaultTextDark) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VaultBackgroundLight)
            )
        },
        bottomBar = { VaultBottomNavigation(currentRoute = "insights", onNavigate = onNavigate) }
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
                SummaryCard(totalIncome, totalExpense)
                Spacer(modifier = Modifier.height(24.dp))
                Text("AI Smart Analysis", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VaultTextDark)
                Spacer(modifier = Modifier.height(12.dp))
                AIAnalysisItem(
                    "Spending Pattern",
                    "You've spent ₹${String.format("%.2f", totalExpense)} this month. Most of it went to General categories.",
                    Icons.Default.TrendingUp
                )
                Spacer(modifier = Modifier.height(16.dp))
                AIAnalysisItem(
                    "Saving Potential",
                    "Based on your income, you could save up to 20% more by optimizing your food expenses.",
                    Icons.Default.AutoAwesome
                )
            }
        }
    }
}

@Composable
fun SummaryCard(income: Double, expense: Double) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = VaultSurface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Monthly Overview", color = VaultTextLight, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Income", color = VaultIncome, fontWeight = FontWeight.Bold)
                    Text(currencyFormatter.format(income), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VaultTextDark)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expenses", color = VaultExpense, fontWeight = FontWeight.Bold)
                    Text(currencyFormatter.format(expense), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VaultTextDark)
                }
            }
        }
    }
}

@Composable
fun AIAnalysisItem(title: String, description: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VaultSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = VaultPrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = VaultPrimary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = VaultTextDark, fontSize = 16.sp)
                Text(text = description, color = VaultTextLight, fontSize = 13.sp)
            }
        }
    }
}
