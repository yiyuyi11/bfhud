# ModernHUD — Forge 1.20.1

**Minecraft 1.20.1 ｜ Forge 47.4.16+ ｜ 版本 1.3.0-forge1201**

现代化 HUD：武器/弹药/状态/快捷栏/命中反馈/Boss 信息层。1.21.1 版本在 `main` 分支。

## 构建状态

`clean build` 通过（ForgeGradle 6.0.54 / Java 17 / Gradle 8.8），产物：

```
build/libs/modernhud-1.3.0-forge1201.jar
```

## 构建

```bash
gradle build     # JDK 17 + Gradle 8.8
```

## 1.20.1 适配要点

- HUD 注册改用 Forge `RegisterGuiOverlaysEvent`；
- 绘制适配 1.20.1 的 `BufferBuilder`/`Tesselator` 接口；
- 玩家皮肤与实体渲染参数按 1.20.1 调整；
- 移除了上游遗留但没有对应 Java 类的 `modernhud.mixins.json`（该配置原本未启用）。

## 已知限制

- 尚未实机检查游戏画面与 TaCZ 数据读取；上游 1.21.1 的 ModNetworking 与服务端反馈本就是空实现，本次未补。
