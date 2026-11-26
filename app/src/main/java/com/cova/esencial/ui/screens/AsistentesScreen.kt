package com.cova.esencial.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cova.esencial.ui.components.CovaSubScreenTopBar
import com.cova.esencial.ui.components.copiarAlPortapapeles
import com.cova.esencial.ui.theme.*
import com.cova.esencial.viewmodel.CovaUiState

@Composable
fun AsistentesScreen(
    uiState: CovaUiState,
    onMostrarMensaje: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val caseta = uiState.casetaActiva

    val pasosChecklist = remember {
        listOf(
            "Verificar equipo de radio y batería al iniciar",
            "Confirmar estado de puertas, accesos y luminarias",
            "Revisar bitácora física y consignas vigentes",
            "Realizar rondines programados en los módulos asignados",
            "Registrar novedades e incidencias antes del relevo"
        )
    }
    val marcados = remember { mutableStateMapOf<Int, Boolean>() }

    val mensajeRelevoRapido = remember(caseta) {
        "Playa del Carmen, Quintana Roo.\nAVISO OPERATIVO — ${caseta?.nombre ?: \"Caseta\"}\nMódulos (${caseta?.modulos ?: \"-\"}) verificados y operando sin novedad."
    }

    Scaffold(
        containerColor = CovaBgDark,
        topBar = {
            CovaSubScreenTopBar(
                titulo = "ASISTENTES DE TURNO",
                subtitulo = "Apoyo rápido offline · Sin IA",
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
            Card(
                colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
                border = BorderStroke(1.dp, CovaBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LISTA DE VERIFICACIÓN DE TURNO",
                        color = CovaGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    pasosChecklist.forEachIndexed { idx, texto ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = marcados[idx] == true,
                                onCheckedChange = { marcados[idx] = it },
                                colors = CheckboxDefaults.colors(checkedColor = CovaGreenPrimary)
                            )
                            Text(text = texto, color = CovaTextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CovaSurfaceDark),
                border = BorderStroke(1.dp, CovaBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TEXTO RÁPIDO SIN NOVEDAD",
                        color = CovaGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(text = mensajeRelevoRapido, color = CovaTextPrimary, fontSize = 13.sp)
                    OutlinedButton(
                        onClick = {
                            copiarAlPortapapeles(context, "Aviso rápido COVA", mensajeRelevoRapido)
                            onMostrarMensaje("Aviso rápido copiado al portapapeles.", false)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CovaGreenPrimary)
                        Spacer(Modifier.width(6.dp))
                        Text("Copiar aviso rápido", color = CovaGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
