package com.diegovillena.apponirica.core.keywords

/**
 * Extrae palabras clave de un texto en español: tokeniza, descarta stopwords y agrupa
 * las variantes por stem conservando la forma original como "display".
 */
data class PalabraClaveExtraida(val stem: String, val display: String, val tf: Int)

class ExtractoraPalabrasClave {

    private val regexPalabras = Regex("[\\p{L}]+")

    fun extraer(texto: String, stopwords: Set<String>): List<PalabraClaveExtraida> {
        val acumuladas = LinkedHashMap<String, PalabraClaveExtraida>()
        regexPalabras.findAll(texto).forEach { ocurrencia ->
            val display = ocurrencia.value
            val normalizada = StemmerEspanol.normalizar(display)
            if (normalizada.length > 2 && normalizada !in stopwords) {
                val stem = StemmerEspanol.stem(display)
                val previa = acumuladas[stem]
                acumuladas[stem] =
                    if (previa == null) PalabraClaveExtraida(stem, display, tf = 1)
                    else previa.copy(tf = previa.tf + 1)
            }
        }
        return acumuladas.values
            .sortedWith(compareByDescending<PalabraClaveExtraida> { it.tf }.thenBy { it.display })
    }

    /** Convierte una consulta libre en una consulta FTS4 con prefijos, p.ej. "vol*" "mar*". */
    fun consultaFts(consulta: String): String =
        regexPalabras.findAll(consulta).joinToString(" ") { "\"${it.value}\"*" }
}