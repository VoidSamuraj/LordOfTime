package com.voidsamurai.lordoftime

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.voidsamurai.lordoftime.bd.LOTDatabaseHelper
import com.voidsamurai.lordoftime.databinding.ActivityMainBinding
import layout.DataRowWithColor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {



    private var _mainFragmentBinding: ActivityMainBinding?=null
    private val mainFragmentBinding get()=_mainFragmentBinding!!
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var _menu: Menu
    private lateinit var inflater: LayoutInflater
    private lateinit var db: SQLiteDatabase
    private  var showOutdated:Boolean=true
    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var navController: NavController
        @SuppressLint("StaticFieldLeak")
        private lateinit var editNav: NavController
        private lateinit var oh: LOTDatabaseHelper
        private  var queryArrayByDate: MutableLiveData<ArrayList<DataRowWithColor>> = MutableLiveData()
        private var queryArrayByPriority: MutableLiveData<ArrayList<DataRowWithColor>> = MutableLiveData()
        private  var queryArrayByDuration: MutableLiveData<ArrayList<DataRowWithColor>> = MutableLiveData()
        private  var colorsArray: MutableLiveData<Map<String,String>> = MutableLiveData()
        fun getQueryArrayByDate()= queryArrayByDate
        fun getQueryArrayByPriority()= queryArrayByPriority
        fun getQueryArrayByDuration()= queryArrayByDuration
        fun getColors()= colorsArray
        fun getDBOpenHelper():LOTDatabaseHelper=oh

    }


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

        inflater = LayoutInflater.from(this)



    }

    override fun onDestroy() {
        super.onDestroy()
        oh.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        this._menu = menu
        return true
    }


    override fun onSupportNavigateUp(): Boolean {

        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else if(navController.currentDestination?.label=="fragment_tasks_edit_host"){
            editNav=findNavController(R.id.edit_fragment)

            if(editNav.currentDestination?.label=="EditTask"||editNav.currentDestination?.label=="fragment_colors_list")
                editNav.popBackStack()
            else{
                navController.popBackStack()
            }
        }else if(navController.currentDestination?.label=="fragment_many_charts"){
            findNavController(R.id.nav_host_fragment).navigateUp()
        }
        //
        else
            super.onBackPressed()
    }

    /**
     * @author Karol Robak
     *
     * **/


    fun getDataFromDB(){
        val selectionQuery: String = "SELECT TASKTABLE._id, TASKTABLE.category, TASKTABLE.name, TASKTABLE.datetime,TASKTABLE.working_time,TASKTABLE.priority, COLOR.color " +
                "FROM TASKTABLE LEFT JOIN COLOR on TASKTABLE.category=COLOR.category_id ORDER BY TASKTABLE.datetime, TASKTABLE.priority DESC"
        val query:ArrayList<DataRowWithColor> =ArrayList()
        var c: Cursor = db.rawQuery(selectionQuery, null)
        var array:Map<String,String> = HashMap()

        if(c.moveToFirst())
            do {
                val calendar : Calendar = Calendar.getInstance()
                calendar.time= Date( c.getLong(3))
                if (showOutdated)
                    query.add(DataRowWithColor(c.getInt(0),c.getString(1), c.getString(2),  calendar,c.getFloat(4),c.getInt(5),c.getString(6)?:"#FFFFFF"))
                else
                    if(!calendar.before(Calendar.getInstance()))
                        query.add(DataRowWithColor(c.getInt(0),c.getString(1), c.getString(2),  calendar,c.getFloat(4),c.getInt(5),c.getString(6)?:"#FFFFFF"))

            }while (c.moveToNext())
        c.close()
        c = db.rawQuery("SELECT * FROM COLOR", null)
        if(c.moveToFirst())
            do
                array=array.plus(Pair(c.getString(0),c.getString(1)))
            while (c.moveToNext())
        c.close()
        colorsArray.value=array
        query.forEach { dataRowWithColor ->
            if (dataRowWithColor.date.before(Calendar.getInstance()))
                dataRowWithColor.outdated=true
        }
        queryArrayByDate.value =query
        queryArrayByPriority.value=getSortedByPriority(query)
        queryArrayByDuration.value=getSortedByDuration(query)
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