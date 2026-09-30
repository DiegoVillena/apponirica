package com.diegovillena.apponirica.core.keywords

/**
 * Stemmer en español por reglas: agrupa plurales y variantes verbales frecuentes de una
 * misma palabra en un mismo cubo. Suficiente para el conteo de keywords; no pretende ser
 * un lematizador lingüístico completo.
 */
object StemmerEspanol {

    fun normalizar(palabra: String): String = buildString(palabra.length) {
        for (c in palabra.lowercase()) append(
            when (c) {
                'á', 'à', 'ä' -> 'a'
                'é', 'è', 'ë' -> 'e'
                'í', 'ì', 'ï' -> 'i'
                'ó', 'ò', 'ö' -> 'o'
                'ú', 'ù', 'ü' -> 'u'
                else -> c
            },
        )
    }

    fun stem(palabra: String): String {
        var w = normalizar(palabra)
        if (w.length <= 3) return w
        repeat(4) {
            val previo = w
            w = pasoPlural(w)
            if (w.length > 3) w = pasoVerbo(w)
            if (w == previo) return w
        }
        return w
    }

    private fun pasoPlural(palabra: String): String = when {
        palabra.endsWith("ciones") && palabra.length >= 7 -> palabra.dropLast(2) // operaciones → operacion
        palabra.endsWith("ces") && palabra.length >= 5 -> palabra.dropLast(3) + "z" // luces → luz
        palabra.endsWith("es") && palabra.length >= 5 -> palabra.dropLast(2) // tardes → tard
        palabra.endsWith("s") && palabra.length >= 4 -> palabra.dropLast(1) // casas → casa
        else -> palabra
    }

    // De mayor a menor: gana el primer sufijo que deje un stem de al menos 3 letras.
    private val SUFIJOS_VERBO = listOf(
        "iendo", "ando", "amos", "emos", "imos", "abas", "aban", "aron", "ieron", "aba",
        "ados", "adas", "idos", "idas", "ado", "ada", "ido", "ida",
        "aria", "eria", "iria", "are", "ere", "ire", "ara", "era", "ira",
        "ia", "io", "an", "en", "as", "es", "os", "ar", "er", "ir", "a", "o", "e",
    ).sortedByDescending { it.length }

    private fun pasoVerbo(palabra: String): String {
        for (sufijo in SUFIJOS_VERBO) {
            if (palabra.endsWith(sufijo) && palabra.length - sufijo.length >= 3) {
                return palabra.dropLast(sufijo.length)
            }
        }
        return palabra
    }
}