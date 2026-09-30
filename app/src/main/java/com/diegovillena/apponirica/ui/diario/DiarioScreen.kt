package com.diegovillena.apponirica.ui.diario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.diegovillena.apponirica.data.db.DreamConPalabras
import com.diegovillena.apponirica.ui.common.EstadoVacio
import com.diegovillena.apponirica.ui.common.MarcaChip
import com.diegovillena.apponirica.ui.common.PalabraClaveChip
import com.diegovillena.apponirica.ui.common.formatearFechaSueno
import com.diegovillena.apponirica.ui.theme.Ambar
import com.diegovillena.apponirica.ui.theme.Coral

@Composable
fun DiarioScreen(
    vm: DiarioViewModel,
    filtroInicial: FiltroPalabra?,
    onAbrirSueno: (Long) -> Unit,
) {
    val suenos by vm.suenos.collectAsState()
    val consulta by vm.consulta.collectAsState()
    val filtro by vm.filtro.collectAsState()

    LaunchedEffect(filtroInicial) {
        if (filtroInicial != null) vm.filtrarPor(filtroInicial)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))
            Text(
                "AppOnírica",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tu diario de sueños",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            TextField(
                value = consulta,
                onValueChange = vm::buscar,
                placeholder = {
                    Text(
                        "Busca en tus sueños…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (consulta.isNotEmpty()) {
                        IconButton(onClick = { vm.buscar("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Quita la búsqueda")
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo-buscar"),
            )
            Spacer(Modifier.height(14.dp))
        }

        AnimatedVisibility(visible = filtro != null, enter = fadeIn(), exit = fadeOut()) {
            val filtroActual = filtro
            if (filtroActual != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                    ) {
                        Text(
                            "Filtrado por: «${filtroActual.display}»",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { vm.filtrarPor(null) }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Quita el filtro",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }

        if (suenos.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                EstadoVacio(
                    titulo = "Aún no hay sueños",
                    mensaje = "Toca «Grabar sueño» y cuéntalo con tu voz nada más despertar. Aquí guardaremos tu colección.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("lista-suenos"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 116.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(suenos, key = { it.sueno.id }) { entrada ->
                    TarjetaSueno(
                        entrada,
                        onAbrirSueno = { onAbrirSueno(entrada.sueno.id) },
                        onPulsarPalabra = { stem, display ->
                            vm.filtrarPor(FiltroPalabra(stem, display))
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaSueno(
    entrada: DreamConPalabras,
    onAbrirSueno: () -> Unit,
    onPulsarPalabra: (String, String) -> Unit,
) {
    val sueno = entrada.sueno
    Card(
        onClick = onAbrirSueno,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    sueno.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    formatearFechaSueno(sueno.dreamAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                sueno.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (sueno.esLucido || sueno.esPesadilla || entrada.palabras.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (sueno.esLucido) {
                        MarcaChip("Lúcido", Icons.Filled.AutoAwesome, Ambar, seleccionada = true)
                    }
                    if (sueno.esPesadilla) {
                        MarcaChip("Pesadilla", Icons.Filled.DarkMode, Coral, seleccionada = true)
                    }
                    entrada.palabras
                        .sortedByDescending { it.totalCount }
                        .take(3)
                        .forEach { palabra ->
                            PalabraClaveChip(palabra.display, stem = palabra.stem) {
                                onPulsarPalabra(palabra.stem, palabra.display)
                            }
                        }
                }
            }
        }
    }
}