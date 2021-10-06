package com.voidsamurai.lordoftime.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.voidsamurai.lordoftime.AuthActivity
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    private lateinit var _loginBinding: FragmentLoginBinding
    private val loginBinding get()=_loginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _loginBinding = FragmentLoginBinding.inflate(inflater,container,false)
        return loginBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginBinding.login.setOnClickListener {
            val email = loginBinding.editEmail.text.trim(' ')
            val password = loginBinding.editPassword.text.trim(' ')
            var isGood = true
            if (email.isBlank()) {
                Toast.makeText(
                    context,
                    resources.getString(R.string.no_data) + " " + resources.getString(R.string.login)
                        .lowercase(),
                    Toast.LENGTH_SHORT
                ).show()
                isGood = false
            }
            if (password.isBlank()) {
                Toast.makeText(
                    context,
                    resources.getString(R.string.no_data) + " " + resources.getString(R.string.password)
                        .lowercase(),
                    Toast.LENGTH_SHORT
                ).show()
                isGood = false
            }

            if (isGood) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email.toString(),password.toString())
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            Toast.makeText(context,
                                resources.getString(R.string.logged_in),
                                Toast.LENGTH_SHORT).show()

                            val firebaseUser: FirebaseUser = it.result.user!!

                            val intent = Intent(activity as AuthActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("user_id",firebaseUser.uid)
                            intent.putExtra("email_id",email)
                            startActivity(intent)
                            (activity as AuthActivity).finish()

                        }else{
                            Toast.makeText(context,
                                it.exception!!.message,
                                Toast.LENGTH_SHORT).show()
                        }
                    }

            }
        }
        loginBinding.register.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        loginBinding.forgot.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
        loginBinding.googleLogin.setOnClickListener {
            (activity as AuthActivity).signIn()

        }
    }
}