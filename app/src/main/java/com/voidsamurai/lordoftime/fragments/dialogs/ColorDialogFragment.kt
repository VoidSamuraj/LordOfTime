package com.voidsamurai.lordoftime.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.slider.LightnessSlider
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DAOColors


class ColorDialogFragment(
    private var LayoutId: Int,
    private var dialogType: Int,
    private var oldCategory: String? = null,
    private var oldColor: String? = null
) : AppCompatDialogFragment() {

    companion object{
        const val SAVE=1
        const val EDIT=2
    }

    private var colorchanged:Boolean=false
    private var newColor: Int? = null
    private lateinit var contentView: View
    private lateinit var colorPicker:ColorPickerView
    private lateinit var lightSlider:LightnessSlider

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val colors: DAOColors = (activity as MainActivity).colors
        super.onCreateDialog(savedInstanceState)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        contentView= inflater.inflate(LayoutId, null)
        oldCategory?.let{ contentView.findViewById<TextView>(R.id.category_name).text=it}


        colorPicker=contentView.findViewById(R.id.color_picker_view)
        colorPicker.setInitialColor(Color.WHITE,false)
        lightSlider=contentView.findViewById(R.id.v_lightness_slider)
        builder.setView(contentView)
            .setNegativeButton(resources.getText(R.string.cancel)) { _, _ ->

            }

        contentView.findViewById<ImageButton>(R.id.delete_color).setOnClickListener {
            val text=getName()
            ConfirmDialog(text,{},{
                val state=(activity as MainActivity).getDBOpenHelper().deleteColorRow(getName(),(activity as MainActivity).userId)
                when (state) {
                    -1 -> {//category is currently used
                        Toast.makeText(context,resources.getText(R.string.category_in_use),Toast.LENGTH_SHORT).show()
                    }
                    0 -> {//no row found
                        Toast.makeText(context,resources.getText(R.string.database_processing_error),Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        colors.delete(text)
                        update()

                    }
                }
                dismiss()
            },{}).show(parentFragmentManager,"Confirm")



        }
        if(dialogType==1){

            colorPicker.addOnColorSelectedListener {

                setColorToImageView(contentView, R.id.last_color, R.drawable.ic_circle_l, it)
                setColorToImageView(contentView, R.id.new_color, R.drawable.ic_circle_r, it)
                colorchanged=true
            }
            lightSlider.setOnValueChangedListener {
                setColorToImageView(contentView, R.id.last_color, R.drawable.ic_circle_l, colorPicker.selectedColor)
                setColorToImageView(contentView, R.id.new_color, R.drawable.ic_circle_r, colorPicker.selectedColor)
                colorchanged=true

            }
            builder.setPositiveButton(resources.getText(R.string.save)) { _, _ -> }
        }
        else if(dialogType==2){
            newColor=null
            colorPicker.addOnColorSelectedListener {
                newColor=it
                setColorToImageView(contentView, R.id.new_color, R.drawable.ic_circle_r, it)
                colorchanged=true
            }
            lightSlider.setOnValueChangedListener {
                setColorToImageView(contentView, R.id.new_color, R.drawable.ic_circle_r, colorPicker.selectedColor)
                colorchanged=true
            }
            builder.setPositiveButton(resources.getText(R.string.save)) { _, _ -> }

        }
        val build=builder.create()
            build.setOnShowListener {
            build.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { view->
                if(dialogType==1){
                    if (getName().isEmpty() || (!colorchanged)){
                        if (getName().isEmpty())
                            showNotification(resources.getString(R.string.insert_name))
                        if(!colorchanged)
                            showNotification(resources.getString(R.string.select_color))
                    }else {
                        (activity as MainActivity).getDBOpenHelper()
                            .addColorRow(getName(), getColor(), (activity as MainActivity).userId)
                        colors.add(getName(), getColor())
                        update()
                        it.dismiss()
                    }
                }else{
                    if (getName()==oldCategory && (!colorchanged)){
                        showNotification(resources.getString(R.string.change_name_or_color))
                    }else {
                        val color=if(colorchanged)getColor()else oldColor
                        val ret=(activity as MainActivity).getDBOpenHelper()
                            .editColorRow(oldCategory, getName(), color,(activity as MainActivity).userId)
                        if(ret!=-1){
                        colors.add(getName(),getColor())
                        update()
                        it.dismiss()
                        }else{
                            showNotification(resources.getString(R.string.category_exist))
                        }
                    }
                }

            }
        }
        return build
    }

    override fun onResume() {
        super.onResume()
        if(dialogType==1) {

            setColorToImageView(contentView, R.id.last_color, R.drawable.ic_circle_l, Color.WHITE)
            setColorToImageView(
                contentView,
                R.id.new_color,
                R.drawable.ic_circle_r,
                Color.WHITE
            )
        }
        oldColor?.let{
            if(dialogType==2){
                setColorToImageView(contentView, R.id.last_color, R.drawable.ic_circle_l, parseColor(oldColor))
                setColorToImageView(contentView, R.id.new_color, R.drawable.ic_circle_r, parseColor(oldColor))
            }
        }

    }

    private fun getName():String= contentView.findViewById<TextView>(R.id.category_name).text.toString()
    private fun getColor():String=String.format(
        "#%06X", 0xFFFFFF and
                colorPicker.selectedColor)

    private fun update(){
        (context as MainActivity).getDataFromDB()
    }
    private fun showNotification(text:String){
        Toast.makeText(context, text,
            Toast.LENGTH_SHORT).show()
    }

    private fun getColoredCircle(id: Int, color: Int):Drawable{
        val unwrappedDrawable = AppCompatResources.getDrawable(requireContext(), id)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }
    private fun parseColor(oldColor: String?):Int{
        val color = Color.parseColor(oldColor)
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color shr 0 and 0xFF
        return Color.argb(255,r,g,b)
    }
    private fun setColorToImageView(view:View,elementId:Int,drawableId:Int,color:Int){
        view.findViewById<ImageView>(elementId).setImageDrawable(
            getColoredCircle(
                drawableId,
                color
            )
        )
    }
}