package com.example.mercedes_celular.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mercedes_celular.databinding.ItemProductBinding
import com.example.mercedes_celular.model.Product
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para mostrar productos en un RecyclerView
 */
class ProductAdapter(
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                textProductId.text = if (product.id.length > 8)
                    "${product.id.substring(0, 8)}..." else product.id
                textProductName.text = product.name
                textProductQuantity.text = product.quantity.toString()
                textProductPrice.text = String.format("$%.2f", product.price)
                textProductCategory.text = product.category

                // Formatear fecha
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                textProductLastUpdated.text = product.lastUpdated?.let { dateFormat.format(it) } ?: ""

                // Configurar botones
                buttonEdit.setOnClickListener { onEditClick(product) }
                buttonDelete.setOnClickListener { onDeleteClick(product) }
            }
        }
    }

    /**
     * DiffUtil para optimizar las actualizaciones de la lista
     */
    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}