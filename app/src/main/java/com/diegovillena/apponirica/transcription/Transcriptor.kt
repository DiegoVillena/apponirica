package com.diegovillena.apponirica.transcription

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface EstadoTranscriptor {
    data object Inactivo : EstadoTranscriptor
    data object Escuchando : EstadoTranscriptor
    data object NoDisponible : EstadoTranscriptor
    data class Error(val mensaje: String, val codigo: Int = -1) : EstadoTranscriptor
}

/** Contrato de transcripción de voz a texto: intercambiable entre el recognizer del sistema y motores offline. */
interface Transcriptor {
    val estado: StateFlow<EstadoTranscriptor>
    val textoParcial: StateFlow<String>
    /** Volumen normalizado 0..1 para animar el botón de grabación. */
    val rms: StateFlow<Float>
    val textoFinal: SharedFlow<String>

    fun iniciar()

    fun detener()

    fun liberar()
}