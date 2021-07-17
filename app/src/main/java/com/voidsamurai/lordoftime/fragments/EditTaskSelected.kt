package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.fragments.adapters.ArrayColorAdapter
import kotlinx.android.synthetic.main.fragment_task_edit.*
import layout.DataRowWithColor
import java.util.*


class EditTaskSelected : Fragment() ,DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener{
    companion object{
        private lateinit var adapter: ArrayColorAdapter
    }

    private var dateFormat:java.text.DateFormat= java.text.DateFormat.getDateInstance(
        java.text.DateFormat.LONG,
        Locale.getDefault()
    )

    private var data:DataRowWithColor? = null
    private lateinit var newDate: Calendar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_edit, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setColorSpinner()

        requireActivity().window.setBackgroundDrawable(ColorDrawable(Color.WHITE))


        if(arguments!=null&&EditTaskSelectedArgs.fromBundle(requireArguments()).dataColor!=null){
            data =EditTaskSelectedArgs.fromBundle(requireArguments()).dataColor
            delete_edit_button.visibility=View.VISIBLE
            name_edit.setText(data!!.name)
            check_category.setSelection(adapter.getPosition(Pair(data!!.category,data!!.color)))
            priority_edit.setText(data!!.priority.toString())
            date_edit.setText(dateFormat.format(data!!.date.time))
            duration_edit.setText(data!!.workingTime.toString())
            hour_edit.setText("" + data!!.date.get(Calendar.HOUR) + ":" + data!!.date.get(Calendar.MINUTE))
            newDate= Calendar.getInstance()
            data!!.date.let {newDate.set(
                it.get(Calendar.YEAR),
                it.get(Calendar.MONTH),
                it.get(Calendar.DAY_OF_MONTH),
                it.get(Calendar.HOUR),
                it.get(Calendar.MINUTE),
                0
            )
                newDate.set(Calendar.MILLISECOND, 0)
            }
            delete_edit_button.setOnClickListener{
                deleteRow(data!!)
                update()
                it.findNavController().navigateUp()
            }

            save_edit_button.setOnClickListener{
                updateRow()
                update()
                it.findNavController().navigateUp()
            }
        }else{
            newDate= Calendar.getInstance()
            newDate.set(Calendar.SECOND, 0)
            newDate.set(Calendar.MILLISECOND, 0)
            date_edit.setText(dateFormat.format(newDate.time))
            hour_edit.setText("" + newDate.get(Calendar.HOUR) + ":" + newDate.get(Calendar.MINUTE))

            delete_edit_button.visibility=View.GONE

            save_edit_button.setOnClickListener{

                addRow(
                    (check_category.selectedItem as Pair<*, *>).first.toString (),
                    name_edit.text.toString(),
                    newDate.time.time,
                    duration_edit.text.toString(),
                    priority_edit.text.toString().toInt()
                )
                update()
                it.findNavController().navigateUp()
            }
        }

        add_color.setOnClickListener{
            it?.findNavController()!!.navigate(R.id.action_FirstFragment_to_colorsFragment2)
        }

        date_edit.setOnClickListener{
            val dp =
                DatePickerDialog(
                    requireContext(),
                    this,
                    newDate.get(Calendar.YEAR),
                    newDate.get(Calendar.MONTH),
                    newDate.get(Calendar.DAY_OF_MONTH)
                )
            dp.show()
        }

        hour_edit.setOnClickListener{
            val tp =  TimePickerDialog(
                requireContext(), this,
                newDate.get(Calendar.HOUR), newDate.get(Calendar.MINUTE), true
            )

            tp.show()
        }

        cancel_edit_button.setOnClickListener{
            it.findNavController().navigateUp()
        }
    }

    private fun updateRow() {
        MainActivity.getDBOpenHelper().editTaskRow(
            data!!.id,
            (check_category.selectedItem as Pair<*, *>).first.toString (),
            name_edit.text.toString(),
            newDate.time.time,
            duration_edit.text.toString(),
            priority_edit.text.toString().toInt()
        )
    }

    private fun addRow(
        category: String,
        name: String,
        startDateTime: Long,
        hours: String,
        priority: Int
    ) {
        MainActivity.getDBOpenHelper().addTaskRow(category, name, startDateTime, hours, priority)
        update()
    }

    private fun deleteRow(dataRowWithColor: DataRowWithColor)= MainActivity.getDBOpenHelper().deleteTaskRow(dataRowWithColor.id)

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        newDate.set(year, month, dayOfMonth)
        newDate.set(Calendar.SECOND, 0)
        newDate.set(Calendar.MILLISECOND, 0)
        date_edit.setText(dateFormat.format(newDate.time))
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        newDate.set(Calendar.HOUR, hourOfDay)
        newDate.set(Calendar.MINUTE, minute)
        newDate.set(Calendar.SECOND, 0)
        newDate.set(Calendar.MILLISECOND, 0)
        hour_edit.setText((if (hourOfDay < 10) "0" else "") + hourOfDay.toString() + ":" + if (minute < 10) "0" else "" + minute.toString())
    }

    private fun update(){
        (context as MainActivity).getDataFromDB()
        EditList.update()
        HomeFragment.fillViewsWithDatabaseData()
    }
    private fun setColorSpinner(){
        val list: Array<String> = MainActivity.getColors().keys.toTypedArray()
        list.sort()

        adapter= ArrayColorAdapter(requireContext(),R.layout.color_edit_element,MainActivity.getColors().toList())
        adapter.setDropDownViewResource(R.layout.color_edit_element)
        check_category.adapter = adapter
    }
}