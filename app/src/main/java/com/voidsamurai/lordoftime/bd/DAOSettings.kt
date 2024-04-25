package com.voidsamurai.lordoftime.bd

import com.google.firebase.database.*
import com.voidsamurai.lordoftime.MainActivity
//@Keep
class DAOSettings(val mActivity: MainActivity) {
    private var dbReference: DatabaseReference
    private var vel: ValueEventListener
    init {

        val db= FirebaseDatabase.getInstance()

        dbReference=db.getReference(mActivity.userId!!).child("settings")

        vel=object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (children in snapshot.children) {
                        mActivity.getSettings(SettingsData(
                            mActivity.userId!!,
                            children.child("language").value.toString(),
                            children.child("mode").value?.let{(it as Long).toInt()},
                            children.child("show_outdated").value?.let{it as Boolean},
                            children.child("show_completed").value?.let{it as Boolean},
                            children.child("delete_completed").value?.let{it as Boolean},
                            children.child("main_chart_auto").value?.let{it as Boolean},
                            children.child("main_chart_aim").value?.let{(it as Long).toInt()},
                            children.child("death_date").value?.let{it as Long},
                            children.child("show_notifications").value?.let{it as Boolean},
                            children.child("notifications_sound").value?.let{it as Boolean}
                        ))
                        break
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        }


    }
    /**
     *@param language
     * [EN, PL, DEFAULT]
     * @param mode
     * -1 default mode
     * 1 light mode
     * 2 dark mode
     *
     * */
    fun add(language:String?=null, mode:Int?=null, show_outdated:Boolean?=null, show_completed:Boolean?=null, delete_completed:Boolean?=null, main_chart_auto:Boolean?=null, main_chart_aim:Int?=null,death_date:Long?=null,show_notifications:Boolean?=null,notifications_sound:Boolean?=null){
        language.let {  dbReference.child("language").setValue(it)}
        mode.let {dbReference.child("mode").setValue(it)}
        show_outdated.let {dbReference.child("show_outdated").setValue(it)}
        show_completed.let {dbReference.child("show_completed").setValue(it)}
        delete_completed.let {dbReference.child("delete_completed").setValue(it)}
        main_chart_auto.let {dbReference.child("main_chart_auto").setValue(it)}
        main_chart_aim.let {dbReference.child("main_chart_aim").setValue(it)}
        death_date.let {dbReference.child("death_date").setValue(it)}
        show_notifications.let {dbReference.child("show_notifications").setValue(it)}
        notifications_sound.let {dbReference.child("notifications_sound").setValue(it)}

    }


    fun delete(category: String){
        dbReference.child(category).removeValue()
    }
    fun addListeners(){
        dbReference.addListenerForSingleValueEvent(vel)
    }
    fun removeListeners(){
        dbReference.removeEventListener(vel)
    }
}