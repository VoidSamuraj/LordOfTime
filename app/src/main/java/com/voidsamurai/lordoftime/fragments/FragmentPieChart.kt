package com.voidsamurai.lordoftime.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.databinding.FragmentPieChartBinding
import com.voidsamurai.lordoftime.fragments.adapters.LinearChartAdapter
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import java.util.*
import kotlin.collections.ArrayList


class FragmentPieChart : Fragment() {



    private var _binding: FragmentPieChartBinding?=null
    private val binding get()=_binding!!
    private lateinit  var legendMap:ArrayList<Pair<String,String>>
    private lateinit  var chartMap: TreeMap<String, Pair<Int,Float>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentPieChartBinding.inflate(inflater,container,false)
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.myChart.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        binding.chartDescription.addOnItemTouchListener(RecyclerViewDisabler())
        (activity as MainActivity).getQueryArrayByDuration().observe(viewLifecycleOwner,{
            fillChartWithData(it)

        })
    }

    private fun fillChartWithData(tab:ArrayList<DataRowWithColor>){

        chartMap = TreeMap()
        legendMap = ArrayList()
        for(row in tab) {
            if(chartMap.containsKey(row.category))
                chartMap.getValue(row.category).let {  chartMap.replace(row.category, Pair(it.first,it.second+row.workingTime/3600))}
            else
                chartMap[row.category] = Pair(Color.parseColor(row.color),row.workingTime/3600)
            legendMap.add(Pair(row.category,row.color))

        }
        binding.myChart.fillData(
            chartMap.values.toList().sortedBy { pair ->-pair.second  },24,
            Color.LTGRAY)
        binding.chartDescription.adapter= LinearChartAdapter(legendMap.toMap().toList().asReversed())
        binding.chartDescription.layoutManager=LinearLayoutManager(context)

    }
    class RecyclerViewDisabler : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean=true
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }


    override fun onDestroyView() {
        (activity as MainActivity).getQueryArrayByDuration().removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}