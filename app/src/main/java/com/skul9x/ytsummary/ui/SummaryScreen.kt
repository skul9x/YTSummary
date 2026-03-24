package com.skul9x.ytsummary.ui

import coil.compose.AsyncImage
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skul9x.ytsummary.ui.components.GlassCard
import com.skul9x.ytsummary.ui.components.NeonGlassCard
import com.skul9x.ytsummary.ui.theme.*

@Composable
fun SummaryScreen(
    videoTitle: String,
    thumbnailUrl: String = "",
    summaryText: String,
    onBack: () -> Unit,
    onTTSClick: () -> Unit,
    isPlaying: Boolean = false
) {
    androidx.activity.compose.BackHandler(onBack = onBack)

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
                    "AI Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Summary Card
            NeonGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                glowColor = YouTubeRed
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (thumbnailUrl.isNotEmpty()) {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(bottom = 16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    Text(
                        text = videoTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = YouTubeRed,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clipData = android.content.ClipData.newPlainText("Summary", summaryText)
                            clipboard.setPrimaryClip(clipData)
                            android.widget.Toast.makeText(context, "Đã copy vào bộ nhớ tạm", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlassWhite.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("📋 Sao chép", color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // TTS Controls Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Voice Assistant",
                            style = MaterialTheme.typography.labelMedium,
                            color = YouTubeRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isPlaying) "Playing summary..." else "Ready to read",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { /* Restart handled by Click */ },
                            modifier = Modifier.background(GlassWhite.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart", tint = TextPrimary)
                        }
                        
                        // Task 4.3: Bounce Effect Implementation
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.85f else 1f,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                            label = "ButtonScale"
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .size(56.dp)
                                .background(YouTubeRed, CircleShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null, // Custom scale handling
                                    onClick = onTTSClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
