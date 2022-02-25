package com.voidsamurai.lordoftime.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.voidsamurai.lordoftime.AuthActivity
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentSettingsBinding
import com.voidsamurai.lordoftime.fragments.adapters.StartWorkAdapter
import com.voidsamurai.lordoftime.fragments.dialogs.NumberPicker
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

        settingsBinding.hours.text= String.format("%s %d %s",resources.getText(R.string.aim),(activity as MainActivity).getMainChartRange(),resources.getText(R.string.h))

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

        val adapter:ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(requireContext(),R.array.languages,R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        settingsBinding.languages.adapter=adapter
        settingsBinding.languages.onItemSelectedListener=this
        settingsBinding.languages.setSelection(resources.getStringArray(R.array.languages).indexOf((activity as MainActivity).getLanguage()))

        settingsBinding.oldSwitch.isChecked=mActivity.showOutdated
        settingsBinding.oldSwitch.setOnClickListener {
            mActivity.let {
                val checked=settingsBinding.oldSwitch.isChecked
                it.setOutdated(checked)
                it.settings.add(show_outdated = checked)
                it.getDataFromDB()
            }
        }

        settingsBinding.completeSwitch.isChecked=mActivity.getIsShowingCompleted()
        settingsBinding.completeSwitch.setOnClickListener {
            mActivity.let {
                val checked=settingsBinding.completeSwitch.isChecked
                it.setIsShowingCompleted(checked)
                it.settings.add(show_completed = checked)
                it.getDataFromDB()
            }

        }
        settingsBinding.showNotificationSwitch.isChecked=mActivity.getIsShowingNotifications()
        settingsBinding.showNotificationSwitch.setOnClickListener {
            mActivity.let {
                val checked=settingsBinding.showNotificationSwitch.isChecked
                if(checked)
                    it.createTodayNotifications()
                else
                    it.deleteTodayNotifications()

                settingsBinding.soundNotificationSwitch.isEnabled=checked

                it.setIsShowingNotifications(checked)
                it.settings.add(show_notifications = checked)
                it.getDataFromDB()
            }
        }
        settingsBinding.soundNotificationSwitch.isChecked=mActivity.getHaveNotificationsSound()
        settingsBinding.soundNotificationSwitch.isEnabled=mActivity.getIsShowingNotifications()
        settingsBinding.soundNotificationSwitch.setOnClickListener {
            mActivity.let {
                it.deleteTodayNotifications()
                it.createTodayNotifications()
                val checked=settingsBinding.soundNotificationSwitch.isChecked
                it.setHaveNotificationsSound(checked)
                it.settings.add(notifications_sound = checked)
                it.getDataFromDB()
            }
        }


        settingsBinding.deleteSwitch.isChecked=mActivity.getIsDeletingCompleted()
        settingsBinding.deleteSwitch.setOnClickListener {

            if (settingsBinding.deleteSwitch.isChecked){
                AlertDialog.Builder(requireContext())
                    .setTitle(resources.getString(R.string.confirm))
                    .setMessage(resources.getString(R.string.confirm_delete))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        mActivity.setIsDeletingCompleted(true)
                        mActivity.settings.add(delete_completed =true )
                        mActivity.deleteCompleted()
                        mActivity.getDataFromDB()
                    }
                    .setNegativeButton(R.string.no) { _, _ ->
                        settingsBinding.deleteSwitch.isChecked = false
                    }.setCancelable(false).show()

            }else
                mActivity.setIsDeletingCompleted(false)
            mActivity.settings.add(delete_completed =false )
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (mActivity.getMode()) {
                -1 -> settingsBinding.toggleGroup.check(R.id.auto)
                1 -> settingsBinding.toggleGroup.check(R.id.light)
                2 -> settingsBinding.toggleGroup.check(R.id.dark)
            }

            settingsBinding.toggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                when (checkedId) {
                    R.id.auto -> {
                        mActivity.setStyle(-1)
                        mActivity.settings.add(mode = -1)
                    }
                    R.id.light -> {
                        mActivity.setStyle(1)
                        mActivity.settings.add(mode = 1)
                    }
                    R.id.dark -> {
                        mActivity.setStyle(2)
                        mActivity.settings.add(mode = 2)
                    }
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
        fun setChartStrings(state:Boolean){
            settingsBinding.numberPicker.isClickable=state
            if(!state) {
                settingsBinding.hours.setTextColor(Color.GRAY)
                settingsBinding.numberPicker.backgroundTintList= ColorStateList.valueOf(Color.GRAY)
            }else {
                settingsBinding.hours.setTextColor(resources.getColor(R.color.text,null))
                settingsBinding.numberPicker.backgroundTintList= ColorStateList.valueOf(resources.getColor(R.color.switch_track_stroke_selected,null))
            }
        }
        settingsBinding.chartSwitch.setOnClickListener {
            val state=(activity as MainActivity).setMainChartAuto()
            setChartStrings(state)
            ///make text gray or something
        }


        settingsBinding.numberPicker.setOnClickListener {
            val fnp= NumberPicker((activity as MainActivity).getMainChartRange()) {
                settingsBinding.hours.text= String.format("%s %d %s",resources.getText(R.string.aim),it.value+1,resources.getText(R.string.h))
                (activity as MainActivity).let{itt->
                    itt.setMainChartRange(it.value+1)
                    itt.settings.add(main_chart_aim = it.value+1)
                }
            }
            fnp.show(childFragmentManager,"Hours")
        }

        val isChartSettingsActive=(activity as MainActivity).getIsMainChartManual()
        settingsBinding.chartSwitch.isChecked=!isChartSettingsActive
        setChartStrings(isChartSettingsActive)

        settingsBinding.logout.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.confirm))
                .setMessage(resources.getString(R.string.confirm_logout))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    logout()
                }
                .setNegativeButton(R.string.no, null).show()

        }
        settingsBinding.deleteData.setOnClickListener{
            AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.confirm))
                .setMessage(resources.getString(R.string.confirm_delete_data))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    deleteData()
                }
                .setNegativeButton(R.string.no, null).show()


           }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if(firstClick!=0) {
            val activity=(activity as MainActivity)
            val language=parent!!.getItemAtPosition(position).toString()
            activity.setLanguage(parent.getItemAtPosition(position).toString())
            activity.settings.add(language = language)
        }else
            ++firstClick

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
    private fun deleteData() {

        FirebaseAuth.getInstance().currentUser?.let {
            FirebaseDatabase.getInstance().getReference(it.uid).removeValue().addOnCompleteListener {
                if(it.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.data_deleted_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    (activity as MainActivity).let {
                        it.getStorageReference().delete()

                        if(it.isTaskStarted){
                            StartWorkAdapter.deleteObservers(activity = activity as MainActivity, lifecycleOwner = viewLifecycleOwner)
                            it.isTaskStarted=false
                        }
                        it.getDBOpenHelper().deleteData()
                        it.getDataFromDB()
                    }
                }
                else
                    Toast.makeText(requireContext(),resources.getString(R.string.data_deleted_faliure), Toast.LENGTH_SHORT).show()
            }

        }


    }
    private fun logout(showMSG:Boolean=true){
        (activity as MainActivity).let{
            it.auth.signOut()
            it.googleSignInClient.signOut()
            it.logout()
            val intent= Intent(it, AuthActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            it.finish()
            if (showMSG)
                Toast.makeText(requireContext(),resources.getString(R.string.logout_success), Toast.LENGTH_SHORT).show()

        }



    }
}