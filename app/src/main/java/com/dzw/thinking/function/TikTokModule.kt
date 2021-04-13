package com.dzw.thinking.function

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dzw.thinking.logcat.LogDzw
import com.dzw.thinking.service.MyAccessibility
import com.dzw.thinking.service.click
import com.dzw.thinking.service.imp.IService
import com.dzw.thinking.service.setEditText
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.random.Random

/**
 * @author zhangwei on 2021/4/10.
 * 抖音自动滚动点赞和评论版本version15.3.0 15.4.0
 */
class TikTokModule(private val myAccessibility: MyAccessibility) : IService {
    private var handler = Handler(Looper.getMainLooper());
    private var delayTime = 0L


    private var isStart = false//服务是否启动

    init {
        getRandTime()
    }

    /**当前className*/
    private var className = ""
    private var rootNode: AccessibilityNodeInfo? = null
    override fun onChangeEvent(event: AccessibilityEvent) {
        try {
            if (event.source == null) return
            if (event.packageName != tikTokPackageName) return
            when (event.eventType) {
                //界面跳转的监听
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    className = event.className.toString()
                    LogDzw.e("TYPE_WINDOW_STATE_CHANGED:$className")
                    if (className.contains("MainActivity")) {
                        startRun()
                    }
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    LogDzw.e("TYPE_VIEW_SCROLLED:$event.className.toString()")
                    /**判断是抖音播放页面在滚动的数据*/
                    if (isStart && event.className.contains("ViewPager") && className.contains("MainActivity")) {
                        rootNode = event.source
                    }
//                    onContentChang(event.source)
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    LogDzw.e("TYPE_VIEW_CLICKED:$event.className.toString()")
//                    onContentChang(event.source)
                }
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    LogDzw.e("TYPE_VIEW_FOCUSED:$event.className.toString()  $className")
                    if (isStart && className.contains("MainActivity")) {
                        val source = event.source
                        scopeEditText.launch {
                            try {
                                onFindComment(source)
                                LogDzw.d(editTextInfo)
                                editTextInfo?.let {
                                    /*自动评论*/
                                    handler.post { it.setEditText("你是最靓的仔") }
                                    delay(500)
                                    it.parent?.getChild(1)?.getChild(0)?.getChild(2)?.click()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            /*评论完成返回*/
                            delay(100)
                            myAccessibility.performGlobalAction(GLOBAL_ACTION_BACK)
                            source.recycle()
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**获取包名*/
    override fun onPackageNameChange(packageName: String) {
        LogDzw.d(packageName)
        myAccessibility?.accessManagerView?.setMessage(packageName)
        if (packageName != "com.android.systemui") {
            if (packageName == tikTokPackageName) {
                if (className == "") className = "MainActivity"
                startRun()
            } else {
                LogDzw.d("停止测试")
                handler.removeCallbacks(scrollRun)
                isStart = false
            }
        }
    }

    /**
     *检查界面中的内容
     */
    private fun onContentChang(rootNode: AccessibilityNodeInfo) {
        if (rootNode.childCount == 0) {
//            LogDzw.i(tag, "child widget-----------------------" + rootNode.className)
            LogDzw.i("${rootNode.className}：" + rootNode.text + "   ResourceName：" + rootNode.viewIdResourceName)
        } else {
            for (i in 0 until rootNode.childCount) {
                val childNodeInfo = rootNode.getChild(i)
                if (childNodeInfo != null) {
                    LogDzw.i("child widget----------------------- $i" + childNodeInfo.className)
                    onContentChang(childNodeInfo)
                }
            }
        }
    }


    val scope = CoroutineScope(Dispatchers.IO)//点赞协程

    val scopeEditText = CoroutineScope(Dispatchers.IO)//写入评论发表评论协程

    //模拟手势的监听
    private inner class MyCallBack : AccessibilityService.GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            rootNode = null
            editTextInfo = null
            scope.launch {
                while (rootNode == null) {
                    delay(100)
                }
                if (rootNode != null) {
                    LogDzw.d("onCompleted------------" + rootNode!!.className + "   " + rootNode!!.childCount)
                    findCurrentPager(rootNode!!.getChild(1))
                }
//                rootNode?.recycle()
                getRandTime()
                postRun()
            }
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            LogDzw.d("onCancelled")
            getRandTime()
            postRun()
        }
    }


    /**
     *查找当前播放页面
     *因为用的是viewpager缓存了两个页面所以需要查找出当前页面
     */
    private fun findCurrentPager(rootNode: AccessibilityNodeInfo) {
        for (i in 0 until rootNode.childCount) {
            val childNodeInfo = rootNode.getChild(i)
            if (childNodeInfo != null) {
                LogDzw.i("child widget-----------------------" + rootNode.className + "   " + rootNode.contentDescription)
                onLikeContentClick(childNodeInfo)
                onCommentClick(childNodeInfo)
                findCurrentPager(childNodeInfo)
            }
        }
    }

    /**
     *点赞功能
     */
    private fun onLikeContentClick(childNodeInfo: AccessibilityNodeInfo) {
        if (childNodeInfo.className == "android.widget.LinearLayout" && childNodeInfo.contentDescription != null && childNodeInfo.contentDescription.contains("未选中，喜欢")) {
            LogDzw.i("未选中，喜欢")
            childNodeInfo.click()
        }
    }


    /**
     *点击评论
     */
    private fun onCommentClick(childNodeInfo: AccessibilityNodeInfo) {
        if (childNodeInfo.className == "android.widget.LinearLayout" && childNodeInfo.contentDescription != null && childNodeInfo.contentDescription.contains("评论")) {
            LogDzw.i("未选中，喜欢")
            childNodeInfo.click()
        }
    }

    private var editTextInfo: AccessibilityNodeInfo? = null//输入评论数据

    /**
     *查找输入评论
     */
    private fun onFindComment(rootNode: AccessibilityNodeInfo) {
        if (rootNode.childCount == 0) {
            LogDzw.i("${rootNode.className}：" + rootNode.text + "   ResourceName：" + rootNode.viewIdResourceName)
            if (rootNode.className == "android.widget.EditText" && rootNode.text == "留下你的精彩评论吧") {
                editTextInfo = rootNode
            }
        } else {
            for (i in 0 until rootNode.childCount) {
                if (editTextInfo != null) return
                val childNodeInfo = rootNode.getChild(i)
                if (childNodeInfo != null) {
                    onFindComment(childNodeInfo)
                }
            }
        }
    }


    private val scrollRun = Runnable {
        LogDzw.d(className)
        if (className.contains("MainActivity")) {
            myGesture()
        } else {
            isStart = false
        }
    }

    @Synchronized
    private fun myGesture() { //仿滑动
        LogDzw.d("myGesture")
        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(540f, 1700f)
        path.lineTo(540f, 300f)
        val gestureDescription = builder.addStroke(GestureDescription.StrokeDescription(path, 0, 500)).build()
        //100L 第一个是开始的时间，第二个是持续时间
        myAccessibility?.dispatchGesture(gestureDescription, MyCallBack(), null)
        LogDzw.d("myGesture end")
    }

    /*开启启动测试*/
    private fun startRun() {
        if (!isStart) {
            LogDzw.d("启动测试")
            handler.removeCallbacks(scrollRun)
            postRun()
            isStart = true
        }
    }

    /**延迟自动滚动*/
    private fun postRun() {
        handler.postDelayed(scrollRun, delayTime * 1000)
    }

    private fun getRandTime() {
        delayTime = (Random.nextInt(20) + 6).toLong()
        LogDzw.d("delayTime:   $delayTime")
    }

    companion object {
        val tikTokPackageName = "com.ss.android.ugc.aweme"
    }
}