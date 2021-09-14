package com.voidsamurai.lordoftime.fragments.adapters

import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.charts_and_views.ProgressCircle
import layout.DataRowWithColor
import kotlin.coroutines.coroutineContext

class StartWorkAdapter(private val activity: MainActivity,private val toDoData: ArrayList<DataRowWithColor>, private val lifecycleOwner: LifecycleOwner):RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.start_work_recycle_element,parent,false)
        return LinearViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {
        val layout=holder.layout
        layout.findViewById<TextView>(R.id.work_label).apply{
            text=toDoData[position].name
            isSelected=true
        }
        if (layout.findViewById<TextSwitcher>(R.id.progressPercent).childCount<2)
        layout.findViewById<TextSwitcher>(R.id.progressPercent).setFactory {
            val textView = TextView(activity.applicationContext)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.resources.getDimension(R.dimen.line_text_size))
            textView
        }

        layout.findViewById<TextView>(R.id.category_name).text=toDoData[position].category
        var current=toDoData[position].currentWorkingTime*3600
        val todo=toDoData[position].workingTime*3600
        layout.findViewById<TextSwitcher>(R.id.progressPercent).setText(String.format("%2.2f",(todo-current)/3600)+"h")
        layout.findViewById<ProgressCircle>(R.id.progressCircle).fillData(current,todo)

        layout.findViewById<ImageButton>(R.id.imageButton).setOnClickListener {

            setObserver(layout,position,current,todo)
        }
        if(activity.currentTaskId==toDoData[position].id)
           setObserver(layout,position,current,todo)
    }
    fun setObserver(layout:LinearLayout,position: Int,currentFDB:Float,todo:Float){
        activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
        //activity.getDBOpenHelper().editTaskRow(activity.currentTaskId,null,null,null,null,0,activity.getCurrentWorkingTime().value.toString())
        activity.getCurrentWorkingTime().value=0
        if(activity.currentTaskId!=toDoData[position].id)
            activity.currentTaskId=toDoData[position].id
        activity.getCurrentWorkingTime().observe(lifecycleOwner,{
            val current=it.toFloat()+currentFDB
            layout.findViewById<TextSwitcher>(R.id.progressPercent).setCurrentText(String.format("%2.2f",(todo-current)/3600)+"h")
            layout.findViewById<ProgressCircle>(R.id.progressCircle).fillData(current,todo)

        })
    }



    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
        activity.getDBOpenHelper().editTaskRow(activity.currentTaskId,null,null,null,null,0,activity.getCurrentWorkingTime().value.toString())
    }

    override fun getItemCount(): Int =toDoData.size
}