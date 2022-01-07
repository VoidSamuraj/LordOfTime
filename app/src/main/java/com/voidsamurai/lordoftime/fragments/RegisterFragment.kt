package com.voidsamurai.lordoftime.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.voidsamurai.lordoftime.AuthActivity
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.AuthFragmentRegisterBinding
import com.google.firebase.auth.UserProfileChangeRequest


class RegisterFragment : Fragment() {

    private lateinit var _registerBinding:AuthFragmentRegisterBinding
    private val registerBinding get()=_registerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _registerBinding= AuthFragmentRegisterBinding.inflate(inflater,container,false)
        return registerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerBinding.register.setOnClickListener {
            val password=registerBinding.editPassword.text.trim(' ')
            val password2=registerBinding.editPassword2.text.trim(' ')
            val email=registerBinding.editEmail.text.trim(' ')
            val uName=registerBinding.userName.text.toString()
            var isGood=true
            if(password.isBlank()){
                Toast.makeText(context,
                    resources.getString(R.string.no_data)+" "+resources.getString(R.string.password).lowercase(),
                    Toast.LENGTH_SHORT).show()
                isGood=false
            }
            if(email.isBlank()){
                Toast.makeText(context,
                    resources.getString(R.string.no_data)+" "+resources.getString(R.string.email).lowercase(),
                    Toast.LENGTH_SHORT).show()
                isGood=false
            }
            if (password != password2){
                Toast.makeText(context,
                    resources.getString(R.string.different_passwords)+password+" "+password2,
                    Toast.LENGTH_SHORT).show()
                isGood=false
            }
            if(isGood){
                (activity as AuthActivity).auth.createUserWithEmailAndPassword(email.toString(),password.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful){


                            val intent= Intent(activity as AuthActivity, MainActivity::class.java)
                            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            if (uName.isNotEmpty()) {
                                val user = FirebaseAuth.getInstance().currentUser

                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(uName).build()

                                user!!.updateProfile(profileUpdates)
                                user.updateProfile(profileUpdates)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("FIREBASE", "User profile updated.")
                                        }
                                    }

                                intent.putExtra("user_name",uName)
                            }
                            val firebaseUser:FirebaseUser = it.result.user!!
                            Toast.makeText(context,
                                resources.getString(R.string.registered),
                                Toast.LENGTH_SHORT).show()


                            intent.putExtra("user_id",firebaseUser.uid)
                            intent.putExtra("email_id",email.toString())
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
        registerBinding.backToLogin.setOnClickListener {


            //check if parsed data and display notification

            findNavController().popBackStack()
        }
    }
}