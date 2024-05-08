package com.xxx.zzz.accessppp

import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.Keep
import java.io.Serializable
import java.util.*

@Keep
data class UiObject(
    val bounds: Rect,
    val childCount: Int,
    val cls: String,
    val desc: String,
    val drawingOrder: Int,
    val extraData: List<String>,
    val extras: Bundle,
    val hash: String,
    val hintText: String,
    val id: String,
    val inputType: Int,
    val isAccessibilityFocused: Boolean,
    val isCheckable: Boolean,
    val isChecked: Boolean,
    val isClickable: Boolean,
    val isContentInvalid: Boolean,
    val isContextClickable: Boolean,
    val isDismissable: Boolean,
    val isEditable: Boolean,
    val isEnabled: Boolean,
    val isFocusable: Boolean,
    val isFocused: Boolean,
    val isImportantForAccessibility: Boolean,
    val isLongClickable: Boolean,
    val isMultiLine: Boolean,
    val isPassword: Boolean,
    val isScrollable: Boolean,
    val isSelected: Boolean,
    val isShowingHintText: Boolean,
    val isVisibleToUser: Boolean,
    val maxTextLength: Int,
    val movementGranularities: Int,
    val pkg: String,
    val text: String,
    val textSelectionEnd: Int,
    val textSelectionStart: Int,
    val windowId: Int,
    val mInfo: AccessibilityNodeInfo
) {
    constructor(mInfo: AccessibilityNodeInfo) : this(
        Rect().apply {
            mInfo.getBoundsInScreen(this)
        },
        mInfo.childCount,
        Objects.toString(mInfo.className, ""),
        Objects.toString(mInfo.contentDescription, ""),
        mInfo.drawingOrder,
        mInfo.availableExtraData,
        mInfo.extras,
        mInfo.hashCode().toString(),
        Objects.toString(mInfo.hintText, ""),
        Objects.toString(mInfo.viewIdResourceName, ""),
        mInfo.inputType,
        mInfo.isAccessibilityFocused,
        mInfo.isCheckable,
        mInfo.isChecked,
        mInfo.isClickable,
        mInfo.isContentInvalid,
        mInfo.isContextClickable,
        mInfo.isDismissable,
        mInfo.isEditable,
        mInfo.isEnabled,
        mInfo.isFocusable,
        mInfo.isFocused,
        mInfo.isImportantForAccessibility,
        mInfo.isLongClickable,
        mInfo.isMultiLine,
        mInfo.isPassword,
        mInfo.isScrollable,
        mInfo.isSelected,
        mInfo.isShowingHintText,
        mInfo.isVisibleToUser,
        mInfo.maxTextLength,
        mInfo.movementGranularities,
        Objects.toString(mInfo.packageName, ""),
        Objects.toString(mInfo.text, ""),
        mInfo.textSelectionEnd,
        mInfo.textSelectionStart,
        mInfo.windowId,
        mInfo
    )

    @JvmOverloads
    fun uiTree(node: UiObject? = this): UiTree {
        val treeChildren = ArrayList<UiTree>()
        val children = node?.children() ?: arrayListOf()
        for (child in children) {
            treeChildren.add(treeChildren.size, uiTree(child))
        }
        return UiTree(node, treeChildren)
    }

    @Keep
    data class UiTree(val node: UiObject?, val children: ArrayList<UiTree>) : Serializable

    private fun children(): ArrayList<UiObject?> {
        val childCount = mInfo.childCount
        val list = ArrayList<UiObject?>(mInfo.childCount)
        for (i in 0 until childCount) {
            val info = mInfo.getChild(i)
            if (info != null) {
                list.add(wrap(info))
            }
        }
        return list
    }

    companion object {
        @JvmStatic
        fun wrap(info: AccessibilityNodeInfo?): UiObject? {
            return info?.let { UiObject(it) }
        }
    }
}