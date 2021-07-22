package com.voidsamurai.lordoftime.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatDialogFragment
import com.voidsamurai.lordoftime.R
import java.util.*

class NumberPicker  : AppCompatDialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        val vieww= inflater.inflate(R.layout.fragment_number_picker, null)
        var np:NumberPicker=vieww.findViewById(R.id.picker)
        np.displayedValues=resources.getStringArray(R.array.hours)
        np.setMaxValue(resources.getStringArray(R.array.hours).size-1);
        np.value=DateChartFragment.dayAimH-1
        np.setMinValue(0);
        np.setWrapSelectorWheel(true)
        builder.setView(vieww)
        builder.setPositiveButton("Zapisz") { _, _ ->
            DateChartFragment.dayAimH=np.value+1
            DateChartFragment.dayAim.text=(np.value+1).toString()
        }
        return  builder.create()
    }
}