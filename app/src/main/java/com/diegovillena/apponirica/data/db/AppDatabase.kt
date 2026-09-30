package com.diegovillena.apponirica.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Dream::class, DreamFts::class, PalabraClave::class, SuenoPalabraClave::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao

    abstract fun palabraClaveDao(): PalabraClaveDao

    companion object {
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "apponirica.db").build()
    }
}