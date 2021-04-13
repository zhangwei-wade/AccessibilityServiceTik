package com.dzw.thinking.util

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.provider.Settings
import com.dzw.thinking.service.MyAccessibility

object AppUtils {
    /**通过获取某个应用信息并捕获未安装时的异常判断：*/
    fun checkAppInstalled(context: Context, pkgName: String): Boolean {
        if (pkgName.isEmpty()) {
            return false
        }
        var packageInfo: PackageInfo?
        try {
            packageInfo = context.packageManager.getPackageInfo(pkgName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            packageInfo = null
            e.printStackTrace()
        }
        return packageInfo != null
    }

    /**
     * 获取应用程序名称
     */
    @Synchronized
    fun getAppName(context: Context, pkgName: String): String? {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                pkgName, 0
            )
            val labelRes = packageInfo.applicationInfo.labelRes
            return context.resources.getString(labelRes)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * @param context
     * @return 当前应用的版本名称
     */
    @Synchronized
    fun getVersionName(context: Context, pkgName: String): String? {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                pkgName, 0
            )
            return packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * @param context
     * @return 当前应用的版本名称
     */
    @Synchronized
    fun getVersionCode(context: Context, pkgName: String): Int {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                pkgName, 0
            )
            return packageInfo.versionCode
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }


    /**
     * @param context
     * @return 当前应用的版本名称
     */
    @Synchronized
    fun getPackageName(context: Context, pkgName: String): String? {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                pkgName, 0
            )
            return packageInfo.packageName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * 获取图标 bitmap
     * @param context
     */
    @Synchronized
    fun getBitmap(context: Context, pkgName: String): Bitmap {
        var packageManager: PackageManager? = null
        var applicationInfo: ApplicationInfo?
        try {
            packageManager = context.applicationContext
                .packageManager
            applicationInfo = packageManager!!.getApplicationInfo(
                pkgName, 0
            )
        } catch (e: PackageManager.NameNotFoundException) {
            applicationInfo = null
        }

        val d = packageManager!!.getApplicationIcon(applicationInfo!!) //xxx根据自己的情况获取drawable
        val bd = d as BitmapDrawable
        return bd.bitmap
    }


    /**通过包名打开指定应用*/
    fun startComponentNameAct(context: Context, packageName: String) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        context.startActivity(intent)
    }

    /**判断是否开启无障碍服务*/
    fun isAccessibilitySettingsOn(context: Context, className: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(100)// 获取正在运行的服务列表
        if (runningServices.size < 0) {
            return false
        }
        for (i in runningServices.indices) {
            val service = runningServices[i].service
            if (service.className == className) {
                return true
            }
        }
        return false
    }


    /**打开系统无障碍服务页面*/
    fun startAccessAct(context: Context) {
        if (!isAccessibilitySettingsOn(context, MyAccessibility::class.java.name)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}
