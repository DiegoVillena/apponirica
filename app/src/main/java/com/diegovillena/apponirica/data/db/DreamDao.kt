package com.diegovillena.apponirica.data.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class DreamConPalabras(
    @Embedded val sueno: Dream,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SuenoPalabraClave::class,
            parentColumn = "dreamId",
            entityColumn = "keywordId",
        ),
    )
    val palabras: List<PalabraClave>,
)

data class VinculoConTf(val stem: String, val tf: Int)

@Dao
interface DreamDao {
    @Transaction
    @Query("SELECT * FROM dreams ORDER BY dreamAt DESC")
    fun listarTodo(): Flow<List<DreamConPalabras>>

    @Transaction
    @Query("SELECT * FROM dreams WHERE id = :id")
    suspend fun obtener(id: Long): DreamConPalabras?

    @Insert
    suspend fun insertarSueno(dream: Dream): Long

    @Update
    suspend fun actualizarSueno(dream: Dream)

    @Query("DELETE FROM dreams WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("DELETE FROM dream_keywords WHERE dreamId = :id")
    suspend fun eliminarVinculos(id: Long)

    @Query(
        "SELECT k.stem AS stem, v.tf AS tf FROM dream_keywords v " +
            "INNER JOIN keywords k ON k.id = v.keywordId WHERE v.dreamId = :dreamId",
    )
    suspend fun vinculosDe(dreamId: Long): List<VinculoConTf>

    @Insert
    suspend fun insertarVinculo(vinculo: SuenoPalabraClave)

    @Query(
        "SELECT COUNT(*) FROM dream_keywords v INNER JOIN keywords k ON k.id = v.keywordId " +
            "WHERE k.stem = :stem AND v.dreamId != :dreamId",
    )
    suspend fun contarOtrosVinculosPorStem(stem: String, dreamId: Long): Int

    @Transaction
    @Query(
        "SELECT dreams.* FROM dreams INNER JOIN dreams_fts ON dreams.rowid = dreams_fts.rowid " +
            "WHERE dreams_fts MATCH :consulta ORDER BY dreams.dreamAt DESC",
    )
    suspend fun buscar(consulta: String): List<DreamConPalabras>

    @Transaction
    @Query(
        "SELECT d.* FROM dreams d INNER JOIN dream_keywords v ON v.dreamId = d.id " +
            "INNER JOIN keywords k ON k.id = v.keywordId WHERE k.stem = :stem " +
            "ORDER BY d.dreamAt DESC",
    )
    suspend fun suenosDePalabra(stem: String): List<DreamConPalabras>

    @Query("SELECT COUNT(*) FROM dreams")
    fun contarSuenos(): Flow<Int>

    @Query("SELECT dreamAt FROM dreams ORDER BY dreamAt DESC")
    fun fechasSuenos(): Flow<List<Long>>
}