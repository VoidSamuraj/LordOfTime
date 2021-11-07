package com.voidsamurai.lordoftime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipDescription
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
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
import com.voidsamurai.lordoftime.fragments.HomeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.voidsamurai.lordoftime.bd.DataRowWithColor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.voidsamurai.lordoftime.bd.DAOColors
import com.voidsamurai.lordoftime.bd.DAOOldTasks
import com.voidsamurai.lordoftime.bd.DAOTasks
import com.voidsamurai.lordoftime.bd.DataRow
import com.voidsamurai.lordoftime.bd.OldData

import java.io.*
import android.graphics.ImageDecoder
import androidx.core.app.NotificationCompat
import com.voidsamurai.lordoftime.charts_and_views.NTuple4
import com.voidsamurai.lordoftime.fragments.WorkingFragment
import android.widget.RemoteViews





class MainActivity : AppCompatActivity() {

    companion object{
        val SHARED_PREFERENCES:String="sharedPreferences"
        fun String.formatToFloat():Float{
            return toString()
                .replace(',','.')
                .replace('-','.')
                .toFloat()
        }

    }

    private lateinit var firebaseDB:FirebaseDatabase
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var storageReference:StorageReference
   // private lateinit var notification:Notification
   // private lateinit var  contentView:RemoteViews
  //  private lateinit var notificationManager:NotificationManager
    //val jobSchedulerName get()=JOB_SCHEDULER_SERVICE
   // private val WIDGET_ID:String="1"
  //  private val WIDGET_NAME="LOTR"
    private val LANGUAGE:String="Language"
    private val SHOW_OUTDATED:String="showOutdated"
    private val SETTINGS_CHANGE:String="changeColor"
    private val MODE:String="Mode"
    private var _mainFragmentBinding: ActivityMainBinding?=null
    private val mainFragmentBinding get()=_mainFragmentBinding!!
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var inflater: LayoutInflater
    private lateinit var navController: NavController
    var showOutdated:Boolean =true
    fun logout(){
        getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit().putBoolean("logged_in",false).apply()
    }
    fun setOutdated(boolean: Boolean){
        showOutdated=boolean
        sharedPreferences.edit().putBoolean(SHOW_OUTDATED,boolean).apply()
    }
    fun setSortInWorkFragment(order:WorkingFragment.Order,sortBy:WorkingFragment.SortBy){
        sharedPreferences.edit().putString("ORDER",order.name).putString("SORTBY",sortBy.name).apply()
    }
    fun setMainChartRange(range:Int=24){
        sharedPreferences.edit().putInt("MAIN_CHART_RANGE",range).apply()
    }

    fun getMainChartRange():Int{
        return sharedPreferences.getInt("MAIN_CHART_RANGE",24)
    }
    fun setCurrentTaskId(id:Int){
        sharedPreferences.edit().putInt("TASK_ID",id).apply()
    }

    fun getCurrentTaskId():Int{
        return sharedPreferences.getInt("TASK_ID",-1)
    }
    fun setIsRunningTask(isRunning: Boolean){
        sharedPreferences.edit().putBoolean("IS_RUNNING_TASK",isRunning).apply()
    }

    fun getTimeToAdd():Int{
        return sharedPreferences.getInt("TIME_TO_ADD",0)
    }
    fun setTimeToAdd(timeToAdd: Int){
        sharedPreferences.edit().putInt("TIME_TO_ADD",timeToAdd).apply()
    }
    fun getStartTime():Long{
        return sharedPreferences.getLong("START_TIME",Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time)
    }
    fun setStartTime(startTime: Long){
        sharedPreferences.edit().putLong("START_TIME",startTime).apply()
    }

