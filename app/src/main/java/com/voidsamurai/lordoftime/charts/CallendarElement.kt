package com.voidsamurai.lordoftime.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CallendarElement(context: Context?, attributeSet: AttributeSet):
    View(context,attributeSet) {

    private var canvas:Canvas?=null
    private var date:Int=0
    private var duration:Float=0.0f
    private var color:Boolean=true
    private var paint: Paint = Paint()
    private var side:Int=0
    private var scale:Int=24

    fun fillData(
        date:Int,
        duration:Float,
        currentMonth:Boolean,
        scale:Int?
    ) {
        this.date=date
        this.duration=duration
        this.color=currentMonth
        canvas?.let {
            drawElement(canvas!!) }
        forceLayout()
        scale?.let { this.scale=it }
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas=canvas

        side = if (width > height) {
            height * 8 / 10
        } else {
            width * 8 / 10
        }
        canvas?.let {drawElement(canvas) }
    }
    private fun drawElement(canvas: Canvas){
        paint.setShadowLayer(15f,0f,0f,Color.BLACK)
        if(color)
            drawArc(canvas,0.0f,360.0f,Color.WHITE)
        else
            drawArc(canvas,0.0f,360.0f,Color.LTGRAY)

        paint.setShadowLayer(0f,0f,0f,Color.BLACK)
        paint.style= Paint.Style.FILL
        if(color)
            drawArc(canvas,0f,duration*360/scale,Color.GREEN)
        else
            drawArc(canvas,0f,duration*360/scale,Color.GRAY)
        if(color)
            paint.color=Color.WHITE
        else
            paint.color=Color.LTGRAY
        canvas.drawCircle(width/2.0f,height/2.0f,side/4f,Paint(paint))
        paint.textAlign=Paint.Align.CENTER
        paint.color = Color.BLACK
        paint.textSize=30f
        canvas.drawText(date.toString(),width/2f , height/2.0f- ((paint.descent() + paint.ascent()) / 2),paint)


    }
    private fun drawArc(canvas: Canvas,startAngle:Float,sweepAngle:Float,color: Int){
        paint.style= Paint.Style.FILL
        paint.color = color
        canvas.drawArc(
            (width / 2 - side / 2).toFloat(),
            (height / 2 - side / 2).toFloat(),
            (width / 2 + side / 2).toFloat(),
            (height / 2 + side / 2).toFloat(),
            startAngle,
            sweepAngle,
            true,
            paint
        )
        paint.color = Color.BLACK
        paint.strokeWidth=3f
    }
}