package com.voidsamurai.lordoftime.charts_and_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import com.voidsamurai.lordoftime.R

class CalendarScrollEdit(context: Context?, attributeSet: AttributeSet):
    View(context,attributeSet){
    private var canvas:Canvas?=null


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas=canvas
        minimumHeight=4800
        drawLines()
    }
    fun drawLines(){
        val paint=Paint()
        paint.color=resources.getColor(R.color.text,null)
        paint.textSize=22f
        val scaleL=height/24f
        val scales=scaleL/3f
        for(i in 1..24){
            canvas!!.drawText((i-1).toString(),15f,i*scaleL-(scaleL/2),paint)
            paint.strokeWidth=5f
            canvas!!.drawLine(0f,i*scaleL,width.toFloat(),i*scaleL,paint)
            paint.strokeWidth=2f
            for(j in 1..2) {
                canvas!!.drawLine(
                    60f,
                    i * scaleL - (j  *scales),
                    width.toFloat(),
                    i * scaleL - (j  * scales),
                    paint
                )
            }
        }
        paint.strokeWidth=5f
        canvas!!.drawLine(
            60f,
            0f,
            60f,
            height.toFloat(),
            paint
        )
    }
}