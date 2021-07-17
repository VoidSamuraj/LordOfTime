package com.voidsamurai.lordoftime.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.fragments.adapters.EditAdapter
import kotlinx.android.synthetic.main.fragment_changer_list.*
import layout.DataRowWithColor


class EditList : Fragment() {
    private lateinit var editTasksRecycleView: RecyclerView

    companion object{
        private lateinit var editAdapter: EditAdapter
        fun update()=editAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_changer_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTasksRecycleView=edit_tasks_recycleView
        fillEditList(MainActivity.getQueryArrayByPriority())

        add_record_button.setOnClickListener{

            it?.findNavController()!!.navigate(R.id.action_listChanger_to_editTask)

        }
    }

    private fun fillEditList(query:ArrayList<DataRowWithColor>){
        editAdapter= EditAdapter(query)
        editTasksRecycleView.adapter=editAdapter
        editTasksRecycleView.layoutManager=LinearLayoutManager(context)
        editTasksRecycleView.smoothScrollToPosition(editTasksRecycleView.adapter!!.itemCount)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setBackgroundDrawable(ColorDrawable(Color.WHITE))

    }
}