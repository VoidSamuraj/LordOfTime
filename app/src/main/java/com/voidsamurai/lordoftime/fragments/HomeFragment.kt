package com.voidsamurai.lordoftime.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.*
import android.util.Log
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
import com.voidsamurai.lordoftime.fragments.adapters.LinearChartAdapter
import com.voidsamurai.lordoftime.fragments.adapters.ToDoAdapter
import com.voidsamurai.lordoftime.startAnimation
import kotlinx.android.synthetic.main.fragment_chart.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.collections.ArrayList
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.voidsamurai.lordoftime.charts.MyChart
import com.voidsamurai.lordoftime.fragments.adapters.ToDoDateAdapter


class HomeFragment : Fragment() {

    companion object{

        private lateinit var myChart: MyChart
        private lateinit  var legendMap:ArrayList<Pair<String,String>>
        private lateinit  var chartMap: TreeMap<String, Pair<Int,Float>>
        private lateinit var todoRecyclerView: RecyclerView
        private lateinit var newestRecyclerView: RecyclerView
        private lateinit var chartRecyclerView: RecyclerView
        @SuppressLint("StaticFieldLeak")
        private  var contx : Context?=null

        fun fillViewsWithDatabaseData(){
            fillChartWithData()
            fillPriorityData()
            fillNewestData()
        }
        private fun fillPriorityData() {
            setAdapterManager(todoRecyclerView
                , ToDoAdapter(MainActivity.getQueryArrayByPriority())
                ,LinearLayoutManager(contx))
        }

        private fun fillNewestData(){
            setAdapterManager(newestRecyclerView
                , ToDoDateAdapter(ArrayList(MainActivity.getQueryArrayByDate()
                    .subList(0,if(MainActivity.getQueryArrayByDate().size>=3)3 else MainActivity.getQueryArrayByDate().size ).toList()))
                , LinearLayoutManager(contx))
        }

        private fun fillChartWithData(){

            chartMap= TreeMap()
            legendMap= ArrayList()
            for(row in MainActivity.getQueryArrayByDuration()) {
                if(chartMap.containsKey(row.category))
                    chartMap.getValue(row.category).let {  chartMap.replace(row.category, Pair(it.first,it.second+row.workingTime))}
                else
                    chartMap[row.category] = Pair(Color.parseColor(row.color),row.workingTime)
                legendMap.add(Pair(row.category,row.color))

            }
            myChart.fillData(chartMap .values.toList().sortedBy { pair ->-pair.second  },24,Color.LTGRAY)
            setAdapterManager(chartRecyclerView
                ,LinearChartAdapter(legendMap.toMap().toList().asReversed())
                ,LinearLayoutManager(contx))
        }
        private fun setAdapterManager(recyclerView: RecyclerView, adapter:RecyclerView.Adapter<LinearViewHolder>, layoutManager: RecyclerView.LayoutManager){
            recyclerView.adapter=adapter
            recyclerView.layoutManager=layoutManager
        }
    }



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
        legendMap=ArrayList()
        chartMap=TreeMap()
        fillViewsWithDatabaseData()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        //enterTransition=TransitionInflater.from(context).inflateTransition(android.R.transition.fade)
        //exitTransition=TransitionInflater.from(context).inflateTransition(android.R.transition.fade)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // pieChart=pie_chart
        postponeEnterTransition()
        card2.doOnPreDraw { startPostponedEnterTransition() }

        myChart=my_chart
        contx=context
        todoRecyclerView=todo_recycle_view
        newestRecyclerView=newest_Tasks_RecycleView
        chartRecyclerView=chart_description
        chartRecyclerView.addOnItemTouchListener(RecyclerViewDisabler())
        newestRecyclerView.addOnItemTouchListener(RecyclerViewDisabler())

        val animation = AnimationUtils.loadAnimation(requireContext(),R.anim.circle_explosion_anim).apply {
            duration = 500
            interpolator = FastOutSlowInInterpolator()
        }

        hiddenWidth=button_group.layoutParams.width
        cornerWidth=corner1.layoutParams.width
        fun getValueButtonAnimator():ValueAnimator{

            val width=resources.displayMetrics.widthPixels
            val valAnim:ValueAnimator=ValueAnimator.ofInt(hiddenWidth,width)

            valAnim.addUpdateListener {
               val lp=button_group.layoutParams
               lp.width=(it.animatedValue as Int)

                val c1=corner1.layoutParams
                val c2=corner2.layoutParams
                val cornerW:Int
                if(lp.width<hiddenWidth){

                    cornerW=(cornerWidth*(button_status.width.toFloat()/cornerWidth.toFloat())).toInt()
                    Log.v("perc","cornerwidth:"+cornerWidth+" perc:"+" scale:"+cornerW+"  lp:"+lp.width+"/"+hiddenWidth)
                    c1.width=cornerW
                    c1.height=cornerW
                    c2.width=cornerW
                    c2.height=cornerW

                }else cornerW=cornerWidth

                c1.width=cornerW
                c1.height=cornerW
                c2.width=cornerW
                c2.height=cornerW
                corner1.layoutParams=c1
                corner2.layoutParams=c2
                val perc= (((it.animatedValue as Int).toFloat()-hiddenWidth)/(width-hiddenWidth))

               status_image.alpha=1-perc
               label_hour.alpha=perc
               button_group.layoutParams=lp
            }

            valAnim.duration=500
            valAnim.interpolator=AnticipateOvershootInterpolator()
            return valAnim
        }


        task_changerFAB.setOnClickListener {

          //  task_changerFAB.visibility= View.INVISIBLE
            circle_anim.visibility= View.VISIBLE

            val vanim: ValueAnimator
            vanim = ValueAnimator.ofFloat(100f, 0f)
            vanim.addUpdateListener {
                Log.v("test",""+(it.animatedValue as Float) /100f)

                button_group.alpha=(it.animatedValue as Float) /100f

            }
            vanim.duration=400
            vanim.interpolator=AccelerateInterpolator()
            vanim.doOnEnd {   circle_anim.startAnimation(animation){
                requireActivity().window.   setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.task_color,null)))
                circle_anim.findNavController().navigate(R.id.action_FirstFragment_to_tasksEditHostFragment)
            } }
            if(!isButtonHidden) {
                va.doOnStart { vanim.reverse() }
                va.reverse()
                va.doOnStart { }
            }
            else
                vanim.start()


        }

        card2.setOnClickListener{
            val extras = FragmentNavigatorExtras(
                card2 to "chartCard"
            )

            findNavController().navigate(R.id.action_FirstFragment_to_manyCharts, null, null, extras)

        }

        label_hour.setOnClickListener {
            it?.findNavController()!!.navigate(R.id.action_FirstFragment_to_workingFragment)
        }

        for(i in 0 until button_group .childCount){
            button_group.getChildAt(i).setOnClickListener {
                if(isTaskStarted) {
                    va=getValueButtonAnimator()
                    if (isButtonHidden) {
                        va.start()
                        label_hour.isClickable=true
                        isButtonHidden = false
                    } else {
                        va.reverse()
                        label_hour.isClickable=false
                        isButtonHidden = true
                    }
                }else
                    it?.findNavController()!!.navigate(R.id.action_FirstFragment_to_workingFragment)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        label_hour.isClickable=false
        label_hour.alpha=0f
        isButtonHidden=true
    }
}
