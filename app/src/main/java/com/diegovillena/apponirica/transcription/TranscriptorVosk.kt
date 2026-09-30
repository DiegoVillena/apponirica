package com.diegovillena.apponirica.transcription

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.diegovillena.apponirica.core.audio.EscritorWav
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

/**
 * Transcripción 100% offline (Vosk con el modelo small-es en assets). Una sola fuente de
 * audio: el propio AudioRecord alimenta al reconocedor en streaming y, a la vez, escribe
 * el WAV adjunto — el relato y el audio del sueño siempre coinciden y nada sale del móvil.
 *
 * No usa StorageService.unpack: los modelos Vosk llevan un metadato "uuid" que es un
 * symlink y no se copia bien a assets desde Windows; el copiador propio evita ese tropiezo.
 */
class TranscriptorVosk(contexto: Context) : Transcriptor {

    private val appContext: Context = contexto.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** "Seguir escuchando" se marca ya en iniciar(): parar durante la carga debe tener efecto. */
    @Volatile
    private var sigoEscuchando = false

    /** Hay una sesión (carga de modelo + escucha + cierre) en vuelo: reiniciar es un no-op. */
    @Volatile
    private var sesionEnVuelo = false

    /** El reconocedor del hilo de escucha llega aquí apenas arranca, para poder pararlo desde otro hilo. */
    @Volatile
    private var record: AudioRecord? = null

    // El modelo se conserva entre sesiones (recargarlo costaría más de un segundo).
    private var modelo: Model? = null

    private val _estado = MutableStateFlow<EstadoTranscriptor>(EstadoTranscriptor.Inactivo)
    override val estado: StateFlow<EstadoTranscriptor> = _estado.asStateFlow()

    private val _textoParcial = MutableStateFlow("")
    override val textoParcial: StateFlow<String> = _textoParcial.asStateFlow()

    private val _rms = MutableStateFlow(0f)
    override val rms: StateFlow<Float> = _rms.asStateFlow()

    private val _textoFinal = MutableSharedFlow<String>(extraBufferCapacity = 8)
    override val textoFinal: SharedFlow<String> = _textoFinal.asSharedFlow()

    private val _ficheroAudio = MutableStateFlow<String?>(null)
    override val ficheroAudio: StateFlow<String?> = _ficheroAudio.asStateFlow()

    // Vosk reconoce de forma continua: no corta con las pausas y no necesita encadenar.
    override val cadenaAuto: Boolean = false

    override fun iniciar() {
        if (sesionEnVuelo) return
        sesionEnVuelo = true
        sigoEscuchando = true
        _textoParcial.value = ""
        _ficheroAudio.value = null
        scope.launch {
            var reconocedor: Recognizer? = null
            var escritor: EscritorWav? = null
            var recordSesion: AudioRecord? = null
            try {
                val modeloCargado = modelo ?: cargarModelo().also { modelo = it }
                if (!sigoEscuchando) return@launch
                // El reconocedor nace y muere con la sesión: parar/liberar desde otro hilo no
                // compite con el hilo de escucha por un objeto que otro cierre está usando.
                val rec = Recognizer(modeloCargado, 16_000f).also { it.setWords(false) }
                reconocedor = rec
                recordSesion = crearAudioRecord()
                record = recordSesion
                _estado.value = EstadoTranscriptor.Escuchando

                val carpeta = File(appContext.getExternalFilesDir(null), "notas_voz").apply { mkdirs() }
                escritor = EscritorWav(File(carpeta, "nota_${System.currentTimeMillis()}.wav")).also {
                    it.abrir()
                }
                recordSesion.startRecording()

                val buffer = ShortArray(TAMANO_BUFFER)
                while (sigoEscuchando && recordSesion.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val leidos = recordSesion.read(buffer, 0, buffer.size)
                    if (leidos <= 0) continue
                    emitirRms(buffer, leidos)
                    escritor.anexarMuestras(buffer, leidos)
                    if (rec.acceptWaveForm(buffer, leidos)) {
                        val frase = textoDe(rec.getResult(), "text")
                        if (frase.isNotBlank()) {
                            _textoParcial.value = ""
                            _textoFinal.tryEmit(frase)
                        }
                    } else {
                        _textoParcial.value = textoDe(rec.getPartialResult(), "partial")
                    }
                }
            } catch (ex: Exception) {
                Log.d(TAG, "escucha interrumpida", ex)
                _estado.value = EstadoTranscriptor.Error(
                    if (ex is IllegalStateException) "El micrófono ya está ocupado por otra app"
                    else "Fallo de la transcripción local: ${ex.message ?: "desconocido"}",
                )
            } finally {
                cerrarSesion(reconocedor, escritor, recordSesion)
            }
        }
    }

