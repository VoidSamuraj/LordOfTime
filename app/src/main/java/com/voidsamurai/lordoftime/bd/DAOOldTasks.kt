package com.voidsamurai.lordoftime.bd

import com.google.firebase.database.*
import com.voidsamurai.lordoftime.MainActivity
import java.util.*

class DAOOldTasks (mActivity: MainActivity) {
    private val data: ArrayList<OldData> = arrayListOf()
    private var dbReference: DatabaseReference
    private var vel: ValueEventListener

    init {

        val db = FirebaseDatabase.getInstance()

        dbReference = db.getReference(mActivity.userId!!).child("oldTask")

        vel = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val calendar = Calendar.getInstance()

                    for (child in snapshot.children){
                        val id = child.key!!.toLong()
                        calendar.time = Date(id)
                        data.add(
                            OldData(
                                date_id = id.toInt(),
                                child.child("category").value.toString(),
                                (child.child("currentWorkingTime").value as Long).toFloat(),
                            )
                        )
                    }
                    mActivity.updateLocalOldTaskDB(data)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }

    fun add(
        category: String,
        dateTime: Long,
        currentWorkingTime: Int
    ) {
        val ref = dbReference.child(dateTime.toString())
        ref.child("category").setValue(category)
        ref.child("currentWorkingTime").setValue(currentWorkingTime)


    }

    fun delete(taskId: String) {
        dbReference.child(taskId).removeValue()
    }

    fun addListeners() {
        dbReference.addListenerForSingleValueEvent(vel)
    }

    fun removeListeners() {
        dbReference.removeEventListener(vel)
    }
}

//date_id INTEGER PRIMARY KEY , working_time INTEGER, category TEXT