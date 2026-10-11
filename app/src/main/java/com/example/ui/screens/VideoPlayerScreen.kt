package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TalkingVideoProject
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    projectId: Long,
    viewModel: StudioViewModel,
    onBack: () -> Unit,
    onEditInStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedProjects by viewModel.savedProjects.collectAsState()
    val project = savedProjects.find { it.id == projectId }

    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val currentWordIndex by viewModel.currentWordIndex.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val speechProgress by viewModel.speechProgress.collectAsState()

    // Intercept back button gracefully
    BackHandler {
        viewModel.stopSpeaking()
        onBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSpeaking()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = project?.title ?: "Lecteur Vidéo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopSpeaking()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    project?.let { p ->
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, p.title)
                                putExtra(Intent.EXTRA_TEXT, "${p.title}\n\n\"${p.scriptText}\"\n\nCréé avec l'application PhotoParle !")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager la vidéo"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Partager",
                                tint = StudioSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioSurface
                )
            )
        },
        containerColor = Color(0xFF0F0B1E)
    ) { padding ->
        if (project == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Vidéo introuvable", color = TextSecondary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Video View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                TalkingAvatarView(
                    avatarSource = project.avatarSource,
                    isSpeaking = isSpeaking,
                    audioAmplitude = audioAmplitude,
                    videoFilterStyle = project.videoFilterStyle,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtitles Overlay
                SubtitlesOverlay(
                    scriptText = project.scriptText,
                    currentWordIndex = currentWordIndex,
                    isSpeaking = isSpeaking,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Progress Scrubber
            LinearProgressIndicator(
                progress = { if (isSpeaking) speechProgress else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StudioSecondary,
                trackColor = Color(0x3306B6D4)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Player Controls: Replay, Play/Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replay Button
                IconButton(
                    onClick = {
                        viewModel.stopSpeaking()
                        viewModel.ttsManager.speak(
                            text = project.scriptText,
                            pitch = project.speechPitch,
                            rate = project.speechRate
                        )
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StudioSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Recommencer",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Play / Pause Primary Button
                Button(
                    onClick = {
                        if (isSpeaking) {
                            viewModel.stopSpeaking()
                        } else {
                            viewModel.ttsManager.speak(
                                text = project.scriptText,
                                pitch = project.speechPitch,
                                rate = project.speechRate
                            )
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) StudioAccent else StudioPrimary
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isSpeaking) "Pause" else "Lire",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Project Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Script prononcé :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = StudioSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${project.scriptText}\"",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Style visuel", fontSize = 11.sp, color = TextSecondary)
                            Text(text = project.videoFilterStyle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Column {
                            Text(text = "Ton vocal", fontSize = 11.sp, color = TextSecondary)
                            Text(text = project.voiceTone, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Column {
                            Text(text = "Date", fontSize = 11.sp, color = TextSecondary)
                            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(project.createdAt))
                            Text(text = dateStr, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary Actions: Edit in Studio, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.loadProjectIntoStudio(project)
                        onEditInStudio()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = StudioPrimaryBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Modifier", color = StudioPrimaryBright)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.deleteProject(project)
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Supprimer", color = Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
