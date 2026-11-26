package com.cova.esencial.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cova.esencial.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CasetaDao {
    @Query("SELECT * FROM casetas ORDER BY id ASC")
    fun observeAll(): Flow<List<CasetaEntity>>

    @Query("SELECT * FROM casetas ORDER BY id ASC")
    suspend fun getAllOnce(): List<CasetaEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(caseta: CasetaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(caseta: CasetaEntity): Long

    @Update
    suspend fun update(caseta: CasetaEntity)

    @Query("DELETE FROM casetas WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ConsignaDao {
    @Query("SELECT * FROM consignas ORDER BY id ASC")
    fun observeAll(): Flow<List<ConsignaEntity>>

    @Query("SELECT * FROM consignas ORDER BY id ASC")
    suspend fun getAllOnce(): List<ConsignaEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(consigna: ConsignaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(consigna: ConsignaEntity): Long
}

@Dao
interface PlantillaRecorridoDao {
    @Query("SELECT * FROM plantillas_recorrido ORDER BY id ASC")
    fun observeAll(): Flow<List<PlantillaRecorridoEntity>>

    @Query("SELECT * FROM plantillas_recorrido ORDER BY id ASC")
    suspend fun getAllOnce(): List<PlantillaRecorridoEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(plantilla: PlantillaRecorridoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plantilla: PlantillaRecorridoEntity): Long
}

@Dao
interface RecorridoDao {
    @Query("SELECT * FROM recorridos_realizados ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<RecorridoEntity>>

    @Query("SELECT * FROM recorridos_realizados ORDER BY timestamp DESC")
    suspend fun getAllOnce(): List<RecorridoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recorrido: RecorridoEntity): Long

    @Query("DELETE FROM recorridos_realizados WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ReporteDao {
    @Query("SELECT * FROM reportes ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes ORDER BY timestamp DESC")
    suspend fun getAllOnce(): List<ReporteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reporte: ReporteEntity): Long

    @Query("DELETE FROM reportes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM configuracion WHERE id = 1")
    fun observeConfig(): Flow<ConfiguracionEntity?>

    @Query("SELECT * FROM configuracion WHERE id = 1")
    suspend fun getConfigOnce(): ConfiguracionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ConfiguracionEntity)
}
