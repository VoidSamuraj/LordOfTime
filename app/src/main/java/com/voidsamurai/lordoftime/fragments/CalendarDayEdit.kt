package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import com.voidsamurai.lordoftime.calendarToRead
import com.voidsamurai.lordoftime.databinding.FragmentCalendarDayEditBinding
import com.voidsamurai.lordoftime.fragments.dialogs.EditTaskDialog
import com.voidsamurai.lordoftime.timeToRead
import java.util.*

/**
 * Fragment witch displays scrollable view of tasks in day.
 */
class CalendarDayEdit : Fragment() {
    private var x:Float?=null
    private var y:Float?=null
    private var daysData:Calendar= Calendar.getInstance()
    private var _CalendarBinding:FragmentCalendarDayEditBinding?=null
    private val calendarBinding get() = _CalendarBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _CalendarBinding= FragmentCalendarDayEditBinding.inflate(inflater, container, false)
        return calendarBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments!=null){
            val args= requireArguments().getLong("date")
            daysData.timeInMillis=args
        }
        calendarBinding.scroll.setOnTouchListener { v, event ->
            var ret=false
            when(event.action){
                MotionEvent.ACTION_DOWN->{
                    x=event.x
                    y=event.y
                    ret=true
                }
                MotionEvent.ACTION_UP->{
                    val sec=(event.y*86400/v.height)
                    val c = daysData.clone() as Calendar
                    c.set(Calendar.MINUTE,((sec%3600)/60).toInt())
                    c.set(Calendar.HOUR_OF_DAY,(sec/3600).toInt())
                    c.set(Calendar.SECOND,0)
                    c.set(Calendar.MILLISECOND,0)
                    val etd= EditTaskDialog(R.layout.dialog_edit_task,
                        EditTaskDialog.Companion.MODE.SAVE,c,event.y.toInt())
                    etd.setFrag(this)
                    etd.show(requireActivity().supportFragmentManager,"Task")
                    ret=false
                }
            }
            return@setOnTouchListener ret
        }
        getView()?.post {
            val tasks = (activity as MainActivity).getDBOpenHelper().getTodayTasks(daysData.time.time,(activity as MainActivity).userId)
            tasks?.let {
                for (task in it)
                    addElement(task)
            }
        }

    }

    /**
     * Function to add new button (task)
     * @param drwc  - data of new task
     * @param marginTop - top margin of new button
     */
    fun addElement(drwc: DataRowWithColor, marginTop:Int){
        val button=Button(requireContext())
        button.id=drwc.id
        button.text=drwc.name
        button.backgroundTintList= ColorStateList.valueOf(Color.parseColor(drwc.color))
        setButtonListener(button,drwc)
        button.minHeight=getHeight(drwc.workingTime)
        calendarBinding.parent.addView(button)
        setDimens(marginTop,button)
    }
    /**
     * Function to add new button (task). Top margin will be calculated basing on date in parameter.
     * @param drwc  - data of new task
     */
    private fun addElement(drwc: DataRowWithColor){
        val ctr=drwc.date.calendarToRead()
        val h=ctr.get(Calendar.HOUR_OF_DAY)
        val m=ctr.get(Calendar.MINUTE).toFloat()/60
        addElement(drwc,((m+h)*(calendarBinding.parent.height/24)).toInt())
    }
    fun editElement(drwc: DataRowWithColor){
        val button=calendarBinding.parent.findViewById<Button>(drwc.id)
        val button2=Button(requireContext())
        calendarBinding.parent.removeView(button)
        button2.id=drwc.id
        button2.text=drwc.name
        button2.backgroundTintList= ColorStateList.valueOf(Color.parseColor(drwc.color))
        setButtonListener(button2,drwc)
        button2.minHeight=getHeight(drwc.workingTime)
        val h=drwc.date.get(Calendar.HOUR_OF_DAY)
        val m=drwc.date.get(Calendar.MINUTE).toFloat()/60
        val hour=(drwc.date.timeToRead()-drwc.date.timeInMillis).toFloat()/3600_000f+h+m
        calendarBinding.parent.addView(button2)
        setDimens(((hour)*resources.getDimension(R.dimen.scroll)/24f).toInt(),button)
    }

    /**
     * Function to get calculated height from duration.
     * @param dur - duration of task. Number between 0.0 and 24.0
     * @return calculated height based on duration.
     */
    fun getHeight(dur: Float):Int{
        return (dur*calendarBinding.scroll.height/24f).toInt()
    }

    /**
     * Function to set dimensions of button in view
     * @param topMargin - y position of button
     * @param button - button to which positions will be assigned
     */
    private fun setDimens(topMargin:Int, button:Button){
        val set = ConstraintSet()
        set.clone(calendarBinding.parent)
        set.connect(button.id, ConstraintSet.TOP, calendarBinding.parent.id, ConstraintSet.TOP, topMargin)
        set.connect(button.id, ConstraintSet.END, calendarBinding.parent.id, ConstraintSet.END, 0)
        set.connect(button.id, ConstraintSet.START, calendarBinding.parent.id, ConstraintSet.START, 60)
        set.applyTo(calendarBinding.parent)
    }

    private fun setButtonListener(button: Button, drwc: DataRowWithColor){
        button.width=calendarBinding.scroll.width-60
        button.setOnClickListener {
            val etd= EditTaskDialog(R.layout.dialog_edit_task,
                EditTaskDialog.Companion.MODE.EDIT,daysData.clone() as Calendar,id = drwc.id)
            etd.setFrag(this)
            etd.show(requireActivity().supportFragmentManager,"Task")
        }
    }

    /**
     * Function to delete button from view with specified id
     * @param id - id of element to remove
     */
    fun deleteElement(id:Int){
        val but=calendarBinding.parent.findViewById<Button>(id)
        calendarBinding.parent.removeView(but)
    }

    /**
     * Function to calculate max hours for task in selected place.
     * @param y - y position of new button (task)
     * @param pref - preferred duration of task
     * @param id - id of existing button (when editing task) or null
     */
    fun getMaxDur(y:Int,pref:Int,id:Int?=null):Float{
        val max=getButtonY(y,pref, id)
        return max.toFloat()/calendarBinding.scroll.height*24
    }

    /**
     * Function to get top margin of new button, calculated by position of click
     * @param nextStartHour - hour in which task will start
     * @param duration - duration of task
     * @param id - id of existing button (when editing task) or null
     * @return calculated margin if possible to place element with specified duration in y position, else -1.
     */
    fun getStartMargin(nextStartHour:Float,duration:Float,id:Int?=-1):Int {
        var canBe=true
        val y=  (nextStartHour * calendarBinding.scroll.height / 24f).toInt()
        val dur=  (duration * calendarBinding.scroll.height / 24f).toInt()

        for(child in calendarBinding.parent.children){
            if(child is Button&&child.id!=id) {
                val topCMargin = child.marginTop
                if (!((topCMargin>y&&(topCMargin/*+child.height*/)>(y+dur))||((topCMargin)<y&&(topCMargin+child.height)<y)))
                    canBe = false

            }
        }
        return if(canBe)
            y
        else
            -1
    }

    /**
     * Function to calculate max possible height of component
     * @param y - y position of new button (task)
     * @param prefDuration - preferred duration of task
     * @param id - id of existing button (when editing task) or null
     * @return prefDuration if there is space for new task. Max free space if area between y and y + prefDuration is occupied by other button. Else returns parent height
     */
    private fun getButtonY(y:Int, prefDuration:Int, id:Int?=null):Int{
        var max:Int=calendarBinding.parent.height-y
            for(child in calendarBinding.parent.children){
                if(child is Button && (id==null || id!=child.id)) {
                    val topC = child.marginTop
                    if (topC>y && (topC-y)<max)
                        max = topC-y
                }
            }
        return if(max<prefDuration)max else if(prefDuration<=calendarBinding.parent.height) prefDuration else calendarBinding.parent.height
    }
}