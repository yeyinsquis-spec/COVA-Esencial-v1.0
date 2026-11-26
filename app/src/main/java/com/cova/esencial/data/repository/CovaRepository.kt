package com.cova.esencial.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.cova.esencial.data.local.CovaDatabase
import com.cova.esencial.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CovaRepository(
    private val context: Context,
    private val db: CovaDatabase
) {
    private val seedMutex = Mutex()

    val casetas: Flow<List<CasetaEntity>> = db.casetaDao().observeAll()
    val consignas: Flow<List<ConsignaEntity>> = db.consignaDao().observeAll()
    val plantillasRecorrido: Flow<List<PlantillaRecorridoEntity>> = db.plantillaRecorridoDao().observeAll()
    val recorridos: Flow<List<RecorridoEntity>> = db.recorridoDao().observeAll()
    val reportes: Flow<List<ReporteEntity>> = db.reporteDao().observeAll()
    val configuracion: Flow<ConfiguracionEntity?> = db.configuracionDao().observeConfig()

    /**
     * Carga inicial idempotente: utiliza claves únicas e inserción IGNORE
     * para evitar duplicaciones incluso si se ejecuta múltiples veces.
     */
    suspend fun ensureInitialDataLoaded() = withContext(Dispatchers.IO) {
        seedMutex.withLock {
            val rawJson = context.assets.open("initial_cova_data.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JSONObject(rawJson)
            val seedVersion = root.optInt("seedVersion", 1)

            db.withTransaction {
                val existingConfig = db.configuracionDao().getConfigOnce()
                if (existingConfig == null) {
                    db.configuracionDao().saveConfig(
                        ConfiguracionEntity(
                            id = 1,
                            casetaSeleccionadaCodigo = "SUR",
                            seedVersionAplicada = seedVersion
                        )
                    )
                }

                val casetasArr = root.getJSONArray("casetas")
                for (i in 0 until casetasArr.length()) {
                    val obj = casetasArr.getJSONObject(i)
                    db.casetaDao().insertIgnore(
                        CasetaEntity(
                            codigo = obj.getString("codigo").trim().uppercase(Locale.ROOT),
                            nombre = obj.getString("nombre").trim(),
                            modulos = obj.getString("modulos").trim(),
                            activa = obj.optBoolean("activa", true)
                        )
                    )
                }

                val consignasArr = root.getJSONArray("consignas")
                for (i in 0 until consignasArr.length()) {
                    val obj = consignasArr.getJSONObject(i)
                    db.consignaDao().insertIgnore(
                        ConsignaEntity(
                            clave = obj.getString("clave").trim(),
                            casetaCodigo = obj.getString("casetaCodigo").trim().uppercase(Locale.ROOT),
                            categoria = obj.getString("categoria").trim(),
                            titulo = obj.getString("titulo").trim(),
                            contenido = obj.getString("contenido").trim()
                        )
                    )
                }

                val plantillasArr = root.getJSONArray("plantillasRecorrido")
                for (i in 0 until plantillasArr.length()) {
                    val obj = plantillasArr.getJSONObject(i)
                    db.plantillaRecorridoDao().insertIgnore(
                        PlantillaRecorridoEntity(
                            clave = obj.getString("clave").trim(),
                            casetaCodigo = obj.getString("casetaCodigo").trim().uppercase(Locale.ROOT),
                            titulo = obj.getString("titulo").trim(),
                            contenido = obj.getString("contenido").trim()
                        )
                    )
                }

                if (existingConfig != null && existingConfig.seedVersionAplicada < seedVersion) {
                    db.configuracionDao().saveConfig(
                        existingConfig.copy(seedVersionAplicada = seedVersion)
                    )
                }
            }
        }
    }

    suspend fun seleccionarCaseta(codigo: String) = withContext(Dispatchers.IO) {
        val current = db.configuracionDao().getConfigOnce() ?: ConfiguracionEntity()
        db.configuracionDao().saveConfig(current.copy(casetaSeleccionadaCodigo = codigo))
    }

    suspend fun actualizarPreferencias(nombreGuardia: String, turno: String) = withContext(Dispatchers.IO) {
        val current = db.configuracionDao().getConfigOnce() ?: ConfiguracionEntity()
        db.configuracionDao().saveConfig(
            current.copy(
                nombreGuardiaDefault = nombreGuardia.trim(),
                turnoDefault = turno.trim()
            )
        )
    }

    suspend fun guardarCaseta(codigo: String, nombre: String, modulos: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val cleanCode = codigo.trim().uppercase(Locale.ROOT)
            val cleanName = nombre.trim()
            val cleanModulos = modulos.trim()
            if (cleanCode.isEmpty() || cleanName.isEmpty() || cleanModulos.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Completa código, nombre y módulos de la caseta."))
            }
            val existente = db.casetaDao().getAllOnce().firstOrNull { it.codigo == cleanCode }
            if (existente != null) {
                db.casetaDao().update(existente.copy(nombre = cleanName, modulos = cleanModulos, activa = true))
            } else {
                db.casetaDao().insertIgnore(
                    CasetaEntity(codigo = cleanCode, nombre = cleanName, modulos = cleanModulos, activa = true)
                )
            }
            Result.success(Unit)
        }

    suspend fun eliminarCaseta(caseta: CasetaEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val todas = db.casetaDao().getAllOnce()
        if (todas.size <= 1) {
            return@withContext Result.failure(IllegalStateException("Debe existir al menos una caseta registrada."))
        }
        db.casetaDao().deleteById(caseta.id)
        val config = db.configuracionDao().getConfigOnce()
        if (config?.casetaSeleccionadaCodigo == caseta.codigo) {
            val siguiente = db.casetaDao().getAllOnce().firstOrNull()
            if (siguiente != null) {
                db.configuracionDao().saveConfig(config.copy(casetaSeleccionadaCodigo = siguiente.codigo))
            }
        }
        Result.success(Unit)
    }

    suspend fun guardarRecorrido(
        caseta: CasetaEntity,
        contenido: String
    ): Result<RecorridoEntity> = withContext(Dispatchers.IO) {
        val limpio = contenido.trim()
        if (limpio.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("El texto del recorrido no puede estar vacío."))
        }
        val now = System.currentTimeMillis()
        val fecha = formatTimestamp(now)
        val entity = RecorridoEntity(
            casetaCodigo = caseta.codigo,
            casetaNombre = caseta.nombre,
            modulos = caseta.modulos,
            contenido = limpio,
            fechaHora = fecha,
            timestamp = now
        )
        val id = db.recorridoDao().insert(entity)
        Result.success(entity.copy(id = id))
    }

    suspend fun eliminarRecorrido(id: Long) = withContext(Dispatchers.IO) {
        db.recorridoDao().deleteById(id)
    }

    suspend fun guardarReporte(
        tipo: String,
        caseta: CasetaEntity,
        guardiaNombre: String,
        turno: String,
        contenidoFormateado: String
    ): Result<ReporteEntity> = withContext(Dispatchers.IO) {
        val limpio = contenidoFormateado.trim()
        if (limpio.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("El contenido del reporte no puede estar vacío."))
        }
        val now = System.currentTimeMillis()
        val fecha = formatTimestamp(now)
        val entity = ReporteEntity(
            tipo = tipo,
            casetaCodigo = caseta.codigo,
            casetaNombre = caseta.nombre,
            modulos = caseta.modulos,
            guardiaNombre = guardiaNombre.trim(),
            turno = turno.trim(),
            contenidoFormateado = limpio,
            fechaHora = fecha,
            timestamp = now
        )
        val id = db.reporteDao().insert(entity)
        Result.success(entity.copy(id = id))
    }

    suspend fun eliminarReporte(id: Long) = withContext(Dispatchers.IO) {
        db.reporteDao().deleteById(id)
    }

    /**
     * Exporta toda la base local a un JSON estructurado y verificable.
     */
    suspend fun exportarRespaldoJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "COVA_ESENCIAL")
        root.put("backupVersion", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val config = db.configuracionDao().getConfigOnce() ?: ConfiguracionEntity()
        root.put("configuracion", JSONObject().apply {
            put("casetaSeleccionadaCodigo", config.casetaSeleccionadaCodigo)
            put("nombreGuardiaDefault", config.nombreGuardiaDefault)
            put("turnoDefault", config.turnoDefault)
            put("seedVersionAplicada", config.seedVersionAplicada)
        })

        val casetasArr = JSONArray()
        db.casetaDao().getAllOnce().forEach {
            casetasArr.put(JSONObject().apply {
                put("codigo", it.codigo)
                put("nombre", it.nombre)
                put("modulos", it.modulos)
                put("activa", it.activa)
            })
        }
        root.put("casetas", casetasArr)

        val consignasArr = JSONArray()
        db.consignaDao().getAllOnce().forEach {
            consignasArr.put(JSONObject().apply {
                put("clave", it.clave)
                put("casetaCodigo", it.casetaCodigo)
                put("categoria", it.categoria)
                put("titulo", it.titulo)
                put("contenido", it.contenido)
            })
        }
        root.put("consignas", consignasArr)

        val recorridosArr = JSONArray()
        db.recorridoDao().getAllOnce().forEach {
            recorridosArr.put(JSONObject().apply {
                put("casetaCodigo", it.casetaCodigo)
                put("casetaNombre", it.casetaNombre)
                put("modulos", it.modulos)
                put("contenido", it.contenido)
                put("fechaHora", it.fechaHora)
                put("timestamp", it.timestamp)
            })
        }
        root.put("recorridos", recorridosArr)

        val reportesArr = JSONArray()
        db.reporteDao().getAllOnce().forEach {
            reportesArr.put(JSONObject().apply {
                put("tipo", it.tipo)
                put("casetaCodigo", it.casetaCodigo)
                put("casetaNombre", it.casetaNombre)
                put("modulos", it.modulos)
                put("guardiaNombre", it.guardiaNombre)
                put("turno", it.turno)
                put("contenidoFormateado", it.contenidoFormateado)
                put("fechaHora", it.fechaHora)
                put("timestamp", it.timestamp)
            })
        }
        root.put("reportes", reportesArr)

        root.toString(2)
    }

    /**
     * Importa respaldo JSON validando estructura antes de modificar la base de datos.
     * Evita duplicar registros ya existentes mediante validación de claves y marcas de tiempo.
     */
    suspend fun importarRespaldoJson(jsonText: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonText)
            if (root.optString("app") != "COVA_ESENCIAL") {
                return@withContext Result.failure(IllegalArgumentException("El archivo no corresponde a un respaldo válido de COVA."))
            }

            var casetasImportadas = 0
            var recorridosImportados = 0
            var reportesImportados = 0

            db.withTransaction {
                if (root.has("casetas")) {
                    val arr = root.getJSONArray("casetas")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val inserted = db.casetaDao().insertIgnore(
                            CasetaEntity(
                                codigo = obj.getString("codigo").trim().uppercase(Locale.ROOT),
                                nombre = obj.getString("nombre").trim(),
                                modulos = obj.getString("modulos").trim(),
                                activa = obj.optBoolean("activa", true)
                            )
                        )
                        if (inserted != -1L) casetasImportadas++
                    }
                }

                if (root.has("consignas")) {
                    val arr = root.getJSONArray("consignas")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        db.consignaDao().insertIgnore(
                            ConsignaEntity(
                                clave = obj.getString("clave").trim(),
                                casetaCodigo = obj.getString("casetaCodigo").trim().uppercase(Locale.ROOT),
                                categoria = obj.getString("categoria").trim(),
                                titulo = obj.getString("titulo").trim(),
                                contenido = obj.getString("contenido").trim()
                            )
                        )
                    }
                }

                val existingRecorridos = db.recorridoDao().getAllOnce()
                    .map { "${it.timestamp}_${it.casetaCodigo}_${it.contenido.hashCode()}" }
                    .toHashSet()

                if (root.has("recorridos")) {
                    val arr = root.getJSONArray("recorridos")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val ts = obj.getLong("timestamp")
                        val code = obj.getString("casetaCodigo")
                        val body = obj.getString("contenido")
                        val sig = "${ts}_${code}_${body.hashCode()}"
                        if (!existingRecorridos.contains(sig)) {
                            db.recorridoDao().insert(
                                RecorridoEntity(
                                    casetaCodigo = code,
                                    casetaNombre = obj.getString("casetaNombre"),
                                    modulos = obj.getString("modulos"),
                                    contenido = body,
                                    fechaHora = obj.getString("fechaHora"),
                                    timestamp = ts
                                )
                            )
                            existingRecorridos.add(sig)
                            recorridosImportados++
                        }
                    }
                }

                val existingReportes = db.reporteDao().getAllOnce()
                    .map { "${it.timestamp}_${it.tipo}_${it.contenidoFormateado.hashCode()}" }
                    .toHashSet()

                if (root.has("reportes")) {
                    val arr = root.getJSONArray("reportes")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val ts = obj.getLong("timestamp")
                        val tipo = obj.getString("tipo")
                        val body = obj.getString("contenidoFormateado")
                        val sig = "${ts}_${tipo}_${body.hashCode()}"
                        if (!existingReportes.contains(sig)) {
                            db.reporteDao().insert(
                                ReporteEntity(
                                    tipo = tipo,
                                    casetaCodigo = obj.getString("casetaCodigo"),
                                    casetaNombre = obj.getString("casetaNombre"),
                                    modulos = obj.getString("modulos"),
                                    guardiaNombre = obj.optString("guardiaNombre", ""),
                                    turno = obj.optString("turno", ""),
                                    contenidoFormateado = body,
                                    fechaHora = obj.getString("fechaHora"),
                                    timestamp = ts
                                )
                            )
                            existingReportes.add(sig)
                            reportesImportados++
                        }
                    }
                }

                if (root.has("configuracion")) {
                    val cfg = root.getJSONObject("configuracion")
                    val current = db.configuracionDao().getConfigOnce() ?: ConfiguracionEntity()
                    db.configuracionDao().saveConfig(
                        current.copy(
                            casetaSeleccionadaCodigo = cfg.optString("casetaSeleccionadaCodigo", current.casetaSeleccionadaCodigo),
                            nombreGuardiaDefault = cfg.optString("nombreGuardiaDefault", current.nombreGuardiaDefault),
                            turnoDefault = cfg.optString("turnoDefault", current.turnoDefault)
                        )
                    )
                }
            }

            Result.success(
                "Respaldo restaurado: $casetasImportadas casetas nuevas, $recorridosImportados recorridos y $reportesImportados reportes."
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("No se pudo leer el respaldo: ${e.message ?: "formato inválido"}"))
        }
    }

    private fun formatTimestamp(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))
        return sdf.format(Date(millis))
    }
}
