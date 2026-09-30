package com.diegovillena.apponirica.data.db

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dreams")
data class Dream(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val texto: String,
    val audioPath: String? = null,
    val origenTexto: String = ORIGEN_TECLADO,
    val esLucido: Boolean = false,
    val esPesadilla: Boolean = false,
    val mood: Int? = null,
    val claridad: Int? = null,
    val createdAt: Long,
    val dreamAt: Long,
) {
    companion object {
        const val ORIGEN_VOZ = "VOZ"
        const val ORIGEN_TECLADO = "TECLADO"
    }
}

/** Sombra FTS del texto para búsqueda de texto completo (el rowid coincide con dreams.id). */
@Fts4(contentEntity = Dream::class)
@Entity(tableName = "dreams_fts")
data class DreamFts(
    val texto: String,
)

@Entity(tableName = "keywords", indices = [Index(value = ["stem"], unique = true)])
data class PalabraClave(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stem: String,
    val display: String,
    val totalCount: Int = 0,
    val dreamCount: Int = 0,
    val lastSeenAt: Long = 0,
)

@Entity(
    tableName = "dream_keywords",
    primaryKeys = ["dreamId", "keywordId"],
    foreignKeys = [
        ForeignKey(
            entity = Dream::class,
            parentColumns = ["id"],
            childColumns = ["dreamId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PalabraClave::class,
            parentColumns = ["id"],
            childColumns = ["keywordId"],
        ),
    ],
    indices = [Index(value = ["keywordId"])],
)
data class SuenoPalabraClave(
    val dreamId: Long,
    val keywordId: Long,
    val tf: Int,
)