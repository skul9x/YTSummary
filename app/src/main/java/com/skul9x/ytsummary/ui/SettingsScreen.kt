package com.skul9x.ytsummary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.ui.components.GlassCard
import com.skul9x.ytsummary.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager.getInstance(context) }
    
    var newKey by remember { mutableStateOf("") }
    var apiKeys by remember { mutableStateOf(apiKeyManager.getApiKeys()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF1E1E2E), DarkBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    "API Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Add Key Input Section
            Text(
                "Add Gemini API Key",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newKey,
                        onValueChange = { 
                            newKey = it 
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("AIzaSy...", color = TextSecondary.copy(alpha = 0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (apiKeyManager.addApiKey(newKey)) {
                                apiKeys = apiKeyManager.getApiKeys()
                                newKey = ""
                                errorMessage = null
                            } else {
                                errorMessage = "Invalid or duplicate key"
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(YouTubeRed)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
            }
            
            errorMessage?.let {
                Text(
                    text = it,
                    color = YouTubeRed,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Managed Keys (${apiKeys.size})",
                style = MaterialTheme.typography.labelMedium,
                color = YouTubeRed,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Key List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(apiKeys) { index, key ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = apiKeyManager.maskApiKey(key),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Gemini AI Key #${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = { 
                                apiKeyManager.removeApiKey(index)
                                apiKeys = apiKeyManager.getApiKeys()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete, 
                                    contentDescription = "Delete", 
                                    tint = Color.Gray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
            
            if (apiKeys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No API keys added yet.", color = TextSecondary)
                }
            }
        }
    }
}
