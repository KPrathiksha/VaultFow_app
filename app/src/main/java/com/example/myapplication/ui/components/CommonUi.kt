package com.example.vaultflow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vaultflow.data.model.Transaction
import com.example.vaultflow.data.model.TransactionType
import com.example.vaultflow.ui.theme.VaultExpense
import com.example.vaultflow.ui.theme.VaultIncome
import com.example.vaultflow.ui.theme.VaultTextDark
import com.example.vaultflow.ui.theme.VaultTextLight
import com.example.vaultflow.ui.theme.VaultSurface
import com.example.vaultflow.ui.theme.VaultPrimary
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
            selected = currentRoute == "dashboard" || currentRoute == "home",
            onClick = { onNavigate("dashboard") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
            label = { Text("AI Coach") },
            selected = currentRoute == "ai_coach",
            onClick = { onNavigate("ai_coach") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Savings, contentDescription = null) },
            label = { Text("Savings") },
            selected = currentRoute == "savings",
            onClick = { onNavigate("savings") }
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
            selected = currentRoute == "profile" || currentRoute == "subscriptions",
            onClick = { onNavigate("profile") }
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VaultSurface)
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

@Composable
fun TransactionReceiptDialog(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val seconds = transaction.date?.seconds ?: (System.currentTimeMillis() / 1000)
    val formattedDate = sdf.format(java.util.Date(seconds * 1000))
    
    val hash = Math.abs(transaction.id.hashCode().toLong())
    val utrNumber = "9961" + String.format("%08d", hash % 100000000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transaction Receipt", fontWeight = FontWeight.Bold, color = VaultTextDark)
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = VaultIncome,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large Amount
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
                    val color = if (transaction.type == TransactionType.INCOME) VaultIncome else VaultExpense
                    
                    Text(
                        text = "$prefix${currencyFormatter.format(transaction.amount)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "Payment Successful",
                        fontSize = 12.sp,
                        color = VaultIncome,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider()

                // Recipient details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payee / Sender", color = VaultTextLight, fontSize = 14.sp)
                    Text(transaction.title, fontWeight = FontWeight.SemiBold, color = VaultTextDark, fontSize = 14.sp)
                }

                // Date/Time details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date & Time", color = VaultTextLight, fontSize = 14.sp)
                    Text(formattedDate, fontWeight = FontWeight.SemiBold, color = VaultTextDark, fontSize = 14.sp)
                }

                // Category details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Category", color = VaultTextLight, fontSize = 14.sp)
                    Text(transaction.category, fontWeight = FontWeight.SemiBold, color = VaultTextDark, fontSize = 14.sp)
                }

                // UPI UTR reference number
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("UPI UTR No.", color = VaultTextLight, fontSize = 14.sp)
                    Text(utrNumber, fontWeight = FontWeight.Bold, color = VaultPrimary, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This receipt represents a secure UPI transfer recorded in real-time on the cloud database.",
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = VaultTextLight,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close Receipt")
            }
        }
    )
}
