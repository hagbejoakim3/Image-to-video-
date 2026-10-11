package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.PresetAvatar
import com.example.ui.StudioViewModel
import com.example.ui.components.SubtitlesOverlay
import com.example.ui.components.TalkingAvatarView
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryBright
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioCreateScreen(
    viewModel: StudioViewModel,
    onNavigateToPlayer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val currentWordIndex by viewModel.currentWordIndex.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val speechProgress by viewModel.speechProgress.collectAsState()

    var showTuningOptions by remember { mutableStateOf(false) }

    // Modern Android Photo Picker (zero storage permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.selectCustomPhoto(uri)
            Toast.makeText(context, "Photo importée avec succès !", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Studio Vidéo Parlante",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Transformez votre photo en vidéo animée",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Surface(
                color = StudioPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = StudioPrimaryBright,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "IA Animée",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPrimaryBright
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Centerpiece Animated Talking Photo Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
        ) {
            TalkingAvatarView(
                avatarSource = uiState.selectedAvatarSource,
                isSpeaking = isSpeaking,
                audioAmplitude = audioAmplitude,
                videoFilterStyle = uiState.videoFilterStyle,
                modifier = Modifier.fillMaxSize()
            )

            // Subtitles overlay directly on top of video view
            SubtitlesOverlay(
                scriptText = uiState.scriptText,
                currentWordIndex = currentWordIndex,
                isSpeaking = isSpeaking,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Speech progress bar when talking
        if (isSpeaking) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { speechProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = StudioSecondary,
                trackColor = Color(0x3306B6D4)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Avatar Switcher + Custom Photo Import Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choisir l'Image ou l'Avatar",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextSecondary
            )
            Text(
                text = "Importer ma photo",
                color = StudioSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal avatars selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Import photo button card
            Card(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                modifier = Modifier.size(width = 80.dp, height = 96.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Importer photo",
                        tint = StudioSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ma Photo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Preset Avatars
            PresetAvatar.ALL.forEach { avatar ->
                val isSelected = uiState.selectedAvatarSource == "res:${avatar.id}"
                Card(
                    onClick = { viewModel.selectPresetAvatar(avatar) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StudioPrimary.copy(alpha = 0.2f) else StudioSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) StudioPrimary else StudioCardBorder
                    ),
                    modifier = Modifier.size(width = 80.dp, height = 96.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) StudioPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            AsyncImage(
                                model = avatar.drawableRes,
                                contentDescription = avatar.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = avatar.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StudioPrimaryBright else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Script Text Input Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ce que la vidéo doit dire :",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextSecondary
            )

            // AI Script Assistant Button
            OutlinedButton(
                onClick = { viewModel.generateOrEnhanceScript() },
                enabled = !uiState.isGeneratingAiScript,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioPrimaryBright),
                modifier = Modifier.testTag("ai_enhance_script_button")
            ) {
                if (uiState.isGeneratingAiScript) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = StudioPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Création...", fontSize = 12.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = StudioAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Améliorer IA", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.scriptText,
            onValueChange = { viewModel.updateScriptText(it) },
            placeholder = { Text("Écrivez le texte que votre photo va prononcer...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .testTag("script_input_field"),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = StudioSurface,
                unfocusedContainerColor = StudioSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = StudioPrimary,
                unfocusedIndicatorColor = StudioCardBorder
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick French Script Inspiration Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val templates = listOf(
                "🎙️ Présentation" to "Bonjour à tous ! Je suis ravi de vous accueillir dans cette nouvelle vidéo.",
                "📰 Flash JT" to "Flash spécial ! Une avancée technologique majeure vient de transformer le monde des images.",
                "😂 Blague" to "Pourquoi les plongeurs plongent toujours en arrière ? Parce que sinon ils tombent dans le bateau !",
                "🎂 Anniversaire" to "Joyeux anniversaire ! Que cette année t'apporte bonheur, santé et plein de succès !",
                "🚀 Motivation" to "Croyez en vos rêves. Chaque grand accomplissement a commencé par une simple décision d'essayer."
            )
            templates.forEach { (label, text) ->
                Surface(
                    onClick = { viewModel.updateScriptText(text) },
                    shape = RoundedCornerShape(12.dp),
                    color = StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice Tone & Emotion Chips
        Text(
            text = "Ton & Émotion de la Voix",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tones = listOf(
                "Joyeux & Dynamique",
                "Sérieux & Pro",
                "Futuriste & Robot",
                "Doux & Apaisant",
                "Narrateur Épique"
            )
            tones.forEach { tone ->
                val isSelected = uiState.selectedVoiceTone == tone
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setVoiceTone(tone) },
                    label = { Text(tone, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = StudioSurface,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Video Filter Style Chips (Studio, Cyberpunk, Vintage, Hologram, Cinéma)
        Text(
            text = "Style Visuel de la Vidéo",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val styles = listOf(
                "Studio" to "🎬 Studio Pro",
                "Cyberpunk" to "⚡ Cyberpunk",
                "Vintage" to "🎞️ Vintage Film",
                "Hologram" to "🌐 Hologramme",
                "Cinéma" to "🎥 Cinéma"
            )
            styles.forEach { (styleKey, label) ->
                val isSelected = uiState.videoFilterStyle == styleKey
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setVideoFilterStyle(styleKey) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioSecondary,
                        selectedLabelColor = Color.Black,
                        containerColor = StudioSurface,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Advanced Voice Fine-Tuning Accordion
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showTuningOptions = !showTuningOptions }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = StudioPrimaryBright,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (showTuningOptions) "Masquer les réglages audio" else "Affiner la vitesse & tonalité",
                fontSize = 13.sp,
                color = StudioPrimaryBright,
                fontWeight = FontWeight.Medium
            )
        }

        AnimatedVisibility(visible = showTuningOptions) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Vitesse d'élocution: ${(uiState.speechRate * 100).toInt()}%",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.speechRate,
                        onValueChange = { viewModel.setSpeechRate(it) },
                        valueRange = 0.6f..1.6f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioSecondary,
                            activeTrackColor = StudioSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tonalité (Grave / Aiguë): ${(uiState.speechPitch * 100).toInt()}%",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.speechPitch,
                        onValueChange = { viewModel.setSpeechPitch(it) },
                        valueRange = 0.6f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioPrimary,
                            activeTrackColor = StudioPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Primary Bottom Action Bar: Preview & Export Video Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Preview / Play Button
            Button(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("preview_speech_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpeaking) StudioAccent else StudioSurfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSpeaking) StudioAccent else StudioCardBorder)
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isSpeaking) "Pause" else "Écouter",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSpeaking) "Arrêter" else "Écouter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            // Export / Create Video Button
            Button(
                onClick = {
                    viewModel.exportAndSaveVideo { createdId ->
                        onNavigateToPlayer(createdId)
                    }
                },
                enabled = !uiState.isExportingVideo,
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp)
                    .testTag("export_video_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioPrimary
                )
            ) {
                if (uiState.isExportingVideo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Génération...",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Créer la vidéo",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Créer la Vidéo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
