package com.voidsamurai.lordoftime.fragments.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor

class ToDoAdapter(private val dataSet: ArrayList<DataRowWithColor>, private val activity:MainActivity): RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_todo, parent, false)

        return LinearViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {

        val layout: LinearLayout =holder.layout
        val imageView=layout.findViewById<ImageView>(R.id.imageView)
        if(dataSet[position].finished==1||(dataSet[position].workingTime- dataSet[position].currentWorkingTime)<=0) {

            imageView.setImageDrawable(
                activity.resources.getDrawable(
                    R.drawable.ic_baseline_check_circle_outline_24,
                    null
                )
            )
        }
        imageView.setOnClickListener {
            dataSet[position].let {
                activity.getDBOpenHelper().editTaskRow(it.id,null,null,null,0,0,0,if(it.finished==0)1 else 0)
                activity.getDataFromDB()
                notifyItemChanged(position)

            }
        }

        layout.findViewById<TextView>(R.id.todo_name).text = dataSet[position].name
        dataSet[position].outdated?.let {
            if (it)
                layout.findViewById<TextView>(R.id.todo_name).setTextColor(Color.RED)
        }

    }

}