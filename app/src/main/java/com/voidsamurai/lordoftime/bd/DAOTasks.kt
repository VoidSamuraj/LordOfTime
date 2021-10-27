package com.voidsamurai.lordoftime.bd

import android.util.Log
import com.google.firebase.database.*
import com.voidsamurai.lordoftime.MainActivity
import java.util.*
import kotlin.collections.ArrayList

class DAOTasks(mActivity: MainActivity){
    private val data:ArrayList<DataRow> = arrayListOf()
    private var dbReference: DatabaseReference
    private var vel:ValueEventListener
    init {

        val db=FirebaseDatabase.getInstance()

        dbReference=db.getReference(mActivity.userId!!).child("task")

        vel=object:ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("BD","\nbd\n|\n")
                if (snapshot.exists()) {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    for (child in snapshot.children){
                        calendar.time = Date(child.child("dateTime").value as Long)
                        Log.v("OnlineDB",""+child.key+" "+calendar.time)
                        data.add(
                            DataRow(
                                child.key!!.toInt(),
                                child.child("category").value.toString(),
                                child.child("name").value.toString(),
                                calendar,
                                (child.child("workingTime").value as Long).toFloat(),
                                (child.child("priority").value as Long).toInt(),
                                (child.child("currentWorkingTime").value as Long).toFloat(),

                                )
                        )
                    }
                    mActivity.updateLocalTaskDB(data)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }

    fun add(id:Int,category: String,name:String, dateTime:Long, workingTime:Int, priority:Int, currentWorkingTime:Int){
        val ref=dbReference.child(id.toString())
        ref.child("category").setValue(category)
        ref.child("name").setValue(name)
        ref.child("dateTime").setValue(dateTime)
        ref.child("workingTime").setValue(workingTime)
        ref.child("priority").setValue(priority)
        ref.child("currentWorkingTime").setValue(currentWorkingTime)
    }

    fun add(data: DataRowWithColor){
        val ref=dbReference.child(data.id.toString())
        ref.child("category").setValue(data.category)
        ref.child("name").setValue(data.name)
        ref.child("dateTime").setValue(data.date.time.time)
        ref.child("workingTime").setValue(data.workingTime)
        ref.child("priority").setValue(data.priority)
        ref.child("currentWorkingTime").setValue(data.currentWorkingTime)
    }

    fun delete(taskId: String){
        dbReference.child(taskId).removeValue()
    }
    fun addListeners(){
        dbReference.addListenerForSingleValueEvent(vel)
    }
    fun removeListeners(){
        dbReference.removeEventListener(vel)
    }
}