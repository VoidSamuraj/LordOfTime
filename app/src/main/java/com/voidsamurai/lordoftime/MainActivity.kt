package com.voidsamurai.lordoftime

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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
import com.voidsamurai.lordoftime.bd.*
import com.voidsamurai.lordoftime.charts_and_views.NTuple4
import com.voidsamurai.lordoftime.databinding.ActivityMainBinding
import com.voidsamurai.lordoftime.fragments.HomeFragment
import com.voidsamurai.lordoftime.fragments.WorkingFragment
import com.voidsamurai.lordoftime.fragments.dialogs.RepeatDialog
import com.voidsamurai.lordoftime.fragments.dialogs.YourDaysDialog
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var repeatDialog: RepeatDialog

    companion object{
        const val isMondayFirstDay=true
        const val SHARED_PREFERENCES:String="sharedPreferences"
        fun String.formatToFloat():Float{
            // return
            val str=toString()
                .replace(',','.')
                .replace('-','.')
            if(str.startsWith('.'))
                return str.substring(1).toFloat()
            return  str.toFloat()
        }

    }


    var timeToEndTask:Long=0L
    lateinit var homeDrawable: Drawable
    private lateinit var firebaseDB:FirebaseDatabase
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var storageReference:StorageReference
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
    var notificationService:BackgroundTimeService=BackgroundTimeService()
    var isTaskStarted:Boolean=false
    //var isFromEditFragment:Boolean=false
    var isFromWorkFragment:Boolean=false
    var isFromCalendarFragment:Boolean=false
    var currentTaskId:Int?=null
    var currentTaskCategory:String?=null
    var lastTaskId:Int?=null
    var lastTaskPositioon:Int?=null
    var lastButton:ImageButton?=null
    var userId:String?=null
    var emailId:String?=null
    var userName:String?=null
    var userImage:Bitmap?=null
    lateinit var  colors: DAOColors
    lateinit var  tasks: DAOTasks
    lateinit var  rutines: DAORutines
    lateinit var  oldTasks: DAOOldTasks
    lateinit var settings: DAOSettings
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
    private  var rutinesArray: MutableLiveData<Map<Int,RutinesRow>> = MutableLiveData()
    fun getCurrentWorkingTime()= workingTime
    fun getQueryArrayByDate()= queryArrayByDate
    fun getQueryArrayByPriority()= queryArrayByPriority
    fun getQueryArrayByDuration()= queryArrayByDuration
    fun getColors()= colorsArray
    fun getRutines()= rutinesArray
    fun getDBOpenHelper():LOTDatabaseHelper= oh
    lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient:GoogleSignInClient
    var change:Triple<Boolean,Boolean,Boolean> = Triple(false, false, false)

    val homeFabPosition:IntArray=IntArray(2)


    fun logout(){
        sharedPreferences.edit().putBoolean("logged_in",false).apply()
    }

    fun getCurrentUserId():String{
        return sharedPreferences.getString("user_id","")?:""
    }



    fun getSettings(sd:SettingsData) {

        if (getIsUserChanged()){

            sd.show_completed?.let { setIsShowingCompleted(it) }
            sd.delete_completed?.let { setIsDeletingCompleted(it) }
            sd.show_outdated?.let { setOutdated(it) }
            sd.main_chart_auto?.let {
                sharedPreferences.edit().putBoolean("MAIN_CHART_AUTO", it).apply()
            }
            sd.main_chart_aim?.let { setMainChartRange(it) }
            sd.death_date?.let {setYourTime(it) }


            var modeChanged = false
            var languageChanged = false
            sd.mode?.let {
                sharedPreferences.edit().putInt(MODE, it).putBoolean(SETTINGS_CHANGE, true).apply()
                modeChanged = true
            }
            sd.language?.let {
                setLanguage(it)
                languageChanged = true
            }
            sharedPreferences.edit().putBoolean("IS_USER_CHANGED",false).apply()
            if (modeChanged && (!languageChanged))
                newIntent()

        }
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
    fun setTaskCategory(category:String){
        sharedPreferences.edit().putString("TASK_CATEGORY",category).apply()
    }
    fun getTaskCategory():String{
        return sharedPreferences.getString("TASK_CATEGORY","")?:""
    }
    fun setIsRunningTask(isRunning: Boolean){
        sharedPreferences.edit().putBoolean("IS_RUNNING_TASK",isRunning).apply()
    }
    fun getIsRunningTask():Boolean{
        return sharedPreferences.getBoolean("IS_RUNNING_TASK",false)
    }
    fun getIsUserChanged():Boolean{
        return sharedPreferences.getBoolean("IS_USER_CHANGED",false)
    }
    fun setStartTime(startTime: Long){
        sharedPreferences.edit().putLong("START_TIME",startTime).apply()
    }
    fun getStartTime():Long{
        return sharedPreferences.getLong("START_TIME",Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time)
    }
    fun setYourTime(startTime: Long){
        sharedPreferences.edit().putLong("LIFE_TIME",startTime).apply()
    }
    fun getYourTime():Long{
        return sharedPreferences.getLong("LIFE_TIME",0L)
    }

    fun setIsDeletingCompleted(isDeleting:Boolean){
        sharedPreferences.edit().putBoolean("IS_DELETING_COMPLETED",isDeleting).apply()
    }
    fun getIsDeletingCompleted():Boolean{
        return sharedPreferences.getBoolean("IS_DELETING_COMPLETED",false)
    }
    fun setIsShowingCompleted(isShowing:Boolean){
        sharedPreferences.edit().putBoolean("IS_SHOWING_COMPLETED",isShowing).apply()
    }
    fun getIsShowingCompleted():Boolean{
        return sharedPreferences.getBoolean("IS_SHOWING_COMPLETED",true)
    }
    fun setMainChartAuto():Boolean{
        val state=getIsMainChartManual().not()
        settings.add(main_chart_auto = state)
        sharedPreferences.edit().putBoolean("MAIN_CHART_AUTO",state).apply()
        return state
    }
    fun getIsMainChartManual():Boolean{
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



    /**
     * Sets light styles
     *
     * @param style
     * -1 default mode
     * 1 light mode
     * 2 dark mode
     * */
    fun setStyle(style:Int){
        val sharedPreferences:SharedPreferences=application.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)
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
        CoroutineScope(Dispatchers.IO).launch {
            val localData: Map<String, String> = colorsArray.value!!
            for (row in data) {
                if (localData.containsKey(row.key)) {
                    if (localData[row.key] != row.value) {
                        oh.editColorRow(row.key, row.key, row.value,userId)
                        change = Triple(true, change.second, change.third)
                        refreshHomeFragmentAndDB()
                    }
                } else {
                    oh.addColorRow(row.key, row.value,userId)
                    change = Triple(true, change.second, change.third)
                    refreshHomeFragmentAndDB()
                }
            }
        }
    }

    fun updateLocalRutinesDB(data:ArrayList<RutinesRow>){
        CoroutineScope(Dispatchers.IO).launch {//
            val localData: Map<Int, RutinesRow> = getRutines().value!!
            for (row in data) {
                if (localData.containsKey(row.id)) {
                    if (localData[row.id]!!.task_id != row.task_id) {
                        oh.editRutinesRow(row.id, row.task_id, row.days, row.hours,userId)
                        refreshHomeFragmentAndDB()
                    }
                } else {
                    val id = oh.addRutinesRow(
                        row.task_id,
                        row.days,
                        row.hours,
                        userId
                    )                         //update id got in local db cloud
                    rutines.delete(row.id)
                    rutines.add(
                        id.toInt(),
                        row.task_id,
                        row.days,
                        row.hours
                    )
                    refreshHomeFragmentAndDB()
                }
            }
        }
    }

    fun updateLocalTaskDB(dataFromFirebase:ArrayList<DataRow>){
        CoroutineScope(Dispatchers.IO).launch {
            val localData: MutableList<DataRowWithColor> = queryArrayByDate.value!!
            fun DataRow.compareById(array: MutableList<DataRowWithColor>): Boolean {
                for (el in array)
                    if (el.id == this.id)
                        return true
                return false
            }

            fun DataRow.compareByRestAndId(array: MutableList<DataRowWithColor>): Boolean {
                for (el in array) {
                    if (el.id == this.id && (el.name != this.name || el.category != this.category || el.date.time.time != this.date.time.time))
                        return true

                }
                return false
            }
            for (row in dataFromFirebase) {
                if (row.compareByRestAndId(localData)) {

                    oh.editTaskRow(
                        row.id,
                        row.category,
                        row.name,
                        row.date.time.time,
                        row.workingTime.toInt(),
                        row.priority,
                        row.currentWorkingTime.toInt(),
                        row.finished
                    )
                    change = Triple(change.first, true, change.third)
                    refreshHomeFragmentAndDB()

                } else if (!row.compareById(localData)) {
                    row.let {
                        val id: Long = oh.addTaskRow(
                            it.category,
                            it.name,
                            it.date.time.time,
                            it.workingTime.toInt(),
                            it.priority,
                            it.currentWorkingTime.toInt(),
                            userId
                        )
                        if (id != -1L) {
                            tasks.delete(row.id.toString())
                            tasks.add(
                                id.toInt(),
                                it.category,
                                it.name,
                                it.date.time.time,
                                it.workingTime.toInt(),
                                it.priority,
                                it.currentWorkingTime.toInt(),
                                it.finished
                            )
                            change = Triple(change.first, true, change.third)
                            refreshHomeFragmentAndDB()
                        }
                    }


                }
            }
        }
    }

    fun updateLocalOldTaskDB(data:ArrayList<OldData>){
        CoroutineScope(Dispatchers.IO).launch {
            val data2 = data.map {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                cal.time = Date(it.date_id)

                OldData(cal.time.time, it.category, it.workingTime)
            } as ArrayList<*>

            val localData: List<Triple<Long, Float, String>> =
                getOldData().map { Triple(it.first.time.time, it.second, it.third) }

            fun OldData.compareID(array: List<Triple<Long, Float, String>>): Boolean {
                var isGood = false
                for (el in array) {
                    if (el.first == this.date_id)
                        isGood = true
                    //   Log.v("Testy",""+el.first+" "+this.date_id+" "+isGood)
                }
                return isGood
            }

            fun OldData.compareTimeWithId(array: List<Triple<Long, Float, String>>): Boolean {
                for (el in array)
                    if (el.first == this.date_id && el.second != this.workingTime)
                        return true
                return false
            }
            for (row in data2) {
                row as OldData
                if (row.compareTimeWithId(localData)) {
                    oh.editOldstatRow(
                        row.date_id,
                        row.date_id,
                        row.workingTime.toInt(),
                        row.category,
                        userId
                    )
                    change = Triple(change.first, change.second, true)
                    refreshHomeFragmentAndDB()
                } else if (!row.compareID(localData)) {
                    oh.addOldstatRow(row.date_id, row.workingTime.toInt(), row.category,userId)
                    change = Triple(change.first, change.second, true)
                    refreshHomeFragmentAndDB()
                }
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
        bmp!!.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()


/*
        try{
            val fo=openFileOutput(userId+".jpeg", MODE_PRIVATE)
            fo.write(byteArray)
            fo.close()
        }catch (e:Exception){
            Log.e("IMAGE_INTERNAL_SAVING", e.cause.toString())
        }*/

        userImage=bmp

        mainFragmentBinding.navView.getHeaderView(0)
            .findViewById<ImageView>(R.id.user_avatar).setImageURI(uri)
        storageReference.putFile(uri)
            .addOnSuccessListener { Toast.makeText(this, resources.getString(R.string.avatar_changed), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { Log.e("AVATAR",it.cause.toString())
            }
        oh.addAvatar(userId,byteArray)

    }
    fun getAvatar(){

        // val f=File(userId+".jpeg")

        // CoroutineScope(Dispatchers.IO).launch {
        val optional=oh.getAvatar(userId)


        /*
         if (f.exists()) {
             val fo = openFileInput(f.path)
             bitmap = BitmapFactory.decodeStream(fo)
         }*/
        // }
        if(optional.isPresent){
            val bitmap: Bitmap=optional.get()
            userImage=bitmap
            mainFragmentBinding.navView.getHeaderView(0)
                .findViewById<ImageView>(R.id.user_avatar).setImageBitmap(bitmap)
        }else{
            val file = File.createTempFile("template", ".jpg")
            storageReference.getFile(file).addOnSuccessListener {

                val bmp:Bitmap? = BitmapFactory.decodeStream(FileInputStream(file), null, null)
                val stream = ByteArrayOutputStream()
                bmp!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                userImage = bmp

                mainFragmentBinding.navView.getHeaderView(0)
                    .findViewById<ImageView>(R.id.user_avatar).setImageBitmap(bmp)

                try{
                    CoroutineScope(Dispatchers.IO).launch {
                        /* val fo=openFileOutput(userId+".jpeg", MODE_PRIVATE)
                         fo.write(stream.toByteArray())
                         fo.close()*/
                    }
                }catch (e:Exception){
                    Log.e("IMAGE_INTERNAL_SAVING", e.cause.toString())
                }
            }


        }

    }

    fun setLanguage(language:String){
        val languageToLoad = when(language){
            "DEFAULT","DOMYŚLNIE"-> Locale.getDefault().language
            "PL"-> "pl"
            "EN"->"en_EN"
            else->{"en"}
        }
        application.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit().putBoolean(SETTINGS_CHANGE,true).apply()

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
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)

        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        sharedPreferences=application.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)

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
        if(userId==null){
            userId=getCurrentUserId()
        }
        userId?.let {                        getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit().putString("user_id",it).apply()
        }
        if(userId.isNullOrBlank()||userName.isNullOrBlank()){
            auth.currentUser.let {
                userId = auth.uid
                it?.email.let{itt-> emailId =itt}


                it?.displayName.let{ itt->userName =itt}
            }
        }

        firebaseDB=Firebase.database
        colors=DAOColors(this)
        tasks= DAOTasks(this)
        oldTasks= DAOOldTasks(this)
        rutines= DAORutines(this)
        settings= DAOSettings(this)
        storageReference=FirebaseStorage.getInstance().reference.child("profileImages").child(userId!!+".jpg")



        showOutdated= sharedPreferences.getBoolean(SHOW_OUTDATED,true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            AppCompatDelegate.setDefaultNightMode(sharedPreferences.getInt(MODE,-1 ))
        super.onCreate(savedInstanceState)
        oh = LOTDatabaseHelper(this)
        db = oh.readableDatabase

        val timeToadd=Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis-getStartTime()

        if(getIsRunningTask()){
            if(getCurrentTaskId()!=-1&&timeToadd!=0L){
                isTaskStarted=true
                currentTaskId=getCurrentTaskId()
                val row=oh.getTaskRow(currentTaskId!!,userId)
                currentTaskCategory=getTaskCategory()
                val wt=(timeToadd/1000).toInt()
                workingTime.value=wt
                // oh.addOldstatRow(getStartTime(), workingTime.value!!,getCurrentTaskId())
                setStartTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis)
                Log.v("TIME",""+wt)

                updateOldstats(wt+(row.currentWorkingTime*3600).toInt(),wt)
            }

        }


        if(getIsDeletingCompleted())
            deleteCompleted()

        getDataFromDB()
        colors.addListeners()
        tasks.addListeners()
        oldTasks.addListeners()
        rutines.addListeners()
        settings.addListeners()

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

        if((getYourTime()-Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis)<0L){
            val ydd= YourDaysDialog()
            ydd.show(supportFragmentManager,"Memento_Mori")

        }
        val menu: Menu = mainFragmentBinding.navView.menu
        val menuItem = menu.findItem(R.id.calendarEditFragment)
        val calendarItem = menu.findItem(R.id.manyCharts)
        menuItem.setOnMenuItemClickListener {

            drawerLayout.closeDrawer(GravityCompat.START)

            MainScope().launch {
                delay(200)
                val nhf = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val hf= nhf.childFragmentManager.fragments[0] as HomeFragment
                hf.homeFragmentBinding.taskChangerFAB.performClick()

            }
            true
        }
        calendarItem.setOnMenuItemClickListener {

            drawerLayout.closeDrawer(GravityCompat.START)
            MainScope().launch {
                delay(200)
                val nhf = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val hf= nhf.childFragmentManager.fragments[0] as HomeFragment
                hf.homeFragmentBinding.card2Click.performClick()

            }
            true
        }

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
        requestPermissions()
       // createTodayNotifications()
       // deleteTodayNotifications()
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
        rutines.removeListeners()
        settings.removeListeners()
        // db.close()
        // oh.close()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        findViewById<TextView>(R.id.user_email).text=emailId
        findViewById<TextView>(R.id.user_name).text=userName
        fillMementoMori()
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    fun fillMementoMori() {
        val mTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        if (getYourTime() != 0L){
            mTime.time = Date(getYourTime())

            var mYears = mTime.get(Calendar.YEAR) - now.get(Calendar.YEAR)
            var mMonths = mTime.get(Calendar.MONTH) - now.get(Calendar.MONTH)
            if (mMonths < 0) {
                mMonths += 12
                --mYears
            }


            var mDays = mTime.get(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_MONTH)



            if (mDays < 0) {
                mDays =
                    (mTime.get(Calendar.DAY_OF_MONTH) + now.getActualMaximum(Calendar.DAY_OF_MONTH)) - now.get(
                        Calendar.DAY_OF_MONTH
                    )
                --mMonths
                if (mMonths < 0) {
                    mMonths += 12
                    --mYears
                }
            }

            findViewById<TextView>(R.id.year_val).text = mYears.toString()
            findViewById<TextView>(R.id.month_val).text = mMonths.toString()
            findViewById<TextView>(R.id.day_val).text = mDays.toString()

            if (mYears == 1)
                findViewById<TextView>(R.id.year_label).text = resources.getText(R.string.year)

            if (mMonths == 1)
                findViewById<TextView>(R.id.month_label).text = resources.getText(R.string.month)
            if (mMonths in 3..4)
                findViewById<TextView>(R.id.month_label).text = resources.getText(R.string.months2)

            if (mDays == 1)
                findViewById<TextView>(R.id.day_label).text = resources.getText(R.string.day)
        }else{

            findViewById<TextView>(R.id.year_val).text ="0"
            findViewById<TextView>(R.id.month_val).text ="0"
            findViewById<TextView>(R.id.day_val).text = "0"
        }

    }
/*
    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.v("POKAZAC","IIIO")
        if(item.itemId==R.id.calendarEditFragment){

            val navHostFragment: NavHostFragment? =
                supportFragmentManager.findFragmentById(R.id.nav_view) as NavHostFragment?
            val hfb=(navHostFragment!!.childFragmentManager.fragments[0] as HomeFragment).homeFragmentBinding

            val background=hfb.homeFrag
            val intArray= IntArray(2)
            hfb.taskChangerFAB.getLocationOnScreen(intArray)
            intArray[0]= (intArray[0]+(resources.getDimension(R.dimen.fab_size)/2)/*resources.getDimension(R.dimen.fab_margin)+resources.getDimension(R.dimen.small_padding)*/).toInt()
            intArray[1]= (intArray[1]-resources.getDimension(R.dimen.bar_height)).toInt()


                homeFabPosition.let { itt ->
                    itt[0]=intArray[0]
                    itt[1]=intArray[1]

                homeDrawable=background.drawToBitmap().toDrawable(resources)

            }

        }
        return super.onContextItemSelected(item)
    }*/

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        navController.currentDestination?.label.let {
            when {
                drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
                /* it == "EditList" -> {
                     isFromEditFragment=true
                     super.onBackPressed()
                 }*/
                it == "WorkingFragment" -> {
                    isFromWorkFragment=true
                    super.onBackPressed()
                }
                it == "first_fragment_label" ->{
                    val nh= supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val fragment=nh.childFragmentManager.primaryNavigationFragment
                    if(fragment is HomeFragment){
                        val frag= fragment
                        if (!frag.isButtonHidden)
                            frag.homeFragmentBinding.view.performClick()
                    }else
                        super.onBackPressed()
                }
                it=="fragment_calendar_edit"->{
                    isFromCalendarFragment=true
                    super.onBackPressed()
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
                "SELECT TASKTABLE._id, TASKTABLE.category, TASKTABLE.name, TASKTABLE.datetime,TASKTABLE.working_time,TASKTABLE.priority, TASKTABLE.current_work_time, COLOR.color, TASKTABLE.is_finished " +
                        "FROM TASKTABLE LEFT JOIN COLOR on TASKTABLE.category=COLOR.category_id WHERE TASKTABLE.user_id=? OR TRIM(TASKTABLE.user_id) IS NULL ORDER BY TASKTABLE.datetime, TASKTABLE.priority DESC"

            val query: ArrayList<DataRowWithColor> = ArrayList()
            var c: Cursor = db.rawQuery(selectionQuery, arrayOf(userId))
            var array: Map<String, String> = HashMap()

            if (c.moveToFirst())
                do {
                    val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

                    calendar.time = Date(c.getLong(3))
                    if (showOutdated&&getIsShowingCompleted())
                        query.add(
                            DataRowWithColor(
                                c.getInt(0),
                                c.getString(1),
                                c.getString(2),
                                calendar,
                                c.getFloat(4),
                                c.getInt(5),
                                c.getFloat(6),
                                c.getString(7) ?: "#FFFFFF",
                                finished = c.getInt(8)
                            )
                        )
                    else if (getIsShowingCompleted()&&(!calendar.before(Calendar.getInstance(TimeZone.getTimeZone("UTC")))))
                        query.add(
                            DataRowWithColor(
                                c.getInt(0),
                                c.getString(1),
                                c.getString(2),
                                calendar,
                                c.getFloat(4),
                                c.getInt(5),
                                c.getFloat(6),
                                c.getString(7) ?: "#FFFFFF",
                                finished = c.getInt(8)
                            )
                        )
                    else if (c.getInt(8)==0&&(c.getFloat(4)-c.getFloat(6))>0&&showOutdated)
                        query.add(
                            DataRowWithColor(
                                c.getInt(0),
                                c.getString(1),
                                c.getString(2),
                                calendar,
                                c.getFloat(4),
                                c.getInt(5),
                                c.getFloat(6),
                                c.getString(7) ?: "#FFFFFF",
                                finished = c.getInt(8)
                            )
                        )
                    else if (c.getInt(8)==0&&(c.getFloat(4)-c.getFloat(6))>0&&(!calendar.before(Calendar.getInstance(TimeZone.getTimeZone("UTC")))))
                        query.add(
                            DataRowWithColor(
                                c.getInt(0),
                                c.getString(1),
                                c.getString(2),
                                calendar,
                                c.getFloat(4),
                                c.getInt(5),
                                c.getFloat(6),
                                c.getString(7) ?: "#FFFFFF",
                                finished = c.getInt(8)
                            )
                        )


                } while (c.moveToNext())
            c.close()
            c = db.rawQuery("SELECT * FROM COLOR WHERE COLOR.user_id=? OR TRIM(COLOR.user_id) IS NULL", arrayOf(userId))
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
            rutinesArray.value=oh.getUserRutinesArray(userId)
            queryArrayByDate.value = query
            queryArrayByPriority.value = getSortedByPriority(query)
            queryArrayByDuration.value = getSortedByDuration(query)


        }

    }


    private fun getSortedByDuration(qa: ArrayList<DataRowWithColor>): ArrayList<DataRowWithColor> {
        CoroutineScope(Dispatchers.IO).run {
            qa.sortWith(compareBy<DataRowWithColor> { it.workingTime }.thenBy { it.name })
            val na: ArrayList<DataRowWithColor> = ArrayList()
            na.addAll(qa.toList())
            qa.sortWith(compareBy<DataRowWithColor> { it.date.time.time }.thenByDescending { it.priority })
            return na
        }
    }

    private fun getSortedByPriority(querryArray:ArrayList<DataRowWithColor>):ArrayList<DataRowWithColor>{
        CoroutineScope(Dispatchers.IO).run {
            querryArray.sortWith(compareByDescending<DataRowWithColor> { it.priority }.thenBy { it.date.time.time })
            querryArray.reverse()
            val newArray: ArrayList<DataRowWithColor> = ArrayList()
            newArray.addAll(querryArray.toList())
            querryArray.sortWith(compareBy<DataRowWithColor> { it.date.time.time }.thenByDescending { it.priority })
            return newArray
        }
    }
    fun getOldData():ArrayList<Triple<Calendar,Float,String>> {
        CoroutineScope(Dispatchers.IO).run {
            val selectionQuery =
                "SELECT OLDSTATS.date_id, OLDSTATS.working_time, OLDSTATS.category from OLDSTATS WHERE OLDSTATS.user_id=? OR TRIM(OLDSTATS.user_id) IS NULL "
            val c: Cursor = db.rawQuery(selectionQuery, arrayOf(userId))
            val map: MutableMap<Calendar, Pair<Float, String>> = java.util.HashMap()
            val list: ArrayList<Triple<Calendar, Float, String>> = arrayListOf()
            if (c.moveToFirst())
                do {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    cal.time = Date(c.getLong(0))
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    map.putIfAbsent(cal, Pair(c.getInt(1).toFloat() / 3600, c.getString(2)))?.let {
                        map.replace(
                            cal,
                            Pair(it.first + c.getInt(1).toFloat() / 3600, c.getString(2))
                        )
                    }

                } while (c.moveToNext())
            c.close()
            for (element in map)
                list.add(Triple(element.key, element.value.first, element.value.second))


            return list
        }
    }

    /**
     * sets currentWorkingTime of CurrentTask
     * add row for old tasks
     * @param currentTime - summary time of  task to change in CurrentTask progress
     * @param timeToAdd - time to add in OldTasks table
     */
    fun updateOldstats(currentTime:Int, timeToAdd:Int){
        CoroutineScope(Dispatchers.IO).run {
            if(currentTime!=0&&currentTaskId!=null) {
                //update current task time

                //   if(this::oh.isInitialized){
                oh.editTaskRow(
                    currentTaskId!!,
                    null, null, null, 0, 0, currentTime,-1
                )

                tasks.update(currentTaskId!!,null,null,null,null,null,currentTime,-1)
                //add oldstats row
                //need also edit in firebase
                if (currentTaskCategory.isNullOrEmpty())
                    currentTaskCategory=getTaskCategory()
                timeToAdd.let {
                    oh.addOldstatRow(
                        Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time, it,
                        currentTaskCategory,
                        userId
                    )
                    oldTasks.add(
                        dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time.time,
                        currentWorkingTime = it,
                        category = currentTaskCategory!!
                    )
                }
            }
        }
        //    }
    }
    /**
     * @return ArrayList<Ntuple<Calendar,Int,String,String>>
     *         ArrayList<Ntuple<start_date, working_time_sec, category, color>>
     * */
    fun getOldDataWithColors(currentDay:Boolean):ArrayList<NTuple4<Calendar, Int, String, String>>{
        CoroutineScope(Dispatchers.IO).run {
            val selectionQuery =
                "SELECT OLDSTATS.date_id, OLDSTATS.working_time, OLDSTATS.category, COLOR.color from OLDSTATS  JOIN COLOR ON OLDSTATS.category=COLOR.category_id"
            val c: Cursor = db.rawQuery(selectionQuery, null)
            var map: MutableMap<Calendar, Triple<Int, String, String>> = java.util.HashMap()
            val list: ArrayList<NTuple4<Calendar, Int, String, String>> = arrayListOf()
            val calC = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            if (c.moveToFirst())
                do {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    cal.time = Date(c.getLong(0))
                    if ((!currentDay) || (cal.get(Calendar.YEAR) == calC.get(Calendar.YEAR) && cal.get(
                            Calendar.MONTH
                        ) == calC.get(Calendar.MONTH) && cal.get(Calendar.DAY_OF_MONTH) == calC.get(
                            Calendar.DAY_OF_MONTH
                        ))
                    ) {

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
                } while (c.moveToNext())
            c.close()
            if (currentDay)
                map = map.filterKeys { calendar ->
                    calendar.get(Calendar.DAY_OF_MONTH) == calC.get(Calendar.DAY_OF_MONTH)
                }.toMutableMap()
            for (element in map)
                list.add(
                    NTuple4(
                        element.key,
                        element.value.first,
                        element.value.second,
                        element.value.third
                    )
                )


            return list
        }
    }
    fun deleteCompleted(){
        CoroutineScope(Dispatchers.IO).run {
            val selectionQuery =
                "SELECT TASKTABLE._id, TASKTABLE.working_time, TASKTABLE.current_work_time, RUTINES._id FROM TASKTABLE LEFT JOIN RUTINES ON TASKTABLE._id=RUTINES.task_id "
            val c: Cursor = db.rawQuery(selectionQuery, null)
            val array: ArrayList<Int> = ArrayList()
            if (c.moveToFirst())
                do {
                    if ((c.getInt(1) - c.getInt(2)) < 0 && (c.isNull(3)))
                        array.add(c.getInt(0))

                } while (c.moveToNext())

            for (id in array) {
                oh.deleteTaskRow(id)
                tasks.delete(id)
            }

            c.close()
        }
    }



    fun updateRutines(){
        CoroutineScope(Dispatchers.IO).run {
            val selectionQuery =
                "SELECT TASKTABLE._id, TASKTABLE.datetime, RUTINES._id, RUTINES.days,RUTINES.hours,TASKTABLE.working_time FROM TASKTABLE JOIN RUTINES ON TASKTABLE._id=RUTINES.task_id "
            val c: Cursor = db.rawQuery(selectionQuery, null)
            val cCalendar = Calendar.getInstance(/*TimeZone.getTimeZone("UTC")*/)


/*        if (isMondayFirstDay)
            cCalendar.firstDayOfWeek=Calendar.MONDAY*/
            val dow = cCalendar.get(Calendar.DAY_OF_WEEK)
            val currentDayOfWeek = if (isMondayFirstDay) when (dow) {
                1 -> 7
                else -> dow - 1
            } else dow

            val currentHour = cCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = cCalendar.get(Calendar.MINUTE)
            var daysArray: List<Int>

            var hoursArray: List<String>

            fun getCalendarTime(
                calendar: Calendar,
                dayShort: Int,
                hour: Int,
                minute: Int,
                nextWeek: Boolean? = false
            ): Long {
                var day = dayShort
                if (isMondayFirstDay)
                    day = when (dayShort) {
                        7 -> 1
                        else -> dayShort + 1
                    }
                val cal = calendar.clone() as Calendar
                cal.set(Calendar.DAY_OF_WEEK, day)
                nextWeek.let {
                    if (nextWeek!!)
                        cal.add(Calendar.WEEK_OF_YEAR, 1)
                }
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                cal.timeZone = TimeZone.getTimeZone("UTC")

                return cal.timeInMillis
            }

            var anyChanges = false
            if (c.moveToFirst())
                do {
                    val localCal =
                        Calendar.getInstance()                      //błąd porównania strefy czasowej roznica 1h
                    localCal.timeInMillis = c.getLong(1) + (c.getLong(5) * 1000)
                    val durationMinutes = (c.getInt(5) / 60)
                    val durationHours = (durationMinutes / 60)



                    Log.v(
                        "Time",
                        "" + localCal.timeInMillis + " " + c.getLong(5) + "  " + (localCal.timeInMillis + c.getLong(
                            5
                        )) + " " + cCalendar.timeInMillis + " id:" + c.getInt(0) + " Timezone:" + localCal.timeZone.rawOffset
                    )
                    if ((localCal.timeInMillis/*-localCal.timeZone.rawOffset*/) < (cCalendar.timeInMillis + cCalendar.timeZone.rawOffset)
                    ) {

                        daysArray = c.getString(3).split(',')
                            .sortedWith(compareBy { DAORutines.getDayNr(it, isMondayFirstDay) })
                            .map {
                                DAORutines.getDayNr(
                                    it,
                                    isMondayFirstDay
                                )
                            }//.sortedWith(compareBy{DAORutines.getDayNr(it)})
                        if (isMondayFirstDay) {
                            daysArray.sorted()
                        }
                        hoursArray = c.getString(4).split(',')
                            .sortedWith(compareBy { it: String -> it.split(':')[0].toInt() }.thenBy { it: String ->
                                it.split(':')[1].toInt()
                            })


                        val daysBefore = daysArray.filter { it < currentDayOfWeek }
                        val daysAfter = daysArray.filter { it > currentDayOfWeek }
                        val today = daysArray.filter { it == currentDayOfWeek }

                        var changed = false

                        if (!today.isNullOrEmpty())
                            hoursArray.forEach {
                                if (!changed) {
                                    val time = it.split(':')
                                    val hour = time[0].toInt()
                                    val minute = time[1].toInt()

                                    // Log.v("TIMES_TODAY,", "" + currentHour + "/" + hour + " " + currentMinute + "/" + minute +" dur"+durationHours+":"+durationMinutes+ " id:" + c.getInt(0))

                                    if (currentHour < (hour + durationHours) || (currentHour == (hour + durationHours) && currentMinute < (minute + durationMinutes))) {
                                        val newDate =
                                            getCalendarTime(cCalendar, today[0], hour, minute)
                                        oh.editTaskRow(
                                            c.getInt(0),
                                            null,
                                            null,
                                            newDate,
                                            0,
                                            0,
                                            -1,
                                            0
                                        )
                                        tasks.update(
                                            c.getInt(0),
                                            dateTime = newDate,
                                            workingTime = 0
                                        )
                                        changed = true
                                        anyChanges = true
                                    }
                                }
                            }

                        if ((!changed) && (!daysAfter.isNullOrEmpty())) {
                            hoursArray.forEach {
                                if (!changed) {
                                    val time = it.split(':')
                                    val hour = time[0].toInt()
                                    val minute = time[1].toInt()
                                    val newDate =
                                        getCalendarTime(cCalendar, daysAfter[0], hour, minute)
                                    Log.v(
                                        "TIMES_AFTER,",
                                        "" + currentHour + "/" + hour + " " + currentMinute + "/" + minute + " id:" + c.getInt(
                                            0
                                        )
                                    )
                                    oh.editTaskRow(c.getInt(0), null, null, newDate, 0, 0, -1, 0)
                                    tasks.update(c.getInt(0), dateTime = newDate, workingTime = 0)
                                    changed = true
                                    anyChanges = true
                                }
                            }
                        }
                        if ((!changed) && (!daysBefore.isNullOrEmpty())) {
                            hoursArray[0].let {
                                val time = it.split(':')
                                val hour = time[0].toInt()
                                val minute = time[1].toInt()
                                Log.v(
                                    "TIMES_BEFORE,",
                                    "" + currentHour + "/" + hour + " " + currentMinute + "/" + minute + " id:" + c.getInt(
                                        0
                                    )
                                )
                                val newDate =
                                    getCalendarTime(cCalendar, daysBefore[0], hour, minute, true)
                                oh.editTaskRow(c.getInt(0), null, null, newDate, 0, 0, -1, 0)
                                tasks.update(c.getInt(0), dateTime = newDate, workingTime = 0)
                                changed = true
                                anyChanges = true
                            }

                        }
                        if ((!changed) && (!today.isNullOrEmpty()) && (!hoursArray.isNullOrEmpty())) {
                            val time = hoursArray[0].split(':')
                            val hour = time[0].toInt()
                            val minute = time[1].toInt()
                            val newDate = getCalendarTime(cCalendar, today[0], hour, minute, true)
                            //val ca = Calendar.getInstance(
                            //    TimeZone.getTimeZone("UTC")
                            // )
                            // ca.timeInMillis = newDate
                            Log.v(
                                "TIMES_TODAY_BEFORE,",
                                "" + currentHour + "/" + time[0] + " " + currentMinute + "/" + time[1] + " id:" + c.getInt(
                                    0
                                )/*+" new Time"+ca.get(Calendar.DAY_OF_MONTH)*/
                            )
                            oh.editTaskRow(c.getInt(0), null, null, newDate, 0, 0, -1, 0)
                            tasks.update(c.getInt(0), dateTime = newDate, workingTime = 0)
                            changed = true
                            anyChanges = true
                        }
                    }
                } while (c.moveToNext())
            if (anyChanges)
                getDataFromDB()
            c.close()
        }
    }


    private fun requestPermission(permission:String, onSuccess:()->Unit,onFailure:()->Unit) {
        if(
            ContextCompat.checkSelfPermission(this, permission)
            == PackageManager.PERMISSION_DENIED ){
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted)
                    onSuccess()
                else
                    onFailure()
            }.launch(permission)

        }
    }




    private fun requestPermissions(){
        CoroutineScope(Dispatchers.Default).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && (!sharedPreferences.getBoolean(
                    "dont_show " + Manifest.permission.FOREGROUND_SERVICE,
                    false
                ))
            )
                requestPermission(Manifest.permission.FOREGROUND_SERVICE, {}, {
                    // createDialog(Manifest.permission.FOREGROUND_SERVICE)
                })


            if (!sharedPreferences.getBoolean("dont_show " + Manifest.permission.INTERNET, false))
                requestPermission(Manifest.permission.INTERNET, {}, {
                    //  createDialog(Manifest.permission.INTERNET)
                })
/*
            if (!sharedPreferences.getBoolean("dont_show " + Manifest.permission.READ_EXTERNAL_STORAGE, false))
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, {

                }, {
                 //   createDialog(Manifest.permission.READ_EXTERNAL_STORAGE)
                })
*/




        }
    }


    fun startTaskNotification(timeToAlarm:Long,taskId:Int,taskName:String){
        val myIntent = Intent(this, TimeBroadcastReceiver::class.java)
        myIntent.putExtra("taskId",taskId).putExtra("taskName",taskName)
        val pendingIntent = PendingIntent.getBroadcast(
            this.applicationContext, taskId, myIntent, 0)
        val alarmManager:AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setExact(AlarmManager.RTC, timeToAlarm, pendingIntent)

    }
    fun startFinishedNotification(taskId:Int,taskName:String){
        val myIntent = Intent(this, TimeBroadcastReceiver::class.java)
        myIntent.putExtra("taskId",taskId).putExtra("taskName",taskName).putExtra("finished",true)
        val pendingIntent = PendingIntent.getBroadcast(
            this.applicationContext, taskId, myIntent, 0)            //multiply maybe not necessary
        val alarmManager:AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setExact(AlarmManager.RTC, 1, pendingIntent)

    }

    fun createTodayNotifications(){
        val data=oh.getServiceTaskInfo(userId)
        val now=Calendar.getInstance().timeInMillis+   TimeZone.getDefault().rawOffset

        for(row in data){
            val time=row.third-now
            Log.v("TIMETO",""+time+" "+row.third+" "+now+" "+TimeZone.getDefault().rawOffset)
            if(time>0)
                startTaskNotification(time,row.first,row.second)
        }

    }
    fun deleteTodayNotifications(){

        val alarmManager:AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val data=oh.getServiceTaskInfo(userId)
        for(row in data){
            val myIntent = Intent(this, TimeBroadcastReceiver::class.java)
            myIntent.putExtra("taskId",row.first).putExtra("taskName",row.second)
            val pendingIntent = PendingIntent.getBroadcast(
                this.applicationContext, taskId, myIntent, 0)
            alarmManager.cancel(pendingIntent)
        }

    }

}
private fun createNotificationChannel(taskId: Int,taskName: String):NotificationChannel{
    val importance=NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(taskId.toString(),taskName,importance).apply {
        description="LOT Task Notification"
    }
    return channel

}
private fun displayTaskNotification(taskId: Int,taskName:String,title: String,taskDescription: String,context: Context){

    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context, 0 /* Request code */, intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    //val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val notification=NotificationCompat.Builder(context,taskId.toString())
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(taskDescription)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
    val nm=NotificationManagerCompat.from(context)
    nm.createNotificationChannel(createNotificationChannel(taskId, taskName))
    nm.notify(taskId,notification.build())
    MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI).start()

}

class TimeBroadcastReceiver : BroadcastReceiver() {
    // var mp: MediaPlayer? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        val taskId=intent!!.getIntExtra("taskId",0)
        val mode= intent.getBooleanExtra("finished",false)
        val taskName= intent.getStringExtra("taskName")?:"TASK NAME"
        //val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        /*mp = MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI)
        mp!!.start()*/
        if(mode)
            displayTaskNotification(taskId,taskName,context!!.resources.getText(R.string.task_completed).toString(),taskName ,
                context
            )
        else
            displayTaskNotification(taskId,taskName,context!!.resources.getText(R.string.task_is_waiting).toString(),context.resources.getText(R.string.time_to_task).toString()+" "+taskName ,
                context
            )

        // Toast.makeText(context, "Alarm....", Toast.LENGTH_SHORT).show()
    }
}





