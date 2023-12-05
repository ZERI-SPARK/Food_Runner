package com.anujandankit.foodrunner.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.anujandankit.foodrunner.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            val intentToLoginActivity = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intentToLoginActivity)
            finish()
        }, 2000)
    }
}