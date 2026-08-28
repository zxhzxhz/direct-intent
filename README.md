# Intent 快捷指令 (Direct Intent)

⚡ **一款专为 Android 设计的高性能、深度定制的 Intent / URL Scheme 快捷启动与控制中心磁贴管理工具。**

无论是标准 URL Scheme、复杂 Intent URI 还是需要 Root 提权的底层系统组件调用，`Intent 快捷指令` 都能通过直观的 Material 3 界面进行精细化解析、配置与秒级触发。支持创建桌面快捷方式及系统控制中心下拉磁贴（Quick Settings Tiles）。

---

## ✨ 核心特性

- 🚀 **双模执行引擎（Root / 标准模式）**
  - **Root 提权模式**：自动将复杂 Intent URI 解析并转换为 `su -c am start ...` 指令集，支持组件强制拉起、Activity 越权直达、系统隐藏功能调用，内置失败智能降级回退机制。
  - **标准模式**：基于原生 `startActivity` / `PendingIntent`，兼容非 Root 环境及普通第三方 App 的 URL Scheme 唤醒。
- 📱 **控制中心磁贴扩展（Quick Settings Tiles）**
  - 提供多达 **10 个独立快捷磁贴槽位**（`QuickTile1Service` ~ `QuickTile10Service`）。
  - 支持将常用指令直接绑定至状态栏/控制中心磁贴，免开应用即点即开。
- 🖥️ **桌面快捷方式支持**
  - 通过 `ShortcutLauncherActivity` 生成轻量级桌面图标，支持自定义图标名称与分类。
- 🧩 **全格式 Intent 智能解析与构造**
  - 支持标准 `intent:#Intent;...;end` 格式及自定义协议头（如 `alipays://`, `weixin://`, `bilibili://` 等）。
  - 深度解析并支持 Package、Component、Action、Category、Flags（16进制）、Data URI 以及 Extra 参数（String, Boolean, Int, Long, Float, Double, Array 等）。
- 🪄 **JumpReplay 快捷生成与预设模板**
  - 内置丰富预设（钉钉极速打卡、支付宝/微信扫码与付款码、高德直达导航、MIUI/HyperOS 直连录音、系统开发者选项、电池性能模式等）。
- 💾 **数据备份与迁移**
  - 基于 Room 数据库本地持久化存储，支持 JSON 格式的一键导入与导出。

---

## 🏗️ 技术架构与技术栈

| 模块 / 组件 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **语言** | Kotlin 2.x | 协程异步编程 (`kotlinx.coroutines`) |
| **UI 框架** | Jetpack Compose / Material 3 | 声明式现代 UI，适配深浅色与动态配色 |
| **本地存储** | Android Room Database + KSP | SQLite 抽象封装与响应式 Flow 监听 |
| **序列化/网络** | Moshi + Retrofit + OkHttp | 结构化配置导出导入与数据处理 |
| **系统底层交互** | Root Shell (`ProcessBuilder` / `su`) | 命令行参数安全转义与管道流通信 |
| **快捷入口** | `TileService` & `ShortcutManager` | 10 路 QS 磁贴服务与桌面 Pin 快捷方式 |
| **兼容性** | 最低支持 Android 7.0 (API 24) | 目标兼容 Android 14 / 15 / 16 (API 34+) |

---

## 📂 项目结构

```text
app/src/main/
├── java/com/example/
│   ├── data/                      # 数据持久化层 (Room)
│   │   ├── AppDatabase.kt         # Room 数据库入口
│   │   ├── ShortcutDao.kt          # 数据访问对象 (CRUD & 槽位查询)
│   │   ├── ShortcutEntity.kt       # 快捷指令数据实体
│   │   └── ShortcutRepository.kt   # 数据仓库
│   ├── tiles/                     # 状态栏控制中心磁贴
│   │   ├── BaseQuickTileService.kt # 磁贴基础服务逻辑与生命周期封装
│   │   └── QuickTile1..10Service.kt# 10 个独立磁贴实现
│   ├── ui/                        # Jetpack Compose UI
│   │   ├── components/            # UI 弹窗与组件
│   │   │   ├── AddToDesktopDialog.kt          # 添加到桌面弹窗
│   │   │   ├── ControlCenterTileSheet.kt      # 控制中心磁贴配置面板
│   │   │   ├── ImportExportDialog.kt          # 数据导入与导出面板
│   │   │   ├── JumpReplayGeneratorDialog.kt   # 跳转指令生成器
│   │   │   ├── RootStatusCard.kt              # Root 权限状态指示卡片
│   │   │   ├── ShortcutCard.kt                # 指令列表卡片
│   │   │   └── ShortcutEditDialog.kt          # 指令添加/编辑弹窗
│   │   ├── theme/                 # Material 3 主题配置
│   │   ├── MainScreen.kt          # 首页主界面
│   │   ├── MainViewModel.kt       # UI 状态与业务处理
│   │   └── SettingsScreen.kt      # 设置与关于页面
│   ├── utils/                     # 核心工具库
│   │   ├── IconHelper.kt          # 图标解析与映射
│   │   ├── IntentLauncher.kt      # Intent 解析、构造器与执行核心
│   │   ├── PresetTemplates.kt     # 内置预设模板
│   │   ├── RootShell.kt           # Root 命令行执行与环境探测
│   │   └── ShortcutHelper.kt      # 桌面快捷方式创建助手
│   ├── MainActivity.kt            # 应用主界面入口
│   └── ShortcutLauncherActivity.kt# 桌面快捷方式代理启动 Activity
└── res/                           # 应用资源文件
