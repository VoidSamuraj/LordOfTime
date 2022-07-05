package com.voidsamurai.lordoftime.fragments.dialogs


import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.core.view.children
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DAORutines
import java.util.*

/**
 * @param type 0-SAVE; 1-EDIT
 * */
class RutinesElement(private val taskId:Int,private val type:Int,private val rutinesRowId:Int?=null) : DialogFragment() {
    private lateinit var contentView: View
    private lateinit var  cancel:Button
    private lateinit var  save:Button
    private lateinit var  selectHour:Button
    private lateinit var  monday:Chip
    private lateinit var  tuesday:Chip
    private lateinit var  wednesday:Chip
    private lateinit var  thursday:Chip
    private lateinit var  friday:Chip
    private lateinit var  saturday:Chip
    private lateinit var  sunday:Chip
    private lateinit var  hours:ChipGroup


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        contentView= inflater.inflate(R.layout.dialog_rutines, null)
        cancel=contentView.findViewById(R.id.cancel)
        save=contentView.findViewById(R.id.save)
        selectHour=contentView.findViewById(R.id.selectHour)
        monday=contentView.findViewById(R.id.monday)
        tuesday=contentView.findViewById(R.id.tuesday)
        wednesday=contentView.findViewById(R.id.wendnesday)
        thursday=contentView.findViewById(R.id.thursday)
        friday=contentView.findViewById(R.id.friday)
        saturday=contentView.findViewById(R.id.saturday)
        sunday=contentView.findViewById(R.id.sunday)
        hours=contentView.findViewById(R.id.hours)
        val days= arrayListOf(monday,tuesday,wednesday,thursday,friday,saturday,sunday)


        if(type==1){
            val data=(activity as MainActivity).getDBOpenHelper().getRutinesArray(taskId).filter { rutinesRow -> rutinesRow.id==rutinesRowId }[0]
            val hours=data.hours.split(',')
            val daysNames=data.days.split(',').map { s -> resources.getString(DAORutines.getDayID(s))  }

            days.forEach {
                if(daysNames.contains(it.text))
                    it.isChecked=true

            }
            hours.forEach {
                createChip(it)
            }

        }


        selectHour.setOnClickListener {
            val time= Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val localTime= Calendar.getInstance()
            val tp =  TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    time.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    time.set(Calendar.MINUTE, minute)
                    time.set(Calendar.SECOND, 0)
                    time.set(Calendar.MILLISECOND, 0)

                    val found=hours.children.map { view ->(view as Chip).text  }.contains(selectHour.text)
                    if((!found))
                        createChip(hourOfDay,minute)
                },
                localTime.get(Calendar.HOUR_OF_DAY),
                localTime.get(Calendar.MINUTE),
                true
            )
            tp.show()
        }


        cancel.setOnClickListener {
            dismiss()
        }
        save.setOnClickListener {

            var isDaySelected=false
            var isHourSelected=false
            days.forEach { if(it.isChecked)isDaySelected=true }
            if(hours.size>0)
                isHourSelected=true
            if(isDaySelected&&isHourSelected) {

                var daysRow=""
                if(monday.isChecked)daysRow+="MON,"
                if(tuesday.isChecked)daysRow+="TUE,"
                if(wednesday.isChecked)daysRow+="WED,"
                if(thursday.isChecked)daysRow+="THU,"
                if(friday.isChecked)daysRow+="FRI,"
                if(saturday.isChecked)daysRow+="SAT,"
                if(sunday.isChecked)daysRow+="SUN,"

                daysRow=daysRow.substring(0,daysRow.length-1)

                val time = hours.children.map { view -> (view as Chip).text }
                    .joinToString(separator = ",")

                    (activity as MainActivity).let {
                        val uid=(activity as MainActivity).userId
                        when (type) {
                            0 -> {
                                val id = it.getDBOpenHelper().addRutinesRow(taskId, daysRow, time,uid)
                                if (id.toInt()!=-1) {
                                    it.rutines.add(id.toInt(), taskId, daysRow, time)
                                    it.repeatDialog.notifyItemInserted()
                                }
                            }
                            1 -> {
                                it.getDBOpenHelper().editRutinesRow(rutinesRowId!!, taskId, daysRow, time,uid)
                                if (id!=-1) {
                                    it.rutines.update(id, taskId, daysRow, time)
                                    it.repeatDialog.notifyItemChanged()
                                }
                            }
                            else -> {}
                        }
                    }

            }
            dismiss()
        }


        builder.setView(contentView)
        super.onCreateDialog(savedInstanceState)
        return builder.create()
    }
    fun createChip(hourOfDay:Int,minute:Int)= createChip(String.format("%02d:%02d", hourOfDay,minute))

    //hour chip
    fun createChip(text:String){
        val chip=Chip(requireContext())
        chip.text=text
        chip.setChipBackgroundColorResource(R.color.chip_checked)
        chip.isCloseIconVisible=true
        chip.setOnCloseIconClickListener { hours.removeView(it) }
        hours.addView(chip)

    }
}