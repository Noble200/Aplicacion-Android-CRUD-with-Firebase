package com.example.mercedes_celular

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mercedes_celular.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar la conexión a Firebase
        verifyFirebaseConnection()

        // Configurar la navegación a los diferentes fragmentos
        setupNavigation()
    }

    private fun verifyFirebaseConnection() {
        try {
            // Verificar que Firebase está inicializado correctamente
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                db.collection("products").limit(1).get()
                    .addOnSuccessListener {
                        Log.d(TAG, "Conexión a Firebase exitosa")
                        binding.textStatus.text = "Conectado a Firebase correctamente"
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error en la conexión a Firebase: ${e.message}")
                        binding.textStatus.text = "Error de conexión: ${e.message}"
                    }
            } else {
                Log.e(TAG, "Firebase no está inicializado")
                binding.textStatus.text = "Error: Firebase no está inicializado"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar la conexión: ${e.message}")
            binding.textStatus.text = "Error: ${e.message}"
        }
    }

    private fun setupNavigation() {
        // La navegación se manejará automáticamente a través del
        // NavHostFragment definido en el layout
    }
}