package com.diegovillena.apponirica.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegovillena.apponirica.data.db.DreamConPalabras
import com.diegovillena.apponirica.data.repo.RepositorioSuenos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val repositorio: RepositorioSuenos,
    private val suenoId: Long,
) : ViewModel() {

    private val _sueno = MutableStateFlow<DreamConPalabras?>(null)
    val sueno: StateFlow<DreamConPalabras?> = _sueno.asStateFlow()

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

    init {
        viewModelScope.launch {
            repositorio.obtenerPorId(suenoId)?.let { entrada ->
                _sueno.value = entrada
                _texto.value = entrada.sueno.texto
                _titulo.value = entrada.sueno.titulo
                _esLucido.value = entrada.sueno.esLucido
                _esPesadilla.value = entrada.sueno.esPesadilla
                _mood.value = entrada.sueno.mood
                _claridad.value = entrada.sueno.claridad
            }
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

    suspend fun guardarCambios(): Boolean {
        val entrada = _sueno.value ?: return false
        repositorio.actualizarSueno(
            entrada.sueno.copy(
                titulo = _titulo.value.ifBlank { "Sueño sin título" },
                texto = _texto.value,
                esLucido = _esLucido.value,
                esPesadilla = _esPesadilla.value,
                mood = _mood.value,
                claridad = _claridad.value,
            ),
        )
        return true
    }

    suspend fun borrar() {
        repositorio.eliminarSueno(suenoId)
    }
}