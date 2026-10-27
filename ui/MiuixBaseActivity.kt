package com.example.module.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * 所有 Miuix 页面的公共基类（模板）。
 *
 * 职责只有两件，但都必须在基类做，不能散到各页面：
 * 1) 统一通过 [setAppContent] 装载内容，保证每个页面都被 MiuixTheme 包住；
 * 2) 统一处理 edge-to-edge 系统栏（透明 + 图标明暗跟随）。
 *
 * ThemeController 需要在 Compose 树最外层构造，统一放在基类中处理。
 */
open class MiuixBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiSettings.init(applicationContext)
        setupSystemBars()
    }

    /**
     * Miuix 页面统一入口，取代 setContent。
     *
     * 深浅色优先级：跟随系统（默认）> 指定深色 > 指定浅色。
     * 修改深浅色配置后需调用 recreate() 重新构建 ThemeController。
     */
    protected fun setAppContent(content: @Composable () -> Unit) {
        setContent {
            val mode = when {
                UiSettings.followSystem -> ColorSchemeMode.System
                UiSettings.darkMode -> ColorSchemeMode.Dark
                else -> ColorSchemeMode.Light
            }
            val controller = remember(mode) { ThemeController(colorSchemeMode = mode) }
            MiuixTheme(controller) {
                content()
            }
        }
    }

    /** 当前是否深色，供系统栏图标明暗使用 */
    protected fun isNightMode(): Boolean =
        if (UiSettings.followSystem) {
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        } else {
            UiSettings.darkMode
        }

    private fun setupSystemBars() {
        // edge-to-edge：内容延展到系统栏下方，背景由 Scaffold 的 surface 一路铺上去，
        // 各页面自己用 statusBarsPadding() 给内容留安全区，避免状态栏与内容出现色块割裂。
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val dark = isNightMode()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}
