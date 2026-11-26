package com.cova.esencial.data.templates

enum class TipoReporte(val code: String, val titulo: String) {
    INICIO_TURNO("INICIO_TURNO", "INICIO DE TURNO"),
    CIERRE_TURNO("CIERRE_TURNO", "CIERRE DE TURNO"),
    NOVEDADES("NOVEDADES", "REPORTE DE NOVEDADES"),
    INCIDENTES("INCIDENTES", "REPORTE DE INCIDENTES");

    companion object {
        fun fromCode(code: String): TipoReporte =
            entries.firstOrNull { it.code == code } ?: NOVEDADES
    }
}

object LocalReportFormatter {

    fun normalizarTexto(texto: String): String {
        val limpio = texto
            .trim()
            .replace(Regex("[ \t]+"), " ")
            .replace(Regex("\n{3,}"), "

")
        if (limpio.isEmpty()) return ""
        val conMayuscula = limpio.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        return if (conMayuscula.endsWith(".") || conMayuscula.endsWith("!") || conMayuscula.endsWith("?")) {
            conMayuscula
        } else {
            "$conMayuscula."
        }
    }

    fun generarPlantillaBase(
        tipo: TipoReporte,
        casetaNombre: String,
        modulos: String,
        guardiaNombre: String,
        turno: String,
        fechaHora: String,
        detalleUsuario: String,
        accionesTomadas: String = ""
    ): String {
        val guardia = guardiaNombre.trim().ifEmpty { "No especificado" }
        val turnoFinal = turno.trim().ifEmpty { "No especificado" }
        val detalleNormalizado = normalizarTexto(detalleUsuario)
        val accionesNormalizadas = normalizarTexto(accionesTomadas)

        return buildString {
            appendLine("Playa del Carmen, Quintana Roo")
            appendLine("COVA — ${tipo.titulo}")
            appendLine("----------------------------------------")
            appendLine("Fecha y hora: $fechaHora")
            appendLine("Caseta: $casetaNombre (Módulos: $modulos)")
            appendLine("Turno: $turnoFinal")
            appendLine("Elemento: $guardia")
            appendLine("----------------------------------------")
            when (tipo) {
                TipoReporte.INICIO_TURNO -> {
                    appendLine("ESTADO DE RECEPCIÓN:")
                    appendLine(detalleNormalizado.ifEmpty { "Se recibe servicio, consignas y equipo asignado sin novedad." })
                }
                TipoReporte.CIERRE_TURNO -> {
                    appendLine("RESUMEN DE CIERRE:")
                    appendLine(detalleNormalizado.ifEmpty { "Se entrega servicio, módulos verificados y bitácora al día sin novedad." })
                }
                TipoReporte.NOVEDADES -> {
                    appendLine("DESCRIPCIÓN DE NOVEDAD:")
                    appendLine(detalleNormalizado.ifEmpty { "Sin novedades relevantes durante el periodo reportado." })
                }
                TipoReporte.INCIDENTES -> {
                    appendLine("HECHOS REGISTRADOS:")
                    appendLine(detalleNormalizado.ifEmpty { "Detalle del incidente pendiente de captura." })
                    if (accionesNormalizadas.isNotEmpty()) {
                        appendLine()
                        appendLine("ACCIONES REALIZADAS:")
                        appendLine(accionesNormalizadas)
                    }
                }
            }
            append("----------------------------------------")
        }
    }
}
