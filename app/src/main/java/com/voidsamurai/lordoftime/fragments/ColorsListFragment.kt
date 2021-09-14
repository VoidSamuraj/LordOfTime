package com.voidsamurai.lordoftime.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentColorsListBinding
import com.voidsamurai.lordoftime.fragments.adapters.ColorsAdapter


class ColorsListFragment : Fragment() {


    private lateinit var colorAdapter: ColorsAdapter
    private lateinit var colorList: RecyclerView

    private fun fillEditList(query:Map<String,String>){
        colorAdapter = ColorsAdapter(requireActivity(),query.toList())
        colorList.adapter= colorAdapter
        colorList.layoutManager= LinearLayoutManager(context)
        colorList.smoothScrollToPosition(colorList.adapter!!.itemCount)
    }

    private var _binding:FragmentColorsListBinding?=null
    private val binding get()=_binding!!

    private var data:MutableLiveData<Map<String,String>> = MutableLiveData()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentColorsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        data=(activity as MainActivity).getColors()
        colorList=binding.colorsList
        (activity as MainActivity).getColors().observe(viewLifecycleOwner,{
            fillEditList(it)
        })

        binding.colorFab.setOnClickListener{
            showDialog()
        }

    }
    override fun onDestroyView() {
        (activity as MainActivity).getColors().removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null

    }

    private fun showDialog(){

        val ft =
            ColorDialogFragment(R.layout.dialog_fragment_edit_color_category,ColorDialogFragment.SAVE)
        ft.show(requireActivity().supportFragmentManager,"Kolor")
    }
}