package com.voidsamurai.lordoftime.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentManyChartsBinding

class ManyChartsFragment : Fragment() {

    private var _binding: FragmentManyChartsBinding?=null
    private val binding get()=_binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.move_fade)

    }

    override fun onStart() {
        super.onStart()

        AnimationUtils.loadAnimation(context,R.anim.fade_in).also { animation ->
            binding.chart2.startAnimation(animation)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentManyChartsBinding.inflate(inflater, container, false)
        return binding.root
    }


}