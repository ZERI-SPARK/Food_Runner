package com.anujandankit.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.util.ConnectionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONException
import org.json.JSONObject


class RegisterActivity : AppCompatActivity() {

    private val APPLICATION_ID = "com.anujandankit.foodrunner"
    private val PREFS_LOGIN_INSTANCE = "loginPref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref: SharedPreferences =
            this.getSharedPreferences(APPLICATION_ID, Context.MODE_PRIVATE)
        setContentView(R.layout.activity_register)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        signUpProgressBar.visibility = View.GONE
        button_register.setOnClickListener {
            hideKeyboard()
            editText_name_layout.error = null
            editText_phone_layout.error = null
            editText_delivery_layout.error = null
            editText_email_layout.error = null
            editText_pass_layout.error = null
            editText_confirm_pass_layout.error = null
            if (validate()) {
                editText_name_layout.visibility = View.INVISIBLE
                editText_phone_layout.visibility = View.INVISIBLE
                editText_delivery_layout.visibility = View.INVISIBLE
                editText_email_layout.visibility = View.INVISIBLE
                editText_pass_layout.visibility = View.INVISIBLE
                editText_confirm_pass_layout.visibility = View.INVISIBLE
                button_register.visibility = View.INVISIBLE
                signUpProgressBar.visibility = View.VISIBLE

                val queue = Volley.newRequestQueue(this@RegisterActivity)
                val url = "http://13.235.250.119/v2/register/fetch_result"

                val jsonParams = JSONObject()
                jsonParams.put("name", editText_name.text.toString())
                jsonParams.put("mobile_number", editText_phone.text.toString())
                jsonParams.put("address", editText_delivery.text.toString())
                jsonParams.put("email", editText_email.text.toString())
                jsonParams.put("password", editText_pass.text.toString())

                if (ConnectionManager().checkConnectivity(this@RegisterActivity)) {
                    val jsonObjectRequest =
                        object : JsonObjectRequest(
                            Method.POST, url,
                            jsonParams,
                            Response.Listener {
                                try {
                                    val dataObject = it.getJSONObject("data")
                                    val success = dataObject.getBoolean("success")
                                    if (success) {
                                        val data = dataObject.getJSONObject("data")

                                        val editor: SharedPreferences.Editor = sharedPref.edit()
                                        editor.putBoolean(PREFS_LOGIN_INSTANCE, true)
                                        editor.putString("user_id", data.getString("user_id"))
                                        editor.putString("name", data.getString("name"))
                                        editor.putString("email", data.getString("email"))
                                        editor.putString(
                                            "mobile_number",
                                            data.getString("mobile_number")
                                        )
                                        editor.putString("address", data.getString("address"))
                                        editor.apply()
                                        val intentToMainActivity =
                                            Intent(this@RegisterActivity, MainActivity::class.java)
                                        startActivity(intentToMainActivity)
                                        finish()

                                    } else {
                                        editText_name_layout.visibility = View.VISIBLE
                                        editText_phone_layout.visibility = View.VISIBLE
                                        editText_delivery_layout.visibility = View.VISIBLE
                                        editText_email_layout.visibility = View.VISIBLE
                                        editText_pass_layout.visibility = View.VISIBLE
                                        editText_confirm_pass_layout.visibility = View.VISIBLE
                                        button_register.visibility = View.VISIBLE
                                        signUpProgressBar.visibility = View.GONE
                                        val errorMessage = dataObject.getString("errorMessage")
                                        Snackbar.make(
                                            coordinatorViewSignUp,
                                            errorMessage,
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: JSONException) {
                                    editText_name_layout.visibility = View.VISIBLE
                                    editText_phone_layout.visibility = View.VISIBLE
                                    editText_delivery_layout.visibility = View.VISIBLE
                                    editText_email_layout.visibility = View.VISIBLE
                                    editText_pass_layout.visibility = View.VISIBLE
                                    editText_confirm_pass_layout.visibility = View.VISIBLE
                                    button_register.visibility = View.VISIBLE
                                    signUpProgressBar.visibility = View.GONE
                                    Snackbar.make(
                                        coordinatorViewSignUp,
                                        "Some unexpected error has occurred while we were handling the data.",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            },
                            Response.ErrorListener {
                                editText_name_layout.visibility = View.VISIBLE
                                editText_phone_layout.visibility = View.VISIBLE
                                editText_delivery_layout.visibility = View.VISIBLE
                                editText_email_layout.visibility = View.VISIBLE
                                editText_pass_layout.visibility = View.VISIBLE
                                editText_confirm_pass_layout.visibility = View.VISIBLE
                                button_register.visibility = View.VISIBLE
                                signUpProgressBar.visibility = View.GONE
                                Snackbar.make(
                                    coordinatorViewSignUp,
                                    "We failed to fetch the data. Please Retry.",
                                    Snackbar.LENGTH_LONG
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
                    editText_name_layout.visibility = View.VISIBLE
                    editText_phone_layout.visibility = View.VISIBLE
                    editText_delivery_layout.visibility = View.VISIBLE
                    editText_email_layout.visibility = View.VISIBLE
                    editText_pass_layout.visibility = View.VISIBLE
                    editText_confirm_pass_layout.visibility = View.VISIBLE
                    button_register.visibility = View.VISIBLE
                    signUpProgressBar.visibility = View.GONE
                    Snackbar.make(
                        coordinatorViewSignUp,
                        "No Internet Connection.",
                        Snackbar.LENGTH_LONG
                    ).setAction("Retry") { button_register.performClick() }.show()
                }
            } else {
                if (editText_name.text!!.isEmpty()) {
                    editText_name_layout.error = "Invalid Name"
                    Snackbar.make(
                        coordinatorViewSignUp,
                        "Invalid Name",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else if (!isValidEmail(editText_email.text.toString())) {
                    editText_email_layout.error = "Invalid Email Address"
                    Snackbar.make(
                        coordinatorViewSignUp,
                        "Invalid Email Address",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else if (editText_phone.text?.length != 10) {
                    editText_phone_layout.error = "Incorrect Mobile Number"
                    Snackbar.make(
                        coordinatorViewSignUp,
                        "Incorrect Mobile Number",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else if ((editText_delivery.text!!.isEmpty())) {
                    editText_delivery_layout.error = "Invalid Address"
                    Snackbar.make(
                        coordinatorViewSignUp,
                        "Invalid Address",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else if (editText_pass.text.toString() != editText_confirm_pass.text.toString()) {
                    editText_pass_layout.error = "Passwords don't match"
                    editText_confirm_pass_layout.error = "Passwords don't match"
                    Snackbar.make(
                        coordinatorViewSignUp,
                        "Passwords Don't Match",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else if (editText_pass.text!!.length <= 5) {
                    editText_pass_layout.error = "Weak Password"
                    Snackbar.make(
                        coordinatorViewSignUp,
                        "Weak Password",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun validate(): Boolean = editText_name.text!!.isNotEmpty() &&
            isValidEmail(editText_email.text.toString()) &&
            editText_phone.text?.length == 10 &&
            editText_delivery.text!!.isNotEmpty() && editText_pass.text!!.length > 5 &&
            editText_pass.text.toString() == editText_confirm_pass.text.toString()


    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null && view.windowToken != null) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target)
            .matches()
    }
}
