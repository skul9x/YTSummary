package com.skul9x.ytsummary.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skul9x.ytsummary.manager.PythonUpdateChecker

/**
 * Banner hiển thị thông báo khi có bản cập nhật thư viện Python.
 * Nhận UpdateInfo? từ parent — nếu null thì không render gì.
 *
 * @param updateInfo Thông tin cập nhật (null = không có update hoặc chưa check xong).
 */
@Composable
fun PythonUpdateBanner(
    updateInfo: PythonUpdateChecker.UpdateInfo?,
    modifier: Modifier = Modifier
) {
    updateInfo?.let { info ->
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF332B00) // Dark amber, phù hợp dark theme
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                // Viền amber nhẹ cho nổi bật
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Update Available",
                    tint = Color(0xFFFFC107), // Amber
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Có bản cập nhật thư viện Python!",
                        color = Color(0xFFFFC107),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "youtube-transcript-api: ${info.currentVersion} ➔ ${info.latestVersion}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Mở Android Studio → sửa build.gradle.kts → Sync & Run.",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
