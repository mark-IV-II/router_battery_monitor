package com.router.battery.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ListenableWorker.Result.failure
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.GsonBuilder
import com.router.battery.MainActivity
import com.router.battery.R
import com.router.battery.data.RouterParameters
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class ScrapperWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {


    private val TAG = "WorkerTAG"

    // Create an Intent for the activity you want to start
    private val resultIntent = Intent(
        applicationContext,
        MainActivity::class.java
    )
    // Create the TaskStackBuilder
    private val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(applicationContext).run {
        // Add the intent, which inflates the back stack
        addNextIntentWithParentStack(resultIntent)
        // Get the PendingIntent containing the entire back stack
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val nManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "Permanent Notification"
    private val channelName = "Router Battery Status Indicator"


    private fun showNotification(task: String, desc: String) {
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "Low Battery Alert"
        val channelName = "Low Router Battery Status Indicator"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lockscreenVisibility = 1
            manager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(task)
            .setContentText(desc)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(ContextCompat.getColor(applicationContext, R.color.red))
        manager.notify(1, builder.build())

    }
    private fun updatePermanentNotification(title: String, desc: String){



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.lockscreenVisibility = 0
            nManager.createNotificationChannel(notificationChannel)

        }
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(desc)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(resultPendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
        nManager.notify("statusNotifier",0, builder.build())


    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun doWork(): Result {

        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val currentNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)

        Log.d(TAG, "I am Summoned")

        if (!isStopped) {

            Log.d(TAG, "Not stopped")

            if (capabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                try {
                    Log.d(TAG, "Network is available")

                    val valuesMap = RouterParameters().getValues()

                    if (valuesMap["isError"] == "false") {


                        writeToJSON(RouterParameters().getJSON(valuesMap))

                        val percent: String? = valuesMap["percentage"]
                        val level = percent?.toInt()

                        Log.d(TAG, "Percentage $percent")
                        val title = "Status: ${valuesMap["status"]}    Percentage left: $percent"
                        val desc = "Last updated on: ${valuesMap["time"]}"

                        updatePermanentNotification(title, desc)

                        if (valuesMap["status"]!! == "Discharging" && level!! < 20) {

                            Log.d(TAG, "if condition called at $percent")

                            showNotification(
                                "Router Battery Low at $percent",
                                "Your JioFi Router battery is running low and is at $percent. Tap the notification to close it"
                            )
                        }
                    } else {
                        showNotification("Error. Unable to access router page", "${valuesMap["error"]}")
                        throw Exception(valuesMap["error"])
                    }
                    return success()

                } catch (e: Exception) {
                    return failure()
                }
            }else {
                showNotification("Not Connected to WiFi", "Device not connected to the required WiFi network")
                return failure()
            }
        }else{
            onStopped()
            return failure()
        }
    }

    private fun writeToJSON(JSONString: String) {

        val logFile = File(applicationContext.getExternalFilesDir(null), "scrapLog.json")
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        val valuesJsonString: String = gsonPretty.toJson(JSONString)

        Log.d(TAG, logFile.toString())

        try {
            if (isExternalStorageWritable()){
            logFile.outputStream().write(valuesJsonString.toByteArray())}
            else{
                throw IOException("Unable to write to the Android/data folder")
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

}