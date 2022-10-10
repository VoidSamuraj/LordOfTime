package com.voidsamurai.lordoftime.bd


import com.google.firebase.database.*
import com.voidsamurai.lordoftime.MainActivity
import java.util.*
import kotlin.collections.ArrayList
//@Keep
class DAOTasks(val mActivity: MainActivity){
    private val data:ArrayList<DataRow> = arrayListOf()
    private var dbReference: DatabaseReference
    private var vel:ValueEventListener



    init {
        val db=FirebaseDatabase.getInstance()


        dbReference=db.getReference(mActivity.userId!!).child("task")


        vel=object:ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    for (child in snapshot.children){
                        calendar.time = Date(child.child("dateTime").value as Long)
                        data.add(
                            DataRow(
                                child.key!!.toInt(),
                                child.child("category").value.toString(),
                                child.child("name").value.toString(),
                                calendar.clone() as Calendar,
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


    fun add(id:Int,category: String,name:String, dateTime:Long, workingTime:Int, priority:Int, currentWorkingTime:Int,finished: Int){
        val ref=dbReference.child(id.toString())
        ref.child("category").setValue(category)
        ref.child("name").setValue(name)
        ref.child("dateTime").setValue(dateTime)
        ref.child("workingTime").setValue(workingTime)
        ref.child("priority").setValue(priority)
        ref.child("currentWorkingTime").setValue(currentWorkingTime)
        ref.child("isFinished").setValue(finished)
    }

    fun add(data: DataRowWithColor){
        val ref=dbReference.child(data.id.toString())
        ref.child("name").setValue(data.name)
        ref.child("dateTime").setValue(data.date.time.time)
        ref.child("workingTime").setValue(data.workingTime)
        ref.child("priority").setValue(data.priority)
        ref.child("currentWorkingTime").setValue(data.currentWorkingTime)
        ref.child("isFinished").setValue(data.finished)

    }
    /**
     * @params null-not changes attribute
     * */
    fun update(id:Int,category: String?=null,name:String?=null, dateTime:Long?=null, workingTime:Int?=null, priority:Int?=null, currentWorkingTime:Int?=null,finished: Int?=null){
        val ref=dbReference.child(id.toString())
        category?.let {  ref.child("category").setValue(it) }
        name?.let {ref.child("name").setValue(it)  }
        dateTime?.let { ref.child("dateTime").setValue(it) }
        workingTime?.let { ref.child("workingTime").setValue(it) }
        priority?.let { ref.child("priority").setValue(it) }
        currentWorkingTime?.let { ref.child("currentWorkingTime").setValue(it) }
        finished?.let { ref.child("isFinished").setValue(it) }
    }

    fun delete(taskId: String){
        dbReference.child(taskId).removeValue()
    }
    fun delete(taskId: Int){
        dbReference.child(taskId.toString()).removeValue()
    }
    fun addListeners(){
        dbReference.addListenerForSingleValueEvent(vel)
    }
    fun removeListeners(){
        dbReference.removeEventListener(vel)
    }
}