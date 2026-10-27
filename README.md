# Miuix 模块 UI 通用模板

从两个已上线的 LSPosed 模块（一个 Kotlin 全栈、一个 Java 主体 + Kotlin UI）的 UI 层提炼出的通用骨架。

两者技术栈高度一致：**Jetpack Compose + miuix-kmp 0.9.4**，无 XML 布局、无 Fragment、无 RecyclerView/Adapter、无 material3、无 Navigation 组件、无 `values-night`。本模板取的是它们收敛后的共同写法。

---

## 一、两个项目 UI 的共性（已固化为模板）

| 维度 | 两个项目的一致选择 |
|---|---|
| 技术栈 | Compose + `miuix-ui` + `miuix-preference` 0.9.4 + `material-icons-extended` 1.7.8（图标来源） |
| 工程参数 | compileSdk 37（miuix 0.9.4 硬要求）/ minSdk 26 / targetSdk 34 / Java 17 / AGP 9.2.1 / Kotlin 2.4.20 |
| 分层 | `ui/` 只画界面，`data`/`core` 纯逻辑，两边不混 |
| 基类 | 统一 `MiuixBaseActivity`：`setAppContent{}` 包 `MiuixTheme(ThemeController)` + edge-to-edge 系统栏 |
| 一级页 | `Scaffold + NavigationBar` + `mutableIntStateOf` 切 `when` 分支，4 个 Tab（首页/日志/配置/设置） |
| 二级页 | 独立 Activity + Intent extra（传 String，不传对象）+ `onBack = finish()` |
| 页面骨架 | `SectionTitle`（SmallTitle）+ `CardColumn`（Card 里排 preference 行）+ `Spacer(16.dp)` |
| 列表 | 条目少用 `Column + verticalScroll`；条目多/动态增长用 `LazyColumn` + `key` |
| 设置项 | miuix `preference`：Arrow（跳转）/ Switch / Checkbox / RadioButton / Slider |
| 主题 | `ColorSchemeMode.System/Dark/Light`，跟随系统优先；取色一律 `MiuixTheme.colorScheme.*` / `textStyles.*` |
| 深色切换 | 改配置后必须 `activity.recreate()`（ThemeController 在 remember 里构造） |
| 状态桥接 | Compose 不观察静态单例 → Activity 持有 `mutableStateOf`/`mutableIntStateOf` + 3s 轮询 + `onResume` 重查 |
| 激活态 | `Application.onServiceBind` 置位（唯一权威来源），首页第一屏状态卡展示 |
| 日志页 | 尾部 1MB / 500 条、1s 轮询比对文件 stamp、`reverseLayout=true`、手动上翻时冻结跟随、搜素 + 清空 |
| 顶栏 | 自绘 `MiuixTopBar`（56dp + `statusBarsPadding`），不用 Miuix `TopAppBar` |
| 弹窗 | 自绘 `ConfirmDialog`（`Dialog` + surface + 16dp 圆角），不用 `SuperDialog` |
| 配置跨进程 | 文件直写 + 广播通知注入进程重载 |

### 两处有分歧、按场景二选一

| 分歧点 | 方案 A | 方案 B | 建议 |
|---|---|---|---|
| 业务配置落盘 | UI **直写** json（临时文件 + `renameTo` 原子替换）+ 广播 reload | 只改内存单例，**退出页面时统一落盘**（`hasFieldChanges()` 脏检查） | 字段少、改动即生效 → 直写；字段多、一次改一批 → 内存改 + 退出落盘（但每个可编辑页都要显式调 save，`onBackPressed` 兜底） |
| 设置项模型 | 手写 Composable，开关逐个绑定 | `ModelField<T>` 元数据驱动，`when(type)` 分派渲染 | 少于 ~30 项手写；配置量大或要动态增删 → 元数据驱动 |

---

## 二、目录结构

