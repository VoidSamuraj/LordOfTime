package com.voidsamurai.lordoftime

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SplashScreenActivity : AppCompatActivity(), Animation.AnimationListener{

    lateinit var auth: FirebaseAuth
    var currentUser:FirebaseUser?=null
    private val SHARED_PREFERENCES:String="sharedPreferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        auth = Firebase.auth

         currentUser = auth.currentUser

    }


    override fun onStart() {
        super.onStart()
        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.logo_enter)
        animation.setAnimationListener(this)
        findViewById<ImageView>(R.id.logo).startAnimation(animation)
        super.onStart()

    }


    override fun onAnimationStart(animation: Animation?) {
    }

    override fun onAnimationEnd(animation: Animation?) {

        if (currentUser != null || getSharedPreferences(
                SHARED_PREFERENCES,
                MODE_PRIVATE
            ).getBoolean("logged_in", false)
        ) {
            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("user_id", auth.uid)
            intent.putExtra("user_name", auth.currentUser!!.displayName)
            intent.putExtra("email_id", auth.currentUser!!.email)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this@SplashScreenActivity, AuthActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onAnimationRepeat(animation: Animation?) {
    }

}