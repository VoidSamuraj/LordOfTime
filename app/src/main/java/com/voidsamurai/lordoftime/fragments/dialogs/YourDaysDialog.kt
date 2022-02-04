package com.voidsamurai.lordoftime.fragments.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import java.util.*


class YourDaysDialog : DialogFragment() ,DatePickerDialog.OnDateSetListener {
    private lateinit var contentView: View

    //var _binding:bind
    lateinit var birthDate:EditText
    lateinit var years:EditText
    lateinit var save:Button
    lateinit var cancel:Button

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        contentView= inflater.inflate(R.layout.dialog_your_days, null)
        builder.setView(contentView)
         birthDate=contentView.findViewById(R.id.birth_date)
         years=contentView.findViewById(R.id.years)
         cancel=contentView.findViewById(R.id.cancel)
         save=contentView.findViewById(R.id.save)

        birthDate.setOnClickListener {
            val cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val dp =
                DatePickerDialog(
                    requireContext(),
                    this,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            dp.show()
        }

        save.setOnClickListener {
            var isSet=true
            if(birthDate.text.isNullOrBlank()) {
                birthDate.background.setTint(Color.RED)
                isSet=false
            }else
                birthDate.background.setTint(Color.BLACK)

            if(years.text.isNullOrBlank()) {
                years.background.setTint(Color.RED)
                isSet=false
            }else
                years.background.setTint(Color.BLACK)
            if(isSet){
                val date=birthDate.text.split('/')
                val year=date[0]
                val month=date[1]
                val day=date[2]

                val cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                cal.set(Calendar.YEAR,year.toInt()+years.text.toString().toInt())
                cal.set(Calendar.MONTH,month.toInt())
                cal.set(Calendar.DAY_OF_MONTH,day.toInt())
                cal.set(Calendar.HOUR_OF_DAY,0)
                cal.set(Calendar.MINUTE,0)
                cal.set(Calendar.SECOND,0)
                cal.set(Calendar.MILLISECOND,0)

                (activity as MainActivity).let {
                    it.setYourTime(cal.timeInMillis)
                    it.settings.add(death_date = cal.timeInMillis)
                    it.fillMementoMori()
                }
                dismiss()
            }
        }
        cancel.setOnClickListener {
            dismiss()
        }


        return builder.create()
    }


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
     birthDate.setText("${year}/${month}/${dayOfMonth}")
    }


}