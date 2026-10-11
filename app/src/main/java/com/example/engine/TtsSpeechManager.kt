package com.example.engine

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.UUID

class TtsSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentWordIndex = MutableStateFlow(0)
    val currentWordIndex: StateFlow<Int> = _currentWordIndex.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _speechProgress = MutableStateFlow(0f)
    val speechProgress: StateFlow<Float> = _speechProgress.asStateFlow()

    private var wordsList: List<String> = emptyList()
    private var amplitudeJob: Job? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // Prefer French if available, otherwise default
            val langResult = tts?.setLanguage(Locale.FRENCH)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    mainHandler.post {
                        _isSpeaking.value = true
                        startAmplitudeSimulation()
                    }
                }

                override fun onDone(utteranceId: String?) {
                    mainHandler.post {
                        _isSpeaking.value = false
                        _audioAmplitude.value = 0f
                        _speechProgress.value = 1f
                        stopAmplitudeSimulation()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        _isSpeaking.value = false
                        _audioAmplitude.value = 0f
                        stopAmplitudeSimulation()
                    }
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    mainHandler.post {
                        calculateCurrentWordIndex(start)
                    }
                }
            })
        }
    }

    fun speak(
        text: String,
        pitch: Float = 1.0f,
        rate: Float = 1.0f,
        onComplete: (() -> Unit)? = null
    ) {
        if (text.isBlank()) return
        stop()

        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))

        wordsList = text.trim().split(Regex("\\s+"))
        _currentWordIndex.value = 0
        _speechProgress.value = 0f

        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun calculateCurrentWordIndex(charOffset: Int) {
        if (wordsList.isEmpty()) return
        // Count approximate words passed up to this char offset
        var currentLen = 0
        for (i in wordsList.indices) {
            currentLen += wordsList[i].length + 1
            if (currentLen >= charOffset) {
                _currentWordIndex.value = i
                _speechProgress.value = (i.toFloat() / wordsList.size.coerceAtLeast(1)).coerceIn(0f, 1f)
                break
            }
        }
    }

    private fun startAmplitudeSimulation() {
        stopAmplitudeSimulation()
        amplitudeJob = scope.launch {
            var step = 0
            while (isActive && _isSpeaking.value) {
                step++
                // Generate realistic organic speech envelope: pauses between syllables, bursts when speaking
                val wave1 = Math.sin(step * 0.45).toFloat()
                val wave2 = Math.cos(step * 0.9).toFloat()
                val jitter = (Math.random().toFloat() * 0.3f)
                val rawAmp = ((wave1 * 0.5f + wave2 * 0.3f + 0.5f) + jitter).coerceIn(0.15f, 1.0f)

                // Periodic slight micro-pause simulation (natural breathing between words)
                val isMicroPause = (step % 18 == 0 || step % 29 == 0)
                _audioAmplitude.value = if (isMicroPause) 0.08f else rawAmp

                delay(50)
            }
            _audioAmplitude.value = 0f
        }
    }

    private fun stopAmplitudeSimulation() {
        amplitudeJob?.cancel()
        amplitudeJob = null
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _audioAmplitude.value = 0f
        stopAmplitudeSimulation()
    }

    /**
     * Synthesizes audio directly to a .wav file so the created talking video has persistent audio
     */
    fun synthesizeToFile(
        text: String,
        pitch: Float,
        rate: Float,
        outputFile: File,
        onDone: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isInitialized || tts == null) {
            onError("Le moteur vocal n'est pas encore prêt")
            return
        }

        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))

        val utteranceId = "synth_" + UUID.randomUUID().toString()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val listener = object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    mainHandler.post { onDone(outputFile) }
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    mainHandler.post { onError("Erreur lors de la synthèse audio du fichier") }
                }
            }
        }

        tts?.setOnUtteranceProgressListener(listener)
        val result = tts?.synthesizeToFile(text, params, outputFile, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            onError("Échec de la création du fichier audio")
        }
    }

    fun getAvailableVoices(): List<Voice> {
        return try {
            tts?.voices?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
