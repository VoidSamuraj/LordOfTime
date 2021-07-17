package com.voidsamurai.lordoftime.fragments


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment

import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R

class TasksEditHostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks_edit_host, container, false)
    }


    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.run {
            Handler(Looper.getMainLooper()).postDelayed({
            }, 400)
        }
    }




}