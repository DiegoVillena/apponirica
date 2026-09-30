package com.diegovillena.apponirica.ui.diario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegovillena.apponirica.data.db.DreamConPalabras
import com.diegovillena.apponirica.data.repo.RepositorioSuenos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FiltroPalabra(val stem: String, val display: String)

class DiarioViewModel(private val repositorio: RepositorioSuenos) : ViewModel() {

    private val suenosBase = repositorio.listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta.asStateFlow()

    private val _filtro = MutableStateFlow<FiltroPalabra?>(null)
    val filtro: StateFlow<FiltroPalabra?> = _filtro.asStateFlow()

    val suenos: StateFlow<List<DreamConPalabras>> = combine(
        suenosBase,
        _consulta,
        _filtro,
    ) { base, consulta, filtro ->
        when {
            filtro != null -> repositorio.suenosPorPalabra(filtro.stem)
            consulta.isNotBlank() -> repositorio.buscar(consulta)
            else -> base
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun buscar(consulta: String) {
        _consulta.value = consulta
        if (consulta.isNotBlank()) _filtro.value = null
    }

    fun filtrarPor(filtro: FiltroPalabra?) {
        _filtro.value = filtro
        if (filtro != null) _consulta.value = ""
    }
}