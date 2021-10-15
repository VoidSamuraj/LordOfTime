package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import com.voidsamurai.lordoftime.databinding.FragmentCalendarDayEditBinding
import java.util.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.marginTop
import com.voidsamurai.lordoftime.MainActivity


class CalendarDayEdit : Fragment() {
    var x:Float?=null
    var y:Float?=null
    var daysData:Calendar= Calendar.getInstance()
    var _CalendarBinding:FragmentCalendarDayEditBinding?=null
    val calendarBinding get() = _CalendarBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _CalendarBinding= FragmentCalendarDayEditBinding.inflate(inflater, container, false)
        return calendarBinding.root
    }

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments!=null){
            val args= requireArguments().getLong("date")
            daysData.time=Date(args)
            Log.v("Time",""+daysData.get(Calendar.YEAR)+"/"+daysData.get(Calendar.MONTH)+"/"+daysData.get(Calendar.DAY_OF_MONTH))
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
                    Log.v("CLICK",""+event.y)
                    val sec=(event.y*86400/v.height)
                    daysData.set(Calendar.HOUR_OF_DAY,(sec/3600).toInt())
                    daysData.set(Calendar.MINUTE,((sec%3600)/60).toInt())
                    daysData.set(Calendar.SECOND,0)
                    daysData.set(Calendar.MILLISECOND,0)
                    val etd=EditTaskDialog(R.layout.fragment_edit_task_dialog,EditTaskDialog.Companion.MODE.SAVE,daysData,event.y)
                    etd.setFrag(this)
                    etd.show(requireActivity().supportFragmentManager,"Task")
                    ret=false
                }
            }
            return@setOnTouchListener ret
        }
        getView()?.post {
            val tasks =
                (activity as MainActivity).getDBOpenHelper().getTodayTasks(daysData.time.time)
            tasks?.let {
                for (task in it)
                    addElement(task)
            }
        }

    }


    fun addElement(drwc: DataRowWithColor, marginTop:Int, dur:Float){
        val button=Button(requireContext())
        button.id=drwc.id
        button.text=drwc.name
        button.backgroundTintList= ColorStateList.valueOf(Color.parseColor(drwc.color))
        setButtonListener(button,drwc)
        button.minHeight=getHeight(dur)
        calendarBinding.parent.addView(button)
        setDimens(marginTop,button)
    }
    fun addElement(drwc: DataRowWithColor){
        val button=Button(requireContext())
        button.id=drwc.id
        button.text=drwc.name
        button.backgroundTintList= ColorStateList.valueOf(Color.parseColor(drwc.color))
        setButtonListener(button,drwc)
        button.minHeight=(drwc.workingTime*calendarBinding.scroll.height/24f).toInt()
        calendarBinding.parent.addView(button)
        val h=drwc.date.get(Calendar.HOUR_OF_DAY)
        val m=drwc.date.get(Calendar.MINUTE).toFloat()/60
        val c=Calendar.getInstance()
        c.time=drwc.date.time
        Log.v("TIMER",""+h+"\\"+m+" "+c.get(Calendar.HOUR_OF_DAY)+"\\"+c.get(Calendar.MINUTE)+" "+drwc.date.time.time)
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

        //setDimens(((m+h)*calendarBinding.parent.height/24).toInt(),button)
        setDimens(((m+h)*resources.getDimension(R.dimen.scroll)/24f).toInt(),button)
    }
    fun getHeight(dur: Float):Int{
        return (dur*calendarBinding.scroll.height/24f).toInt()
    }
    fun setDimens(topMargin:Int,button:Button){
        val set = ConstraintSet()
        set.clone(calendarBinding.parent)
        set.connect(button.id, ConstraintSet.TOP, calendarBinding.parent.id, ConstraintSet.TOP, topMargin)
        set.connect(button.id, ConstraintSet.END, calendarBinding.parent.id, ConstraintSet.END, 0)
        set.connect(button.id, ConstraintSet.START, calendarBinding.parent.id, ConstraintSet.START, 60)
        set.applyTo(calendarBinding.parent)
    }
    fun setButtonListener(button: Button,drwc: DataRowWithColor){
        button.width=calendarBinding.scroll.width-60
        button.setOnClickListener {
            val etd=EditTaskDialog(R.layout.fragment_edit_task_dialog,EditTaskDialog.Companion.MODE.EDIT,daysData,id = drwc.id)
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
    fun getButtonYId(id:Int,pref:Int):Int{
        val but=calendarBinding.parent.findViewById<Button>(id)
        val topB=but.top
        return getButtonY(topB,pref)
    }
    fun getButtonY(y:Int,pref:Int):Int{
        var max:Int=calendarBinding.parent.height-y
        for(child in calendarBinding.parent.children){
            if(child is Button) {
                val topC = child.marginTop
                Log.v("_YTop",""+topC )
                if (topC>y&&(topC-y)<max)
                    max = topC-y
            }
        }
        Log.v("_Y","y:"+y+" pref:"+pref+" max:"+max+" parent:"+calendarBinding.parent.height, )
        return if(max<pref)max else pref
    }
}