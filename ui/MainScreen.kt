package com.example.module.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.module.BuildConfig
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 一级页：Scaffold + 底部 NavigationBar + IntState 切 Tab（模板）。
 *
 * 一级页用底部 NavigationBar 切 Tab，二级页一律独立 Activity + Intent extra，返回即 finish()。
 */
@Composable
fun MainScreen(activity: MainActivity) {
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = Icons.Filled.Home,
                    label = "首页"
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = Icons.Filled.Description,
                    label = "日志"
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = Icons.Filled.Tune,
                    label = "配置"
                )
                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { tab = 3 },
                    icon = Icons.Filled.Settings,
                    label = "设置"
                )
            }
        },
        containerColor = MiuixTheme.colorScheme.surface
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                // edge-to-edge 下状态栏会盖住内容，这里补安全区；
                // 背景由 Scaffold 的 surface 一路铺到状态栏，视觉上连成一片
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (tab) {
                0 -> HomeTab(activity)
                1 -> LogTab()
                2 -> ConfigTab(activity)
                else -> SettingsTab(activity)
            }
        }
    }
}

/** 一级页大标题：与二级页顶栏标题区分开，用 32sp 加粗 */
@Composable
private fun PageTitle(text: String) {
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MiuixTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )
}

// ---------------------------------------------------------------- 首页

@Composable
private fun HomeTab(activity: MainActivity) {
    PageTitle("首页")

    val activated = activity.uiActivated
    val hostVersion = activity.uiHostVersion

    // 激活状态卡：放在首页第一屏顶部，使用固定的成功/警告语义色
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                if (activated) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (activated) "已激活" else "未激活",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activated) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
                Spacer(Modifier.height(4.dp))
                Line("版本 ${BuildConfig.VERSION_NAME}")
                Line("Xposed API 102")
                if (activated && hostVersion != null) Line("宿主 $hostVersion")
            }
            Image(
                imageVector = if (activated) Icons.Filled.CheckCircle else Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                colorFilter = ColorFilter.tint(
                    if (activated) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
            )
        }
    }
    Spacer(Modifier.height(16.dp))

    SectionTitle(text = "运行环境")
    CardColumn {
        // 与状态卡不重复：版本/激活已在卡里，这里只放设备侧信息
        InfoRow("SDK API", android.os.Build.VERSION.SDK_INT.toString())
        InfoRow("设备", android.os.Build.MODEL ?: "")
        InfoRow("系统架构", android.os.Build.SUPPORTED_ABIS?.firstOrNull() ?: "")
    }
    Spacer(Modifier.height(16.dp))

    val rows = StatSource.rows()
    if (rows.isNotEmpty()) {
        SectionTitle(text = "数据统计")
        CardColumn {
            activity.dataVersion // 订阅：统计数据变化后自增，触发本表重组
            StatsTable(rows)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Line(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.body2,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
    )
}

/** 统计网格：首列行名，其余列均分（纯 Row/Box.weight 手写，不用表格库） */
@Composable
private fun StatsTable(rows: List<StatRow>) {
    val headers = StatSource.headers()
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1.2f))
            headers.forEach { header ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = header,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        rows.forEach { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1.2f)) {
                    Text(
                        text = row.label,
                        style = MiuixTheme.textStyles.headline1,
                        color = MiuixTheme.colorScheme.onBackground
                    )
                }
                row.values.forEach { value ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = value,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- 日志

@Composable
private fun LogTab() {
    val context = LocalContext.current
    PageTitle("日志")

    SectionTitle(text = "分类记录")
    CardColumn {
        LogCatalog.all().forEach { source ->
            LogRow(context, source)
        }
    }
    Spacer(Modifier.height(16.dp))
}

/** 单个日志类目行：点行看详情，右侧开关控制是否记录 */
@Composable
private fun LogRow(context: Context, source: LogSource) {
    var enabled by remember(source.key) { mutableStateOf(LogCatalog.isEnabled(source)) }
    LogSwitchRow(
        title = source.displayName,
        checked = enabled,
        onClick = {
            context.startActivity(
                Intent(context, LogViewerActivity::class.java)
                    .putExtra(LogViewerActivity.EXTRA_LOG_KEY, source.key)
            )
        }
    ) { next ->
        enabled = next
        LogCatalog.setEnabled(source, next)
    }
}

// ---------------------------------------------------------------- 配置

@Composable
private fun ConfigTab(activity: MainActivity) {
    val context = LocalContext.current
    PageTitle("配置")

    activity.dataVersion
    val entries = remember(activity.dataVersion) { configEntries() }

    SectionTitle(text = "配置管理")
    CardColumn {
        if (entries.isEmpty()) {
            EmptyHint("尚未识别到可配置项")
        } else {
            entries.forEach { (key, title) ->
                ArrowPreference(
                    title = title,
                    onClick = {
                        context.startActivity(
                            Intent(context, DetailActivity::class.java)
                                .putExtra(DetailActivity.EXTRA_KEY, key)
                        )
                    }
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

/** 配置入口列表。TODO: 换成真实来源（账号档案 / 配置分组） */
private fun configEntries(): List<Pair<String, String>> =
    listOf("default" to "默认")

// ---------------------------------------------------------------- 设置

@Composable
private fun SettingsTab(activity: MainActivity) {
    val context = LocalContext.current
    PageTitle("设置")

    SectionTitle(text = "系统设置")
    CardColumn {
        // 已授权时不再显示该行，避免"已授予"这种无操作价值的展示
        if (!activity.uiHasStorage) {
            ArrowPreference(
                title = "申请文件权限",
                summary = "模块需要文件权限才能正常运行",
                onClick = { activity.requestStoragePermission() }
            )
        }
        var iconHidden by remember { mutableStateOf(activity.isIconHidden()) }
        BooleanSwitch("隐藏图标", checked = iconHidden) {
            activity.toggleHideIcon()
            iconHidden = activity.isIconHidden()
        }
        var followSystem by remember { mutableStateOf(UiSettings.followSystem) }
        BooleanSwitch("跟随系统设置", checked = followSystem) {
            followSystem = it
            UiSettings.followSystem = it
            activity.recreate() // 重建以应用新主题
        }
        var darkMode by remember { mutableStateOf(UiSettings.darkMode) }
        BooleanSwitch("深色模式", checked = darkMode) {
            darkMode = it
            UiSettings.darkMode = it
            activity.recreate() // 重建以应用新主题
        }
    }
    Spacer(Modifier.height(16.dp))

    SectionTitle(text = "关于")
    CardColumn {
        ArrowPreference(
            title = "关于应用",
            onClick = { context.startActivity(Intent(context, AboutActivity::class.java)) }
        )
    }
    Spacer(Modifier.height(16.dp))
}

/** 模块 App 进程内没有宿主 Toast，用系统 Toast 提示一次性结果 */
internal fun Context.toast(msg: String) {
    android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
}
