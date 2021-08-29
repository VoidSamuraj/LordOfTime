package com.voidsamurai.lordoftime.fragments.adapters

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.charts.CalendarElement
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.charts.NTuple5


class CalendarAdapter(private val context: Context, private val dataSet: ArrayList<NTuple5<Int, Float, Boolean, Int, Int?>?>) :

    RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_card_view, parent, false)

        return LinearViewHolder(view)
    }


    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {
        val dm=DisplayMetrics()
        val activity=( context as Activity)//.windowManager. defaultDisplay.getMetrics(dm)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = activity.display
            display?.getRealMetrics(dm)
        } else {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(dm)
        }
        val pad= context.resources.getDimension(R.dimen.small_padding)
        val x=(dm.widthPixels-2*pad)/dataSet[position]!!.t4
        val layout=holder.layout
        val calendarView=layout.findViewById<CalendarElement>(R.id.calendarChart)
        val calendarLinear=layout.findViewById<LinearLayout>(R.id.calendar_linear)
        calendarLinear.layoutParams.width=x.toInt()
        calendarLinear.layoutParams.height=x.toInt()
        dataSet[position]?.let { calendarView.fillData(it.t1,it.t2,it.t3,it.t5)}
    }


    override fun getItemCount(): Int=dataSet.size


}