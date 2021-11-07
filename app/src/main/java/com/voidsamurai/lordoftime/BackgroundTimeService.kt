package com.voidsamurai.lordoftime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import android.app.PendingIntent




class BackgroundTimeService :Service() {
    companion object{
        private var startTime:Int=0

    }

  //  private var wakeLock: PowerManager.WakeLock? = null
    val WIDGET_ID:String="1"
    private val WIDGET_NAME="LOTR"

    private lateinit var sP : SharedPreferences
    private lateinit var  contentView:RemoteViews
    private lateinit var notification: Notification
    private lateinit var notificationManager:NotificationManager
    private var isRunning:Boolean=true

    /**
     * @param startTime - time in seconds
     */
    fun setTime(startTime:Int){BackgroundTimeService.startTime=startTime}

    fun setIsRunning(isRunning:Boolean){this.isRunning=isRunning }

    override fun onCreate() {
        sP=getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel("Opis")
       /* if((!this::sP.isInitialized)&&this.intent!=null)
            sP.edit().putString("INTENT_URI", this.intent!!.toUri(0)).apply()*/

        super.onCreate()
    }
    /*
    fun setIntent(intent:Intent){
        this.intent=intent
    }*/

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
/*
        if((!this::sP.isInitialized)&&this.intent!=null)
            sP.edit().putString("INTENT_URI", this.intent!!.toUri(0)).apply()
        */
        if(sP.getBoolean("IS_RUNNING_TASK",false))
            startTime=sP.getInt("TIME_TO_ADD",startTime)


       /* wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }*/
     Thread{
         displayNotification()
         do {
             sP.edit().putInt("TIME_TO_ADD",startTime).apply()
             startTime++
             Thread.sleep(1000)
             updateNotification(startTime/3600,startTime/60%60,startTime%60)
         }while (isRunning)

         removeNotification()
         startTime=0
         Thread.currentThread().interrupt()
     }.start()
        return START_STICKY
    }

    override fun onDestroy() {
       // Log.v("WYJEBALO","APP")
        isRunning=false
      /*  wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }*/
        super.onDestroy()
    }

    fun createNotificationChannel(descript: String){
            val importance=NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(WIDGET_ID,WIDGET_NAME,importance).apply {
                description=descript
            }
        notificationManager.createNotificationChannel(channel)

    }

    private fun displayNotification(){
        contentView = RemoteViews(packageName, R.layout.widget_layout)
        contentView.setTextViewText(R.id.hour,String.format("%02d:%02d:%02d",0,0,0))

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this,WIDGET_ID)
            .setContent(contentView)
            .setContentTitle("Tytul")
            .setContentText("TekstZawartosci")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
        notification = mBuilder.build()
      //  notificationManager.notify(WIDGET_ID.toInt(),notification)
        startForeground(WIDGET_ID.toInt(),notification)

    }

    private fun updateNotification(hours:Int,minutes:Int,seconds:Int){
        contentView.setTextViewText(R.id.hour,String.format("%02d:%02d:%02d",hours,minutes,seconds))
      //  notificationManager.notify(WIDGET_ID.toInt(), notification)
        startForeground(WIDGET_ID.toInt(), notification)
    }

    fun removeNotification(){
        this.isRunning=false
       /* wakeLock?.let {
            if (it.isHeld) {
                it.release().also {
                    stopForeground(WIDGET_ID.toInt())
                }
            }
        }*/

    }


    override fun onBind(intent: Intent?): IBinder? =null
    
}


