package com.voidsamurai.lordoftime.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.MainActivity.Companion.formatToFloat
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.databinding.FragmentTaskEditBinding
import com.voidsamurai.lordoftime.fragments.adapters.ArrayColorAdapter
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import com.voidsamurai.lordoftime.fragments.dialogs.ConfirmDialog
import com.voidsamurai.lordoftime.fragments.dialogs.RepeatDialog
import java.util.*

class EditTaskSelected : Fragment() ,DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener{

    private lateinit var adapter: ArrayColorAdapter
    private var _binding: FragmentTaskEditBinding?=null
    private val binding get()=_binding!!
    var isRutineChecked: MutableLiveData<Boolean> = MutableLiveData(false)

    val onFocusChange= View.OnFocusChangeListener { _, _ -> changed = true }
    var lastElementCategory:String=""
    var changed:Boolean=false

    private var dateFormat:java.text.DateFormat= java.text.DateFormat.getDateInstance(
        java.text.DateFormat.LONG,
        Locale.getDefault()
    )

    private var data: DataRowWithColor? = null
    private lateinit var newDate: Calendar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentTaskEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setColorSpinner()

        requireActivity().window.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.background,null)))


        if(arguments!=null&&EditTaskSelectedArgs.fromBundle(requireArguments()).dataColor!=null){
            data =EditTaskSelectedArgs.fromBundle(requireArguments()).dataColor
            binding.deleteEditButton.visibility=View.VISIBLE
            binding.nameEdit.setText(data!!.name)
            val lastElementPos=adapter.getPosition(Pair(data!!.category,data!!.color))
     //       Log.v("POSITION",""+lastElementPos+" "+adapter.count)
            lastElementCategory=adapter.getItem(lastElementPos)!!.first

            if(data!!.id==(activity as MainActivity).currentTaskId)
                binding.deleteEditButton.isEnabled=false

            binding.checkCategory.setSelection(lastElementPos)
            binding.priorityEdit.setText(data!!.priority.toString())
            binding.dateEdit.setText(dateFormat.format(data!!.date.time))
            binding.durationEdit.setText((((data!!.workingTime/3600)*100).toInt().toFloat()/100).toString())
            binding.hourEdit.setText(String.format("%02d:%02d",data!!.date.get(Calendar.HOUR_OF_DAY),data!!.date.get(Calendar.MINUTE)))
            binding.isFinished.isChecked=data!!.finished==1
            newDate= Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            data!!.date.let {newDate.set(
                it.get(Calendar.YEAR),
                it.get(Calendar.MONTH),
                it.get(Calendar.DAY_OF_MONTH),
                it.get(Calendar.HOUR_OF_DAY),
                it.get(Calendar.MINUTE),
                0
            )
                newDate.set(Calendar.MILLISECOND, 0)
            }
            binding.deleteEditButton.setOnClickListener{
                ConfirmDialog(""+data?.name,{},{
                    deleteRow(data!!)
                    update()
                    it.findNavController().navigateUp()
                },{}).show(parentFragmentManager,"ConfirmDialog")

            }

            binding.saveEditButton.setOnClickListener{
                val canSave=checkIfCanSave()
                val newCategory= (binding.checkCategory.selectedItem as Pair<*, *>).first.toString ()
                if(canSave&&(changed||lastElementCategory!=newCategory)) {
                    updateRow()
                    update()
                }
                it.findNavController().navigateUp()
                }
            if((activity as MainActivity).getDBOpenHelper().getRutinesArray(data!!.id).size>0)
                isRutineChecked.value=true

            binding.isRepeating.setOnClickListener {
                data!!.id.let { it1 ->
                    (activity as MainActivity).let {
                        it.repeatDialog = RepeatDialog(it1,this)
                        it.repeatDialog.show(requireActivity().supportFragmentManager, "Task")

                    }
                }
            }
        }else{
            newDate= Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            newDate.set(Calendar.SECOND, 0)
            newDate.set(Calendar.MILLISECOND, 0)
            binding.dateEdit.setText(dateFormat.format(newDate.time))
            binding.hourEdit.setText("${newDate.get(Calendar.HOUR_OF_DAY)}:${newDate.get(Calendar.MINUTE)}")

            binding.deleteEditButton.visibility=View.GONE

            binding.saveEditButton.setOnClickListener {
                if(checkIfCanSave()){
                addRow(
                    (binding.checkCategory.selectedItem as Pair<*, *>).first.toString(),
                    binding.nameEdit.text.toString(),
                    newDate.time.time,
                    binding.durationEdit.text.toString(),
                    binding.priorityEdit.text.toString().toInt()
                )
                update()
                it.findNavController().navigateUp()
            }
            }

            binding.isRepeating.setOnClickListener {
                if(checkIfCanSave()){
                    val id=addRow(
                        (binding.checkCategory.selectedItem as Pair<*, *>).first.toString(),
                        binding.nameEdit.text.toString(),
                        newDate.time.time,
                        binding.durationEdit.text.toString(),
                        binding.priorityEdit.text.toString().toInt()
                    ).toInt()
                    update()
                   // it.findNavController().navigateUp()
                    if(id!=-1){
                        (activity as MainActivity).let {
                            it.repeatDialog = RepeatDialog(id,this)
                            it.repeatDialog.show(requireActivity().supportFragmentManager, "Task")

                        }
                    }
                }

            }

        }


        binding.nameEdit.onFocusChangeListener=onFocusChange
        binding.priorityEdit.onFocusChangeListener=onFocusChange
        binding.durationEdit.onFocusChangeListener=onFocusChange
        binding.isFinished.onFocusChangeListener=onFocusChange
        binding.isFinished.setOnClickListener {changed=true }
        binding.hourEdit.setOnClickListener {changed=true }
        binding.dateEdit.setOnClickListener {changed=true }




        binding.addColor.setOnClickListener{
            it?.findNavController()!!.navigate(R.id.action_editTaskSelected_to_colorsFragment)
        }

        binding.dateEdit.setOnClickListener{
            val dp =
                DatePickerDialog(
                    requireContext(),
                    this,
                    newDate.get(Calendar.YEAR),
                    newDate.get(Calendar.MONTH),
                    newDate.get(Calendar.DAY_OF_MONTH)
                )
            dp.show()
        }

        binding.hourEdit.setOnClickListener{
            val tp =  TimePickerDialog(
                requireContext(), this,
                newDate.get(Calendar.HOUR_OF_DAY), newDate.get(Calendar.MINUTE), true
            )

            tp.show()
        }

        binding.cancelEditButton.setOnClickListener{
            it.findNavController().navigateUp()
        }

        isRutineChecked.observe(
            viewLifecycleOwner
        ) {
            binding.isRepeating.isChecked = it

        }
        binding.isRepeating.setOnCheckedChangeListener { compoundButton, _ ->
            compoundButton.isChecked=isRutineChecked.value!!
        }

    }
    fun checkIfCanSave():Boolean{
        var canSave=true
        val color=resources.getColor(R.color.blue_gray,null)

        if(binding.nameEdit.text.isNullOrEmpty()){
            binding.nameEdit.backgroundTintList= ColorStateList.valueOf(Color.RED)
            canSave=false
        }else
            binding.nameEdit.backgroundTintList= ColorStateList.valueOf(color)

        if(binding.priorityEdit.text.isNullOrEmpty()){
            binding.priorityEdit.backgroundTintList= ColorStateList.valueOf(Color.RED)
            canSave=false
        }else
            binding.priorityEdit.backgroundTintList= ColorStateList.valueOf(color)

        if( binding.durationEdit.text.isNullOrEmpty()){
            binding.durationEdit.backgroundTintList= ColorStateList.valueOf(Color.RED)
            canSave=false
        }else
            binding.durationEdit.backgroundTintList= ColorStateList.valueOf(color)
        return canSave
    }


    override fun onResume() {
        super.onResume()
        changed=false
    }

    override fun onDestroyView() {
        isRutineChecked.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

    private fun updateRow() {
        (activity as MainActivity).getDBOpenHelper().editTaskRow(
            data!!.id,
            (binding.checkCategory.selectedItem as Pair<*, *>).first.toString (),
            binding.nameEdit.text.toString(),
            newDate.time.time,
            (binding.durationEdit.text.toString().formatToFloat()*3600).toInt(),
            binding.priorityEdit.text.toString().toInt(),
            0,
            if(binding.isFinished.isChecked)1 else 0 )
        (activity as MainActivity).tasks.add  ( id=data!!.id,
            category = (binding.checkCategory.selectedItem as Pair<*, *>).first.toString (),
            name = binding.nameEdit.text.toString(),
            dateTime = newDate.time.time,
            workingTime = (binding.durationEdit.text.toString().formatToFloat()*3600).toInt(),
            priority = binding.priorityEdit.text.toString().toInt(),
            currentWorkingTime = 0,
            finished =if(binding.isFinished.isChecked)1 else 0 )
    }

    private fun addRow(
        category: String,
        name: String,
        startDateTime: Long,
        hours: String,
        priority: Int
    ):Long {
        val id=(activity as MainActivity).getDBOpenHelper().addTaskRow(category, name, startDateTime, (hours.toFloat()*3600).toInt(), priority,0,(activity as MainActivity).userId)
        if(id!=-1L)
            (activity as MainActivity).tasks.add  ( id=id.toInt(),category, name, startDateTime, (hours.toFloat()*3600).toInt(), priority,0, finished =if(binding.isFinished.isChecked)1 else 0 )
        //(activity as MainActivity).getDBOpenHelper().addOldstatRow(startDateTime,(hours.toFloat()*3600).toInt())
        update()
        return id
    }

    private fun deleteRow(dataRowWithColor: DataRowWithColor){
        (activity as MainActivity).let{
            val list=it.getDBOpenHelper().deleteRutinesRowAssignedToTask(dataRowWithColor.id)
            list.forEach {  (activity as MainActivity).rutines.delete(it) }
            it.getDBOpenHelper().deleteTaskRow(dataRowWithColor.id)
            it.tasks.delete(dataRowWithColor.id.toString())
        }

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        newDate.set(year, month, dayOfMonth)
        newDate.set(Calendar.SECOND, 0)
        newDate.set(Calendar.MILLISECOND, 0)
        binding.dateEdit.setText(dateFormat.format(newDate.time))
        changed=true
    }


    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
        newDate.set(Calendar.MINUTE, minute)
        newDate.set(Calendar.SECOND, 0)
        newDate.set(Calendar.MILLISECOND, 0)
        binding.hourEdit.setText((if (hourOfDay < 10) "0" else "") + hourOfDay.toString() + ":" + if (minute < 10) "0" else "" + minute.toString())
        changed=true
    }

    private fun update(){
        (context as MainActivity).getDataFromDB()
    }
    private fun setColorSpinner(){
        val list: Array<String> = (activity as MainActivity).getColors().value!!.keys.toTypedArray()
        list.sort()

        adapter= ArrayColorAdapter(requireContext(),R.layout.element_color_edit,(activity as MainActivity).getColors().value!!.toList())
        adapter.setDropDownViewResource(R.layout.element_color_edit)
        binding.checkCategory.adapter = adapter
    }
}