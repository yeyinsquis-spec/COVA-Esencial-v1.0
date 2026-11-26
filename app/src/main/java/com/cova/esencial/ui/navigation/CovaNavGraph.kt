package com.cova.esencial.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cova.esencial.ui.screens.*
import com.cova.esencial.ui.theme.CovaDanger
import com.cova.esencial.ui.theme.CovaGreenDark
import com.cova.esencial.viewmodel.CovaViewModel
import kotlinx.coroutines.delay

@Composable
fun CovaNavGraph(viewModel: CovaViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.mensajeEstado) {
        if (uiState.mensajeEstado != null) {
            delay(3500)
            viewModel.limpiarMensaje()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    uiState = uiState,
                    onSelectCaseta = viewModel::seleccionarCaseta,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable("reportes") {
                ReportesScreen(
                    uiState = uiState,
                    onGuardarReporte = viewModel::guardarReporte,
                    onEliminarReporte = viewModel::eliminarReporte,
                    onMostrarMensaje = viewModel::mostrarMensaje,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("recorridos") {
                RecorridosScreen(
                    uiState = uiState,
                    onSelectCaseta = viewModel::seleccionarCaseta,
                    onGuardarRecorrido = viewModel::guardarRecorrido,
                    onEliminarRecorrido = viewModel::eliminarRecorrido,
                    onMostrarMensaje = viewModel::mostrarMensaje,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("manual") {
                ManualScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("asistentes") {
                AsistentesScreen(
                    uiState = uiState,
                    onMostrarMensaje = viewModel::mostrarMensaje,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("configuracion") {
                ConfiguracionScreen(
                    uiState = uiState,
                    onGuardarPreferencias = viewModel::guardarPreferencias,
                    onGuardarCaseta = viewModel::guardarCaseta,
                    onEliminarCaseta = viewModel::eliminarCaseta,
                    onGenerarJsonRespaldo = viewModel::generarJsonRespaldo,
                    onImportarJsonRespaldo = viewModel::importarJsonRespaldo,
                    onMostrarMensaje = viewModel::mostrarMensaje,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        val mensaje = uiState.mensajeEstado
        if (mensaje != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = if (uiState.esError) CovaDanger else CovaGreenDark,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { viewModel.limpiarMensaje() }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(mensaje)
            }
        }
    }
}
