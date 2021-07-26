package com.router.battery

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.router.battery.adapters.ItemAdapter
import com.router.battery.data.RouterParameters
import com.router.battery.models.ScrapViewModel
import com.router.battery.workers.ScrapperWorker
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var worker: WorkManager
    private lateinit var nManager: NotificationManager
    private lateinit var alert: AlertDialog.Builder

    private val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    private val scrapperWorkRequest = PeriodicWorkRequestBuilder<ScrapperWorker>(
        15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .addTag("ScrapperWork")
        .build()



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.app_toolbar))

        Log.d("On Create"," toolbar set, loading data, setting up worker")
        WorkManager.getInstance(this).also { worker = it }

        val model: ScrapViewModel by viewModels()
        model.valuesLiveDataValue.observe(this, Observer{values: Map<String, String> -> checkAndLoad(values)})

    }

    override fun onResume() {
        super.onResume()
        refreshStatus(forceRefresh = false)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_refresh -> {
            refreshStatus(forceRefresh = true)
            true
        }

        R.id.action_notify -> {
            AlertDialog.Builder(this).also { alert = it }
            alert.setTitle("Turn on or turn off the notifications")
                .setMessage("Turn this on to see a notification showing status and battery percentage. A low battery warning will also be shown when the percentage is below 15 and is discharging")
                .setCancelable(true)
                .setPositiveButton("On", DialogInterface.OnClickListener { dialog, _ -> startWorker(); dialog.cancel() })
                .setNegativeButton("Off", DialogInterface.OnClickListener { dialog, _ -> stopWorker(); dialog.cancel() }).create().show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }



    private fun startWorker(){
        Log.d("Main is calling", "Calling scrapper")
        worker.enqueueUniquePeriodicWork("scrapperRouterBattery", ExistingPeriodicWorkPolicy.REPLACE,scrapperWorkRequest)
    }

    private fun stopWorker(){
        nManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("Main is calling", "Stop worker called")
        nManager.cancel("statusNotifier",0)
        if(!(worker.getWorkInfosByTag("scrapper").isCancelled)) {
            worker.cancelUniqueWork("scrapperRouterBattery")
        }
    }

    private fun refreshStatus(forceRefresh: Boolean = false){

        val model: ScrapViewModel by viewModels()
        Log.d("Main Activity calling", "Refreshing stuff")
        model.retrieveValues(forceRefresh = forceRefresh)

    }

    private fun checkAndLoad(values: Map<String, String>){
        Log.d("Main check", "checking and loading")
//        findViewById<TextView>(R.id.refresh_time).text = values["time"]
        if (values["isError"] == null){
            refreshStatus()
        }
        if (values["isError"] == "true") {
            AlertDialog.Builder(this).also { alert = it }
            alert.setTitle("Oops! An error has appeared")
                .setMessage("It seems that there is some error in the system. Please make sure you are connected to the right network and the url 'http://jiofi.local.html' is accessible via a normal browser")
                .setCancelable(false)
                .setPositiveButton("Cancel", DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() })
                .setNegativeButton("Exit", DialogInterface.OnClickListener { _, _ -> finish() }).create().show()
        }
        else{
            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
            val cardValues = RouterParameters().loadValuesForCard(values)
            recyclerView.adapter = ItemAdapter(this, cardValues)
            recyclerView.setHasFixedSize(true)
        }

    }
}