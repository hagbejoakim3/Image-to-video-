package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary

@Composable
fun TalkingAvatarView(
    avatarSource: String, // "res:anchor", "res:cyber", "res:scholar", "res:creator", or "file:..." / "content:..."
    isSpeaking: Boolean,
    audioAmplitude: Float,
    videoFilterStyle: String = "Studio",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Head breathing & floating transition
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_motion")
    val breathingFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Speaking head tilt / slight sway
    val headSway by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "head_sway"
    )

    // Hologram scanner line vertical movement
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )

    // Dynamic mouth openness smoothed with amplitude
    val targetMouthOpen = if (isSpeaking) {
        (audioAmplitude * 28f).coerceIn(4f, 32f)
    } else {
        0f
    }
    val animatedMouthOpen by animateFloatAsState(
        targetValue = targetMouthOpen,
        animationSpec = tween(durationMillis = 65, easing = LinearEasing),
        label = "mouth_open"
    )

    val currentHeadTilt = if (isSpeaking) headSway else 0f
    val currentBreathing = if (isSpeaking) breathingFloat else breathingFloat * 0.5f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0A0714))
            .border(2.dp, getBorderColorForStyle(videoFilterStyle), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Base Avatar Image Container with sway & breathing
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = currentBreathing
                    rotationZ = currentHeadTilt
                    scaleX = if (isSpeaking) 1.02f else 1.0f
                    scaleY = if (isSpeaking) 1.02f else 1.0f
                }
        ) {
            // Render Avatar using either Resource Drawable or File/URI
            val imageModel = when {
                avatarSource == "res:robin" || avatarSource.contains("robin") -> R.drawable.avatar_robin
                avatarSource == "res:anchor" || avatarSource.contains("anchor") -> R.drawable.avatar_anchor
                avatarSource == "res:cyber" || avatarSource.contains("cyber") -> R.drawable.avatar_cyber
                avatarSource == "res:scholar" || avatarSource.contains("scholar") -> R.drawable.avatar_scholar
                avatarSource == "res:creator" || avatarSource.contains("creator") -> R.drawable.avatar_creator
                avatarSource.startsWith("content://") || avatarSource.startsWith("file://") -> Uri.parse(avatarSource)
                else -> R.drawable.avatar_anchor
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageModel)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.avatar_anchor),
                error = painterResource(id = R.drawable.avatar_anchor),
                contentDescription = "Avatar parlé",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Lip Sync / Animated Mouth Layer
            if (isSpeaking && animatedMouthOpen > 1f) {
                MouthLipSyncCanvas(
                    mouthOpen = animatedMouthOpen,
                    amplitude = audioAmplitude,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Cinematic Video Filter Overlays
        VideoFilterOverlay(
            filterStyle = videoFilterStyle,
            scanLineProgress = scanLineY,
            isSpeaking = isSpeaking,
            modifier = Modifier.fillMaxSize()
        )

        // Top Status Bar: Live On-Air / Talking Badge & Audio Equalizer
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSpeaking) {
                Surface(
                    color = Color(0xCCEF4444),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EN PAROLE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Audio Wave Visualizer pill
                AudioWavePill(amplitude = audioAmplitude)
            } else {
                Surface(
                    color = Color(0x9918132F),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Prêt",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated mouth opening & lip articulation matching speech rhythm
 */
@Composable
private fun MouthLipSyncCanvas(
    mouthOpen: Float,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width * 0.5f
        val centerY = size.height * 0.675f // Anatomically centered around the mouth area of portrait
        val mouthWidth = (size.width * 0.16f) + (amplitude * 12f)
        val mouthHeight = (mouthOpen * 0.85f).coerceIn(4f, 38f)

        // Natural lip shadow / mouth interior
        val mouthPath = Path().apply {
            moveTo(centerX - mouthWidth, centerY)
            cubicTo(
                centerX - mouthWidth * 0.5f, centerY - mouthHeight * 0.7f,
                centerX + mouthWidth * 0.5f, centerY - mouthHeight * 0.7f,
                centerX + mouthWidth, centerY
            )
            cubicTo(
                centerX + mouthWidth * 0.5f, centerY + mouthHeight,
                centerX - mouthWidth * 0.5f, centerY + mouthHeight,
                centerX - mouthWidth, centerY
            )
            close()
        }

        // Mouth dark cavity
        drawPath(
            path = mouthPath,
            color = Color(0xEE2A0A12)
        )

        // Upper teeth highlight when mouth is reasonably open
        if (mouthHeight > 10f) {
            val teethWidth = mouthWidth * 0.7f
            val teethHeight = (mouthHeight * 0.3f).coerceAtMost(6f)
            drawRoundRect(
                color = Color(0xF2F8FAFC),
                topLeft = Offset(centerX - teethWidth * 0.5f, centerY - mouthHeight * 0.4f),
                size = Size(teethWidth, teethHeight),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }

        // Natural lower lip curve border
        drawPath(
            path = mouthPath,
            color = Color(0x66831843),
            style = Stroke(width = 2.5f)
        )
    }
}

@Composable
private fun AudioWavePill(amplitude: Float) {
    Surface(
        color = Color(0xDD18132F),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val heights = listOf(
                (amplitude * 18f).coerceIn(4f, 20f),
                (amplitude * 26f).coerceIn(6f, 24f),
                (amplitude * 14f).coerceIn(3f, 16f),
                (amplitude * 22f).coerceIn(5f, 22f)
            )
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.5.dp)
                        .width(3.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(StudioSecondary)
                )
            }
        }
    }
}

/**
 * Filter styling effects (Studio Pro, Cyberpunk, Vintage Film, Hologramme, Cinéma)
 */
@Composable
private fun VideoFilterOverlay(
    filterStyle: String,
    scanLineProgress: Float,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        when (filterStyle) {
            "Studio" -> {
                // Subtle professional studio vignette
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0x77050212)),
                        center = center,
                        radius = size.minDimension * 0.85f
                    )
                )
            }
            "Cyberpunk" -> {
                // Futuristic scanlines & neon ambiance
                val scanY = size.height * scanLineProgress
                drawLine(
                    color = Color(0xAA06B6D4),
                    start = Offset(0f, scanY),
                    end = Offset(size.width, scanY),
                    strokeWidth = 3f
                )
                // Corner cyber brackets
                drawCyberBrackets(size)
            }
            "Vintage" -> {
                // Warm retro sepia tone
                drawRect(
                    color = Color(0x2892400E)
                )
                // Film edges
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x55000000), Color.Transparent, Color(0x66000000))
                    )
                )
            }
            "Hologram" -> {
                // Matrix/Hologram cyan tint and grid
                drawRect(
                    color = Color(0x1F06B6D4)
                )
                val scanY = size.height * scanLineProgress
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xDD22D3EE), Color.Transparent)
                    ),
                    start = Offset(0f, scanY),
                    end = Offset(size.width, scanY),
                    strokeWidth = 4f
                )
            }
            "Cinéma" -> {
                // Widescreen 2.39:1 letterbox bars top and bottom
                val barHeight = size.height * 0.08f
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, barHeight)
                )
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(0f, size.height - barHeight),
                    size = Size(size.width, barHeight)
                )
            }
        }
    }
}

private fun DrawScope.drawCyberBrackets(canvasSize: Size) {
    val bracketLen = 30f
    val stroke = 3f
    val color = Color(0xAAEC4899)

    // Top-left bracket
    drawLine(color, Offset(20f, 20f), Offset(20f + bracketLen, 20f), strokeWidth = stroke)
    drawLine(color, Offset(20f, 20f), Offset(20f, 20f + bracketLen), strokeWidth = stroke)

    // Bottom-right bracket
    drawLine(color, Offset(canvasSize.width - 20f, canvasSize.height - 20f), Offset(canvasSize.width - 20f - bracketLen, canvasSize.height - 20f), strokeWidth = stroke)
    drawLine(color, Offset(canvasSize.width - 20f, canvasSize.height - 20f), Offset(canvasSize.width - 20f, canvasSize.height - 20f - bracketLen), strokeWidth = stroke)
}

private fun getBorderColorForStyle(style: String): Color {
    return when (style) {
        "Cyberpunk" -> StudioSecondary
        "Hologram" -> Color(0xFF22D3EE)
        "Cinéma" -> Color(0xFFF59E0B)
        "Vintage" -> Color(0xFFD97706)
        else -> StudioPrimary
    }
}
