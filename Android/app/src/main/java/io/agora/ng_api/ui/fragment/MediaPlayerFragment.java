package io.agora.ng_api.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.view.TextureView;
import android.view.View;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;

import java.util.List;

import io.agora.ng_api.MyApp;
import io.agora.ng_api.R;
import io.agora.ng_api.base.BaseDemoFragment;
import io.agora.ng_api.databinding.FragmentMediaPlayerBinding;
import io.agora.ng_api.util.ExampleUtil;
import io.agora.ng_api.view.DynamicView;
import io.agora.ng_api.view.VideoView;
import io.agora.rte.AgoraRteSDK;
import io.agora.rte.media.data.AgoraRteMediaPlayerObserver;
import io.agora.rte.media.data.AgoraRteVideoFrame;
import io.agora.rte.media.media_player.AgoraRteFileInfo;
import io.agora.rte.media.media_player.AgoraRteMediaPlayer;
import io.agora.rte.media.media_player.AgoraRteMediaPlayerError;
import io.agora.rte.media.media_player.AgoraRteMediaPlayerState;
import io.agora.rte.media.stream.AgoraRtcStreamOptions;
import io.agora.rte.media.stream.AgoraRteMediaStreamInfo;
import io.agora.rte.media.video.AgoraRteVideoCanvas;
import io.agora.rte.media.video.AgoraRteVideoSubscribeOptions;
import io.agora.rte.scene.AgoraRteConnectionChangedReason;
import io.agora.rte.scene.AgoraRteSceneConnState;
import io.agora.rte.scene.AgoraRteSceneEventHandler;

/**
 * This demo demonstrates how to push a online video stream in a existing video scene
 * Make sure you have finished the BasicAudioFragment and BasicVideoFragment
 *
 * 此 Demo 主要展示在音视频通话的场景中推送在线视频
 * 请确保之前的 Basic 的两个示例已经完成
 * 包括{@link BaseDemoFragment#initLocalAudioTrack()}以及{@link BaseDemoFragment#initLocalVideoTrack(DynamicView)}哦
 */
public class MediaPlayerFragment extends BaseDemoFragment<FragmentMediaPlayerBinding> {
    private AgoraRteMediaPlayer mPlayer;
    private AgoraRteMediaPlayerObserver mPlayerObserver;
    private VideoView mVideoView;

    boolean initVideoView = false;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        initListener();

