package io.agora.ng_api.ui.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

import io.agora.ng_api.MyApp;
import io.agora.ng_api.R;
import io.agora.ng_api.base.BaseDemoFragment;
import io.agora.ng_api.databinding.FragmentScreenShareBinding;
import io.agora.ng_api.service.MediaProjectFgService;
import io.agora.ng_api.view.DynamicView;
import io.agora.rte.AgoraRteSDK;
import io.agora.rte.media.stream.AgoraRtcStreamOptions;
import io.agora.rte.media.stream.AgoraRteMediaStreamInfo;
import io.agora.rte.media.track.AgoraRteScreenVideoTrack;
import io.agora.rte.media.video.AgoraRteVideoCanvas;
import io.agora.rte.media.video.AgoraRteVideoEncoderConfiguration;
import io.agora.rte.media.video.AgoraRteVideoSubscribeOptions;
import io.agora.rte.scene.AgoraRteConnectionChangedReason;
import io.agora.rte.scene.AgoraRteSceneConnState;
import io.agora.rte.scene.AgoraRteSceneEventHandler;

/**
 * This demo demonstrates how to push a local screen record video stream in a existing video scene
 * Make sure you have finished the BasicAudioFragment and BasicVideoFragment
 *
 * 此 Demo 主要展示在音视频通话的场景中推送本机屏幕录像
 * 请确保之前的 Basic 的两个示例已经完成
 * 包括{@link BaseDemoFragment#initLocalAudioTrack()}以及{@link BaseDemoFragment#initLocalVideoTrack(DynamicView)}哦
 */
public class ScreenShareFragment extends BaseDemoFragment<FragmentScreenShareBinding> {

    private Intent mediaProjectionIntent;

    @Nullable
    private AgoraRteScreenVideoTrack screenVideoTrack;
    // 请求权限
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initView();
            initListener();
            if (!MyApp.justDebugUIPart) {
                initAgoraRteSDK();
                joinScene();
            }
        } else {
            MyApp.getInstance().shortToast(R.string.screen_share_version_unsupported);
            getNavController().popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().stopService(mediaProjectionIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        mediaProjectionIntent = new Intent(requireActivity(), MediaProjectFgService.class);

        mBinding.btnOpenFgScreenShare.addOnCheckedChangeListener((button, isChecked) -> {
            // Must first startService then request permission
            screenCaptureOperation(isChecked);
            if (isChecked) {
                MediaProjectionManager mpm = (MediaProjectionManager) requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                Intent intent = mpm.createScreenCaptureIntent();
                activityResultLauncher.launch(intent);
            }
        });
    }


    private void initListener() {

        // handle request screen record callback
        // since onActivityResult() is deprecated
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                createScreenVideoTrack(result.getData());
            } else {
                mBinding.btnOpenFgScreenShare.toggle();
                MyApp.getInstance().shortToast(R.string.screen_share_request_denied);
            }
        });

        // Just a regular event handler
        mAgoraHandler = new AgoraRteSceneEventHandler() {
            @Override
            public void onConnectionStateChanged(AgoraRteSceneConnState oldState, AgoraRteSceneConnState newState, AgoraRteConnectionChangedReason reason) {
                super.onConnectionStateChanged(oldState, newState, reason);
                if (newState == AgoraRteSceneConnState.CONN_STATE_CONNECTED && mLocalAudioTrack == null) {
                    // Step 1
                    // TODO createOrUpdateRTCStream
                    // Code here

                    // Step 2
                    initLocalAudioTrack();
                    // Step 3
                    initLocalVideoTrack(mBinding.containerFgScreenShare);

                    mBinding.btnOpenFgScreenShare.setEnabled(true);
                }
            }

            @Override
            public void onRemoteStreamAdded(List<AgoraRteMediaStreamInfo> streams) {
                for (AgoraRteMediaStreamInfo stream : streams) {
                    addRemoteView(stream.getStreamId());
                    // TODO subscribe Audio and Video
                    // Code here
                }
            }

            @Override
            public void onRemoteStreamRemoved(List<AgoraRteMediaStreamInfo> streams) {
                for (AgoraRteMediaStreamInfo stream : streams) {
                    mBinding.containerFgScreenShare.dynamicRemoveViewWithTag(stream.getStreamId());
                    // TODO unsubscribe Audio and Video
                    // Code here
                }
            }
        };
    }

    private void screenCaptureOperation(boolean turnOn) {
        if (turnOn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(mediaProjectionIntent);
            } else {
                requireContext().startService(mediaProjectionIntent);
            }
        } else {
            // stop screen capture and update options
            // 用户取消屏幕录制
            if (screenVideoTrack != null) {
                mScene.unpublishLocalVideoTrack(screenVideoTrack);
                screenVideoTrack.stopCapture();
                mBinding.containerFgScreenShare.dynamicRemoveViewWithTag(mLocalMediaStreamId);
                requireActivity().stopService(mediaProjectionIntent);
            }
        }
    }

    /**
     * Total 4 Steps
     * 1: createScreenVideoTrack
     * 2: setPreviewCanvas
     * 3: startCaptureScreen
     * 4: publishLocalVideoTrack
     */
    private void createScreenVideoTrack(Intent intent) {
        // Add View
        TextureView textureView = new TextureView(requireContext());
        textureView.setTag(mLocalMediaStreamId);
        mBinding.containerFgScreenShare.dynamicAddView(textureView);
        // Create preview canvas
        AgoraRteVideoCanvas canvas = new AgoraRteVideoCanvas(textureView);

        // Step 1 TODO
        // Code here


        // Step 2
        screenVideoTrack.setPreviewCanvas(canvas);

        mScene.createOrUpdateRTCStream(mLocalMediaStreamId, new AgoraRtcStreamOptions());

        // Step 3 TODO
        // Code here

        // Step 4 TODO
        // Code here
    }

    /**
     * Add view to show remote stream data
     *
     * @param streamId related remote streamID
     */
    private void addRemoteView(String streamId) {
        if(mBinding.containerFgScreenShare.findViewWithTag(streamId) != null) return;

        TextureView view = new TextureView(requireContext());
        view.setTag(streamId);
        mBinding.containerFgScreenShare.dynamicAddView(view);
        AgoraRteVideoCanvas canvas = new AgoraRteVideoCanvas(view);
        mScene.setRemoteVideoCanvas(streamId, canvas);
    }

    private void joinScene() {
        doJoinScene(sceneName, mLocalUserId, getString(R.string.agora_access_token));
    }

    @Override
    public void doChangeView() {

    }
}