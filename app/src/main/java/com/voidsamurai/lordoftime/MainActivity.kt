package com.voidsamurai.lordoftime

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.voidsamurai.lordoftime.bd.LOTDatabaseHelper
import com.voidsamurai.lordoftime.databinding.ActivityMainBinding
import com.voidsamurai.lordoftime.fragments.EditListDirections
import com.voidsamurai.lordoftime.fragments.HomeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import layout.DataRowWithColor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {



    private var _mainFragmentBinding: ActivityMainBinding?=null
    private val mainFragmentBinding get()=_mainFragmentBinding!!
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var inflater: LayoutInflater
    private lateinit var db: SQLiteDatabase
    private  var showOutdated:Boolean=true
    private lateinit var navController: NavController
    var isTaskStarted:Boolean=true
    var isFromEditFragment:Boolean=false
    var isFromWorkFragment:Boolean=false
    var currentTask:String="current taskttttttt"
    var currentTaskId:Int=0

    private lateinit var oh: LOTDatabaseHelper
    private  var queryArrayByDate: MutableLiveData<ArrayList<DataRowWithColor>> = MutableLiveData()
    private var queryArrayByPriority: MutableLiveData<ArrayList<DataRowWithColor>> = MutableLiveData()
    private  var queryArrayByDuration: MutableLiveData<ArrayList<DataRowWithColor>> = MutableLiveData()
    private  var workingTime:MutableLiveData<Int> = MutableLiveData(0)
    private  var colorsArray: MutableLiveData<Map<String,String>> = MutableLiveData()
    fun getCurrentWorkingTime()= workingTime
    fun getQueryArrayByDate()= queryArrayByDate
    fun getQueryArrayByPriority()= queryArrayByPriority
    fun getQueryArrayByDuration()= queryArrayByDuration
    fun getColors()= colorsArray
    fun getDBOpenHelper():LOTDatabaseHelper=oh




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oh = LOTDatabaseHelper(this)
        db = oh.readableDatabase
        getDataFromDB()
        _mainFragmentBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainFragmentBinding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        navController = findNavController(R.id.nav_host_fragment)
        drawerLayout = mainFragmentBinding.drawerLayout
        mainFragmentBinding.navView.setupWithNavController(navController)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, drawerLayout)

        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(1000)
                getCurrentWorkingTime().postValue( getCurrentWorkingTime().value!!.inc())
            }}

        inflater = LayoutInflater.from(this)



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if ( navController.currentDestination?.label=="first_fragment_label"||
            item.itemId==R.id.category||
            item.itemId==R.id.order)
            super.onOptionsItemSelected(item)
        else{
            onBackPressed()
            true
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        oh.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        navController.currentDestination?.label.let {
            when {
                drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
                it == "EditList" -> {
                    isFromEditFragment=true
                    super.onBackPressed()
                }
                it == "WorkingFragment" -> {
                    isFromWorkFragment=true
                    super.onBackPressed()
                }
                it == "first_fragment_label" ->{
                    val fragment= supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val frag=fragment.childFragmentManager.primaryNavigationFragment  as HomeFragment
                    if (!frag.isButtonHidden)
                        frag.homeFragmentBinding.view.performClick()
                }
                else -> {
                    super.onBackPressed()
                }
            }
        }
    }

    /**
     * @author Karol Robak
     *
     * **/


    fun getDataFromDB(){
        CoroutineScope(Dispatchers.IO).run {
            val selectionQuery: String =
                "SELECT TASKTABLE._id, TASKTABLE.category, TASKTABLE.name, TASKTABLE.datetime,TASKTABLE.working_time,TASKTABLE.priority, TASKTABLE.current_work_time, COLOR.color " +
                        "FROM TASKTABLE LEFT JOIN COLOR on TASKTABLE.category=COLOR.category_id ORDER BY TASKTABLE.datetime, TASKTABLE.priority DESC"
            val query: ArrayList<DataRowWithColor> = ArrayList()
            var c: Cursor = db.rawQuery(selectionQuery, null)
            var array: Map<String, String> = HashMap()

            if (c.moveToFirst())
                do {
                    val calendar: Calendar = Calendar.getInstance()
                    calendar.time = Date(c.getLong(3))
                    if (showOutdated)
                        query.add(
                            DataRowWithColor(
                                c.getInt(0),
                                c.getString(1),
                                c.getString(2),
                                calendar,
                                c.getFloat(4),
                                c.getInt(5),
                                c.getFloat(6),
                                c.getString(7) ?: "#FFFFFF"
                            )
                        )
                    else
                        if (!calendar.before(Calendar.getInstance()))
                            query.add(
                                DataRowWithColor(
                                    c.getInt(0),
                                    c.getString(1),
                                    c.getString(2),
                                    calendar,
                                    c.getFloat(4),
                                    c.getInt(5),
                                    c.getFloat(6),
                                    c.getString(7) ?: "#FFFFFF"
                                )
                            )

                } while (c.moveToNext())
            c.close()
            c = db.rawQuery("SELECT * FROM COLOR", null)
            if (c.moveToFirst())
                do
                    array = array.plus(Pair(c.getString(0), c.getString(1)))
                while (c.moveToNext())
            c.close()
            colorsArray.value = array
            query.forEach { dataRowWithColor ->
                if (dataRowWithColor.date.before(Calendar.getInstance()))
                    dataRowWithColor.outdated = true
            }
            queryArrayByDate.value = query
            queryArrayByPriority.value = getSortedByPriority(query)
            queryArrayByDuration.value = getSortedByDuration(query)
        }
    }

    private fun getSortedByDuration(qa: ArrayList<DataRowWithColor>): ArrayList<DataRowWithColor> {
        qa.sortWith(compareBy<DataRowWithColor> { it.workingTime }.thenBy{it.name} )
        val na : ArrayList<DataRowWithColor> = ArrayList()
        na.addAll(qa.toList())
        qa.sortWith(compareBy<DataRowWithColor> { it.date.time.time }.thenByDescending{it.priority} )
        return na
    }

    private fun getSortedByPriority(qa:ArrayList<DataRowWithColor>):ArrayList<DataRowWithColor>{
        qa.sortWith(compareByDescending<DataRowWithColor> { it.priority }.thenBy{it.date.time.time} )
        qa.reverse()
        val na : ArrayList<DataRowWithColor> = ArrayList()
        na.addAll(qa.toList())
        qa.sortWith(compareBy<DataRowWithColor> { it.date.time.time }.thenByDescending{it.priority} )
        return na
    }

}