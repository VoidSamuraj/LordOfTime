package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.animation.*
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)                           //
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // pieChart=pie_chart

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

        task_changerFAB.setOnClickListener {
            task_changerFAB.visibility= View.INVISIBLE
            circle_anim.visibility= View.VISIBLE
            circle_anim.startAnimation(animation){
                requireActivity().window.   setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.task_color)))
                it?.findNavController()!!.navigate(R.id.action_FirstFragment_to_tasksEditHostFragment)
            }

        }

        card2.setOnClickListener{
            val extras = FragmentNavigatorExtras(
                card2 to "chartCard"
            )
            findNavController().navigate(R.id.action_FirstFragment_to_manyCharts, null, null, extras)

        }
    }



}