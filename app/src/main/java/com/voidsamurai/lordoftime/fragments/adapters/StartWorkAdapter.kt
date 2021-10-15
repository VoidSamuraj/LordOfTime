package com.voidsamurai.lordoftime.fragments.adapters

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.charts_and_views.ProgressCircle
import com.voidsamurai.lordoftime.fragments.WorkingFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import java.util.*

class StartWorkAdapter(private val activity: MainActivity, private var toDoData: ArrayList<DataRowWithColor>, private val lifecycleOwner: LifecycleOwner):RecyclerView.Adapter<LinearViewHolder>() {
    private lateinit var layout:LinearLayout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.start_work_recycle_element,parent,false)
        return LinearViewHolder(view)
    }


    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        layout = holder.layout

        layout.findViewById<TextView>(R.id.work_label).apply {
            text = toDoData[position].name
            isSelected = true
        }
        if (layout.findViewById<TextSwitcher>(R.id.progressPercent).childCount < 2)
            layout.findViewById<TextSwitcher>(R.id.progressPercent).setFactory {
                val textView = TextView(activity.applicationContext)
                textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    activity.resources.getDimension(R.dimen.small_line_text_size)
                )
                textView
            }

        if (!activity.isTaskStarted)
            layout.findViewById<ImageButton>(R.id.imageButton)
                .setImageResource(R.drawable.ic_baseline_play_arrow_24)

        else {
            if (activity.currentTaskId == toDoData[position].id){
                activity.lastButton=layout.findViewById(R.id.imageButton)
                layout.findViewById<ImageButton>(R.id.imageButton)
                    .setImageResource(R.drawable.ic_baseline_stop_24)
            }
            else
                layout.findViewById<ImageButton>(R.id.imageButton)
                    .setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        layout.findViewById<TextView>(R.id.category_name).text = toDoData[position].category

        val current = toDoData[position].currentWorkingTime.toInt()
        val todo = toDoData[position].workingTime

        val message=if ((todo.toInt()-current) > 0)
            (todo - current.toFloat()) / 3600f
        else
            current.toFloat()/3600f
        layout.findViewById<TextSwitcher>(R.id.progressPercent)
            .setText(String.format("%2.2f",
                message
            )  + "h")
        layout.findViewById<ProgressCircle>(R.id.progressCircle).fillData(current.toFloat(), todo)



        layout.findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            layout=holder.layout
            activity.currentTaskId = toDoData[position].id

            if (activity.isTaskStarted) {

                val pos = activity.lastTaskPositioon

                if (pos != null && pos != position) {

                    updateDB(pos, activity.lastTaskId!!)
                    activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
                    activity.getDataFromDB()
                    activity.lastTaskPositioon = position
                    activity.lastTaskId = activity.currentTaskId
                    activity.lastButton!!.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    activity.lastButton=layout.findViewById(R.id.imageButton)
                    notifyItemChanged(pos)
                    notifyItemChanged(position)
                    activity.getCurrentWorkingTime().value = 0

                    setObserver(layout, todo, current, position, activity.currentTaskId!!)

                    //for the same result as before
                    activity.isTaskStarted = activity.isTaskStarted.not()
                } else {

                    updateDB(position, activity.currentTaskId!!)
                    notifyItemChanged(position)
                    activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
                    activity.getDataFromDB()
                }
            } else {

                layout.findViewById<ImageButton>(R.id.imageButton)
                    .setImageResource(R.drawable.ic_baseline_stop_24)
                activity.lastTaskPositioon = position
                activity.lastTaskId = activity.currentTaskId
                activity.lastButton=layout.findViewById(R.id.imageButton)
                activity.getCurrentWorkingTime().value = 0
                if(todo-current>0)
                    setObserver(layout, todo, current, position, activity.currentTaskId!!)
                else
                    setEndedObserver(layout, current)

            }
            activity.isTaskStarted = activity.isTaskStarted.not()


        }


        fun endStyle(){
            layout.findViewById<TextView>(R.id.progress_label).setText(R.string.ya_working)
            layout.findViewById<ImageButton>(R.id.imageButton).setColorFilter(Color.parseColor("#D1A441"))

        }
        if(activity.isTaskStarted && position == activity.lastTaskPositioon )
            if ((todo - current) > 0){
                layout.findViewById<TextView>(R.id.progress_label).setText(R.string.left)
                setObserver(layout, todo, current, position, activity.currentTaskId!!)
            }else {
                endStyle()
                setEndedObserver(layout, current)
            }
        else if(todo-current<0)
            endStyle()

    }




    fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        val itemToRemove = toDoData[viewHolder.adapterPosition]
        val pos = viewHolder.adapterPosition
        val isStarted=activity.isTaskStarted

        if(isStarted&&activity.taskId==toDoData[pos].id)
        {
            updateDB(viewHolder.adapterPosition, activity.currentTaskId!!)
            notifyItemChanged(viewHolder.adapterPosition)
            activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
            activity.getDataFromDB()
            activity.isTaskStarted=false
            activity.getCurrentWorkingTime().value=0
        }

        activity.getDBOpenHelper().deleteTaskRow(toDoData[viewHolder.adapterPosition].id)
        activity.tasks.delete(toDoData[viewHolder.adapterPosition].id.toString())
        activity.getDataFromDB()
        toDoData.removeAt(pos)
        notifyItemRemoved(pos)
        Snackbar.make(viewHolder.itemView,"Usunięto "+itemToRemove.name,Snackbar.LENGTH_LONG).setAction("Cofnij"){
            val id=activity.getDBOpenHelper().addTaskRow(itemToRemove)
            activity.tasks.add(itemToRemove)

            activity.getDataFromDB()
            itemToRemove.id=id.toInt()
            toDoData.add(pos,itemToRemove)
            notifyItemInserted(pos)

        }.show()
    }
    fun editItem(position: Int){
        val action: WorkingFragmentDirections.ActionWorkingFragmentToEditTaskSelected =
            WorkingFragmentDirections.actionWorkingFragmentToEditTaskSelected().setDataColor(toDoData[position])
        findNavController(layout).navigate(action)
    }
    fun updateDB(position: Int,id:Int){
        if(activity.getCurrentWorkingTime().hasObservers())
            activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
        val time=activity.getCurrentWorkingTime().value!! +toDoData[position].currentWorkingTime.toInt()
        if(time!=0) {
            val oh=activity.getDBOpenHelper()
            oh.editTaskRow(
                id,
                null, null, null, 0, 0, time
            )
            activity.tasks.add( id = id,
                category = toDoData[position].category, name = toDoData[position].name, dateTime = toDoData[position].date.time.time, workingTime = toDoData[position].workingTime.toInt(), toDoData[position].priority, time)

            oh.addOldstatRow(Calendar.getInstance().time.time,activity.getCurrentWorkingTime().value!!,toDoData[position].category)
            activity.oldTasks.add(dateTime = Calendar.getInstance().time.time,currentWorkingTime = activity.getCurrentWorkingTime().value!!,category =toDoData[position].category )
        }

        toDoData[position].currentWorkingTime=time.toFloat()

        // notifyItemChanged(position)
        activity.getCurrentWorkingTime().value=0
    }


    fun setObserver(layout: View, todo:Float, current:Int, position: Int, id: Int){
        if(activity.getCurrentWorkingTime().hasObservers())
            activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
        activity.getCurrentWorkingTime().observe(lifecycleOwner,{
            val curr=it.toFloat()+current
            val left =(todo - curr) / 3600
            if(left>0) {
                CoroutineScope(Dispatchers.Main).launch {
                    layout.findViewById<TextSwitcher>(R.id.progressPercent)
                        .setCurrentText(String.format("%2.2f", left) + "h")
                    layout.findViewById<ProgressCircle>(R.id.progressCircle)
                        .fillData(curr, todo)
                }
            }
            else{
                updateDB( position, id)
                activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
                activity.getCurrentWorkingTime().value=0
                notifyItemChanged(position)
            }

        })
    }
    fun setEndedObserver(layout:View,current:Int){
        layout.findViewById<ProgressCircle>(R.id.progressCircle).fillData(1f, 1f)
        if(activity.getCurrentWorkingTime().hasObservers())
            activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
        activity.getCurrentWorkingTime().value=0
        activity.getCurrentWorkingTime().observe(lifecycleOwner,{
            val curr=it.toFloat()+current
            CoroutineScope(Dispatchers.Main).launch {
                layout.findViewById<TextSwitcher>(R.id.progressPercent)
                    .setCurrentText(String.format("%2.2f", curr/3600) + "h")

            }
        })
    }



    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
        /*activity.getCurrentWorkingTime().value?.let {
            if(it+current!!!=0){
            activity.getDBOpenHelper().editTaskRow(activity.currentTaskId!!,null,null,null,0,0,
                it+current!!)
                Log.v("wartosci","time:"+it+" cur:"+current)
            }else
                Log.v("brak wartosci","")


        }*/
    }

    override fun getItemCount(): Int =toDoData.size
}