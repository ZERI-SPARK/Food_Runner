package com.anujandankit.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.model.FoodItem
import com.anujandankit.foodrunner.model.OrderDetails
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderHistoryRecyclerViewAdapter(
    val context: Context,
    private val orderHistoryList: ArrayList<OrderDetails>
) :
    RecyclerView.Adapter<OrderHistoryRecyclerViewAdapter.OrderHistoryViewHolder>() {
    class OrderHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val restaurantName: TextView = view.findViewById(R.id.restaurantName)
        val orderPlaced: TextView = view.findViewById(R.id.orderTime)
        val orderTotal: TextView = view.findViewById(R.id.totalPrice)
        val recyclerResHistory: RecyclerView = view.findViewById(R.id.recyclerResHistoryItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.recycler_view_order_history_row, parent, false)
        return OrderHistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderHistoryList.size
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val orderHistory = orderHistoryList[position]
        holder.orderTotal.text = "₹${orderHistory.totalCost}"
        holder.restaurantName.text = orderHistory.restaurantName
        holder.orderPlaced.text = formatDate(orderHistory.orderPlacedAt)
        setUpRecyclerView(holder.recyclerResHistory, orderHistory)
    }

    private fun setUpRecyclerView(
        recyclerResHistory: RecyclerView,
        orderHistoryList: OrderDetails
    ) {
        val foodItemsList = ArrayList<FoodItem>()
        for (i in 0 until orderHistoryList.foodItems.length()) {
            val foodJson = orderHistoryList.foodItems.getJSONObject(i)
            foodItemsList.add(
                FoodItem(
                    foodJson.getString("food_item_id"),
                    foodJson.getString("name"),
                    foodJson.getString("cost").toInt()
                )
            )
        }
        val cartItemAdapter = CartItemRecyclerViewAdapter(foodItemsList, context)
        val mLayoutManager = LinearLayoutManager(context)
        recyclerResHistory.layoutManager = mLayoutManager
        recyclerResHistory.itemAnimator = DefaultItemAnimator()
        recyclerResHistory.adapter = cartItemAdapter
    }

    private fun formatDate(dateString: String): String? {
        val inputFormatter = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.ENGLISH)
        val date: Date = inputFormatter.parse(dateString) as Date

        val outputFormatter = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.ENGLISH)
        return outputFormatter.format(date)
    }

}