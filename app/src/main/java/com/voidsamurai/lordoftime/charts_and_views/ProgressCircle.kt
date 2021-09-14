package com.voidsamurai.lordoftime.charts_and_views

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.minus
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R

class ProgressCircle(context: Context?, attributeSet: AttributeSet) : View(context,attributeSet) {

    var canvas: Canvas?=null
    private var timeCurrent:Float=8f
    private var timeAim:Float=10f
    private var side:Int=0

    fun fillData( timeCurrent:Float, timeAim:Float){
        this.timeCurrent=timeCurrent
        this.timeAim=timeAim
        postInvalidate()
    }

    fun drawStats(){
        val paint= Paint()
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=40f

        var path= Path()

        val rect = RectF((width / 2 - side / 2).toFloat(),
            (height / 2 - side / 2).toFloat(),
            (width / 2 + side / 2).toFloat(),
            (height / 2 + side / 2).toFloat())


        path.addArc(rect,0f, 360f)
        paint.color= Color.LTGRAY
        canvas!!.drawPath(path,paint)
        path.reset()
        path.addArc(rect,-90f,((timeCurrent/timeAim)*360))
        paint.color=resources.getColor(R.color.working_progress_circle,null)
        canvas!!.drawPath(path,paint)

    }

    override fun onDraw(canva: Canvas?) {
        super.onDraw(canva)
        side = if (width > height) {
            height * 8 / 10
        } else {
            width * 8 / 10
        }
        canva?.let {
            this.canvas=canva
            drawStats()
        }
    }

}