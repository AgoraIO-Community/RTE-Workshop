package io.agora.ng_api.base;

import android.os.Bundle;
import android.view.TextureView;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import java.util.Random;

import io.agora.extension.ExtensionManager;
import io.agora.ng_api.MyApp;
import io.agora.ng_api.R;
import io.agora.ng_api.ui.fragment.DescriptionFragment;
import io.agora.ng_api.util.ExampleUtil;
import io.agora.ng_api.view.DynamicView;
import io.agora.rte.AgoraRteSDK;
import io.agora.rte.AgoraRteSdkConfig;
import io.agora.rte.base.AgoraRteLogConfig;
import io.agora.rte.media.track.AgoraRteCameraVideoTrack;
import io.agora.rte.media.track.AgoraRteMicrophoneAudioTrack;
import io.agora.rte.media.video.AgoraRteVideoCanvas;
import io.agora.rte.scene.AgoraRteScene;
import io.agora.rte.scene.AgoraRteSceneConfig;
import io.agora.rte.scene.AgoraRteSceneEventHandler;
import io.agora.rte.scene.AgoraRteSceneJoinOptions;

/**
 * On Jetpack navigation
 * Fragments enter/exit represent onCreateView/onDestroyView
 * Thus we should detach all reference to the VIEW on onDestroyView
 */
public abstract class BaseDemoFragment<B extends ViewBinding> extends BaseFragment<B> {

    // COMMON FILED
    public final String mLocalUserId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE/2));
    public final String mLocalStreamId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE/2));
    public final String mLocalMediaStreamId = "media-" + new Random().nextInt(Integer.MAX_VALUE/2) + Integer.MAX_VALUE/2;
    public String sceneName;
    public AgoraRteScene mScene;
    public AgoraRteSceneEventHandler mAgoraHandler;
    @Nullable
    public AgoraRteCameraVideoTrack mLocalVideoTrack;
    @Nullable
    public AgoraRteMicrophoneAudioTrack mLocalAudioTrack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ensure sceneName
        if (getArguments() == null || getArguments().get(DescriptionFragment.sceneName) == null) {
            MyApp.getInstance().shortToast(R.string.scene_name_required);
            getNavController().popBackStack();
            return;
        }
        sceneName = getArguments().getString(DescriptionFragment.sceneName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!MyApp.justDebugUIPart) {
            doExitScene();
        }
    }

    public void initAgoraRteSDK() {
        initAgoraRteSDK(false);
    }

    /**
     * app id 优先级
     * 用户主动输入 > strings 内置
     */
    public void initAgoraRteSDK(boolean enableExtension) {
        String extensionName = ExtensionManager.EXTENSION_NAME;
        String appId = ExampleUtil.getSp(requireContext()).getString(ExampleUtil.APPID, "");
        // check strings
        if (appId.isEmpty()) appId = getString(R.string.agora_app_id);

        AgoraRteLogConfig logConfig = new AgoraRteLogConfig(requireContext().getExternalCacheDir().getAbsolutePath());

       // TODO 1) create a AgoraRteSdkConfig and set params using above values
        // Code here

        if(enableExtension) {
            // TODO 2) support feature for extension demo
            // Code here
//            rteSdkConfig.addExtension(extensionName);
        }

        // TODO 2 init SDK using all the parameters above
//        AgoraRteSDK.init(rteSdkConfig);
    }

    /**
     * @param sceneName scene name
     * @param userId      userId
     * @param token       access token
     */
    public void doJoinScene(String sceneName, String userId, String token) {
        doJoinScene(sceneName, userId, token, new AgoraRteSceneConfig());
    }

    /**
     * 1. create scene
     * 2. registerSceneEventHandler
     * 3. join scene
     */
    public void doJoinScene(String sceneId, String userId, String token, AgoraRteSceneConfig config) {
        // Step 1
        // TODO create scene using sceneId and config
        // Code here


        // Step 2
        mScene.registerSceneEventHandler(mAgoraHandler);


        // Step 3
        // TODO join scene using userId and token
        // Code here
    }

    public void doExitScene() {
        if(mLocalAudioTrack != null)
            mLocalAudioTrack.destroy();
        if(mLocalVideoTrack != null)
            mLocalVideoTrack.destroy();
        if (mScene != null) {
            mScene.leave();
            mScene.destroy();
        }
        AgoraRteSDK.deInit();
    }

    /**
     * 模版代码，高级演示中使用
     * Template code
     */
    public void initLocalAudioTrack(){
        // TODO COMPLETE THIS TEMPLATE CODE
        // Code here

    }

    /**
     * 模版代码，高级演示中使用
     * Template code
     */
    public void initLocalVideoTrack(DynamicView dynamicView){
        // init mLocalVideoTrack
        // TODO CODE HERE 1
        // Code here


        addLocalView(dynamicView);

        // startCapture & publish
        // 必须先添加setPreviewCanvas，然后才能 startCapture
        // Must first setPreviewCanvas, then we can startCapture
        // TODO CODE HERE 2
        // Code here


    }

    public void addLocalView(DynamicView dynamicView) {
        addLocalView(dynamicView,AgoraRteVideoCanvas.RENDER_MODE_FIT);
    }

    public void addLocalView(DynamicView dynamicView, @IntRange(from = 1,to = 3) int renderMode) {
        TextureView textureView = new TextureView(requireContext());
        dynamicView.dynamicAddView(textureView);

        AgoraRteVideoCanvas videoCanvas = new AgoraRteVideoCanvas(textureView);
        videoCanvas.renderMode = renderMode;
        if (mLocalVideoTrack != null)
            mLocalVideoTrack.setPreviewCanvas(videoCanvas);
    }

}
