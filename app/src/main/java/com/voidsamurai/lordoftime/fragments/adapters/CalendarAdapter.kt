package com.voidsamurai.lordoftime.fragments.adapters

import android.app.Activity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.charts.CallendarElement
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.fragments.ManyChartsFragment
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.charts.NTuple4


class CalendarAdapter(private val dataSet: ArrayList<NTuple4<Int, Float, Boolean,Int>?>) :

    RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_card_view, parent, false)

        return LinearViewHolder(view)
    }


    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {
        val dm=DisplayMetrics()
        ( ManyChartsFragment.getContext() as Activity).windowManager.defaultDisplay.getMetrics(dm)

        val pad= ManyChartsFragment.getContext()!!.resources.getDimension(R.dimen.small_padding)
        val x=(dm.widthPixels-2*pad)/dataSet[position]!!.t4
        val layout=holder.layout
        val calendarView=layout.findViewById<CallendarElement>(R.id.callendarChartt)
        val calendarLinear=layout.findViewById<LinearLayout>(R.id.calendar_linear)
        calendarLinear.layoutParams.width=x.toInt()
        calendarLinear.layoutParams.height=x.toInt()
        dataSet[position]?.let { calendarView.fillData(it.t1,it.t2,it.t3)}
    }


    override fun getItemCount(): Int=dataSet.size


}