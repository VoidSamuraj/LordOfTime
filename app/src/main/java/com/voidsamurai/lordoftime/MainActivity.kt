package com.voidsamurai.lordoftime

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


class MainActivity : AppCompatActivity() {

    private lateinit var firebaseDB:FirebaseDatabase
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var storageReference:StorageReference

    private val LANGUAGE:String="Language"
    private val SHOW_OUTDATED:String="showOutdated"
    private val SETTINGS_CHANGE:String="changeColor"
    private val MODE:String="Mode"
    private val SHARED_PREFERENCES:String="sharedPreferences"
    private var _mainFragmentBinding: ActivityMainBinding?=null
    private val mainFragmentBinding get()=_mainFragmentBinding!!
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var inflater: LayoutInflater
    private lateinit var navController: NavController
    var showOutdated:Boolean =true
    fun setOutdated(boolean: Boolean){
        showOutdated=boolean
        sharedPreferences.edit().putBoolean(SHOW_OUTDATED,boolean).apply()
    }
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
    fun updateLocalColorDB(data:Map<String,String>){
        val localData:Map<String,String> = colorsArray.value!!
        for(row in data){
            if(localData.containsKey(row.key)){
                if (localData[row.key]!=row.value)
                    oh.editColorRow(row.key,row.key,row.value)
            }
            else
                oh.addColorRow(row.key,row.value)
        }

    }
    fun updateLocalTaskDB(data:ArrayList<DataRow>){
        val localData:MutableList<DataRowWithColor> = queryArrayByDate.value!!
        fun DataRow.compareById(array: MutableList<DataRowWithColor>):Boolean{
            for(el in array)
                if(el.id==this.id)
                    return true
            return false
        }
        fun DataRow.compareByRest(array: MutableList<DataRowWithColor>):Boolean{
            for(el in array)
                if(el.name==this.name || el.category==this.category || el.date.time.time==this.date.time.time)
                    return true
            return false
        }
        for(row in data) {
            if (row.compareById(localData)) {
                if (row.compareByRest(localData)) {
                    oh.editTaskRow(
                        row.id,
                        row.category,
                        row.name,
                        row.date.time.time,
                        row.workingTime.toInt(),
                        row.priority,
                        row.currentWorkingTime.toInt()
                    )
                }
            } else{
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
                    }
                }


            }
        }

    }

    fun updateLocalOldTaskDB(data:ArrayList<OldData>){
        val localData:List<Triple<Long,Float,String>> = getOldData().map { Triple(it.first.time.time,it.second,it.third) }
        fun OldData.compareID(array: List<Triple<Long,Float,String>>):Boolean{
            for(el in array)
                if(el.first.toInt()==this.date_id)
                    return true
            return false
        }
        fun OldData.compareTime(array: List<Triple<Long,Float,String>>):Boolean{
            for(el in array)
                if(el.second==this.workingTime)
                    return true
            return false
        }
        for(row in data) {
            if (row.compareID(localData))
                if(row.compareTime(localData))
                    oh.editOldstatRow(row.date_id.toLong(),row.date_id.toLong(),row.workingTime.toInt(),row.category)
                else
                    oh.addOldstatRow(row.date_id.toLong(),row.workingTime.toInt(),row.category)
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

        if(oh.isAvatarRowExist(userId))
            oh.editAvatarRow(userId,byteArray)
        else
            oh.addAvatarRow(userId,byteArray)

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

        if(!oh.isAvatarRowExist(userId)) {
            val file = File.createTempFile("template", ".jpg")
            storageReference.getFile(file).addOnSuccessListener {

                val bmp = BitmapFactory.decodeStream(FileInputStream(file), null, null)
                val stream = ByteArrayOutputStream()
                bmp!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                userImage = bmp

                mainFragmentBinding.navView.getHeaderView(0)
                    .findViewById<ImageView>(R.id.user_avatar).setImageBitmap(bmp)
            }
        } else{
            val array = oh.getAvatarRow(userId)
            val bmp = BitmapFactory.decodeByteArray(array, 0, array.size)
            userImage=bmp
            mainFragmentBinding.navView.getHeaderView(0)
                .findViewById<ImageView>(R.id.user_avatar).setImageBitmap(bmp)
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
        showOutdated= sharedPreferences.getBoolean(SHOW_OUTDATED,true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            AppCompatDelegate.setDefaultNightMode(sharedPreferences.getInt(MODE,-1 ))
        super.onCreate(savedInstanceState)
        oh = LOTDatabaseHelper(this)
        db = oh.readableDatabase

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





    override fun onResume() {
        super.onResume()
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
    fun getOldData():ArrayList<Triple<Calendar,Float,String>>{
        val selectionQuery = "SELECT OLDSTATS.date_id, OLDSTATS.working_time, OLDSTATS.category from OLDSTATS "
        val c: Cursor = db.rawQuery(selectionQuery, null)
        val map:MutableMap<Calendar,Pair<Float,String>> = java.util.HashMap()
        val list:ArrayList<Triple<Calendar,Float,String>> = arrayListOf()
        if(c.moveToFirst())
            do {
                val cal = Calendar.getInstance()
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

}