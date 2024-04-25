package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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

import android.view.ViewAnimationUtils

import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.drawToBitmap
import kotlin.math.max

/**
 * Fragment displaying calendar with color gradient of time spend on tasks.
 */
class CalendarEditFragment : Fragment() {

    private var _calendarViewBinding:FragmentCalendarEditBinding?=null
    private val binding get()=_calendarViewBinding!!

    private lateinit var background:View
    private lateinit var weeks1:Array<RecyclerView>
    private lateinit var weeks2:Array<RecyclerView>
    private var date: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
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

        createDaysCalendarChart(Calendar.getInstance(TimeZone.getTimeZone("UTC")),true, weeks1)
        createDaysCalendarChart(Calendar.getInstance(TimeZone.getTimeZone("UTC")),true, weeks2)

        date= Calendar.getInstance(TimeZone.getTimeZone("UTC"))
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

        }

        binding.dateForward.setOnClickListener {
            date.add(Calendar.MONTH,1)
            slideRight()
            binding.currentMonthLabel.setText(mdf.format(date.time))
            if (binding.dateLayout.visibility==View.VISIBLE)
                createDaysCalendarChart(date,true,weeks1)
            else
                createDaysCalendarChart(date,true,weeks2)
        }

        binding.currentMonthLabel.setText(mdf.format(date.time))

        background = binding.background

        background.visibility = View.INVISIBLE
        (activity as MainActivity).homeDrawable?.let {
            binding.bg.background=it
        }



        val viewTreeObserver = background.viewTreeObserver

        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    circularReveal()
                    background.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    override fun onPause() {
        background.drawToBitmap().toDrawable(resources).let{
            (activity as MainActivity).homeDrawable=it
        }
        super.onPause()
    }

    override fun onDestroyView() {
        _calendarViewBinding=null
        super.onDestroyView()
    }

    private fun createDaysCalendarChart(monthCalendar: Calendar, isMondayFirstDay:Boolean=false, weeks:Array<RecyclerView>) {

        if (isMondayFirstDay) {
            monthCalendar.firstDayOfWeek = Calendar.MONDAY

            date.firstDayOfWeek = Calendar.MONDAY
        } else {
            monthCalendar.firstDayOfWeek = Calendar.SUNDAY
            date.firstDayOfWeek = Calendar.SUNDAY

        }
        val allData: MutableMap<Calendar, ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> =
            mutableMapOf()


        val data = (activity as MainActivity).getQueryArrayByDate().value
        data?.let {
            for (x in it) {
                val value =
                    NTuple6(x.date, x.workingTime / 3600f, x.priority, x.name, x.category, x.color)
                val rest = allData.putIfAbsent(x.date, arrayListOf(value))
                if (rest != null) {
                    rest.add(value)
                    allData.replace(x.date, rest)
                }
            }
        }
        /**
         * @param relativeMonth - is adding number to current month
         * */
        fun getMonthData(relativeMonth: Int): Map<Int, ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> {
            val selectedCalendar=monthCalendar.clone() as Calendar
            selectedCalendar.add(Calendar.MONTH,relativeMonth)

            return allData.filter { entry ->

                (selectedCalendar.get(Calendar.YEAR) == entry.key.get(Calendar.YEAR))
                        && (selectedCalendar.get(Calendar.MONTH) == entry.key.get(Calendar.MONTH))
            }.mapKeys { it.key.get(Calendar.DAY_OF_MONTH) }
        }
        val monthData:Map<Int,ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> =getMonthData(0)
        val lastMonthData:Map<Int,ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> =getMonthData(-1)
        val nextMonthData:Map<Int,ArrayList<NTuple6<Calendar,Float, Int, String, String, String>>> =getMonthData(1)

        val currentCalendar: Calendar = monthCalendar.clone() as Calendar

        currentCalendar.set(Calendar.DAY_OF_MONTH,1)
        val firstMonthDay:Int =
            if(isMondayFirstDay)
                if(currentCalendar.get(Calendar.DAY_OF_WEEK)==1)
                    7
                else
                    currentCalendar.get(Calendar.DAY_OF_WEEK)-1
            else
                currentCalendar.get(Calendar.DAY_OF_WEEK)

        val lastMonth: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        lastMonth.add(Calendar.MONTH,-1)
        var lastMonthDays=lastMonth.getActualMaximum(Calendar.DAY_OF_MONTH)-(firstMonthDay-1)
        var nextMonthDays=1

        val monthDays:Int= currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
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

                            cal.add(Calendar.MONTH,-1)

                            cal.set(Calendar.DAY_OF_MONTH,lastMonthDays)
                            val row:ArrayList<NTuple6<Calendar,Float,Int, String, String,String>> = arrayListOf(NTuple6(cal,null,null,null,null,null))
                            val ldd= lastMonthData[lastMonthDays++] ?: row
                            localList.add(ldd)
                        }else {
                            //in month
                            if (!started) {
                                day = 1
                            }
                            val cal=date.clone() as Calendar
                            cal.set(Calendar.DAY_OF_MONTH,day)
                            val row:ArrayList<NTuple6<Calendar,Float,Int, String, String,String>> = arrayListOf(NTuple6(cal,null,null,null,null,null))
                            val dd= monthData[day] ?: row
                            localList.add(dd)

                            started = true
                        }
                        ++day
                    }else {
                        //after month
                        val cal=date.clone() as Calendar
                        cal.add(Calendar.MONTH,1)
                        cal.set(Calendar.DAY_OF_MONTH,nextMonthDays)
                        val row:ArrayList<NTuple6<Calendar,Float,Int, String, String,String>> = arrayListOf(NTuple6(cal,null,null,null,null,null))
                        val ndd=nextMonthData[nextMonthDays++]?:row
                        localList.add(ndd)

                    }
                }
            setAdapterManager(
                week,
                CalendarEditAdapter(requireContext(),localList),
                LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
            )
        }

    }

    private fun setAdapterManager(recyclerView: RecyclerView, adapter:RecyclerView.Adapter<LinearViewHolder>, layoutManager: RecyclerView.LayoutManager){
        recyclerView.adapter=adapter
        recyclerView.layoutManager=layoutManager
    }

    /**
     * Function to perform circular reveal animation
     */
    private fun circularReveal() {
        val finalRadius: Int = max(background.width, background.height)
        val circularReveal = ViewAnimationUtils.createCircularReveal(
            background,
            (activity as MainActivity).homeFabPosition[0],
            (activity as MainActivity).homeFabPosition[1],
            0f,
            finalRadius.toFloat()
        )
        circularReveal.duration = 1000
        background.visibility = View.VISIBLE
        circularReveal.start()

    }

}