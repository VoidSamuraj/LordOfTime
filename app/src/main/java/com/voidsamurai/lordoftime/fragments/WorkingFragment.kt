package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.databinding.FragmentWorkingBinding
import com.voidsamurai.lordoftime.fragments.adapters.StartWorkAdapter
import android.view.*
import com.voidsamurai.lordoftime.R


class WorkingFragment : Fragment() {

    private var _workingFragmentBinding : FragmentWorkingBinding?=null
    val workingFragmentBinding get() =_workingFragmentBinding!!
    private var currentOrder=Order.ASC
    private var currentSortBy=SortBy.DATE
    private enum class Order(order:Int){
        ASC(1),
        DESC(2)
    }
    private enum class SortBy(order:String){
        DATE("R.string.date"),
        PRIORITY("R.string.category")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _workingFragmentBinding= FragmentWorkingBinding.inflate(inflater,container,false)
        return workingFragmentBinding.root
    }


    override fun onStart() {
        super.onStart()
       setListData()
    }
    private fun setListData(){
        (activity as MainActivity).getQueryArrayByDate().removeObservers(viewLifecycleOwner)
        (activity as MainActivity).getQueryArrayByPriority().removeObservers(viewLifecycleOwner)
        if(currentSortBy==SortBy.DATE) {

            if(currentOrder==Order.ASC)
                (activity as MainActivity).getQueryArrayByDate().value!!.sortBy { dataRowWithColor ->dataRowWithColor.date  }
            else
                (activity as MainActivity).getQueryArrayByDate().value!!.sortByDescending { dataRowWithColor ->dataRowWithColor.date  }

            (activity as MainActivity).getQueryArrayByDate().observe(viewLifecycleOwner, {
                workingFragmentBinding.taskList.adapter = StartWorkAdapter(
                    requireActivity() as MainActivity,
                    it,
                    lifecycleOwner = viewLifecycleOwner
                )
                workingFragmentBinding.taskList.layoutManager =
                    LinearLayoutManager(requireContext())

            })
        }else{
            if(currentOrder==Order.ASC)
                (activity as MainActivity).getQueryArrayByPriority().value!!.sortBy { dataRowWithColor ->dataRowWithColor.priority  }
            else
                (activity as MainActivity).getQueryArrayByPriority().value!!.sortByDescending { dataRowWithColor ->dataRowWithColor.priority  }

            (activity as MainActivity).getQueryArrayByPriority().observe(viewLifecycleOwner,{
                workingFragmentBinding.taskList.adapter=StartWorkAdapter(requireActivity() as MainActivity,it,lifecycleOwner = viewLifecycleOwner)
                workingFragmentBinding.taskList.layoutManager=LinearLayoutManager(requireContext())
            })
        }
    }
    private fun sortList(){
        if(currentSortBy==SortBy.DATE) {
            if (currentOrder == Order.ASC)
                (activity as MainActivity).getQueryArrayByDate().value!!.sortBy { dataRowWithColor -> dataRowWithColor.date }
            else
                (activity as MainActivity).getQueryArrayByDate().value!!.sortByDescending { dataRowWithColor -> dataRowWithColor.date }
        }
        else{
            if(currentOrder==Order.ASC)
                (activity as MainActivity).getQueryArrayByPriority().value!!.sortBy { dataRowWithColor ->dataRowWithColor.priority  }
            else
                (activity as MainActivity).getQueryArrayByPriority().value!!.sortByDescending { dataRowWithColor ->dataRowWithColor.priority  }

        }
        }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.working_menu,menu)
        if(currentOrder==Order.ASC)
            menu.findItem(R.id.order).icon=resources.getDrawable(R.drawable.ic_baseline_arrow_drop_up_24,null)
        else
            menu.findItem(R.id.order).icon=resources.getDrawable(R.drawable.ic_baseline_arrow_drop_down_24,null)

        if (currentSortBy==SortBy.DATE)
            menu.findItem(R.id.category).title=resources.getString(when(currentSortBy){
                SortBy.DATE->R.string.date
                else ->R.string.priority
            })

        super.onCreateOptionsMenu(menu, inflater)
    }


    @SuppressLint("NotifyDataSetChanged", "UseCompatLoadingForDrawables")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {



        return when(item.itemId){
           R.id.order->{
               currentOrder = if(currentOrder==Order.ASC) {
                   item.icon=resources.getDrawable(R.drawable.ic_baseline_arrow_drop_down_24,null)
                   Order.DESC
               }else {
                   item.icon=resources.getDrawable(R.drawable.ic_baseline_arrow_drop_up_24,null)
                   Order.ASC
               }
               sortList()
               workingFragmentBinding.taskList.adapter!!.notifyDataSetChanged()

               true }
           R.id.category->{
               currentSortBy = if(currentSortBy==SortBy.PRIORITY)
                   SortBy.DATE
               else
                   SortBy.PRIORITY
               item.title=resources.getString(when(currentSortBy){
                   SortBy.DATE->R.string.date
                    else ->R.string.priority
               })
               setListData()
               true}
           else->super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).getQueryArrayByPriority().removeObservers(viewLifecycleOwner)
        (activity as MainActivity).getQueryArrayByDate().removeObservers(viewLifecycleOwner)
    }
}