package com.voidsamurai.lordoftime.fragments.adapters


import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.LinearViewHolder
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.charts_and_views.NTuple6
import com.voidsamurai.lordoftime.fragments.CalendarEditFragmentDirections
import java.util.*


class CalendarEditAdapter (private val context: Context, private val dataSet: ArrayList<ArrayList<NTuple6<Calendar,Float,Int, String, String,String>>?>) :

    RecyclerView.Adapter<LinearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_edit_element, parent, false)

        return LinearViewHolder(view)
    }


    override fun onBindViewHolder(holder: LinearViewHolder, position: Int) {
        val dm=DisplayMetrics()
        val activity=( context as Activity)//.windowManager. defaultDisplay.getMetrics(dm)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = activity.display
            display?.getRealMetrics(dm)
        } else {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(dm)
        }

        val pad= context.resources.getDimension(R.dimen.small_padding)
        val x=(dm.widthPixels-2*pad)/7
        val layout=holder.layout
        val calendarLinear=layout.findViewById<LinearLayout>(R.id.calendar_linear)
        calendarLinear.layoutParams.width=x.toInt()
        calendarLinear.layoutParams.height=x.toInt()
        calendarLinear.setOnClickListener {
            val  action:CalendarEditFragmentDirections.ActionCalendarEditFragmentToCalendarDayEdit=
                CalendarEditFragmentDirections.actionCalendarEditFragmentToCalendarDayEdit(dataSet[position]!![0].t1.time.time)
           it.findNavController().navigate(action)
        }


        dataSet.get(position)?.let {
            val date=calendarLinear.findViewById<TextView>(R.id.date)

            if(context.getResources().configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK==Configuration.UI_MODE_NIGHT_YES) {
                date.setShadowLayer(10f,0f,0f,Color.BLACK)
            }
            date.text=it[0].t1.get(Calendar.DAY_OF_MONTH).toString()

            if(it.size>0){
                var dur=0f
                for(r in it){
                    if(r.t2!=null)
                        Log.v("DANE","day:"+r.t1.get(Calendar.DAY_OF_MONTH)+" dzas:"+r.t2+" "+r.t3+" "+r.t4+" "+r.t5+" "+r.t6)
                    dur+= r.t2 ?: 0f
                }
                if(dur>0) {
                    if(dur>18f)
                        dur=18f
                    val color =
                        Color.rgb((255f * dur / 18f).toInt(), (255f - (255f * dur / 18f)).toInt(), 0)
                    Log.v("color",""+dur+" size:"+it.size)
                    date.setBackgroundColor(color)
                }
            }
        }
    }


    override fun getItemCount(): Int= dataSet.size


}