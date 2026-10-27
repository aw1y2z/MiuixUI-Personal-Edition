package com.example.module.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 项目内复用的 Miuix 组件集合（模板）。
 *
 * 原则：只做「薄封装」，不重写库的能力，把组件用法集中在一处，
 * 而不是每个页面各写一套。
 */

/**
 * 卡片容器：一张卡里排若干 preference 行。
 *
 * 行顶满卡片宽度，圆角与裁剪由卡片自身处理。
 */
@Composable
fun CardColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier.fillMaxWidth()) {
        content()
    }
}

/** 区块小标题，用于给卡片分组 */
@Composable
fun SectionTitle(text: String) {
    SmallTitle(text = text)
}

/** 布尔开关行（薄封装 SwitchPreference） */
@Composable
fun BooleanSwitch(
    title: String,
    checked: Boolean,
    summary: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    SwitchPreference(
        title = title,
        summary = summary,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

/**
 * 「整行可点 + 右侧开关」的行。
 *
 * 在 preference 行上层盖一层透明可点区域实现整行点击，
 * 并为右侧开关让出宽度、补回行标题的无障碍语义。
 */
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    summary: String? = null,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(Modifier.fillMaxWidth()) {
        SwitchPreference(
            title = title,
            summary = summary,
            checked = checked,
            onCheckedChange = onCheckedChange,
            // 库默认上下 16dp，收紧到 8dp 更贴近 Miuix 设置页的行距
            insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        )
        Box(
            Modifier
                .matchParentSize()
                .padding(end = SWITCH_RESERVED_WIDTH)
                .clickable(onClick = onClick)
                .semantics { contentDescription = title }
        )
    }
}

/** 「点行进详情 + 右侧开关控制是否记录」，日志类目专用，约束同 [SwitchRow] */
@Composable
fun LogSwitchRow(
    title: String,
    checked: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(Modifier.fillMaxWidth()) {
        SwitchPreference(
            title = title,
            checked = checked,
            onCheckedChange = onCheckedChange,
            insideMargin = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        )
        Box(
            Modifier
                .matchParentSize()
                .padding(end = SWITCH_RESERVED_WIDTH)
                .clickable(onClick = onClick)
                .semantics { contentDescription = title }
        )
    }
}

/** 上层可点区域为右侧开关预留的宽度 */
private val SWITCH_RESERVED_WIDTH = 96.dp

/** 只读的「标签 - 值」行，用于版本、宿主状态等 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.headline1,
            color = MiuixTheme.colorScheme.onBackground
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.primary
        )
    }
}

/** 通用顶栏：返回键 + 标题 + 动作区，适配 edge-to-edge */
@Composable
fun MiuixTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (onBack == null) 12.dp else 0.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onBackground,
                maxLines = 1
            )
            actions()
        }
    }
}

/** 通用确认弹窗 */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String = "确定",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(MiuixTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(title, color = MiuixTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text(text, color = MiuixTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(text = "取消", onClick = onDismiss)
                    Spacer(Modifier.width(8.dp))
                    TextButton(text = confirmText, onClick = onConfirm)
                }
            }
        }
    }
}

/** 空态提示：没有数据时给一句可操作的话，而不是一片空白 */
@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        style = MiuixTheme.textStyles.body2,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
    )
}
