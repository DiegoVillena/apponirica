package com.diegovillena.apponirica.ui.diario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegovillena.apponirica.data.db.DreamConPalabras
import com.diegovillena.apponirica.data.repo.RepositorioSuenos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class FiltroPalabra(val stem: String, val display: String)

@OptIn(ExperimentalCoroutinesApi::class)
class DiarioViewModel(private val repositorio: RepositorioSuenos) : ViewModel() {

    private val suenosBase = repositorio.listar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta.asStateFlow()

    private val _filtro = MutableStateFlow<FiltroPalabra?>(null)
    val filtro: StateFlow<FiltroPalabra?> = _filtro.asStateFlow()

    /** La lista es el flujo de la base SALVO cuando hay filtro o búsqueda activos. Las queries
     *  de filtro/búsqueda viven dentro del flatMapLatest (una por emisión) para no suspender
     *  en el transform del combine: un transform suspendido por una query antigua tapaba las
     *  emisiones nuevas y dejaba la lista desactualizada tras guardar o borrar. */
    val suenos: StateFlow<List<DreamConPalabras>> =
        combine(suenosBase, _consulta, _filtro) { base, consulta, filtro ->
            when {
                filtro != null -> Fuentes.Filtro(filtro)
                consulta.isNotBlank() -> Fuentes.Busqueda(consulta)
                else -> Fuentes.Base(base)
            }
        }.flatMapLatest { fuente ->
            when (fuente) {
                is Fuentes.Filtro -> flow { emit(repositorio.suenosPorPalabra(fuente.filtro.stem)) }
                is Fuentes.Busqueda -> flow { emit(repositorio.buscar(fuente.consulta)) }
                is Fuentes.Base -> flowOf(fuente.suenos)
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

    private sealed interface Fuentes {
        data class Filtro(val filtro: FiltroPalabra) : Fuentes
        data class Busqueda(val consulta: String) : Fuentes
        data class Base(val suenos: List<DreamConPalabras>) : Fuentes
    }
}