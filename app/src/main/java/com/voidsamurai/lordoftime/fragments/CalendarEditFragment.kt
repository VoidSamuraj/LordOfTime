package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.charts_and_views.NTuple6
import com.voidsamurai.lordoftime.databinding.FragmentCalendarEditBinding
import com.voidsamurai.lordoftime.fragments.adapters.CalendarEditAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class CalendarEditFragment : Fragment() {

    var _calendarViewBinding:FragmentCalendarEditBinding?=null
    val binding get()=_calendarViewBinding!!

    var dayAimH: MutableLiveData<Int> = MutableLiveData(6)
    private var productiveDays:Int=0
    private var workPer:Float=0f
    private var productivePer:Float=0f
    private lateinit var weeks1:Array<RecyclerView>
    private lateinit var weeks2:Array<RecyclerView>
    private var date: Calendar = Calendar.getInstance()
    @SuppressLint("SimpleDateFormat")
    private val mdf= SimpleDateFormat("MM/yy")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _calendarViewBinding= FragmentCalendarEditBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        weeks1 = arrayOf( binding.firstWeek,binding.secondWeek,binding.thirdWeek,binding.fourthWeek,binding.fifthWeek,binding.sixthWeek)
        weeks2 = arrayOf( binding.firstWeek2,binding.secondWeek2,binding.thirdWeek2,binding.fourthWeek2,binding.fifthWeek2,binding.sixthWeek2)

        createDaysCalendarChart(Calendar.getInstance(),true, weeks1)
        createDaysCalendarChart(Calendar.getInstance(),true, weeks2)

        fun fillLabels(){
            productivePer= (productivePer * 100).roundToInt().toFloat()/100
            workPer= (workPer * 100).roundToInt().toFloat()/100
        }
        fun fillAim(){
            fillLabels()
        }
        fun updateAfterAimChange(){
            fillLabels()
        }
        dayAimH.observe(viewLifecycleOwner, {
            updateAfterAimChange()
        })


        date= Calendar.getInstance()
        binding.currentMonthLabel.setText(mdf.format(date.time))
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

    }

    override fun onDestroyView() {
        dayAimH.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
    override fun onDestroy() {
        super.onDestroy()
        _calendarViewBinding=null
    }
    private fun createDaysCalendarChart(monthCalendar: Calendar, isMondayFirstDay:Boolean=false, weeks:Array<RecyclerView>){
        productiveDays=0
        val hours=0f
        if (isMondayFirstDay)
            monthCalendar.firstDayOfWeek=Calendar.MONDAY
        else
            monthCalendar.firstDayOfWeek=Calendar.SUNDAY

        val allData:MutableMap<Calendar,ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> = mutableMapOf()


        val data=(activity as MainActivity).getQueryArrayByDate().value
        data?.let {
            for (x in it) {

                val value=NTuple6(x.date,x.workingTime,x.priority,x.name,x.category,x.color)
                val rest= allData.putIfAbsent(x.date, arrayListOf(value))
                if(rest!=null){
                    rest.add(value)
                    allData.replace(x.date,rest)
                }
            }
        }

        val monthData:Map<Int,ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> =allData.filter { entry ->
            (monthCalendar.get(Calendar.YEAR)== entry.key.get(Calendar.YEAR))
                    &&(monthCalendar.get(Calendar.MONTH)==entry.key.get(Calendar.MONTH))
        }.mapKeys { it.key.get(Calendar.DAY_OF_MONTH)  }

        val lastMonthData:Map<Int,ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> = allData.filter { entry ->
            (monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR))
                    &&((monthCalendar.get(Calendar.MONTH)-1)==entry.key.get(Calendar.MONTH))
        }.mapKeys { it.key.get(Calendar.DAY_OF_MONTH)  }

        val nextMonthData:Map<Int,ArrayList<NTuple6<Calendar,Float, Int, String, String, String>>> =allData.filter { entry ->
            (monthCalendar.get(Calendar.YEAR)==entry.key.get(Calendar.YEAR)) &&((monthCalendar.get(Calendar.MONTH)+1)==entry.key.get(Calendar.MONTH))
        }.mapKeys { it.key.get(Calendar.DAY_OF_MONTH)  }

        val c:Calendar= Calendar.getInstance()
        c.set(2021, 11, 1, 12, 6,0)

        val list:ArrayList<ArrayList<ArrayList<NTuple6<Calendar,Float,Int, String, String,String>>?>> = arrayListOf()
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
        var localList:ArrayList<ArrayList<NTuple6<Calendar,Float,Int, String, String,String>>?>

        for (week in weeks){
            localList= arrayListOf()
            if(day<=monthDays)
                for(i in 1..7){
                    if(day<=monthDays) {
                        if (!started && (day < firstMonthDay)) {
                            //before month
                            val cal=date.clone() as Calendar
                            cal.set(Calendar.MONTH,cal.get(Calendar.MONTH)-1)
                            cal.set(Calendar.DAY_OF_MONTH,++lastMonthDays)
                            val row:ArrayList<NTuple6<Calendar,Float,Int, String, String,String>> = arrayListOf(NTuple6(cal,null,null,null,null,null))
                            val ldd=lastMonthData.get(lastMonthDays)?: row
                            localList.add(ldd)
                        }else {
                            //in month
                            if (!started)
                                day = 1
                            val cal=date.clone() as Calendar
                            cal.set(Calendar.DAY_OF_MONTH,day)
                            val row:ArrayList<NTuple6<Calendar,Float,Int, String, String,String>> = arrayListOf(NTuple6(cal,null,null,null,null,null))
                            monthData.get(day)?.let{
                            }
                            val dd=monthData.get(day)?: row
                            localList.add(dd)

                            started = true
                        }
                        ++day
                    }else {
                        //after month
                        val cal=date.clone() as Calendar
                        cal.set(Calendar.MONTH,cal.get(Calendar.MONTH)+1)
                        cal.set(Calendar.DAY_OF_MONTH,nextMonthDays++)
                        val row:ArrayList<NTuple6<Calendar,Float,Int, String, String,String>> = arrayListOf(NTuple6(cal,null,null,null,null,null))

                        val ndd=nextMonthData[nextMonthDays]?:row
                        localList.add(ndd)
                    }
                }

            list.add(localList)
        }

        weeks.forEachIndexed { weekDay, week ->
            setAdapterManager(
                week,
                CalendarEditAdapter(requireContext(),list[weekDay]),
                LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
            )
        }

        workPer=hours/( dayAimH.value!! * monthDays)
        productivePer=(productiveDays.toFloat()/monthDays*100)
    }

    private fun setAdapterManager(recyclerView: RecyclerView, adapter:RecyclerView.Adapter<LinearViewHolder>, layoutManager: RecyclerView.LayoutManager){
        recyclerView.adapter=adapter
        recyclerView.layoutManager=layoutManager
    }
}