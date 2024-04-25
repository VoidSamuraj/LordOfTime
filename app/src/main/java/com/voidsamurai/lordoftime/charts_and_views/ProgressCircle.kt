package com.voidsamurai.lordoftime.charts_and_views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.voidsamurai.lordoftime.R
import android.graphics.Shader
import android.os.Build

/**
 * Chart to display time spend on task by percentage
 */
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


    private fun drawStats(){
        val paint= Paint()
        paint.style=Paint.Style.STROKE

        val path= Path()

        val rect = RectF((width / 2 - side / 2).toFloat(),
            (height / 2 - side / 2).toFloat(),
            (width / 2 + side / 2).toFloat(),
            (height / 2 + side / 2).toFloat())

        fun drawShadow(){
            paint.strokeWidth=30f
            path.addArc(rect, 0f, 360f)
            paint.color = Color.LTGRAY
            paint.setShadowLayer(10f,0f,0f,Color.BLACK)
            canvas!!.drawPath(path, paint)
            paint.setShadowLayer(0f,0f,0f,Color.BLACK)
            paint.strokeWidth=31f
            path.reset()
        }

        if(timeCurrent<timeAim) {
            drawShadow()
            path.addArc(rect, -90f, ((timeCurrent / timeAim) * 360))
            paint.color = resources.getColor(R.color.working_progress_circle, null)

            canvas!!.drawPath(path, paint)

        }else{
            drawShadow()
            path.addArc(rect, 0f, 360f)
            val oldshader=paint.shader
            val arrayHex= arrayOf("#BF953F", "#FCF6BA", "#B38728", "#FBF5B7", "#AA771C")
            val arrayInt =IntArray(arrayHex.size){ Color.parseColor(arrayHex[it])}
            val shader: Shader? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                LinearGradient(0f,
                    0f,
                    rect.width(),
                    rect.height(),
                    arrayInt,
                    null,
                    Shader.TileMode.MIRROR)
            } else {
                paint.color=Color.parseColor("#BF953F")
                oldshader
            }

            shader?.let {paint.shader = it}
            canvas!!.drawPath(path, paint)
            oldshader?.let {paint.shader = it}


        }

    }
    override fun onDraw(canva: Canvas?) {
        super.onDraw(canva)
        side = if (width > height) {
            height * 6 / 10
        } else {
            width * 6 / 10
        }
        canva?.let {
            this.canvas=canva
            drawStats()
        }
    }

}