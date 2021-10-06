package com.voidsamurai.lordoftime.fragments.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor

class ToDoAdapter(private val dataSet: ArrayList<DataRowWithColor>): RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.todo_element, parent, false)

        return LinearViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {

        val layout: LinearLayout =holder.layout
        layout.findViewById<TextView>(R.id.todo_name).text = dataSet[position].name
        dataSet[position].outdated?.let {
            if (it)
                layout.findViewById<TextView>(R.id.todo_name).setTextColor(Color.RED)
        }

    }

}