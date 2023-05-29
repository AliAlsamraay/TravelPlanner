package com.psut.travelplanner

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.psut.travelplanner.TravelAlarmService.Companion.EXTRA_TRAVEL_ID
import java.text.SimpleDateFormat
import java.util.*


class TravelAlarmService : Service() {

    companion object {
        const val ACTION_SET_ALARM = "com.psut.travelplanner.ACTION_SET_ALARM"
        const val EXTRA_TRAVEL_ID = "com.psut.travelplanner.EXTRA_TRAVEL_ID"
    }

    private lateinit var alarmManager: AlarmManager


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        when (intent?.action) {

            ACTION_SET_ALARM -> {

                val travelId = intent.getIntExtra(EXTRA_TRAVEL_ID, -1)
                if (travelId != -1) {
                    setAlarmForTravel(travelId)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun setAlarmForTravel(travelId: Int) {
        val dbHelper = TravelDbHelper(this)
        val travel = dbHelper.getTravelById(travelId)

        val title = travel?.title
        val date = travel?.date
        val time = travel?.time


        // Convert date and time to milliseconds
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateTime = "$date $time"
        val dateTimeInMillis = dateTimeFormat.parse(dateTime)?.time ?: return

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(this, TravelAlarmReceiver::class.java)
        alarmIntent.putExtra(EXTRA_TRAVEL_ID, travelId)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            travelId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Check if the alarm should be set in the future or has already passed
        val currentTime = System.currentTimeMillis()
        if (dateTimeInMillis > currentTime) {
            // Schedule the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dateTimeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    dateTimeInMillis,
                    pendingIntent
                )
            }
        } else {
            Toast.makeText(this, "the travel time has already passed", Toast.LENGTH_SHORT).show()
        }
    }

}



class TravelAlarmReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == null || intent.action == TravelAlarmService.ACTION_SET_ALARM) {
            val travelId = intent?.getIntExtra(TravelAlarmService.EXTRA_TRAVEL_ID, -1)

            if (travelId != null && travelId != -1) {
                showNotification(context, travelId)
                Toast.makeText(context, "You have an upcoming travel", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showNotification(context: Context?, travelId: Int) {
        val channelId = "travel_app_channel"
        val channelName = "Travel App Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH

        // Create the notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification content
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.putExtra(EXTRA_TRAVEL_ID, travelId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            travelId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context!!, channelId)
            .setContentTitle("Travel Reminder")
            .setContentText("You have an upcoming travel.")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(travelId, notificationBuilder.build())
    }

}

