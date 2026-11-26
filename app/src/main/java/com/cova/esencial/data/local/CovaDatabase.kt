package com.cova.esencial.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cova.esencial.data.local.dao.*
import com.cova.esencial.data.local.entities.*

@Database(
    entities = [
        CasetaEntity::class,
        ConsignaEntity::class,
        PlantillaRecorridoEntity::class,
        RecorridoEntity::class,
        ReporteEntity::class,
        ConfiguracionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CovaDatabase : RoomDatabase() {
    abstract fun casetaDao(): CasetaDao
    abstract fun consignaDao(): ConsignaDao
    abstract fun plantillaRecorridoDao(): PlantillaRecorridoDao
    abstract fun recorridoDao(): RecorridoDao
    abstract fun reporteDao(): ReporteDao
    abstract fun configuracionDao(): ConfiguracionDao

    companion object {
        const val DATABASE_NAME = "cova_esencial.db"

        @Volatile
        private var INSTANCE: CovaDatabase? = null

        /**
         * Migraciones explícitas preparadas para futuras versiones (v1 -> v2, etc.).
         * Nunca se usa fallbackToDestructiveMigration() para proteger reportes y recorridos históricos.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Plantilla segura de evolución sin pérdida de datos
            }
        }

        fun getInstance(context: Context): CovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CovaDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
