package com.dzw.thinking.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.dzw.thinking.R
import com.dzw.thinking.databinding.ActivityMainBinding
import com.dzw.thinking.function.TikTokModule
import com.dzw.thinking.logcat.LogDzw
import com.dzw.thinking.service.MyAccessibility
import com.dzw.thinking.service.imp.IService
import com.dzw.thinking.util.AppUtils
import com.dzw.thinking.viewModel.MainVM
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("CAST_NEVER_SUCCEEDS")
class MainV : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModelMain: MainVM
    private lateinit var mSwitchPermissions: SwitchCompat
    private lateinit var mSwitchAccess: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        LogDzw.init(false)
        setTitle(R.string.app_name)
        val dataBindingUtil: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        dataBindingUtil.lifecycleOwner = this
        viewModelMain = ViewModelProviders.of(this, ViewModelProvider.NewInstanceFactory()).get(MainVM::class.java)
        dataBindingUtil.viewMode = viewModelMain
        dataBindingUtil.clickListener = this
        mSwitchAccess = findViewById(R.id.switch_access)
        mSwitchPermissions = findViewById(R.id.switch_permissions)
        mSwitchPermissions.isChecked = Settings.canDrawOverlays(applicationContext)
        mSwitchAccess.isChecked = AppUtils.isAccessibilitySettingsOn(this, MyAccessibility::class.java.name)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(applicationContext)) {
//            //启动Activity让用户给悬浮权限授权
//            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
//            intent.data = Uri.parse("package:$packageName")
//            startActivityForResult(intent, 100)
//        } else {
//            AppUtils.startAccessAct(this)
//        }
    }

    override fun onResume() {
        super.onResume()
        mSwitchPermissions.isChecked = Settings.canDrawOverlays(applicationContext)
        mSwitchAccess.isChecked = AppUtils.isAccessibilitySettingsOn(this, MyAccessibility::class.java.name)
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.switch_permissions -> {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            R.id.switch_access -> {
                AppUtils.startAccessAct(this)
            }
            R.id.start_tik -> {
                if (!switch_access.isChecked && !switch_permissions.isChecked) {
                    Toast.makeText(this, "请打开所有开关", Toast.LENGTH_SHORT).show()
                    return
                }
                if (AppUtils.checkAppInstalled(this, TikTokModule.tikTokPackageName)) {
                    AppUtils.startComponentNameAct(this, TikTokModule.tikTokPackageName)
                } else {
                    Toast.makeText(this, "请先安装应用", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
