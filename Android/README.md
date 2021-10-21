### 🔥这是下一代声网 SDK 的示例学习项目

## 当前所有可用的Demo如下
- [语音通话][basicAudio]
- [视频通话][basicVideo]
- [媒体播放][mediaPlayer]
- [屏幕共享][screenShare]
- [插件演示][simpleExtension]

## 开始

- Agora SDK 集成
> 将`agora-rte-sdk.jar`放置到`/app/libs/`目录下
> 将so文件按对应架构放到`/agoraLibs/`目录下
> 然后点击 `Sync Project with Gradle Files`
> 如果NDK集成遇到问题，可以在`local.properties`中通过`ndk.dir=/Users/xxx/Library/Android/sdk/ndk/12.3.456789`手动指定版本

- 第一步 基础音视频通话场景搭建
> 1. 实现`语音通话` 2. 实现`视频通话`

  - 完成[basicAudio]页面的所有`TODO`及[BaseDemoFragment]中的 SDK 初始化及加入场景的`TODO`
  - 完成[basicVideo]页面的所有`TODO`

- 第二步 在视频通话的基础上推送在线URL视频
> 实现`媒体播放`

  - 完成[mediaPlayer]中的所有`TODO`

- 第三步 在视频通话的基础上推送本机屏幕
> 实现`屏幕共享`

  - 完成[screenShare]页面的所有`TODO`

- 第四步 在视频通话的基础上体验基础插件功能
> 实现`插件演示`
> 通过插件调节推流音量大小及给推送的视频流加上滤镜

  - 完成[simpleExtension]页面的所有`TODO`



<br/>
<br/>

[BaseDemoFragment]: app/src/main/java/io/agora/ng_api/base/BaseDemoFragment.java
[basicAudio]: app/src/main/java/io/agora/ng_api/ui/fragment/BasicAudioFragment.java
[basicVideo]: app/src/main/java/io/agora/ng_api/ui/fragment/BasicVideoFragment.java
[mediaPlayer]: app/src/main/java/io/agora/ng_api/ui/fragment/MediaPlayerFragment.java
[screenShare]: app/src/main/java/io/agora/ng_api/ui/fragment/ScreenShareFragment.java
[simpleExtension]: app/src/main/java/io/agora/ng_api/ui/fragment/SimpleExtensionFragment.java