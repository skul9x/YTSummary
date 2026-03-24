package com.skul9x.ytsummary.ui

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skul9x.ytsummary.manager.TtsManager
import com.skul9x.ytsummary.model.AiResult
import com.skul9x.ytsummary.repository.SummarizationRepository
import com.skul9x.ytsummary.ui.components.GlassCard
import com.skul9x.ytsummary.ui.components.NeonGlassCard
import com.skul9x.ytsummary.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TtsManager
    private lateinit var repository: SummarizationRepository
    private val incomingUrl = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)
        
        // Task 4.1: Initialize TTS and Set Volume to 80% on startup
        ttsManager = TtsManager(this) {
            ttsManager.setVolume(80) 
        }
        repository = SummarizationRepository.getInstance(this)

        setContent {
            YTSummaryTheme {
                var currentScreen by remember { mutableStateOf("main") }
                var summaryResult by remember { mutableStateOf<AiResult?>(null) }
                var loadingMessage by remember { mutableStateOf("Đang chuẩn bị...") }
                var videoTitle by remember { mutableStateOf("Summarizing...") }
                var thumbnailUrl by remember { mutableStateOf("") }
                var isTtsPlaying by remember { mutableStateOf(false) }
                
                val scope = rememberCoroutineScope()


                val performSummarization: (String) -> Unit = { url ->
                    val videoId = extractVideoId(url)
                    if (videoId != null) {
                        videoTitle = "Fetching info..."
                        currentScreen = "loading"
                        scope.launch {
                            // Step 1: Chạy song song cả 2 (Fix C2)
                            val metadataJob = async {
                                repository.getVideoMetadata(videoId).firstOrNull()
                            }
                            
                            // Cập nhật UI ngay khi Metadata load xong
                            launch {
                                val metadata = metadataJob.await()
                                if (metadata != null) {
                                    videoTitle = metadata.title
                                    thumbnailUrl = metadata.thumbnailUrl
                                }
                            }
                            
                            var lastReadIndex = 0
                            
                            // Chờ Summary xong mới redirect
                            repository.getSummary(videoId).collect { result ->
                                if (result is AiResult.Loading) {
                                    loadingMessage = result.message
                                } else if (result is AiResult.Success) {
                                    summaryResult = result
                                    currentScreen = "summary"
                                    
                                    // Item 4: TTS Sentence detection logic for speakChunk
                                    val newTextSoFar = result.text
                                    val newPart = newTextSoFar.substring(lastReadIndex)
                                    val lastPunct = newPart.lastIndexOfAny(charArrayOf('.', '!', '?', ':', '\n'))
                                    
                                    if (lastPunct != -1) {
                                        val toRead = newPart.substring(0, lastPunct + 1)
                                        ttsManager.speakChunk(toRead)
                                        lastReadIndex += lastPunct + 1
                                    }
                                } else {
                                    summaryResult = result
                                    currentScreen = "summary"
                                }
                            }
                            
                            // Final cleanup: read remaining and save to history once
                            val finalResult = summaryResult
                            if (finalResult is AiResult.Success) {
                                val remaining = finalResult.text.substring(lastReadIndex)
                                if (remaining.isNotBlank()) {
                                    ttsManager.speakChunk(remaining)
                                }
                                
                                // Save to history only once and NOT if it was from cache
                                if (finalResult.model != "cache") {
                                    val metadata = metadataJob.await()
                                    repository.saveToHistory(
                                        videoId = videoId,
                                        title = metadata?.title ?: videoId,
                                        thumbnailUrl = metadata?.thumbnailUrl ?: "",
                                        summaryText = finalResult.text
                                    )
                                }
                            }
                            isTtsPlaying = true
                        }
                    }
                }

                val sharedUrl by incomingUrl.collectAsState()
                LaunchedEffect(sharedUrl) {
                    sharedUrl?.let { url ->
                        incomingUrl.value = null // reset after trigger
                        // currentScreen = "main" // optionally reset first, but performSummarization handles it
                        performSummarization(url)
                    }
                }

                when (currentScreen) {
                    "main" -> MainScreen(
                        onSettingsClick = { currentScreen = "settings" },
                        onSummaryRequest = performSummarization,
                        onHistoryClick = { currentScreen = "history" }
                    )
                    "history" -> HistoryScreen(
                        repository = repository,
                        onBack = { currentScreen = "main" },
                        onItemClick = { item ->
                            videoTitle = item.title
                            thumbnailUrl = item.thumbnailUrl
                            summaryResult = AiResult.Success(item.summaryText, "Local / History")
                            currentScreen = "summary"
                        }
                    )
                    "settings" -> SettingsScreen(onBack = { currentScreen = "main" })
                    "loading" -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = YouTubeRed)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(loadingMessage, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    "summary" -> {
                        summaryResult?.let { result ->
                            when (result) {
                                is AiResult.Success -> SummaryScreen(
                                    videoTitle = videoTitle,
                                    thumbnailUrl = thumbnailUrl,
                                    summaryText = result.text,
                                    isPlaying = isTtsPlaying,
                                    onBack = { 
                                        ttsManager.stop()
                                        isTtsPlaying = false
                                        currentScreen = "main" 
                                    },
                                    onTTSClick = {
                                        if (isTtsPlaying) {
                                            ttsManager.stop()
                                            isTtsPlaying = false
                                        } else {
                                            ttsManager.speak(result.text)
                                            isTtsPlaying = true
                                        }
                                    }
                                )
                                is AiResult.Error -> {
                                    // Handle Error screen or dialog
                                    Text("Error: ${result.message}\nBấm quay lại để thử lại.")
                                    // ...
                                }
                                else -> Text("Checking status...")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }

    private fun extractVideoId(url: String): String? {
        val pattern = "^(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:youtube\\.com\\/(?:(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})"
        return Regex(pattern).find(url)?.groupValues?.get(1)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                // Find URL in text
                val urlRegex = "(https?://(?:www\\.|m\\.)?(?:youtube\\.com/|youtu\\.be/)[^\\s]+)".toRegex()
                val match = urlRegex.find(sharedText)
                if (match != null) {
                    incomingUrl.value = match.value
                } else {
                    // Fallback to extractVideoId to see if there's an ID
                    val extracted = extractVideoId(sharedText)
                    if (extracted != null) {
                        incomingUrl.value = sharedText
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSummaryRequest: (String) -> Unit
) {
    var youtubeLink by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF1A1A2E), DarkBackground)
                )
            )
    ) {
        // Bokeh/Ambient Glows
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(y = (-50).dp, x = (-50).dp)
                .background(YouTubeRed.copy(alpha = 0.1f), CircleShape)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YT Summary AI",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = YouTubeRed
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onHistoryClick,
                        modifier = Modifier.background(GlassWhite.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = TextPrimary)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.background(GlassWhite.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Hero
            Text(text = "Summarize fast.", style = MaterialTheme.typography.displayLarge.copy(lineHeight = 40.sp), color = TextPrimary)
            Text(text = "Understand more.", style = MaterialTheme.typography.displayLarge.copy(lineHeight = 40.sp), color = TextSecondary)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Input Card
            NeonGlassCard(modifier = Modifier.fillMaxWidth(), glowColor = YouTubeRed) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = youtubeLink,
                        onValueChange = { youtubeLink = it },
                        modifier = Modifier.weight(1f).padding(8.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                        decorationBox = { innerTextField ->
                            if (youtubeLink.isEmpty()) {
                                Text("Paste video link...", style = MaterialTheme.typography.bodyLarge, color = TextSecondary.copy(alpha = 0.5f))
                            }
                            innerTextField()
                        }
                    )
                    
                    IconButton(
                        onClick = { if (youtubeLink.isNotBlank()) onSummaryRequest(youtubeLink) },
                        modifier = Modifier.background(YouTubeRed, CircleShape).size(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Go", tint = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clipData = clipboard.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val text = clipData.getItemAt(0).text?.toString() ?: ""
                        if (text.isNotBlank()) {
                            youtubeLink = text
                            onSummaryRequest(text)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
            ) {
                Text("📋 Paste & Tóm tắt nhanh", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("32", color = YouTubeRed, fontWeight = FontWeight.Bold)
                        Text("Summaries", style = MaterialTheme.typography.labelMedium, fontSize = 10.sp)
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Active", color = Color.Green, fontWeight = FontWeight.Bold)
                        Text("Rotation", style = MaterialTheme.typography.labelMedium, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
