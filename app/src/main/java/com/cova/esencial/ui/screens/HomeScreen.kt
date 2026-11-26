package com.cova.esencial.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cova.esencial.data.local.entities.CasetaEntity
import com.cova.esencial.ui.components.OfflineStatusBadge
import com.cova.esencial.ui.theme.*
import com.cova.esencial.viewmodel.CovaUiState

@Composable
fun HomeScreen(
    uiState: CovaUiState,
    onSelectCaseta: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        containerColor = CovaBgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera COVA + Configuración + Estado Offline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COVA",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = CovaTextPrimary
                    )
                    Text(
                        text = "Asistencia Operativa · v1.0",
                        fontSize = 13.sp,
                        color = CovaTextSecondary
                    )
                }
                IconButton(
                    onClick = { onNavigate("configuracion") },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = CovaSurfaceDark
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = CovaTextPrimary
                    )
                }
            }

            OfflineStatusBadge()

            // Selector de Caseta
            Card(
                colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
                border = BorderStroke(1.dp, CovaBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CASETA OPERATIVA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CovaTextSecondary
                        )
                        val activa = uiState.casetaActiva
                        if (activa != null) {
                            Text(
                                text = "Módulos: ${activa.modulos}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CovaGreenPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.casetas.forEach { caseta: CasetaEntity ->
                            val selected = caseta.codigo == uiState.casetaActiva?.codigo
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectCaseta(caseta.codigo) },
                                label = {
                                    Text(
                                        text = caseta.nombre.removePrefix("Caseta "),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CovaGreenPrimary,
                                    selectedLabelColor = CovaBgDark,
                                    containerColor = CovaSurfaceElevated,
                                    labelColor = CovaTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Cuadrícula de 2 columnas con los 4 accesos principales
            Text(
                text = "MÓDULOS PRINCIPALES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CovaTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridCard(
                    titulo = "REPORTES",
                    descripcion = "Inicio, cierre, novedades e incidentes",
                    icono = Icons.Default.Description,
                    onClick = { onNavigate("reportes") },
                    modifier = Modifier.weight(1f)
                )
                MenuGridCard(
                    titulo = "RECORRIDOS",
                    descripcion = "Plantillas por caseta e historial",
                    icono = Icons.Default.DirectionsWalk,
                    onClick = { onNavigate("recorridos") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridCard(
                    titulo = "MANUAL",
                    descripcion = "Consignas, protocolos y búsqueda",
                    icono = Icons.Default.MenuBook,
                    onClick = { onNavigate("manual") },
                    modifier = Modifier.weight(1f)
                )
                MenuGridCard(
                    titulo = "ASISTENTES\nDE TURNO",
                    descripcion = "Guías rápidas y listas de apoyo",
                    icono = Icons.Default.Checklist,
                    onClick = { onNavigate("asistentes") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MenuGridCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(168.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
        border = BorderStroke(1.dp, CovaBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CovaGreenPrimary.copy(alpha = 0.16f)
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = CovaGreenPrimary,
                    modifier = Modifier.padding(10.dp).size(26.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CovaTextPrimary
                )
                Text(
                    text = descripcion,
                    fontSize = 12.sp,
                    color = CovaTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
