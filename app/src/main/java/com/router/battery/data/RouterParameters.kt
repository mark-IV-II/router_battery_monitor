package com.router.battery.data


//import java.text.DateFormat.getDateTimeInstance
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.router.battery.R
import com.router.battery.models.InfoCard
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*



class RouterParameters{

    private var isError: String = ""
    private var time: String = ""
    private var device : String = ""
    private var status : String = ""
    private var percentage : String = ""
    private var signal : String = ""
    private var devices : String = ""
    private var uptime : String = ""

    fun getJSON(values: Map<String, String>): String {
        return "Session [time: ${values["time"]}, device: ${values["device"]}, status: ${values["status"]}, percentage: ${values["percentage"]}, signal: ${values["signal"]}, device: ${values["devices"]}, uptime: ${values["uptime"]}"
    }


    @SuppressLint("SimpleDateFormat")
    fun getValues(): Map<String, String> {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")
            time =  current.format(formatter)
            Log.d("Getting Values at",time)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("dd.MM.yyyy. HH:mm:ss")
            time = formatter.format(date)
            Log.d("Getting Values at",time)
        }
        val url = "http://jiofi.local.html"

        try {

            val doc = Jsoup.connect(url).get()

            device = doc.select("#devicemodel").attr("value")
            status = doc.select("#batterystatus").attr("value")
            percentage = doc.select("#batterylevel").attr("value")
            signal = doc.select("#signalstrength").attr("value")
            devices = doc.select("#noOfClient").attr("value")
            uptime = doc.select("#ConnectionTime").attr("value")

            val level = percentage.trim('%')
            isError = "false"
            return mapOf(
                "time" to time,
                "device" to device,
                "status" to status,
                "percentage" to level,
                "signal" to signal,
                "devices" to devices,
                "uptime" to uptime,
                "isError" to isError,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            isError = "true"

            return mapOf(
                "time" to time,
                "error" to e.toString(),
                "isError" to isError,
            )
        }
    }

    fun loadValuesForCard(values: Map<String, String>): List<InfoCard>{
        return listOf(
        InfoCard("Device Model: ${values["device"]}", "Device model of the connected JioFi Router", R.drawable.outline_router_24),
        InfoCard("Battery Status: ${values["status"]}", "Current battery status of the device", R.drawable.outline_dynamic_form_24),
        InfoCard("Battery Percentage: ${values["percentage"]} %", "Current battery percentage of the device", R.drawable.outline_battery_std_24),
        InfoCard("Signal Strength: ${values["signal"]}", "Current network signal strength", R.drawable.outline_network_wifi_24),
        InfoCard("Connected Devices: ${values["devices"]}", "Number of devices connected", R.drawable.outline_devices_24),
        InfoCard("Device Uptime: ${values["uptime"]}", "Total time the device was turned on", R.drawable.outline_history_24)
        )
    }
}