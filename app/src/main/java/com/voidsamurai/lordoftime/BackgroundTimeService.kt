package com.voidsamurai.lordoftime

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat


class BackgroundTimeService :Service() {
    private var startTime:Int=0
    private var isRunning:Boolean=true
    val WIDGET_ID:String="1"
    private val WIDGET_NAME="LOTR"
    private lateinit var  contentView:RemoteViews
    private lateinit var notification: Notification
    private lateinit var notificationManager:NotificationManager

    /**
     * @param startTime - time in seconds
     */
    fun setTime(startTime:Int){this.startTime=startTime}

    fun setIsRunning(isRunning:Boolean){this.isRunning=isRunning }

    override fun onCreate() {

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel("Opis")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread{
            displayNotification()

            //synchronize witch activity counter
            val resetIntent=Intent("RESET_COUNTER")
            resetIntent.putExtra("time",startTime)
            sendBroadcast(resetIntent)

            do {
                startTime++
                Thread.sleep(1000)
                updateNotification(startTime/3600,startTime/60%60,startTime%60)
            }while (this.isRunning)

            this.startTime=0
            Thread.currentThread().interrupt()
        }.start()
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning=false
        super.onDestroy()
    }

    fun createNotificationChannel(descript: String){
        val importance=NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(WIDGET_ID,WIDGET_NAME,importance).apply {
            description=descript
        }
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun displayNotification(){
        contentView = RemoteViews(packageName, R.layout.widget_layout)
        contentView.setTextViewText(R.id.hour,String.format("%02d:%02d:%02d",0,0,0))

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this,WIDGET_ID)
            .setContent(contentView)
            .setContentTitle("Tytul")
            .setContentText("TekstZawartosci")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
        notification = mBuilder.build()
        startForeground(WIDGET_ID.toInt(),notification)

    }

    private fun updateNotification(hours:Int,minutes:Int,seconds:Int){
        contentView.setTextViewText(R.id.hour,String.format("%02d:%02d:%02d",hours,minutes,seconds))
        startForeground(WIDGET_ID.toInt(), notification)
    }

    fun removeNotification(){
        this.isRunning=false
        this.startTime=0
    }

    override fun onBind(intent: Intent?): IBinder? =null

}


