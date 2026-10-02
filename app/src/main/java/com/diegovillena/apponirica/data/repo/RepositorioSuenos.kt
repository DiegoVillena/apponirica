package com.diegovillena.apponirica.data.repo

import androidx.room.withTransaction
import com.diegovillena.apponirica.core.keywords.ExtractoraPalabrasClave
import com.diegovillena.apponirica.core.keywords.PalabraClaveExtraida
import com.diegovillena.apponirica.core.keywords.StopwordsEspanol
import com.diegovillena.apponirica.data.db.AppDatabase
import com.diegovillena.apponirica.data.db.Dream
import com.diegovillena.apponirica.data.db.DreamConPalabras
import com.diegovillena.apponirica.data.db.PalabraClave
import com.diegovillena.apponirica.data.db.SuenoPalabraClave
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class Estadisticas(
    val totalSuenos: Int,
    val diasRacha: Int,
    val meses: Map<YearMonth, Int>,
    val topPalabras: List<PalabraClave>,
)

/** Única puerta de entrada a la base de datos: guarda, edita, borra y recalcula keywords. */
@OptIn(ExperimentalCoroutinesApi::class)
class RepositorioSuenos(
    private val db: AppDatabase,
    private val extractor: ExtractoraPalabrasClave,
) {
    private val dao = db.dreamDao()
    private val daoPalabras = db.palabraClaveDao()

    /** Contador de escrituras: la invalidación automática de Room no reemite los flows de
     *  listas de forma fiable en caliente (tras guardar/borrar la UI mostraba datos viejos),
     *  así que cada escritura incrementa esto y los flows de lectura re-cargan sobre la señal. */
    private val version = MutableStateFlow(0L)

    private fun marcarCambio() { version.value += 1 }

    fun listar(): Flow<List<DreamConPalabras>> = version.flatMapLatest { dao.listarTodo() }

    suspend fun guardarSueno(
        texto: String,
        titulo: String,
        lucido: Boolean,
        pesadilla: Boolean,
        mood: Int?,
        claridad: Int?,
        audioPath: String?,
        origen: String,
        ahora: Long = System.currentTimeMillis(),
    ): Long {
        val id = db.withTransaction {
            val dream = Dream(
                titulo = titulo.ifBlank { "Sueño sin título" },
                texto = texto,
                audioPath = audioPath,
                origenTexto = origen,
                esLucido = lucido,
                esPesadilla = pesadilla,
                mood = mood,
                claridad = claridad,
                createdAt = ahora,
                dreamAt = ahora,
            )
            val idNuevo = dao.insertarSueno(dream)
            aplicarPalabras(idNuevo, extraer(texto))
            idNuevo
        }
        marcarCambio()
        return id
    }

    suspend fun actualizarSueno(dream: Dream) {
        db.withTransaction {
            dao.actualizarSueno(dream)
            aplicarPalabras(dream.id, extraer(dream.texto))
        }
        marcarCambio()
    }

    suspend fun eliminarSueno(id: Long) {
        db.withTransaction {
            val vinculos = dao.vinculosDe(id)
            val dreamViejo = dao.obtener(id)
            dao.eliminar(id)
            val ahora = System.currentTimeMillis()
            vinculos.forEach { vinculo ->
                val otros = dao.contarOtrosVinculosPorStem(vinculo.stem, id)
                daoPalabras.aplicarDelta(vinculo.stem, -vinculo.tf, if (otros == 0) -1 else 0, ahora)
            }
            dreamViejo?.sueno?.audioPath?.let { ruta ->
                runCatching { File(ruta).delete() }
            }
            daoPalabras.purgarHuerfanas()
        }
        marcarCambio()
    }

    suspend fun obtenerPorId(id: Long): DreamConPalabras? = dao.obtener(id)

    suspend fun buscar(consulta: String): List<DreamConPalabras> {
        val consultaFts = extractor.consultaFts(consulta)
        if (consultaFts.isBlank()) return emptyList()
        return dao.buscar(consultaFts)
    }

    suspend fun suenosPorPalabra(stem: String): List<DreamConPalabras> = dao.suenosDePalabra(stem)

    val estadisticas: Flow<Estadisticas> = version.flatMapLatest {
        combine(
            dao.contarSuenos(),
            dao.fechasSuenos(),
            daoPalabras.topPalabras(20),
        ) { total, fechas, top ->
            Estadisticas(
                totalSuenos = total,
                diasRacha = calcularRacha(fechas),
                meses = contarPorMes(fechas),
                topPalabras = top,
            )
        }
    }

    private fun extraer(texto: String): List<PalabraClaveExtraida> =
        extractor.extraer(texto, StopwordsEspanol.conjunto)

    /**
     * Sincroniza las palabras clave de un sueño. Los vínculos por sueño son la fuente de
     * verdad y cada ciclo ajusta los contadores agregados de las keywords (deltas).
     */
    private suspend fun aplicarPalabras(dreamId: Long, extraidas: List<PalabraClaveExtraida>) {
        val previas = dao.vinculosDe(dreamId).associate { it.stem to it.tf }
        dao.eliminarVinculos(dreamId)
        val ahora = System.currentTimeMillis()

        previas.filterKeys { stem -> extraidas.none { it.stem == stem } }.forEach { (stem, tfAnterior) ->
            val otros = dao.contarOtrosVinculosPorStem(stem, dreamId)
            daoPalabras.aplicarDelta(stem, -tfAnterior, if (otros == 0) -1 else 0, ahora)
        }

        extraidas.forEach { extraida ->
            var palabra = daoPalabras.porStem(extraida.stem)
            if (palabra == null) {
                daoPalabras.insertar(
                    PalabraClave(stem = extraida.stem, display = extraida.display, lastSeenAt = ahora),
                )
                palabra = daoPalabras.porStem(extraida.stem)
            }
            val keywordId = palabra?.id ?: return@forEach
            val ligadaAntes = previas.containsKey(extraida.stem)
            val deltaTotal = extraida.tf - (previas[extraida.stem] ?: 0)
            val deltaSuenos = if (ligadaAntes) 0 else 1
            daoPalabras.aplicarDelta(extraida.stem, deltaTotal, deltaSuenos, ahora)
            dao.insertarVinculo(SuenoPalabraClave(dreamId, keywordId, extraida.tf))
        }
        daoPalabras.purgarHuerfanas()
    }

    private fun calcularRacha(fechasMs: List<Long>): Int {
        val dias = fechasMs
            .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
            .toSet()
        var dia = LocalDate.now()
        if (dia !in dias) dia = dia.minusDays(1)
        var racha = 0
        while (dia in dias) {
            racha += 1
            dia = dia.minusDays(1)
        }
        return racha
    }

    private fun contarPorMes(fechasMs: List<Long>): Map<YearMonth, Int> {
        val conteos = fechasMs.groupBy {
            YearMonth.from(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()))
        }.mapValues { (_, fechas) -> fechas.size }
        val ahora = YearMonth.now()
        return (0L..5L).associate { delta ->
            val mes = ahora.minusMonths(delta)
            mes to (conteos[mes] ?: 0)
        }
    }
}