package com.anujandankit.foodrunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orderItems")
data class OrderEntity(
    @ColumnInfo(name = "order_food_restaurant_id") val restaurantId: String,
    @PrimaryKey val foodItem: String
)