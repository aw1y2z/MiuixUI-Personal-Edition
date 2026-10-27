package com.example.module.ui

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

/**
 * 模块自身的外观 / UI 级开关（模板）。
 *
 * 这类配置的特点是「UI 进程读写、注入进程只读」，所以走 SharedPreferences：
 * - 普通模块：用本地 SP 即可（本文件默认实现）；
 * - libxposed 102 模块：在注入入口里把 `getRemotePreferences(PREF_NAME)` 赋给 [remote]，
 *   之后读写自动切到远程 SP，宿主进程也能读到同一份（见 snippets/EntryPoint 说明）。
 *
 * 业务配置不要放这里，业务配置走自己的 json 文件（见 README「配置三通道」）。
 */
object UiSettings {

    private const val PREF_NAME = "module_ui"

    const val KEY_FOLLOW_SYSTEM = "follow_system_theme"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_HIDE_ICON = "hide_icon"

    /** 由注入入口赋值；非空时优先于本地 SP */
    @Volatile
    var remote: SharedPreferences? = null

    private var local: SharedPreferences? = null

    /** 两者都拿不到时的兜底（例如注入进程早于 Application 初始化） */
    private val memory = ConcurrentHashMap<String, Boolean>()

    fun init(context: Context) {
        if (local == null) {
            local = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun prefs(): SharedPreferences? = remote ?: local

    var followSystem: Boolean
        get() = getBoolean(KEY_FOLLOW_SYSTEM, true)
        set(value) = putBoolean(KEY_FOLLOW_SYSTEM, value)

    var darkMode: Boolean
        get() = getBoolean(KEY_DARK_MODE, false)
        set(value) = putBoolean(KEY_DARK_MODE, value)

    var hideIcon: Boolean
        get() = getBoolean(KEY_HIDE_ICON, false)
        set(value) = putBoolean(KEY_HIDE_ICON, value)

    fun getBoolean(key: String, def: Boolean): Boolean {
        val p = prefs()
        if (p != null) return p.getBoolean(key, def)
        return memory[key] ?: def
    }

    fun putBoolean(key: String, value: Boolean) {
        val p = prefs()
        if (p != null) p.edit().putBoolean(key, value).apply() else memory[key] = value
    }
}
