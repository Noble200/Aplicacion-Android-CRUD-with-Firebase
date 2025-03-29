package com.example.mercedes_celular.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseConfig {
    private const val TAG = "FirebaseConfig"
    private var initialized = false
    private lateinit var db: FirebaseFirestore

    fun initialize(context: Context) {
        if (initialized) return

        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.i(TAG, "Firebase inicializado correctamente")
            }

            db = FirebaseFirestore.getInstance()
            initialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar Firebase: ${e.message}", e)
            throw e
        }
    }

    fun getFirestore(): FirebaseFirestore {
        if (!initialized) {
            throw IllegalStateException("Firebase no ha sido inicializado. Llame a initialize() primero.")
        }
        return db
    }
}