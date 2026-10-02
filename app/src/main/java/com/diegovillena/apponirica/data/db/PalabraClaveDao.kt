package com.diegovillena.apponirica.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PalabraClaveDao {
    /** El ranking es por PRESENCIA: una palabra vale 1 por sueño, diganla 1 o 10 veces
     *  en el mismo (totalCount solo desempata a igual número de sueños). */
    @Query("SELECT * FROM keywords ORDER BY dreamCount DESC, totalCount DESC LIMIT :limite")
    fun topPalabras(limite: Int): Flow<List<PalabraClave>>

    @Query("SELECT * FROM keywords WHERE stem = :stem LIMIT 1")
    suspend fun porStem(stem: String): PalabraClave?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(palabra: PalabraClave): Long

    @Query(
        "UPDATE keywords SET totalCount = totalCount + :dTotal, " +
            "dreamCount = dreamCount + :dDreams, lastSeenAt = :ahora WHERE stem = :stem",
    )
    suspend fun aplicarDelta(stem: String, dTotal: Int, dDreams: Int, ahora: Long): Int

    @Query("DELETE FROM keywords WHERE id NOT IN (SELECT keywordId FROM dream_keywords)")
    suspend fun purgarHuerfanas()
}