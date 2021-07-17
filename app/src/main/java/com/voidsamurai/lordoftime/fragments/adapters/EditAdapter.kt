package com.voidsamurai.lordoftime.fragments.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.fragments.EditListDirections
import layout.DataRowWithColor
import java.util.*


class EditAdapter(private val dataSet: ArrayList<DataRowWithColor>) :

    RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.todo_edit_element, parent, false)

        return LinearViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {

        val layout: LinearLayout = holder.layout
        layout.findViewById<TextView>(R.id.todo_name).text = (dataSet[position].name)
        layout.findViewById<TextView>(R.id.priority_value).text = (dataSet[position].priority.toString())
        layout.findViewById<TextView>(R.id.category_val).text = (dataSet[position].category)
        layout.findViewById<TextView>(R.id.duration_val).text = dataSet[position].workingTime.toString()
        val df:java.text.DateFormat= java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG,
            Locale.getDefault())
        layout.findViewById<TextView>(R.id.start_val).text = df.format(dataSet[position].date.time)
        layout.findViewById<RelativeLayout>(R.id.relative_edit).background =
            (0x7F000000 or (Color.parseColor(dataSet[position].color) and 0xFFFFFF)).toDrawable()
        layout.findViewById<RelativeLayout>(R.id.relative_edit).setOnClickListener { v: View? ->
            val action: EditListDirections.ActionListChangerToEditTask =
                EditListDirections.actionListChangerToEditTask().setDataColor(dataSet[position])
            v?.findNavController()!!.navigate(action)

        }


    }
}