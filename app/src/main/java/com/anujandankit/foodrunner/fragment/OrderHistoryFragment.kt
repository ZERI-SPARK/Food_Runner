package com.anujandankit.foodrunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.adapter.OrderHistoryRecyclerViewAdapter
import com.anujandankit.foodrunner.model.OrderDetails
import com.anujandankit.foodrunner.util.ConnectionManager
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException

/**
 * A simple [Fragment] subclass.
 */
class OrderHistoryFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var recyclerAdapter: OrderHistoryRecyclerViewAdapter
    lateinit var progressBar: ProgressBar
    val orderHistoryList = arrayListOf<OrderDetails>()

    private val APPLICATION_ID = "com.anujandankit.foodrunner"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_order_history)
        layoutManager = LinearLayoutManager(activity)
        progressBar = view.findViewById(R.id.progress_circular_order_history)
        setHasOptionsMenu(true)
        if (activity != null) {
            val sharedPref: SharedPreferences =
                (context as Activity).getSharedPreferences(APPLICATION_ID, Context.MODE_PRIVATE)
            val userId = sharedPref.getString("user_id", "null")
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"
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
                                    if (data.length() == 0) {
                                        Toast.makeText(context, "No Orders", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        for (i in 0 until data.length()) {
                                            val orderJSONObject = data.getJSONObject(i)
                                            val foodItemsArray =
                                                orderJSONObject.getJSONArray("food_items")
                                            val orderDetails =
                                                OrderDetails(
                                                    orderJSONObject.getString("order_id"),
                                                    orderJSONObject.getString("restaurant_name"),
                                                    orderJSONObject.getString("order_placed_at"),
                                                    orderJSONObject.getString("total_cost"),
                                                    foodItemsArray
                                                )
                                            orderHistoryList.add(orderDetails)
                                            recyclerAdapter = OrderHistoryRecyclerViewAdapter(
                                                activity as Context,
                                                orderHistoryList
                                            )
                                            recyclerView.layoutManager = layoutManager
                                            recyclerView.adapter = recyclerAdapter
                                        }
                                    }

                                    /*for (i in 0 until data.length()) {
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
                                    }*/
                                } else {
                                    val errorMessage = dataObject.getString("errorMessage")
                                    progressBar.visibility = View.GONE
                                    Snackbar.make(
                                        view,
                                        errorMessage,
                                        Snackbar.LENGTH_INDEFINITE
                                    ).show()
                                }
                                queue.cancelAll(this::class.java.simpleName)
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

}
