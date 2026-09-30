package com.diegovillena.apponirica.core.audio

import java.io.File
import java.io.RandomAccessFile

/**
 * Escribe un fichero WAV (PCM 16-bit mono, 16 kHz): deja la cabecera como marcador desde la
 * apertura y la reescribe con el tamaño real al cerrar (estándar para streams en vivo).
 */
class EscritorWav(private val fichero: File) {

    private var acceso: RandomAccessFile? = null
    private var totalMuestras = 0L

    fun abrir() {
        acceso = RandomAccessFile(fichero, "rw").apply {
            setLength(0)
            write(cabeceraWav(bytesPCM = 0L))
        }
        totalMuestras = 0
    }

    fun anexarMuestras(buffer: ShortArray, leidos: Int) {
        val ficheroActual = acceso ?: return
        val bytes = ByteArray(leidos * 2)
        var indice = 0
        for (n in 0 until leidos) {
            val muestra = buffer[n].toInt()
            bytes[indice++] = (muestra and 0xFF).toByte()
            bytes[indice++] = ((muestra shr 8) and 0xFF).toByte()
        }
        ficheroActual.write(bytes)
        totalMuestras += bytes.size
    }

    /** Cierra el fichero. Devuelve su ruta o null si no hay nada que guardar. */
    fun cerrar(): String? {
        val ficheroActual = acceso
        acceso = null
        if (ficheroActual == null) return null
        return try {
            ficheroActual.seek(0)
            ficheroActual.write(cabeceraWav(totalMuestras))
            ficheroActual.close()
            if (totalMuestras > 0L) fichero.absolutePath else { fichero.delete(); null }
        } catch (ex: Exception) {
            runCatching { ficheroActual.close() }
            fichero.delete()
            null
        }
    }

    private fun cabeceraWav(bytesPCM: Long): ByteArray {
        val cabecera = ByteArray(44)
        fun ponTexto(desde: Int, texto: String) =
            texto.forEachIndexed { n, letra -> cabecera[desde + n] = letra.code.toByte() }
        fun pon32(desde: Int, valor: Int) {
            cabecera[desde] = (valor and 0xFF).toByte()
            cabecera[desde + 1] = ((valor shr 8) and 0xFF).toByte()
            cabecera[desde + 2] = ((valor shr 16) and 0xFF).toByte()
            cabecera[desde + 3] = ((valor shr 24) and 0xFF).toByte()
        }
        fun pon16(desde: Int, valor: Int) {
            cabecera[desde] = (valor and 0xFF).toByte()
            cabecera[desde + 1] = ((valor shr 8) and 0xFF).toByte()
        }
        ponTexto(0, "RIFF")
        pon32(4, (bytesPCM + 36).toInt())
        ponTexto(8, "WAVE")
        ponTexto(12, "fmt ")
        pon32(16, 16) // blockSize
        pon16(20, 1) // PCM
        pon16(22, 1) // MONO
        pon32(24, 16_000) // sample rate
        pon32(28, 32_000) // bytes/seg = 16000 × 2
        pon16(32, 2) // tamaño de bloque por muestra
        pon16(34, 16) // bits por muestra
        ponTexto(36, "data")
        pon32(40, bytesPCM.toInt())
        return cabecera
    }
}