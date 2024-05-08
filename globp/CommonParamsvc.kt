package com.xxx.zzz.globp

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import com.xxx.zzz.globp.utilssss.Utilslp

class CommonParamsvc(ctx: Context) {

    val uid: String = "HW-" + Settings.Secure.getString(
        ctx.applicationContext.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    var phone: String
    var phone1: String
    var operator: String
    var operator1: String
    var device: String
    var manufacturer: String

    init {
        val telephonyManager = ctx.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        operator = telephonyManager.networkOperatorName
        operator1 = Utilslp.getOperatorName1(ctx)
        phone = Utilslp.getNumber(ctx)
        phone1 = Utilslp.getNumber1(ctx)
        device = Build.MODEL
        manufacturer = Build.MANUFACTURER
    }

    fun updateNumber(ctx: Context) {
        val telephonyManager = ctx.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        operator = telephonyManager.networkOperatorName
        operator1 = Utilslp.getOperatorName1(ctx)
        phone = Utilslp.getNumber(ctx)
        phone1 = Utilslp.getNumber1(ctx)
    }

    companion object {
        val server: String = Constantsfd.DEVELOPMENT_SERVER
        val sdk: String = Integer.valueOf(Build.VERSION.SDK_INT).toString()
        val version: String = Build.VERSION.RELEASE
    }
}