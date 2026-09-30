package com.diegovillena.apponirica.transcription

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Transcripción con el recognizer del sistema. La API exige llamadas desde el hilo
 * principal: toda operación se postea al Looper principal internamente.
 */
class TranscriptorSistema(contexto: Context) : Transcriptor {

    private val appContext: Context = contexto.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private var recognizer: SpeechRecognizer? = null
    private var volumenBeep = -1

    /** Silencia el "beep" que el servicio reproduce al arrancar cada segmento de escucha. */
    private fun silenciarBeep() {
        try {
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (volumenBeep == -1) volumenBeep = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        } catch (ignored: Exception) {
        }
    }

    private fun restaurarBeep() {
        if (volumenBeep == -1) return
        try {
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volumenBeep, 0)
        } catch (ignored: Exception) {
        }
        volumenBeep = -1
    }

    private val _estado = MutableStateFlow<EstadoTranscriptor>(EstadoTranscriptor.Inactivo)
    override val estado: StateFlow<EstadoTranscriptor> = _estado.asStateFlow()

    private val _textoParcial = MutableStateFlow("")
    override val textoParcial: StateFlow<String> = _textoParcial.asStateFlow()

    private val _rms = MutableStateFlow(0f)
    override val rms: StateFlow<Float> = _rms.asStateFlow()

    private val _textoFinal = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val textoFinal: SharedFlow<String> = _textoFinal.asSharedFlow()

    override val ficheroAudio: StateFlow<String?> = MutableStateFlow<String?>(null)

    override val cadenaAuto: Boolean = true

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _rms.value = 0f
            restaurarBeep()
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {
            _rms.value = rmsdB.coerceIn(0f, 10f) / 10f
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            restaurarBeep()
            _estado.value = EstadoTranscriptor.Error(mensajeDe(error), error)
        }

        override fun onResults(resultados: Bundle?) {
            restaurarBeep()
            _estado.value = EstadoTranscriptor.Inactivo
            val texto = resultados
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()?.trim().orEmpty()
            if (texto.isNotEmpty()) {
                _textoParcial.value = ""
                _textoFinal.tryEmit(texto)
            }
        }

        override fun onPartialResults(resultadosParciales: Bundle?) {
            val parcial = resultadosParciales
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.lastOrNull().orEmpty()
            if (parcial.isNotBlank()) _textoParcial.value = parcial
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    override fun iniciar() {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                _estado.value = EstadoTranscriptor.NoDisponible
                return@post
            }
            _textoParcial.value = ""
            silenciarBeep()
            _estado.value = EstadoTranscriptor.Escuchando
            val rec = recognizer ?: SpeechRecognizer.createSpeechRecognizer(appContext).also {
                it.setRecognitionListener(listener)
                recognizer = it
            }
            rec.startListening(intent())
        }
    }

    override fun detener() {
        mainHandler.post { recognizer?.stopListening() }
    }

    override fun liberar() {
        mainHandler.post {
            recognizer?.destroy()
            recognizer = null
            _estado.value = EstadoTranscriptor.Inactivo
            _textoParcial.value = ""
            _rms.value = 0f
        }
    }

    private fun intent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private fun mensajeDe(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Problema de audio durante la grabación"
        SpeechRecognizer.ERROR_CLIENT -> "Error del cliente de reconocimiento"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Falta el permiso de micrófono"
        SpeechRecognizer.ERROR_NETWORK -> "Sin conexión al servicio de reconocimiento"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "El servicio de reconocimiento ha tardado demasiado"
        SpeechRecognizer.ERROR_NO_MATCH -> "No se ha entendido la nota de voz"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El recognizer está ocupado"
        SpeechRecognizer.ERROR_SERVER -> "Error del servidor de reconocimiento"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se ha detectado ninguna voz"
        else -> "Error de reconocimiento ($error)"
    }
}