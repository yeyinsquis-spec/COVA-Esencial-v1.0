package com.cova.esencial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cova.esencial.data.local.entities.*
import com.cova.esencial.data.repository.CovaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CovaUiState(
    val casetas: List<CasetaEntity> = emptyList(),
    val casetaActiva: CasetaEntity? = null,
    val consignas: List<ConsignaEntity> = emptyList(),
    val plantillasRecorrido: List<PlantillaRecorridoEntity> = emptyList(),
    val recorridos: List<RecorridoEntity> = emptyList(),
    val reportes: List<ReporteEntity> = emptyList(),
    val configuracion: ConfiguracionEntity = ConfiguracionEntity(),
    val mensajeEstado: String? = null,
    val esError: Boolean = false
)

class CovaViewModel(private val repository: CovaRepository) : ViewModel() {

    private val _mensaje = MutableStateFlow<Pair<String, Boolean>?>(null)

    val uiState: StateFlow<CovaUiState> = combine(
        repository.casetas,
        repository.consignas,
        repository.plantillasRecorrido,
        repository.recorridos,
        repository.reportes,
        repository.configuracion,
        _mensaje
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val casetas = values[0] as List<CasetaEntity>
        @Suppress("UNCHECKED_CAST")
        val consignas = values[1] as List<ConsignaEntity>
        @Suppress("UNCHECKED_CAST")
        val plantillas = values[2] as List<PlantillaRecorridoEntity>
        @Suppress("UNCHECKED_CAST")
        val recorridos = values[3] as List<RecorridoEntity>
        @Suppress("UNCHECKED_CAST")
        val reportes = values[4] as List<ReporteEntity>
        val config = (values[5] as ConfiguracionEntity?) ?: ConfiguracionEntity()
        @Suppress("UNCHECKED_CAST")
        val msg = values[6] as Pair<String, Boolean>?

        val casetaActiva = casetas.firstOrNull { it.codigo == config.casetaSeleccionadaCodigo }
            ?: casetas.firstOrNull()

        CovaUiState(
            casetas = casetas,
            casetaActiva = casetaActiva,
            consignas = consignas,
            plantillasRecorrido = plantillas,
            recorridos = recorridos,
            reportes = reportes,
            configuracion = config,
            mensajeEstado = msg?.first,
            esError = msg?.second ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CovaUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialDataLoaded()
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun mostrarMensaje(texto: String, error: Boolean = false) {
        _mensaje.value = texto to error
    }

    fun seleccionarCaseta(codigo: String) {
        viewModelScope.launch {
            repository.seleccionarCaseta(codigo)
        }
    }

    fun guardarPreferencias(nombreGuardia: String, turno: String) {
        viewModelScope.launch {
            repository.actualizarPreferencias(nombreGuardia, turno)
            mostrarMensaje("Preferencias guardadas correctamente.")
        }
    }

    fun guardarCaseta(codigo: String, nombre: String, modulos: String) {
        viewModelScope.launch {
            repository.guardarCaseta(codigo, nombre, modulos)
                .onSuccess { mostrarMensaje("Caseta guardada correctamente.") }
                .onFailure { mostrarMensaje(it.message ?: "Error al guardar caseta.", error = true) }
        }
    }

    fun eliminarCaseta(caseta: CasetaEntity) {
        viewModelScope.launch {
            repository.eliminarCaseta(caseta)
                .onSuccess { mostrarMensaje("Caseta eliminada.") }
                .onFailure { mostrarMensaje(it.message ?: "No se pudo eliminar la caseta.", error = true) }
        }
    }

    fun guardarRecorrido(contenido: String) {
        val caseta = uiState.value.casetaActiva
        if (caseta == null) {
            mostrarMensaje("Selecciona una caseta antes de guardar el recorrido.", error = true)
            return
        }
        viewModelScope.launch {
            repository.guardarRecorrido(caseta, contenido)
                .onSuccess { mostrarMensaje("Recorrido guardado en historial local.") }
                .onFailure { mostrarMensaje(it.message ?: "Error al guardar recorrido.", error = true) }
        }
    }

    fun eliminarRecorrido(id: Long) {
        viewModelScope.launch {
            repository.eliminarRecorrido(id)
            mostrarMensaje("Registro de recorrido eliminado.")
        }
    }

    fun guardarReporte(
        tipo: String,
        guardiaNombre: String,
        turno: String,
        contenidoFormateado: String
    ) {
        val caseta = uiState.value.casetaActiva
        if (caseta == null) {
            mostrarMensaje("Selecciona una caseta antes de guardar el reporte.", error = true)
            return
        }
        viewModelScope.launch {
            repository.guardarReporte(tipo, caseta, guardiaNombre, turno, contenidoFormateado)
                .onSuccess { mostrarMensaje("Reporte guardado en el dispositivo.") }
                .onFailure { mostrarMensaje(it.message ?: "Error al guardar reporte.", error = true) }
        }
    }

    fun eliminarReporte(id: Long) {
        viewModelScope.launch {
            repository.eliminarReporte(id)
            mostrarMensaje("Reporte eliminado del historial.")
        }
    }

    suspend fun generarJsonRespaldo(): String = repository.exportarRespaldoJson()

    fun importarJsonRespaldo(jsonText: String) {
        viewModelScope.launch {
            repository.importarRespaldoJson(jsonText)
                .onSuccess { mostrarMensaje(it, error = false) }
                .onFailure { mostrarMensaje(it.message ?: "Error al importar respaldo.", error = true) }
        }
    }

    class Factory(private val repository: CovaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CovaViewModel(repository) as T
        }
    }
}
