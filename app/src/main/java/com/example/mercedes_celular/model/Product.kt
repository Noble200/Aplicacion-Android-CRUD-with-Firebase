package com.example.mercedes_celular.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Clase que representa un producto en el inventario
 */
data class Product(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var quantity: Int = 0,
    var price: Double = 0.0,
    var category: String = "",
    @ServerTimestamp
    var lastUpdated: Date? = null
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this("", "", 0, 0.0, "", null)

    companion object {
        /**
         * Convierte un mapa de Firestore a un objeto Product
         */
        fun fromMap(map: Map<String, Any>, id: String? = null): Product {
            val product = Product()
            id?.let { product.id = it }
            map["name"]?.let { product.name = it as String }
            map["quantity"]?.let { product.quantity = (it as Number).toInt() }
            map["price"]?.let { product.price = (it as Number).toDouble() }
            map["category"]?.let { product.category = it as String }
            map["last_updated"]?.let {
                product.lastUpdated = when (it) {
                    is Timestamp -> it.toDate()
                    is Date -> it
                    else -> Date()
                }
            }
            return product
        }
    }

    /**
     * Convierte el objeto Product a un mapa para Firestore
     */
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "name" to name,
            "quantity" to quantity,
            "price" to price,
            "category" to category
            // No incluimos last_updated ya que Firestore lo manejará con ServerTimestamp
        )
    }

    override fun toString(): String {
        return "$name (ID: $id) - Cantidad: $quantity, Precio: $${String.format("%.2f", price)}"
    }
}