        if (!MyApp.justDebugUIPart) {
            initAgoraRteSDK();
            joinScene();
        }
    }

    @Override
    public void onDestroyView() {
        if (mPlayer != null) {
            mPlayer.unregisterMediaPlayerObserver(mPlayerObserver);
            mPlayer.destroy();
        }
        super.onDestroyView();
    }

    private void initView() {
        // 点击按钮播放 url 文件
        mBinding.btnOpenFgPlayer.setOnClickListener(v -> openURL());
        // 初始化 视频控制View
        mVideoView = new VideoView(requireContext());
        // 设置播放按钮事件
        mVideoView.mPlayBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            mVideoView.showOverlay();
            if (isChecked) mPlayer.play();
            else mPlayer.pause();
        });
        // 设置拖动条拖动事件
        mVideoView.mProgressSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mPlayer.seek((long) slider.getValue());
            }
        });
    }

    private void initListener() {
        mPlayerObserver = new AgoraRteMediaPlayerObserver() {
            @Override
            public void onVideoFrame(AgoraRteFileInfo fileInfo, AgoraRteVideoFrame videoFrame) {
                // We want the playerView's size matches the video frame's size,
                // so when the first frame shows we adjust view's size immediately.
                // 我们希望视图的宽高与视频的宽高匹配
                // 所以当第一帧出现时立即改变视图宽高
                if(!initVideoView){
                    initVideoView = true;

//                    new Handler(Looper.getMainLooper()).postAtFrontOfQueue(() -> {
//                        mVideoView.mTextureView.getLayoutParams().height = mVideoView.mTextureView.getMeasuredWidth() * videoFrame.getHeight() / videoFrame.getWidth();
//                        mVideoView.mTextureView.requestLayout();
//                    });
                }
            }

            @Override
            public void onPlayerStateChanged(AgoraRteFileInfo fileInfo, AgoraRteMediaPlayerState state, AgoraRteMediaPlayerError error) {
                ExampleUtil.utilLog(fileInfo.beginTime + "/" + fileInfo.duration + "state: " + state + ", error: " + error);
                /*
                    监听到文件打开成功
                    1. Duration setup
                    2. Start to play
                    3. Hide the loading
                 */
                if (state == AgoraRteMediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                    // Step 1
                    mVideoView.mProgressSlider.setValueTo(mPlayer.getDuration());
                    // Step 2
                    mPlayer.play();
                    // Step 3
                    mVideoView.mLoadingView.setVisibility(View.GONE);
                } else if (state == AgoraRteMediaPlayerState.PLAYER_STATE_PLAYBACK_COMPLETED) {
                    // Play COMPLETED -> set the play button state to pause.
                    mVideoView.mPlayBtn.setChecked(false);
                }
            }

            @Override
            public void onPositionChanged(AgoraRteFileInfo fileInfo, long position) {
                super.onPositionChanged(fileInfo, position);
                // Indicate current position is changed
                if(!mVideoView.mProgressSlider.isPressed())
                    mVideoView.mProgressSlider.setValue(position);
            }

        };
        mAgoraHandler = new AgoraRteSceneEventHandler() {
            @Override
            public void onConnectionStateChanged(AgoraRteSceneConnState oldState, AgoraRteSceneConnState newState, AgoraRteConnectionChangedReason reason) {
                if (newState == AgoraRteSceneConnState.CONN_STATE_CONNECTED && mLocalAudioTrack == null) {
                    ExampleUtil.utilLog("onConnectionStateChanged,Thread:"+Thread.currentThread().getName());
                    // 连接建立完成 初始化 localAudioTrack、localVideoTrack
                    /*
                        1. CreateOrUpdateRTCStream
                        2. InitLocalAudioTrack
                        3. InitLocalVideoTrack
                        4. InitAgoraMediaPlayer
                        5. Enable button to
                    */

                    // TODO createOrUpdateRTCStream
                    // Step 1
                    // Code here

                    // Step 2
                    initLocalAudioTrack();
                    // Step 3
                    initLocalVideoTrack(mBinding.containerFgPlayer);

                    // Step 4
                    initAgoraMediaPlayer(); // TODO finish this method
                    mBinding.btnOpenFgPlayer.setEnabled(true);
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
                    mBinding.containerFgPlayer.dynamicRemoveViewWithTag(stream.getStreamId());
                    // TODO unsubscribe Audio and Video
                    // Code here
                }
            }
        };
    }

    /**
     * 1. create media player
     * 2. registerMediaPlayerObserver
     */
    private void initAgoraMediaPlayer(){
        // TODO
        // Code here
    }

    /**
     * Just a examination for the url
     *
     * Step 1. Check whether the url is valid
     * Step 2. Valid -> play, invalid -> alert user
     */
    private void openURL() {
        boolean valid = false;
        String url = "";
        Editable editable = mBinding.inputUrlFgPlayer.getText();
        if (editable != null) {
            url = editable.toString();
            valid = URLUtil.isValidUrl(url);
        }

        if (valid) {
            doOpenURL(url);
            mBinding.btnOpenFgPlayer.setEnabled(false);
        }
        else ExampleUtil.shakeViewAndVibrateToAlert(mBinding.layoutInputUrlFgPlayer);
    }

    /**
     * 1. add view
     * 2. open file
     * 3. createOrUpdateRTCStream
     * 4. publish media player
     */
    private void doOpenURL(String url) {
        addMediaView();
        mPlayer.open(url, 0);

        // TODO (note that you need to use streamId different from the id you published localXXXTrack)
        // TODO (注意这里不能与发布本地Track的streamId一样)

        // mLocalMediaStreamId is recommended
        // 推荐直接用 mLocalMediaStreamId
        // Code here
    }

    private void joinScene() {
        doJoinScene(sceneName, mLocalUserId, getString(R.string.agora_access_token));
    }

    /**
     * Add a view which contains some video control stuff like play button and progress bar
     */
    private void addMediaView() {
        mBinding.containerFgPlayer.dynamicAddView(mVideoView);
        mPlayer.setView(mVideoView.mTextureView);
    }


    /**
     * Add a view to preview remote video stream
     */
    private void addRemoteView(String streamId) {
        TextureView view = new TextureView(requireContext());
        view.setTag(streamId);
        mBinding.containerFgPlayer.dynamicAddView(view);
        AgoraRteVideoCanvas canvas = new AgoraRteVideoCanvas(view);
        mScene.setRemoteVideoCanvas(streamId, canvas);
    }

    @Override
    public void doChangeView() {

    }
}
