package com.voidsamurai.lordoftime.fragments.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.voidsamurai.lordoftime.R


class ArrayColorAdapter(
    context: Context,
    @LayoutRes
    private val layoutResource: Int,
    private val list: List<Pair<String, String>>
):
    ArrayAdapter<Pair<String, String>>(context, layoutResource, list){

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var listItem = convertView
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.element_color_edit, parent, false)

        listItem?.findViewById<View>(R.id.chart_color_block)!!.setBackgroundColor(
            Color.parseColor(
                list[position].second
            ))
        listItem.findViewById<TextView>(R.id.chart_text_block)!!.text = list[position].first
        return listItem
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }
}