```
.
├── README.md                 本文件
├── snippets/                 配置片段，拷进工程后改占位符
│   ├── build.gradle.root
│   ├── build.gradle.app
│   ├── AndroidManifest.xml
│   ├── themes.xml
│   └── ModuleApplication.kt  激活态 + 远程 SP 接线
└── ui/                       直接拷到 app/src/main/java/<你的包名>/ui/
    ├── MiuixBaseActivity.kt  主题 + 系统栏（所有页面继承）
    ├── UiSettings.kt         外观开关（本地 SP / libxposed 远程 SP）
    ├── AppState.kt           ActivationState / LogCatalog / StatSource / ConfigStore
    ├── MiuixComponents.kt    CardColumn / SectionTitle / 开关行 / 顶栏 / 弹窗
    ├── MainActivity.kt       状态桥接 + 轮询 + 动作（隐藏图标、权限）
    ├── MainScreen.kt         一级页 4 Tab
    ├── DetailActivity.kt     二级页骨架
    ├── LogViewerActivity.kt  日志查看器
    └── AboutActivity.kt      关于页
```

## 三、套用步骤

1. `ui/` 整个目录拷到 `app/src/main/java/<包名>/ui/`，全局替换包名 `com.example.module` → 你的包名。
2. 按 `snippets/build.gradle.app` 补齐依赖与开关（`buildFeatures { compose = true; buildConfig = true }`、`packaging.merges`）。
3. Manifest 配 `MainActivity`（带 `de.robv.android.xposed.category.MODULE_SETTINGS`）+ `MainActivityAlias`（桌面图标）+ 三个二级 Activity。
4. 接 `ModuleApplication.kt`：绑定 XposedService，把 `ActivationState.active` 与 `UiSettings.remote` 接上。
5. 替换占位实现：
   - `LogCatalog.register(...)` 注册你的日志类目（在 Application 或首个 Activity 里）；
   - `StatSource.rows()` 换成真实统计；
   - `ConfigStore` 换成真实配置读写（保留任意层级路径 + 原子落盘）；
   - `MainScreen.configEntries()` 换成真实配置入口列表。
6. 关于页 logo 换成 `painterResource(R.drawable.ic_logo)`（**必须是矢量 xml**）。

---

## 四、架构约定（改动前先读）

1. **Compose 不观察单例。** 静态 object 不是 State，改了不会重组。统一由 Activity 持有 state，或用「数据版本号 +1」让页面重新计算。
2. **页面不直接做 IO。** 读文件/读配置放在 Activity 的轮询里，或 `LaunchedEffect` 里带 stamp 比对；Composable 只负责渲染。
3. **二级页返回即 `finish()`。** 不引入导航图，不用 Fragment。
4. **配置三通道分开：**
   - 业务配置 → 自己的 json 文件（UI 直写，原子落盘 + 广播通知注入进程）；
   - 外观/UI 开关 → `UiSettings`（远程 SP，两进程共享）；
   - 注入进程内存态（抓包开关等） → 只能靠广播改，写了文件也没用。
5. **改主题必须 `recreate()`。** 只改 state 不会重建 `ThemeController`。
6. **语义色可硬编码，其余一律走 `colorScheme`。** miuix 没有"成功/警告"语义 token（两项目都用 `0xFF2E7D32` / `0xFFE65100`），接主题反而会跑偏。
7. **执行结果写日志，不靠 Toast 报成功。** Toast 只用于"已触发""已清空"这类一次性反馈。

---

## 五、扩展点（TODO 都标在代码里）

- `LogCatalog.register(...)`：加一个日志类目，一级页与查看器自动出条目；
- `StatSource.rows()`：接统计后首页自动出现"数据统计"卡；
- `ConfigStore`：换成真实读写（任意层级路径、原子落盘、广播重载）；
- `MainScreen.SettingsTab`：往"系统设置"卡里加自己的开关；
- 需要更多层级时照 `DetailActivity` 复制（实际项目最深用到四级：Tab → 分组目录 → 分组字段 → 选择型字段编辑）。

---

## 六、许可证与致谢

本项目采用 **GPL-3.0**，详见 [LICENSE](LICENSE)。

UI 构建于以下项目之上：

- [miuix](https://github.com/compose-miuix-ui/miuix) — Compose Multiplatform UI 组件库（Apache-2.0）
- [libxposed](https://github.com/libxposed/api) — Xposed 模块 API 与服务（Apache-2.0）
- [AndroidX Material Icons](https://github.com/androidx/androidx) — 图标来源（Apache-2.0）
