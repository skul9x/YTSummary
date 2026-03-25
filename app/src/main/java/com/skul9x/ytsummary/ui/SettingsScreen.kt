package com.skul9x.ytsummary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.ui.components.GlassCard
import com.skul9x.ytsummary.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager.getInstance(context) }
    val modelManager = remember { ModelManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var newKey by remember { mutableStateOf("") }
    var apiKeys by remember { mutableStateOf(apiKeyManager.getApiKeys()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var models by remember { mutableStateOf(modelManager.getModels()) }
    var newModelName by remember { mutableStateOf("") }
    var testResults by remember { mutableStateOf(mapOf<Int, Boolean?>()) }
    var testingIndex by remember { mutableIntStateOf(-1) }
    
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    "API Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // --- SECTION: API KEYS ---
                item {
                    Text(
                        "Add Gemini API Keys (Paste mixed text)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            TextField(
                                value = newKey,
                                onValueChange = { 
                                    newKey = it 
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                placeholder = { Text("Paste text here... App will auto-extract API keys.", color = TextSecondary.copy(alpha = 0.5f)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = false,
                                maxLines = 5
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clipData = clipboard.primaryClip
                                        if (clipData != null && clipData.itemCount > 0) {
                                            val text = clipData.getItemAt(0).text?.toString() ?: ""
                                            newKey += if (newKey.isNotEmpty()) "\n$text" else text
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                ) {
                                    Text("Paste")
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Button(
                                    onClick = {
                                        val regex = Regex("AIza[a-zA-Z0-9_\\\\-]+")
                                        val matches = regex.findAll(newKey).map { it.value }.toList()
                                        if (matches.isNotEmpty()) {
                                            var addedCount = 0
                                            matches.forEach { key ->
                                                if (apiKeyManager.addApiKey(key)) addedCount++
                                            }
                                            apiKeys = apiKeyManager.getApiKeys()
                                            newKey = ""
                                            errorMessage = "Saved $addedCount valid API keys!"
                                        } else {
                                            errorMessage = "No valid API keys found."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                                ) {
                                    Text("Save Keys")
                                }
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
                }

                item {
                    Text(
                        "Managed Keys (${apiKeys.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = YouTubeRed,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (apiKeys.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            Text("No API keys added yet.", color = TextSecondary)
                        }
                    }
                } else {
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

                // --- SECTION: MODEL PRIORITY ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Model Priority (${models.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = YouTubeRed,
                            fontWeight = FontWeight.Black
                        )
                        TextButton(
                            onClick = {
                                modelManager.resetToDefaults()
                                models = modelManager.getModels()
                                testResults = emptyMap()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reset", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = newModelName,
                                onValueChange = { newModelName = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Enter model name (e.g. models/gemini-2.0-flash)", fontSize = 12.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                if (modelManager.addModel(newModelName)) {
                                    models = modelManager.getModels()
                                    newModelName = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Model", tint = YouTubeRed)
                            }
                        }
                    }
                }

                itemsIndexed(models) { index, model ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1} $model",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(
                                    onClick = {
                                        if (modelManager.moveUp(index)) {
                                            models = modelManager.getModels()
                                            testResults = emptyMap()
                                        }
                                    },
                                    enabled = index > 0
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = if (index > 0) TextPrimary else Color.DarkGray)
                                }
                                
                                IconButton(
                                    onClick = {
                                        if (modelManager.moveDown(index)) {
                                            models = modelManager.getModels()
                                            testResults = emptyMap()
                                        }
                                    },
                                    enabled = index < models.size - 1
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = if (index < models.size - 1) TextPrimary else Color.DarkGray)
                                }

                                IconButton(onClick = {
                                    modelManager.removeModel(index)
                                    models = modelManager.getModels()
                                    testResults = emptyMap()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.3f))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            testingIndex = index
                                            val apiKey = apiKeyManager.getApiKeys().firstOrNull()
                                            if (apiKey == null) {
                                                testingIndex = -1
                                                // Ideally show a toast here
                                                return@launch
                                            }
                                            
                                            val success = testModel(model, apiKey)
                                            testResults = testResults + (index to success)
                                            testingIndex = -1
                                        }
                                    },
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    enabled = testingIndex == -1
                                ) {
                                    if (testingIndex == index) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = YouTubeRed)
                                    } else {
                                        Text("Test", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                val result = testResults[index]
                                if (result != null) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (result) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (result) Color.Green else YouTubeRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun testModel(model: String, apiKey: String): Boolean {
    val client = com.skul9x.ytsummary.di.NetworkModule.okHttpClient
    val requestBody = """{"contents": [{"parts":[{"text": "Hi"}]}], "generationConfig": {"maxOutputTokens": 1}}"""
    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/$model:generateContent")
        .header("x-goog-api-key", apiKey)
        .post(requestBody.toRequestBody("application/json".toMediaType()))
        .build()

    return try {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        }
    } catch (e: Exception) {
        false
    }
}
