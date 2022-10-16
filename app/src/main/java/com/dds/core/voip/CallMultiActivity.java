package com.dds.core.voip;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dds.core.base.BaseActivity;
import com.dds.permission.Permissions;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.skywebrtc.except.NotInitializedException;
import com.dds.webrtc.R;

import java.util.UUID;

/**
 * Created by dds on 2018/7/26.
 * 다중 통화
 */
public class CallMultiActivity extends BaseActivity implements CallSession.CallSessionCallback, View.OnClickListener {
    private SkyEngineKit gEngineKit;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageView meetingHangupImageView;
    private CallSession.CallSessionCallback currentFragment;
    public static final String EXTRA_MO = "isOutGoing";
    private boolean isOutgoing;


    public static void openActivity(Activity activity, String room, boolean isOutgoing) {
        Intent intent = new Intent(activity, CallMultiActivity.class);
        intent.putExtra("room", room);
        intent.putExtra(EXTRA_MO, isOutgoing);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_multi_call);
        initView();
        initListener();
        initData();
    }

    @Override
    public void onBackPressed() {

    }
    private void initView() {
        meetingHangupImageView = findViewById(R.id.meetingHangupImageView);
        Fragment fragment = new FragmentMeeting();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.meeting_container, fragment)
                .commit();
        currentFragment = (CallSession.CallSessionCallback) fragment;
    }

    private void initListener() {
        meetingHangupImageView.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        String room = intent.getStringExtra("room");
        isOutgoing = intent.getBooleanExtra(EXTRA_MO, false);
        try {
            gEngineKit = SkyEngineKit.Instance();
        } catch (NotInitializedException e) {
            finish();
        }
        String[] per = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        Permissions.request(this, per, integer -> {
            if (integer == 0) {
                // 权限同意
                init(room, isOutgoing);
            } else {
                // 权限拒绝
                CallMultiActivity.this.finish();
            }
        });


    }

    private void init(String room, boolean isOutgoing) {
        SkyEngineKit.init(new VoipEvent());
        if (isOutgoing) {
            // 방 만들고 들어감
//            UUID.randomUUID().toString().substring(0, 16);
            String roomcode = "testing";

            //랜덤 방 코드 생성
            Toast.makeText(getBaseContext(), roomcode, Toast.LENGTH_LONG).show();

            gEngineKit.createAndJoinRoom(this,
                    "room-" + roomcode);
        } else {
            // 방에 들어감
            gEngineKit.joinRoom(getApplicationContext(), room);
        }


        CallSession session = gEngineKit.getCurrentSession();
        if (session == null) {
            this.finish();
        } else {
            session.setSessionCallback(this);

        }


    }


    public SkyEngineKit getEngineKit() {
        return gEngineKit;
    }


    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }


    //-------------------------------------------------콜백 관련------------------------------------
    @Override
    public void didCallEndWithReason(EnumType.CallEndReason var1) {
        finish();
    }

    @Override
    public void didChangeState(EnumType.CallState callState) {
        handler.post(() -> currentFragment.didChangeState(callState));
    }

    @Override
    public void didChangeMode(boolean var1) {
        handler.post(() -> currentFragment.didChangeMode(var1));
    }

    @Override
    public void didCreateLocalVideoTrack() {
        handler.post(() -> currentFragment.didCreateLocalVideoTrack());
    }

    @Override
    public void didReceiveRemoteVideoTrack(String userId) {
        handler.post(() -> currentFragment.didReceiveRemoteVideoTrack(userId));
    }

    @Override
    public void didUserLeave(String userId) {
        handler.post(() -> currentFragment.didUserLeave(userId));
    }

    @Override
    public void didError(String var1) {
        finish();
    }

    @Override
    public void didDisconnected(String userId) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.meetingHangupImageView:
                handleHangup();
                break;
        }

    }

    // 处理挂断事件
    private void handleHangup() {
        SkyEngineKit.Instance().leaveRoom();
        this.finish();
    }
}
