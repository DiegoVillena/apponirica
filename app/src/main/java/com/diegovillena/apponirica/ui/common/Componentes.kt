package com.diegovillena.apponirica.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diegovillena.apponirica.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun saneaTag(stem: String): String = stem
    .replace(Regex("[^a-zA-Z0-9_]"), "_")
    .ifBlank { "x" }

/** Chip de palabra clave; el tag es su stem saneado para localizarlo con UI Automator. */
@Composable
fun PalabraClaveChip(
    display: String,
    stem: String = "",
    onClick: (() -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
        modifier = (if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Text(
            display,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .then(if (stem.isNotBlank()) Modifier.testTag("chip-keyword-" + saneaTag(stem)) else Modifier),
        )
    }
}

/** Chip de marca (Lúcido / Pesadilla); interactivo o estático dentro de una tarjeta. */
@Composable
fun MarcaChip(
    etiqueta: String,
    icono: ImageVector,
    color: Color,
    seleccionada: Boolean,
    modifier: Modifier = Modifier,
    onAlternar: (() -> Unit)? = null,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (seleccionada) color.copy(alpha = 0.16f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (seleccionada) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
        ),
        modifier = modifier.then(if (onAlternar != null) Modifier.clickable(onClick = onAlternar) else Modifier),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = if (seleccionada) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = if (seleccionada) color else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Selector circular 1..5 (ánimo / claridad), con toggle: pulsa de nuevo para quitar. */
@Composable
fun SelectorMarcado(
    etiqueta: String,
    valor: Int?,
    onFijar: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            (1..5).forEach { nivel ->
                val seleccionado = valor == nivel
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (seleccionado) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (seleccionado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = CircleShape,
                        )
                        .clickable { onFijar(nivel) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        nivel.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (seleccionado) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun EstadoVacio(titulo: String, mensaje: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ilustracion_suenos),
            contentDescription = null,
            modifier = Modifier.size(210.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 28.dp),
        )
    }
}

/** Barra de conteo relativa para el ranking de palabras clave. */
@Composable
fun BarraConteo(
    faccion: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.16f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(faccion.coerceIn(0.03f, 1f))
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color),
        )
    }
}

/** Fecha relativa amigable: "Hoy · 14:32", "Ayer · …" o "3 sep · …". */
fun formatearFechaSueno(ms: Long): String {
    val fechaHora = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())
    val dia = fechaHora.toLocalDate()
    val hora = fechaHora.format(DateTimeFormatter.ofPattern("HH:mm"))
    val hoy = LocalDate.now()
    val prefijo = when (dia) {
        hoy -> "Hoy"
        hoy.minusDays(1) -> "Ayer"
        else -> dia.format(DateTimeFormatter.ofPattern("d MMM", Locale("es")))
            .replaceFirstChar { it.uppercase() }
    }
    return "$prefijo · $hora"
}