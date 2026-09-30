package com.diegovillena.apponirica.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diegovillena.apponirica.data.repo.Estadisticas
import com.diegovillena.apponirica.data.repo.RepositorioSuenos
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class EstadisticasViewModel(repositorio: RepositorioSuenos) : ViewModel() {
    val estadisticas: StateFlow<Estadisticas> = repositorio.estadisticas
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Estadisticas(0, 0, emptyMap(), emptyList()),
        )
}