    fun getIsRunningTask():Boolean{
        return sharedPreferences.getBoolean("IS_RUNNING_TASK",false)
    }
    fun setMainChartAuto():Boolean{
        val state=getMainChartAuto().not()
        sharedPreferences.edit().putBoolean("MAIN_CHART_AUTO",state).apply()
        return state
    }
    fun getMainChartAuto():Boolean{
        return sharedPreferences.getBoolean("MAIN_CHART_AUTO",false)
    }
    fun setCalendarChartRange(range:Int){
        sharedPreferences.edit().putInt("CALENDAR_CHART_RANGE",range).apply()
    }
    fun getCalendarChartRange():Int{
        return sharedPreferences.getInt("CALENDAR_CHART_RANGE",8)
    }
    fun getWorkSorting():Pair<String,String>{
        val order=sharedPreferences.getString("ORDER","ASC")
        val sort=sharedPreferences.getString("SORTBY","DATE")
        return Pair(order!!,sort!!)
    }

    var notificationService:BackgroundTimeService=BackgroundTimeService()
    var isTaskStarted:Boolean=false
    var isFromEditFragment:Boolean=false
    var isFromWorkFragment:Boolean=false
    var currentTaskId:Int?=null
    var lastTaskId:Int?=null
    var lastTaskPositioon:Int?=null
    var lastButton:ImageButton?=null
    var userId:String?=null
    var emailId:String?=null
    var userName:String?=null
    var userImage:Bitmap?=null
    lateinit var  colors: DAOColors
    lateinit var  tasks: DAOTasks
    lateinit var  oldTasks: DAOOldTasks
    private lateinit var sharedPreferences:SharedPreferences
    fun getMode()=sharedPreferences.getInt(MODE,-1)
    fun getLanguage()=sharedPreferences.getString(LANGUAGE,"DEFAULT")
    private lateinit var db: SQLiteDatabase
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
    fun getDBOpenHelper():LOTDatabaseHelper= oh
    lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient:GoogleSignInClient
    var change:Triple<Boolean,Boolean,Boolean> = Triple(false, false, false)



