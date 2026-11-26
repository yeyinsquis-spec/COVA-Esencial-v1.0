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
import com.cova.esencial.data.local.entities.RecorridoEntity
import com.cova.esencial.ui.components.ConfirmacionBorradoDialog
import com.cova.esencial.ui.components.CovaSubScreenTopBar
import com.cova.esencial.ui.components.copiarAlPortapapeles
import com.cova.esencial.ui.theme.*
import com.cova.esencial.viewmodel.CovaUiState

@Composable
fun RecorridosScreen(
    uiState: CovaUiState,
    onSelectCaseta: (String) -> Unit,
    onGuardarRecorrido: (String) -> Unit,
    onEliminarRecorrido: (Long) -> Unit,
    onMostrarMensaje: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val caseta = uiState.casetaActiva
    val plantillasCaseta = remember(uiState.plantillasRecorrido, caseta?.codigo) {
        uiState.plantillasRecorrido.filter { it.casetaCodigo == caseta?.codigo }
    }

    var textoRecorrido by remember { mutableStateOf("") }
    var recorridoAEliminar by remember { mutableStateOf<RecorridoEntity?>(null) }

    LaunchedEffect(caseta?.codigo, plantillasCaseta) {
        val plantillaDefault = plantillasCaseta.firstOrNull()?.contenido
            ?: caseta?.let {
                "Playa del Carmen, Quintana Roo.
RECORRIDO DE VIGILANCIA — ${it.nombre.uppercase()}
- Módulos (${it.modulos}): Verificados sin novedad.
Estado general: SIN NOVEDAD."
            }.orEmpty()
        textoRecorrido = plantillaDefault
    }

    if (recorridoAEliminar != null) {
        ConfirmacionBorradoDialog(
            titulo = "Eliminar recorrido",
            mensaje = "¿Deseas eliminar este registro de recorrido del historial?",
            onConfirmar = {
                onEliminarRecorrido(recorridoAEliminar!!.id)
                recorridoAEliminar = null
            },
            onCancelar = { recorridoAEliminar = null }
        )
    }

    Scaffold(
        containerColor = CovaBgDark,
        topBar = {
            CovaSubScreenTopBar(
                titulo = "RECORRIDOS",
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
            Text("SELECCIÓN DE CASETA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CovaTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                uiState.casetas.forEach { item ->
                    val selected = item.codigo == caseta?.codigo
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCaseta(item.codigo) },
                        label = { Text(item.nombre.removePrefix("Caseta ")) },
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

            if (plantillasCaseta.isNotEmpty()) {
                Text("PLANTILLAS DISPONIBLES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CovaTextSecondary)
                plantillasCaseta.forEach { plantilla ->
                    OutlinedButton(
                        onClick = {
                            textoRecorrido = plantilla.contenido
                            onMostrarMensaje("Plantilla cargada.", false)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(plantilla.titulo, color = CovaGreenPrimary)
                    }
                }
            }

            OutlinedTextField(
                value = textoRecorrido,
                onValueChange = { textoRecorrido = it },
                label = { Text("Texto del recorrido (editable)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onGuardarRecorrido(textoRecorrido) },
                    colors = ButtonDefaults.buttonColors(containerColor = CovaGreenPrimary),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = CovaBgDark)
                    Spacer(Modifier.width(6.dp))
                    Text("Registrar", color = CovaBgDark, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        copiarAlPortapapeles(context, "Recorrido COVA", textoRecorrido)
                        onMostrarMensaje("Recorrido copiado al portapapeles.", false)
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
                text = "HISTORIAL DE RECORRIDOS (${uiState.recorridos.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CovaTextSecondary
            )

            if (uiState.recorridos.isEmpty()) {
                Text("No hay recorridos registrados todavía.", color = CovaTextSecondary, fontSize = 13.sp)
            } else {
                uiState.recorridos.forEach { rec ->
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
                                    text = "${rec.casetaNombre} (Mód. ${rec.modulos})",
                                    color = CovaGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(text = rec.fechaHora, color = CovaTextSecondary, fontSize = 12.sp)
                            }
                            Text(text = rec.contenido, color = CovaTextPrimary, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        copiarAlPortapapeles(context, "Recorrido COVA", rec.contenido)
                                        onMostrarMensaje("Recorrido copiado.", false)
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copiar")
                                }
                                OutlinedButton(onClick = { recorridoAEliminar = rec }) {
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
