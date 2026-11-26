package com.cova.esencial.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cova.esencial.data.local.entities.CasetaEntity
import com.cova.esencial.ui.components.ConfirmacionBorradoDialog
import com.cova.esencial.ui.components.CovaSubScreenTopBar
import com.cova.esencial.ui.theme.*
import com.cova.esencial.viewmodel.CovaUiState
import kotlinx.coroutines.launch

@Composable
fun ConfiguracionScreen(
    uiState: CovaUiState,
    onGuardarPreferencias: (String, String) -> Unit,
    onGuardarCaseta: (String, String, String) -> Unit,
    onEliminarCaseta: (CasetaEntity) -> Unit,
    onGenerarJsonRespaldo: suspend () -> String,
    onImportarJsonRespaldo: (String) -> Unit,
    onMostrarMensaje: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var guardiaDefault by remember(uiState.configuracion.nombreGuardiaDefault) {
        mutableStateOf(uiState.configuracion.nombreGuardiaDefault)
    }
    var turnoDefault by remember(uiState.configuracion.turnoDefault) {
        mutableStateOf(uiState.configuracion.turnoDefault)
    }
    var nuevoCodigo by remember { mutableStateOf("") }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevosModulos by remember { mutableStateOf("") }
    var casetaAEliminar by remember { mutableStateOf<CasetaEntity?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = onGenerarJsonRespaldo()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    onMostrarMensaje("Respaldo exportado correctamente.", false)
                } catch (e: Exception) {
                    onMostrarMensaje("Error al exportar respaldo: ${e.message}", true)
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val contenido = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                if (contenido != null) {
                    onImportarJsonRespaldo(contenido)
                } else {
                    onMostrarMensaje("No se pudo leer el archivo seleccionado.", true)
                }
            } catch (e: Exception) {
                onMostrarMensaje("Error al abrir respaldo: ${e.message}", true)
            }
        }
    }

    if (casetaAEliminar != null) {
        ConfirmacionBorradoDialog(
            titulo = "Eliminar caseta",
            mensaje = "¿Confirmas eliminar ${casetaAEliminar!!.nombre}?",
            onConfirmar = {
                onEliminarCaseta(casetaAEliminar!!)
                casetaAEliminar = null
            },
            onCancelar = { casetaAEliminar = null }
        )
    }

    Scaffold(
        containerColor = CovaBgDark,
        topBar = {
            CovaSubScreenTopBar(
                titulo = "CONFIGURACIÓN",
                subtitulo = "Casetas, preferencias y respaldos",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preferencias del elemento
            Card(
                colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
                border = BorderStroke(1.dp, CovaBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("DATOS PREDETERMINADOS DEL ELEMENTO", color = CovaGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = guardiaDefault,
                        onValueChange = { guardiaDefault = it },
                        label = { Text("Nombre predeterminado") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = turnoDefault,
                        onValueChange = { turnoDefault = it },
                        label = { Text("Turno predeterminado") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = { onGuardarPreferencias(guardiaDefault, turnoDefault) },
                        colors = ButtonDefaults.buttonColors(containerColor = CovaGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar preferencias", color = CovaBgDark, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Administración de casetas y módulos
            Card(
                colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
                border = BorderStroke(1.dp, CovaBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CASETAS Y MÓDULOS", color = CovaGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    uiState.casetas.forEach { c ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${c.nombre} (${c.codigo})", color = CovaTextPrimary, fontWeight = FontWeight.Bold)
                                Text("Módulos: ${c.modulos}", color = CovaTextSecondary, fontSize = 12.sp)
                            }
                            IconButton(onClick = { casetaAEliminar = c }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar caseta", tint = CovaDanger)
                            }
                        }
                    }

                    HorizontalDivider(color = CovaBorder)

                    OutlinedTextField(
                        value = nuevoCodigo,
                        onValueChange = { nuevoCodigo = it },
                        label = { Text("Código (ej. NORTE)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre (ej. Caseta Norte)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nuevosModulos,
                        onValueChange = { nuevosModulos = it },
                        label = { Text("Módulos asignados (ej. 2 y 6)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedButton(
                        onClick = {
                            onGuardarCaseta(nuevoCodigo, nuevoNombre, nuevosModulos)
                            nuevoCodigo = ""
                            nuevoNombre = ""
                            nuevosModulos = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar o actualizar caseta", color = CovaGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Respaldo y recuperación
            Card(
                colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
                border = BorderStroke(1.dp, CovaBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("RESPALDO Y RECUPERACIÓN LOCAL", color = CovaGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = "Exporta tus reportes, recorridos y casetas a un archivo JSON en tu teléfono o restaura un respaldo previo sin internet.",
                        color = CovaTextSecondary,
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = { exportLauncher.launch("respaldo_cova_v1.json") },
                        colors = ButtonDefaults.buttonColors(containerColor = CovaGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = CovaBgDark)
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar respaldo (.json)", color = CovaBgDark, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = CovaTextPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text("Importar respaldo (.json)", color = CovaTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
