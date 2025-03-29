package com.example.mercedes_celular.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mercedes_celular.adapter.ProductAdapter
import com.example.mercedes_celular.databinding.FragmentProductListBinding
import com.example.mercedes_celular.model.Product
import com.example.mercedes_celular.viewmodel.ProductViewModel

class ProductListFragment : Fragment() {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productViewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter
    private val TAG = "ProductListFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupSearchView()
        setupObservers()
        setupFab()
        setupSwipeRefresh()
    }

    private fun setupViewModel() {
        try {
            productViewModel = ViewModelProvider(requireActivity())[ProductViewModel::class.java]
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar ViewModel: ${e.message}", e)
            Toast.makeText(context, "Error de inicialización: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            productAdapter = ProductAdapter(
                onEditClick = { product: Product ->
                    navigateToProductForm(product.id)
                },
                onDeleteClick = { product: Product ->
                    showDeleteConfirmationDialog(product)
                }
            )

            binding.recyclerProducts.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this@ProductListFragment.productAdapter
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar RecyclerView: ${e.message}", e)
            Toast.makeText(context, "Error al configurar lista: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    productViewModel.filterProducts(newText ?: "")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al filtrar: ${e.message}", e)
                }
                return true
            }
        })
    }

    private fun setupObservers() {
        // Observar la lista filtrada de productos
        productViewModel.filteredProducts.observe(viewLifecycleOwner, Observer { products: List<Product> ->
            updateProductList(products)
        })

        // Observar el estado de carga
        productViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading: Boolean ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        })

        // Observar errores
        productViewModel.error.observe(viewLifecycleOwner, Observer { error: String? ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                productViewModel.clearError()
            }
        })
    }

    private fun setupFab() {
        binding.fabAddProduct.setOnClickListener {
            navigateToProductForm(null)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            try {
                productViewModel.loadProducts()
            } catch (e: Exception) {
                Log.e(TAG, "Error al recargar productos: ${e.message}", e)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun updateProductList(products: List<Product>) {
        try {
            productAdapter.submitList(products)
            binding.textNoProducts.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar lista: ${e.message}", e)
        }
    }

    private fun navigateToProductForm(productId: String?) {
        try {
            // Usar la clase generada por SafeArgs
            val action = ProductListFragmentDirections.actionProductListFragmentToProductFormFragment(productId ?: "")
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Error en la navegación: ${e.message}", e)
            Toast.makeText(context, "Error al abrir formulario: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        try {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar ${product.name}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    deleteProduct(product)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar diálogo: ${e.message}", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteProduct(product: Product) {
        productViewModel.deleteProduct(
            productId = product.id,
            onSuccess = {
                Toast.makeText(context, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show()
            },
            onError = { error: String ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}