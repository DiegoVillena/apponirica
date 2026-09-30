package com.diegovillena.apponirica.core.keywords

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtractoraPalabrasClaveTest {

    private val extractor = ExtractoraPalabrasClave()

    @Test
    fun filtraStopwordsYcuentaFrecuencias() {
        val texto = "Soñé que volaba sobre un mar de nubes y volaba muy alto, sobre las nubes."
        val extraidas = extractor.extraer(texto, StopwordsEspanol.conjunto)
        val porStem = extraidas.associateBy { it.stem }

        assertEquals(2, porStem["vol"]?.tf)
        assertEquals(2, porStem["nub"]?.tf)
        assertEquals(1, porStem["mar"]?.tf)
        assertEquals("volaba", porStem["vol"]?.display)
        assertEquals("nubes", porStem["nub"]?.display)
        assertFalse(porStem.containsKey("sobr"))
    }

    @Test
    fun laConsultaFtsUsaPrefijos() {
        assertEquals("\"volaba\"* \"mar\"*", extractor.consultaFts("volaba mar"))
        assertTrue(extractor.consultaFts("  .,; ").isBlank())
    }
}