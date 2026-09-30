package com.diegovillena.apponirica.ui.ajustes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diegovillena.apponirica.transcription.MotorTranscripcion

@Composable
fun AjustesScreen(
    motor: MotorTranscripcion,
    onElegirMotor: (MotorTranscripcion) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Ajustes",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(20.dp))

            Text("Motor de transcripción", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "El motor local transcribe sin internet, no manda tu voz a nadie y guarda el audio del sueño. El de Google es algo más exacto, pero envía el audio a sus servidores.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            OpcionMotor(
                titulo = "Vosk local (recomendado)",
                descripcion = "Transcribe en vivo, guarda el WAV adjunto y solo suena tu voz: nada abandona el móvil.",
                seleccionado = motor == MotorTranscripcion.OFFLINE,
                onElegir = { onElegirMotor(MotorTranscripcion.OFFLINE) },
                tag = "ajuste-offline",
            )
            Spacer(Modifier.height(10.dp))
            OpcionMotor(
                titulo = "Google del sistema",
                descripcion = "Máxima calidad, pero la voz va al servicio de Google y MIUI no permite guardar el audio adjunto.",
                seleccionado = motor == MotorTranscripcion.GOOGLE,
                onElegir = { onElegirMotor(MotorTranscripcion.GOOGLE) },
                tag = "ajuste-google",
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "AppOnírica · MVP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OpcionMotor(
    titulo: String,
    descripcion: String,
    seleccionado: Boolean,
    onElegir: () -> Unit,
    tag: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable(onClick = onElegir),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            RadioButton(selected = seleccionado, onClick = null)
            Spacer(Modifier.width(4.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}