    /**
     * Sets light styles
     *
     * @param style
     * -1 default mode
     * 1 light mode
     * 2 dark mode
     * */
    fun setStyle(style:Int){
        val sharedPreferences:SharedPreferences=getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)
        val editor:SharedPreferences.Editor=sharedPreferences.edit()
        editor.putInt(MODE,style).putBoolean(SETTINGS_CHANGE,true).apply()
        newIntent()
    }
    private fun refreshHomeFragmentAndDB(){
        getDataFromDB()
        if(change.first&&change.second&&change.third)
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment()).commit()
    }

    fun updateLocalColorDB(data:Map<String,String>){
        val localData:Map<String,String> = colorsArray.value!!
        for(row in data){
            if(localData.containsKey(row.key)){
                if (localData[row.key]!=row.value) {
                    oh.editColorRow(row.key, row.key, row.value)
                    change= Triple(true,change.second,change.third)
                    refreshHomeFragmentAndDB()
                }
            }
            else{
                oh.addColorRow(row.key,row.value)
                change= Triple(true,change.second,change.third)
                refreshHomeFragmentAndDB()
            }
        }

    }

    fun updateLocalTaskDB(dataFromFirebase:ArrayList<DataRow>){
        val localData:MutableList<DataRowWithColor> = queryArrayByDate.value!!
        fun DataRow.compareById(array: MutableList<DataRowWithColor>):Boolean{
            for(el in array)
                if(el.id==this.id)
                    return true
            return false
        }
        fun DataRow.compareByRestAndId(array: MutableList<DataRowWithColor>):Boolean{
            for(el in array){
                if(el.id==this.id && (el.name!=this.name || el.category!=this.category || el.date.time.time!=this.date.time.time))
                    return true

            }
            return false
        }
        for(row in dataFromFirebase) {
            if (row.compareByRestAndId(localData)) {

                  oh.editTaskRow(
                      row.id,
                      row.category,
                      row.name,
                      row.date.time.time,
                      row.workingTime.toInt(),
                      row.priority,
                      row.currentWorkingTime.toInt()
                  )
                  change= Triple(change.first,true,change.third)
                  refreshHomeFragmentAndDB()

            } else if(!row.compareById(localData)){
                row.let {
                    val id:Long=oh.addTaskRow(
                        it.category,
                        it.name,
                        it.date.time.time,
                        it.workingTime.toInt(),
                        it.priority,
                        it.currentWorkingTime.toInt()
                    )
                    if(id!=-1L) {
                        tasks.delete(row.id.toString())
                        tasks.add(
                            id.toInt(),
                            it.category,
                            it.name,
                            it.date.time.time,
                            it.workingTime.toInt(),
                            it.priority,
                            it.currentWorkingTime.toInt()
                        )
                        change= Triple(change.first,true,change.third)
                        refreshHomeFragmentAndDB()
                    }
                }


            }
        }

    }

    fun updateLocalOldTaskDB(data:ArrayList<OldData>){

        val data2=data.map {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.time=Date(it.date_id)
            cal.set(Calendar.HOUR,0)
            cal.set(Calendar.MINUTE,0)
            cal.set(Calendar.SECOND,0)
            cal.set(Calendar.MILLISECOND,0)
            OldData(cal.time.time,it.category,it.workingTime)
        } as ArrayList<*>

        val localData:List<Triple<Long,Float,String>> = getOldData().map { Triple(it.first.time.time,it.second,it.third) }
        fun OldData.compareID(array: List<Triple<Long,Float,String>>):Boolean{
            var isGood=false
            for(el in array) {
                if (el.first == this.date_id)
                    isGood = true
             //   Log.v("Testy",""+el.first+" "+this.date_id+" "+isGood)
            }
            return isGood
        }
        fun OldData.compareTimeWithId(array: List<Triple<Long,Float,String>>):Boolean{
            for(el in array)
                if(el.first==this.date_id && el.second!=this.workingTime)
                    return true
            return false
        }
        for(row in data2) {
            row as OldData
            if(row.compareTimeWithId(localData)){
                oh.editOldstatRow(row.date_id,row.date_id,row.workingTime.toInt(),row.category)
                change= Triple(change.first,change.second,true)
                refreshHomeFragmentAndDB()
            }
            else if(!row.compareID(localData)){
                oh.addOldstatRow(row.date_id, row.workingTime.toInt(), row.category)
                change= Triple(change.first,change.second,true)
                refreshHomeFragmentAndDB()
            }
        }

    }
    fun saveAvatar(uri: Uri){
        val bmp: Bitmap?
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            bmp= ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        else
            bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)

        val stream = ByteArrayOutputStream()
        bmp!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()



        try{
            val fo=openFileOutput(userId+".jpeg", MODE_PRIVATE)
            fo.write(byteArray)
            fo.close()
        }catch (e:Exception){
            Log.e("IMAGE_INTERNAL_SAVING", e.cause.toString())
        }

        userImage=bmp

        mainFragmentBinding.navView.getHeaderView(0)
            .findViewById<ImageView>(R.id.user_avatar).setImageURI(uri)
        storageReference.putFile(uri)
            .addOnSuccessListener { Toast.makeText(this, resources.getString(R.string.avatar_changed), Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { Log.e("AVATAR",it.cause.toString())

            }

    }
    fun getAvatar(){

        val f=File(userId+".jpeg")
        var bitmap:Bitmap?=null
        if(f.exists()) {
            val fo = openFileInput(f.path)
            bitmap = BitmapFactory.decodeStream(fo)
        }

        if(bitmap==null) {
            val file = File.createTempFile("template", ".jpg")
            storageReference.getFile(file).addOnSuccessListener {

                val bmp = BitmapFactory.decodeStream(FileInputStream(file), null, null)
                val stream = ByteArrayOutputStream()
                bmp!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                userImage = bmp

                mainFragmentBinding.navView.getHeaderView(0)
                    .findViewById<ImageView>(R.id.user_avatar).setImageBitmap(bmp)

                try{
                    val fo=openFileOutput(userId+".jpeg", MODE_PRIVATE)
                    fo.write(stream.toByteArray())
                    fo.close()
                }catch (e:Exception){
                    Log.e("IMAGE_INTERNAL_SAVING", e.cause.toString())
                }
            }


        }else{
            userImage=bitmap
            mainFragmentBinding.navView.getHeaderView(0)
                .findViewById<ImageView>(R.id.user_avatar).setImageBitmap(bitmap)
        }

    }

    fun setLanguage(language:String){
        val languageToLoad = when(language){
            "DEFAULT","DOMYŚLNIE"-> Locale.getDefault().language
            "PL"-> "pl"
            "EN"->"en_EN"
            else->{"en"}
        }
        getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit().putBoolean(SETTINGS_CHANGE,true).apply()

        val locale = Locale(languageToLoad)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        sharedPreferences.edit().putString(LANGUAGE,language).apply()
        resources.updateConfiguration(config, resources.displayMetrics)

        newIntent()
    }
    fun newIntent(){
        val intent = intent
        //val intent = Intent(this,SplashScreenActivity::class.java)

        // intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)

        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {

       // notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //createNotificationChannel("Opis")



        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.defaultt_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        analytics = Firebase.analytics
        auth = Firebase.auth

        userId=intent.getStringExtra("user_id")
        emailId=intent.getStringExtra("email_id")
        userName=intent.getStringExtra("user_name")

        if(userId.isNullOrBlank()||userName.isNullOrBlank()){
            auth.currentUser.let {
                userId = auth.uid
                it?.email.let{it-> emailId =it}


                it?.displayName.let{ it -> userName =it}
            }
        }

        firebaseDB=Firebase.database
        colors=DAOColors(this)
        tasks= DAOTasks(this)
        oldTasks= DAOOldTasks(this)
        storageReference=FirebaseStorage.getInstance().reference.child("profileImages").child(userId!!+".jpg")

        sharedPreferences=getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)


            Log.v("TESTSTART",""+getIsRunningTask()+" "+getCurrentTaskId()+" "+getTimeToAdd())
        if(getIsRunningTask()){
            if(getCurrentTaskId()!=-1&&getTimeToAdd()!=0){
                isTaskStarted=true
                currentTaskId=getCurrentTaskId()
                workingTime.value=getTimeToAdd()
            }

        }
        showOutdated= sharedPreferences.getBoolean(SHOW_OUTDATED,true)
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            AppCompatDelegate.setDefaultNightMode(sharedPreferences.getInt(MODE,-1 ))
        super.onCreate(savedInstanceState)
        oh = LOTDatabaseHelper(this)
        db = oh.readableDatabase

        if(getIsRunningTask()){
            val startTime=getStartTime()
            val time=getTimeToAdd()
            val id=getCurrentTaskId()
            oh.addOldstatRow(startTime,time,id)
        }

        getDataFromDB()
        colors.addListeners()
        tasks.addListeners()
        oldTasks.addListeners()

        queryArrayByPriority.value = getSortedByPriority(getQueryArrayByDate().value!!)
        queryArrayByDuration.value = getSortedByDuration(getQueryArrayByDate().value!!)

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
        getAvatar()


    }

    override fun onStart() {
        super.onStart()
        if(sharedPreferences.getBoolean(SETTINGS_CHANGE,false)) {
            navController.navigate(R.id.action_FirstFragment_to_settings)
            sharedPreferences.edit().putBoolean(SETTINGS_CHANGE,false).apply()
        }
        findViewById<LinearLayout>(R.id.settings).setOnClickListener {
            navController.navigate(R.id.action_FirstFragment_to_settings)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

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
        colors.removeListeners()
        tasks.removeListeners()
        oldTasks.removeListeners()
        // db.close()
        // oh.close()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        findViewById<TextView>(R.id.user_email).text=emailId
        findViewById<TextView>(R.id.user_name).text=userName
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
                    val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

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
                        if (!calendar.before(Calendar.getInstance(TimeZone.getTimeZone("UTC"))))
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
                if (dataRowWithColor.date.before(Calendar.getInstance(TimeZone.getTimeZone("UTC"))))
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
    fun getOldData():ArrayList<Triple<Calendar,Float,String>>{
        val selectionQuery = "SELECT OLDSTATS.date_id, OLDSTATS.working_time, OLDSTATS.category from OLDSTATS "
        val c: Cursor = db.rawQuery(selectionQuery, null)
        val map:MutableMap<Calendar,Pair<Float,String>> = java.util.HashMap()
        val list:ArrayList<Triple<Calendar,Float,String>> = arrayListOf()
        if(c.moveToFirst())
            do {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                cal.time=Date(c.getLong(0))
                cal.set(Calendar.HOUR,0)
                cal.set(Calendar.MINUTE,0)
                cal.set(Calendar.SECOND,0)
                cal.set(Calendar.MILLISECOND,0)
                map.putIfAbsent(cal,Pair(c.getInt(1).toFloat()/3600,c.getString(2)))?.let { map.replace(cal,Pair(it.first+c.getInt(1).toFloat()/3600,c.getString(2))) }

            }while (c.moveToNext())
        c.close()
        for (element in map)
            list.add(Triple(element.key,element.value.first,element.value.second))


        return list

    }

    fun getOldDataWithColors(currentDay:Boolean):ArrayList<NTuple4<Calendar, Int, String, String>>{
        val selectionQuery = "SELECT OLDSTATS.date_id, OLDSTATS.working_time, OLDSTATS.category, COLOR.color from OLDSTATS  JOIN COLOR ON OLDSTATS.category=COLOR.category_id"
        val c: Cursor = db.rawQuery(selectionQuery, null)
        val map:MutableMap<Calendar,Triple<Int,String,String>> = java.util.HashMap()
        val list:ArrayList<NTuple4<Calendar, Int, String, String>> = arrayListOf()
        val calC=Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        if(c.moveToFirst())
            do {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                cal.time=Date(c.getLong(0))
                if(!currentDay||(cal.get(Calendar.YEAR)==calC.get(Calendar.YEAR)&&cal.get(Calendar.MONTH)==calC.get(Calendar.MONTH)&&cal.get(Calendar.DAY_OF_MONTH)==calC.get(Calendar.DAY_OF_MONTH))) {
                    cal.set(Calendar.HOUR, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    map.putIfAbsent(
                        cal,
                        Triple(c.getInt(1), c.getString(2), c.getString(3))
                    )?.let {
                        map.replace(
                            cal,
                            Triple(
                                it.first + c.getInt(1),
                                c.getString(2),
                                c.getString(3)
                            )
                        )
                    }
                }
            }while (c.moveToNext())
        c.close()
        for (element in map)
            list.add(NTuple4(element.key,element.value.first,element.value.second,element.value.third))


        return list

    }


/*
    fun createNotificationChannel(descript: String){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val importance=NotificationManager.IMPORTANCE_HIGH
            val channel =NotificationChannel(WIDGET_ID,WIDGET_NAME,importance).apply {
                description=descript
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
    fun displayNotification(){
        contentView = RemoteViews(packageName, R.layout.widget_layout)
        contentView.setTextViewText(R.id.hour,String.format("%d:%d",1,1))
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this,WIDGET_ID)
            .setContent(contentView)
            .setContentTitle("Tytul")
            .setContentText("TekstZawartosci")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notification = mBuilder.build()
        notificationManager.notify(WIDGET_ID.toInt(), notification)

    }
    fun updateNotification(hours:Int,minutes:Int,seconds:Int){
            contentView.setTextViewText(R.id.hour,String.format("%d:%d:%d",hours,minutes,seconds))
            notificationManager.notify(WIDGET_ID.toInt(), notification)


    }
    fun removeNotification(){
        notificationManager.cancel(WIDGET_ID.toInt())
    }*/

}