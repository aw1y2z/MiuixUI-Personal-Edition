// app/build.gradle（只列 UI 相关与必要的配置开关）
apply plugin: 'com.android.application'
apply plugin: 'org.jetbrains.kotlin.plugin.compose'

android {
    namespace 'com.example.module'

    // miuix 0.9.4 要求 compileSdk >= 37
    compileSdk 37

    defaultConfig {
        applicationId "com.example.module"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }

    // libxposed 102：模块声明文件必须合进 APK，否则框架读不到模块
    packaging {
        resources {
            merges += "META-INF/xposed/**"
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true   // 模板里 AboutActivity / MainScreen 用 BuildConfig.VERSION_NAME
        compose = true
    }
}

dependencies {
    compileOnly 'io.github.libxposed:api:102.0.0'          // Xposed API 仅编译期
    implementation 'io.github.libxposed:service:102.0.0'   // 必须打包：提供 XposedService（激活态来源）

    implementation 'androidx.core:core-ktx:1.13.1'
    implementation 'androidx.activity:activity-compose:1.9.2'
    implementation 'androidx.appcompat:appcompat:1.7.0'    // 只为 AppCompatActivity 宿主
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.8.6'

    // Miuix（HyperOS 风格 Compose 组件库）：两个 artifact 都要
    implementation 'top.yukonga.miuix.kmp:miuix-ui:0.9.4'
    implementation 'top.yukonga.miuix.kmp:miuix-preference:0.9.4'

    // 图标仍来自 material-icons-extended：Miuix 不自带图标集
    implementation 'androidx.compose.material:material-icons-extended:1.7.8'
}

// 以下依赖无需引入（Miuix 已覆盖）：androidx.compose.material3、androidx.navigation、
// androidx.recyclerview、androidx.preference、com.google.android.material
