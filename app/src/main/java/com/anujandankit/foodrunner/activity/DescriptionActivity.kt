package com.anujandankit.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.adapter.MenuItemRecyclerViewAdapter
import com.anujandankit.foodrunner.database.MenuItemEntity
import com.anujandankit.foodrunner.database.OrderEntity
import com.anujandankit.foodrunner.database.RestaurantDatabase
import com.anujandankit.foodrunner.database.RestaurantEntity
import com.anujandankit.foodrunner.fragment.HomeFragment
import com.anujandankit.foodrunner.model.MenuItem
import com.anujandankit.foodrunner.util.ConnectionManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_description.*
import org.json.JSONException

class DescriptionActivity : AppCompatActivity() {
    var restaurantId: Int? = 2901
    var restaurantName: String? = ""
    var restaurantRating: String? = ""
    var restaurantImageUrl: String? = ""
    var restaurantCostForOne: String? = ""
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var recyclerAdapter: MenuItemRecyclerViewAdapter
    lateinit var progressBar: ProgressBar
    val menuItemList = arrayListOf<MenuItem>()
    private var orderList = arrayListOf<MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        proceed_button.visibility = View.GONE

        if (intent != null) {
            restaurantId = intent.getIntExtra("restaurant_id", 2901)
            restaurantName = intent.getStringExtra("restaurant_name")
            restaurantRating = intent.getStringExtra("restaurant_rating")
            restaurantCostForOne = intent.getStringExtra("restaurant_cost_for_one")
            restaurantImageUrl = intent.getStringExtra("restaurant_image_url")
            supportActionBar?.title = restaurantName
        } else {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected error occurred",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (restaurantId == 2901) {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected error occurred",
                Toast.LENGTH_SHORT
            ).show()
        }

        proceed_button.setOnClickListener {

            /*Here we see the implementation of Gson.
            * Whenever we want to convert the custom data types into simple data types
            * which can be transferred across for utility purposes, we will use Gson*/
            val gson = Gson()

            /*With the below code, we convert the list of order items into simple string which can be easily stored in DB*/
            val foodItems = gson.toJson(orderList)

            val async =
                ItemsOfCart(
                    this@DescriptionActivity,
                    restaurantId.toString(),
                    foodItems,
                    1
                ).execute()
            val result = async.get()
            if (result) {
                val data = Bundle()
                data.putInt("resId", restaurantId as Int)
                data.putString("resName", restaurantName)
                val intent = Intent(this@DescriptionActivity, CartActivity::class.java)
                intent.putExtra("data", data)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@DescriptionActivity,
                    "Some unexpected error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        recyclerView = findViewById(R.id.recycler_view_description)
        layoutManager = LinearLayoutManager(this@DescriptionActivity)
        progressBar = findViewById(R.id.progress_circular_description)

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/$restaurantId"
        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {
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
                                    val menuItemJSONObject = data.getJSONObject(i)
                                    val menuItemObject = MenuItem(
                                        menuItemJSONObject.getString("id"),
                                        menuItemJSONObject.getString("name"),
                                        menuItemJSONObject.getString("cost_for_one"),
                                        menuItemJSONObject.getString("restaurant_id")
                                    )
                                    menuItemList.add(menuItemObject)
                                    recyclerAdapter =
                                        MenuItemRecyclerViewAdapter(
                                            this@DescriptionActivity,
                                            menuItemList,
                                            object :
                                                MenuItemRecyclerViewAdapter.OnItemClickListener {
                                                override fun onAddItemClick(menuItem: MenuItem) {
                                                    orderList.add(menuItem)
                                                    if (orderList.isNotEmpty()) {
                                                        proceed_button.visibility = View.VISIBLE
                                                    }
                                                }

                                                override fun onRemoveItemClick(menuItem: MenuItem) {
                                                    orderList.remove(menuItem)
                                                    if (orderList.isEmpty()) {
                                                        proceed_button.visibility = View.GONE
                                                    }
                                                }
                                            }
                                        )
                                    recyclerView.adapter = recyclerAdapter
                                    recyclerView.layoutManager = layoutManager
                                }
                            } else {
                                val errorMessage = dataObject.getString("errorMessage")
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    coordinatorViewDescription,
                                    errorMessage,
                                    Snackbar.LENGTH_INDEFINITE
                                ).show()
                            }
                        } catch (e: JSONException) {
                            progressBar.visibility = View.GONE
                            Snackbar.make(
                                coordinatorViewDescription,
                                "Some unexpected error has occurred while we were handling the data.",
                                Snackbar.LENGTH_INDEFINITE
                            ).show()
                        }
                    },
                    Response.ErrorListener {
                        progressBar.visibility = View.GONE
                        Snackbar.make(
                            coordinatorViewDescription,
                            "We failed to fetch the data. Please Retry.",
                            Snackbar.LENGTH_INDEFINITE
                        ).show()

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
            Toast.makeText(
                this@DescriptionActivity,
                "No Internet Connection Found.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        val restaurantEntity = RestaurantEntity(
            restaurantId.toString(),
            restaurantName.toString(),
            restaurantRating.toString(),
            restaurantCostForOne.toString(),
            restaurantImageUrl.toString()
        )
        val checkFav =
            HomeFragment.DBAsyncTask(this@DescriptionActivity, restaurantEntity, 1).execute()
        val isFav = checkFav.get()
        if (isFav) {
            imgFavoriteImageDescription.setImageResource(R.drawable.ic_favorite_colored_24dp)
        } else {
            imgFavoriteImageDescription.setImageResource(R.drawable.ic_favorite_outlined_colored_24dp)
        }
        imgFavoriteImageDescription.setOnClickListener {
            if (!HomeFragment.DBAsyncTask(this@DescriptionActivity, restaurantEntity, 1)
                    .execute()
                    .get()
            ) {
                val async =
                    HomeFragment.DBAsyncTask(this@DescriptionActivity, restaurantEntity, 2)
                        .execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(
                        this@DescriptionActivity,
                        "Restaurant Added To Favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                    imgFavoriteImageDescription.setImageResource(R.drawable.ic_favorite_colored_24dp)
                } else {
                    Toast.makeText(
                        this@DescriptionActivity,
                        "Some Error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val async =
                    HomeFragment.DBAsyncTask(this@DescriptionActivity, restaurantEntity, 3)
                        .execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(
                        this@DescriptionActivity,
                        "Restaurant Removed From Favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                    imgFavoriteImageDescription.setImageResource(R.drawable.ic_favorite_outlined_colored_24dp)
                } else {
                    Toast.makeText(
                        this@DescriptionActivity,
                        "Some Error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        DeleteMenuItems(this@DescriptionActivity).execute()
        super.onBackPressed()
    }

    class DBAsyncTask(
        val context: Context,
        private val menuItemEntity: MenuItemEntity,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {
        /*
        Mode 1- > Item Added or Not
        Mode 2 -> Add to cart
        Mode 3 -> Remove from cart
         */
        private val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "menu-items-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val restaurant: MenuItemEntity? = db.menuItemDao()
                        .getMenuItemById(menuItemEntity.menuItemId)
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    db.menuItemDao().insertMenuItem(menuItemEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.menuItemDao().deleteMenuItem(menuItemEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    class ItemsOfCart(
        context: Context,
        val restaurantId: String,
        private val foodItems: String,
        private val mode: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        /* 1-> Insert Order
           2-> Delete Order
         */
        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }

                2 -> {
                    db.orderDao().deleteOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }
            }

            return false
        }
    }

    class DeleteMenuItems(val context: Context) :
        AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            val db =
                Room.databaseBuilder(context, RestaurantDatabase::class.java, "menu-items-db")
                    .build()
            db.menuItemDao().deleteAllMenuItems()
            db.close()
            return null
        }
    }
}