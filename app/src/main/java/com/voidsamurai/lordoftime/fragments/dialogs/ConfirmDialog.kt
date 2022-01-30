package com.voidsamurai.lordoftime.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.voidsamurai.lordoftime.R

class ConfirmDialog(private var what_to_delete:String,private var no:()->Unit,private var yes:()->Unit ):AppCompatDialogFragment() {

    private lateinit var contentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        contentView= inflater.inflate(R.layout.dialog_confirm, null)
        contentView.findViewById<TextView>(R.id.operation_description).text=resources.getText(R.string.confirm_delete_dialog_text).toString()+" "+what_to_delete
        contentView.findViewById<Button>(R.id.yes).setOnClickListener {
            yes()
            dismiss()
        }
        contentView.findViewById<Button>(R.id.no).setOnClickListener {
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