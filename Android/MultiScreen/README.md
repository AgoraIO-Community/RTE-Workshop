# MultiScreen

## 教程说明
了解最新屏幕共享API，学会同时共享多屏幕来减少开会中来回切换屏幕的烦恼。

**RTC版本：4.0.0-rc.1**

### 任务列表
- 配置APP ID，使用临时Token(可选)
- 加入主频道并推摄像头视频源，退出主频道
- 开启/预览/关闭屏幕共享
- 加入Ex频道并推屏幕共享视频源，退出Ex频道

### 效果

![image](imgs/final_app_snapshot.png)


## 快速上手

### 准备环境
- [Android Studio Chipmunk](https://developer.android.com/studio#downloads)
- [JDK 11](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)
- 在[控制台](https://console.agora.io/)上注册项目并获取APP ID
- (可选)若开启了Token认证，需要[获取临时Token](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-rtc-%E4%B8%B4%E6%97%B6-token)
- (可选)若手动集成，[下载rtc sdk](https://download.agora.io/sdk/release/Agora_Native_SDK_for_Android_v4.0.0-rc.1_FULL.zip)

### 实现步骤

#### 1. 配置appId、临时Token(可选)
在[app/src/main/res/values/strings_config.xml](app/src/main/res/values/strings_config.xml)下配置
```
<string name="agora_rtc_app_id"><=YOUR APP ID=></string>
<string name="agora_rtc_token"><=Your ACCESS TOKEN=></string>
```
**PS：没有临时Token，agora_rtc_token留空。如果开启了临时Token，在加入频道时要使用申请临时Token时使用的ChannelId**

#### 2. 加入主频道并推摄像头视频源，退出主频道
Practise 0：join main channel and push camera video.
补充[LivingActivity](app/src/mainjava/io/agora/multiscreen/LivingActivity.java)中joinMainChannel方法的部分代码

答案：
```java
ChannelMediaOptions options = new ChannelMediaOptions();
options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
options.autoSubscribeVideo = true;
options.autoSubscribeAudio = true;
options.publishCameraTrack = true;
options.publishMicrophoneTrack = true;
mMainChannelOptions = options;
rtcEngine.joinChannel(getString(R.string.agora_rtc_access_token),
mChannelId, mMainUid, options);
```

Practise 0.1：leave main channel.
补充[LivingActivity](app/src/mainjava/io/agora/multiscreen/LivingActivity.java)中leaveMainChannel方法的部分代码

答案：
```java
rtcEngine.leaveChannel();
```

#### 3. 开启/预览/关闭屏幕共享
Practise 1：start screen sharing.
补充[LivingActivity](app/src/mainjava/io/agora/multiscreen/LivingActivity.java)中startScreenCapture方法的部分代码

答案：
```java
ScreenCaptureParameters parameters = new ScreenCaptureParameters();
DisplayMetrics metrics = new DisplayMetrics();
getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
parameters.captureVideo = true;
parameters.videoCaptureParameters.width = 720;
parameters.videoCaptureParameters.height = (int) (720 * 1.0f / metrics.widthPixels * metrics.heightPixels);
parameters.videoCaptureParameters.framerate = 15;
parameters.captureAudio = false;
parameters.audioCaptureParameters.captureSignalVolume = 50;
rtcEngine.startScreenCapture(parameters);
```

Practise 2：setup screen sharing preview.
补充[LivingActivity](app/src/mainjava/io/agora/multiscreen/LivingActivity.java)中upLocalSeat方法的部分代码

答案：
```java
rtcEngine.setupLocalVideo(new VideoCanvas(renderView, Constants.RENDER_MODE_FIT, Constants.VIDEO_MIRROR_MODE_DISABLED,
                    Constants.VIDEO_SOURCE_SCREEN_PRIMARY, uid));
           rtcEngine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);
```

Practise 3：stop screen sharing.
补充[LivingActivity](app/src/mainjava/io/agora/multiscreen/LivingActivity.java)中startScreenCapture方法的部分代码

答案：
```java
rtcEngine.stopScreenCapture();
```


#### 4. 加入Ex频道并推屏幕共享视频源，退出Ex频道
Practise 4：join external channel and push screen sharing video source.
补充[LivingActivity](app/src/mainjava/io/agora/multiscreen/LivingActivity.java)中startScreenCapture方法的部分代码，并给mScreenConnection全局变量赋值

答案：
```java
ChannelMediaOptions options = new ChannelMediaOptions();
options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
options.autoSubscribeVideo = false;
options.autoSubscribeAudio = false;
options.publishScreenCaptureVideo = true;
options.publishScreenCaptureAudio = false;
mScreenConnection = new RtcConnection();
mScreenConnection.channelId = mChannelId;
mScreenConnection.localUid = screenUid;
rtcEngine.joinChannelEx(getString(R.string.agora_rtc_token), 	
			mScreenConnection, options, new IRtcEngineEventHandler() {});
```

Practise 5：leave external channel.
补充[LivingActivity](app/src/mainjava/io/agora/multiscreen/LivingActivity.java)中leaveScreenChannel方法的部分代码

答案：
```java
if (mScreenConnection != null) {
    rtcEngine.leaveChannelEx(mScreenConnection);
    mScreenConnection = null;
}
```

## 参考文档

- [RTC Java SDK 产品概述](https://docs.agora.io/cn/video-call-4.x/landing-page?platform=Android)
- [RTC Java SDK API 参考](https://docs.agora.io/cn/video-call-4.x/api-ref?platform=Android)

## 相关资源

- 如果你想了解更多官方示例，可以参考 [官方 SDK 示例](https://github.com/AgoraIO/API-Examples/tree/4.0.0-GA/Android/APIExample/app/src/main/java/io/agora/api/example/examples/advanced/customaudio)
- 如果你想了解声网 SDK 在复杂场景下的应用，可以参考 [官方场景案例](https://github.com/AgoraIO-usecase)

## 代码许可

示例项目遵守 MIT 许可证。