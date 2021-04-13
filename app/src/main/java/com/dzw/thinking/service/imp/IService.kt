package com.dzw.thinking.service.imp

import android.view.accessibility.AccessibilityEvent


/**
 * @author zhangwei on 2021/4/9.
 */
interface IService {
    fun onChangeEvent(event: AccessibilityEvent)

    fun onPackageNameChange(packageName: String)
}