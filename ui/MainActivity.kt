package com.example.module.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 一级页容器（模板）。
 *
 * 它不画界面，只做三件事：
 * 1) 把静态单例/文件态桥接成 Compose 能观察的 state；
 * 2) 定时轮询 + onResume 重查，让"注入侧改了状态"能反映到 UI；
 * 3) 提供页面要用的动作（跳二级页、隐藏图标、申请权限）。
 *
 * Compose 无法观察普通单例，因此由本类持有 State 并驱动重组。
 */
class MainActivity : MiuixBaseActivity() {

    // ---- 供 Compose 订阅的状态 ----

    var uiActivated by mutableStateOf(ActivationState.active)
        private set

    var uiHostVersion by mutableStateOf<String?>(null)
        private set

    /** 数据版本号：业务数据变化时自增，页面"读一次"即完成订阅 */
    var dataVersion by mutableIntStateOf(0)
        private set

    var uiHasStorage by mutableStateOf(false)
        private set

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private val poller = object : Runnable {
        override fun run() {
            uiActivated = ActivationState.active
            // TODO: 按需刷新 uiHostVersion / dataVersion（数据变化时才自增）
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiHasStorage = hasStoragePermission()
        // 仅在冷启动时引导一次授权
        if (savedInstanceState == null && !uiHasStorage) {
            requestStoragePermission()
        }
        setAppContent { MainScreen(this) }
    }

    override fun onResume() {
        super.onResume()
        uiActivated = ActivationState.active
        uiHasStorage = hasStoragePermission()
        handler.removeCallbacks(poller)
        handler.post(poller)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(poller)
    }

    /** 业务侧数据变化后调用，刷新订阅了 dataVersion 的页面 */
    fun refreshData() {
        dataVersion++
    }

    // ---- 动作 ----

    fun isIconHidden(): Boolean = UiSettings.hideIcon

    /** 隐藏/显示桌面图标：通过 alias 控制，不影响 LSPosed 入口 */
    fun toggleHideIcon() {
        val next = !UiSettings.hideIcon
        UiSettings.hideIcon = next
        val alias = ComponentName(this, "$packageName.MainActivityAlias")
        packageManager.setComponentEnabledSetting(
            alias,
            if (next) PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun hasStoragePermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        }

    /** 公共目录（Android/media/<宿主包名>/...）需要「所有文件访问」权限 */
    fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1001
            )
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 3000L
    }
}
