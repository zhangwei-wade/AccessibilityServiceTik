package com.dzw.thinking.view

import android.content.Context
import android.widget.TextView
import com.dzw.thinking.R
import com.dzw.thinking.view.base.BWindowView


/**
 * @author zhangwei on 2020/6/18.
 * 查看跳转界面详细信息
 */
open class AccessManagerView(context: Context) : BWindowView(context) {

    override fun setContentView(): Int {
        setCanMove(true)
        return R.layout.access_manager_view
    }

    open fun setMessage(msg: String) {
        findViewById<TextView>(R.id.show_info)?.text = msg
    }
}