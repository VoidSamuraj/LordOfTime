package com.voidsamurai.lordoftime.fragments

import android.os.Build
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

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            binding.usageCardView.visibility=View.INVISIBLE
        super.onViewCreated(view, savedInstanceState)
    }
}