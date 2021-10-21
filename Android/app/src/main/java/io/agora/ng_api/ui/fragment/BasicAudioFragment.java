package io.agora.ng_api.ui.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import java.util.List;

import io.agora.ng_api.MyApp;
import io.agora.ng_api.R;
import io.agora.ng_api.base.BaseDemoFragment;
import io.agora.ng_api.databinding.FragmentBasicAudioBinding;
import io.agora.ng_api.view.ScrollableLinearLayout;
import io.agora.rte.AgoraRteSDK;
import io.agora.rte.media.stream.AgoraRtcStreamOptions;
import io.agora.rte.media.stream.AgoraRteMediaStreamInfo;
import io.agora.rte.scene.AgoraRteSceneConnState;
import io.agora.rte.scene.AgoraRteSceneEventHandler;

/**
 * This demo demonstrates how to make a Basic Audio Scene
 *
 * You need to complete 2 TODOS in this file
 */
public class BasicAudioFragment extends BaseDemoFragment<FragmentBasicAudioBinding> {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initListener();
        if (!MyApp.justDebugUIPart) {
            // TODO complete this method
            initAgoraRteSDK();
            // TODO complete this method
            joinScene();
        }
    }

    private void initView() {
    }

    private void initListener() {

        mAgoraHandler = new AgoraRteSceneEventHandler() {

            @Override
            public void onConnectionStateChanged(AgoraRteSceneConnState state, AgoraRteSceneConnState state1, io.agora.rte.scene.AgoraRteConnectionChangedReason reason) {
                // 连接建立完成
                /*
                    1. createOrUpdateRTCStream
                    2. addLocalAudioView 在本地显示一个 View 表示加入场景成功
                    3. initBasicLocalAudioTrack
                 */
                if (state1 == AgoraRteSceneConnState.CONN_STATE_CONNECTED && mLocalAudioTrack == null) {
                    // Step 1
                    // TODO You have joined this scene, try createOrUpdateRTCStream using mLocalStreamId
                    // Code here


                    // Step 2
                    addLocalAudioView();


                    // Step 3
                    // TODO complete initBasicLocalAudioTrack();
                    initBasicLocalAudioTrack();

                }
            }

            @Override
            public void onRemoteStreamAdded(List<AgoraRteMediaStreamInfo> list) {
                if (mBinding == null) return;
                for (AgoraRteMediaStreamInfo info : list) {
                    addRemoteAudioView(info);
                    // TODO subscribe remote audio stream in this scene
                    // Code here
                }
            }

            @Override
            public void onRemoteStreamRemoved(List<AgoraRteMediaStreamInfo> list) {
                for (AgoraRteMediaStreamInfo info : list) {
                    mBinding.containerBasicAudio.dynamicRemoveViewWithTag(info.getStreamId());
                    // TODO unsubscribe remote audio stream
                    // Code here
                }
            }

        };

    }

    /**
     * 初始化 AudioTrack
     * 1. init mLocalAudioTrack using {@link io.agora.rte.AgoraRteSDK}
     * 2. startRecording
     * 3. publish track in this scene using {@link BaseDemoFragment#mLocalStreamId}
     */
    private void initBasicLocalAudioTrack() {
        // TODO
        // CODE HERE
    }

    private void addLocalAudioView(){
        // 设置本地视图显示的名称
        String title = getString(R.string.local_user_id_format, mLocalUserId);
        // 工具方法，直接返回一个 CardView, 内部包含一个 TextView
        CardView cardView = ScrollableLinearLayout.getChildAudioCardView(requireContext(), null, title);
        // 工具方法，简化属性设置流程, 直接添加View
        mBinding.containerBasicAudio.dynamicAddView(cardView);
    }

    /**
     * Every time a user joined scene, we add a view
     * 每当有用户加入场景，在界面上添加一个View
     *
     * @param info 用户信息
     *             若为 null 则表示为本机用户，不会订阅音频流.
     *             if info is null, just add a view
     *             indicates that we have successfully
     *             joined this scene and will not subscribe
     *             a audio stream.
     */
    private void addRemoteAudioView(@NonNull AgoraRteMediaStreamInfo info) {
        // 用 streamId 作为 tag 标记每个用户
        String tag = info.getStreamId();
        // 本地视图 title 为 local_{userId}，远端为 {userId}
        String title = info.getUserId();
        // 工具方法，直接返回一个 CardView, 内部包含一个 TextView
        CardView cardView = ScrollableLinearLayout.getChildAudioCardView(requireContext(), tag, title);
        // 工具方法，简化属性设置流程, 直接添加View
        mBinding.containerBasicAudio.dynamicAddView(cardView);
    }

    private void joinScene() {
        doJoinScene(sceneName, mLocalUserId, getString(R.string.agora_access_token));
    }

    @Override
    public void doChangeView() {

    }
}