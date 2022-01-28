package com.voidsamurai.lordoftime

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var intentAuth: Intent
    private val SHARED_PREFERENCES:String="sharedPreferences"
    fun setLoggedIn(){
        getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit().putBoolean("logged_in",true).apply()
    }
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.v("code", "" + it.resultCode)
            if (it.resultCode == Activity.RESULT_OK) {

                val task = GoogleSignIn.getLastSignedInAccount(this)
                try {
                    task?.let { itt -> firebaseAuthWithGoogle(itt)

                        getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit().putString("user_id",itt.id).apply()

                    }
                } catch (e: ApiException) {
                    Log.w("Login", "Google sign in failed", e)
                }
            }
        }
    override fun onBackPressed() {
        val nav=  findNavController(R.id.nav)
        nav.currentDestination?.label.let {
            when(it){
                "ForgotPasswordFragment","fragment_register"->
                    nav.popBackStack()
                else -> super.onBackPressed()
            }

        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,resources.getString(R.string.logged_in),Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    setLoggedIn()
                    startActivity(intent)
                    finish()

                } else {
                    Log.e("Login", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this,resources.getString(R.string.login_error),Toast.LENGTH_SHORT).show()
                }

            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_activity)
        supportActionBar?.hide()
        auth = Firebase.auth

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.defaultt_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)



    }



    fun signIn(){
        intentAuth=googleSignInClient.signInIntent
        getResult.launch(intentAuth)
    }

}