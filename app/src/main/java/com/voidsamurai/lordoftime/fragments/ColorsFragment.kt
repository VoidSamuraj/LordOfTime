package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentColorsListBinding
import com.voidsamurai.lordoftime.fragments.adapters.ColorsAdapter


class ColorsFragment : Fragment() {

    companion object{
        private lateinit var colorAdapter: ColorsAdapter
        private lateinit var colorList: RecyclerView
        @SuppressLint("StaticFieldLeak")
        private  var cntx: Context?=null
        private lateinit var activit: FragmentActivity

        fun fillEditList(querry:Map<String,String>){
            colorAdapter = ColorsAdapter(querry.toList())
            colorList.adapter= colorAdapter
            colorList.layoutManager= LinearLayoutManager(cntx)
            colorList.smoothScrollToPosition(colorList.adapter!!.itemCount)
        }
        fun getActicity()= activit
    }
    private var _binding:FragmentColorsListBinding?=null
    private val binding get()=_binding!!

    private lateinit var data:Map<String,String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentColorsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        data=MainActivity.getColors()
        colorList=binding.colorsList
        cntx=context
        activit=requireActivity()

        fillEditList(data)
        binding.colorFab.setOnClickListener{
            showDialog()
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }


    private fun showDialog(){

        val ft =
            ColorDialogFragment(R.layout.fragment_edit_color_category, ColorDialogFragment.SAVE)
        ft.show(requireActivity().supportFragmentManager,"Kolor")
    }
}