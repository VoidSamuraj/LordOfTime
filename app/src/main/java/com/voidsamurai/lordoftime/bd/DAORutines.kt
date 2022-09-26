package com.voidsamurai.lordoftime.bd

import com.google.firebase.database.*
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import java.util.*
import kotlin.collections.ArrayList

class DAORutines(mActivity: MainActivity){
    private val data:ArrayList<RutinesRow> = arrayListOf()
    private var dbReference: DatabaseReference
    private var vel: ValueEventListener
    init {

        val db= FirebaseDatabase.getInstance()

        dbReference=db.getReference(mActivity.userId!!).child("rutines")

        vel=object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children){
                        data.add(
                            RutinesRow(
                                child.key!!.toInt(),
                                (child.child("task_id").value as Long).toInt(),
                                child.child("days").value.toString(),
                                child.child("hour").value.toString()
                            )
                        )
                    }
                    mActivity.updateLocalRutinesDB(data)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }

    fun add(id:Int,task_id:Int,days:String, hours:String){
        val ref=dbReference.child(id.toString())
        ref.child("task_id").setValue(task_id)
        ref.child("days").setValue(days)
        ref.child("hours").setValue(hours)

    }

    fun add(data: RutinesRow){
        val ref=dbReference.child(data.id.toString())
        ref.child("task_id").setValue(data.task_id)
        ref.child("days").setValue(data.days)
        ref.child("hours").setValue(data.hours)

    }
    fun update(id:Int,task_id:Int,days:String, hours:String){
        val ref=dbReference.child(id.toString())
        val childUpdates = hashMapOf<String, Any>()
        task_id.let { childUpdates.put("task_id",it) }
        days.let {childUpdates.put("days",it)  }
        hours.let { childUpdates.put("hours",it) }
        ref.updateChildren(childUpdates)

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


    companion object {
        /**
         * @param day - short version of day name as in database [3 first letters in uppercase]
         * @return id of string resources of day
         *
         * */
        fun getDayID(day: String): Int {
            return when (day) {
                "MON" -> R.string.monday
                "TUE" -> R.string.tuesday
                "WED" -> R.string.wednesday
                "THU" -> R.string.thursday
                "FRI" -> R.string.friday
                "SAT" -> R.string.saturday
                "SUN" -> R.string.sunday
                else -> 0
            }
            /**
             * @param day - short version of day name as in database [3 first letters in uppercase]
             * @return Callendar.DAY
             *
             * */
        }   fun getDayNr(day: String, isMondayFirstDay:Boolean?=null): Int {
            return  if (isMondayFirstDay!=null&&isMondayFirstDay==true)
                when (day) {
                    "MON" -> 1
                    "TUE" -> 2
                    "WED" -> 3
                    "THU" -> 4
                    "FRI" -> 5
                    "SAT" -> 6
                    "SUN" -> 7
                    else -> 0
                }
            else
                when (day) {
                    "MON" -> Calendar.MONDAY
                    "TUE" -> Calendar.TUESDAY
                    "WED" -> Calendar.WEDNESDAY
                    "THU" -> Calendar.THURSDAY
                    "FRI" -> Calendar.FRIDAY
                    "SAT" -> Calendar.SATURDAY
                    "SUN" -> Calendar.SUNDAY
                    else -> 0
                }
        }
    }
}