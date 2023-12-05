package com.anujandankit.foodrunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.adapter.HomeRecyclerViewAdapter
import com.anujandankit.foodrunner.database.RestaurantDatabase
import com.anujandankit.foodrunner.database.RestaurantEntity
import com.anujandankit.foodrunner.model.Restaurant
import com.anujandankit.foodrunner.util.ConnectionManager
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */

class HomeFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var recyclerAdapter: HomeRecyclerViewAdapter
    lateinit var progressBar: ProgressBar
    val restaurantList = arrayListOf<Restaurant>()
    private var ratingComparator =
        Comparator<Restaurant> { restaurant1, restaurant2 ->

            if (restaurant1.restaurantRating.compareTo(restaurant2.restaurantRating, true) == 0) {
                restaurant1.restaurantName.compareTo(restaurant2.restaurantName, true)
            } else {
                restaurant1.restaurantRating.compareTo(restaurant2.restaurantRating, true)
            }
        }
    private var costForOneComparator =
        Comparator<Restaurant> { restaurant1, restaurant2 ->

            if (restaurant1.restaurantCostForOne.compareTo(
                    restaurant2.restaurantCostForOne,
                    true
                ) == 0
            ) {
                restaurant1.restaurantName.compareTo(restaurant2.restaurantName, true)
            } else {
                restaurant1.restaurantCostForOne.compareTo(restaurant2.restaurantCostForOne, true)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_home)
        layoutManager = LinearLayoutManager(activity)
        progressBar = view.findViewById(R.id.progress_circular_home)
        setHasOptionsMenu(true)
        if (activity != null) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/"
            if (ConnectionManager().checkConnectivity(activity as Context)) {
                val jsonObjectRequest =
                    object : JsonObjectRequest(
                        Method.GET, url,
                        null,
                        Response.Listener {
                            try {
                                val dataObject = it.getJSONObject("data")
                                val success = dataObject.getBoolean("success")
                                if (success) {
                                    progressBar.visibility = View.GONE
                                    val data = dataObject.getJSONArray("data")
                                    for (i in 0 until data.length()) {
                                        val restaurantJSONObject = data.getJSONObject(i)
                                        val restaurantObject = Restaurant(
                                            restaurantJSONObject.getString("id"),
                                            restaurantJSONObject.getString("name"),
                                            restaurantJSONObject.getString("rating"),
                                            restaurantJSONObject.getString("cost_for_one"),
                                            restaurantJSONObject.getString("image_url")
                                        )
                                        restaurantList.add(restaurantObject)
                                        recyclerAdapter =
                                            HomeRecyclerViewAdapter(
                                                activity as Context,
                                                restaurantList
                                            )
                                        recyclerView.adapter = recyclerAdapter
                                        recyclerView.layoutManager = layoutManager
                                    }
                                } else {
                                    val errorMessage = dataObject.getString("errorMessage")
                                    progressBar.visibility = View.GONE
                                    Snackbar.make(
                                        view,
                                        errorMessage,
                                        Snackbar.LENGTH_INDEFINITE
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    view,
                                    "Some unexpected error has occurred while we were handling the data.",
                                    Snackbar.LENGTH_INDEFINITE
                                ).show()
                            }
                        },
                        Response.ErrorListener {
                            if (activity != null) {
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    view, "We failed to fetch the data. Please Retry.",
                                    Snackbar.LENGTH_INDEFINITE
                                ).show()
                            }
                        }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "2d020a6c927f14"
                            return headers
                        }
                    }
                queue.add(jsonObjectRequest)
            } else {
                val dialog = AlertDialog.Builder(activity as Context)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection Not Found.")
                dialog.setPositiveButton("Open Settings") { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    activity?.finish()
                }
                dialog.setNegativeButton("Exit") { _, _ ->
                    ActivityCompat.finishAffinity(activity as Activity)
                }
                dialog.create().show()
            }
        }
        return view
    }

    fun search(string: String?) {
        if (string?.length == 0) {
            recyclerView.adapter = recyclerAdapter
        } else {
            val myDataArrayList: ArrayList<Restaurant> = arrayListOf()
            for (data: Restaurant in restaurantList) {
                if (data.restaurantName.toLowerCase(Locale.getDefault())
                        .contains(string.toString().toLowerCase(Locale.getDefault()))
                ) {
                    myDataArrayList.add(data)
                }
            }
            val recyclerAdapter = HomeRecyclerViewAdapter(
                activity as Context,
                myDataArrayList
            )
            recyclerView.adapter = recyclerAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)


        val searchItem = menu.findItem(R.id.action_search)
        val searchView: androidx.appcompat.widget.SearchView =
            searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                return true
            }
        })

        // Define the listener
        val expandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Do something when action item collapses
                recyclerAdapter = HomeRecyclerViewAdapter(
                    activity as Context,
                    restaurantList
                )
                recyclerView.adapter = recyclerAdapter
                recyclerAdapter.notifyDataSetChanged()
                val sortMenu = menu.findItem(R.id.action_sort)
                sortMenu.isEnabled = true
                sortMenu.setIcon(R.drawable.ic_sort_white_24dp)
                return true // Return true to collapse action view
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Do something when expanded
                val sortMenu = menu.findItem(R.id.action_sort)
                sortMenu.isEnabled = false
                sortMenu.setIcon(R.drawable.ic_sort_white_disabled)
                return true // Return true to expand action view
            }
        }

        // Assign the listener to that action item
        searchItem?.setOnActionExpandListener(expandListener)
        // Assign the listener to that action item
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_sort -> {
                if (recyclerView.isNotEmpty()) {
                    val builder = AlertDialog.Builder(activity as Activity)
                    builder.setTitle("Sort By")
                        .setSingleChoiceItems(
                            arrayOf(
                                "Cost (Low to High)",
                                "Cost (High to Low)",
                                "Rating (Low to High)",
                                "Rating (High to Low)"
                            ),
                            -1
                        ) { dialogInterface, i ->
                            when (i) {
                                0 -> {
                                    Collections.sort(recyclerAdapter.itemList, costForOneComparator)
                                    recyclerAdapter.notifyDataSetChanged()
                                }
                                1 -> {
                                    Collections.sort(recyclerAdapter.itemList, costForOneComparator)
                                    restaurantList.reverse()
                                    recyclerAdapter.notifyDataSetChanged()
                                }
                                2 -> {
                                    Collections.sort(recyclerAdapter.itemList, ratingComparator)
                                    recyclerAdapter.notifyDataSetChanged()

                                }
                                3 -> {
                                    Collections.sort(recyclerAdapter.itemList, ratingComparator)
                                    restaurantList.reverse()
                                    recyclerAdapter.notifyDataSetChanged()
                                }
                            }
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton(
                            "Cancel"
                        ) { dialogInterface, i -> dialogInterface.dismiss() }

                    builder.create().show()
                } else {
                    Toast.makeText(
                        activity as Context,
                        "Data has not been fully fetched yet. Please wait till data loads.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                return true
            }
            R.id.action_search -> {
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    class DBAsyncTask(
        val context: Context,
        private val restaurantEntity: RestaurantEntity,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {
        /*
        Mode 1- > Favorite or not
        Mode 2 -> Save as favorite
        Mode 3 -> Remove from favorite
         */
        private val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurantId)
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    override fun onResume() {
        if (recyclerView.isNotEmpty()) {
            recyclerAdapter.notifyDataSetChanged()
        }
        super.onResume()
    }
}
