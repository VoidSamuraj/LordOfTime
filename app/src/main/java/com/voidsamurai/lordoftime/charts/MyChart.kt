package com.voidsamurai.lordoftime.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


class MyChart(context: Context?, attributeSet: AttributeSet ):
    View(context,attributeSet) {

    fun fillData(
        data: List<Pair<Int, Float>>?,
        maxVal: Int? = null,
        fillColorDefaultt: Int? = null,
        spaceBetween: Int? = null
    ) {
        this.data=data
        spaceBetween?.let { this.spaceBetween=it}
        maxVal?.let {this.maxVal=it}
        fillColorDefaultt?.let {this.fillColorDefaultt=it}
        canvas?.let { drawPie(it)}
        forceLayout()
    }

    private var canvas:Canvas?=null
    private var data:List<Pair<Int, Float>>? = null
    private var spaceBetween:Int=0
    private var maxVal:Int?=null
    private var fillColorDefaultt:Int?=null
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
                drawArc1(canvas,0.0f,360.0f,fillColorDefaultt?:Color.WHITE)
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
            paint.color = Color.WHITE
            canvas.drawCircle(width/2.0f,height/2.0f,80f,Paint(paint))
            paint.setShadowLayer(0f,0f,0f,Color.BLACK)
            paint.color = Color.BLACK
            paint.textSize=30f
            val text:String=String.format("%02d",duration.toInt())+":"+String.format("%02d",(duration%1*60).toInt())+"h"
            canvas.drawText(text,width/2f , height/2.0f- ((paint.descent() + paint.ascent()) / 2),paint)

        }

    }
    private fun drawArc1(canvas: Canvas,startAngle:Float,sweepAngle:Float,color: Int,duration:Float=0.0f){

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
            canvas.drawLine(xc, yc, x, y, paint)

            val text:String=String.format("%02d",duration.toInt())+":"+String.format("%02d",(duration%1*60).toInt())

            val len=20f
            paint.textSize=30f
            val lenText=paint.measureText(text)

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
    private fun drawArc(canvas: Canvas?,startAngle:Float,sweepAngle:Float,color: Int){
        val skok=10
        val move:Pair<Float,Float> = when(val mid=startAngle+(sweepAngle/2)){
            in 0.0f..90.0f ->Pair((1-(mid/90))*skok,mid/90*skok)
            in 90.0f..180.0f ->Pair(-(mid%90)/90*skok, (1-((mid%90)/90))*skok)
            in 180.0f..270.0f ->Pair(-(1-((mid%90)/90))*skok,-(mid%90)/90*skok)
            in 270.0f..360.0f ->Pair((mid%90)/90*skok,- (1-((mid%90)/90))*skok)
            else -> Pair(0.0f,0.0f)
        }
        paint.style=Paint.Style.FILL
        paint.color = color
        canvas!!.drawArc(
            (width / 2 - side / 2).toFloat()+move.first,
            (height / 2 - side / 2).toFloat()+move.second,
            (width / 2 + side / 2).toFloat()+move.first,
            (height / 2 + side / 2).toFloat()+move.second,
            startAngle,
            sweepAngle,
            true,
            paint
        )
    }

}