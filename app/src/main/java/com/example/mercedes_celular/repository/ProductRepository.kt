package com.example.mercedes_celular.repository

import com.example.mercedes_celular.model.Product
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar operaciones CRUD de productos en Firestore
 */
class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("products")

    /**
     * Obtiene todos los productos en una lista
     */
    suspend fun getAll(): List<Product> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    Product.fromMap(data, doc.id)
                } else null
            }
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Observa cambios en la colección de productos y emite actualizaciones
     */
    fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val listenerRegistration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        Product.fromMap(data, doc.id)
                    } else null
                }
                trySend(products)
            }
        }

        // Cerrar el listener cuando se cancele el flujo
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Obtiene un producto por su ID
     */
    suspend fun getById(productId: String): Product? {
        return try {
            val doc = collection.document(productId).get().await()
            if (doc.exists() && doc.data != null) {
                Product.fromMap(doc.data!!, doc.id)
            } else null
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Añade un nuevo producto
     */
    suspend fun add(product: Product): Product {
        try {
            val docRef = collection.add(product.toMap()).await()
            product.id = docRef.id
            return product
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Actualiza un producto existente
     */
    suspend fun update(product: Product): Product {
        try {
            if (product.id.isEmpty()) {
                throw IllegalArgumentException("El producto debe tener un ID para actualizarlo")
            }

            collection.document(product.id).update(product.toMap()).await()
            return product
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Elimina un producto por su ID
     */
    suspend fun delete(productId: String): Boolean {
        return try {
            collection.document(productId).delete().await()
            true
        } catch (e: Exception) {
            throw e
        }
    }
}