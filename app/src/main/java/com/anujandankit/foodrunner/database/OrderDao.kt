package com.anujandankit.foodrunner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OrderDao {
    @Insert
    fun insertOrder(orderEntity: OrderEntity)

    @Delete
    fun deleteOrder(orderEntity: OrderEntity)

    @Query("SELECT * FROM orderItems")
    fun getAllOrders(): List<OrderEntity>

    @Query("DELETE FROM orderItems")
    fun deleteAllOrders()

}