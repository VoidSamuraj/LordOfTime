package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.charts.MyChart
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.LOTDatabaseHelper
import com.voidsamurai.lordoftime.fragments.adapters.LinearChartAdapter
import kotlinx.android.synthetic.main.fragment_chart.*
import java.util.*
import kotlin.collections.ArrayList


class ManyChartsFragment : Fragment() {

    private lateinit var db: SQLiteDatabase
    private lateinit var oh: LOTDatabaseHelper
    private lateinit  var legendMap:ArrayList<Pair<String,String>>
    private lateinit  var chartMap: TreeMap<String, Pair<Int,Float>>
    private lateinit var myChart: MyChart

    private lateinit var chartRecyclerView: RecyclerView
    companion object{
        @SuppressLint("StaticFieldLeak")
        private var cntx:Context?=null
        fun getContext()= cntx
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oh = LOTDatabaseHelper(requireContext())
        db = oh.readableDatabase
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        enterTransition=TransitionInflater.from(context).inflateTransition(android.R.transition.fade)
        exitTransition=TransitionInflater.from(context).inflateTransition(android.R.transition.fade)


    }

    override fun onStart() {
        super.onStart()
        legendMap =ArrayList()
        chartMap =TreeMap()
        chartRecyclerView =chart_description
        chartRecyclerView.addOnItemTouchListener(HomeFragment.RecyclerViewDisabler())
        fillChartWithData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_many_charts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cntx =context
        myChart =my_chart




    }

    private fun fillChartWithData(){

        chartMap = TreeMap()
        legendMap = ArrayList()
        for(row in MainActivity.getQueryArrayByDuration()) {
            if(chartMap.containsKey(row.category))
                chartMap.getValue(row.category).let {  chartMap.replace(row.category, Pair(it.first,it.second+row.workingTime))}
            else
                chartMap[row.category] = Pair(Color.parseColor(row.color),row.workingTime)
            legendMap.add(Pair(row.category,row.color))

        }
        myChart.fillData(
            chartMap.values.toList().sortedBy { pair ->-pair.second  },24,
            Color.LTGRAY)
        chartRecyclerView.adapter= LinearChartAdapter(legendMap.toMap().toList().asReversed())
        chartRecyclerView.layoutManager=LinearLayoutManager(requireContext())
        val cal:Calendar=Calendar.getInstance()
        cal.set(Calendar.MONTH,6)

    }


}