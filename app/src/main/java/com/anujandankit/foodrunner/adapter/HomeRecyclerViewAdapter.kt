package com.anujandankit.foodrunner.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.activity.DescriptionActivity
import com.anujandankit.foodrunner.database.RestaurantEntity
import com.anujandankit.foodrunner.fragment.HomeFragment
import com.anujandankit.foodrunner.model.Restaurant
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class HomeRecyclerViewAdapter(val context: Context, val itemList: ArrayList<Restaurant>) :
    RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeViewHolder>() {
    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val restaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val restaurantImage: ImageView = view.findViewById(R.id.imgFoodImage)
        val pricePerPerson: TextView = view.findViewById(R.id.txtPrice)
        val restaurantRating: TextView = view.findViewById(R.id.txtRestaurantRating)
        val favoriteRestaurant: ImageView = view.findViewById(R.id.imgFavoriteImage)
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_home_row, parent, false)
        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val restaurant = itemList[position]
        holder.restaurantName.text = restaurant.restaurantName
        Picasso.get().load(restaurant.restaurantImageUrl).error(R.drawable.ic_error_black_24dp)
            .into(holder.restaurantImage);
        holder.pricePerPerson.text = "₹${restaurant.restaurantCostForOne}/person"
        holder.restaurantRating.text = restaurant.restaurantRating
        val restaurantEntity = RestaurantEntity(
            restaurant.restaurantId,
            restaurant.restaurantName,
            restaurant.restaurantRating,
            restaurant.restaurantCostForOne,
            restaurant.restaurantImageUrl
        )
        val checkFav = HomeFragment.DBAsyncTask(context as Activity, restaurantEntity, 1).execute()
        val isFav = checkFav.get()
        if (isFav) {
            holder.favoriteRestaurant.setImageResource(R.drawable.ic_favorite_colored_24dp)
        } else {
            holder.favoriteRestaurant.setImageResource(R.drawable.ic_favorite_outlined_colored_24dp)
        }
        holder.favoriteRestaurant.setOnClickListener {
            if (!HomeFragment.DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val async = HomeFragment.DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(
                        context,
                        "Restaurant Added To Favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                    notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        context as Activity,
                        "Some Error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                    notifyDataSetChanged()
                }
            } else {
                val async = HomeFragment.DBAsyncTask(context, restaurantEntity, 3).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(
                        context as Activity,
                        "Restaurant Removed From Favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                    notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        context,
                        "Some Error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                    notifyDataSetChanged()
                }
            }
        }
        holder.cardView.setOnClickListener {
            val intentToDescription = Intent(context, DescriptionActivity::class.java)
            intentToDescription.putExtra("restaurant_id", restaurant.restaurantId.toInt())
            intentToDescription.putExtra("restaurant_name", restaurant.restaurantName)
            intentToDescription.putExtra("restaurant_rating", restaurant.restaurantRating)
            intentToDescription.putExtra("restaurant_cost_for_one", restaurant.restaurantCostForOne)
            intentToDescription.putExtra("restaurant_image_url", restaurant.restaurantImageUrl)
            context.startActivity(intentToDescription)
        }
    }
}