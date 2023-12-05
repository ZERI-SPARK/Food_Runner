package com.anujandankit.foodrunner.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.adapter.FavoriteRecyclerViewAdapter
import com.anujandankit.foodrunner.database.RestaurantDatabase
import com.anujandankit.foodrunner.database.RestaurantEntity
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass.
 */
class FavoritesFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var recyclerAdapter: FavoriteRecyclerViewAdapter
    lateinit var progressBar: ProgressBar
    var dbRestaurantList = listOf<RestaurantEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_favorites)
        layoutManager = LinearLayoutManager(activity)
        progressBar = view.findViewById(R.id.progress_circular_favorites)
        dbRestaurantList = RetrieveFavorites(activity as Context).execute().get()
        if (activity != null) {
            if (dbRestaurantList.isNotEmpty()) {
                progressBar.visibility = View.GONE
                recyclerAdapter =
                    FavoriteRecyclerViewAdapter(activity as Context, dbRestaurantList)
                recyclerView.adapter = recyclerAdapter
                recyclerView.layoutManager = layoutManager

            } else {
                progressBar.visibility = View.GONE
                Snackbar.make(view, "No Favorite Restaurants", Snackbar.LENGTH_SHORT)
                    .setAction("Go Back") {
                        requireActivity().onBackPressed()
                    }.show()
            }
        }

        return view
    }

    class RetrieveFavorites(val context: Context) :
        AsyncTask<Void, Void, List<RestaurantEntity>>() {
        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db")
                .build()
            return db.restaurantDao().getAllRestaurants()
        }
    }

    override fun onResume() {
        if (recyclerView.isNotEmpty()) {
            recyclerAdapter.notifyDataSetChanged()
        }
        super.onResume()
    }

}
