package com.example.module

import android.app.Application
import com.example.module.ui.ActivationState
import com.example.module.ui.UiSettings
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

/**
 * 模块 App 进程的 Application（模板）。
 *
 * 唯一职责：把「模块是否被激活」和「远程 SP」两件事接到 UI 上。
 * 激活态以框架是否推送 XposedService 为准。
 */
class ModuleApplication : Application(), XposedServiceHelper.OnServiceListener {

    override fun onCreate() {
        super.onCreate()
        UiSettings.init(this)
        // 回调发生在模块 App 进程：框架把 XposedService 推过来即证明模块已激活
        XposedServiceHelper.bindService(this)
    }

    override fun onServiceBind(service: XposedService) {
        ActivationState.active = true
        // 让 UI 侧的外观设置与注入进程共享同一份
        UiSettings.remote = service.getRemotePreferences("module_ui")
    }

    override fun onServiceDied(service: XposedService) {
        ActivationState.active = false
        UiSettings.remote = null
    }
}
