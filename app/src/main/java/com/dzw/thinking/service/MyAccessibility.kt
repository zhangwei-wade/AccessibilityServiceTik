package com.dzw.thinking.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.*
import android.graphics.Path
import android.graphics.Rect
import android.os.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dzw.thinking.function.TikTokModule
import com.dzw.thinking.logcat.LogDzw
import com.dzw.thinking.view.AccessManagerView
import java.util.*


class MyAccessibility : AccessibilityService() {
    private val tag = "RobMoney"
    private lateinit var tikTokModule: TikTokModule
    override fun onCreate() {
        super.onCreate()
        LogDzw.d(tag, "onCreate")
        tikTokModule = TikTokModule(this)
    }

    var rootNode: AccessibilityNodeInfo? = null

    /*用于存储临时包名*/
    var tempPackageName = ""

    var accessManagerView: AccessManagerView? = null
//    private var currentTime = 0L//执行间隔时间

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        LogDzw.d(tag, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            rootNode = event.source
            val temp = System.currentTimeMillis()
            //时间间隔500毫秒
//            if (temp - currentTime < 100) {
//                return
//            }
//            currentTime = temp
            if (event.packageName.toString() != tempPackageName) {
                tikTokModule.onPackageNameChange(event.packageName.toString())
                tempPackageName = event.packageName.toString()
            }
            tikTokModule?.onChangeEvent(event)
            when (event.eventType) {
                //界面跳转的监听
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    accessManagerView?.setMessage(event.packageName.toString())
                    LogDzw.e(tag, "TYPE_WINDOW_STATE_CHANGED:$event.className.toString()")
                }
                AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                    LogDzw.e(tag, "TYPE_WINDOWS_CHANGED:$event.className.toString()")
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
//                    LogDzw.e(tag, "TYPE_WINDOW_CONTENT_CHANGED:$event.className.toString()")
                }
                AccessibilityEvent.TYPE_VIEW_SELECTED -> {
//                    LogDzw.e(tag, "TYPE_VIEW_SELECTED:$event.className.toString()")
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    LogDzw.e(tag, "TYPE_VIEW_CLICKED:$event.className.toString()")
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    LogDzw.e(tag, "TYPE_VIEW_SCROLLED:$event.className.toString()")
                }
                AccessibilityEvent.TYPE_TOUCH_INTERACTION_START -> {
                    LogDzw.e(tag, "TYPE_TOUCH_INTERACTION_START:$event.className.toString()")
                }
                AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> {
                    LogDzw.e(tag, "TYPE_TOUCH_INTERACTION_END:$event.className.toString()")
                }

                AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> {
                    LogDzw.e(tag, "TYPE_GESTURE_DETECTION_START:$event.className.toString()")
                }

                AccessibilityEvent.TYPE_GESTURE_DETECTION_END -> {
                    LogDzw.e(tag, "TYPE_GESTURE_DETECTION_END:$event.className.toString()")
                }
                AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE -> {
                    LogDzw.e(tag, "CONTENT_CHANGE_TYPE_SUBTREE$event.className.toString()")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     *检查界面中的内容
     */
    private fun onContentChang(rootNode: AccessibilityNodeInfo) {
        if (rootNode.childCount == 0) {
//            LogDzw.i(tag, "child widget-----------------------" + rootNode.className)
            LogDzw.i(tag, "${rootNode.className}：" + rootNode.text + "   ResourceName：" + rootNode.viewIdResourceName)
        } else {
            for (i in 0 until rootNode.childCount) {
//                LogDzw.i(tag, "child widget-----------------------" + rootNode.className)
                val childNodeInfo = rootNode.getChild(i)
                if (childNodeInfo != null) {
                    onContentChang(childNodeInfo)
                }
            }
        }
    }

    private fun click(childNodeInfo: AccessibilityNodeInfo) {
        val path = Path()
        val nodeRect = Rect()
        childNodeInfo.getBoundsInScreen(nodeRect)
        path.moveTo(nodeRect.left.toFloat(), nodeRect.top.toFloat()) //X和Y是需要双击的按钮坐标
        val builder: GestureDescription.Builder = GestureDescription.Builder()
        val gestureDescription: GestureDescription =
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, 100)).build()
        dispatchGesture(gestureDescription, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                LogDzw.d(tag, "scroll cancell.")
            }
        }, null)
    }

    //    @Throws(Exception::class)
//    private fun getDetailData(rootNode: AccessibilityNodeInfo, data: List<String>) {
//        if (rootNode.childCount == 0) {
//            Log.i(tag, "child widget----------------------------" + rootNode.className)
//            LogDzw.i(tag, "Text：" + rootNode.text)
//            if (rootNode.text == null)
//                return
//        } else {
//            0
//            for (i in 0 until rootNode.childCount) {
//                Log.i(tag, "child widget----------------------------" + rootNode.className + i)
//                if (rootNode.parent != null && ListView::class.java.name == rootNode.className && (i == 0 || i == rootNode.childCount - 1)) {
//                    continue
//                }
//            }
//        }
//    }

    /**
     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    override fun onInterrupt() {
        //ToastMessage.showToast(this, "我快被终结了啊-----", Toast.LENGTH_SHORT);
    }

    /**
     * 服务连接
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
//        val serviceInfo = serviceInfo
//        serviceInfo.packageNames = Array(1) { "com.ss.android.ugc.aweme" }
//        setServiceInfo(serviceInfo)
        accessManagerView = AccessManagerView(this@MyAccessibility)
        accessManagerView?.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (accessManagerView?.isShowing() == true) accessManagerView?.dismiss()
    }


    /**
     * 服务断开
     */
    override fun onUnbind(intent: Intent): Boolean {
        if (accessManagerView?.isShowing() == true) accessManagerView?.dismiss()
        return super.onUnbind(intent)
    }

}

fun AccessibilityNodeInfo.click() {
    performAction(AccessibilityNodeInfo.ACTION_CLICK)
}


fun AccessibilityNodeInfo.checkClassName(name: String): Boolean {
    if (this.className.toString().toLowerCase(Locale.CHINA).contains(name)) {
        return true
    }
    return false
}

//设置EditText值通过复制粘贴
fun AccessibilityNodeInfo.copyText(context: Context, label: String?, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
//焦点（n是AccessibilityNodeInfo对象）
    performAction(AccessibilityNodeInfo.FOCUS_INPUT)
    performAction(AccessibilityNodeInfo.ACTION_FOCUS)
//粘贴进入内容
    performAction(AccessibilityNodeInfo.ACTION_PASTE)
}

//设置EditText值通过Accessibility自动方法
fun AccessibilityNodeInfo.setEditText(text: String) {
    val arguments = Bundle()
    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
    performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
}

