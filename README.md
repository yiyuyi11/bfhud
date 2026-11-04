# ModernHUD — NeoForge 1.21.1 移植版

把 TaCZ 生态里的「现代战场风格 HUD」（ModernHud，原版面向 1.20.1 Forge）移植到 **Minecraft 1.21.1 / NeoForge 21.1**，纯客户端模组。

移植过程中按需重写了渲染管线、配置系统与兼容层，并针对 **Battlefield Ops**（另一个 1.21.1 模组）做了界面避让：在原版 HUD 与战地 HUD 同时启用时不会互相遮挡。

---

## 功能

| 模块 | 说明 |
|---|---|
| **武器栏** | 弹药数 / 备弹 / 弹匣、武器名称与图标、开镜与换弹提示；数据来自 TaCZ、Superb Warfare、LR Tactical 或原版物品 |
| **个人状态** | 生命、护盾（吸收）、体力、护甲、饥饿等分段条形，支持数字与百分比格式 |
| **小队面板** | 队友血量/状态列表、队长标识，支持队伍配色 |
| **9 格装备条** | 替换原版快捷栏的横向格子布局，显示 6 个战斗槽 + 战术道具 |
| **命中反馈** | 命中标记、伤害数字、击杀/助攻提示、连杀提示 |
| **Boss 条** | 替换原版 Boss 血条的样式与位置 |
| **罗盘 / 坐标** | 可选显示方位、坐标、所在区域 |
| **提示音** | 命中、击杀、低弹药、换弹等战斗音效（可单独开关） |
| **配置** | JSON 配置文件 + 多套预设，支持热重载；HUD 缩放、锚点、颜色、边框样式均可调 |

**刻意不做**：击杀信息栏（kill feed）与击杀图标 —— 这两块交给 Battlefield Ops，避免重复显示。

## 运行环境

- Minecraft **1.21.1**
- NeoForge **21.1.x**（开发时使用 21.1.233）
- 仅客户端（`dist = Dist.CLIENT`）
- 可选依赖：**TaCZ**（枪械数据）、Superb Warfare、LR Tactical（检测到才启用对应兼容层）

## 构建

本仓库使用一个轻量的 `build.py`（javac + zip），需要一份 NeoForge 开发环境的依赖 classpath：

```bash
# 1) 准备依赖 classpath（任意 NeoForge 21.1 工程均可）
#    在 Gradle 工程里执行一次任务，把 compileClasspath 导出为一个用 ';' 分隔的文本文件，
#    例如 BattlefieldOps 仓库的 build/compile-classpath.txt
export BF_CLASSPATH_FILE=/path/to/compile-classpath.txt

# 2) 构建
python build.py
# 产物：build/libs/modernhud-full-1.0.0-neoforge-1.21.1.jar
```

`build.py` 也支持把本仓库放在 `BattlefieldOps/` 旁边，直接复用 `../BattlefieldOps/build/compile-classpath.txt`。


## 自动构建

推送 tag（如 `v1.3.0`）会触发 GitHub Actions：

1. 拉取 TaCZ 1.21.1 的编译期依赖（Modrinth `tacz-1.21.1`）
2. `./gradlew build`
3. 把 `build/libs/*.jar` 作为 Release 附件发布（自动生成 release notes）

普通 push 与 PR 也会构建并上传 artifact，方便验证。

## 目录结构

```
src/main/java/cc/xypp/modernhud/
  ModernHud.java          入口（客户端专用）
  client/                 各类 HUD 覆盖层、绘制工具、按键、兼容层
  client/compat/          TaCZ / Superb Warfare / LR Tactical / Modern Loadout 适配
  config/                 JSON 配置模型、预设、热重载
  network/                客户端与服务端的少量同步
  sound/                  战斗音效
src/main/resources/
  META-INF/neoforge.mods.toml
  assets/modernhud/       贴图、音效、语言文件
```

## 配置

首次启动后在 `config/` 下生成 JSON 配置。主要项：

- `hud`：缩放模式、锚点、元素样式、边框
- `weapon`：武器栏数据来源、弹匣显示方式、热量条
- `bossBar` / `compass` / `sounds` / `hitIndication` / `killstreak`：各自独立开关

## 已知限制

- 只支持 1.21.1 + NeoForge，不支持 Forge 或 Fabric
- 与原版 ModernHud 的配置格式不兼容（字段与默认值按 1.21.1 重做过）
- 依赖第三方枪械模组的数据接口，接口变化时需要同步更新兼容层

## 版权与致谢

- 原版 **ModernHud** 由 [xypp](https://github.com/xypp) 开发（TaCZ 生态，1.20.1 Forge），以 **GPL-3.0** 发布。本仓库是面向 1.21.1 NeoForge 的**非官方移植**，与原作者无隶属关系。
- 本仓库同样以 **GPL-3.0** 发布（见 `LICENSE`），贴图与音效在原许可证下继续分发，版权归原作者所有。
- 第三方模组（TaCZ、Superb Warfare、LR Tactical 等）不随本仓库分发，需自行安装。
- 移植与改动部分：渲染管线、配置系统、兼容层、Battlefield Ops 避让逻辑。
