package com.voidsamurai.lordoftime.fragments

import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.*
import android.view.*
import android.view.animation.*
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.fragments.adapters.ToDoAdapter
import com.voidsamurai.lordoftime.startAnimation
import kotlin.collections.ArrayList
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.voidsamurai.lordoftime.databinding.FragmentHomeBinding
import com.voidsamurai.lordoftime.fragments.adapters.ToDoDateAdapter


class HomeFragment : Fragment() {

        private lateinit var todoRecyclerView: RecyclerView
        private lateinit var newestRecyclerView: RecyclerView

        private fun setAdapterManager(recyclerView: RecyclerView, adapter:RecyclerView.Adapter<LinearViewHolder>, layoutManager: RecyclerView.LayoutManager){
            recyclerView.adapter=adapter
            recyclerView.layoutManager=layoutManager
        }


    private var _homeFragmentBinding: FragmentHomeBinding?=null
    private val homeFragmentBinding get()=_homeFragmentBinding!!
    private var hiddenWidth:Int=0
    private var cornerWidth:Int=0
    private var isButtonHidden:Boolean=true
    private var isTaskStarted:Boolean=true
    private lateinit var va: ValueAnimator

    class RecyclerViewDisabler : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean=true
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    override fun onStart() {
        super.onStart()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        MainActivity.getQueryArrayByPriority().observe(viewLifecycleOwner,{
            setAdapterManager(todoRecyclerView
                , ToDoAdapter(it)
                ,LinearLayoutManager(requireContext()))
        })
        MainActivity.getQueryArrayByDate().observe(viewLifecycleOwner,{
            setAdapterManager(newestRecyclerView
                , ToDoDateAdapter(ArrayList(it
                    .subList(0,if(it.size>=3)3 else it.size ).toList()))
                , LinearLayoutManager(requireContext()))
        })
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
        postponeEnterTransition()
        homeFragmentBinding.card2.doOnPreDraw { startPostponedEnterTransition() }
        todoRecyclerView=homeFragmentBinding.todoRecycleView
        newestRecyclerView=homeFragmentBinding.newestTasksRecycleView
        newestRecyclerView.addOnItemTouchListener(RecyclerViewDisabler())

        val animation = AnimationUtils.loadAnimation(requireContext(),R.anim.circle_explosion_anim).apply {
            duration = 500
            interpolator = FastOutSlowInInterpolator()
        }

        hiddenWidth=homeFragmentBinding.buttonGroup.layoutParams.width
        cornerWidth=homeFragmentBinding.corner1.layoutParams.width
        fun getValueButtonAnimator():ValueAnimator{

            val width=resources.displayMetrics.widthPixels
            val valAnim:ValueAnimator=ValueAnimator.ofInt(hiddenWidth,width)

            valAnim.addUpdateListener {
               val lp=homeFragmentBinding.buttonGroup.layoutParams
               lp.width=(it.animatedValue as Int)

                val c1=homeFragmentBinding.corner1.layoutParams
                val c2=homeFragmentBinding.corner2.layoutParams
                val cornerW:Int
                if(lp.width<hiddenWidth){

                    cornerW=(cornerWidth*(homeFragmentBinding.buttonStatus.width.toFloat()/cornerWidth.toFloat())).toInt()
                    c1.width=cornerW
                    c1.height=cornerW
                    c2.width=cornerW
                    c2.height=cornerW

                }else cornerW=cornerWidth

                c1.width=cornerW
                c1.height=cornerW
                c2.width=cornerW
                c2.height=cornerW
                homeFragmentBinding.corner1.layoutParams=c1
                homeFragmentBinding.corner2.layoutParams=c2
                val percentage= (((it.animatedValue as Int).toFloat()-hiddenWidth)/(width-hiddenWidth))

               homeFragmentBinding.statusImage.alpha=1-percentage
               homeFragmentBinding.labelHour.alpha=percentage
               homeFragmentBinding.buttonGroup.layoutParams=lp
            }

            valAnim.duration=500
            valAnim.interpolator=AnticipateOvershootInterpolator()
            return valAnim
        }


        homeFragmentBinding.taskChangerFAB.setOnClickListener {

            homeFragmentBinding.circleAnim.visibility= View.VISIBLE

            val valueAnimator: ValueAnimator= ValueAnimator.ofFloat(100f, 0f)
            valueAnimator.addUpdateListener {

                homeFragmentBinding.buttonGroup.alpha=(it.animatedValue as Float) /100f

            }
            valueAnimator.duration=400
            valueAnimator.interpolator=AccelerateInterpolator()
            valueAnimator.doOnEnd {   homeFragmentBinding.circleAnim.startAnimation(animation){
                requireActivity().window.   setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.task_color,null)))
                homeFragmentBinding.circleAnim.findNavController().navigate(R.id.action_FirstFragment_to_tasksEditHostFragment)
            } }
            if(!isButtonHidden) {
                va.doOnStart { valueAnimator.reverse() }
                va.reverse()
                va.doOnStart { }
            }
            else
                valueAnimator.start()


        }

        homeFragmentBinding.card2.setOnClickListener{
            val extras = FragmentNavigatorExtras(
                homeFragmentBinding.card2 to "chartCard"
            )

            findNavController().navigate(R.id.action_FirstFragment_to_manyCharts, null, null, extras)

        }

        homeFragmentBinding.labelHour.setOnClickListener {
            it?.findNavController()!!.navigate(R.id.action_FirstFragment_to_workingFragment)
        }

        for(i in 0 until homeFragmentBinding.buttonGroup.childCount){
            homeFragmentBinding.buttonGroup.getChildAt(i).setOnClickListener {
                if(isTaskStarted) {
                    va=getValueButtonAnimator()
                    if (isButtonHidden) {
                        va.start()
                        homeFragmentBinding.labelHour.isClickable=true
                        isButtonHidden = false
                    } else {
                        va.reverse()
                        homeFragmentBinding.labelHour.isClickable=false
                        isButtonHidden = true
                    }
                }else
                    it?.findNavController()!!.navigate(R.id.action_FirstFragment_to_workingFragment)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        homeFragmentBinding.labelHour.isClickable=false
        homeFragmentBinding.labelHour.alpha=0f
        isButtonHidden=true
    }

    override fun onDestroy() {
        super.onDestroy()
        _homeFragmentBinding=null
    }


    override fun onDestroyView() {
        MainActivity.getQueryArrayByPriority().removeObservers(viewLifecycleOwner)
        MainActivity.getQueryArrayByDate().removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}
