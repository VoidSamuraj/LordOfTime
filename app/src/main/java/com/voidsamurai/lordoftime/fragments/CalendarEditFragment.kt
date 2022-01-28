package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import android.util.TypedValue

import android.view.ViewAnimationUtils

import android.animation.Animator
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.drawToBitmap
import androidx.navigation.fragment.navArgs


class CalendarEditFragment : Fragment() {

    var _calendarViewBinding:FragmentCalendarEditBinding?=null
    val binding get()=_calendarViewBinding!!

    private lateinit var background:View
    var dayAimH: MutableLiveData<Int> = MutableLiveData(6)
    private var productiveDays:Int=0
    private var workPer:Float=0f
    private var productivePer:Float=0f
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

        background = binding.background

        background.visibility = View.INVISIBLE
        binding.bg.background=(activity as MainActivity).homeDrawable



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
        (activity as MainActivity).homeDrawable=background.drawToBitmap().toDrawable(resources)
        super.onPause()
    }

    override fun onDestroyView() {
        dayAimH.removeObservers(viewLifecycleOwner)
        _calendarViewBinding=null
        //(activity as MainActivity).homeDrawable=background.drawToBitmap().toDrawable(resources)

        super.onDestroyView()





    }
    /*
     private fun setFirstDay(calendar: Calendar, isMondayFirstDay: Boolean){
         if (isMondayFirstDay)
             calendar.firstDayOfWeek=Calendar.MONDAY
         else
             calendar.firstDayOfWeek=Calendar.SUNDAY
     }*/
    private fun createDaysCalendarChart(monthCalendar: Calendar, isMondayFirstDay:Boolean=false, weeks:Array<RecyclerView>) {
        productiveDays = 0
        val hours = 0f
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
                // Log.v("DataRAW",""+nr+++" "+x.date.get(Calendar.MONTH)+"/"+x.date.get(Calendar.DAY_OF_MONTH)+" "+x.name+" "+x.id)
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
            /*
            var selectedMonth=monthCalendar.get(Calendar.MONTH)
            if(selectedMonth)
            val selectedYear=monthCalendar.get(Calendar.YEAR)
            */
            return allData.filter { entry ->

                //  setFirstDay( entry.key,isMondayFirstDay)

                (selectedCalendar.get(Calendar.YEAR) == entry.key.get(Calendar.YEAR))
                        && (selectedCalendar.get(Calendar.MONTH) == entry.key.get(Calendar.MONTH))
            }.mapKeys { it.key.get(Calendar.DAY_OF_MONTH) }
        }
        val monthData:Map<Int,ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> =getMonthData(0)
        val lastMonthData:Map<Int,ArrayList<NTuple6<Calendar, Float, Int, String, String, String>>> =getMonthData(-1)
        val nextMonthData:Map<Int,ArrayList<NTuple6<Calendar,Float, Int, String, String, String>>> =getMonthData(1)

        /*
         for(x in allData.values){
             for(y in x)
                 Log.v("Data",""+y.t1.get(Calendar.MONTH)+"/"+y.t1.get(Calendar.DAY_OF_MONTH)+" "+y.t4+" "+y.t5+" "+y.t6)
         }*/

        // val c:Calendar= Calendar.getInstance()
        //c.set(2021, 11, 1, 12, 6,0)

        //  val list:ArrayList<ArrayList<ArrayList<NTuple6<Calendar,Float,Int, String, String,String>>?>> = arrayListOf()
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

        Log.v("Monthdata", ""+monthData)
        val lastMonth: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        // setFirstDay(lastMonth,isMondayFirstDay)
        lastMonth.add(Calendar.MONTH,-1)
        var lastMonthDays=lastMonth.getActualMaximum(Calendar.DAY_OF_MONTH)-(firstMonthDay-1)
        var nextMonthDays=1
      /*  if(isMondayFirstDay) {
            ++lastMonthDays
            ++nextMonthDays

        }*/
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
                              /*  if(isMondayFirstDay)
                                    ++day*/
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
            // list.add(localList)
        }
/*
        weeks.forEachIndexed { weekDay, week ->
            setAdapterManager(
                week,
                CalendarEditAdapter(requireContext(),list[weekDay]),
                LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
            )
        }*/

        workPer=hours/( dayAimH.value!! * monthDays)
        productivePer=(productiveDays.toFloat()/monthDays*100)
    }

    private fun setAdapterManager(recyclerView: RecyclerView, adapter:RecyclerView.Adapter<LinearViewHolder>, layoutManager: RecyclerView.LayoutManager){
        recyclerView.adapter=adapter
        recyclerView.layoutManager=layoutManager
    }


    private fun circularReveal() {
        val finalRadius: Int = Math.max(background.width, background.height)
        val circularReveal = ViewAnimationUtils.createCircularReveal(
            background,
            (activity as MainActivity).homeFabPosition[0],
            (activity as MainActivity).homeFabPosition[1],
           /* cx,
            cy, */
            0f,
            finalRadius.toFloat()
        )
        circularReveal.duration = 1000
        background.visibility = View.VISIBLE
        circularReveal.start()

    }
/*
    private fun circularRevealBack(des:()->Unit) {

        val finalRadius = Math.max(background.width, background.height).toFloat()
        val circularReveal =
            ViewAnimationUtils.createCircularReveal(
                background,
                position[0],
                position[1],
                finalRadius,
                0f)
        circularReveal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                background.visibility = View.INVISIBLE
                des()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        circularReveal.duration = 3000
        circularReveal.start()

    }*/

}