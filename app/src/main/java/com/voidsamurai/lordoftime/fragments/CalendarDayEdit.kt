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
import com.voidsamurai.lordoftime.databinding.FragmentCalendarDayEditBinding
import com.voidsamurai.lordoftime.fragments.dialogs.EditTaskDialog
import java.util.*


class CalendarDayEdit : Fragment() {
    private var x:Float?=null
    private var y:Float?=null
    private var daysData:Calendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"))
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
    private fun addElement(drwc: DataRowWithColor){
        val button=Button(requireContext())
        button.id=drwc.id
        button.text=drwc.name
        button.backgroundTintList= ColorStateList.valueOf(Color.parseColor(drwc.color))
        setButtonListener(button,drwc)
        button.minHeight=getHeight(drwc.workingTime)
        val h=drwc.date.get(Calendar.HOUR_OF_DAY)
        val m=drwc.date.get(Calendar.MINUTE).toFloat()/60
        calendarBinding.parent.addView(button)
        setDimens(((m+h)*(calendarBinding.parent.height/24)).toInt(),button)

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
        calendarBinding.parent.addView(button2)
        setDimens(((m+h)*resources.getDimension(R.dimen.scroll)/24f).toInt(),button)
    }

    fun getHeight(dur: Float):Int{
        return (dur*calendarBinding.scroll.height/24f).toInt()
    }

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

    fun deleteElement(id:Int){
        val but=calendarBinding.parent.findViewById<Button>(id)
        calendarBinding.parent.removeView(but)
    }

    fun getMaxDur(y:Int,pref:Int):Float{
        val max=getButtonY(y,pref)
        return max.toFloat()/calendarBinding.scroll.height*24
    }
    fun getMaxDur(y:Int,pref:Int,id:Int):Float{
        val max=getButtonY(y,pref,id)
        return max.toFloat()/calendarBinding.scroll.height*24
    }

        fun getStartMargin(nextStartHour:Float,duration:Float,id:Int):Int {
        var canBe=true
        val y=  (nextStartHour * calendarBinding.scroll.height / 24f).toInt()
        val dur=  (duration * calendarBinding.scroll.height / 24f).toInt()

        for(child in calendarBinding.parent.children){
            if(child is Button&&child.id!=id) {
                val topCMargin = child.marginTop
            //    if (!((topCMargin>y&&(topCMargin+child.height)>(y+dur))||(topCMargin<y&&(topCMargin+child.height)<(y+dur))))

                if (!((topCMargin>y&&(topCMargin/*+child.height*/)>(y+dur))||((topCMargin)<y&&(topCMargin+child.height)<y)))
                    canBe = false

            }
        }
        return if(canBe)
            y
        else
            -1
    }
/*
    private fun getButtonY(y:Int, pref:Int, id:Int?=null):Int{
        var max:Int=calendarBinding.parent.height-y
        if (id != null)
            for(child in calendarBinding.parent.children){
                if(child is Button&&id!=child.id) {
                    val topC = child.marginTop
                    if (topC>y&&(topC-y)<max)
                        max = topC-y
                }
            }
        else
            for(child in calendarBinding.parent.children){
                if(child is Button) {
                    val topC = child.marginTop
                    if (topC>y&&(topC-y)<max)
                        max = topC-y
                }
            }
        return if(max<pref)max else pref
    }*/
    private fun getButtonY(y:Int, prefDuration:Int, id:Int?=null):Int{
        var max:Int=calendarBinding.parent.height-y
        if (id != null)
            for(child in calendarBinding.parent.children){
                if(child is Button&&id!=child.id) {
                    val topC = child.marginTop
                    if (topC>y&&(topC-y)<max)
                        max = topC-y
                }
            }
        else
            for(child in calendarBinding.parent.children){
                if(child is Button) {
                    val topC = child.marginTop
                    if (topC>y&&(topC-y)<max)
                        max = topC-y
                }
            }
        return if(max<prefDuration)max else if(prefDuration<=calendarBinding.parent.height) prefDuration else calendarBinding.parent.height
    }
}