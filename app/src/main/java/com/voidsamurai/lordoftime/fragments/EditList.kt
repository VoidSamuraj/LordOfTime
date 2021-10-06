package com.voidsamurai.lordoftime.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentEditListBinding
import com.voidsamurai.lordoftime.fragments.adapters.EditAdapter
import com.voidsamurai.lordoftime.bd.DataRowWithColor


class EditList : Fragment() {

    private lateinit var editAdapter: EditAdapter
    private var _binding: FragmentEditListBinding?=null
    private val binding get()=_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=FragmentEditListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).getQueryArrayByPriority().observe(viewLifecycleOwner,{
            fillEditList(it)
        })
        binding.addRecordButton.setOnClickListener{

            it?.findNavController()!!.navigate(R.id.action_editList_to_editTaskSelected)

        }
    }

    private fun fillEditList(query:ArrayList<DataRowWithColor>){
        editAdapter= EditAdapter(query)
        binding.editTasksRecycleView.adapter=editAdapter
        binding.editTasksRecycleView.layoutManager=LinearLayoutManager(context)
        binding.editTasksRecycleView.smoothScrollToPosition(binding.editTasksRecycleView.adapter!!.itemCount)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }


    override fun onDestroyView() {
        (activity as MainActivity).getQueryArrayByPriority().removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}