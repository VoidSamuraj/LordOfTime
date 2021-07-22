

package com.voidsamurai.lordoftime.fragments

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.LOTDatabaseHelper
import com.voidsamurai.lordoftime.charts.NTuple5
import com.voidsamurai.lordoftime.fragments.adapters.CalendarAdapter
import kotlinx.android.synthetic.main.fragment_date_widget.*
import java.text.SimpleDateFormat
import java.util.*


class DateChartFragment : Fragment() {

    companion object{
        var dayAimH:Int=6
        lateinit var dayAim:TextView

    }
    private var productiveDays:Int=0
    private var workPer:Float=0f
    private var productivePer:Float=0f
    private lateinit var summary:TextView
    private lateinit var dayAimButton:LinearLayout
    private lateinit var weeks1:Array<RecyclerView>
    private lateinit var weeks2:Array<RecyclerView>
    private lateinit var hostLinearLayout:LinearLayout
    private lateinit var hostLinearLayout2:LinearLayout
    private lateinit var monthLabel: TextSwitcher
    private lateinit var dateBack: ImageButton
    private lateinit var dateForward: ImageButton
    private lateinit var monthButton:Button
    private lateinit var weekButton:Button
    private lateinit var dayButton:Button
    private var date:Calendar= Calendar.getInstance()
    private lateinit var db: SQLiteDatabase
    private lateinit var oh: LOTDatabaseHelper
    private lateinit var nc:NavController
    private val ydf= SimpleDateFormat("YYYY")
    private val mdf= SimpleDateFormat("MM/YY")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oh = LOTDatabaseHelper(requireContext())
        db = oh.readableDatabase
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_date_widget, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        summary=summary_text
        dayAim=hours
        dayAimButton=numberPicker
        hostLinearLayout=date_layout
        hostLinearLayout2=date_layout2
        nc = findNavController()
        monthLabel=current_month_label
        dateBack=date_back
        dateForward=date_forward
        monthButton=month_button
        weekButton=week_button
        dayButton=day_button
        weeks1 = arrayOf( first_week,second_week,third_week,fourth_week,fifth_week,sixth_week)
        weeks2 = arrayOf( first_week2,second_week2,third_week2,fourth_week2,fifth_week2,sixth_week2)

        createDaysCalendarChart(Calendar.getInstance(),true, weeks1)
        createDaysCalendarChart(Calendar.getInstance(),true, weeks2)

        fun fillLabels(){
            productivePer=Math.round(productivePer*100).toFloat()/100
            workPer=Math.round(workPer*100).toFloat()/100
            if (workPer>100)workPer=100f
            summary.text="Produktywne dni: $productiveDays dni. Wydajność:$workPer%"
        }
        fun fillAim(){
            dayAim.text=dayAimH.toString()
          fillLabels()
        }
        fun updateAfterAimChange(){
          var id=materialButtonToggleGroup.checkedButtonId
            if(id==R.id.day_button)
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createDaysCalendarChart(date,true,weeks1)
                else
                    createDaysCalendarChart(date,true,weeks2)
            else if (id==R.id.week_button)
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createWeekCalendarChart(date,true,weeks1)
                else
                    createWeekCalendarChart(date,true,weeks2)
            else if (id==R.id.month_button)
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createMonthCalendarChart(date,true,weeks1)
                else
                    createMonthCalendarChart(date,true,weeks2)
            fillLabels()
        }
        dayAim.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                updateAfterAimChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        fun slideNone(){
            monthLabel.inAnimation= null
            monthLabel.outAnimation= null
            hostLinearLayout.animation=null
            hostLinearLayout2.animation=null
        }

