package com.example.module.ui

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File
import java.io.RandomAccessFile

/**
 * 日志查看器（模板）。
 *
 * - 只读文件尾部（默认 1MB / 500 条），避免日志增长拖慢读取；
 * - 轮询比对文件 stamp（长度 + 修改时间），无变化则不读盘、不重组；
 * - reverseLayout = true 使最新在下，用户手动上翻时暂停自动跟随；
 * - 无时间戳的行合并到上一条，多行正文聚合为同一张卡片。
 */
class LogViewerActivity : MiuixBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val source = LogCatalog.get(intent.getStringExtra(EXTRA_LOG_KEY) ?: "")
        setAppContent {
            if (source == null) {
                EmptyHint("未知日志类目")
            } else {
                LogScreen(source) { finish() }
            }
        }
    }

    companion object {
        const val EXTRA_LOG_KEY = "logKey"
    }
}

/** 单条日志条目（可能含多行正文） */
private data class LogItem(
    val lineNumber: Int,
    val time: String?,
    val body: String
)

@Composable
private fun LogScreen(source: LogSource, onBack: () -> Unit) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(readLogItems(source.file())) }
    var revision by remember { mutableIntStateOf(0) }
    var browsingHistory by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // 列表停下时按位置判断是否停在最新：还能往回滚说明用户在看历史
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling) browsingHistory = listState.canScrollBackward
            }
    }

    // 定时刷新：文件无变化时只做一次轻量比较
    LaunchedEffect(source.key) {
        var stamp = fileStamp(source.file())
        while (true) {
            delay(LOG_REFRESH_INTERVAL_MS)
            val file = source.file() ?: continue
            val newStamp = fileStamp(file) ?: continue
            if (newStamp == stamp) continue
            stamp = newStamp
            entries = readLogItems(file)
            revision++
        }
    }

    LaunchedEffect(revision) {
        if (!browsingHistory && entries.isNotEmpty()) listState.animateScrollToItem(0)
    }

    val filtered = remember(entries, searchQuery) {
        if (searchQuery.isBlank()) entries
        else entries.filter { it.body.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            MiuixTopBar(
                title = source.displayName,
                onBack = onBack,
                actions = {
                    TextButton(
                        text = "清空",
                        onClick = {
                            source.clear()
                            entries = emptyList()
                            revision++
                            context.toast("已清空${source.displayName}")
                        }
                    )
                }
            )
        },
        containerColor = MiuixTheme.colorScheme.surface
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "",
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "搜索",
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )
            }

            if (filtered.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "无匹配日志" else "暂无日志",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(
                        filtered.asReversed(),
                        key = { _, e -> "${e.lineNumber}" }
                    ) { _, entry ->
                        LogItemCard(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItemCard(entry: LogItem) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .padding(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.time ?: "—",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.primary
            )
        }
        if (entry.body.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.body,
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onBackground
            )
        }
    }
}

// ---------------------------------------------------------------- 文件读取

private const val MAX_TAIL_BYTES = 1024 * 1024L
private const val MAX_LOG_ENTRIES = 500
private const val LOG_REFRESH_INTERVAL_MS = 1000L

private data class LogFileStamp(val length: Long, val modified: Long)

private fun fileStamp(file: File?): LogFileStamp? =
    if (file != null && file.exists()) LogFileStamp(file.length(), file.lastModified()) else null

/** 从文件尾部读取文本；非从头读时丢弃首个可能被截断的行 */
private fun readTailText(file: File, maxBytes: Long): String {
    val length = file.length()
    if (length <= 0L) return ""
    val start = maxOf(0L, length - maxBytes)
    RandomAccessFile(file, "r").use { raf ->
        raf.seek(start)
        val bytes = ByteArray((length - start).toInt())
        raf.readFully(bytes)
        var text = String(bytes, Charsets.UTF_8)
        if (start > 0L) {
            val idx = text.indexOf('\n')
            text = if (idx >= 0) text.substring(idx + 1) else ""
        }
        return text
    }
}

/**
 * 解析日志文件为条目；无时间戳的行合并到上一条。
 * 行格式按写入侧约定：`HH:mm:ss  message`，不匹配的时间格式改这里的正则。
 */
private fun readLogItems(file: File?): List<LogItem> {
    if (file == null || !file.exists()) return emptyList()
    val timeRegex = Regex("^(\\d{2}:\\d{2}:\\d{2})\\s+(.*)$")
    val entries = ArrayDeque<LogItem>()
    return try {
        var lineNumber = 0
        for (line in readTailText(file, MAX_TAIL_BYTES).lineSequence()) {
            lineNumber++
            val match = timeRegex.find(line)
            if (match != null) {
                entries.addLast(LogItem(lineNumber, match.groupValues[1], match.groupValues[2]))
            } else {
                if (entries.isNotEmpty()) {
                    val last = entries.removeLast()
                    entries.addLast(last.copy(body = last.body + "\n" + line))
                } else {
                    entries.addLast(LogItem(lineNumber, null, line))
                }
            }
            while (entries.size > MAX_LOG_ENTRIES) entries.removeFirst()
        }
        entries.toList()
    } catch (t: Throwable) {
        emptyList()
    }
}
