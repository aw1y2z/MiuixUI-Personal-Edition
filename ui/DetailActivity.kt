package com.example.module.ui

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 二级页骨架（模板）：标题栏 + 可滚动内容区。
 *
 * 二级页约定：
 * - 独立 Activity，参数用 Intent extra（String 为主，别传对象），返回直接 finish()；
 * - 顶栏用 [MiuixTopBar]，onBack 传 finish()；
 * - 内容区用 Column + verticalScroll（条目少）；条目多或动态增长换 LazyColumn + key。
 */
class DetailActivity : MiuixBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val key = intent.getStringExtra(EXTRA_KEY) ?: "default"
        setAppContent {
            DetailScreen(key) { finish() }
        }
    }

    companion object {
        const val EXTRA_KEY = "detailKey"
    }
}

@Composable
private fun DetailScreen(key: String, onBack: () -> Unit) {
    val context = LocalContext.current

    // 每个开关一个可观察 state：改完立即落盘，UI 与文件保持一致
    var enabled by remember(key) {
        mutableStateOf(ConfigStore.readBool("items.$key.enabled", false))
    }

    Scaffold(
        topBar = { MiuixTopBar(title = key, onBack = onBack) },
        containerColor = MiuixTheme.colorScheme.surface
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SectionTitle(text = "功能")
            CardColumn {
                BooleanSwitch(
                    title = "启用",
                    summary = "关闭后不再执行",
                    checked = enabled
                ) { next ->
                    enabled = next
                    ConfigStore.putBool("items.$key.enabled", next)
                }
            }
            Spacer(Modifier.height(16.dp))

            SectionTitle(text = "操作")
            CardColumn {
                ArrowPreference(
                    title = "立即执行一次",
                    onClick = {
                        // TODO: 广播通知注入进程执行，结果写入日志
                        context.toast("已触发，结果见日志")
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
