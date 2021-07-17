package com.voidsamurai.lordoftime.fragments.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R

class LinearChartAdapter(private val dataSet: List<Pair<String,String>>):RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chart_details_element, parent, false)

        return LinearViewHolder(view)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {
        val layout:LinearLayout=holder.layout
        layout.findViewById<View>(R.id.chart_color_block) .setBackgroundColor(Color.parseColor(dataSet[position].second))
        layout.findViewById<TextView>(R.id.chart_text_block).text = dataSet[position].first

    }

}