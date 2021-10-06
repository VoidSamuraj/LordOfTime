package com.voidsamurai.lordoftime.charts_and_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.voidsamurai.lordoftime.R
import kotlin.math.cos
import kotlin.math.sin


class MyChart(context: Context?, attributeSet: AttributeSet ):
    View(context,attributeSet) {

    fun fillData(
        data: List<Pair<Int, Float>>?,
        maxVal: Int? = null,
        fillColorDefault: Int? = null,
        spaceBetween: Int? = null
    ) {
        this.data=data
        spaceBetween?.let { this.spaceBetween=it}
        maxVal?.let {this.maxVal=it}
        fillColorDefault?.let {this.fillColorDefault=it}
        canvas?.let { drawPie(it)}
        forceLayout()
    }

    private var canvas:Canvas?=null
    private var data:List<Pair<Int, Float>>? = null
    private var spaceBetween:Int=0
    private var maxVal:Int?=null
    private var fillColorDefault:Int?=null
    private var paint:Paint= Paint()
    private var side:Int =0
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas=canvas

        side = if (width > height)
            height * 8 / 10
         else
            width * 8 / 10

        drawPie(canvas)
    }

    private fun drawPie(canvas: Canvas?){

        fun fillRound(scale:Float):Float{
            var startAngle =0.0f
            var dur =0.0f
            var width:Float
            for (item in data!!){
                dur+=item.second
                width=scale*item.second
                drawArc1(canvas!!,startAngle,width,item.first,item.second)
                startAngle+=width
            }
            return dur
        }

        paint.style=Paint.Style.FILL
        val duration:Float
        if(data!=null&&canvas!=null){
            if(maxVal!=null){
                paint.setShadowLayer(15f,0f,0f,Color.BLACK)
                drawArc1(canvas,0.0f,360.0f,fillColorDefault?:Color.WHITE)
                paint.setShadowLayer(0f,0f,0f,Color.BLACK)
                val scale:Float = 360.0f/ maxVal!!
                duration=fillRound(scale)

            }else{

                var dur =0.0f
                for (item in data!!)
                    dur += item.second

                val scale:Float = 360.0f/dur

                duration=fillRound(scale)
            }

            paint.setShadowLayer(8f,0f,0f,Color.BLACK)
            paint.textAlign=Paint.Align.CENTER
            paint.style= Paint.Style.FILL
            paint.color = Color.WHITE //resources.getColor(R.color.background,null)
            canvas.drawCircle(width/2.0f,height/2.0f,80f,Paint(paint))
            paint.setShadowLayer(0f,0f,0f,Color.BLACK)
            paint.color = Color.BLACK
            paint.textSize=30f
            val text:String=String.format("%02d",duration.toInt())+":"+String.format("%02d",(duration%1*60).toInt())+"h"
            canvas.drawText(text,width/2f , height/2.0f- ((paint.descent() + paint.ascent()) / 2),paint)

        }

    }
    private fun drawArc1(canvas: Canvas,startAngle:Float,sweepAngle:Float,color: Int,duration:Float=0.0f){

        /*
        *
        paint.style= Paint.Style.STROKE
        paint.color = color
        val rect:RectF =RectF( (width / 2 - side / 3).toFloat(),
            (height / 2 - side / 3).toFloat(),
            (width / 2 + side / 3).toFloat(),
            (height / 2 + side / 3).toFloat())
        val path=Path()
        paint.strokeWidth=100f
        path.addArc(rect, startAngle, sweepAngle)
        canvas.drawPath(path,paint)

        * */
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

        if(duration>=1.5) {
            val mid = startAngle + (sweepAngle / 2)
            val x: Float =
                (width / 2) + cos(Math.toRadians(mid.toDouble())).toFloat() * (side / 1.8f)
            val y: Float =
                (height / 2) + sin(Math.toRadians(mid.toDouble())).toFloat() * (side / 1.8f)
            val xc: Float =
                (width / 2) + cos(Math.toRadians(mid.toDouble())).toFloat() * (side / 2f)
            val yc: Float =
                (height / 2) + sin(Math.toRadians(mid.toDouble())).toFloat() * (side / 2f)
            paint.color=resources.getColor(R.color.text,null)

            canvas.drawLine(xc, yc, x, y, paint)

            val text:String=String.format("%02d",duration.toInt())+":"+String.format("%02d",(duration%1*60).toInt())

            val len=20f
            paint.textSize=30f
            val lenText=paint.measureText(text)
            paint.color=resources.getColor(R.color.text,null)
            paint.textAlign=Paint.Align.LEFT

            fun drawChartDesc(lineEnd:Float,textMove:Float=0.0f){
                canvas.drawLine(x,y,lineEnd,y,paint)
                canvas.drawText(text,lineEnd+textMove,y,paint)
            }

            val xt:Float
            when(mid){
                in 0.0f..90.0f ->{xt=x+len
                    drawChartDesc(xt)
                }
                in 90.0f..270.0f ->{xt=x-len
                    drawChartDesc(xt,-lenText)
                }
                in 270.0f..360.0f ->{xt=x+len
                    drawChartDesc(xt)
                }
                else ->{}
            }

        }
    }
   

}