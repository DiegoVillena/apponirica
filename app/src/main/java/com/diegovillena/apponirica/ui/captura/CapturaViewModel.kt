package com.diegovillena.apponirica.ui.captura

import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegovillena.apponirica.core.audio.GrabadoraAudio
import com.diegovillena.apponirica.data.db.Dream
import com.diegovillena.apponirica.data.repo.RepositorioSuenos
import com.diegovillena.apponirica.transcription.EstadoTranscriptor
import com.diegovillena.apponirica.transcription.Transcriptor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CampoActivo { RELATO, TITULO }

class CapturaViewModel(
    private val repositorio: RepositorioSuenos,
    private val transcriptor: Transcriptor,
    private val grabadora: GrabadoraAudio,
) : ViewModel() {

    val estadoTranscriptor: StateFlow<EstadoTranscriptor> = transcriptor.estado
    val textoParcial: StateFlow<String> = transcriptor.textoParcial
    val rms: StateFlow<Float> = transcriptor.rms

    private val _texto = MutableStateFlow("")
    val texto: StateFlow<String> = _texto.asStateFlow()

    private val _titulo = MutableStateFlow("")
    val titulo: StateFlow<String> = _titulo.asStateFlow()

    private val _esLucido = MutableStateFlow(false)
    val esLucido: StateFlow<Boolean> = _esLucido.asStateFlow()

    private val _esPesadilla = MutableStateFlow(false)
    val esPesadilla: StateFlow<Boolean> = _esPesadilla.asStateFlow()

    private val _mood = MutableStateFlow<Int?>(null)
    val mood: StateFlow<Int?> = _mood.asStateFlow()

    private val _claridad = MutableStateFlow<Int?>(null)
    val claridad: StateFlow<Int?> = _claridad.asStateFlow()

    private val _grabandoAudio = MutableStateFlow(false)
    val grabandoAudio: StateFlow<Boolean> = _grabandoAudio.asStateFlow()

    /** Escucha continua: true mientras el usuario no pulse "parar"; los segmentos se encadenan. */
    private val _escuchaContinua = MutableStateFlow(false)
    val escuchaActiva: StateFlow<Boolean> = _escuchaContinua.asStateFlow()

    /** Campo que tiene el foco en pantalla: es el destino de la transcripción por voz. */
    private val _campoActivo = MutableStateFlow(CampoActivo.RELATO)
    val campoActivo: StateFlow<CampoActivo> = _campoActivo.asStateFlow()

    /** Banner de avisos transitorio; lo calcula el collect de estado, la pantalla solo pinta. */
    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private var rutaAudio: String? = null
    private var vozActiva = false
    private var huboVoz = false
    private var paradaManual = false

    init {
        viewModelScope.launch {
            transcriptor.textoFinal.collect { final ->
                huboVoz = true
                if (_campoActivo.value == CampoActivo.TITULO) {
                    _titulo.value = (_titulo.value + " " + final).trim()
                } else {
                    _texto.value = (_texto.value + " " + final).trim()
                }
            }
        }
        viewModelScope.launch {
            transcriptor.estado.collect { estado ->
                val estabaVoz = vozActiva
                vozActiva = estado is EstadoTranscriptor.Escuchando
                when {
                    // Sin recognizer (ROM sin Google): el micrófono queda para la nota de audio pura.
                    estado is EstadoTranscriptor.NoDisponible && !_grabandoAudio.value -> {
                        _grabandoAudio.value = grabadora.iniciar() != null
                    }
                    estabaVoz && !vozActiva -> {
                        // El segmento terminó por sí mismo (pausa del usuario o corte del servicio).
                        if (_grabandoAudio.value) {
                            rutaAudio = grabadora.detener()
                            _grabandoAudio.value = false
                        }
                        if (_escuchaContinua.value) encadenarEscucha(estado)
                    }
                }
                _aviso.value = when {
                    paradaManual -> {
                        paradaManual = false
                        null
                    }
                    estado is EstadoTranscriptor.Error && !_escuchaContinua.value -> estado.mensaje
                    estado is EstadoTranscriptor.NoDisponible ->
                        "Este dispositivo no tiene reconocimiento de voz: escribe tu sueño a mano y la nota de audio se graba igual."
                    else -> null
                }
            }
        }
    }

    fun fijarCampoActivo(campo: CampoActivo) {
        _campoActivo.value = campo
    }

    /** El recognizer del sistema corta la escucha en cada pausa: mientras no se pulse "parar",
     *  relanzamos la escucha y el texto acumula frase a frase. */
    private fun encadenarEscucha(estado: EstadoTranscriptor) {
        val error = estado as? EstadoTranscriptor.Error ?: return transcriptor.iniciar()
        when (error.codigo) {
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
            -> viewModelScope.launch {
                delay(350)
                if (_escuchaContinua.value && !vozActiva) transcriptor.iniciar()
            }
            else -> _escuchaContinua.value = false // error duro (red, permiso, servidor): parar
        }
    }

    fun grabar() {
        if (vozActiva || _grabandoAudio.value) {
            // Parada manual: cierra la nota de audio y pide el resultado al recognizer. El aviso
            // transitorio que dispare el servicio se suprime (no es un fallo del que hacer eco).
            _escuchaContinua.value = false
            paradaManual = true
            rutaAudio = grabadora.detener()
            _grabandoAudio.value = false
            transcriptor.detener()
            return
        }
        // El recognizer va PRIMERO: el micrófono es un recurso que la mayoría de móviles
        // (MIUI incluido) no comparte, y la prioridad del MVP es el texto transcrito. Si
        // la grabadora no consigue el mic, start() falla a lo suyo y se sigue sin audio.
        paradaManual = false
        _aviso.value = null
        _escuchaContinua.value = true
        transcriptor.iniciar()
    }

    fun actualizarTexto(valor: String) {
        _texto.value = valor
    }

    fun actualizarTitulo(valor: String) {
        _titulo.value = valor
    }

    fun alternarLucido() {
        _esLucido.value = !_esLucido.value
    }

    fun alternarPesadilla() {
        _esPesadilla.value = !_esPesadilla.value
    }

    fun marcarMood(valor: Int) {
        _mood.value = if (_mood.value == valor) null else valor
    }

    fun marcarClaridad(valor: Int) {
        _claridad.value = if (_claridad.value == valor) null else valor
    }

    suspend fun guardar(): Long = repositorio.guardarSueno(
        texto = _texto.value,
        titulo = _titulo.value,
        lucido = _esLucido.value,
        pesadilla = _esPesadilla.value,
        mood = _mood.value,
        claridad = _claridad.value,
        audioPath = rutaAudio,
        origen = if (huboVoz) Dream.ORIGEN_VOZ else Dream.ORIGEN_TECLADO,
    )

    override fun onCleared() {
        transcriptor.liberar()
        super.onCleared()
    }
}