package com.anujandankit.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.model.FoodItem

class CartItemRecyclerViewAdapter(private val cartList: ArrayList<FoodItem>, val context: Context) :
    RecyclerView.Adapter<CartItemRecyclerViewAdapter.CartViewHolder>() {
    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cartItemTextName: TextView = view.findViewById(R.id.txtCartItemName)
        val cartItemPrice: TextView = view.findViewById(R.id.txtCartPrice)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_cart_item_row, parent, false)
        return CartViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartObject = cartList[position]
        holder.cartItemTextName.text = cartObject.itemName
        holder.cartItemPrice.text = "₹${cartObject.itemCostForOne?.toString()}"
    }
}