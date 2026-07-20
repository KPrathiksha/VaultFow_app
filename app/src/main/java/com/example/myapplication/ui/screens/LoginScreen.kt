package com.example.vaultflow.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vaultflow.AuthResult
import com.example.vaultflow.GoogleAuthClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val authClient = remember { GoogleAuthClient(context) }
    val coroutineScope = rememberCoroutineScope()

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            val authResult = authClient.handleSignInResult(result.data)
            when (authResult) {
                is AuthResult.Success -> {
                    isLoading = false
                    onLoginSuccess(authResult.userName)
                }
                is AuthResult.Error -> {
                    isLoading = false
                    errorMessage = authResult.message
                }
            }
        }
    }

    // Gradient matching Flutter's 0xFF6A1B9A to 0xFF9C4DCC
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6A1B9A), // Primary Purple
            Color(0xFF9C4DCC)  // Light Purple
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Logo and Welcome (exactly styled like the Flutter container)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(width = 1.5.dp, color = Color.White.copy(alpha = 0.25f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Wallet Brand",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = if (isSignUpMode) "Create Account" else "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Sign in to manage your money with AI",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // White Card filling the rest of the screen (exactly matching Flutter rounded top card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Full Name Input (SIGNUP MODE ONLY)
                    if (isSignUpMode) {
                        Text(
                            text = "Full Name",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        TextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            placeholder = { Text("Enter your full name", color = Color(0xFF666666).copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = "Name", tint = Color(0xFF666666))
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6A1B9A).copy(alpha = 0.03f),
                                unfocusedContainerColor = Color(0xFF6A1B9A).copy(alpha = 0.03f),
                                focusedIndicatorColor = Color(0xFF6A1B9A),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color(0xFF1A1A1A),
                                unfocusedTextColor = Color(0xFF1A1A1A)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = "Email Address",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    // TextFormField matching Flutter's custom padding, colors & rounded borders
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Enter your email", color = Color(0xFF666666).copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Email, contentDescription = "Email", tint = Color(0xFF666666))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF6A1B9A).copy(alpha = 0.03f),
                            unfocusedContainerColor = Color(0xFF6A1B9A).copy(alpha = 0.03f),
                            focusedIndicatorColor = Color(0xFF6A1B9A),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFF1A1A1A),
                            unfocusedTextColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "Forgot?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6A1B9A),
                            modifier = Modifier.clickable { }
                        )
                    }

                    // TextFormField matching Flutter password layout & rounded borders
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Enter your password", color = Color(0xFF666666).copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Lock, contentDescription = "Password", tint = Color(0xFF666666))
                        },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password", tint = Color(0xFF666666))
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF6A1B9A).copy(alpha = 0.03f),
                            unfocusedContainerColor = Color(0xFF6A1B9A).copy(alpha = 0.03f),
                            focusedIndicatorColor = Color(0xFF6A1B9A),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFF1A1A1A),
                            unfocusedTextColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Elevated Button matching Flutter padding & color
                    Button(
                        onClick = {
                            if (email.isBlank() || password.length < 6) {
                                errorMessage = "Please enter valid email and 6+ char password"
                                return@Button
                            }
                            if (isSignUpMode && displayName.isBlank()) {
                                errorMessage = "Please enter your name"
                                return@Button
                            }
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                val result = if (isSignUpMode) {
                                    authClient.signUpWithEmail(email, password, displayName)
                                } else {
                                    authClient.signInWithEmail(email, password)
                                }
                                
                                when (result) {
                                    is AuthResult.Success -> {
                                        isLoading = false
                                        onLoginSuccess(result.userName)
                                    }
                                    is AuthResult.Error -> {
                                        isLoading = false
                                        errorMessage = result.message
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A1B9A),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (isSignUpMode) "Sign Up" else "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mode Toggle Action with requested wording
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                            color = Color(0xFF666666),
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isSignUpMode) "Login" else "User not found. Signup here",
                            color = Color(0xFF6A1B9A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                isSignUpMode = !isSignUpMode
                                errorMessage = null
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Minimal Google Sign-in integrated elegantly into the white card
                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            googleSignInLauncher.launch(authClient.getSignInIntent())
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Google",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}
