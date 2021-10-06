package com.voidsamurai.lordoftime.bd

import com.google.firebase.database.*
import com.voidsamurai.lordoftime.MainActivity
class DAOColors(mActivity: MainActivity){
    private val data:MutableMap<String,String> = mutableMapOf()
    private var dbReference: DatabaseReference
    private var vel:ValueEventListener
    init {

        val db=FirebaseDatabase.getInstance()

        dbReference=db.getReference(mActivity.userId!!).child("color")

        vel=object:ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    for (colors in snapshot.children)
                        data[colors.key!!] = colors.value.toString()
                    mActivity.updateLocalColorDB(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }
    fun add(category:String,color:String){
        dbReference.child(category).setValue(color)
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