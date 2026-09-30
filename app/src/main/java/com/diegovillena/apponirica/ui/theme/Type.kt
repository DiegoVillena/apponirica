@file:OptIn(ExperimentalTextApi::class)

package com.diegovillena.apponirica.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.diegovillena.apponirica.R

// Nunito es una fuente variable: cada FontWeight se instancia con su eje de variación.
private fun fuente(peso: Int, weight: FontWeight) = Font(
    resId = R.font.nunito_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(peso)),
)

val FamiliaNunito = FontFamily(
    fuente(400, FontWeight.Normal),
    fuente(600, FontWeight.SemiBold),
    fuente(700, FontWeight.Bold),
    fuente(800, FontWeight.ExtraBold),
)

val Tipografia = Typography(
    displaySmall = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FamiliaNunito, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 16.sp,
    ),
)