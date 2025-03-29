package com.example.mercedes_celular.ui.fragment

import android.os.Bundle
import androidx.navigation.NavDirections
import com.example.mercedes_celular.R
import kotlin.Int
import kotlin.String

public class ProductListFragmentDirections private constructor() {
  private data class ActionProductListFragmentToProductFormFragment(
    public val productId: String = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_productListFragment_to_productFormFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("productId", this.productId)
        return result
      }
  }

  public companion object {
    public fun actionProductListFragmentToProductFormFragment(productId: String = ""): NavDirections
        = ActionProductListFragmentToProductFormFragment(productId)
  }
}
