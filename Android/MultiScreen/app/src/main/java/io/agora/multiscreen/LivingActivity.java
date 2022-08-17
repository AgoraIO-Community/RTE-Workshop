package io.agora.multiscreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import io.agora.multiscreen.databinding.LivingActivityBinding;
import io.agora.multiscreen.databinding.LivingItemBinding;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.VideoCanvas;

public class LivingActivity extends AppCompatActivity {
    private static final String TAG = "LivingActivity";
    private static final String EXTRA_DATA_CHANNEL_ID = "ChannelId";
    private static final int SCREEN_SCREEN_UID_APPEND = 1000;

    private LivingActivityBinding mBinding;
    private String mChannelId;
    private LivingItemBinding[] mSeats;

    private RtcEngineEx rtcEngine;
    private RtcConnection mScreenConnection;
    private final int mMainUid = new Random(System.currentTimeMillis()).nextInt(SCREEN_SCREEN_UID_APPEND - 1);
    private ChannelMediaOptions mMainChannelOptions;
    private final IRtcEngineEventHandler mMainEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.d(TAG, "RtcEngineEventHandler >> onUserJoined uid=" + uid + ", elapsed=" + elapsed);
            if (isLocalUid(uid)) {
                return;
            }
            runOnUiThread(() -> upRemoteSeat(uid));
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            Log.d(TAG, "RtcEngineEventHandler >> onUserOffline uid=" + uid + ", reason=" + reason);
            if (isLocalUid(uid)) {
                return;
            }
            runOnUiThread(() -> downRemoteSeat(uid));
        }

        @Override
        public void onLocalVideoStateChanged(Constants.VideoSourceType source, int state, int error) {
            super.onLocalVideoStateChanged(source, state, error);
            if(source == Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY){
                if (state == Constants.LOCAL_VIDEO_STREAM_STATE_ENCODING) {
                    if (error == Constants.ERR_OK) {
                        runOnUiThread(() -> Toast.makeText(LivingActivity.this, "Screen sharing start successfully.", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(LivingActivity.this, "Screen sharing start failed for error " + error, Toast.LENGTH_LONG).show();
                            stopScreenCapture();
                            leaveScreenChannel();
                            downLocalSeat(getScreenUid(mMainUid));
                            mBinding.btnScreenShare.setActivated(false);
                        });
                    }
                } else if (state == Constants.LOCAL_AUDIO_STREAM_STATE_FAILED) {
                    if (error == Constants.ERR_SCREEN_CAPTURE_SYSTEM_NOT_SUPPORTED) {
                        runOnUiThread(() -> Toast.makeText(LivingActivity.this, "Screen sharing has been cancelled", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(LivingActivity.this, "Screen sharing start failed for error " + error, Toast.LENGTH_LONG).show());
                    }
                    runOnUiThread(() -> {
                        stopScreenCapture();
                        leaveScreenChannel();
                        downLocalSeat(getScreenUid(mMainUid));
                        mBinding.btnScreenShare.setActivated(false);
                    });
                }
            }
        }
    };

    public static Intent launch(Context context, String channelId) {
        Intent intent = new Intent(context, LivingActivity.class);
        intent.putExtra(EXTRA_DATA_CHANNEL_ID, channelId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = LivingActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        hideStatusBar(getWindow(), true);
        getWindow().getDecorView().setKeepScreenOn(true);
        mChannelId = getIntent().getStringExtra(EXTRA_DATA_CHANNEL_ID);
        mBinding.tvChannelId.setText(mChannelId);
        initView();
        initRtcEngine();
        upLocalSeat(mMainUid);
        joinMainChannel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveMainChannel();
        leaveScreenChannel();
        rtcEngine.stopPreview();
        RtcEngine.destroy();
    }

    public static void hideStatusBar(Window window, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        window.getDecorView().setSystemUiVisibility(flag |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void initRtcEngine() {
        RtcEngineConfig config = new RtcEngineConfig();
        config.mContext = getApplicationContext();
        config.mAppId = getString(R.string.agora_rtc_app_id);
        config.mEventHandler = mMainEventHandler;
        config.mAreaCode = RtcEngineConfig.AreaCode.AREA_CODE_GLOB;
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        try {
            rtcEngine = (RtcEngineEx) RtcEngine.create(config);
            rtcEngine.enableVideo();
            rtcEngine.enableAudio();
            rtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinMainChannel() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.autoSubscribeVideo = true;
        options.autoSubscribeAudio = true;
        options.publishCameraTrack = true;
        options.publishMicrophoneTrack = true;
        mMainChannelOptions = options;
        rtcEngine.joinChannel(getString(R.string.agora_rtc_access_token),
                mChannelId, mMainUid, options);
    }

    private void leaveMainChannel() {
        rtcEngine.leaveChannel();
    }

    private void startScreenCapture() {
        // TODO Practise 1：start screen sharing.

    }

    private void setupLocalScreenView(TextureView videoView){
        // TODO Practise 2：setup screen sharing preview.

    }

    private void stopScreenCapture() {
        // TODO Practise 3：stop screen sharing.

    }

    private void joinScreenChannel() {
        int screenUid = getScreenUid(mMainUid);
        // TODO Practise 4：join external channel and push screen sharing video source.
        // PS: mScreenConnection = xxx;

    }

    private void leaveScreenChannel() {
        // TODO Practise 5：leave external channel.
        // PS: mScreenConnection = null;

    }

    private int getScreenUid(int uid) {
        return uid + SCREEN_SCREEN_UID_APPEND;
    }

    private boolean isScreenUid(int uid) {
        return uid > SCREEN_SCREEN_UID_APPEND;
    }

    private boolean isLocalUid(int uid) {
        return uid == mMainUid || uid == getScreenUid(mMainUid);
    }

    private void initView() {
        mSeats = new LivingItemBinding[]{
                mBinding.container01,
                mBinding.container03,
                mBinding.container02,
                mBinding.container04
        };
        mBinding.btnClose.setOnClickListener(v -> finish());
        mBinding.btnScreenShare.setOnClickListener(v -> {
            boolean activated = v.isActivated();
            v.setActivated(!activated);
            if (activated) {
                stopScreenCapture();
                leaveScreenChannel();
                downLocalSeat(getScreenUid(mMainUid));
                mBinding.btnScreenShare.setActivated(false);
            } else {
                startScreenCapture();
                joinScreenChannel();
                upLocalSeat(getScreenUid(mMainUid));
            }
        });
        mBinding.btnMic.setActivated(true);
        mBinding.btnMic.setOnClickListener(v -> {
            boolean activated = v.isActivated();
            v.setActivated(!activated);
            boolean enableAudio = !activated;
            mMainChannelOptions.publishMicrophoneTrack = enableAudio;
            rtcEngine.updateChannelMediaOptions(mMainChannelOptions);
            if(enableAudio){
                rtcEngine.enableAudio();
            }else{
                rtcEngine.disableAudio();
            }
        });
        mBinding.btnVideo.setActivated(true);
        mBinding.btnVideo.setOnClickListener(v -> {
            boolean activated = v.isActivated();
            v.setActivated(!activated);
            boolean enableVideo = !activated;
            mMainChannelOptions.publishCameraTrack = enableVideo;
            rtcEngine.updateChannelMediaOptions(mMainChannelOptions);
            if(enableVideo){
                rtcEngine.enableVideo();
            }else{
                rtcEngine.disableVideo();
            }
        });
    }


    private LivingItemBinding getIdleSeat() {
        for (LivingItemBinding seat : mSeats) {
            if (seat.getRoot().getTag() == null) {
                return seat;
            }
        }
        return null;
    }

    private LivingItemBinding getSeatById(int uid) {
        for (LivingItemBinding seat : mSeats) {
            if (seat.getRoot().getTag() instanceof Integer && uid == (Integer) seat.getRoot().getTag()) {
                return seat;
            }
        }
        return null;
    }

    private void upLocalSeat(int uid) {
        boolean isScreenUid = isScreenUid(uid);
        int seatUid = isScreenUid ? uid - SCREEN_SCREEN_UID_APPEND : uid;

        LivingItemBinding seat = getSeatById(seatUid);
        if (seat == null) {
            seat = getIdleSeat();
            if (seat == null) {
                return;
            }
        }
        seat.getRoot().setVisibility(View.VISIBLE);
        seat.getRoot().setTag(seatUid);

        seat.largeContainer.removeAllViews();
        TextureView renderView = new TextureView(this);
        seat.largeContainer.addView(renderView);
        if (isScreenUid) {

            setupLocalScreenView(renderView);

            seat.smallContainer.setVisibility(View.VISIBLE);
            seat.smallContainer.removeAllViews();
            renderView = new TextureView(this);
            seat.smallContainer.addView(renderView);
            enableSmallLargeViewSwitch(seat);
        } else {
            seat.smallContainer.setVisibility(View.GONE);
        }
        seat.tvSeatUid.setText( String.valueOf(seatUid));
        rtcEngine.setupLocalVideo(new VideoCanvas(renderView,
                Constants.RENDER_MODE_HIDDEN,
                Constants.VIDEO_MIRROR_MODE_AUTO,
                Constants.VIDEO_SOURCE_CAMERA_PRIMARY,
                seatUid));
        rtcEngine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_CAMERA_PRIMARY);
    }

    private void downLocalSeat(int uid) {
        boolean isScreenUid = isScreenUid(uid);
        int seatUid = isScreenUid ? uid - SCREEN_SCREEN_UID_APPEND : uid;
        LivingItemBinding seat = getSeatById(seatUid);
        if (seat != null) {
            if (isScreenUid) {
                seat.smallContainer.setVisibility(View.GONE);
                seat.smallContainer.removeAllViews();
                seat.largeContainer.removeAllViews();

                TextureView renderView = new TextureView(this);
                seat.largeContainer.addView(renderView);

                rtcEngine.stopPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);
                rtcEngine.setupLocalVideo(new VideoCanvas(renderView, Constants.RENDER_MODE_HIDDEN, Constants.VIDEO_MIRROR_MODE_AUTO,
                        Constants.VIDEO_SOURCE_CAMERA_PRIMARY, seatUid));
            } else {
                seat.smallContainer.removeAllViews();
                seat.largeContainer.removeAllViews();
                seat.getRoot().setTag(null);
                seat.getRoot().setVisibility(View.GONE);
            }
        }
    }

    private void upRemoteSeat(int uid) {
        boolean isScreenUid = isScreenUid(uid);
        int seatUid = isScreenUid ? uid - SCREEN_SCREEN_UID_APPEND : uid;

        LivingItemBinding seat = getSeatById(seatUid);
        if (seat == null) {
            seat = getIdleSeat();
            if (seat == null) {
                return;
            }
        }
        seat.getRoot().setTag(seatUid);
        seat.getRoot().setVisibility(View.VISIBLE);

        seat.largeContainer.removeAllViews();
        TextureView renderView = new TextureView(this);
        seat.largeContainer.addView(renderView);
        if (isScreenUid) {
            rtcEngine.setupRemoteVideo(new VideoCanvas(renderView, Constants.RENDER_MODE_FIT, uid));
            seat.smallContainer.removeAllViews();
            renderView = new TextureView(this);
            seat.smallContainer.setVisibility(View.VISIBLE);
            seat.smallContainer.addView(renderView);
            enableSmallLargeViewSwitch(seat);
        } else {
            seat.smallContainer.setVisibility(View.GONE);
        }
        seat.tvSeatUid.setText(String.valueOf(seatUid));
        rtcEngine.setupRemoteVideo(new VideoCanvas(renderView, Constants.RENDER_MODE_HIDDEN, seatUid));
    }


    private void downRemoteSeat(int uid) {
        boolean isScreenUid = isScreenUid(uid);
        int seatUid = isScreenUid ? uid - SCREEN_SCREEN_UID_APPEND : uid;

        LivingItemBinding seat = getSeatById(seatUid);
        if (seat != null) {
            if (isScreenUid) {
                seat.smallContainer.setVisibility(View.GONE);
                seat.smallContainer.removeAllViews();
                seat.largeContainer.removeAllViews();

                TextureView renderView = new TextureView(this);
                seat.largeContainer.addView(renderView);

                rtcEngine.setupRemoteVideo(new VideoCanvas(renderView, Constants.RENDER_MODE_HIDDEN, seatUid));
            } else {
                seat.smallContainer.removeAllViews();
                seat.largeContainer.removeAllViews();
                seat.getRoot().setTag(null);
                seat.getRoot().setVisibility(View.GONE);
                reOrderSeats();
            }
        }
    }

    private void enableSmallLargeViewSwitch(LivingItemBinding seat) {
        if (seat.smallContainer.getChildCount() > 0) {
            seat.smallContainer.getChildAt(0).setOnClickListener(v -> {
                if (seat.smallContainer.getChildCount() > 0 && seat.largeContainer.getChildCount() > 0) {
                    View smallView = seat.smallContainer.getChildAt(0);
                    View largeView = seat.largeContainer.getChildAt(0);
                    seat.smallContainer.removeAllViews();
                    seat.largeContainer.removeAllViews();
                    seat.smallContainer.addView(largeView);
                    seat.largeContainer.addView(smallView);
                }
            });
        }
    }

    private void reOrderSeats(){
        for (int i = 0; i < mSeats.length; i++) {
            LivingItemBinding seat = mSeats[i];
            if (seat.getRoot().getTag() == null) {
                LivingItemBinding replaceSeat = null;
                for (int j = i + 1; j < mSeats.length; j++) {
                    LivingItemBinding _seat = mSeats[j];
                    if(_seat.getRoot().getTag() != null){
                        replaceSeat = _seat;
                        break;
                    }
                }
                if (replaceSeat != null) {
                    Object tag = replaceSeat.getRoot().getTag();
                    View largeView = replaceSeat.largeContainer.getChildAt(0);
                    View smallView = replaceSeat.smallContainer.getChildCount() > 0 ? replaceSeat.smallContainer.getChildAt(0) : null;
                    String seatUid = replaceSeat.tvSeatUid.getText().toString();
                    replaceSeat.largeContainer.removeAllViews();
                    replaceSeat.smallContainer.removeAllViews();
                    replaceSeat.getRoot().setVisibility(View.GONE);
                    replaceSeat.getRoot().setTag(null);

                    seat.getRoot().setVisibility(View.VISIBLE);
                    seat.getRoot().setTag(tag);
                    seat.largeContainer.addView(largeView);
                    seat.tvSeatUid.setText(seatUid);
                    if(smallView != null){
                        seat.smallContainer.setVisibility(View.VISIBLE);
                        seat.smallContainer.addView(smallView);
                        enableSmallLargeViewSwitch(seat);
                    }else{
                        seat.smallContainer.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}
