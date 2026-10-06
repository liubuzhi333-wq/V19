# 36.5°数字温控系统 V19（Android Native）

V19 不再使用 WebView 承担主界面和媒体渲染。UI、模特动画、热力图轮播和双指缩放均为 Android 原生实现。

## V19 关键变化

- 原生 1920×1080 设计坐标系，按真实可用像素等比缩放；不受系统 DPI / fontScale 影响导致文字重排。
- 左侧模特：120 帧原始 480×854 RGBA 素材无损合并为 Animated WebP，Android 11 使用 `ImageDecoder + AnimatedImageDrawable` 原生播放。
- 保留 `model_frames.zip` 作为解码失败时的逐帧原生备用播放器。
- “播放 / 暂停 / 重置”是真正的原生动画控制；重置回第 1 帧并恢复 1× 居中。
- 模特、热力图分别支持原生双指缩放、放大后单指拖动、双击复位。
- 4 张热力图由 Android Handler 每 1.2 秒轮播；缩放/拖动不会停止轮播。
- 点击热力视角按钮会跳转到指定视角，约 3 秒后继续自动轮播。
- 保留横屏、沉浸式全屏、常亮、开机自启。

## 设备诊断页

连续点击左上角主标题 **5 次**，打开隐藏诊断页。请在 86 寸设备上拍下完整诊断页。

诊断页包含：
- Android / 设备型号
- Real Resolution / App Metrics / Root View
- density / densityDpi / scaledDensity / fontScale / xdpi / ydpi
- 系统栏 Insets
- Android System WebView 包版本
- 兼容 V18 WebView 行为的 `window.innerWidth / innerHeight / devicePixelRatio / screen.width / screen.height`

点击诊断页任意位置关闭。

## GitHub Actions

你的上传环境会隐藏 `.github` 文件夹，所以工程根目录额外提供了可见文件：

`BUILD_APK_WORKFLOW.yml`

如果 GitHub 中没有 `Build APK`：
1. Add file → Create new file
2. 文件名输入 `.github/workflows/build-apk.yml`
3. 将 `BUILD_APK_WORKFLOW.yml` 的完整内容复制进去并 Commit
4. Actions → Build APK → Run workflow

## 建议测试顺序

1. UI 是否在小米平板和 86 寸屏均保持相同比例且无文字重叠。
2. 模特是否高清连续旋转，无 GIF 条状拖影。
3. 暂停、播放、重置是否真实有效。
4. 模特双指缩放/拖动时动画是否继续。
5. 热力图双指缩放/拖动后是否继续轮播。
6. 连续点标题 5 次拍摄设备诊断页。
7. 断电重启验证开机自启。
