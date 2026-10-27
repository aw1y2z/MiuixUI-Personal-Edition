package com.example.module.ui

import java.io.File

/**
 * UI 依赖的全局状态源（模板）。
 *
 * 这些是普通单例，Compose 无法观察，需由 Activity 持有 State 并驱动重组
 * （见 MainActivity）。
 */

/**
 * 模块激活态。
 *
 * 由注入侧维护：libxposed 102 在 Application.onServiceBind 里置 true，
 * onServiceDied 置 false。
 */
object ActivationState {
    @Volatile
    var active: Boolean = false
}

/** 一个日志类目：名字 + 文件位置 + 清空动作 */
interface LogSource {
    /** 稳定 key，用于 Intent extra 与开关持久化 */
    val key: String
    val displayName: String
    fun file(): File?
    fun clear()
}

/** 日志类目注册表：新模块把自己的类目 register 进来即可，UI 自动出条目 */
object LogCatalog {

    private val sources = LinkedHashMap<String, LogSource>()

    fun register(source: LogSource) {
        sources[source.key] = source
    }

    fun all(): List<LogSource> = sources.values.toList()

    fun get(key: String): LogSource? = sources[key]

    fun isEnabled(source: LogSource): Boolean =
        UiSettings.getBoolean("log_" + source.key, true)

    fun setEnabled(source: LogSource, enabled: Boolean) {
        UiSettings.putBoolean("log_" + source.key, enabled)
        if (!enabled) source.clear()
    }
}

/** 首页统计表的一行：行名 + 各列取值 */
data class StatRow(
    val label: String,
    val values: List<String>
)

/**
 * 统计数据源。
 *
 * 默认返回空列表（首页不显示统计卡）。接入时把 rows() 换成自己的统计读取，
 * 并让 Activity 在数据变化时 ++dataVersion 触发重组。
 */
object StatSource {

    fun headers(): List<String> = listOf("今日", "本月", "今年")

    fun rows(): List<StatRow> = emptyList() // TODO: 换成真实统计
}

/**
 * 业务配置读写占位。
 *
 * 真实模块换成自己的实现，需满足：
 * 1) 路径支持任意层级（例如 "tasks.sign_in.enabled" 为三级）；
 * 2) 写盘用「临时文件 + renameTo」原子替换，临时名带 pid/tid 防多进程交叉写；
 *    写完广播通知注入进程重载。
 */
object ConfigStore {

    private val values = HashMap<String, Boolean>()

    fun readBool(path: String, def: Boolean): Boolean = values[path] ?: def

    fun putBool(path: String, value: Boolean) {
        values[path] = value
        // TODO: 原子落盘 + 广播重载（reloadConfig / restart）
    }
}
