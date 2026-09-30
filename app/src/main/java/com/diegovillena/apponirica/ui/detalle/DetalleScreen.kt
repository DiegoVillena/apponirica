package com.diegovillena.apponirica.ui.detalle

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.diegovillena.apponirica.ui.common.MarcaChip
import com.diegovillena.apponirica.ui.common.SelectorMarcado
import com.diegovillena.apponirica.ui.common.formatearFechaSueno
import com.diegovillena.apponirica.ui.theme.Ambar
import com.diegovillena.apponirica.ui.theme.Coral
import kotlinx.coroutines.launch

@Composable
fun DetalleScreen(vm: DetalleViewModel, onVolver: () -> Unit) {
    val sueno by vm.sueno.collectAsState()
    val texto by vm.texto.collectAsState()
    val titulo by vm.titulo.collectAsState()
    val lucido by vm.esLucido.collectAsState()
    val pesadilla by vm.esPesadilla.collectAsState()
    val mood by vm.mood.collectAsState()
    val claridad by vm.claridad.collectAsState()
    val scope = rememberCoroutineScope()
    var pedirConfirmacion by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver, modifier = Modifier.testTag("btn-volver")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.width(8.dp))
            Text("Detalle", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            Text(
                sueno?.let { formatearFechaSueno(it.sueno.dreamAt) } ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(18.dp))

        sueno?.sueno?.audioPath?.let { ruta ->
            ReproductorAudio(ruta)
            Spacer(Modifier.height(14.dp))
        }

        TextField(
            value = titulo,
            onValueChange = vm::actualizarTitulo,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("campo-titulo"),
        )
        Spacer(Modifier.height(12.dp))

        TextField(
            value = texto,
            onValueChange = vm::actualizarTexto,
            placeholder = {
                Text(
                    "Cuenta el sueño…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            minLines = 6,
            modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp).testTag("campo-texto"),
        )
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MarcaChip("Lúcido", Icons.Filled.AutoAwesome, Ambar, lucido, onAlternar = vm::alternarLucido)
            MarcaChip("Pesadilla", Icons.Filled.DarkMode, Coral, pesadilla, onAlternar = vm::alternarPesadilla)
        }
        Spacer(Modifier.height(16.dp))
        SelectorMarcado("Ánimo (1–5)", mood, vm::marcarMood)
        Spacer(Modifier.height(14.dp))
        SelectorMarcado("Claridad (1–5)", claridad, vm::marcarClaridad)
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { scope.launch { vm.guardarCambios(); onVolver() } },
            enabled = texto.isNotBlank(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("btn-guardar"),
        ) {
            Text("Guardar cambios", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { pedirConfirmacion = true },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn-borrar"),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.height(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Eliminar sueño", style = MaterialTheme.typography.titleSmall)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (pedirConfirmacion) {
        AlertDialog(
            onDismissRequest = { pedirConfirmacion = false },
            title = { Text("¿Eliminar sueño?") },
            text = {
                Text("Se borra el texto y el audio de este sueño. Las palabras clave se recalculan al momento.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pedirConfirmacion = false
                        scope.launch { vm.borrar(); onVolver() }
                    },
                ) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pedirConfirmacion = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun ReproductorAudio(ruta: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            ReproductorBoton(ruta)
            Text("Nota de voz guardada", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReproductorBoton(ruta: String) {
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    val icono = if (player != null) Icons.Filled.Pause else Icons.Filled.PlayArrow
    DisposableEffect(ruta) {
        onDispose {
            player?.let { actual ->
                runCatching { actual.stop() }
                runCatching { actual.release() }
            }
        }
    }
    IconButton(
        onClick = {
            val actual = player
            if (actual != null) {
                runCatching { actual.stop() }
                runCatching { actual.release() }
                player = null
            } else {
                runCatching {
                    val nuevo = MediaPlayer()
                    nuevo.setDataSource(ruta)
                    nuevo.setOnCompletionListener { enUso ->
                        runCatching { enUso.release() }
                        player = null
                    }
                    nuevo.prepare()
                    nuevo.start()
                    player = nuevo
                }
            }
        },
    ) {
        Icon(icono, contentDescription = "Reproducir la nota de voz", tint = MaterialTheme.colorScheme.primary)
    }
}