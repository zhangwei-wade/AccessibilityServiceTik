package com.dzw.thinking.view.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import com.dzw.thinking.logcat.LogDzw


/**
 * @author zhangwei on 2020/6/18.
 */
abstract class BWindowView(private val context: Context) {
    private var mWindowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var mParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
    private var isShowing = false
    private var mCreate = false
    private var mDecor: View? = null
    private var isCanMove = false
    private var mWidth: Int
    private var mHeight: Int

    init {
        //窗口管理器
        //布局参数
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        //        mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //        mParams.type = WindowManager.LayoutParams.TYPE_SEARCH_BAR;
        //        mParams.type = WindowManager.LayoutParams.TYPE_INPUT_METHOD;
        mParams.format = PixelFormat.RGBA_8888
        //设置之后window永远不会获取焦点,所以用户不能给此window发送点击事件焦点会传递给在其下面的可获取焦点的windo

        //设置之后window永远不会获取焦点,所以用户不能给此window发送点击事件焦点会传递给在其下面的可获取焦点的windo
        mParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL; //设置之后window永远不会获取焦点,所以用户不能给此window发送点击事件焦点会传递给在其下面的可获取焦点的windo
        //WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        //WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |//当这个window对用户是可见状态,则保持设备屏幕不关闭且不变暗
        // WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |//允许window扩展值屏幕之外
        //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;//当window被添加或者显示,系统会点亮屏幕,就好像用户唤醒屏幕一样*/
//        mParams.x = 0;
        mParams.gravity = Gravity.START or Gravity.TOP
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        if (setCreateAnimator() != 0) {
            mParams.windowAnimations = setCreateAnimator()
        }
        //获取资源对象

        //获取资源对象
        val resources: Resources = context.resources
        //获取屏幕数据
        //获取屏幕数据
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        //获取屏幕宽高，单位是像素
        //获取屏幕宽高，单位是像素
        mWidth = displayMetrics.widthPixels
        mHeight = displayMetrics.heightPixels
        //获取屏幕密度倍数
        //获取屏幕密度倍数
        val density = displayMetrics.density

    }

    private fun setCreateAnimator(): Int {
        return -1
    }

    protected abstract fun setContentView(): Int

    open fun show() {
        if (isShowing) {
            if (mDecor != null) {
                mDecor?.visibility = View.VISIBLE
            }
            return
        }
        if (!mCreate) {
            disPathOnCreate()
        }
        mDecor?.alpha = 0.8f
        mWindowManager.addView(mDecor, mParams)
        isShowing = true
        onStart()
    }

    private fun disPathOnCreate() {
        if (!mCreate) {
            create()
            mCreate = true
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun create() {
        mDecor = LayoutInflater.from(getContext()).inflate((setContentView()), null)
        onCreate(mDecor, mParams)
        if (isCanMove) {
            mDecor?.setOnTouchListener(FloatOnTouchListener())
        }

        //如果集成的有ButterKnife，可以在这里声明ButterKnife.bind(this, mDecor);
    }

    //留给子类修改布局参数使用。
    protected open fun onCreate(decor: View?, layoutParams: WindowManager.LayoutParams?) {}

    protected open fun onStart() {}

    open fun dismiss() {
        if (mDecor == null || !isShowing) {
            return
        }
        try {
            onStop()
            mWindowManager.removeViewImmediate(mDecor)
            //这个地方可以注销ButterKnife
        } finally {
            mDecor = null
            isShowing = false
            mCreate = false
            //这里可以还原参数
        }
    }

    //获取当前悬浮窗是否展示
    fun isShowing(): Boolean {
        return isShowing
    }

    fun hide() {
        if (mDecor != null) {
            mDecor?.visibility = View.GONE
        }
    }

    fun getParams(): WindowManager.LayoutParams {
        return mParams
    }

    fun getWindowManager(): WindowManager {
        return mWindowManager
    }

    protected open fun <T : View> findViewById(id: Int): T? {
        return mDecor?.findViewById(id)
    }

    private fun onStop() {}

    protected fun getContext(): Context {
        return context
    }

    open fun setCanMove(isCan: Boolean) {
        isCanMove = isCan
    }

    private inner class FloatOnTouchListener : View.OnTouchListener {
        private var x = 0
        private var y = 0
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            LogDzw.e("FloatView", "onTouch: " + event.rawX + "-----------" + event.rawY)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    mParams.x = mParams.x + movedX
                    mParams.y = mParams.y + movedY
                    if (mParams.x < 0) {
                        mParams.x = 0
                    }
                    if (mParams.x + v.width > mWidth) {
                        mParams.x = mWidth - v.width
                    }
                    if (mParams.y < 0) {
                        mParams.y = 0
                    }
                    if (mParams.y + v.height > mHeight) {
                        mParams.y = mHeight - v.height
                    }
                    // 更新悬浮窗控件布局
                    mWindowManager.updateViewLayout(v, mParams)
                }
                else -> {

                }
            }
            return false
        }
    }
}