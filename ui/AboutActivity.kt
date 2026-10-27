package com.example.module.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.module.BuildConfig
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 关于页（模板） */
class AboutActivity : MiuixBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppContent { AboutScreen { finish() } }
    }
}

@Composable
private fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = { MiuixTopBar(title = "关于", onBack = onBack) },
        containerColor = MiuixTheme.colorScheme.surface
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 换成自己的 logo：painterResource(R.drawable.ic_logo)，需为矢量 xml
            Image(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "模块名称",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onBackground
            )
            Text(
                text = "版本 ${BuildConfig.VERSION_NAME}",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
            Spacer(Modifier.height(16.dp))

            SectionTitle(text = "相关")
            CardColumn {
                ArrowPreference(
                    title = "项目地址",
                    summary = "github.com/...",
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"))
                        )
                    }
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionTitle(text = "免责声明")
            CardColumn {
                Text(
                    text = "本模块仅供学习交流，使用产生的任何后果由使用者自行承担。",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
