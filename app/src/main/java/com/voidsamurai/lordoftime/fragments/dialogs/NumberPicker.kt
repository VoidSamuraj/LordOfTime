package com.voidsamurai.lordoftime.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatDialogFragment
import com.voidsamurai.lordoftime.R

class NumberPicker(private var value: Int,private val onSave:(np:NumberPicker)->Unit)  : AppCompatDialogFragment() {



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        val view= inflater.inflate(R.layout.view_number_picker, null)
        val np:NumberPicker=view.findViewById(R.id.picker)
        np.displayedValues=resources.getStringArray(R.array.hours)
        np.maxValue = resources.getStringArray(R.array.hours).size-1
        np.value=value-1
        np.minValue = 0
        np.wrapSelectorWheel = true
        builder.setView(view)
        builder.setPositiveButton("Zapisz") { _, _ ->
           onSave(np)
        }
        return  builder.create()
    }
}