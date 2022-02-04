package com.voidsamurai.lordoftime.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import com.voidsamurai.lordoftime.R

class ConfirmDialog(private var itemName:String, private var no:()->Unit, private var yes:()->Unit, private var dontShowPreferences: ()->Unit, private var showCheckBox:Boolean=false):AppCompatDialogFragment() {

    private lateinit var contentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        contentView= inflater.inflate(R.layout.dialog_confirm, null)

        if(showCheckBox){

            contentView.findViewById<CheckBox>(R.id.checkBox).visibility=View.VISIBLE
            contentView.findViewById<TextView>(R.id.operation_description).text=resources.getText(R.string.witchout_permission).toString()
            contentView.findViewById<Button>(R.id.yes).text=resources.getText(R.string.try_again).toString()
            contentView.findViewById<Button>(R.id.no).text=resources.getText(R.string.cancel).toString()

        }else
            contentView.findViewById<TextView>(R.id.operation_description).text=resources.getText(R.string.confirm_delete_dialog_text).toString()+" "+itemName
        contentView.findViewById<Button>(R.id.yes).setOnClickListener {
            if(showCheckBox)
                dontShowPreferences()
            yes()
            dismiss()
        }
        contentView.findViewById<Button>(R.id.no).setOnClickListener {
            if(showCheckBox)
                dontShowPreferences()
            no()
            dismiss()
        }
        builder.setView(contentView)
           /* .setNegativeButton(resources.getText(R.string.cancel)) { _, _ ->
                cancel()
            }.setPositiveButton(resources.getText(R.string.save)){ _, _ ->
                save()
            }
*/


        return builder.create()
    }

}