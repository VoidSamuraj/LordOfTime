package com.voidsamurai.lordoftime.fragments

import android.app.AppOpsManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentPieChartBinding
import com.voidsamurai.lordoftime.fragments.adapters.LinearChartAdapter
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * Fragment to display statistics about other apps usage. That needs permission to data usage.
 */
class AppsUsage : Fragment(){

    private var _binding: FragmentPieChartBinding?=null
    private val binding get()=_binding!!
    private lateinit  var legendMap:TreeMap<String,Pair<String,Float>>
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CoroutineScope(Dispatchers.Default ).launch {
                (activity as MainActivity).let {activity->
                    if(activity.getHaveUsagePermission()){
                        binding.loading.visibility=View.VISIBLE
                        binding.myChart.visibility=View.GONE
                        binding.chartDescription.visibility=View.GONE
                        fillChartWithData(withContext(Dispatchers.IO) {
                            activity.createDailyUsageStats()
                        }
                        )
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.chartDescription.visibility=View.GONE
                            binding.myChart.visibility=View.GONE
                            binding.none.apply {
                                visibility = View.VISIBLE
                                text = resources.getString(R.string.give_permission_to_display_stats)
                                setPadding(0,10,0,20)
                                setOnClickListener {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        val appOpsManager: AppOpsManager =
                                            activity.getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager
                                        val mode = appOpsManager.unsafeCheckOpNoThrow(
                                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                                            Process.myUid(),
                                            activity.packageName
                                        )
                                        if (mode != AppOpsManager.MODE_ALLOWED) {
                                            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                        } else {                                                                    //only legend displayed but everytime
                                            activity.setHaveUsagePermission(true)
                                            withContext(Dispatchers.Main){
                                                // binding.chartDescription.visibility=View.VISIBLE
                                                // binding.myChart.visibility=View.VISIBLE
                                                binding.loading.visibility=View.VISIBLE
                                                it.visibility=View.GONE
                                            }
                                            fillChartWithData(withContext(Dispatchers.IO) {
                                                activity.createDailyUsageStats()
                                            }
                                            )

                                        }

                                    }
                                }
                            }
                        }
                    }
                }

            }
        }else
            binding.chartFragment.visibility=View.GONE

    }

    fun fillChartWithData(tab:ArrayList<Pair<String,Long>>) {
        chartMap = TreeMap()
        legendMap = TreeMap()
        //   var sum = 0
        tab.forEach {
            println("EL$it")
        }
        if (tab.isEmpty())
            binding.chartFragment.visibility = View.GONE
        else {
            tab.sortByDescending { it.second }
            // val table = tab.subList(0, 10)

            for (row in tab) {
                val hours = row.second / 3600_000f
                if (hours > 0.0) {
                    val rnd = Random()
                    val color = String.format(
                        "#%06X",
                        0xFFFFFF and Color.argb(
                            255,
                            rnd.nextInt(256),
                            rnd.nextInt(256),
                            rnd.nextInt(256)
                        )
                    )

                    chartMap.putIfAbsent(row.first, Pair(Color.parseColor(color), hours))?.let {
                        val oldValue=chartMap[row.first]!!.second
                        chartMap.replace(row.first,Pair(it.first,it.second+oldValue))
                    }
                    legendMap.putIfAbsent(row.first,Pair(color, hours))?.let {
                        val oldValue=legendMap[row.first]!!.second
                        legendMap.replace(row.first,Pair(it.first,it.second+oldValue))

                    }
                }
            }

            val aim = -1
            MainScope().launch{
                try {
                    binding.loading.visibility=View.GONE
                    if (chartMap.values.isEmpty() && legendMap.isEmpty()) {
                        binding.chartDescription.visibility = View.GONE
                        binding.myChart.visibility = View.GONE
                    } else {
                        binding.none.visibility = View.GONE
                        binding.chartDescription.visibility = View.VISIBLE
                        binding.myChart.visibility = View.VISIBLE


                        binding.myChart.fillData(
                            chartMap.values.toList().sortedByDescending { it.second }.subList(0,minOf(chartMap.size,5)),
                            aim,
                            fillColorDefault = Color.LTGRAY
                        )
                        val legendList= legendMap.toList().sortedByDescending { it.second.second }.subList(0,minOf(chartMap.size,5)).map { Pair<String,String>(it.first,it.second.first) }
                        binding.chartDescription.adapter =
                            LinearChartAdapter(legendList)
                        binding.chartDescription.layoutManager = LinearLayoutManager(context)
                    }
                }catch (e:Exception){
                    Firebase.crashlytics.recordException(e)
                }
            }
        }
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