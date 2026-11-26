package com.cova.esencial

import android.app.Application
import com.cova.esencial.data.local.CovaDatabase
import com.cova.esencial.data.repository.CovaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CovaApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: CovaDatabase by lazy { CovaDatabase.getInstance(this) }
    val repository: CovaRepository by lazy { CovaRepository(this, database) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.ensureInitialDataLoaded()
        }
    }
}
