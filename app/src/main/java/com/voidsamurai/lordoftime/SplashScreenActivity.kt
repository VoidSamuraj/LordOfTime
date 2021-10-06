package com.voidsamurai.lordoftime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity(), Animation.AnimationListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        CoroutineScope(Dispatchers.Default).launch {
            delay(3000)
           // val intent=Intent(this@SplashScreenActivity,MainActivity::class.java)
            val intent=Intent(this@SplashScreenActivity,AuthActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    override fun onStart() {
        super.onStart()
        val animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_enter)
        animation.setAnimationListener(this)
        findViewById<ImageView>(R.id.logo).startAnimation(animation)
    }

    override fun onAnimationStart(animation: Animation?) {

    }


    override fun onAnimationEnd(animation: Animation?) {

    }


    override fun onAnimationRepeat(animation: Animation?) {
    }
}