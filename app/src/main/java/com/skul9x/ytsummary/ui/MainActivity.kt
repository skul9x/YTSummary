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
import com.skul9x.ytsummary.manager.PythonManager
import com.skul9x.ytsummary.manager.TtsManager
import com.skul9x.ytsummary.model.AiResult
import com.skul9x.ytsummary.repository.SummarizationRepository
import com.skul9x.ytsummary.ui.components.GlassCard
import com.skul9x.ytsummary.ui.components.NeonGlassCard
import com.skul9x.ytsummary.ui.components.PythonUpdateBanner
import com.skul9x.ytsummary.manager.PythonUpdateChecker
import com.skul9x.ytsummary.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull

import androidx.lifecycle.viewmodel.compose.viewModel
import com.skul9x.ytsummary.ui.ScreenState
import androidx.activity.compose.BackHandler
import androidx.paging.compose.collectAsLazyPagingItems

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TtsManager
    private val incomingUrl = MutableStateFlow<String?>(null)
    private var onTtsDoneCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)
        
        ttsManager = TtsManager(
            context = this,
            onInitSuccess = { },
            onTtsDone = {
                runOnUiThread { onTtsDoneCallback?.invoke() }
            }
        )

        com.skul9x.ytsummary.manager.NotificationHelper.createChannel(this)

        setContent {
            YTSummaryTheme {
                val viewModel: SummaryViewModel = viewModel()
                val scope = rememberCoroutineScope()
                
                // Xin quyền Notification (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                    ) { _ ->
                        // Xử lý sau khi xin quyền (nếu cần)
                    }
                    
                    LaunchedEffect(Unit) {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
                
                // Gán callback để TtsManager có thể báo ngược lại ViewModel
                DisposableEffect(Unit) {
                    onTtsDoneCallback = {
                        viewModel.setTtsPlaying(false)
                        viewModel.resetTtsPausedIndex()
                    }
                    onDispose { onTtsDoneCallback = null }
                }
                val uiState by viewModel.uiState.collectAsState()

                // Auto-Read Observer: đọc toàn bộ text từ đầu khi summary hoàn tất
                LaunchedEffect(uiState.autoReadPending) {
                    if (uiState.autoReadPending) {
                        val currentState = uiState.screenState
                        if (currentState is ScreenState.Summary) {
                            val result = currentState.result
                            if (result is AiResult.Success) {
                                ttsManager.speak(result.text, 0)
                                viewModel.setTtsPlaying(true)
                            }
                        }
                        viewModel.clearAutoRead()
                    }
                }

                val sharedUrl by incomingUrl.collectAsState()
                val historyCount by viewModel.getHistoryCount().collectAsState(initial = 0)

                // === FIX: Chặn swipe back gesture cho tất cả màn hình con ===
                BackHandler(enabled = uiState.screenState !is ScreenState.Main) {
                    // Nếu đang ở Summary và đang đọc TTS thì tắt trước
                    if (uiState.screenState is ScreenState.Summary) {
                        ttsManager.stop()
                        viewModel.setTtsPlaying(false)
                        viewModel.resetTtsPausedIndex()
                    }
                    // Quay về màn hình chính
                    viewModel.navigateTo(ScreenState.Main)
                }

                LaunchedEffect(sharedUrl) {
                    sharedUrl?.let { url ->
                        incomingUrl.value = null
                        viewModel.summarize(url)
                    }
                }

                when (val state = uiState.screenState) {
                    is ScreenState.Main -> MainScreen(
                        summaryCount = historyCount,
                        updateInfo = uiState.updateInfo,
                        onSettingsClick = { viewModel.navigateTo(ScreenState.Settings) },
                        onSummaryRequest = { viewModel.summarize(it) },
                        onHistoryClick = { viewModel.navigateTo(ScreenState.History) }
                    )
                    is ScreenState.History -> HistoryScreen(
                        historyItems = viewModel.historyPagingData.collectAsLazyPagingItems(),
                        onDelete = { videoId -> scope.launch { viewModel.deleteHistoryItem(videoId) } },
                        onClearAll = { scope.launch { viewModel.clearAllHistory() } },
                        onBack = { viewModel.navigateTo(ScreenState.Main) },
                        onItemClick = { item ->
                            viewModel.loadFromHistory(item.title, item.thumbnailUrl, item.summaryText)
                        }
                    )
                    is ScreenState.Settings -> SettingsScreen(onBack = { viewModel.navigateTo(ScreenState.Main) })
                    is ScreenState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = YouTubeRed)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(state.message, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    is ScreenState.Summary -> {
                        val result = state.result
                        when (result) {
                            is AiResult.Success -> SummaryScreen(
                                videoTitle = uiState.videoTitle,
                                thumbnailUrl = uiState.thumbnailUrl,
                                summaryText = result.text,
                                isPlaying = uiState.isTtsPlaying,
                                onBack = { 
                                    ttsManager.stop()
                                    viewModel.setTtsPlaying(false)
                                    viewModel.resetTtsPausedIndex()
                                    viewModel.navigateTo(ScreenState.Main) 
                                },
                                onTTSClick = {
                                    if (uiState.isTtsPlaying) {
                                        // Pause: lưu vị trí đọc hiện tại để resume đúng chỗ
                                        val pausedAt = ttsManager.pause()
                                        viewModel.updateTtsPausedIndex(pausedAt)
                                        viewModel.setTtsPlaying(false)
                                    } else {
                                        ttsManager.speak(result.text, viewModel.getTtsPausedIndex())
                                        viewModel.setTtsPlaying(true)
                                    }
                                },
                                onRestartClick = {
                                    ttsManager.stop()
                                    viewModel.resetTtsPausedIndex()
                                    ttsManager.speak(result.text, 0)
                                    viewModel.setTtsPlaying(true)
                                }
                            )
                            is AiResult.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Error: ${result.message}", color = YouTubeRed)
                                        Button(onClick = { viewModel.navigateTo(ScreenState.Main) }) {
                                            Text("Quay lại")
                                        }
                                    }
                                }
                            }
                            else -> Text("Status: $result")
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
        return YOUTUBE_ID_REGEX.find(url)?.groupValues?.get(1)
    }

    companion object {
        private val YOUTUBE_ID_REGEX = Regex("^(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:youtube\\.com\\/(?:(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})")
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
                val urlRegex = "(https?://(?:www\\.|m\\.)?(?:youtube\\.com/|youtu\\.be/)[^\\s]+)".toRegex()
                val match = urlRegex.find(sharedText)
                if (match != null) {
                    incomingUrl.value = match.value
                } else {
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
    summaryCount: Int,
    updateInfo: PythonUpdateChecker.UpdateInfo?,
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PythonUpdateBanner(updateInfo = updateInfo)
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                        Text("$summaryCount", color = YouTubeRed, fontWeight = FontWeight.Bold)
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
