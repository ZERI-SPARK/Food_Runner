package com.anujandankit.foodrunner.model

import org.json.JSONArray

data class OrderDetails(
    val orderId: String,
    val restaurantName: String,
    val orderPlacedAt: String,
    val totalCost: String,
    val foodItems: JSONArray
)
