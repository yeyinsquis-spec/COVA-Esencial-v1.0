package com.cova.esencial.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cova.esencial.ui.components.CovaSubScreenTopBar
import com.cova.esencial.ui.theme.*
import com.cova.esencial.viewmodel.CovaUiState

@Composable
fun ManualScreen(
    uiState: CovaUiState,
    onBack: () -> Unit
) {
    val caseta = uiState.casetaActiva
    var busqueda by remember { mutableStateOf("") }
    var filtroAlcance by remember { mutableStateOf("TODAS") } // TODAS, GENERAL, CASETA

    val consignasFiltradas = remember(uiState.consignas, caseta?.codigo, busqueda, filtroAlcance) {
        uiState.consignas.filter { item ->
            val coincideCaseta = when (filtroAlcance) {
                "GENERAL" -> item.casetaCodigo == "GENERAL"
                "CASETA" -> item.casetaCodigo == caseta?.codigo
                else -> item.casetaCodigo == "GENERAL" || item.casetaCodigo == caseta?.codigo
            }
            val q = busqueda.trim()
            val coincideTexto = q.isEmpty() ||
                item.titulo.contains(q, ignoreCase = true) ||
                item.contenido.contains(q, ignoreCase = true) ||
                item.categoria.contains(q, ignoreCase = true)
            coincideCaseta && coincideTexto
        }
    }

    Scaffold(
        containerColor = CovaBgDark,
        topBar = {
            CovaSubScreenTopBar(
                titulo = "MANUAL Y CONSIGNAS",
                subtitulo = caseta?.let { "Consulta offline · ${it.nombre}" },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar consigna, protocolo o procedimiento") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("TODAS" to "Todas", "GENERAL" to "Generales", "CASETA" to "De Caseta").forEach { (clave, etiqueta) ->
                    val selected = filtroAlcance == clave
                    FilterChip(
                        selected = selected,
                        onClick = { filtroAlcance = clave },
                        label = { Text(etiqueta) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CovaGreenPrimary,
                            selectedLabelColor = CovaBgDark,
                            containerColor = CovaSurfaceDark,
                            labelColor = CovaTextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (consignasFiltradas.isEmpty()) {
                Text(
                    text = "No se encontraron consignas con ese criterio.",
                    color = CovaTextSecondary,
                    fontSize = 14.sp
                )
            } else {
                consignasFiltradas.forEach { consigna ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
                        border = BorderStroke(1.dp, CovaBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${consigna.categoria} · ${consigna.casetaCodigo}",
                                color = CovaGreenPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = consigna.titulo,
                                color = CovaTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = consigna.contenido,
                                color = CovaTextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
