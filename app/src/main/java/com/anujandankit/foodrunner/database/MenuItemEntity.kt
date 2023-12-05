package com.anujandankit.foodrunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menuItems")
data class MenuItemEntity(
    @PrimaryKey val menuItemId: String,
    @ColumnInfo(name = "menu_item_name") val menuItemName: String,
    @ColumnInfo(name = "menu_item_cost_for_one") val menuItemCostForOne: String,
    @ColumnInfo(name = "menu_item_restaurant_id") val menuItemRestaurantId: String
)