    override fun detener() {
        sigoEscuchando = false
        record?.let { actual -> runCatching { actual.stop() } }
    }

    override fun liberar() {
        sigoEscuchando = false
        record?.let { actual ->
            runCatching { actual.stop() }
            runCatching { actual.release() }
        }
        record = null
        _rms.value = 0f
        _textoParcial.value = ""
        _estado.value = EstadoTranscriptor.Inactivo
    }

    /** Copia los assets del modelo a filesDir una vez y lo carga. */
    private fun cargarModelo(): Model {
        val destino = File(appContext.filesDir, "model-es-small")
        if (!File(destino, "conf").exists()) {
            copiarCarpetaAssets("model-es-small", destino)
        }
        return Model(destino.absolutePath)
    }

    private fun copiarCarpetaAssets(ruta: String, destino: File) {
        destino.mkdirs()
        val nombres: List<String> = appContext.assets.list(ruta)?.toList() ?: emptyList()
        for (nombre in nombres) {
            val rutaHija = "$ruta/$nombre"
            val ficheroHijo = File(destino, nombre)
            val hijos: List<String> = appContext.assets.list(rutaHija)?.toList() ?: emptyList()
            if (hijos.isEmpty()) {
                appContext.assets.open(rutaHija).use { entrada ->
                    ficheroHijo.outputStream().use { salida -> entrada.copyTo(salida) }
                }
            } else {
                copiarCarpetaAssets(rutaHija, ficheroHijo)
            }
        }
    }

    /** Cierre de la sesión en el orden seguro: WAV → audio → resultado pendiente → recognizer.
     *  La sesión libera lo suyo; un liberar() externo nunca cierra lo que el hilo aún usa.
     *  Al terminar despeja sesionEnVuelo: el siguiente toque al micrófono ya puede arrancar. */
    private fun cerrarSesion(reconocedor: Recognizer?, escritor: EscritorWav?, recordSesion: AudioRecord?) {
        sigoEscuchando = false
        record = null
        val rutaAdjunta = try { escritor?.cerrar() } catch (ignored: Exception) { null }
        recordSesion?.let { actual ->
            runCatching { if (actual.recordingState == AudioRecord.RECORDSTATE_RECORDING) actual.stop() }
            runCatching { actual.release() }
        }
        _ficheroAudio.value = rutaAdjunta
        // Una frase a medias al pulsar "parar" cierra como resultado final:
        try {
            if (reconocedor != null) {
                val pendiente = textoDe(reconocedor.getFinalResult(), "text")
                if (pendiente.isNotBlank()) {
                    _textoParcial.value = ""
                    _textoFinal.tryEmit(pendiente)
                }
            }
        } catch (ignored: Exception) {
        }
        runCatching { reconocedor?.close() }
        _rms.value = 0f
        _estado.value = EstadoTranscriptor.Inactivo
        sesionEnVuelo = false
    }

    private fun crearAudioRecord(): AudioRecord {
        val minimo = AudioRecord.getMinBufferSize(
            16_000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        // VOICE_RECOGNITION es el fuente preferido para ASR, pero alguna ROM/emulador no lo
        // implementa: en ese caso probamos MIC antes de rendirnos.
        for (source in intArrayOf(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.MIC,
        )) {
            val record = AudioRecord(
                source,
                16_000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minimo.coerceAtLeast(4_096) * 4,
            )
            if (record.state == AudioRecord.STATE_INITIALIZED) return record
            runCatching { record.release() }
        }
        throw IllegalStateException("El micrófono no está disponible")
    }

    private fun emitirRms(buffer: ShortArray, leidos: Int) {
        var suma = 0.0
        for (n in 0 until leidos) {
            val muestra = buffer[n]
            suma += muestra.toDouble() * muestra
        }
        val rms = kotlin.math.sqrt(suma / leidos) / 32_768.0
        _rms.value = (rms * 12.0).toFloat().coerceIn(0f, 1f)
    }

    private fun textoDe(json: String, campo: String): String =
        runCatching { JSONObject(json).optString(campo) }.getOrDefault("")

    private companion object {
        const val TAMANO_BUFFER = 2_048 // ~128 ms de audio a 16 kHz
        const val TAG = "TranscriptorVosk"
    }
}