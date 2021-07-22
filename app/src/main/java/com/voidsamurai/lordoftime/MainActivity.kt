package com.voidsamurai.lordoftime

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.voidsamurai.lordoftime.bd.LOTDatabaseHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_many_charts.*
import layout.DataRowWithColor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

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
        private lateinit var queryArrayByDate: ArrayList<DataRowWithColor>
        private lateinit var queryArrayByPriority: ArrayList<DataRowWithColor>
        private lateinit var queryArrayByDuration: ArrayList<DataRowWithColor>
        private lateinit var colorsArray: Map<String,String>
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

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)


        navController = findNavController(R.id.nav_host_fragment)
        drawerLayout = drawer_layout
        nav_view.setupWithNavController(navController)
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

            if(editNav.currentDestination?.label=="EditTask")
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


    /**
     * @author Karol Robak
     * Implements toolbar buttons animations
     * **/

    fun getDataFromDB(){
        val selectionQuery: String = "SELECT TASKTABLE._id, TASKTABLE.category, TASKTABLE.name, TASKTABLE.datetime,TASKTABLE.working_time,TASKTABLE.priority, COLOR.color " +
                "FROM TASKTABLE LEFT JOIN COLOR on TASKTABLE.category=COLOR.category_id ORDER BY TASKTABLE.datetime, TASKTABLE.priority DESC"
        val query:ArrayList<DataRowWithColor> =ArrayList()
        var c: Cursor = db.rawQuery(selectionQuery, null)
        colorsArray=HashMap()

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
                colorsArray=colorsArray.plus(Pair(c.getString(0),c.getString(1)))
            while (c.moveToNext())
        c.close()
        query.forEach { dataRowWithColor ->
            if (dataRowWithColor.date.before(Calendar.getInstance()))
                dataRowWithColor.outdated=true
        }
        queryArrayByDate =query
        queryArrayByPriority=getSortedByPriority(query)
        queryArrayByDuration=getSortedByDuration(query)
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