package com.anujandankit.foodrunner.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.adapter.CartItemRecyclerViewAdapter
import com.anujandankit.foodrunner.database.OrderEntity
import com.anujandankit.foodrunner.database.RestaurantDatabase
import com.anujandankit.foodrunner.model.FoodItem
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_cart.*
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerCart: RecyclerView
    private lateinit var cartItemAdapter: CartItemRecyclerViewAdapter
    private var orderList = ArrayList<FoodItem>()

    private lateinit var rlLoading: ProgressBar

    private lateinit var btnPlaceOrder: Button
    private var resId: Int = 0
    private var resName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        init()
        setupToolbar()
        setUpCartList()
        placeOrder()
    }

    private fun init() {
        rlLoading = findViewById(R.id.progress_circular_cart)
        val bundle = intent.getBundleExtra("data")
        resId = bundle?.getInt("resId", 0) as Int
        resName = bundle.getString("resName", "") as String
        restaurantName.text = resName
    }

    private fun setupToolbar() {
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpCartList() {
        recyclerCart = findViewById(R.id.recycler_view_cart)
        val dbList = GetItemsFromDBAsync(applicationContext).execute().get()


        for (element in dbList) {
            orderList.addAll(
                Gson().fromJson(element.foodItem, Array<FoodItem>::class.java).asList()
            )
        }

        if (orderList.isEmpty()) {
            recyclerCart.visibility = View.GONE
            rlLoading.visibility = View.VISIBLE
        } else {
            recyclerCart.visibility = View.VISIBLE
            rlLoading.visibility = View.GONE
        }

        cartItemAdapter = CartItemRecyclerViewAdapter(orderList, this@CartActivity)
        val mLayoutManager = LinearLayoutManager(this@CartActivity)
        recyclerCart.layoutManager = mLayoutManager
        recyclerCart.itemAnimator = DefaultItemAnimator()
        recyclerCart.adapter = cartItemAdapter
    }


    private fun placeOrder() {
        btnPlaceOrder = findViewById(R.id.place_order_button)

        /*Before placing the order, the user is displayed the price or the items on the button for placing the orders*/
        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].itemCostForOne as Int
        }
        val total = "Place Order(Total: Rs. $sum)"
        btnPlaceOrder.text = total

        btnPlaceOrder.setOnClickListener {
            rlLoading.visibility = View.VISIBLE
            recyclerCart.visibility = View.INVISIBLE
            sendServerRequest()
        }
    }

    private fun sendServerRequest() {
        val queue = Volley.newRequestQueue(this)

        /*Creating the json object required for placing the order*/
        val jsonParams = JSONObject()
        jsonParams.put(
            "user_id",
            this@CartActivity.getSharedPreferences(
                "com.anujandankit.foodrunner",
                Context.MODE_PRIVATE
            ).getString(
                "user_id",
                null
            ) as String
        )
        jsonParams.put("restaurant_id", resId.toString() as String)
        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].itemCostForOne as Int
        }
        jsonParams.put("total_cost", sum.toString())
        val foodArray = JSONArray()
        for (i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].itemId)
            foodArray.put(i, foodId)
        }
        jsonParams.put("food", foodArray)

        val url = "http://13.235.250.119/v2/place_order/fetch_result/"
        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        ClearDBAsync(applicationContext).execute().get()
                        DescriptionActivity.DeleteMenuItems(this@CartActivity).execute()
                        progress_circular_cart.visibility = View.GONE


                        val dialog = Dialog(
                            this@CartActivity,
                            android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
                        )
                        dialog.setContentView(R.layout.order_placed_dialog_layout)
                        dialog.show()
                        dialog.setCancelable(false)
                        val btnOk = dialog.findViewById<Button>(R.id.done)
                        btnOk.setOnClickListener {
                            startActivity(Intent(this@CartActivity, MainActivity::class.java))
                            ActivityCompat.finishAffinity(this@CartActivity)
                            dialog.dismiss()
                        }
                    } else {
                        recyclerCart.visibility = View.VISIBLE
                        Toast.makeText(this@CartActivity, "Some Error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }

                } catch (e: Exception) {
                    recyclerCart.visibility = View.VISIBLE
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                recyclerCart.visibility = View.VISIBLE
                Toast.makeText(this@CartActivity, it.message, Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "2d020a6c927f14"
                    return headers
                }
            }

        queue.add(jsonObjectRequest)

    }


    /*Asynctask class for extracting the items from the database*/
    class GetItemsFromDBAsync(context: Context) : AsyncTask<Void, Void, List<OrderEntity>>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            return db.orderDao().getAllOrders()
        }

    }

    class ClearDBAsync(context: Context) : AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteAllOrders()
            db.close()
            return true
        }

    }

    override fun onBackPressed() {
        ClearDBAsync(applicationContext).execute().get()
        super.onBackPressed()
    }

}