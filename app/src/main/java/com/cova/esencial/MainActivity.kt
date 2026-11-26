package com.cova.esencial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cova.esencial.ui.navigation.CovaNavGraph
import com.cova.esencial.ui.theme.CovaTheme
import com.cova.esencial.viewmodel.CovaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CovaApplication
        setContent {
            CovaTheme {
                val vm: CovaViewModel = viewModel(
                    factory = CovaViewModel.Factory(app.repository)
                )
                CovaNavGraph(viewModel = vm)
            }
        }
    }
}
