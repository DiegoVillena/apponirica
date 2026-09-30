package com.diegovillena.apponirica.core.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Graba la nota de voz en m4a (AAC). Todo fail-safe: si el micrófono está ocupado por el
 * recognizer u ocurre cualquier error, devuelve null y la app sigue solo con el texto.
 */
class GrabadoraAudio(private val contexto: Context) {

    private var grabador: MediaRecorder? = null
    private var ficheroActual: File? = null

    /** Pone en marcha la grabación. Devuelve el fichero destino o null si no pudo empezar. */
    fun iniciar(): File? {
        if (grabador != null) return ficheroActual
        val carpeta = File(contexto.getExternalFilesDir(null), "notas_voz").apply { mkdirs() }
        val fichero = File(carpeta, "nota_${System.currentTimeMillis()}.m4a")
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(contexto)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        return try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioSamplingRate(44_100)
            recorder.setAudioEncodingBitRate(96_000)
            recorder.setAudioChannels(1)
            recorder.setOutputFile(fichero.absolutePath)
            recorder.prepare()
            recorder.start()
            grabador = recorder
            ficheroActual = fichero
            fichero
        } catch (ex: Exception) {
            try {
                recorder.release()
            } catch (ignored: Exception) {
            }
            fichero.delete()
            null
        }
    }

    /** Cierra la grabación. Devuelve la ruta del fichero o null si quedó inservible. */
    fun detener(): String? {
        val recorder = grabador ?: return null
        grabador = null
        val fichero = ficheroActual
        ficheroActual = null
        return try {
            recorder.stop()
            fichero?.absolutePath
        } catch (ex: Exception) {
            fichero?.delete()
            null
        } finally {
            try {
                recorder.release()
            } catch (ignored: Exception) {
            }
        }
    }
}