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
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentChangerListBinding
import com.voidsamurai.lordoftime.fragments.adapters.EditAdapter
import layout.DataRowWithColor


class EditList : Fragment() {

    companion object{
        private lateinit var editAdapter: EditAdapter
        fun update()=editAdapter.notifyDataSetChanged()
    }


    private var _binding: FragmentChangerListBinding?=null
    private val binding get()=_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=FragmentChangerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillEditList(MainActivity.getQueryArrayByPriority())

        binding.addRecordButton.setOnClickListener{

            it?.findNavController()!!.navigate(R.id.action_listChanger_to_editTask)

        }
    }

    private fun fillEditList(query:ArrayList<DataRowWithColor>){
        editAdapter= EditAdapter(query)
        binding.editTasksRecycleView.adapter=editAdapter
        binding.editTasksRecycleView.layoutManager=LinearLayoutManager(context)
        binding.editTasksRecycleView.smoothScrollToPosition(binding.editTasksRecycleView.adapter!!.itemCount)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setBackgroundDrawable(ColorDrawable(Color.WHITE))

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}