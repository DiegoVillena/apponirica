package com.diegovillena.apponirica.ui.captura

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.diegovillena.apponirica.transcription.EstadoTranscriptor
import com.diegovillena.apponirica.ui.common.MarcaChip
import com.diegovillena.apponirica.ui.common.SelectorMarcado
import com.diegovillena.apponirica.ui.theme.Ambar
import com.diegovillena.apponirica.ui.theme.Coral
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun CapturaScreen(vm: CapturaViewModel, onVolver: () -> Unit) {
    val estado by vm.estadoTranscriptor.collectAsState()
    val escuchaActiva by vm.escuchaActiva.collectAsState()
    val campoActivo by vm.campoActivo.collectAsState()
    val texto by vm.texto.collectAsState()
    val titulo by vm.titulo.collectAsState()
    val lucido by vm.esLucido.collectAsState()
    val pesadilla by vm.esPesadilla.collectAsState()
    val mood by vm.mood.collectAsState()
    val claridad by vm.claridad.collectAsState()
    val parcial by vm.textoParcial.collectAsState()
    val volumen by vm.rms.collectAsState()
    val grabando = escuchaActiva || estado is EstadoTranscriptor.Escuchando
    val scope = rememberCoroutineScope()

    val contexto = LocalContext.current
    val conPermiso = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permisoDenegado by remember { mutableStateOf(false) }
    val lanzarPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        conPermiso.value = concedido
        if (concedido) {
            vm.grabar()
        } else {
            permisoDenegado = true
        }
    }

    var segundos by remember { mutableIntStateOf(0) }
    LaunchedEffect(grabando) {
        if (grabando) {
            while (isActive) {
                delay(1_000)
                segundos += 1
            }
        } else {
            segundos = 0
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver, modifier = Modifier.testTag("btn-volver")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.width(8.dp))
            Text("Sueño nuevo", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(20.dp))

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val transicion = rememberInfiniteTransition(label = "pulso")
            val pulso by transicion.animateFloat(
                initialValue = 1f,
                targetValue = 1.07f,
                animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                label = "pulso",
            )
            val escala = if (grabando) pulso * (1f + volumen * 0.18f) else 1f
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer { scaleX = escala; scaleY = escala }
                    .background(
                        brush = Brush.linearGradient(
                            if (grabando) {
                                listOf(Coral, Color(0xFFE0657B))
                            } else {
                                listOf(MaterialTheme.colorScheme.primary, Color(0xFF7C5CBF))
                            },
                        ),
                        shape = CircleShape,
                    )
                    .clip(CircleShape)
                    .clickable {
                        if (conPermiso.value) {
                            vm.grabar()
                        } else {
                            lanzarPermiso.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                    .testTag("btn-grabar"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (grabando) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = if (grabando) "Parar la grabación" else "Grabar un sueño",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (grabando) String.format("%d:%02d", segundos / 60, segundos % 60)
                else "Toca para grabar tu sueño",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(18.dp))

        // El relato crece con el sueño: el formulario scrollea y el botón de guardar queda
        // FIJO abajo — con un texto largo no se pierde de la pantalla.
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
        ) {
            val avisoVm by vm.aviso.collectAsState()
            val aviso: String? = avisoVm
                ?: if (permisoDenegado) "Sin permiso de micrófono: escribe tu sueño a mano." else null
            if (aviso != null) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        aviso,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            TextField(
                value = if (escuchaActiva && parcial.isNotBlank()) {
                    (texto + " " + parcial).trim()
                } else {
                    texto
                },
                onValueChange = { if (!grabando) vm.actualizarTexto(it) },
                placeholder = {
                    Text(
                        "Habla tu sueño…\no escribe a mano",
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
                minLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 190.dp)
                    .testTag("campo-texto")
                    .onFocusChanged { if (it.isFocused) vm.fijarCampoActivo(CampoActivo.RELATO) },
            )
            Spacer(Modifier.height(14.dp))

            TextField(
                value = if (campoActivo == CampoActivo.TITULO && escuchaActiva && parcial.isNotBlank()) {
                    (titulo + " " + parcial).trim()
                } else {
                    titulo
                },
                onValueChange = { if (!grabando) vm.actualizarTitulo(it) },
                placeholder = {
                    Text(
                        "Título (opcional)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo-titulo")
                    .onFocusChanged { if (it.isFocused) vm.fijarCampoActivo(CampoActivo.TITULO) },
            )
            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MarcaChip("Lúcido", Icons.Filled.AutoAwesome, Ambar, lucido, onAlternar = vm::alternarLucido)
                MarcaChip("Pesadilla", Icons.Filled.DarkMode, Coral, pesadilla, onAlternar = vm::alternarPesadilla)
            }
            Spacer(Modifier.height(18.dp))
            SelectorMarcado("Ánimo (1–5)", mood, vm::marcarMood)
            Spacer(Modifier.height(14.dp))
            SelectorMarcado("Claridad (1–5)", claridad, vm::marcarClaridad)
            Spacer(Modifier.height(18.dp))
        }

        Button(
            onClick = { scope.launch { vm.guardar(); onVolver() } },
            enabled = texto.isNotBlank() && !grabando,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("btn-guardar"),
        ) {
            Text("Guardar sueño", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(24.dp))
    }
}