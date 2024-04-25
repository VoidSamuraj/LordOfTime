package com.voidsamurai.lordoftime.fragments.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import com.voidsamurai.lordoftime.calendarToRead
import java.util.Calendar

class ToDoDateAdapter(private val dataSet: ArrayList<DataRowWithColor>, private val context: android.content.Context): RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_todo, parent, false)

        return LinearViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {

        val layout: LinearLayout =holder.layout
        layout.findViewById<TextView>(R.id.todo_name).text = dataSet[position].name

        val now= Calendar.getInstance().timeInMillis//dataSet[position].date.timeInMillis//.timeToRead()//-(2*TimeZone.getDefault().rawOffset)
        val prev=dataSet[position].date.timeInMillis
        val workingTime=dataSet[position].workingTime.toInt()*1000

        if (now>(prev+workingTime)) {
            layout.findViewById<TextView>(R.id.todo_name).setTextColor(Color.RED)
            layout.findViewById<TextView>(R.id.todo_date).setTextColor(Color.RED)
        }else if(now>prev){
            val color= context.resources.getColor(R.color.task_to_do,null)
            layout.findViewById<TextView>(R.id.todo_name).setTextColor(color)
            layout.findViewById<TextView>(R.id.todo_date).setTextColor(color)
        }

      //  }
        layout.findViewById<ImageView>(R.id.imageView).visibility=View.GONE
        val cal =(dataSet[position].date.clone() as  Calendar).calendarToRead()

        layout.findViewById<TextView>(R.id.todo_date).text = String.format("%02d/%02d  %02d:%02d"
            , cal.get(Calendar.DAY_OF_MONTH)
            , cal.get(Calendar.MONTH)+1
            , cal.get(Calendar.HOUR_OF_DAY)
            , cal.get(Calendar.MINUTE))

    }

}
