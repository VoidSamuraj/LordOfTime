package com.voidsamurai.lordoftime.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import android.view.animation.AnticipateOvershootInterpolator
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentHomeBinding
import com.voidsamurai.lordoftime.fragments.adapters.ToDoAdapter
import com.voidsamurai.lordoftime.fragments.adapters.ToDoDateAdapter
import java.util.*
import kotlin.math.abs
import kotlin.math.max


class HomeFragment : Fragment() {

    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var newestRecyclerView: RecyclerView
    private  var hiddenWidth:Int=0
    private  var cornerWidth:Int=0
    private var _homeFragmentBinding: FragmentHomeBinding?=null
    val homeFragmentBinding get()=_homeFragmentBinding!!
    private var statusMaxWidth:Int=0
    var isButtonHidden:Boolean=true
    private lateinit var valueButtonAnimator: ValueAnimator
    private var hours:Int=0
    private var minutes:Int=0
    private var seconds:Int=0


    private fun setAdapterManager(recyclerView: RecyclerView, adapter:RecyclerView.Adapter<LinearViewHolder>, layoutManager: RecyclerView.LayoutManager){
        recyclerView.adapter=adapter
        recyclerView.layoutManager=layoutManager
    }

    //blocking recycler view click
    class RecyclerViewDisabler : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean=true
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    override fun onStart() {
        super.onStart()
        hiddenWidth = resources.getDimension(R.dimen.button_group_width).toInt()
        cornerWidth = resources.getDimension(R.dimen.button_corners).toInt()

        // set keyboard mode
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        (activity as MainActivity).getQueryArrayByPriority().observe(viewLifecycleOwner) {
            setAdapterManager(
                todoRecyclerView, ToDoAdapter(
                    ArrayList(
                        it
                            .subList(0, if (it.size >= 10) 10 else it.size).toList()
                    ), activity as MainActivity
                ), LinearLayoutManager(requireContext())
            )
        }
        (activity as MainActivity).getQueryArrayByDate().observe(viewLifecycleOwner) {
            setAdapterManager(
                newestRecyclerView, ToDoDateAdapter(
                    ArrayList(
                        it
                            .subList(0, if (it.size >= 3) 3 else it.size).toList()
                    ),
                    requireContext()
                ), LinearLayoutManager(requireContext())
            )
        }

        homeFragmentBinding.switcherHour.text = "0"
        homeFragmentBinding.switcherMinutes1.text = "0"
        homeFragmentBinding.switcherMinutes2.text = "0"
        homeFragmentBinding.switcherSeconds1.text = "0"
        homeFragmentBinding.switcherSeconds2.text = "0"

        val updateIntervalSeconds = 1 * 30

        if ((activity as MainActivity).isTaskStarted){

            (activity as MainActivity).getCurrentWorkingTime().observe(viewLifecycleOwner) {timenow->
                val it=((Calendar.getInstance().timeInMillis-(activity as MainActivity).getTimeStarted())/1000).toInt()
                var currentFormatedTime = ((it - (it % 3600)) / 3600)
                homeFragmentBinding.switcherHour.text = currentFormatedTime.toString()
                hours = currentFormatedTime
                currentFormatedTime = ((it - (it % 60)) / 60) % 60
                homeFragmentBinding.switcherMinutes1.text =
                    ((currentFormatedTime - (currentFormatedTime % 10)) / 10).toString()
                homeFragmentBinding.switcherMinutes2.text = (currentFormatedTime % 10).toString()
                minutes = currentFormatedTime
                currentFormatedTime = it % 60
                homeFragmentBinding.switcherSeconds1.text =
                    ((currentFormatedTime - (currentFormatedTime % 10)) / 10).toString()
                homeFragmentBinding.switcherSeconds2.text = (currentFormatedTime % 10).toString()

                if (it % updateIntervalSeconds == 0) {
                    val wt =
                        ((Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis - (activity as MainActivity).getLastTimeUpdated()) / 1000).toInt()
                    (activity as MainActivity).setLastTimeUpdated(
                        Calendar.getInstance(
                            TimeZone.getTimeZone(
                                "UTC"
                            )
                        ).timeInMillis
                    )
                    (activity as MainActivity).updateOldstats(
                        wt + ((activity as MainActivity).getDBOpenHelper().getTaskRow(
                            (activity as MainActivity).getCurrentTaskId(),
                            (activity as MainActivity).userId
                        ).currentWorkingTime * 3600).toInt(), updateIntervalSeconds
                    )
                    val myFragment: FragmentPieChart =
                        childFragmentManager.findFragmentById(R.id.chart_frag) as FragmentPieChart
                    myFragment.fillChartWithData(
                        (activity as MainActivity).getOldDataWithColors(
                            true
                        )
                    )

                }
                seconds = currentFormatedTime
            }
            }
        enterTransition=null

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transition=TransitionInflater.from(context).inflateTransition(R.transition.move_fade)
        sharedElementEnterTransition = transition
        sharedElementReturnTransition = transition
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _homeFragmentBinding=FragmentHomeBinding.inflate(inflater,container,false)
        return homeFragmentBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if((activity as MainActivity).isTaskStarted) {
            homeFragmentBinding.statusImage.visibility=View.INVISIBLE
            homeFragmentBinding.analogClock.visibility=View.VISIBLE

        }else{
            homeFragmentBinding.statusImage.visibility=View.VISIBLE
            homeFragmentBinding.analogClock.visibility = View.INVISIBLE
        }

        todoRecyclerView=homeFragmentBinding.todoRecycleView
        newestRecyclerView=homeFragmentBinding.newestTasksRecycleView
        newestRecyclerView.addOnItemTouchListener(RecyclerViewDisabler())

        homeFragmentBinding.taskChangerFAB.setOnClickListener { it ->

            val background=homeFragmentBinding.homeFrag
            val intArray= IntArray(2)
            homeFragmentBinding.taskChangerFAB.getLocationOnScreen(intArray)
            intArray[0]= (intArray[0]+(resources.getDimension(R.dimen.fab_size)/2)/*resources.getDimension(R.dimen.fab_margin)+resources.getDimension(R.dimen.small_padding)*/).toInt()
            intArray[1]= (intArray[1]-resources.getDimension(R.dimen.bar_height)).toInt()

            (activity as MainActivity).let {
                it.homeFabPosition.let { itt ->
                    itt[0]=intArray[0]
                    itt[1]=intArray[1]
                }
                it.homeDrawable=background.drawToBitmap().toDrawable(resources)

            }

            it.findNavController().navigate(R.id.action_FirstFragment_to_calendarEditFragment)
        }

        //chart listener
        homeFragmentBinding.card2Click.setOnClickListener{
            val extras = FragmentNavigatorExtras(homeFragmentBinding.card2 to "chartCard2")
            findNavController().navigate(R.id.action_FirstFragment_to_manyCharts, null, null, extras)

        }

        //view is visible after workbutton clicked
        homeFragmentBinding.view.setOnClickListener {
            valueButtonAnimator.reverse()
            homeFragmentBinding.view.isClickable=false
            isButtonHidden = true
            setWorkButtonListener()
        }
        homeFragmentBinding.view.isClickable=false
        setWorkButtonListener()

        (activity as MainActivity).let {
            if(it.isFromCalendarFragment){

                homeFragmentBinding.bg.background=it.homeDrawable
                homeFragmentBinding.bg.visibility=View.VISIBLE
                val viewTreeObserver = homeFragmentBinding.bg.viewTreeObserver

                if (viewTreeObserver.isAlive) {
                    viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            circularRevealBack()
                            homeFragmentBinding.bg.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    })
                }

                it.isFromCalendarFragment=false
            }
        }
    }
    private fun setWorkButtonListener(){
        for(i in 0 until homeFragmentBinding.buttonGroup.childCount){
            homeFragmentBinding.buttonGroup.getChildAt(i).setOnClickListener {
                valueButtonAnimator=getWorkButtonNavigationAnimator()
                if (isButtonHidden) {
                    valueButtonAnimator.start()
                    homeFragmentBinding.view.visibility=View.VISIBLE
                    homeFragmentBinding.view.isClickable=true
                    isButtonHidden = false

                    for(j in 0 until homeFragmentBinding.buttonGroup.childCount)
                        homeFragmentBinding.buttonGroup.getChildAt(j).setOnClickListener {
                            homeFragmentBinding.corner1bg.visibility = View.VISIBLE
                            statusMaxWidth = homeFragmentBinding.buttonStatus.layoutParams.width
                            findNavController().navigate(R.id.action_FirstFragment_to_workingFragment)

                        }
                }

            }
        }
    }

    //set work button animation on navigation
    private fun getWorkButtonNavigationAnimator():ValueAnimator{

        val width=resources.displayMetrics.widthPixels
        val valAnim:ValueAnimator=ValueAnimator.ofInt(hiddenWidth,width)

        valAnim.addUpdateListener {
            val lp=homeFragmentBinding.buttonGroup.layoutParams
            lp.width=(it.animatedValue as Int)

            val cornerW:Int = if(lp.width<hiddenWidth){
                val minus=resources.getDimension(R.dimen.button_half_sphere_width)+resources.getDimension(R.dimen.padding_elevation)
                val scale=hiddenWidth-minus
                abs(cornerWidth*((homeFragmentBinding.buttonGroup.width.toFloat()-minus)/scale)).toInt()

            }else cornerWidth

            setCornersSize(homeFragmentBinding.corner1,cornerW)
            setCornersSize(homeFragmentBinding.corner2,cornerW)

            val percentage= (((it.animatedValue as Int).toFloat()-hiddenWidth)/(width-hiddenWidth))
            if((activity as MainActivity).isTaskStarted) {
                homeFragmentBinding.analogClock.alpha = 1 - percentage
                homeFragmentBinding.labelHour.alpha=percentage

            }else
                homeFragmentBinding.startTaskLabel.alpha=percentage
            homeFragmentBinding.buttonGroup.alpha=(2f*percentage)+0.8f
            homeFragmentBinding.statusImage.alpha=1-percentage
            homeFragmentBinding.buttonGroup.layoutParams=lp
        }

        valAnim.duration=500
        valAnim.interpolator=AnticipateOvershootInterpolator()
        return valAnim
    }


    private fun setCornersSize(corner:View,dp:Int,X:Boolean=true,Y:Boolean=true){
        val c1=corner.layoutParams
        if(X)
            c1.width=dp
        if (Y)
            c1.height=dp
        corner.layoutParams=c1
    }
    override fun onResume() {
        super.onResume()
        homeFragmentBinding.labelHour.visibility=View.VISIBLE
        homeFragmentBinding.startTaskLabel.visibility=View.VISIBLE
        (activity as MainActivity).updateRutines()
        homeFragmentBinding.startTaskLabel.alpha=0f
        homeFragmentBinding.labelHour.alpha=0f
        isButtonHidden=true
    }

    override fun onDestroy() {
        super.onDestroy()
        _homeFragmentBinding=null
    }


    override fun onDestroyView() {
        (activity as MainActivity).getQueryArrayByPriority().removeObservers(viewLifecycleOwner)
        (activity as MainActivity).getQueryArrayByDate().removeObservers(viewLifecycleOwner)
        (activity as MainActivity).getCurrentWorkingTime().removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }

    /**
     * Animation on navigate back from tasks calendar.
     */
    private fun circularRevealBack() {
        val background=homeFragmentBinding.bg
        val finalRadius = max(background.width, background.height).toFloat()
        val circularReveal =
            ViewAnimationUtils.createCircularReveal(
                background,
                (activity as MainActivity).homeFabPosition[0],
                (activity as MainActivity).homeFabPosition[1],
                finalRadius,
                0f)
        circularReveal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {

                homeFragmentBinding.bg.visibility=View.INVISIBLE

            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        circularReveal.duration = 1000
        circularReveal.start()

    }
}
