package com.example.mercedes_celular

import android.app.Application
import android.util.Log
import com.example.mercedes_celular.firebase.FirebaseConfig

class StockControlApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseConfig.initialize(this)
            Log.d("StockControlApplication", "Firebase inicializado correctamente")
        } catch (e: Exception) {
            Log.e("StockControlApplication", "Error al inicializar Firebase: ${e.message}", e)
        }
    }
}