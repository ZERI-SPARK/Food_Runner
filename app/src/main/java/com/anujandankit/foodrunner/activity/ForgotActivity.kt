package com.anujandankit.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anujandankit.foodrunner.R
import com.anujandankit.foodrunner.util.ConnectionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_forgot.*
import org.json.JSONException
import org.json.JSONObject

class ForgotActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
        forgotProgressBar.visibility = View.GONE
        button_next.setOnClickListener {
            hideKeyboard()
            editText_phone_layout.error = null
            editText_email_layout.error = null
            if (validate()) {
                editText_phone_layout.visibility = View.INVISIBLE
                editText_email_layout.visibility = View.INVISIBLE
                button_next.isEnabled = false
                forgotProgressBar.visibility = View.VISIBLE

                val queue = Volley.newRequestQueue(this@ForgotActivity)
                val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

                val jsonParams = JSONObject()
                jsonParams.put("mobile_number", editText_phone.text.toString())
                jsonParams.put("email", editText_email.text.toString())

                if (ConnectionManager().checkConnectivity(this@ForgotActivity)) {
                    val jsonObjectRequest =
                        object : JsonObjectRequest(
                            Method.POST, url,
                            jsonParams,
                            Response.Listener {
                                try {
                                    val dataObject = it.getJSONObject("data")
                                    val success = dataObject.getBoolean("success")
                                    if (success) {
                                        val firstTry = dataObject.getBoolean("first_try")
                                        if (!firstTry) {
                                            val intentToResetActivity =
                                                Intent(
                                                    this@ForgotActivity,
                                                    ResetActivity::class.java
                                                )
                                            val bundle = Bundle()
                                            bundle.putString("data", "resetting")
                                            bundle.putString(
                                                "phone_number",
                                                editText_phone.text.toString()
                                            )
                                            intentToResetActivity.putExtra("details", bundle)
                                            startActivity(intentToResetActivity)
                                            finish()
                                        } else {
                                            editText_phone_layout.visibility = View.VISIBLE
                                            editText_email_layout.visibility = View.VISIBLE
                                            button_next.isEnabled = true
                                            forgotProgressBar.visibility = View.GONE
                                            Toast.makeText(
                                                this@ForgotActivity,
                                                "You have exceeded your OTP Limit. Try again in 24 hours.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        }
                                    } else {
                                        editText_phone_layout.visibility = View.VISIBLE
                                        editText_email_layout.visibility = View.VISIBLE
                                        button_next.isEnabled = true
                                        forgotProgressBar.visibility = View.GONE
                                        val errorMessage = dataObject.getString("errorMessage")
                                        Toast.makeText(
                                            this@ForgotActivity,
                                            errorMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                } catch (e: JSONException) {
                                    editText_phone_layout.visibility = View.VISIBLE
                                    editText_email_layout.visibility = View.VISIBLE
                                    button_next.isEnabled = true
                                    forgotProgressBar.visibility = View.GONE
                                    Toast.makeText(
                                        this@ForgotActivity,
                                        "Some unexpected error has occurred while we were handling the data.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            },
                            Response.ErrorListener {
                                editText_phone_layout.visibility = View.VISIBLE
                                editText_email_layout.visibility = View.VISIBLE
                                button_next.isEnabled = true
                                forgotProgressBar.visibility = View.GONE
                                Snackbar.make(
                                    coordinatorViewForgot,
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
                    editText_phone_layout.visibility = View.VISIBLE
                    editText_email_layout.visibility = View.VISIBLE
                    button_next.isEnabled = true
                    forgotProgressBar.visibility = View.GONE
                    Snackbar.make(
                        coordinatorViewForgot,
                        "No Internet Connection.",
                        Snackbar.LENGTH_LONG
                    ).setAction("Retry") { button_next.performClick() }.show()
                }
            } else {
                if (!isValidEmail(editText_email.text.toString())) {
                    editText_email_layout.error = "Invalid Email Address"
                    Snackbar.make(
                        coordinatorViewForgot,
                        "Invalid Email Address",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else if (editText_phone.text?.length != 10) {
                    editText_phone_layout.error = "Incorrect Mobile Number"
                    Snackbar.make(
                        coordinatorViewForgot,
                        "Incorrect Mobile Number",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun validate(): Boolean =
        isValidEmail(editText_email.text.toString()) &&
                editText_phone.text?.length == 10


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
