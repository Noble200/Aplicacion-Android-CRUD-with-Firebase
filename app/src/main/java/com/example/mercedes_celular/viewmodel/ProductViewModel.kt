package com.example.mercedes_celular.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mercedes_celular.model.Product
import com.example.mercedes_celular.repository.ProductRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar los datos de productos
 */
class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val TAG = "ProductViewModel"

    // LiveData para la lista de productos
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para errores
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // LiveData para el filtrado
    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    // Texto de búsqueda actual
    private var searchText = ""

    init {
        // Iniciar la observación de productos
        observeProducts()
    }

    /**
     * Observa los cambios en la colección de productos de Firestore
     */
    private fun observeProducts() {
        _isLoading.value = true
        try {
            repository.observeProducts()
                .onEach { productList ->
                    _products.value = productList
                    applyFilter(searchText) // Aplicar el filtro actual a la nueva lista
                    _isLoading.value = false
                }
                .catch { e ->
                    Log.e(TAG, "Error observando productos: ${e.message}", e)
                    _error.value = "Error al cargar productos: ${e.message}"
                    _isLoading.value = false
                }
                .launchIn(viewModelScope)
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar observación: ${e.message}", e)
            _error.value = "Error al iniciar observación: ${e.message}"
            _isLoading.value = false
        }
    }

    /**
     * Carga los productos manualmente (como fallback si la observación falla)
     */
    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val productList = repository.getAll()
                _products.value = productList
                applyFilter(searchText)
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando productos: ${e.message}", e)
                _error.value = "Error al cargar productos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Añade un nuevo producto
     */
    fun addProduct(product: Product, onSuccess: (Product) -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val addedProduct = repository.add(product)
                _isLoading.value = false
                onSuccess(addedProduct)
                // No es necesario actualizar _products ya que la observación lo hará
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(TAG, "Error añadiendo producto: ${e.message}", e)
                onError("Error al añadir producto: ${e.message}")
            }
        }
    }

    /**
     * Actualiza un producto existente
     */
    fun updateProduct(product: Product, onSuccess: (Product) -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val updatedProduct = repository.update(product)
                _isLoading.value = false
                onSuccess(updatedProduct)
                // No es necesario actualizar _products ya que la observación lo hará
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(TAG, "Error actualizando producto: ${e.message}", e)
                onError("Error al actualizar producto: ${e.message}")
            }
        }
    }

    /**
     * Elimina un producto
     */
    fun deleteProduct(productId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val success = repository.delete(productId)
                if (success) {
                    _isLoading.value = false
                    onSuccess()
                    // No es necesario actualizar _products ya que la observación lo hará
                } else {
                    _isLoading.value = false
                    onError("No se pudo eliminar el producto")
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(TAG, "Error eliminando producto: ${e.message}", e)
                onError("Error al eliminar producto: ${e.message}")
            }
        }
    }

    /**
     * Filtra productos basado en texto de búsqueda
     */
    fun filterProducts(query: String) {
        try {
            searchText = query.lowercase()
            applyFilter(searchText)
        } catch (e: Exception) {
            Log.e(TAG, "Error al filtrar productos: ${e.message}", e)
            _error.value = "Error al filtrar productos: ${e.message}"
        }
    }

    private fun applyFilter(query: String) {
        try {
            val currentList = _products.value ?: return

            if (query.isEmpty()) {
                _filteredProducts.value = currentList
            } else {
                val filtered = currentList.filter { product ->
                    product.name.lowercase().contains(query) ||
                            product.category.lowercase().contains(query)
                }
                _filteredProducts.value = filtered
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al aplicar filtro: ${e.message}", e)
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Obtiene un producto por su ID
     */
    suspend fun getProductById(productId: String): Product? {
        try {
            _isLoading.value = true
            return repository.getById(productId).also {
                _isLoading.value = false
            }
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e(TAG, "Error obteniendo producto por ID: ${e.message}", e)
            _error.value = "Error al obtener producto: ${e.message}"
            return null
        }
    }
}