        fun slideRight(){
            monthLabel.inAnimation= AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
            monthLabel.outAnimation= AnimationUtils.loadAnimation(context, R.anim.slide_out_left)
            if (hostLinearLayout.visibility==View.VISIBLE){
                hostLinearLayout2.visibility=View.VISIBLE
                hostLinearLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left))
                hostLinearLayout2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right))
                hostLinearLayout.visibility=View.INVISIBLE

            }
            else{
                hostLinearLayout.visibility=View.VISIBLE
                hostLinearLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right))
                hostLinearLayout2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left))
                hostLinearLayout2.visibility=View.INVISIBLE

            }

        }
        fun slideLeft(){
            monthLabel.inAnimation= AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
            monthLabel.outAnimation= AnimationUtils.loadAnimation(context, R.anim.slide_out_right)

            if (hostLinearLayout.visibility==View.VISIBLE){
                hostLinearLayout2.visibility=View.VISIBLE
                hostLinearLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_right))
                hostLinearLayout2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left))
                hostLinearLayout.visibility=View.INVISIBLE

            }
            else{
                hostLinearLayout.visibility=View.VISIBLE
                hostLinearLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left))
                hostLinearLayout2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_right))
                hostLinearLayout2.visibility=View.INVISIBLE

            }

        }
        dayButton.callOnClick()

        monthButton.setOnClickListener{

            date= Calendar.getInstance()
            slideNone()
            monthLabel.setText(ydf.format(date.time))
            if (hostLinearLayout.visibility==View.VISIBLE)
                createMonthCalendarChart(date,true,weeks1)
            else
                createMonthCalendarChart(date,true,weeks2)
            fillAim()

            dateBack.setOnClickListener {
                date.add(Calendar.YEAR,-1)
                slideLeft()
                monthLabel.setText(ydf.format(date.time))
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createMonthCalendarChart(date,true,weeks1)
                else
                    createMonthCalendarChart(date,true,weeks2)
                fillAim()

            }
            dateForward.setOnClickListener {
                date.add(Calendar.YEAR,1)
               slideRight()
                monthLabel.setText(ydf.format(date.time))
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createMonthCalendarChart(date,true,weeks1)
                else
                    createMonthCalendarChart(date,true,weeks2)
                fillAim()

            }

        }

        weekButton.setOnClickListener{
            date= Calendar.getInstance()
            slideNone()
            monthLabel.setText(mdf.format(date.time))
            if (hostLinearLayout.visibility==View.VISIBLE)
                createWeekCalendarChart(date,true,weeks1)
            else
                createWeekCalendarChart(date,true,weeks2)
            fillAim()



            dateBack.setOnClickListener {
                date.add(Calendar.MONTH,-1)
                slideLeft()
                monthLabel.setText(mdf.format(date.time))
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createWeekCalendarChart(date,true,weeks1)
                else
                    createWeekCalendarChart(date,true,weeks2)
                fillAim()

            }

            dateForward.setOnClickListener {

                date.add(Calendar.MONTH,1)
                slideRight()
                monthLabel.setText(mdf.format(date.time))
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createWeekCalendarChart(date,true,weeks1)
                else
                    createWeekCalendarChart(date,true,weeks2)
                fillAim()

            }
        }

        dayButton.setOnClickListener{
            date= Calendar.getInstance()
            slideNone()
            monthLabel.setText(mdf.format(date.time))
            if (hostLinearLayout.visibility==View.VISIBLE)
                createDaysCalendarChart(date,true,weeks1)
            else
                createDaysCalendarChart(date,true,weeks2)
            fillAim()


            dateBack.setOnClickListener {
                date.add(Calendar.MONTH,-1)
                slideLeft()
                monthLabel.setText(mdf.format(date.time))
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createDaysCalendarChart(date,true,weeks1)
                else
                    createDaysCalendarChart(date,true,weeks2)
                fillAim()

            }

            dateForward.setOnClickListener {
                date.add(Calendar.MONTH,1)
                slideRight()
                monthLabel.setText(mdf.format(date.time))
                if (hostLinearLayout.visibility==View.VISIBLE)
                    createDaysCalendarChart(date,true,weeks1)
                else
                    createDaysCalendarChart(date,true,weeks2)
                fillAim()

            }
        }

        dateBack.setOnClickListener {
            date.add(Calendar.MONTH,-1)
            slideLeft()
            monthLabel.setText(mdf.format(date.time))
            if (hostLinearLayout.visibility==View.VISIBLE)
                createDaysCalendarChart(date,true,weeks1)
            else
                createDaysCalendarChart(date,true,weeks2)
            fillAim()

        }

        dateForward.setOnClickListener {
            date.add(Calendar.MONTH,1)
            slideRight()
            monthLabel.setText(mdf.format(date.time))
            if (hostLinearLayout.visibility==View.VISIBLE)
                createDaysCalendarChart(date,true,weeks1)
            else
                createDaysCalendarChart(date,true,weeks2)
            fillAim()

        }

        monthLabel.setText(mdf.format(date.time))


        fillAim()

        dayAimButton.setOnClickListener {
         var fnp=NumberPicker()
         fnp.show(requireActivity().supportFragmentManager,"Hours")

        }
    }

    private fun createDaysCalendarChart(monthCalendar: Calendar, isMondayFirstDay:Boolean=false, weeks:Array<RecyclerView>){
        productiveDays=0
        var hours:Float=0f
        if (isMondayFirstDay)
            monthCalendar.firstDayOfWeek=Calendar.MONDAY
        else
            monthCalendar.firstDayOfWeek=Calendar.SUNDAY


        val allData:Map<Calendar,String> =getDBData()

        val monthData:Map<Calendar,String> =allData.filter { entry ->
            (monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)) &&(monthCalendar.get(Calendar.MONTH)==entry.key.get(Calendar.MONTH))
        }
        val lastMonthData:Map<Calendar,String> =allData.filter { entry ->
            (monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)) &&((monthCalendar.get(Calendar.MONTH)-1)==entry.key.get(Calendar.MONTH))
        }
        val nextMonthData:Map<Calendar,String> =allData.filter { entry ->
            (monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)) &&((monthCalendar.get(Calendar.MONTH)+1)==entry.key.get(Calendar.MONTH))
        }

        var daysData:Map<Int,String> = TreeMap()
        monthData.forEach { (t, u) ->
            daysData=daysData.plus(Pair(t.get(Calendar.DAY_OF_MONTH),u))
        }
        var lastDaysData:Map<Int,String> = TreeMap()
        lastMonthData.forEach { (t, u) ->
            lastDaysData=lastDaysData.plus(Pair(t.get(Calendar.DAY_OF_MONTH),u))
        }
        var nextDaysData:Map<Int,String> = TreeMap()
        nextMonthData.forEach { (t, u) ->
            nextDaysData=nextDaysData.plus(Pair(t.get(Calendar.DAY_OF_MONTH),u))
        }

        val list:ArrayList<ArrayList<NTuple5<Int,Float,Boolean,Int,Int?>?>> = ArrayList()

        val calendar: Calendar = monthCalendar

        calendar.set(Calendar.DAY_OF_MONTH,1)
        val firstMonthDay:Int = calendar.get(Calendar.DAY_OF_WEEK)


        val lastMonth: Calendar = Calendar.getInstance()
        lastMonth.set(Calendar.MONTH,monthCalendar.get(Calendar.MONTH)-1)
        var lastMonthDays=lastMonth.getActualMaximum(Calendar.DAY_OF_MONTH)-(firstMonthDay-1)
        var nextMonthDays=1

        val monthDays:Int= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var day=1
        var started=false
        var localList:ArrayList<NTuple5<Int,Float,Boolean,Int,Int?>?>

        for (week in weeks){
            localList= ArrayList()
            if(day<=monthDays)
                for(i in 1..7){
                    var dur=0.0f
                    if(day<=monthDays) {
                        if (!started && (day < firstMonthDay)) {
                            if(lastDaysData.containsKey(++lastMonthDays))
                                dur= lastDaysData[lastMonthDays]!!.toFloat()
                            localList.add(NTuple5(lastMonthDays, dur, false,7, dayAimH))
                        }else {
                            if (!started)
                                day = 1
                            if(daysData.containsKey(day)){
                                dur= daysData[day]!!.toFloat()
                                hours+=dur
                                if (dur>=dayAimH)
                                    ++productiveDays
                            }
                            localList.add(NTuple5(day, dur,true,7, dayAimH))
                            started = true
                        }
                        ++day
                    }else {
                        if(nextDaysData.containsKey(nextMonthDays))
                            dur= nextDaysData[nextMonthDays]!!.toFloat()
                        localList.add(NTuple5(nextMonthDays++, dur, false,7, dayAimH))
                    }
                }

            list.add(localList)
        }
        var weekday=0
        for (week in weeks)
            setAdapterManager(
                week,
                CalendarAdapter(list[weekday++]),
                LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
            )
        workPer=hours/( dayAimH * monthDays)
        productivePer=(productiveDays.toFloat()/monthDays*100)
    }

    private fun createWeekCalendarChart(monthCalendar:Calendar, isMondayFirstDay:Boolean=false,weeks:Array<RecyclerView>){
        productiveDays=0
        var hours:Float=0f
        var days=0
        if (isMondayFirstDay)
            monthCalendar.firstDayOfWeek=Calendar.MONDAY
        else
            monthCalendar.firstDayOfWeek=Calendar.SUNDAY

        val allData:Map<Calendar,String> =getDBData()
        val cal:Calendar=monthCalendar
        cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH))

        val lastWeek=cal.get(Calendar.WEEK_OF_YEAR)
        cal.set(Calendar.DAY_OF_MONTH,1)
        val firstWeek=cal.get(Calendar.WEEK_OF_YEAR)

        val monthData:Map<Calendar,String> =allData.filter { entry ->
            (cal.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR))&&(entry.key.get(Calendar.WEEK_OF_YEAR) in firstWeek..lastWeek)
        }
        monthData.let {
            for(el in it){
                if(el.value.toFloat()>=dayAimH)
                    ++productiveDays
                hours+=el.value.toFloat()
            }
        }

        var daysData:Map<Int,String> = TreeMap()
        monthData.forEach { (t, u) ->

            if(daysData.containsKey(t.get(Calendar.WEEK_OF_YEAR)))
                daysData=daysData.plus(Pair(t.get(Calendar.WEEK_OF_YEAR),
                    daysData[t.get(Calendar.WEEK_OF_YEAR)] +u))
            daysData=daysData.plus(Pair(t.get(Calendar.WEEK_OF_YEAR),u))
        }


        val calendar:Calendar= monthCalendar

        calendar.set(Calendar.DAY_OF_MONTH,1)
        days=calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val lastMonth:Calendar=Calendar.getInstance()
        lastMonth.set(Calendar.MONTH,monthCalendar.get(Calendar.MONTH)-1)

        fun setWeekAdapter(recyclerView:RecyclerView,arrayList:ArrayList<NTuple5<Int,Float,Boolean,Int,Int?>?> ) {
            setAdapterManager(
                recyclerView,
                CalendarAdapter(arrayList),
                LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
            )
        }

        val scale:Int= dayAimH*days
        var localList:ArrayList<NTuple5<Int,Float,Boolean,Int,Int?>?> =ArrayList()
        for (week in firstWeek..firstWeek + 2) {
            if(daysData.containsKey(week)){
                localList.add(NTuple5(week, daysData[week]!!.toFloat(),true,3, scale))

            }
            else
                localList.add(NTuple5(week,0f,true,3, scale))

        }
        setWeekAdapter(weeks[0],localList)
        localList = ArrayList()
        if(lastWeek>firstWeek)
            for (week in firstWeek+3..lastWeek) {
                if(daysData.containsKey(week)) {
                    localList.add(NTuple5(week, daysData[week]!!.toFloat(), true, 3, scale))

                }else
                    localList.add(NTuple5(week,0f,true,3, scale))

            }else{
            for (week in firstWeek+3..cal.getActualMaximum(Calendar.WEEK_OF_YEAR)) {                        //maybe +1 in max
                if (daysData.containsKey(week)) {
                    localList.add(NTuple5(week, daysData[week]!!.toFloat(), true, 3, scale))

                }else
                    localList.add(NTuple5(week, 0f, true, 3, scale))
            }

        }
        setWeekAdapter(weeks[1],localList)


        setWeekAdapter(weeks[2], ArrayList())
        setWeekAdapter(weeks[3],ArrayList())
        setWeekAdapter(weeks[4],ArrayList())
        setWeekAdapter(weeks[5],ArrayList())
        workPer=hours/(days * dayAimH)
        productivePer=(productiveDays.toFloat()/days*100)
    }

    private fun createMonthCalendarChart(monthCalendar:Calendar, isMondayFirstDay:Boolean=false, weeks:Array<RecyclerView>){
        var hours:Float=0f
        productiveDays=0

        if (isMondayFirstDay)
            monthCalendar.firstDayOfWeek=Calendar.MONDAY
        else
            monthCalendar.firstDayOfWeek=Calendar.SUNDAY


        val allData:Map<Calendar,String> =getDBData()



        val yearData:Map<Calendar,String> =allData.filter { entry ->
            monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)
        }
        yearData.let {
            for(el in it){
                if( el.value.toFloat()>=dayAimH)
                    ++productiveDays
                hours+=el.value.toFloat()
            }
        }

        var monthsData:Map<Int,String> = TreeMap()
        yearData.forEach { (t, u) ->
            if(monthsData.containsKey(t.get(Calendar.MONTH)+1))
                monthsData=monthsData.plus(Pair(t.get(Calendar.MONTH)+1,
                    monthsData[t.get(Calendar.MONTH)] +u))
            monthsData=monthsData.plus(Pair(t.get(Calendar.MONTH)+1,u))
        }

        var days=monthCalendar.getActualMaximum(Calendar.DAY_OF_YEAR)
        var scale=days* dayAimH
        fun setMonthAdapter(recyclerView:RecyclerView,arrayList:ArrayList<NTuple5<Int,Float,Boolean,Int,Int?>?> ) {
            setAdapterManager(
                recyclerView,
                CalendarAdapter(arrayList),
                LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
            )
        }

        fun fillLine(start:Int,end:Int):ArrayList<NTuple5<Int,Float,Boolean,Int,Int?>?>{
            val localList:ArrayList<NTuple5<Int, Float, Boolean, Int,Int?>?> = ArrayList()
            for (month in start..end) {
                if(monthsData.containsKey(month))
                    localList.add(
                        NTuple5(
                            month,
                            monthsData[month]!!.toFloat(),
                            true,
                            end - start + 1
                            , scale
                        )
                    )
                else
                    localList.add(NTuple5(month,0f,true,end-start+1, scale))
            }
            return  localList
        }


        setMonthAdapter(weeks[0],fillLine(1,4))
        setMonthAdapter(weeks[1],fillLine(5,8))
        setMonthAdapter(weeks[2], fillLine(9,12))
        setMonthAdapter(weeks[3],ArrayList())
        setMonthAdapter(weeks[4],ArrayList())
        setMonthAdapter(weeks[5],ArrayList())
        workPer=hours/scale
        productivePer=(productiveDays.toFloat()/days*100)
    }



    private fun getDBData():Map<Calendar,String>{
        val selectionQuery = "SELECT OLDSTATS.date_id, OLDSTATS.working_time from OLDSTATS "
        val c: Cursor = db.rawQuery(selectionQuery, null)
        var map:Map<Calendar,String> = HashMap()

        if(c.moveToFirst())
            do {
                val cal = Calendar.getInstance()
                cal.time=Date(c.getLong(0))
                cal.set(Calendar.HOUR,0)
                cal.set(Calendar.MINUTE,0)
                cal.set(Calendar.SECOND,0)
                cal.set(Calendar.MILLISECOND,0)
                map=map.plus(Pair(cal,c.getString(1)))

            }while (c.moveToNext())
        c.close()

        return map

    }
    private fun setAdapterManager(recyclerView: RecyclerView, adapter:RecyclerView.Adapter<LinearViewHolder>, layoutManager: RecyclerView.LayoutManager){
        recyclerView.adapter=adapter
        recyclerView.layoutManager=layoutManager
    }

}
