package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.PresetAvatar
import com.example.data.model.TalkingVideoProject
import com.example.engine.GeminiScriptService
import com.example.engine.TtsSpeechManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class StudioUiState(
    val selectedAvatarSource: String = "res:anchor",
    val selectedAvatarName: String = "Lucas",
    val scriptText: String = "Bonsoir à tous ! En direct de notre studio, voici les actualités de la journée.",
    val speechPitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val selectedVoiceTone: String = "Sérieux & Pro",
    val videoFilterStyle: String = "Studio", // Studio, Cyberpunk, Vintage, Hologram, Cinéma
    val bgAmbiance: String = "Aucun",
    val isGeneratingAiScript: Boolean = false,
    val isExportingVideo: Boolean = false,
    val currentPlayingProjectId: Long? = null,
    val userMessage: String? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val projectDao = db.projectDao()
    val ttsManager = TtsSpeechManager(application)
    private val geminiService = GeminiScriptService()

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val savedProjects: StateFlow<List<TalkingVideoProject>> = projectDao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSpeaking = ttsManager.isSpeaking
    val currentWordIndex = ttsManager.currentWordIndex
    val audioAmplitude = ttsManager.audioAmplitude
    val speechProgress = ttsManager.speechProgress

    val isAiAvailable = geminiService.isApiKeyAvailable

    fun selectPresetAvatar(avatar: PresetAvatar) {
        _uiState.value = _uiState.value.copy(
            selectedAvatarSource = "res:${avatar.id}",
            selectedAvatarName = avatar.name,
            scriptText = avatar.defaultGreeting,
            selectedVoiceTone = avatar.defaultTone
        )
        // Stop any ongoing speech when switching avatar
        ttsManager.stop()
    }

    fun selectCustomPhoto(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedAvatarSource = uri.toString(),
            selectedAvatarName = "Ma Photo Personnalisée"
        )
        ttsManager.stop()
    }

    fun updateScriptText(text: String) {
        _uiState.value = _uiState.value.copy(scriptText = text)
    }

    fun setSpeechPitch(pitch: Float) {
        _uiState.value = _uiState.value.copy(speechPitch = pitch)
    }

    fun setSpeechRate(rate: Float) {
        _uiState.value = _uiState.value.copy(speechRate = rate)
    }

    fun setVoiceTone(tone: String) {
        val (pitch, rate) = when (tone) {
            "Joyeux & Dynamique" -> Pair(1.2f, 1.15f)
            "Sérieux & Pro" -> Pair(0.95f, 0.95f)
            "Futuriste & Robot" -> Pair(0.75f, 1.1f)
            "Doux & Apaisant" -> Pair(1.05f, 0.85f)
            "Narrateur Épique" -> Pair(0.85f, 0.9f)
            else -> Pair(1.0f, 1.0f)
        }
        _uiState.value = _uiState.value.copy(
            selectedVoiceTone = tone,
            speechPitch = pitch,
            speechRate = rate
        )
    }

    fun setVideoFilterStyle(style: String) {
        _uiState.value = _uiState.value.copy(videoFilterStyle = style)
    }

    fun setBgAmbiance(ambiance: String) {
        _uiState.value = _uiState.value.copy(bgAmbiance = ambiance)
    }

    fun togglePlayPause() {
        if (ttsManager.isSpeaking.value) {
            ttsManager.stop()
        } else {
            val text = _uiState.value.scriptText
            if (text.isNotBlank()) {
                ttsManager.speak(
                    text = text,
                    pitch = _uiState.value.speechPitch,
                    rate = _uiState.value.speechRate
                )
            }
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun generateOrEnhanceScript(topicOrPrompt: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingAiScript = true)
            val promptToUse = topicOrPrompt.ifBlank { _uiState.value.scriptText }
            val result = geminiService.generateOrEnhanceScript(
                prompt = promptToUse,
                style = _uiState.value.selectedVoiceTone,
                avatarRole = _uiState.value.selectedAvatarName
            )
            result.onSuccess { script ->
                _uiState.value = _uiState.value.copy(
                    scriptText = script,
                    isGeneratingAiScript = false,
                    userMessage = "Script généré avec succès !"
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isGeneratingAiScript = false,
                    userMessage = "Impossible de générer le script"
                )
            }
        }
    }

    fun exportAndSaveVideo(onSuccess: (Long) -> Unit) {
        val currentState = _uiState.value
        if (currentState.scriptText.isBlank()) {
            _uiState.value = _uiState.value.copy(userMessage = "Veuillez écrire un texte avant d'exporter")
            return
        }

        _uiState.value = _uiState.value.copy(isExportingVideo = true)

        viewModelScope.launch {
            try {
                // Generate permanent audio file
                val audioDir = File(getApplication<Application>().filesDir, "talking_videos_audio")
                if (!audioDir.exists()) audioDir.mkdirs()
                val audioFile = File(audioDir, "audio_${System.currentTimeMillis()}.wav")

                ttsManager.synthesizeToFile(
                    text = currentState.scriptText,
                    pitch = currentState.speechPitch,
                    rate = currentState.speechRate,
                    outputFile = audioFile,
                    onDone = { savedAudioFile ->
                        viewModelScope.launch {
                            val wordCount = currentState.scriptText.trim().split(Regex("\\s+")).size
                            val estimatedSec = (wordCount / (2.5f * currentState.speechRate)).toInt().coerceAtLeast(3)

                            val project = TalkingVideoProject(
                                title = "Vidéo Parlante - ${currentState.selectedAvatarName}",
                                avatarSource = currentState.selectedAvatarSource,
                                scriptText = currentState.scriptText,
                                voiceTone = currentState.selectedVoiceTone,
                                speechRate = currentState.speechRate,
                                speechPitch = currentState.speechPitch,
                                videoFilterStyle = currentState.videoFilterStyle,
                                bgAmbiance = currentState.bgAmbiance,
                                audioPath = savedAudioFile.absolutePath,
                                estimatedDurationSec = estimatedSec
                            )

                            val newId = projectDao.insertProject(project)
                            _uiState.value = _uiState.value.copy(
                                isExportingVideo = false,
                                userMessage = "Vidéo créée et sauvegardée dans votre galerie !"
                            )
                            onSuccess(newId)
                        }
                    },
                    onError = { errMsg ->
                        // Even if file synthesis had an issue, save project with in-memory synthesis capability
                        viewModelScope.launch {
                            val project = TalkingVideoProject(
                                title = "Vidéo Parlante - ${currentState.selectedAvatarName}",
                                avatarSource = currentState.selectedAvatarSource,
                                scriptText = currentState.scriptText,
                                voiceTone = currentState.selectedVoiceTone,
                                speechRate = currentState.speechRate,
                                speechPitch = currentState.speechPitch,
                                videoFilterStyle = currentState.videoFilterStyle,
                                bgAmbiance = currentState.bgAmbiance,
                                estimatedDurationSec = 5
                            )
                            val newId = projectDao.insertProject(project)
                            _uiState.value = _uiState.value.copy(
                                isExportingVideo = false,
                                userMessage = "Vidéo sauvegardée !"
                            )
                            onSuccess(newId)
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExportingVideo = false,
                    userMessage = "Erreur lors de l'enregistrement de la vidéo"
                )
            }
        }
    }

    fun loadProjectIntoStudio(project: TalkingVideoProject) {
        _uiState.value = _uiState.value.copy(
            selectedAvatarSource = project.avatarSource,
            selectedAvatarName = project.title.removePrefix("Vidéo Parlante - "),
            scriptText = project.scriptText,
            speechPitch = project.speechPitch,
            speechRate = project.speechRate,
            selectedVoiceTone = project.voiceTone,
            videoFilterStyle = project.videoFilterStyle,
            bgAmbiance = project.bgAmbiance,
            userMessage = "Projet chargé dans le studio"
        )
    }

    fun deleteProject(project: TalkingVideoProject) {
        viewModelScope.launch {
            projectDao.deleteProject(project)
            project.audioPath?.let { path ->
                try { File(path).delete() } catch (_: Exception) {}
            }
            _uiState.value = _uiState.value.copy(userMessage = "Vidéo supprimée")
        }
    }

    fun clearUserMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
    }
}
