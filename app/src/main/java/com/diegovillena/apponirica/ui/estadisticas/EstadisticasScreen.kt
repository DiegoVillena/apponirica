package com.diegovillena.apponirica.ui.estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.diegovillena.apponirica.ui.common.BarraConteo
import com.diegovillena.apponirica.ui.common.EstadoVacio
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EstadisticasScreen(vm: EstadisticasViewModel, onFiltrarPalabra: (String, String) -> Unit) {
    val est by vm.estadisticas.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Estadísticas",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(20.dp))
        }

        if (est.totalSuenos == 0) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                EstadoVacio(
                    titulo = "Aún sin datos",
                    mensaje = "Cuando guardes sueños, aquí verás tus temas recurrentes: las palabras que vuelven noche tras noche.",
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TarjetaStat("Sueños", est.totalSuenos.toString(), Modifier.weight(1f))
                    TarjetaStat("Racha", if (est.diasRacha > 0) "${est.diasRacha} d" else "—", Modifier.weight(1f))
                    TarjetaStat("Este mes", (est.meses.entries.firstOrNull()?.value ?: 0).toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(26.dp))
                Text("Palabras clave más recurrentes", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(14.dp))
                if (est.topPalabras.isEmpty()) {
                    Text(
                        "Sin palabras todavía.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val maximo = est.topPalabras.first().dreamCount.coerceAtLeast(1)
                    est.topPalabras.forEachIndexed { indice, palabra ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onFiltrarPalabra(palabra.stem, palabra.display) }
                                .padding(vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    palabra.display,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    if (palabra.dreamCount == 1) "1 sueño" else "${palabra.dreamCount} sueños",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            BarraConteo(
                                palabra.dreamCount.toFloat() / maximo,
                                color = MaterialTheme.colorScheme.primary.copy(
                                    alpha = (1f - indice * 0.045f).coerceAtLeast(0.55f),
                                ),
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
                Spacer(Modifier.height(26.dp))
                Text("Sueños por mes", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                BarraMensual(est.meses)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TarjetaStat(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(valor, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BarraMensual(meses: Map<YearMonth, Int>) {
    val maximo = (meses.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val formato = DateTimeFormatter.ofPattern("MMM", Locale("es"))
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        meses.entries.reversed().forEach { (mes, cuenta) ->
            Column(
                Modifier.weight(1f),
                // alineadas a la base
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(maxOf(((cuenta.toFloat() / maximo) * 90).dp, 4.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (cuenta > 0) 0.85f else 0.25f,
                            ),
                        ),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    mes.format(formato).replace(".", ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}