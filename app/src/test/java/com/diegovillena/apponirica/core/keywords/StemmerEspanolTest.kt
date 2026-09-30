package com.diegovillena.apponirica.core.keywords

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StemmerEspanolTest {

    @Test
    fun agrupaPlurales() {
        assertEquals(StemmerEspanol.stem("casas"), StemmerEspanol.stem("casa"))
        assertEquals(StemmerEspanol.stem("Lunas"), StemmerEspanol.stem("luna"))
        assertEquals(StemmerEspanol.stem("pesadillas"), StemmerEspanol.stem("pesadilla"))
    }

    @Test
    fun agrupaFormasVerbales() {
        assertEquals(StemmerEspanol.stem("volando"), StemmerEspanol.stem("volar"))
        assertEquals(StemmerEspanol.stem("volaba"), StemmerEspanol.stem("volar"))
        assertEquals(StemmerEspanol.stem("corriendo"), StemmerEspanol.stem("correr"))
        assertEquals(StemmerEspanol.stem("corrio"), StemmerEspanol.stem("correr"))
    }

    @Test
    fun manejaTildesSinPerderLaEnie() {
        assertEquals(StemmerEspanol.stem("Sueño"), StemmerEspanol.stem("sueños"))
        assertTrue(StemmerEspanol.stem("Sueño").contains("ñ"))
    }

    @Test
    fun palabrasCortasQuedanComoEstan() {
        assertEquals("luz", StemmerEspanol.stem("luz"))
        assertEquals("pie", StemmerEspanol.stem("PIE"))
    }

    @Test
    fun lucesVocesCaenEnLuzVoz() {
        assertEquals(StemmerEspanol.stem("luces"), StemmerEspanol.stem("luz"))
        assertEquals(StemmerEspanol.stem("voces"), StemmerEspanol.stem("voz"))
    }
}