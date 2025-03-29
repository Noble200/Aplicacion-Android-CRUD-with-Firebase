package com.example.mercedes_celular.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mercedes_celular.databinding.FragmentProductFormBinding
import com.example.mercedes_celular.model.Product
import com.example.mercedes_celular.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

class ProductFormFragment : Fragment() {
    private var _binding: FragmentProductFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var productViewModel: ProductViewModel
    private val TAG = "ProductFormFragment"

    // Usar navArgs() para manejar los argumentos de manera segura
    private val args by navArgs<ProductFormFragmentArgs>()

    private var currentProduct: Product? = null
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupViewModel()
            determineMode()
            setupButtons()
        } catch (e: Exception) {
            Log.e(TAG, "Error en onViewCreated: ${e.message}", e)
            Toast.makeText(context, "Error al inicializar formulario: ${e.message}", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        }
    }

    private fun setupViewModel() {
        try {
            productViewModel = ViewModelProvider(requireActivity())[ProductViewModel::class.java]
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar ViewModel: ${e.message}", e)
            throw e
        }
    }

    private fun determineMode() {
        try {
            // Obtener el ID del producto desde los argumentos seguros
            val productId = args.productId

            // Si el ID no está vacío, estamos en modo edición
            isEditMode = productId.isNotEmpty()

            binding.textFormTitle.text = if (isEditMode) "Editar Producto" else "Nuevo Producto"
            binding.buttonSave.text = if (isEditMode) "Actualizar" else "Guardar"

            if (isEditMode) {
                loadProduct(productId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al determinar modo: ${e.message}", e)
            throw e
        }
    }

    private fun loadProduct(productId: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val product = productViewModel.getProductById(productId)
                if (product != null) {
                    currentProduct = product
                    populateForm(product)
                } else {
                    Toast.makeText(context, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar producto: ${e.message}", e)
                Toast.makeText(context, "Error al cargar producto: ${e.message}", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } finally {
                if (isAdded()) {  // Verificar que el fragmento siga adjunto a la actividad
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun populateForm(product: Product) {
        try {
            binding.editTextName.setText(product.name)
            binding.editTextQuantity.setText(product.quantity.toString())
            binding.editTextPrice.setText(product.price.toString())
            binding.editTextCategory.setText(product.category)
        } catch (e: Exception) {
            Log.e(TAG, "Error al llenar formulario: ${e.message}", e)
            Toast.makeText(context, "Error al mostrar datos: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupButtons() {
        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonSave.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        if (!validateForm()) {
            return
        }

        try {
            val name = binding.editTextName.text.toString().trim()
            val quantityStr = binding.editTextQuantity.text.toString().trim()
            val priceStr = binding.editTextPrice.text.toString().trim()
            val category = binding.editTextCategory.text.toString().trim()

            val quantity = quantityStr.toIntOrNull() ?: 0
            val price = priceStr.toDoubleOrNull() ?: 0.0

            binding.progressBar.visibility = View.VISIBLE

            if (isEditMode && currentProduct != null) {
                // Crear un nuevo objeto producto con los valores actualizados
                val updatedProduct = currentProduct!!.copy(
                    name = name,
                    quantity = quantity,
                    price = price,
                    category = category
                )

                productViewModel.updateProduct(
                    updatedProduct,
                    onSuccess = {
                        Toast.makeText(context, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        if (isAdded()) {  // Verificar que el fragmento siga adjunto
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                )
            } else {
                // Crear un nuevo producto
                val newProduct = Product(
                    name = name,
                    quantity = quantity,
                    price = price,
                    category = category
                )

                productViewModel.addProduct(
                    newProduct,
                    onSuccess = {
                        Toast.makeText(context, "Producto añadido correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        if (isAdded()) {  // Verificar que el fragmento siga adjunto
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar producto: ${e.message}", e)
            Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val name = binding.editTextName.text.toString().trim()
        if (name.isEmpty()) {
            binding.editTextName.error = "El nombre es obligatorio"
            isValid = false
        }

        val quantityStr = binding.editTextQuantity.text.toString().trim()
        if (quantityStr.isEmpty()) {
            binding.editTextQuantity.error = "La cantidad es obligatoria"
            isValid = false
        } else {
            val quantity = quantityStr.toIntOrNull()
            if (quantity == null || quantity < 0) {
                binding.editTextQuantity.error = "La cantidad debe ser un número entero positivo"
                isValid = false
            }
        }

        val priceStr = binding.editTextPrice.text.toString().trim()
        if (priceStr.isEmpty()) {
            binding.editTextPrice.error = "El precio es obligatorio"
            isValid = false
        } else {
            val price = priceStr.toDoubleOrNull()
            if (price == null || price < 0) {
                binding.editTextPrice.error = "El precio debe ser un número positivo"
                isValid = false
            }
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}