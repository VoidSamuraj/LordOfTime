package com.voidsamurai.lordoftime.fragments.dialogs
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.RutinesRow
import com.voidsamurai.lordoftime.fragments.EditTaskSelected
import com.voidsamurai.lordoftime.fragments.adapters.RepeatAdapter

class RepeatDialog(
    private val taskId:Int,
    private val parentDialog:Fragment?
) : DialogFragment() {
    private var data:List<RutinesRow>?=null
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: View
    private lateinit var edit:Button
    private lateinit var add:Button
    private lateinit var delete:Button
    private lateinit var save:Button
    companion object {
        var lastPosition:Int?=null
        var selectedId: Int? = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        contentView= inflater.inflate(R.layout.dialog_repeat, null)
        recyclerView=contentView.findViewById(R.id.recyclerView)
        lastPosition =null
        selectedId =null
        getData(taskId)
        edit=contentView.findViewById(R.id.edit)
        delete=contentView.findViewById(R.id.delete)
        save=contentView.findViewById(R.id.save)
        add=contentView.findViewById(R.id.add)



        edit.setOnClickListener {
            if(selectedId !=null){
            val dialog= RutinesElement(taskId,1, selectedId)
            dialog.show(requireActivity().supportFragmentManager,"Rutines element")
            }
        }
        add.setOnClickListener {
            val dialog= RutinesElement(taskId,0)
            dialog.show(requireActivity().supportFragmentManager,"Rutines element")
        }
        delete.setOnClickListener {
            if(selectedId !=null) {
                (activity as MainActivity).getDBOpenHelper().deleteRutinesRow(selectedId!!)
                (activity as MainActivity).rutines.delete(selectedId!!)
                lastPosition?.let { notifyItemDeleted(it)}
            }
        }
        save.setOnClickListener {
            if(parentDialog is EditTaskDialog)
                parentDialog.isRutineChecked.value =recyclerView.size>0
            if(parentDialog is EditTaskSelected)
                parentDialog.isRutineChecked.value =recyclerView.size>0

            dismiss()
        }


        builder.setView(contentView)
        super.onCreateDialog(savedInstanceState)
        return builder.create()
    }
    private fun getData(taskId: Int){
        data=(activity as MainActivity).getDBOpenHelper().getRutinesArray(taskId)
        data?.let {
            data=it.filter {rutinesRow: RutinesRow -> rutinesRow.task_id==taskId }
            recyclerView.adapter=RepeatAdapter(data!!.toList())
            recyclerView.layoutManager=LinearLayoutManager(requireContext())
        }
    }


    fun notifyItemChanged(){
        getData(taskId)
        lastPosition?.let {   recyclerView.adapter!!.notifyItemChanged(it)}}
    fun notifyItemDeleted(position:Int){
        getData(taskId)
        recyclerView.adapter!!.notifyItemRemoved(position)
    }
    fun notifyItemInserted(){
        getData(taskId)
        recyclerView.adapter!!.notifyItemInserted(recyclerView.adapter!!.itemCount)
    }
}