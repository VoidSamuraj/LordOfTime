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
import com.voidsamurai.lordoftime.fragments.adapters.ColorsAdapter
import kotlinx.android.synthetic.main.fragment_colors_list.*


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

    private lateinit var data:Map<String,String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_colors_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        data=MainActivity.getColors()
        colorList=colors_list
        cntx=context
        activit=requireActivity()

        fillEditList(data)
        color_fab.setOnClickListener{
            showDialog()
        }

    }



    private fun showDialog(){

        val ft =
            ColorDialogFragment(R.layout.fragment_edit_color_category, ColorDialogFragment.SAVE)
        ft.show(requireActivity().supportFragmentManager,"Kolor")
    }
}