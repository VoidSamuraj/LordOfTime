package com.voidsamurai.lordoftime.fragments.adapters


import android.content.Intent
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
import com.voidsamurai.lordoftime.BackgroundTimeService
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import com.voidsamurai.lordoftime.charts_and_views.ProgressCircle
import com.voidsamurai.lordoftime.fragments.WorkingFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class StartWorkAdapter(private val activity: MainActivity, private var toDoData: ArrayList<DataRowWithColor>, private val lifecycleOwner: LifecycleOwner):RecyclerView.Adapter<LinearViewHolder>() {

    private lateinit var layout: LinearLayout
    private var changeFromObserverToEndObserver = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_start_work_recycle, parent, false)
        return LinearViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {

        holder.setIsRecyclable(false)

        layout = holder.layout

        layout.findViewById<TextView>(R.id.work_label).apply {
            text = toDoData[position].name
            isSelected = true  //for moving text
        }
        if (layout.findViewById<TextSwitcher>(R.id.progressPercent).childCount < 2)
            layout.findViewById<TextSwitcher>(R.id.progressPercent).setFactory {
                val textView = TextView(activity.applicationContext)
                textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    activity.resources.getDimension(R.dimen.normal_text_size)
                )
                textView.setTextColor(layout.findViewById<TextView>(R.id.category_name).currentTextColor)
                textView
            }

        if (!activity.isTaskStarted)
            layout.findViewById<ImageButton>(R.id.imageButton)
                .setImageResource(R.drawable.ic_baseline_play_arrow_24)
        else {
            if (activity.currentTaskId == toDoData[position].id) {
                activity.lastButton = layout.findViewById(R.id.imageButton)
                layout.findViewById<ImageButton>(R.id.imageButton)
                    .setImageResource(R.drawable.ic_baseline_stop_24)
            } else
                layout.findViewById<ImageButton>(R.id.imageButton)
                    .setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
        layout.findViewById<TextView>(R.id.category_name).text = toDoData[position].category

        val current = toDoData[position].currentWorkingTime.toInt()
        val todo = toDoData[position].workingTime

        val message = if ((todo.toInt() - current) > 0)
            (todo - current.toFloat()) / 3600f
        else
            current.toFloat() / 3600f
        layout.findViewById<TextSwitcher>(R.id.progressPercent)
            .setText(
                String.format(
                    "%2.2f",
                    message
                ) + "h"
            )
        layout.findViewById<ProgressCircle>(R.id.progressCircle).fillData(current.toFloat(), todo)


        layout.findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            layout = holder.layout
            activity.currentTaskId = toDoData[position].id

            if (activity.isTaskStarted) {

                val pos = activity.lastTaskPositioon

                if (pos != null && pos != position) {

                    updateDB(pos, activity.lastTaskId!!)
                    deleteObservers()
                    activity.setCurrentTaskId(toDoData[position].id)
                    activity.setTaskCategory(toDoData[position].category)
                    activity.getDataFromDB()
                    setIsRunning(false)
                    activity.lastTaskPositioon = position
                    activity.lastTaskId = activity.currentTaskId
                    activity.lastButton!!.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    activity.lastButton = layout.findViewById(R.id.imageButton)
                    notifyItemChanged(pos)
                    notifyItemChanged(position)
                    setTime(0)
                    activity.setStartTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time)
                    setObserver(layout, todo, current, position, activity.currentTaskId!!)

                } else {
                    updateDB(position, activity.currentTaskId!!)
                    deleteObservers()
                    setIsRunning(false)
                    activity.getDataFromDB()
                    notifyItemChanged(position)
                    setTime(0)


                }
            } else {
                activity.setStartTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time)
                setTime(0)
                setIsRunning(true)
                activity.setCurrentTaskId(toDoData[position].id)
                activity.setTaskCategory(toDoData[position].category)
                layout.findViewById<ImageButton>(R.id.imageButton)
                    .setImageResource(R.drawable.ic_baseline_stop_24)
                activity.lastTaskPositioon = position
                activity.lastTaskId = activity.currentTaskId
                activity.lastButton = layout.findViewById(R.id.imageButton)

                if (todo - current > 0)
                    setObserver(layout, todo, current, position, activity.currentTaskId!!)
                else
                    setEndedObserver(position, layout, current, true)

            }
        }

        fun setEndStyle() {
            layout.findViewById<TextView>(R.id.progress_label).setText(R.string.ya_working)
            layout.findViewById<ImageButton>(R.id.imageButton)
                .setColorFilter(Color.parseColor("#D1A441"))

        }

        if (activity.isTaskStarted && position == activity.lastTaskPositioon) {
            val currentFull = activity.getCurrentWorkingTime().value as Int + current
            if ((todo - currentFull) > 0) {
                setIsRunning(true)
                layout.findViewById<TextView>(R.id.progress_label).setText(R.string.left)
                if (!activity.getCurrentWorkingTime().hasObservers())
                    activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
                setObserver(
                    layout, todo, currentFull, position, activity.currentTaskId!!
                )
            } else {
                setIsRunning(true)
                setEndStyle()
                if (!activity.getCurrentWorkingTime().hasObservers())
                    activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
                setEndedObserver(position, layout, currentFull, !changeFromObserverToEndObserver)
                changeFromObserverToEndObserver = false
            }
        } else if (todo - current <= 0)
            setEndStyle()

    }

    fun editItem(position: Int) {
        val action: WorkingFragmentDirections.ActionWorkingFragmentToEditTaskSelected =
            WorkingFragmentDirections.actionWorkingFragmentToEditTaskSelected()
                .setDataColor(toDoData[position])
        findNavController(layout).navigate(action)
    }

    fun updateDB(position: Int, id: Int) {
        if (activity.getCurrentWorkingTime().hasObservers())
            deleteObservers()
        val time =
            activity.getCurrentWorkingTime().value!! + toDoData[position].currentWorkingTime.toInt()
        if (time != 0) {
            val oh = activity.getDBOpenHelper()
            oh.editTaskRow(
                id,
                null, null, null, 0, 0, time, -1
            )
            activity.tasks.add(
                id = id,
                category = toDoData[position].category,
                name = toDoData[position].name,
                dateTime = toDoData[position].date.time.time,
                workingTime = toDoData[position].workingTime.toInt(),
                toDoData[position].priority,
                time,
                toDoData[position].finished
            )
            activity.getCurrentWorkingTime().value?.let {
                oh.addOldstatRow(
                    Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time, it,
                    toDoData[position].category,
                    activity.userId
                )
                activity.oldTasks.add(
                    dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time,
                    currentWorkingTime = it,
                    category = toDoData[position].category
                )
            }
        }

        toDoData[position].currentWorkingTime = time.toFloat()

    }

    fun setObserver(layout: View, todo: Float, current: Int, position: Int, id: Int) {
        if (activity.getCurrentWorkingTime().hasObservers())
            deleteObservers()
        setTime(0)
        setIsRunning(true)
        startIntent()

        activity.getCurrentWorkingTime().observe(lifecycleOwner) {
            val curr = it.toFloat() + current
            var left = (todo - curr)
            if (left > 0) {
                left /= 3600

                CoroutineScope(Dispatchers.Main).launch {
                    layout.findViewById<TextSwitcher>(R.id.progressPercent)
                        .setCurrentText(String.format("%2.2f", left) + "h")
                    layout.findViewById<ProgressCircle>(R.id.progressCircle)
                        .fillData(curr, todo)
                }
            } else {
                updateDB(position, id)
                deleteObservers()
                changeFromObserverToEndObserver = true
                setTime(0)
                notifyItemChanged(position)
            }
        }
    }

    fun deleteObservers(stopService: Boolean = true) {

        if (stopService) {
            val intent = startIntent()
            activity.stopService(intent)
            activity.notificationService.removeNotification()
        }
        activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
    }
    companion object{
        fun deleteObservers(activity: MainActivity, lifecycleOwner: LifecycleOwner) {
            val intent = startIntent(activity)
            activity.stopService(intent)
            activity.notificationService.removeNotification()
            activity.getCurrentWorkingTime().removeObservers(lifecycleOwner)
        }

        private fun startIntent(activity: MainActivity): Intent =
            Intent(activity, BackgroundTimeService::class.java).also {
                activity.startService(it)
            }
    }
    private fun startIntent(): Intent =
        Intent(activity, BackgroundTimeService::class.java).also {
            activity.startService(it)
        }


    /**
     * Observer witch completed task style
     *
     * @param cleanStart - if true starting from 0, if false starts from currentTime
     * */
    fun setEndedObserver(position: Int,layout:View, currentTime:Int, cleanStart:Boolean=false){

        layout.findViewById<ProgressCircle>(R.id.progressCircle).fillData(1f, 1f)
        if(activity.getCurrentWorkingTime().hasObservers())
            deleteObservers()

        val t=if(cleanStart)
            0
        else {
            activity.startFinishedNotification(toDoData[position].id,toDoData[position].name)
            currentTime
        }

        setTime(t)
        setIsRunning(true)
        startIntent()
        activity.getCurrentWorkingTime().observe(lifecycleOwner) {
            val curr = (it.toFloat() + currentTime) / 3600
            CoroutineScope(Dispatchers.Main).launch {
                layout.findViewById<TextSwitcher>(R.id.progressPercent)
                    .setCurrentText(String.format("%2.2f", curr) + "h")
            }
        }

    }


    private fun setTime(time:Int){
        activity.getCurrentWorkingTime().value = time
        activity.notificationService.setTime(time)
    }

    private fun setIsRunning(isRunning:Boolean){
        activity.setIsRunningTask(isRunning)
        activity.notificationService.setIsRunning(isRunning)
        activity.isTaskStarted=isRunning
    }



    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        deleteObservers(false)
    }

    override fun getItemCount(): Int =toDoData.size
}


