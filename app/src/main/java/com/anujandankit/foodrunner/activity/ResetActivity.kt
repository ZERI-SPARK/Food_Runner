package com.anujandankit.foodrunner.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.activity_reset.*
import org.json.JSONException
import org.json.JSONObject

class ResetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset)
        resetProgressBar.visibility = View.GONE
        if (intent != null) {
            val details = intent.getBundleExtra("details")
            val data = details?.getString("data")
            if (data == "resetting") {
                val mobileNumber = details.getString("phone_number")

                button_submit.setOnClickListener {
                    hideKeyboard()
                    editText_otp_layout.error = null
                    editText_pass_layout.error = null
                    editText_confirm_pass_layout.error = null
                    if (validate()) {
                        editText_otp_layout.visibility = View.INVISIBLE
                        editText_pass_layout.visibility = View.INVISIBLE
                        editText_confirm_pass_layout.visibility = View.INVISIBLE
                        button_submit.isEnabled = false
                        resetProgressBar.visibility = View.VISIBLE

                        val queue = Volley.newRequestQueue(this@ResetActivity)
                        val url = "http://13.235.250.119/v2/reset_password/fetch_result"

                        val jsonParams = JSONObject()
                        jsonParams.put("mobile_number", mobileNumber.toString())
                        jsonParams.put("otp", editText_otp.text.toString())
                        jsonParams.put("password", editText_pass.text.toString())
                        Log.d("JSON",jsonParams.toString())
                        if (ConnectionManager().checkConnectivity(this@ResetActivity)) {
                            val jsonObjectRequest =
                                object : JsonObjectRequest(
                                    Method.POST, url,
                                    jsonParams,
                                    Response.Listener {
                                        try {
                                            val dataObject = it.getJSONObject("data")
                                            val success = dataObject.getBoolean("success")
                                            if (success) {
                                                val successMessage =
                                                    dataObject.getString("successMessage")
                                                Toast.makeText(
                                                    this@ResetActivity,
                                                    successMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                val intentToLoginActivity =
                                                    Intent(
                                                        this@ResetActivity,
                                                        LoginActivity::class.java
                                                    )
                                                intentToLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                intentToLoginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                intentToLoginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                intentToLoginActivity.putExtra("EXIT", true)
                                                startActivity(intentToLoginActivity)
                                                finish()

                                            } else {
                                                editText_otp_layout.visibility = View.VISIBLE
                                                editText_pass_layout.visibility = View.VISIBLE
                                                editText_confirm_pass_layout.visibility =
                                                    View.VISIBLE
                                                button_submit.isEnabled = true
                                                resetProgressBar.visibility = View.GONE
                                                val errorMessage =
                                                    dataObject.getString("errorMessage")
                                                Toast.makeText(
                                                    this@ResetActivity,
                                                    errorMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish()
                                            }
                                        } catch (e: JSONException) {
                                            editText_otp_layout.visibility = View.VISIBLE
                                            editText_pass_layout.visibility = View.VISIBLE
                                            editText_confirm_pass_layout.visibility = View.VISIBLE
                                            button_submit.isEnabled = true
                                            resetProgressBar.visibility = View.GONE
                                            Snackbar.make(
                                                coordinatorViewReset,
                                                "Some unexpected error has occurred while we were handling the data.",
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    Response.ErrorListener {
                                        editText_otp_layout.visibility = View.VISIBLE
                                        editText_pass_layout.visibility = View.VISIBLE
                                        editText_confirm_pass_layout.visibility = View.VISIBLE
                                        button_submit.isEnabled = true
                                        resetProgressBar.visibility = View.GONE
                                        Snackbar.make(
                                            coordinatorViewReset,
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
                            editText_otp_layout.visibility = View.VISIBLE
                            editText_pass_layout.visibility = View.VISIBLE
                            editText_confirm_pass_layout.visibility = View.VISIBLE
                            button_submit.isEnabled = true
                            resetProgressBar.visibility = View.GONE
                            Snackbar.make(
                                coordinatorViewReset,
                                "No Internet Connection.",
                                Snackbar.LENGTH_LONG
                            ).setAction("Retry") { button_submit.performClick() }.show()
                        }
                    } else {
                        if (editText_otp.text?.length != 4) {
                            editText_otp_layout.error = "OTP length is of 4 disits"
                            Snackbar.make(
                                coordinatorViewReset,
                                "Invalid OTP",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else if (editText_pass.text.toString() != editText_confirm_pass.text.toString()) {
                            editText_pass_layout.error = "Passwords don't match"
                            editText_confirm_pass_layout.error = "Passwords don't match"
                            Snackbar.make(
                                coordinatorViewReset,
                                "Passwords Don't Match",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else if (editText_pass.text!!.length <= 5) {
                            editText_pass_layout.error = "Weak Password"
                            Snackbar.make(
                                coordinatorViewReset,
                                "Weak Password",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }


            }

        } else {
            Toast.makeText(
                this@ResetActivity,
                "Some Unexpected Error Occurred. Please Retry.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun validate(): Boolean =
        editText_otp.text?.length == 4 && editText_pass.text!!.length > 5 &&
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
}
