package com.cova.esencial.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "casetas",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class CasetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val modulos: String,
    val activa: Boolean = true
)

@Entity(
    tableName = "consignas",
    indices = [Index(value = ["clave"], unique = true)]
)
data class ConsignaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clave: String,
    val casetaCodigo: String,
    val categoria: String,
    val titulo: String,
    val contenido: String
)

@Entity(
    tableName = "plantillas_recorrido",
    indices = [Index(value = ["clave"], unique = true)]
)
data class PlantillaRecorridoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clave: String,
    val casetaCodigo: String,
    val titulo: String,
    val contenido: String
)

@Entity(tableName = "recorridos_realizados")
data class RecorridoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val casetaCodigo: String,
    val casetaNombre: String,
    val modulos: String,
    val contenido: String,
    val fechaHora: String,
    val timestamp: Long
)

@Entity(tableName = "reportes")
data class ReporteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String, // INICIO_TURNO, CIERRE_TURNO, NOVEDADES, INCIDENTES
    val casetaCodigo: String,
    val casetaNombre: String,
    val modulos: String,
    val guardiaNombre: String,
    val turno: String,
    val contenidoFormateado: String,
    val fechaHora: String,
    val timestamp: Long
)

@Entity(tableName = "configuracion")
data class ConfiguracionEntity(
    @PrimaryKey val id: Int = 1,
    val casetaSeleccionadaCodigo: String = "SUR",
    val nombreGuardiaDefault: String = "",
    val turnoDefault: String = "Nocturno",
    val seedVersionAplicada: Int = 0
)
