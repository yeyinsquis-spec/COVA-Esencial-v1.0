package com.cova.esencial.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cova.esencial.data.local.entities.ReporteEntity
import com.cova.esencial.data.templates.LocalReportFormatter
import com.cova.esencial.data.templates.TipoReporte
import com.cova.esencial.ui.components.ConfirmacionBorradoDialog
import com.cova.esencial.ui.components.CovaSubScreenTopBar
import com.cova.esencial.ui.components.copiarAlPortapapeles
import com.cova.esencial.ui.theme.*
import com.cova.esencial.viewmodel.CovaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportesScreen(
    uiState: CovaUiState,
    onGuardarReporte: (String, String, String, String) -> Unit,
    onEliminarReporte: (Long) -> Unit,
    onMostrarMensaje: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val caseta = uiState.casetaActiva

    var tipoSeleccionado by remember { mutableStateOf(TipoReporte.INICIO_TURNO) }
    var guardiaNombre by remember(uiState.configuracion.nombreGuardiaDefault) {
        mutableStateOf(uiState.configuracion.nombreGuardiaDefault)
    }
    var turno by remember(uiState.configuracion.turnoDefault) {
        mutableStateOf(uiState.configuracion.turnoDefault)
    }
    var detalle by remember { mutableStateOf("") }
    var acciones by remember { mutableStateOf("") }
    var vistaPreviaEditada by remember { mutableStateOf("") }
    var reporteAEliminar by remember { mutableStateOf<ReporteEntity?>(null) }

    fun regenerarTexto() {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX")).format(Date())
        vistaPreviaEditada = LocalReportFormatter.generarPlantillaBase(
            tipo = tipoSeleccionado,
            casetaNombre = caseta?.nombre ?: "Sin caseta",
            modulos = caseta?.modulos ?: "-",
            guardiaNombre = guardiaNombre,
            turno = turno,
            fechaHora = fecha,
            detalleUsuario = detalle,
            accionesTomadas = acciones
        )
    }

    LaunchedEffect(tipoSeleccionado, caseta?.codigo) {
        regenerarTexto()
    }

    if (reporteAEliminar != null) {
        ConfirmacionBorradoDialog(
            titulo = "Eliminar reporte",
            mensaje = "¿Deseas eliminar este reporte guardado del historial local?",
            onConfirmar = {
                onEliminarReporte(reporteAEliminar!!.id)
                reporteAEliminar = null
            },
            onCancelar = { reporteAEliminar = null }
        )
    }

    Scaffold(
        containerColor = CovaBgDark,
        topBar = {
            CovaSubScreenTopBar(
                titulo = "REPORTES",
                subtitulo = caseta?.let { "${it.nombre} · Módulos ${it.modulos}" },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("TIPO DE REPORTE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CovaTextSecondary)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TipoChip(TipoReporte.INICIO_TURNO, tipoSeleccionado, Modifier.weight(1f)) { tipoSeleccionado = it }
                    TipoChip(TipoReporte.CIERRE_TURNO, tipoSeleccionado, Modifier.weight(1f)) { tipoSeleccionado = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TipoChip(TipoReporte.NOVEDADES, tipoSeleccionado, Modifier.weight(1f)) { tipoSeleccionado = it }
                    TipoChip(TipoReporte.INCIDENTES, tipoSeleccionado, Modifier.weight(1f)) { tipoSeleccionado = it }
                }
            }

            OutlinedTextField(
                value = guardiaNombre,
                onValueChange = { guardiaNombre = it },
                label = { Text("Nombre del elemento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = turno,
                onValueChange = { turno = it },
                label = { Text("Turno (ej. Nocturno / Diurno)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = detalle,
                onValueChange = { detalle = it },
                label = { Text("Detalle operativo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (tipoSeleccionado == TipoReporte.INCIDENTES) {
                OutlinedTextField(
                    value = acciones,
                    onValueChange = { acciones = it },
                    label = { Text("Acciones tomadas") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            OutlinedButton(
                onClick = {
                    regenerarTexto()
                    onMostrarMensaje("Estructura aplicada y redacción ordenada.", false)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ordenar y aplicar plantilla", color = CovaGreenPrimary, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = vistaPreviaEditada,
                onValueChange = { vistaPreviaEditada = it },
                label = { Text("Texto final del reporte (editable)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 7
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        onGuardarReporte(tipoSeleccionado.code, guardiaNombre, turno, vistaPreviaEditada)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CovaGreenPrimary),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = CovaBgDark)
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar", color = CovaBgDark, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        copiarAlPortapapeles(context, "Reporte COVA", vistaPreviaEditada)
                        onMostrarMensaje("Reporte copiado al portapapeles.", false)
                    },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CovaTextPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("Copiar", color = CovaTextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = CovaBorder)

            Text(
                text = "HISTORIAL DE REPORTES (${uiState.reportes.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CovaTextSecondary
            )

            if (uiState.reportes.isEmpty()) {
                Text("Aún no hay reportes guardados en este dispositivo.", color = CovaTextSecondary, fontSize = 13.sp)
            } else {
                uiState.reportes.forEach { rep ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
                        border = BorderStroke(1.dp, CovaBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${TipoReporte.fromCode(rep.tipo).titulo} · ${rep.casetaNombre}",
                                    color = CovaGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(text = rep.fechaHora, color = CovaTextSecondary, fontSize = 12.sp)
                            }
                            Text(text = rep.contenidoFormateado, color = CovaTextPrimary, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        copiarAlPortapapeles(context, "Reporte COVA", rep.contenidoFormateado)
                                        onMostrarMensaje("Reporte histórico copiado.", false)
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copiar")
                                }
                                OutlinedButton(
                                    onClick = { reporteAEliminar = rep }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = CovaDanger, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Eliminar", color = CovaDanger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TipoChip(
    tipo: TipoReporte,
    actual: TipoReporte,
    modifier: Modifier = Modifier,
    onSelect: (TipoReporte) -> Unit
) {
    val selected = tipo == actual
    FilterChip(
        selected = selected,
        onClick = { onSelect(tipo) },
        label = { Text(tipo.titulo, fontSize = 11.sp, maxLines = 1) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CovaGreenPrimary,
            selectedLabelColor = CovaBgDark,
            containerColor = CovaSurfaceDark,
            labelColor = CovaTextPrimary
        ),
        modifier = modifier
    )
}
