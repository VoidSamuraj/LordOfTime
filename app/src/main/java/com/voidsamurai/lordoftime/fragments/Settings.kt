package com.voidsamurai.lordoftime.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.voidsamurai.lordoftime.AuthActivity
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentSettingsBinding
import kotlinx.coroutines.*


class Settings : Fragment(), AdapterView.OnItemSelectedListener {

    private var firstClick=0
    private var _settingsBinding:FragmentSettingsBinding?=null
    private val settingsBinding get() =_settingsBinding!!


    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if(it.resultCode==Activity.RESULT_OK&&it.data!=null&& it.data!!.data!=null){
                val uri= it.data!!.data
                settingsBinding.avatar.setImageURI(uri!!)
                (activity as MainActivity).saveAvatar(uri)

            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _settingsBinding= FragmentSettingsBinding.inflate(inflater,container,false)
        return settingsBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mActivity=(activity as MainActivity)

        settingsBinding.imageEdit.setOnClickListener {
            val intent =Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            getResult.launch(intent)


        }

        var imageSet=false
        CoroutineScope(Dispatchers.Default).launch {
            var count=0
            do {
                    mActivity.userImage?.let {
                        MainScope().launch {
                            settingsBinding.avatar.setImageBitmap(mActivity.userImage)
                        }
                        imageSet = true
                    }

                ++count
                delay(1000)
            } while (!imageSet&&count<5)
        }

        val adapter:ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(requireContext(),R.array.languages,android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        settingsBinding.languages.adapter=adapter
        settingsBinding.languages.onItemSelectedListener=this
        settingsBinding.languages.setSelection(resources.getStringArray(R.array.languages).indexOf((activity as MainActivity).getLanguage()))

        settingsBinding.oldSwitch.isChecked=mActivity.showOutdated
        settingsBinding.oldSwitch.setOnClickListener {
            mActivity.setOutdated(settingsBinding.oldSwitch.isChecked)
            mActivity.getDataFromDB()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (mActivity.getMode()) {
                -1 -> settingsBinding.toggleGroup.check(R.id.auto)
                1 -> settingsBinding.toggleGroup.check(R.id.light)
                2 -> settingsBinding.toggleGroup.check(R.id.dark)
            }

            settingsBinding.toggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                when (checkedId) {
                    R.id.auto -> mActivity.setStyle(-1)
                    R.id.light -> mActivity.setStyle(1)
                    R.id.dark -> mActivity.setStyle(2)
                }
            }

        } else{
            settingsBinding.toggleGroup.visibility=View.GONE
            settingsBinding.textView12.visibility=View.GONE
        }
        (activity as MainActivity).userName.let {
            settingsBinding.name.text=it
        }

        (activity as MainActivity).emailId.let {
            settingsBinding.email.text=it
        }
        settingsBinding.logout.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.confirm))
                .setMessage(resources.getString(R.string.confirm_logout))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    (activity as MainActivity).auth.signOut()
                    (activity as MainActivity).googleSignInClient.signOut()
                    (activity as MainActivity).logout()
                    val intent= Intent(activity as MainActivity, AuthActivity::class.java)
                    intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    (activity as MainActivity).finish()
                    Toast.makeText(requireContext(),resources.getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.no, null).show()


        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    if(firstClick!=0) {
        (activity as MainActivity).setLanguage(parent!!.getItemAtPosition(position).toString())
    }else
        ++firstClick

    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}