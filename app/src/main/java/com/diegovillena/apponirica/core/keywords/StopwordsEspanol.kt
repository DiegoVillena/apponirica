package com.diegovillena.apponirica.core.keywords

/** Stopwords en español (sin tildes, minúsculas): palabras funcionales que nunca serán keywords. */
object StopwordsEspanol {

    val conjunto: Set<String> = setOf(
        "a", "al", "algo", "alguna", "algunas", "alguno", "algunos", "ante", "antes",
        "aquel", "aquella", "aquellas", "aquello", "aquellos", "aqui", "aun", "aunque",
        "asi", "bastante", "bien", "cada", "casi", "como", "con", "contra", "cual",
        "cuales", "cuando", "cuanta", "cuantas", "cuanto", "cuantos", "de", "del",
        "desde", "despues", "donde", "dos", "durante", "era", "eran", "eramos",
        "eras", "esa", "esas", "ese", "eso", "esos", "esta", "estaba", "estabamos",
        "estaban", "estabas", "estan", "estar", "estas", "este", "esto", "estos",
        "estoy", "estuve", "fue", "fuera", "fueron", "fui", "ha", "habia", "habian",
        "habido", "han", "has", "hasta", "hay", "iba", "iban", "ibamos", "incluso",
        "ir", "jamas", "la", "las", "lo", "los", "mas", "me", "mi", "mis",
        "mientras", "misma", "mismas", "mismo", "mismos", "muy", "nada", "ni", "no",
        "nos", "nosotras", "nosotros", "nuestra", "nuestras", "nuestro", "nuestros",
        "o", "os", "otra", "otras", "otro", "otros", "para", "pero", "podia",
        "podian", "podemos", "pude", "pueden", "puedo", "pudo", "por", "porque",
        "pues", "que", "quien", "quienes", "se", "sea", "sean", "ser", "si", "sido",
        "siempre", "sin", "sino", "sobre", "solamente", "solo", "son", "soy", "su",
        "sus", "tal", "tales", "tambien", "tan", "tanto", "te", "tenia", "tenian",
        "tenido", "tiene", "tienen", "tengo", "toda", "todas", "todo", "todos",
        "tras", "todavia", "tu", "tus", "un", "una", "unas", "uno", "unos",
        "usted", "ustedes", "va", "van", "vas", "vez", "ya", "yo", "ahora", "alla",
        "ahi", "alli", "detras", "delante", "encima", "debajo", "alrededor",
        "muchas", "muchos", "mucho", "pocas", "pocos", "parecia", "parecian",
        "intentaba", "veia", "sentia", "dije", "dijo", "dijeron", "creo", "sabia",
        "sabian", "quise", "queria", "finalmente", "etc",
    )
}