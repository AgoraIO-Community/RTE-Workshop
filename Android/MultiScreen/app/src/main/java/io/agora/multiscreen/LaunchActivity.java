package io.agora.multiscreen;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.agora.multiscreen.databinding.LaunchActivityBinding;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LaunchActivity extends AppCompatActivity {
    private static final int RC_CAMERA_AND_LOCATION = 100;
    private LaunchActivityBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = LaunchActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        hideStatusBar(getWindow(), false);

        mBinding.btnJoin.setOnClickListener(v -> requestPermissions());
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


    private void launchLiving() {
        String channelId = mBinding.etChannelId.getText().toString();
        if (TextUtils.isEmpty(channelId)) {
            Toast.makeText(LaunchActivity.this, "The channel id must not be null!", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(LivingActivity.launch(LaunchActivity.this, channelId));
    }

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            launchLiving();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "",
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
