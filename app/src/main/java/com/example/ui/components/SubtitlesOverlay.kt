package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioSecondary

@Composable
fun SubtitlesOverlay(
    scriptText: String,
    currentWordIndex: Int,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    if (scriptText.isBlank()) return

    val words = remember(scriptText) {
        scriptText.trim().split(Regex("\\s+"))
    }

    // Keep visible window centered around current word
    val windowSize = 10
    val startIdx = (currentWordIndex - 4).coerceAtLeast(0)
    val endIdx = (startIdx + windowSize).coerceAtMost(words.size)
    val displayWords = words.subList(startIdx, endIdx)

    val annotatedString = buildAnnotatedString {
        displayWords.forEachIndexed { relativeIdx, word ->
            val absoluteIdx = startIdx + relativeIdx
            val isCurrent = isSpeaking && absoluteIdx == currentWordIndex
            val isPast = isSpeaking && absoluteIdx < currentWordIndex

            if (isCurrent) {
                withStyle(
                    style = SpanStyle(
                        color = StudioSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        background = Color(0x3306B6D4)
                    )
                ) {
                    append(word)
                }
            } else if (isPast) {
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFFF1F5F9),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                ) {
                    append(word)
                }
            } else {
                withStyle(
                    style = SpanStyle(
                        color = Color(0x9994A3B8),
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp
                    )
                ) {
                    append(word)
                }
            }
            append(" ")
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xCC0B0716), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = annotatedString,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}
