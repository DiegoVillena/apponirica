package com.diegovillena.apponirica.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// "Medianoche": identidad oscura propia (sin dynamic color)
private val EsquemaMedianoche = darkColorScheme(
    primary = Lavanda,
    onPrimary = Color(0xFF221B3F),
    primaryContainer = Color(0xFF3A2C6E),
    onPrimaryContainer = Color(0xFFD9CCFF),
    secondary = Color(0xFFC3B5F5),
    onSecondary = Color(0xFF221B3F),
    tertiary = Ambar,
    onTertiary = Color(0xFF221B3F),
    tertiaryContainer = Color(0xFF52401A),
    onTertiaryContainer = Ambar,
    background = MedianocheFondo,
    onBackground = TextoLuna,
    surface = MedianocheSuperficie,
    onSurface = TextoLuna,
    surfaceVariant = MedianocheSuperficieAlta,
    onSurfaceVariant = TextoLunaSuave,
    outline = Color(0xFF4A3E75),
    error = Color(0xFFFF8B8B),
    onError = Color(0xFF3A1516),
    errorContainer = Color(0xFF5C2330),
    onErrorContainer = Color(0xFFFFB4B0),
)

// "Amanecer": variante clara de la misma identidad
private val EsquemaAmanecer = lightColorScheme(
    primary = VioletaVivo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7DBFF),
    onPrimaryContainer = Color(0xFF2A1448),
    secondary = Color(0xFF9E7CF7),
    onSecondary = Color.White,
    tertiary = Ambar,
    onTertiary = Color(0xFF3D2C00),
    tertiaryContainer = Color(0xFFFFE6BF),
    onTertiaryContainer = Color(0xFF6B4A00),
    background = AmanecerFondo,
    onBackground = TextoNoche,
    surface = AmanecerSuperficie,
    onSurface = TextoNoche,
    surfaceVariant = Color(0xFFF0EAFF),
    onSurfaceVariant = TextoNocheSuave,
    outline = Color(0xFFC9BCF0),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AppOniricaTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaMedianoche else EsquemaAmanecer,
        typography = Tipografia,
        shapes = Formas,
        content = content,
    )
}