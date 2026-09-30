package com.diegovillena.apponirica.transcription

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface EstadoTranscriptor {
    data object Inactivo : EstadoTranscriptor
    data object Escuchando : EstadoTranscriptor
    data object NoDisponible : EstadoTranscriptor
    data class Error(val mensaje: String, val codigo: Int = -1) : EstadoTranscriptor
}

/** Motores de transcripción disponibles para elegir en Ajustes. */
enum class MotorTranscripcion { OFFLINE, GOOGLE }

/** Contrato de transcripción de voz a texto: intercambiable entre motor local y el del sistema. */
interface Transcriptor {
    val estado: StateFlow<EstadoTranscriptor>
    val textoParcial: StateFlow<String>
    /** Volumen normalizado 0..1 para animar el botón de grabación. */
    val rms: StateFlow<Float>
    val textoFinal: SharedFlow<String>
    /** Fichero de audio de la última sesión de escucha (null si el motor no lo captura). */
    val ficheroAudio: StateFlow<String?>
    /** true si el motor corta sus sesiones por sí solo (Google) y hay que reencadenar escuchas. */
    val cadenaAuto: Boolean

    fun iniciar()

    fun detener()

    fun liberar()
}