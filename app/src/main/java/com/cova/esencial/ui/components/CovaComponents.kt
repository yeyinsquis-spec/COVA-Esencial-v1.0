package com.cova.esencial.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cova.esencial.ui.theme.*

fun copiarAlPortapapeles(context: Context, etiqueta: String, texto: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(etiqueta, texto)
    clipboard.setPrimaryClip(clip)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CovaSubScreenTopBar(
    titulo: String,
    subtitulo: String? = null,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = CovaTextPrimary
                )
                if (!subtitulo.isNullOrBlank()) {
                    Text(
                        text = subtitulo,
                        fontSize = 12.sp,
                        color = CovaGreenPrimary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver a inicio",
                    tint = CovaTextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CovaSurfaceDark
        )
    )
}

@Composable
fun OfflineStatusBadge() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CovaSurfaceElevated,
        border = BorderStroke(1.dp, CovaGreenPrimary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(CovaGreenPrimary, CircleShape)
            )
            Text(
                text = "MODO OFFLINE ACTIVO · DATOS LOCALES",
                color = CovaGreenPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ConfirmacionBorradoDialog(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = CovaSurfaceDark,
        title = { Text(titulo, color = CovaTextPrimary, fontWeight = FontWeight.Bold) },
        text = { Text(mensaje, color = CovaTextSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = CovaDanger)
            ) {
                Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancelar) {
                Text("Cancelar", color = CovaTextPrimary)
            }
        }
    )
}
