package com.example.shelfship.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.shelfship.R
import com.example.shelfship.utils.FirebaseUtils.isLoggedIn


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed(object : Runnable {
            override fun run() {
                if (isLoggedIn) {
                    startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
                }
                finish()
            }
        }, 1000)
    }
}