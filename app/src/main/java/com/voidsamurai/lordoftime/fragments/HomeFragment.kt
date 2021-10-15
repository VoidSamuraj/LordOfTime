package com.voidsamurai.lordoftime.fragments

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.Interpolator
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
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
import com.voidsamurai.lordoftime.startAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs


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

    class RecyclerViewDisabler : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean=true
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    override fun onStart() {
        super.onStart()
        hiddenWidth=resources.getDimension(R.dimen.button_group_width).toInt()
        cornerWidth =resources.getDimension(R.dimen.button_corners).toInt()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        (activity as MainActivity).getQueryArrayByPriority().observe(viewLifecycleOwner,{
            setAdapterManager(todoRecyclerView
                , ToDoAdapter(it)
                ,LinearLayoutManager(requireContext()))
        })
        (activity as MainActivity).getQueryArrayByDate().observe(viewLifecycleOwner,{
            setAdapterManager(newestRecyclerView
                , ToDoDateAdapter(ArrayList(it
                    .subList(0,if(it.size>=3)3 else it.size ).toList()))
                , LinearLayoutManager(requireContext()))
        })

        fun factory():TextView{
            val textView = TextView(requireContext())
            textView.textSize=40f
            return textView
        }

        homeFragmentBinding.switcherHour.setFactory{factory()}
        homeFragmentBinding.switcherMinutes1.setFactory{factory()}
        homeFragmentBinding.switcherMinutes2.setFactory{factory()}
        homeFragmentBinding.switcherSeconds1.setFactory{factory()}
        homeFragmentBinding.switcherSeconds2.setFactory{factory()}

        homeFragmentBinding.switcherHour.setText("0")
        homeFragmentBinding.switcherMinutes1.setText("0")
        homeFragmentBinding.switcherMinutes2.setText("0")
        homeFragmentBinding.switcherSeconds1.setText("0")
        homeFragmentBinding.switcherSeconds2.setText("0")

        (activity as MainActivity).getCurrentWorkingTime().observe(viewLifecycleOwner,{
            var currentFormatedTime=((it-(it%3600))/3600)
            if(currentFormatedTime!=hours) {
                homeFragmentBinding.switcherHour.setText(currentFormatedTime.toString())
                hours=currentFormatedTime
            }
            currentFormatedTime=((it-(it%60))/60)%60
            if(currentFormatedTime!=minutes) {
                if(minutes-(minutes%10)!=currentFormatedTime-(currentFormatedTime%10)){
                    homeFragmentBinding.switcherMinutes1.setText(((currentFormatedTime-(currentFormatedTime%10))/10).toString())
                    homeFragmentBinding.switcherMinutes2.setText((currentFormatedTime%10).toString())
                }
                else
                    homeFragmentBinding.switcherMinutes2.setText((currentFormatedTime%10).toString())
                minutes=currentFormatedTime
            }
            currentFormatedTime=it%60
            if(currentFormatedTime!=seconds) {
                if(seconds-(seconds%10)!=currentFormatedTime-(currentFormatedTime%10)) {
                    homeFragmentBinding.switcherSeconds1.setText(((currentFormatedTime-(currentFormatedTime%10)) / 10).toString())
                    homeFragmentBinding.switcherSeconds2.setText((currentFormatedTime % 10).toString())
                }
                else
                    homeFragmentBinding.switcherSeconds2.setText((currentFormatedTime % 10).toString())

                seconds=currentFormatedTime
            }
        })


        enterTransition=null

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)

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
        //settings for back navigation
        if((activity as MainActivity).isFromEditFragment){

            requireActivity().window.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.task_color,null)))
            homeFragmentBinding.buttonGroup.alpha=0f
            homeFragmentBinding.taskChangerFAB.alpha=0f
            val animator=getCircleValueAnimator(true)
            animator.start()
            CoroutineScope(Dispatchers.Main).launch{
                val color=resources.getColor(R.color.background,null)
                homeFragmentBinding.relativeLayout.setBackgroundColor(color)
                delay(510)
                requireActivity().window.setBackgroundDrawable(ColorDrawable(color))
            }
            (activity as MainActivity).isFromEditFragment=false

        }else if((activity as MainActivity).isFromWorkFragment&&false){

            homeFragmentBinding.statusImage.visibility = View.INVISIBLE
            homeFragmentBinding.analogClock.visibility = View.INVISIBLE
            homeFragmentBinding.view.visibility = View.VISIBLE
            homeFragmentBinding.view.setBackgroundColor(
                resources.getColor(
                    R.color.work_button_color,
                    null
                )
            )

            val va = getWorkButtonClickAnimator(true)
            va.doOnStart {
                val cornersW = cornerWidth * 51
                setCornersSize(homeFragmentBinding.buttonGroup, statusMaxWidth, Y = false)
                setCornersSize(homeFragmentBinding.corner1, cornersW)
                setCornersSize(homeFragmentBinding.corner1bg, cornersW)
                homeFragmentBinding.corner1bg.visibility = View.VISIBLE
                homeFragmentBinding.buttonGroup.alpha = 1f
                homeFragmentBinding.view.visibility = View.INVISIBLE
                homeFragmentBinding.view.setBackgroundColor(Color.TRANSPARENT)
            }
            va.doOnEnd {
                setCornersSize(homeFragmentBinding.buttonGroup, hiddenWidth, Y = false)
                setCornersSize(homeFragmentBinding.corner1, cornerWidth)
                setCornersSize(homeFragmentBinding.corner1bg, cornerWidth)
                homeFragmentBinding.corner1bg.visibility = View.INVISIBLE
                if ((activity as MainActivity).isTaskStarted)
                    homeFragmentBinding.analogClock.visibility = View.VISIBLE
                else
                    homeFragmentBinding.statusImage.visibility = View.VISIBLE
                setCornersSize(homeFragmentBinding.corner2, cornerWidth)
                homeFragmentBinding.buttonGroup.alpha = 0.8f

            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(resources.getInteger(R.integer.time_duration_normal).toLong())
                va.reverse()
            }
            (activity as MainActivity).isFromWorkFragment = false

        }



        todoRecyclerView=homeFragmentBinding.todoRecycleView
        newestRecyclerView=homeFragmentBinding.newestTasksRecycleView
        newestRecyclerView.addOnItemTouchListener(RecyclerViewDisabler())

        homeFragmentBinding.taskChangerFAB.setOnClickListener {

            homeFragmentBinding.circleAnim.visibility= View.VISIBLE

            val valueAnimator=getCircleValueAnimator(false)
            valueAnimator.start()


        }

        //chart listener
        homeFragmentBinding.card2Click.setOnClickListener{
            val extras = FragmentNavigatorExtras(
                homeFragmentBinding.card2 to "chartCard"
            )

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

                    for(i in 0 until homeFragmentBinding.buttonGroup.childCount)
                        homeFragmentBinding.buttonGroup.getChildAt(i).setOnClickListener {

                            homeFragmentBinding.corner1bg.visibility = View.VISIBLE

                          //  val valAnim = getWorkButtonClickAnimator(false)

                         //   valAnim.doOnEnd {
                                statusMaxWidth = homeFragmentBinding.buttonStatus.layoutParams.width
                                findNavController().navigate(R.id.action_FirstFragment_to_workingFragment)
                         /*   }
                            CoroutineScope(Dispatchers.Main).run {
                                launch {
                                    valAnim.start()
                                }
                            }*/
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

            val cornerW:Int
            if(lp.width<hiddenWidth){
                val minus=resources.getDimension(R.dimen.button_half_sphere_width)+resources.getDimension(R.dimen.padding_elevation)
                val scale=hiddenWidth-minus
                cornerW=abs(cornerWidth*((homeFragmentBinding.buttonGroup.width.toFloat()-minus)/scale)).toInt()

            }else cornerW=cornerWidth

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

    private fun getWorkButtonClickAnimator(isEnter:Boolean):ValueAnimator{
        val lp=homeFragmentBinding.buttonGroup.layoutParams
        val groupW=lp.width
        val valAnim: ValueAnimator=
            if (isEnter)
                ValueAnimator.ofInt(120,1)
            else
                ValueAnimator.ofInt(1,120)

        valAnim.addUpdateListener {
            if (!isEnter)
                lp.width=groupW*(20*(it.animatedValue as Int)/100)+groupW
            else {
                val minus=resources.getDimension(R.dimen.button_half_sphere_width)+resources.getDimension(R.dimen.padding_elevation)
                // lp.width = hiddenWidth * (80 * ((it.animatedValue as Int) / 100)) + hiddenWidth
                lp.width =cornerWidth * (39*(it.animatedValue as Int)/100) + cornerWidth+minus.toInt()
            }

            homeFragmentBinding.buttonGroup.layoutParams=lp

            val cornerW =cornerWidth * (39*(it.animatedValue as Int)/100) + cornerWidth
            setCornersSize(homeFragmentBinding.corner1bg,cornerW)
            setCornersSize(homeFragmentBinding.corner1,cornerW)
            setCornersSize(homeFragmentBinding.corner2,cornerW)

        }

        valAnim.duration=400

        valAnim.interpolator=FastOutLinearInInterpolator()
        if (isEnter){
            valAnim.interpolator=ReverseInterpolator()
        }
        return valAnim
    }
    private fun getCircleValueAnimator(isEnter:Boolean):ValueAnimator{

        homeFragmentBinding.buttonGroup.elevation=1f

        val valueAnimator: ValueAnimator=
            if (isEnter)
                ValueAnimator.ofFloat(0f, 100f)
            else
                ValueAnimator.ofFloat(100f, 0f)

            valueAnimator.addUpdateListener {
                var alpha=(it.animatedValue as Float) /100f
                if(isEnter){
                    if (it.animatedValue as Float>96)
                        alpha=100f
                    else if(it.animatedValue as Float>10)
                        alpha=alpha*9/1000
                    else
                        alpha=0f

                }
                homeFragmentBinding.buttonGroup.alpha=alpha
                homeFragmentBinding.taskChangerFAB.alpha=alpha
            }
        if (isEnter)
            valueAnimator.duration=500
        else
            valueAnimator.duration=200
        valueAnimator.interpolator=AccelerateInterpolator()

        val animation = AnimationUtils.loadAnimation(requireContext(),R.anim.circle_explosion_anim).apply {
            duration = 500
            interpolator = FastOutSlowInInterpolator()
        }

        if(isEnter)
            animation.interpolator=ReverseInterpolator()

        valueAnimator.doOnStart {
            CoroutineScope(Dispatchers.Main).launch {

                delay(1)
                homeFragmentBinding.circleAnim.startAnimation(animation) {
                    if (!isEnter) {
                        (activity as MainActivity).isFromEditFragment=true
                        homeFragmentBinding.circleAnim.findNavController()
                            .navigate(R.id.action_FirstFragment_to_calendarEditFragment)
                    }
                }
            }
        }
        homeFragmentBinding.buttonGroup.elevation=100f

        return valueAnimator
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
    class ReverseInterpolator : Interpolator {
        override fun getInterpolation(paramFloat: Float): Float {
            return abs(paramFloat - 1f)
        }
    }
}
