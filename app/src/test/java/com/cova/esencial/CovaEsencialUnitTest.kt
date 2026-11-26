package com.cova.esencial

import com.cova.esencial.data.templates.LocalReportFormatter
import com.cova.esencial.data.templates.TipoReporte
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CovaEsencialUnitTest {

    @Test
    fun cargaInicial_tieneCasetasSinDuplicadosYDistribucionBase() {
        val seedFile = File("src/main/assets/initial_cova_data.json")
        assertTrue("Debe existir initial_cova_data.json", seedFile.exists())

        val json = JSONObject(seedFile.readText())
        val casetas = json.getJSONArray("casetas")
        val codigos = mutableSetOf<String>()
        val mapaModulos = mutableMapOf<String, String>()

        for (i in 0 until casetas.length()) {
            val item = casetas.getJSONObject(i)
            val codigo = item.getString("codigo")
            assertFalse("No debe haber códigos de caseta duplicados", codigos.contains(codigo))
            codigos.add(codigo)
            mapaModulos[codigo] = item.getString("modulos")
        }

        assertEquals("1 y 5", mapaModulos["SUR"])
        assertEquals("3", mapaModulos["CENTRAL"])
        assertEquals("4", mapaModulos["CENOTE"])
    }

    @Test
    fun formateadorLocal_estructuraReporteCorrectamenteSinIA() {
        val salida = LocalReportFormatter.generarPlantillaBase(
            tipo = TipoReporte.INICIO_TURNO,
            casetaNombre = "Caseta Sur",
            modulos = "1 y 5",
            guardiaNombre = "Víctor Herrera",
            turno = "Nocturno",
            fechaHora = "26/11/2026 22:00",
            detalleUsuario = "revisión de perímetro completada"
        )

        assertTrue(salida.contains("Playa del Carmen, Quintana Roo"))
        assertTrue(salida.contains("INICIO DE TURNO"))
        assertTrue(salida.contains("Caseta Sur (Módulos: 1 y 5)"))
        assertTrue(salida.contains("Revisión de perímetro completada."))
    }
}
