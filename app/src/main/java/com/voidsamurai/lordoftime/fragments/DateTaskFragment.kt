package com.voidsamurai.lordoftime.fragments

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.LOTDatabaseHelper
import com.voidsamurai.lordoftime.charts.NTuple4
import com.voidsamurai.lordoftime.fragments.adapters.CalendarAdapter
import kotlinx.android.synthetic.main.fragment_date_chart_element.*
import java.util.*

class DateTaskFragment : Fragment() {



    private lateinit var oh: LOTDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oh = LOTDatabaseHelper(requireContext())
        db = oh.readableDatabase

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_date_chart_element, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DateTaskFragment.context=context
        week1 = first_week
        week2 = second_week
        week3 = third_week
        week4 = fourth_week
        week5 = fifth_week
        week6 = sixth_week


    }

    companion object {
        private lateinit var db: SQLiteDatabase
        private  var context: Context?=null


        private lateinit var week1: RecyclerView
        private lateinit var week2: RecyclerView
        private lateinit var week3: RecyclerView
        private lateinit var week4: RecyclerView
        private lateinit var week5: RecyclerView
        private lateinit var week6: RecyclerView

        private fun createDaysCalendarChart(monthCalendar: Calendar, isMondayFirstDay:Boolean=false){

            if (isMondayFirstDay)
                monthCalendar.firstDayOfWeek= Calendar.MONDAY
            else
                monthCalendar.firstDayOfWeek= Calendar.SUNDAY


            val allData:Map<Calendar,String> =getDBData()

            val monthData:Map<Calendar,String> =allData.filter { entry ->
                (monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)) &&(monthCalendar.get(
                    Calendar.MONTH)==entry.key.get(Calendar.MONTH))
            }
            val lastMonthData:Map<Calendar,String> =allData.filter { entry ->
                (monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)) &&((monthCalendar.get(
                    Calendar.MONTH)-1)==entry.key.get(Calendar.MONTH))
            }
            val nextMonthData:Map<Calendar,String> =allData.filter { entry ->
                (monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)) &&((monthCalendar.get(
                    Calendar.MONTH)+1)==entry.key.get(Calendar.MONTH))
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

            val list:ArrayList<ArrayList<NTuple4<Int, Float, Boolean, Int>?>> = ArrayList()
            val weeks= arrayOf(week1, week2,week3,week4,week5,week6)

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
            var localList:ArrayList<NTuple4<Int, Float, Boolean, Int>?>

            //   val sdf= SimpleDateFormat("MM/YY")

            //  monthLabel.text = sdf.format(calendar.time)

            for (week in weeks){
                localList= ArrayList()
                if(day<=monthDays)
                    for(i in 1..7){
                        var dur=0.0f
                        if(day<=monthDays) {
                            if (!started && (day < firstMonthDay)) {
                                if(lastDaysData.containsKey(++lastMonthDays))
                                    dur= lastDaysData[lastMonthDays]!!.toFloat()
                                localList.add(NTuple4(lastMonthDays, dur, false,7))
                            }else {
                                if (!started)
                                    day = 1
                                if(daysData.containsKey(day))
                                    dur= daysData[day]!!.toFloat()
                                localList.add(NTuple4(day, dur,true,7))
                                started = true
                            }
                            ++day
                        }else {
                            if(nextDaysData.containsKey(nextMonthDays))
                                dur= nextDaysData[nextMonthDays]!!.toFloat()
                            localList.add(NTuple4(nextMonthDays++, dur, false,7))
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
        }

        private fun createWeekCalendarChart(monthCalendar: Calendar, isMondayFirstDay:Boolean=false){

            if (isMondayFirstDay)
                monthCalendar.firstDayOfWeek= Calendar.MONDAY
            else
                monthCalendar.firstDayOfWeek= Calendar.SUNDAY

            val allData:Map<Calendar,String> =getDBData()
            val cal: Calendar =monthCalendar
            cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH))

            val lastWeek=cal.get(Calendar.WEEK_OF_YEAR)
            cal.set(Calendar.DAY_OF_MONTH,1)
            val firstWeek=cal.get(Calendar.WEEK_OF_YEAR)

            val monthData:Map<Calendar,String> =allData.filter { entry ->
                (cal.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR))&&(entry.key.get(Calendar.WEEK_OF_YEAR) in firstWeek..lastWeek)
            }

            var daysData:Map<Int,String> = TreeMap()
            monthData.forEach { (t, u) ->

                if(daysData.containsKey(t.get(Calendar.WEEK_OF_YEAR)))
                    daysData=daysData.plus(Pair(t.get(Calendar.WEEK_OF_YEAR),
                        daysData[t.get(Calendar.WEEK_OF_YEAR)] +u))
                daysData=daysData.plus(Pair(t.get(Calendar.WEEK_OF_YEAR),u))
            }


            val calendar: Calendar = monthCalendar

            calendar.set(Calendar.DAY_OF_MONTH,1)


            // val sdf= SimpleDateFormat("MM/YY")
            //  monthLabel.text = sdf.format(calendar.time)


            val lastMonth: Calendar = Calendar.getInstance()
            lastMonth.set(Calendar.MONTH,monthCalendar.get(Calendar.MONTH)-1)

            fun setWeekAdapter(recyclerView:RecyclerView,arrayList:ArrayList<NTuple4<Int, Float, Boolean, Int>?> ) {
                setAdapterManager(
                    recyclerView,
                    CalendarAdapter(arrayList),
                    LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
                )
            }


            var localList:ArrayList<NTuple4<Int, Float, Boolean, Int>?> =ArrayList()
            for (week in firstWeek..firstWeek + 2) {
                if(daysData.containsKey(week))
                    localList.add(NTuple4(week, daysData[week]!!.toFloat(),true,3))
                else
                    localList.add(NTuple4(week,0f,true,3))
            }
            setWeekAdapter(week1,localList)
            localList = ArrayList()
            if(lastWeek>firstWeek)
                for (week in firstWeek+3..lastWeek) {
                    if(daysData.containsKey(week))
                        localList.add(NTuple4(week, daysData[week]!!.toFloat(),true,3))
                    else
                        localList.add(NTuple4(week,0f,true,3))
                }else{
                for (week in firstWeek+3..cal.getActualMaximum(Calendar.WEEK_OF_YEAR)) {                        //maybe +1 in max
                    if (daysData.containsKey(week))
                        localList.add(NTuple4(week, daysData[week]!!.toFloat(), true, 3))
                    else
                        localList.add(NTuple4(week, 0f, true, 3))
                }

            }
            setWeekAdapter(week2,localList)


            setWeekAdapter(week3, ArrayList())
            setWeekAdapter(week4,ArrayList())
            setWeekAdapter(week5,ArrayList())
            setWeekAdapter(week6,ArrayList())
        }

        private fun createMonthCalendarChart(monthCalendar: Calendar, isMondayFirstDay:Boolean=false){

            if (isMondayFirstDay)
                monthCalendar.firstDayOfWeek= Calendar.MONDAY
            else
                monthCalendar.firstDayOfWeek= Calendar.SUNDAY


            val allData:Map<Calendar,String> =getDBData()

            val yearData:Map<Calendar,String> =allData.filter { entry ->
                monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)
            }

            var monthsData:Map<Int,String> = TreeMap()
            yearData.forEach { (t, u) ->
                if(monthsData.containsKey(t.get(Calendar.MONTH)+1))
                    monthsData=monthsData.plus(Pair(t.get(Calendar.MONTH)+1,
                        monthsData[t.get(Calendar.MONTH)] +u))
                monthsData=monthsData.plus(Pair(t.get(Calendar.MONTH)+1,u))
            }


            fun setMonthAdapter(recyclerView:RecyclerView,arrayList:ArrayList<NTuple4<Int, Float, Boolean, Int>?> ) {
                setAdapterManager(
                    recyclerView,
                    CalendarAdapter(arrayList),
                    LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
                )
            }
            fun fillLine(start:Int,end:Int):ArrayList<NTuple4<Int, Float, Boolean, Int>?>{
                val localList:ArrayList<NTuple4<Int, Float, Boolean, Int>?> = ArrayList()
                for (month in start..end) {
                    if(monthsData.containsKey(month))
                        localList.add(NTuple4(month, monthsData[month]!!.toFloat(),true,end-start+1))
                    else
                        localList.add(NTuple4(month,0f,true,end-start+1))
                }
                return  localList
            }


            setMonthAdapter(week1,fillLine(1,4))
            setMonthAdapter(week2,fillLine(5,8))
            setMonthAdapter(week3, fillLine(9,12))
            setMonthAdapter(week4,ArrayList())
            setMonthAdapter(week5,ArrayList())
            setMonthAdapter(week6,ArrayList())
        }

        private fun getDBData():Map<Calendar,String>{
            val selectionQuery = "SELECT OLDSTATS.date_id, OLDSTATS.working_time from OLDSTATS "
            val c: Cursor = db.rawQuery(selectionQuery, null)
            var map:Map<Calendar,String> = HashMap()

            if(c.moveToFirst())
                do {
                    val cal = Calendar.getInstance()
                    cal.time= Date(c.getLong(0))
                    cal.set(Calendar.HOUR,0)
                    cal.set(Calendar.MINUTE,0)
                    cal.set(Calendar.SECOND,0)
                    cal.set(Calendar.MILLISECOND,0)
                    map=map.plus(Pair(cal,c.getString(1)))

                }while (c.moveToNext())
            c.close()

            return map

        }
        private fun setAdapterManager(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<LinearViewHolder>, layoutManager: RecyclerView.LayoutManager){
            recyclerView.adapter=adapter
            recyclerView.layoutManager=layoutManager
        }

}



}

