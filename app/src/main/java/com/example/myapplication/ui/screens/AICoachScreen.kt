package com.example.vaultflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vaultflow.ui.components.VaultBottomNavigation
import com.example.vaultflow.ui.theme.*
import com.example.vaultflow.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoachScreen(
    viewModel: VaultViewModel = viewModel(),
    onNavigate: (String) -> Unit = {}
) {
    var userMessage by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val pendingAction by viewModel.pendingAiAction.collectAsState()
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    if (pendingAction != null) {
        val action = pendingAction!!
        AlertDialog(
            onDismissRequest = { viewModel.clearPendingAiAction() },
            title = { Text("AI Copilot Authorization", fontWeight = FontWeight.Bold, color = VaultTextDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your AI Coach has drafted a database creation request for you. Please authorize to write it securely to your database:",
                        fontSize = 13.sp,
                        color = VaultTextLight
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VaultSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            when (action) {
                                is com.example.vaultflow.ui.viewmodel.PendingAiAction.CreateSavingsGoal -> {
                                    Text("🎯 Action: Create Savings Goal", fontWeight = FontWeight.Bold, color = VaultPrimary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Goal Title: ${action.title}", fontSize = 13.sp, color = VaultTextDark)
                                    Text("Target Amount: ₹${action.amount}", fontSize = 13.sp, color = VaultTextDark)
                                }
                                is com.example.vaultflow.ui.viewmodel.PendingAiAction.CreateTransaction -> {
                                    Text("💸 Action: Add Transaction", fontWeight = FontWeight.Bold, color = VaultPrimary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Title: ${action.title}", fontSize = 13.sp, color = VaultTextDark)
                                    Text("Amount: ₹${action.amount}", fontSize = 13.sp, color = VaultTextDark)
                                    Text("Type: ${action.type.name}", fontSize = 13.sp, color = VaultTextDark)
                                    Text("Category: ${action.category}", fontSize = 13.sp, color = VaultTextDark)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingAiAction() },
                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Authorize & Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearPendingAiAction() }) {
                    Text("Cancel", color = VaultTextLight)
                }
            }
        )
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("VaultFlow AI Coach", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VaultBackgroundLight)
            )
        },
        bottomBar = { VaultBottomNavigation(currentRoute = "ai_coach", onNavigate = onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VaultBackgroundLight)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                items(chatMessages) { message ->
                    ChatBubble(message)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (isTyping) {
                    item {
                        Text(
                            "AI Coach is analyzing...",
                            fontSize = 12.sp,
                            color = VaultTextLight,
                            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Input Area
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                color = VaultSurface
            ) {
                Row(
                    modifier = Modifier
                        .imePadding()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userMessage,
                        onValueChange = { userMessage = it },
                        placeholder = { Text("Type your question...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (userMessage.isNotBlank()) {
                                viewModel.sendMessage(userMessage)
                                userMessage = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = VaultPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isUser) VaultPrimary else VaultSurface,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 16.dp
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else VaultTextDark,
                fontSize = 14.sp
            )
        }
    }
}
