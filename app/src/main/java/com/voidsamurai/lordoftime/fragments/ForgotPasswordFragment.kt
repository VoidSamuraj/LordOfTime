package com.voidsamurai.lordoftime.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.AuthFragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth


class ForgotPasswordFragment : Fragment() {

    private lateinit var _passwordBinding:AuthFragmentForgotPasswordBinding
    private val passwordBinding get() = _passwordBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _passwordBinding= AuthFragmentForgotPasswordBinding.inflate(inflater,container,false)
        return passwordBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passwordBinding.send.setOnClickListener {
            val email=passwordBinding.editEmail.text.trim(' ')
            if(email.length<5){
                Toast.makeText(context,
                    resources.getString(R.string.no_data)+" "+resources.getString(R.string.email).lowercase(),
                    Toast.LENGTH_SHORT).show()
            }else{

                FirebaseAuth.getInstance().sendPasswordResetEmail(email.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            Toast.makeText(context,
                                resources.getString(R.string.email_sended),
                                Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener {
                    }
            }

        }

        passwordBinding.backToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}