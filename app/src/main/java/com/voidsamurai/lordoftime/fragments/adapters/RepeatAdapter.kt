package com.voidsamurai.lordoftime.fragments.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DAORutines
import com.voidsamurai.lordoftime.bd.RutinesRow
import com.voidsamurai.lordoftime.fragments.dialogs.RepeatDialog

class RepeatAdapter (private val data:List<RutinesRow>): RecyclerView.Adapter<LinearViewHolder>() {
    var lastPosition:Int?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_rutines, parent, false)

        return LinearViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {
        val layout: LinearLayout = holder.layout
        val resources = holder.itemView.resources
        val days=data[position].days.split(',')
        Log.v("DAYS",""+days)
        val dates= days.map { s ->  resources.getText(DAORutines.getDayID(s))}.joinToString(separator = "\n")

        layout.findViewById<TextView>(R.id.day).text = dates
        val hours=data[position].hours.split(',')
        layout.findViewById<LinearLayout>(R.id.element).setBackgroundColor(resources.getColor(R.color.transparent,null))

        fun setText(text1:String,text2:String,text3:String){

            val h1=layout.findViewById<TextView>(R.id.hours1)
            val h2=layout.findViewById<TextView>(R.id.hours2)
            val h3=layout.findViewById<TextView>(R.id.hours3)
            h1.text=text1
            h2.text=text2
            h3.text=text3
        }

        if(hours.size>=3){
            val rows=hours.size/3
            setText(
                hours.subList(0,rows).joinToString(separator = "\n"),
                hours.subList(rows,rows*2).joinToString(separator = "\n"),
                hours.subList(rows*2,hours.size).joinToString(separator = "\n"))


        }else if(hours.isNotEmpty()){
            var text2=""
            if(hours.size==2)
                text2=hours[1]
            val text1=hours[0]
            setText(text1,text2,"")
        }

        layout.findViewById<LinearLayout>(R.id.element).setOnClickListener {view->
            RepeatDialog.selectedId=data[position].id
            RepeatDialog.lastPosition=position
            {
                if (lastPosition != null)
                    notifyItemChanged(lastPosition!!)
            }.let {
                lastPosition = position
                (view as LinearLayout).setBackgroundColor(
                    resources.getColor(
                        R.color.recycler_view_checked,
                        null
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int =data.size

}