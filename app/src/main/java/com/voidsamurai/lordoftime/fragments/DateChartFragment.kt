

package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.LOTDatabaseHelper
import com.voidsamurai.lordoftime.charts.NTuple5
import com.voidsamurai.lordoftime.databinding.FragmentDateWidgetBinding
import com.voidsamurai.lordoftime.fragments.adapters.CalendarAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class DateChartFragment : Fragment() {


    private var _binding: FragmentDateWidgetBinding?=null
    private val binding get()=_binding!!
    var dayAimH:MutableLiveData<Int> = MutableLiveData(6)
    private var productiveDays:Int=0
    private var workPer:Float=0f
    private var productivePer:Float=0f
    private lateinit var weeks1:Array<RecyclerView>
    private lateinit var weeks2:Array<RecyclerView>
    private var date:Calendar= Calendar.getInstance()
    private lateinit var db: SQLiteDatabase
    private lateinit var oh: LOTDatabaseHelper
    @SuppressLint("SimpleDateFormat")
    private val ydf= SimpleDateFormat("YYYY")
    @SuppressLint("SimpleDateFormat")
    private val mdf= SimpleDateFormat("MM/yy")
    private lateinit var dayAim:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oh = LOTDatabaseHelper(requireContext())
        db = oh.readableDatabase
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentDateWidgetBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dayAim=binding.hours
        weeks1 = arrayOf( binding.firstWeek,binding.secondWeek,binding.thirdWeek,binding.fourthWeek,binding.fifthWeek,binding.sixthWeek)
        weeks2 = arrayOf( binding.firstWeek2,binding.secondWeek2,binding.thirdWeek2,binding.fourthWeek2,binding.fifthWeek2,binding.sixthWeek2)

        createDaysCalendarChart(Calendar.getInstance(),true, weeks1)
        createDaysCalendarChart(Calendar.getInstance(),true, weeks2)

        fun fillLabels(){
            productivePer= (productivePer * 100).roundToInt().toFloat()/100
            workPer= (workPer * 100).roundToInt().toFloat()/100
            if (workPer>100)workPer=100f
            binding.summaryText.text=String.format("Produktywne dni: %o dni. Wydajność: %.2f%%",productiveDays,workPer)
        }
        fun fillAim(){
            dayAim.text=dayAimH.value.toString()
            fillLabels()
        }
        fun updateAfterAimChange(){
            val id=binding.materialButtonToggleGroup.checkedButtonId
            if(id==R.id.day_button)
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createDaysCalendarChart(date,true,weeks1)
                else
                    createDaysCalendarChart(date,true,weeks2)
            else if (id==R.id.week_button)
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createWeekCalendarChart(date,true,weeks1)
                else
                    createWeekCalendarChart(date,true,weeks2)
            else if (id==R.id.month_button)
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createMonthCalendarChart(date,true,weeks1)
                else
                    createMonthCalendarChart(date,true,weeks2)
            fillLabels()
        }
        dayAimH.observe(viewLifecycleOwner, {
            dayAim.text=it.toString()
            updateAfterAimChange()
        })

        fun slideNone(){
            binding.currentMonthLabel.inAnimation= null
            binding.currentMonthLabel.outAnimation= null
            binding.dateLayout.animation=null
            binding.dateLayout2.animation=null
        }

        fun slideRight(){
            binding.currentMonthLabel.inAnimation= AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
            binding.currentMonthLabel.outAnimation= AnimationUtils.loadAnimation(context, R.anim.slide_out_left)
            if (binding.dateLayout.visibility==View.VISIBLE){
                binding.dateLayout2.visibility=View.VISIBLE
                binding.dateLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left))
                binding.dateLayout2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right))
                binding.dateLayout.visibility=View.INVISIBLE

            }
            else{
                binding.dateLayout.visibility=View.VISIBLE
                binding.dateLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right))
                binding.dateLayout2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left))
                binding.dateLayout2.visibility=View.INVISIBLE

            }

        }
        fun slideLeft(){
            binding.currentMonthLabel.inAnimation= AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
            binding.currentMonthLabel.outAnimation= AnimationUtils.loadAnimation(context, R.anim.slide_out_right)

            if (binding.dateLayout.visibility==View.VISIBLE){
                binding.dateLayout2.visibility=View.VISIBLE
                binding.dateLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_right))
                binding.dateLayout2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left))
                binding.dateLayout.visibility=View.INVISIBLE

            }
            else{
                binding.dateLayout.visibility=View.VISIBLE
                binding.dateLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left))
                binding.dateLayout2.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_right))
                binding.dateLayout2.visibility=View.INVISIBLE

            }

        }
        binding.dayButton.callOnClick()

        binding.monthButton.setOnClickListener{

            date= Calendar.getInstance()
            slideNone()
            binding.currentMonthLabel.setText(ydf.format(date.time))
            if (binding.dateLayout.visibility==View.VISIBLE)
                createMonthCalendarChart(date,true,weeks1)
            else
                createMonthCalendarChart(date,true,weeks2)
            fillAim()

            binding.dateBack.setOnClickListener {
                date.add(Calendar.YEAR,-1)
                slideLeft()
                binding.currentMonthLabel.setText(ydf.format(date.time))
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createMonthCalendarChart(date,true,weeks1)
                else
                    createMonthCalendarChart(date,true,weeks2)
                fillAim()

            }
            binding.dateForward.setOnClickListener {
                date.add(Calendar.YEAR,1)
                slideRight()
                binding.currentMonthLabel.setText(ydf.format(date.time))
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createMonthCalendarChart(date,true,weeks1)
                else
                    createMonthCalendarChart(date,true,weeks2)
                fillAim()

            }

        }

        binding.weekButton.setOnClickListener{
            date= Calendar.getInstance()
            slideNone()
            binding.currentMonthLabel.setText(mdf.format(date.time))
            if (binding.dateLayout.visibility==View.VISIBLE)
                createWeekCalendarChart(date,true,weeks1)
            else
                createWeekCalendarChart(date,true,weeks2)
            fillAim()



            binding.dateBack.setOnClickListener {
                date.add(Calendar.MONTH,-1)
                slideLeft()
                binding.currentMonthLabel.setText(mdf.format(date.time))
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createWeekCalendarChart(date,true,weeks1)
                else
                    createWeekCalendarChart(date,true,weeks2)
                fillAim()

            }

            binding.dateForward.setOnClickListener {

                date.add(Calendar.MONTH,1)
                slideRight()
                binding.currentMonthLabel.setText(mdf.format(date.time))
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createWeekCalendarChart(date,true,weeks1)
                else
                    createWeekCalendarChart(date,true,weeks2)
                fillAim()

            }
        }

        binding.dayButton.setOnClickListener{
            date= Calendar.getInstance()
            slideNone()
            binding.currentMonthLabel.setText(mdf.format(date.time))
            if (binding.dateLayout.visibility==View.VISIBLE)
                createDaysCalendarChart(date,true,weeks1)
            else
                createDaysCalendarChart(date,true,weeks2)
            fillAim()


            binding.dateBack.setOnClickListener {
                date.add(Calendar.MONTH,-1)
                slideLeft()
                binding.currentMonthLabel.setText(mdf.format(date.time))
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createDaysCalendarChart(date,true,weeks1)
                else
                    createDaysCalendarChart(date,true,weeks2)
                fillAim()

            }

            binding.dateForward.setOnClickListener {
                date.add(Calendar.MONTH,1)
                slideRight()
                binding.currentMonthLabel.setText(mdf.format(date.time))
                if (binding.dateLayout.visibility==View.VISIBLE)
                    createDaysCalendarChart(date,true,weeks1)
                else
                    createDaysCalendarChart(date,true,weeks2)
                fillAim()

            }
        }

        binding.dateBack.setOnClickListener {
            date.add(Calendar.MONTH,-1)
            slideLeft()
            binding.currentMonthLabel.setText(mdf.format(date.time))
            if (binding.dateLayout.visibility==View.VISIBLE)
                createDaysCalendarChart(date,true,weeks1)
            else
                createDaysCalendarChart(date,true,weeks2)
            fillAim()

        }

        binding.dateForward.setOnClickListener {
            date.add(Calendar.MONTH,1)
            slideRight()
            binding.currentMonthLabel.setText(mdf.format(date.time))
            if (binding.dateLayout.visibility==View.VISIBLE)
                createDaysCalendarChart(date,true,weeks1)
            else
                createDaysCalendarChart(date,true,weeks2)
            fillAim()

        }

        binding.currentMonthLabel.setText(mdf.format(date.time))


        fillAim()

        binding.numberPicker.setOnClickListener {
            val fnp=NumberPicker(dayAimH.value!!)
           // fnp.show(requireActivity().supportFragmentManager,"Hours")
            fnp.show(childFragmentManager,"Hours")
        }
    }


    override fun onDestroyView() {
        dayAimH.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }


    private fun createDaysCalendarChart(monthCalendar: Calendar, isMondayFirstDay:Boolean=false, weeks:Array<RecyclerView>){
        productiveDays=0
        var hours=0f
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
                            localList.add(NTuple5(lastMonthDays, dur, false,7, dayAimH.value!!))
                        }else {
                            if (!started)
                                day = 1
                            if(daysData.containsKey(day)){
                                dur= daysData[day]!!.toFloat()
                                hours+=dur
                                if (dur>=dayAimH.value!!)
                                    ++productiveDays
                            }
                            localList.add(NTuple5(day, dur,true,7, dayAimH.value!!))
                            started = true
                        }
                        ++day
                    }else {
                        if(nextDaysData.containsKey(nextMonthDays))
                            dur= nextDaysData[nextMonthDays]!!.toFloat()
                        localList.add(NTuple5(nextMonthDays++, dur, false,7, dayAimH.value!!))
                    }
                }

            list.add(localList)
        }
        var weekday=0
        for (week in weeks)
            setAdapterManager(
                week,
                CalendarAdapter(requireContext(),list[weekday++]),
                LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
            )
        workPer=hours/( dayAimH.value!! * monthDays)
        productivePer=(productiveDays.toFloat()/monthDays*100)
    }

    private fun createWeekCalendarChart(monthCalendar:Calendar, isMondayFirstDay:Boolean=false,weeks:Array<RecyclerView>){
        productiveDays=0
        var hours=0f
        val days: Int
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
                if(el.value.toFloat()>=dayAimH.value!!)
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
                CalendarAdapter(requireContext(),arrayList),
                LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
            )
        }

        val scale:Int= dayAimH.value!!*days
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
        workPer=hours/(days * dayAimH.value!!)
        productivePer=(productiveDays.toFloat()/days*100)
    }

    private fun createMonthCalendarChart(monthCalendar:Calendar, isMondayFirstDay:Boolean=false, weeks:Array<RecyclerView>){
        var hours =0f
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
                if( el.value.toFloat()>=dayAimH.value!!)
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

        val days=monthCalendar.getActualMaximum(Calendar.DAY_OF_YEAR)
        val scale=days* dayAimH.value!!
        fun setMonthAdapter(recyclerView:RecyclerView,arrayList:ArrayList<NTuple5<Int,Float,Boolean,Int,Int?>?> ) {
            setAdapterManager(
                recyclerView,
                CalendarAdapter(requireContext(),arrayList),
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

