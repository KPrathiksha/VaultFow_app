package com.example.vaultflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vaultflow.data.model.Transaction
import com.example.vaultflow.data.model.TransactionType
import com.example.vaultflow.ui.theme.VaultExpense
import com.example.vaultflow.ui.theme.VaultIncome
import com.example.vaultflow.ui.theme.VaultTextDark
import com.example.vaultflow.ui.theme.VaultTextLight
import java.text.NumberFormat
import java.util.*

@Composable
fun VaultBottomNavigation(
    currentRoute: String = "home",
    onNavigate: (String) -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = currentRoute == "dashboard",
            onClick = { onNavigate("dashboard") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
            label = { Text("Insights") },
            selected = currentRoute == "insights",
            onClick = { onNavigate("insights") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Savings, contentDescription = null) },
            label = { Text("Savings") },
            selected = currentRoute == "savings",
            onClick = { onNavigate("savings") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
            label = { Text("AI Coach") },
            selected = currentRoute == "ai_coach",
            onClick = { onNavigate("ai_coach") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
            label = { Text("Wallet") },
            selected = currentRoute == "wallet",
            onClick = { onNavigate("wallet") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") }
        )
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color) = when (transaction.type) {
                TransactionType.INCOME -> Icons.Default.ArrowDownward to VaultIncome
                TransactionType.EXPENSE -> Icons.Default.ArrowUpward to VaultExpense
            }
            
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title, fontWeight = FontWeight.SemiBold, color = VaultTextDark)
                Text(text = transaction.category, fontSize = 12.sp, color = VaultTextLight)
            }
            
            val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            Text(
                text = "$prefix${currencyFormatter.format(transaction.amount)}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME) VaultIncome else VaultExpense
            )
        }
    }
}

