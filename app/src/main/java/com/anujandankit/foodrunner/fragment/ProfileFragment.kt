package com.anujandankit.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anujandankit.foodrunner.R
import kotlinx.android.synthetic.main.fragment_profile.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    private val APPLICATION_ID = "com.anujandankit.foodrunner"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        if (activity != null) {
            val sharedPref: SharedPreferences =
                (context as Activity).getSharedPreferences(APPLICATION_ID, Context.MODE_PRIVATE)
            view.userName.text = sharedPref.getString("name", "null")
            view.emailAddress.text = sharedPref.getString("email", "null")
            view.userPhone.text = sharedPref.getString("mobile_number", "null")
            view.postalAddress.text = sharedPref.getString("address", "null")
        }
        return view
    }

}
