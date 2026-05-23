package com.example.mychatapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mychatapp.data.remote.ApiConfig
import com.example.mychatapp.ui.theme.IslandBlue
import com.example.mychatapp.ui.theme.IslandGreen500
import com.example.mychatapp.ui.theme.IslandGreen700
import com.example.mychatapp.ui.theme.IslandMuted
import com.example.mychatapp.ui.theme.IslandText

@Composable
fun Avatar(
    icon: String,
    size: Dp,
    modifier: Modifier = Modifier,
    backgroundColor: Brush = Brush.linearGradient(listOf(Color(0xFFD5F4FF), Color(0xFFFFF1CB)))
) {
    val context = LocalContext.current
    val imageUrl = ApiConfig.resolveAssetUrl(context, icon)
        ?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(icon, fontSize = (size.value * 0.42).sp, fontWeight = FontWeight.Bold, color = IslandText)
        }
    }
}

@Composable
fun SearchBar(
    text: String,
    modifier: Modifier = Modifier,
    trailing: String = ""
) {
    InputLike(leading = "搜", text = text, trailing = trailing, modifier = modifier)
}

@Composable
fun InputLike(
    leading: String,
    text: String,
    modifier: Modifier = Modifier,
    trailing: String = ""
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.76f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading.isNotEmpty()) Text(leading, fontSize = 18.sp, color = IslandGreen500, fontWeight = FontWeight.Bold)
            if (leading.isNotEmpty()) Spacer(Modifier.width(12.dp))
            Text(text, color = Color(0xFF999999), fontSize = 17.sp, modifier = Modifier.weight(1f))
            if (trailing.isNotEmpty()) Text(trailing, color = IslandGreen500, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FilterTabs(
    tabs: List<String>,
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        tabs.forEachIndexed { index, text ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text,
                    color = if (index == selectedIndex) IslandGreen700 else IslandText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                if (index == selectedIndex) {
                    Box(
                        Modifier
                            .padding(top = 8.dp)
                            .size(width = 42.dp, height = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(IslandGreen500)
                    )
                }
            }
        }
    }
}

@Composable
fun IslandLandscapeStrip(modifier: Modifier = Modifier) {
    IslandSceneCard(
        modifier = modifier.fillMaxWidth().height(116.dp),
        title = "小岛生活",
        compact = true
    )
}

@Composable
fun SkyDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        fun cloud(cx: Float, cy: Float, scale: Float) {
            val color = Color.White.copy(alpha = 0.34f)
            drawCircle(color, radius = 22f * scale, center = Offset(cx, cy))
            drawCircle(color, radius = 16f * scale, center = Offset(cx + 28f * scale, cy + 2f * scale))
            drawCircle(color, radius = 14f * scale, center = Offset(cx - 24f * scale, cy + 4f * scale))
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - 42f * scale, cy + 8f * scale),
                size = androidx.compose.ui.geometry.Size(84f * scale, 18f * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f * scale, 24f * scale)
            )
        }
        cloud(w * 0.82f, 92f, 1.1f)
        cloud(w * 0.94f, 154f, 0.72f)
    }
}

@Composable
fun IslandSceneCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
        color = IslandBlue.copy(alpha = 0.72f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFDDF7FF), Color(0xFFEAF9D2), Color(0xFFFFE8A8))
                    )
                )
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val seaTop = if (compact) h * 0.64f else h * 0.68f

                drawCircle(Color.White.copy(alpha = 0.9f), radius = w * 0.055f, center = Offset(w * 0.82f, h * 0.18f))
                drawCircle(Color.White.copy(alpha = 0.75f), radius = w * 0.04f, center = Offset(w * 0.88f, h * 0.19f))
                drawCircle(Color.White.copy(alpha = 0.7f), radius = w * 0.035f, center = Offset(w * 0.77f, h * 0.2f))

                drawOval(
                    color = Color(0xFF7FC96F),
                    topLeft = Offset(w * 0.18f, h * 0.58f),
                    size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.2f)
                )
                drawOval(
                    color = Color(0xFFF2D98A),
                    topLeft = Offset(w * 0.23f, h * 0.62f),
                    size = androidx.compose.ui.geometry.Size(w * 0.32f, h * 0.08f)
                )
                drawRect(
                    color = Color(0xFF9B6C32),
                    topLeft = Offset(w * 0.52f, h * 0.43f),
                    size = androidx.compose.ui.geometry.Size(w * 0.11f, h * 0.18f)
                )
                val roof = Path().apply {
                    moveTo(w * 0.49f, h * 0.43f)
                    lineTo(w * 0.575f, h * 0.34f)
                    lineTo(w * 0.66f, h * 0.43f)
                    close()
                }
                drawPath(roof, Color(0xFF2E7D52))

                drawRect(
                    color = Color(0xFF2F8FA3).copy(alpha = 0.55f),
                    topLeft = Offset(0f, seaTop),
                    size = androidx.compose.ui.geometry.Size(w, h - seaTop)
                )
                repeat(3) { index ->
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.55f),
                        topLeft = Offset(w * (0.12f + index * 0.28f), seaTop + h * (0.08f + index * 0.04f)),
                        size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.018f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                    )
                }
            }
            if (!title.isNullOrBlank()) {
                Column(Modifier.align(Alignment.TopStart).padding(if (compact) 16.dp else 22.dp)) {
                    Text(title, color = IslandText, fontWeight = FontWeight.Black, fontSize = if (compact) 18.sp else 26.sp)
                    if (!subtitle.isNullOrBlank()) {
                        Text(subtitle, color = Color(0xFF4F6668), fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Black, color = IslandText)
        Text(label, fontSize = 14.sp, color = IslandMuted)
    }
}

@Composable
fun SettingRow(icon: String, title: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp, color = IslandMuted)
        Spacer(Modifier.width(18.dp))
        Text(title, fontSize = 20.sp, color = IslandText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("›", fontSize = 30.sp, color = IslandMuted)
    }
}
