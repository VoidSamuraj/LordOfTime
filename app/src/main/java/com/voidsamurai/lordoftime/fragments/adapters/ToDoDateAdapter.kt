package com.voidsamurai.lordoftime.fragments.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ToDoDateAdapter(private val dataSet: ArrayList<DataRowWithColor>): RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.todo_element, parent, false)

        return LinearViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {

        val layout: LinearLayout =holder.layout
        layout.findViewById<TextView>(R.id.todo_name).text = dataSet[position].name
        dataSet[position].outdated?.let {
            if (it) {
                layout.findViewById<TextView>(R.id.todo_name).setTextColor(Color.RED)
                layout.findViewById<TextView>(R.id.todo_date).setTextColor(Color.RED)
            }
        }

        val cal =dataSet[position].date.clone() as  Calendar
        Log.v("Timne",""+ cal.get(Calendar.HOUR_OF_DAY)
            +" "+ cal.get(Calendar.MINUTE))
       // val sdf= SimpleDateFormat("dd/MM/yy HH:mm")
        layout.findViewById<TextView>(R.id.todo_date).text = String.format("%02d/%02d  %02d:%02d"
            , cal.get(Calendar.DAY_OF_MONTH)
            , cal.get(Calendar.MONTH)
            , cal.get(Calendar.HOUR_OF_DAY)
            , cal.get(Calendar.MINUTE))

    }

}
