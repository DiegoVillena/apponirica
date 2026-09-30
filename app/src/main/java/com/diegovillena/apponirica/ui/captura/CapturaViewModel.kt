package com.diegovillena.apponirica.ui.captura

import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegovillena.apponirica.data.db.Dream
import com.diegovillena.apponirica.data.repo.RepositorioSuenos
import com.diegovillena.apponirica.transcription.EstadoTranscriptor
import com.diegovillena.apponirica.transcription.Transcriptor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class CampoActivo { RELATO, TITULO }

class CapturaViewModel(
    private val repositorio: RepositorioSuenos,
    private val transcriptor: Transcriptor,
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

    /** Escucha continua: true mientras el usuario no pulse "parar"; los segmentos se encadenan. */
    private val _escuchaContinua = MutableStateFlow(false)
    val escuchaActiva: StateFlow<Boolean> = _escuchaContinua.asStateFlow()

    /** Campo que tiene el foco en pantalla: es el destino de la transcripción por voz. */
    private val _campoActivo = MutableStateFlow(CampoActivo.RELATO)
    val campoActivo: StateFlow<CampoActivo> = _campoActivo.asStateFlow()

    /** Banner de avisos transitorio; lo calcula el collect de estado, la pantalla solo pinta. */
    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private var trabajoAutoParada: Job? = null
    private var paradaManual = false
    private var vozActiva = false
    private var huboVoz = false

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
                if (estabaVoz && !vozActiva && transcriptor.cadenaAuto && _escuchaContinua.value) {
                    encadenarEscucha(estado)
                }
                _aviso.value = when {
                    paradaManual -> {
                        paradaManual = false
                        null
                    }
                    estado is EstadoTranscriptor.Error &&
                        (!transcriptor.cadenaAuto || !_escuchaContinua.value) -> estado.mensaje
                    estado is EstadoTranscriptor.NoDisponible ->
                        "Este dispositivo no tiene reconocimiento de Google: escribe tu sueño a mano o elige el motor local en Ajustes."
                    else -> null
                }
            }
        }
    }

    fun fijarCampoActivo(campo: CampoActivo) {
        _campoActivo.value = campo
    }

    /** El recognizer del sistema corta la escucha en cada pausa: mientras no se pulse "parar",
     *  reencadenamos escuchas y el texto acumula frase a frase. Solo el motor de Google lo necesita. */
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
        if (vozActiva || _escuchaContinua.value) {
            // Parada manual: la clase de transcripción cierra y devuelve su fichero de audio.
            _escuchaContinua.value = false
            paradaManual = true
            trabajoAutoParada?.cancel()
            transcriptor.detener()
            return
        }
        paradaManual = false
        _aviso.value = null
        _escuchaContinua.value = true
        transcriptor.iniciar()
        trabajoAutoParada = viewModelScope.launch {
            // Ahorro de batería: una escucha olvidada se corta sola a los 2 minutos.
            delay(120_000)
            if (_escuchaContinua.value) grabar()
        }
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

    suspend fun guardar(): Long {
        // El audio adjunto lo captura solo el motor local: con Google nunca hay fichero (esperar
        // sería en balde), y un sueño escrito a mano no debe heredar el WAV del sueño anterior.
        val rutaAdjunta = if (huboVoz && !transcriptor.cadenaAuto) {
            // El fichero se cierra al detener la escucha: espera su ruta (≤2 s).
            withTimeoutOrNull(2_000) {
                transcriptor.ficheroAudio.first { fichero -> fichero != null }
            }
        } else {
            null
        }
        // Al cerrar la sesión puede emitirse una frase pendiente a medias: deja al collector
        // procesarla antes de leer el relato definitivo.
        delay(200)
        return repositorio.guardarSueno(
            texto = _texto.value,
            titulo = _titulo.value,
            lucido = _esLucido.value,
            pesadilla = _esPesadilla.value,
            mood = _mood.value,
            claridad = _claridad.value,
            audioPath = rutaAdjunta,
            origen = if (huboVoz) Dream.ORIGEN_VOZ else Dream.ORIGEN_TECLADO,
        )
    }

    override fun onCleared() {
        trabajoAutoParada?.cancel()
        transcriptor.liberar()
        super.onCleared()
    }
}