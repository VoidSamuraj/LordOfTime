package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import com.voidsamurai.lordoftime.databinding.FragmentWorkingBinding
import com.voidsamurai.lordoftime.fragments.adapters.StartWorkAdapter
import kotlinx.coroutines.*


class WorkingFragment : Fragment() {

    private var _workingFragmentBinding : FragmentWorkingBinding?=null
    private var fabVisible=true
    val workingFragmentBinding get() =_workingFragmentBinding!!
    private var currentOrder=Order.ASC
    private var currentSortBy=SortBy.DATE
    var isFromOtherFragment = true
    var currentArray:ArrayList<DataRowWithColor> = ArrayList()

    enum class Order(order:Int){
        ASC(1),
        DESC(2)
    }
    enum class SortBy(order:String){
        DATE("R.string.date"),
        PRIORITY("R.string.priority")
    }
    //var deleteIcon:Drawable?=null
    var editIcon:Drawable?=null

    val swipeGesture=object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT/* or  ItemTouchHelper.RIGHT*/){

        val colorSwipeEdit:ColorDrawable= ColorDrawable(Color.parseColor("#00FF00"))

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean =false


        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            when(direction){
                ItemTouchHelper.LEFT->
                    (workingFragmentBinding.taskList.adapter as StartWorkAdapter).editItem(viewHolder.adapterPosition)
            }
        }


        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView=viewHolder.itemView
            fun  hide(cd:ColorDrawable){
                CoroutineScope(Dispatchers.Default).launch {

                    for( i in 255 downTo 0 step 2){
                        MainScope().launch {
                            cd.alpha=i
                        }
                        delay(10)
                    }
                    MainScope().launch {
                        cd.setBounds(0, 0, 0, 0)

                        cd.alpha=255
                    }

                }
            }
            val widthPart=itemView.width/3
            val size =itemView.height/2
            val margin = (itemView.height- size)/2

            if(dX.toInt()==itemView.left){
                hide(colorSwipeEdit)
            }
            else if(dX<0){
                editIcon!!.setBounds(itemView.right-size-10,itemView.top+margin,itemView.right-10,itemView.bottom-margin)
                colorSwipeEdit.setBounds(itemView.right+dX.toInt(),itemView.top,itemView.right,itemView.bottom)
                if(widthPart>=-dX)
                    colorSwipeEdit.alpha= (-dX/widthPart*255).toInt()
                colorSwipeEdit.draw(c)
                c.save()
                c.clipRect(itemView.right+dX.toInt(),itemView.top,itemView.right,itemView.bottom)
                editIcon!!.draw(c)
                c.restore()
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        }



    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _workingFragmentBinding= FragmentWorkingBinding.inflate(inflater,container,false)
        return workingFragmentBinding.root
    }



    override fun onResume() {
        super.onResume()
        isFromOtherFragment = true
        (activity as MainActivity).getDataFromDB()
        val (order,sort)=(activity as MainActivity).getWorkSorting()
        currentOrder= Order.valueOf(order)
        currentSortBy= SortBy.valueOf(sort)
        setListData()
    }



    private fun setListData(){
        (workingFragmentBinding.taskList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        if(currentSortBy==SortBy.DATE) {
            currentArray = (activity as MainActivity).getQueryArrayByDate().value!!.clone() as ArrayList<DataRowWithColor>
            if(currentOrder==Order.ASC)
                currentArray.sortBy { dataRowWithColor -> dataRowWithColor.date }
            else
                currentArray.sortByDescending { dataRowWithColor -> dataRowWithColor.date }

            //   (activity as MainActivity).getQueryArrayByDate().observe(viewLifecycleOwner, {


            //  })
        }else{
            currentArray= (activity as MainActivity).getQueryArrayByPriority().value!!.clone() as ArrayList<DataRowWithColor>
            if(currentOrder==Order.ASC)
                currentArray.sortBy { dataRowWithColor -> dataRowWithColor.priority }
            else
                currentArray.sortByDescending { dataRowWithColor -> dataRowWithColor.priority }

            // (activity as MainActivity).getQueryArrayByPriority().observe(viewLifecycleOwner,{

        }
        workingFragmentBinding.taskList.let{
            it.adapter=StartWorkAdapter(requireActivity() as MainActivity,currentArray,lifecycleOwner = viewLifecycleOwner,this)
            it.layoutManager=LinearLayoutManager(requireContext())
            it.isNestedScrollingEnabled = false
            //    })
        }


        val touchListner=ItemTouchHelper(swipeGesture)
        touchListner.attachToRecyclerView(workingFragmentBinding.taskList)

    }
    private fun sortList(){
        if(currentSortBy==SortBy.DATE) {
            if (currentOrder == Order.ASC)
                currentArray.sortBy { dataRowWithColor -> dataRowWithColor.date }
            else
                currentArray.sortByDescending { dataRowWithColor -> dataRowWithColor.date }
        }
        else{
            if(currentOrder==Order.ASC)
                currentArray.sortBy { dataRowWithColor ->dataRowWithColor.priority  }
            else
                currentArray.sortByDescending { dataRowWithColor ->dataRowWithColor.priority  }

        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.working_menu,menu)
        if(currentOrder==Order.ASC)
            menu.findItem(R.id.order).icon=resources.getDrawable(R.drawable.ic_baseline_arrow_drop_up_24,null)
        else
            menu.findItem(R.id.order).icon=resources.getDrawable(R.drawable.ic_baseline_arrow_drop_down_24,null)

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

                (activity as MainActivity).setSortInWorkFragment(currentOrder,currentSortBy)
                sortList()
                workingFragmentBinding.taskList.adapter!!.notifyDataSetChanged()

                true }
            R.id.category->{
                currentSortBy = if(currentSortBy==SortBy.PRIORITY)
                    SortBy.DATE
                else
                    SortBy.PRIORITY

                (activity as MainActivity).setSortInWorkFragment(currentOrder,currentSortBy)
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
        editIcon=AppCompatResources.getDrawable(requireContext(),R.drawable.ic_edit)

        workingFragmentBinding.add.setOnClickListener {
            it.findNavController().navigate(R.id.action_workingFragment_to_editTaskSelected)
        }
        workingFragmentBinding.taskList.addOnScrollListener(object :RecyclerView.OnScrollListener(){


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                workingFragmentBinding.add.let{
                    if(dy>0&&fabVisible){
                        it.animate().alpha(0f).setDuration(500).withEndAction { it.hide() }. start()
                        fabVisible=false
                    }
                    else if(dy<0&&(!fabVisible)) {
                        it.animate().alpha(1f).setDuration(500).withEndAction { it.show() }.start()
                        fabVisible=true
                    }
                }

            }
        })
    }

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to [Activity.onStop] of the containing
     * Activity's lifecycle.
     */
    override fun onStop(){
    (activity as MainActivity).getCurrentWorkingTime().removeObservers(viewLifecycleOwner)
        super.onStop()
    }

    override fun onDestroyView() {
        (activity as MainActivity).let{
            it.getDataFromDB()
            it.getCurrentWorkingTime().removeObservers(viewLifecycleOwner)
        }
        super.onDestroyView()

    }


}