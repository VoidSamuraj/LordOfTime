package com.voidsamurai.lordoftime.fragments.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.voidsamurai.lordoftime.MainActivity
import com.voidsamurai.lordoftime.MainActivity.Companion.formatToFloat
import com.voidsamurai.lordoftime.R
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import com.voidsamurai.lordoftime.fragments.CalendarDayEdit
import com.voidsamurai.lordoftime.fragments.adapters.ArrayColorAdapter
import java.util.*


class EditTaskDialog(
    private var LayoutId: Int,
    private val mode: MODE,
    private val startTime:Calendar?=null,
    //  private val margin:Float?=null,
    private val id:Int?=null
) : DialogFragment() {

    companion object{
        enum class MODE(type:Int){
            SAVE(1),
            EDIT(2)
        }
    }

    private var fragment:Fragment?=null
    private lateinit var adapter: ArrayColorAdapter
    private lateinit var nameEdit:EditText
    private lateinit var category:Spinner
    private lateinit var priority:EditText
    private lateinit var startHour:EditText
    private lateinit var endHour:EditText
    private lateinit var isFinished:CheckBox
    private lateinit var isRepeating:CheckBox
    private lateinit var durationEdit:EditText
    private lateinit var addColorB:ImageButton
    private lateinit var deleteButton:Button
    private lateinit var cancelButton:Button
    private lateinit var saveButton:Button
    private lateinit var startHourCalendar:Calendar
    private lateinit var endHourCalendar:Calendar
    private var changed:Boolean=false
    private var dataRow:DataRowWithColor? = null
    var isRutineChecked:MutableLiveData<Boolean> = MutableLiveData(false)
    var lastElementCategory:String=""

    private lateinit var contentView: View

    fun setFrag(frag:Fragment){fragment=frag}
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
    }


    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        contentView= inflater.inflate(LayoutId, null)
        nameEdit =contentView.findViewById(R.id.name_edit)
        category=contentView.findViewById(R.id.check_category)
        priority=contentView.findViewById(R.id.priority_edit)
        startHour=contentView.findViewById(R.id.hour_edit)
        endHour=contentView.findViewById(R.id.hour_to_edit)
        isFinished=contentView.findViewById(R.id.isFinished)
        isRepeating=contentView.findViewById(R.id.isRepeating)
        durationEdit=contentView.findViewById(R.id.duration_edit)
        addColorB=contentView.findViewById(R.id.add_color)
        deleteButton=contentView.findViewById(R.id.delete_edit_button)
        cancelButton=contentView.findViewById(R.id.cancel)
        saveButton=contentView.findViewById(R.id.save)

        setColorSpinner()
        if(mode== MODE.SAVE &&startTime!=null){
            startHourCalendar=startTime.clone() as Calendar
            endHourCalendar=startTime.clone() as Calendar
            val time=String.format("%02d:%02d", startHourCalendar.get(Calendar.HOUR_OF_DAY),startHourCalendar.get(Calendar.MINUTE))
            startHour.setText(time)
            endHour.setText(time)
        }else{
            startHourCalendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            endHourCalendar= Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            dataRow=(activity as MainActivity).getDBOpenHelper().getTaskRow(id!!)
            if(dataRow!=null){
                startHourCalendar= dataRow!!.date.clone() as Calendar
                val cal  =dataRow!!.date.clone() as Calendar
                val baseTime=dataRow!!.workingTime
                cal.add(Calendar.HOUR_OF_DAY,(baseTime/1).toInt())
                cal.add(Calendar.MINUTE,((baseTime%1)*60).toInt())
                if((activity as MainActivity).getDBOpenHelper().getRutinesArray(id).size>0)
                    isRutineChecked.value=true


                endHourCalendar=cal.clone() as Calendar
                startHour.setText(String.format("%02d:%02d", startHourCalendar.get(Calendar.HOUR_OF_DAY),startHourCalendar.get(Calendar.MINUTE)))
                endHour.setText(String.format("%02d:%02d", endHourCalendar.get(Calendar.HOUR_OF_DAY),endHourCalendar.get(Calendar.MINUTE)))
                nameEdit.setText(dataRow!!.name)
                isFinished.isChecked= dataRow!!.finished==1
                //    isRepeating.isChecked= dataRow!!.repeating==1
                durationEdit.setText(String.format("%.2f",dataRow!!.workingTime))
                priority.setText(dataRow!!.priority.toString())
                val pos=adapter.getPosition(Pair(dataRow!!.category,dataRow!!.color))
                lastElementCategory=adapter.getItem(pos)!!.first
                category.setSelection(pos)

            }

        }


        super.onCreateDialog(savedInstanceState)



        builder.setView(contentView)
        cancelButton.setOnClickListener {
            dismiss()
        }


        saveButton.setOnClickListener {

            val canSave=checkIfCanSave()

            if(mode== MODE.SAVE){
                if(canSave) {
                    addNewElement()
                    dismiss()
                }
            }else{
                val category = (category.selectedItem as Pair<*, *>).first.toString()
                if(canSave&&(changed||lastElementCategory!=category)) {
                    if (durationEdit.text.isNullOrEmpty())
                        setDuration()

                    updateRow((activity as MainActivity).getDBOpenHelper().getTaskRow(id!!))
                }
                dismiss()
            }

        }
        isRepeating.setOnClickListener {
            if(mode== MODE.SAVE){
                if (checkIfCanSave()) {
                    val id=addNewElement().toInt()
                   if(id!=-1){

                        (activity as MainActivity).let {
                            it.repeatDialog = RepeatDialog(id,this)
                            it.repeatDialog.show(requireActivity().supportFragmentManager, "Task")

                        }
                    }
                    dismiss()

                }
            }
            else
                id?.let { it1 ->
                    (activity as MainActivity).let {
                        it.repeatDialog = RepeatDialog(it1,this)
                        it.repeatDialog.show(requireActivity().supportFragmentManager, "Task")

                    }
                }
        }
        addColorB.setOnClickListener {
            it?.findNavController()!!.navigate(R.id.action_editTaskSelected_to_colorsFragment)

        }


        startHourCalendar.set(Calendar.SECOND, 0)                                                 //set hours from prev frag
        startHourCalendar.set(Calendar.MILLISECOND, 0)

        endHourCalendar.set(Calendar.SECOND, 0)
        endHourCalendar.set(Calendar.MILLISECOND, 0)

        startHour.setOnClickListener{
            val tp =  TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    startHourCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    startHourCalendar.set(Calendar.MINUTE, minute)
                    startHourCalendar.set(Calendar.SECOND, 0)
                    startHourCalendar.set(Calendar.MILLISECOND, 0)
                    startHour.setText(String.format("%02d:%02d", hourOfDay,minute))//(if (hourOfDay < 10) "0" else "") + hourOfDay.toString() + ":" + if (minute < 10) "0" else "" + minute.toString())
                    setDuration()
                    changed=true
                },
                startHourCalendar.get(Calendar.HOUR_OF_DAY),
                startHourCalendar.get(Calendar.MINUTE),
                true
            )

            tp.show()
        }
        endHour.setOnClickListener{
            val tp =  TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    endHourCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    endHourCalendar.set(Calendar.MINUTE, minute)
                    endHourCalendar.set(Calendar.SECOND, 0)
                    endHourCalendar.set(Calendar.MILLISECOND, 0)
                    endHourCalendar.set(Calendar.MILLISECOND, 0)
                    endHour.setText(String.format("%02d:%02d", hourOfDay,minute))
                    setDuration()
                    changed=true
                },
                endHourCalendar.get(Calendar.HOUR_OF_DAY),
                endHourCalendar.get(Calendar.MINUTE),
                true
            )
            tp.show()
        }
        val onFocusChange= View.OnFocusChangeListener { _, _ -> changed = true }

        category.onFocusChangeListener =onFocusChange
        durationEdit.onFocusChangeListener = onFocusChange
        nameEdit.onFocusChangeListener=onFocusChange
       // isFinished.onFocusChangeListener=onFocusChange
        isFinished.setOnClickListener {changed=true }
       // binding.hourEdit.setOnClickListener {changed=true }
       // binding.dateEdit.setOnClickListener {changed=true }
        /*
        startHour.onFocusChangeListener=onFocusChange
        endHour.onFocusChangeListener=onFocusChange
        */
        priority.onFocusChangeListener=onFocusChange

        durationEdit.addTextChangedListener{
            if(it!!.isNotEmpty()) {

                val dur = (60f * it.toString().formatToFloat()).toInt()
                val minute = dur % 60
                val hour = (dur - minute) / 60
                var hourf=hour+startHourCalendar.get(Calendar.HOUR_OF_DAY)
                var minutef=minute+startHourCalendar.get(Calendar.MINUTE)
                if(minutef>60){
                    minutef-=60
                    ++hourf
                }
                if(hourf>24){
                    hourf=24
                    minutef=0
                    durationEdit.setText(((hourf-startHourCalendar.get(Calendar.HOUR_OF_DAY))+((minutef-startHourCalendar.get(Calendar.MINUTE)).toFloat()/60f)).toString())
                }
                endHourCalendar.set(Calendar.HOUR_OF_DAY,hourf)
                endHourCalendar.set(Calendar.MINUTE,minutef)
                endHour.setText(String.format("%02d:%02d", hourf, minutef))
               // changed=true
            }
        }



        if(mode== MODE.EDIT) {
            deleteButton.setOnClickListener {
                deleteRow(id!!)
                (fragment as CalendarDayEdit).deleteElement(id)
                dismiss()
            }
        }else{
            deleteButton.visibility=View.GONE
        }
        addColorB.setOnClickListener {

        }

        isRutineChecked.observe(
            this,
            { isRepeating.isChecked=it
            }
        )
        isRepeating.setOnCheckedChangeListener { compoundButton, _ ->
            compoundButton.isChecked=isRutineChecked.value!!
        }
        changed=false

        return builder.create()
    }


    fun checkIfCanSave():Boolean{

        val color=resources.getColor(R.color.blue_gray,null)
        var canSave =true
        if(nameEdit.text.isNullOrEmpty()){
            nameEdit.backgroundTintList= ColorStateList.valueOf(Color.RED)
            canSave=false
        }else
            nameEdit.backgroundTintList= ColorStateList.valueOf(color)

        if(priority.text.isNullOrEmpty()){
            priority.backgroundTintList= ColorStateList.valueOf(Color.RED)
            canSave=false
        }else
            priority.backgroundTintList= ColorStateList.valueOf(color)

        return canSave
    }


    fun addNewElement():Long{
        if(durationEdit.text.isNullOrEmpty()||durationEdit.text.toString().toFloat()<0f)
            setDuration()
        val (id,dur)=addRow(
            (category.selectedItem as Pair<*, *>).first.toString (),
            nameEdit.text.toString(),
            startHourCalendar.time.time,
            durationEdit.text.toString(),
            priority.text.toString().toInt()
        )

        if(id!=-1L) {
            val category = (category.selectedItem as Pair<*, *>).first.toString()
            val drwc = DataRowWithColor(
                name = nameEdit.text.toString(),
                category = category,
                date = startHourCalendar,
                workingTime = dur,
                currentWorkingTime = 0f,
                outdated = startHourCalendar.time.time < Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time,
                color = (activity as MainActivity).getColors().value!![category]!!,
                priority = priority.text.toString().toInt(),
                id =id.toInt()

            )
            // add to layout
            val dura =  (durationEdit.text.toString().formatToFloat())
            val (hour,min)=(startHour.text.toString().split(":"))
            val start = hour.toFloat()+min.toFloat()/60                                //czy na 100
            val m=(fragment as CalendarDayEdit).getStartMargin(start,dura,id.toInt())
            if(m==-1)
            {
                Toast.makeText(context,resources.getText(R.string.time_occupied),Toast.LENGTH_SHORT).show()
            }
            else {
                (fragment as CalendarDayEdit).addElement(
                    drwc,
                    m//margin?.toInt() ?: 1
                )
            }
        }
        return id
    }

    private fun setDuration(){
        var hoursInFloat=endHourCalendar.get(Calendar.HOUR_OF_DAY)-startHourCalendar.get(Calendar.HOUR_OF_DAY)+((endHourCalendar.get(Calendar.MINUTE)-startHourCalendar.get(Calendar.MINUTE)).toFloat()/60f)
        if (hoursInFloat<0){
            hoursInFloat*=-1
            // endHour.setText(String.format("%02d:%02d",startHourCalendar.get(Calendar.HOUR_OF_DAY)+(allMinutes/60).toInt(), startHourCalendar.get(Calendar.MINUTE)+(allMinutes%60).toInt()))
        }
        // if (startHourCalendar>endHourCalendar){
        /*
        val allMinutes=hoursInFloat*60
        var endMinutes=startHourCalendar.get(Calendar.MINUTE)+(allMinutes%60).toInt()
        var endHours=startHourCalendar.get(Calendar.HOUR_OF_DAY)+(allMinutes/60).toInt()
        */
        /* if(endMinutes<0) {
             endMinutes += 60
             --endHours
         }*/

        // startHour.setText(String.format("%02d:%02d",endHours,endMinutes ))

        // }
        /*
        if((endHours+hoursInFloat.toInt()+(endMinutes+hoursInFloat%60).toInt())>24){
            endHours=24
            endMinutes=0
            hoursInFloat=endHours-startHourCalendar.get(Calendar.HOUR_OF_DAY)+((endMinutes-startHourCalendar.get(Calendar.MINUTE)).toFloat()/60f)
            if (hoursInFloat<0)
                hoursInFloat*=-1
        }
        */
        durationEdit.setText(hoursInFloat.toString())

    }


    private fun setColorSpinner(){
        val list: Array<String> = (activity as MainActivity).getColors().value!!.keys.toTypedArray()
        list.sort()

        adapter= ArrayColorAdapter(requireContext(),R.layout.element_color_edit,(activity as MainActivity).getColors().value!!.toList())
        adapter.setDropDownViewResource(R.layout.element_color_edit)
        category.adapter = adapter
    }


    private fun updateRow(data:DataRowWithColor) {
        setDuration()

        var dur =  (durationEdit.text.toString().formatToFloat())
        val (hour,min)=(startHour.text.toString().split(":"))
        val start = hour.toFloat()+min.toFloat()/60                                //czy na 100
        val m=(fragment as CalendarDayEdit).getStartMargin(start,dur,data.id)
        if(m==-1)
        {
            Toast.makeText(context,resources.getText(R.string.time_occupied),Toast.LENGTH_SHORT).show()
            return
        }
        m.let{

            dur=(fragment as CalendarDayEdit).getMaxDur(it, (fragment as CalendarDayEdit).getHeight(dur),data.id)
        }
        //if((dur+(startHourCalendar.timeInMillis.toDouble()/3600000)>24))

        //edit element in layout
        (fragment as CalendarDayEdit).editElement(
            DataRowWithColor(
                data.id,
                (category.selectedItem as Pair<*, *>).first.toString (),
                nameEdit.text.toString(),
                startHourCalendar.clone() as Calendar,
                dur,
                priority.text.toString().toInt(),
                0f,
                (category.selectedItem as Pair<*, *>).second.toString (),
                null
            )
        )
        dur*=3600
        (activity as MainActivity).getDBOpenHelper().editTaskRow(
            data.id,
            (category.selectedItem as Pair<*, *>).first.toString (),
            nameEdit.text.toString(),
            startHourCalendar.time.time,
            dur.toInt(),
            priority.text.toString().toInt(),
            0,
            if(isFinished.isChecked)1 else 0)
        //edit firebase row
        (activity as MainActivity).tasks.add  ( id=data.id,
            category = (category.selectedItem as Pair<*, *>).first.toString (),
            name = nameEdit.text.toString(),
            dateTime = startHourCalendar.time.time,
            workingTime = dur.toInt(),
            priority = priority.text.toString().toInt(),
            currentWorkingTime = 0,
            finished = if(isFinished.isChecked)1 else 0)
        (context as MainActivity).getDataFromDB()

    }


    private fun addRow(
        category: String,
        name: String,
        startDateTime: Long,
        hours: String,
        priority: Int
    ):Pair<Long,Float> {
        val dur = hours.toFloat()*3600
        val d=(fragment as CalendarDayEdit).getMaxDur((hours.toFloat()*resources.getDimension(R.dimen.scroll)/24f).toInt(), (fragment as CalendarDayEdit).getHeight(hours.toFloat()))
        // dur= d*3600f

        val id=(activity as MainActivity).getDBOpenHelper().addTaskRow(category, name, startDateTime,dur.toInt(), priority,0)
        if(id!=-1L)
            (activity as MainActivity).tasks.add  ( id=id.toInt(),category = category, name=name, dateTime = startDateTime, workingTime =dur.toInt(), priority = priority,currentWorkingTime = 0, finished = if(isFinished.isChecked)1 else 0)
        (context as MainActivity).getDataFromDB()
        return Pair(id,d)
    }


    private fun deleteRow(id:Int):Int {
        (activity as MainActivity).let{
            val list=it.getDBOpenHelper().deleteRutinesRowAssignedToTask(id)
            list.forEach{
                (activity as MainActivity).rutines.delete(it)
            }
            it.getDBOpenHelper().deleteTaskRow(id)
            it.tasks.delete (id.toString())
        }
        (context as MainActivity).getDataFromDB()
        return id
    }

    override fun onDismiss(dialog: DialogInterface) {
        isRutineChecked.removeObservers(this)
        super.onDismiss(dialog)
    }
}