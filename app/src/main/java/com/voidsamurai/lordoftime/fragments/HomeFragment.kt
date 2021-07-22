package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.transition.*
import android.view.*
import android.view.animation.*
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
        return inflater.inflate(R.layout.fragment_home, container, false)                           //
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
          //  findNavController().navigate(R.id.action_FirstFragment_to_manyCharts)

           /* var endFragment=ManyChartsFragment
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setSharedElementReturnTransition(TransitionInflater.from(
                    getActivity()).inflateTransition(android.R.transition.move));
                setExitTransition(
                    TransitionInflater.from(
                    getActivity()).inflateTransition(android.R.transition.fade));

                endFragment.setSharedElementEnterTransition(TransitionInflater.from(
                    getActivity()).inflateTransition(android.R.transition.move));
                endFragment.setEnterTransition(TransitionInflater.from(
                    getActivity()).inflateTransition(android.R.transition.fade));
            }

            var bundle =  Bundle();
            bundle.putString("ACTION", textView.getText().toString());
            bundle.putParcelable("IMAGE", ((BitmapDrawable) imageView.getDrawable()).getBitmap());
            endFragment.setArguments(bundle);
            var fragmentManager = fragmentManager;
            fragmentManager?.beginTransaction()
                ?.replace(R.id.container, endFragment)
                ?.addToBackStack("Payment")
                ?.addSharedElement(staticImage, getString(R.string.fragment_image_trans))
                ?.commit();
        }*/
        }
    }



}