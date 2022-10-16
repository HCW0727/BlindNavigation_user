package com.dds.core.voip;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dds.core.util.BarUtils;
import com.dds.core.util.OSUtils;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType.CallState;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.R;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapView;

import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;

/**
 * Created by dds on 2018/7/26.
 * android_shuai@163.com
 * 视频通话控制界面
 */
public class FragmentVideo extends SingleCallFragment implements View.OnClickListener , TMapGpsManager.onLocationChangedCallback{

    public      static String  mApiKey ="cd801069-d783-4402-a170-dcef9b3fe221";   //' "38c7269d-5eb5-4739-b305-9886986b658f"; // 발급받은 appKey
    ListView listView1;
    ArrayAdapter<String> adapter;
    ArrayList<String> listItem;
    VideoView videoView;

    private static final String TAG = "FragmentVideo";
    private ImageView outgoingAudioOnlyImageView;
    private LinearLayout audioLayout;
    private ImageView incomingAudioOnlyImageView;
    private LinearLayout hangupLinearLayout;
    private LinearLayout acceptLinearLayout;
    private ImageView connectedAudioOnlyImageView;
    private ImageView connectedHangupImageView;
    private ImageView switchCameraImageView;
    private FrameLayout fullscreenRenderer;
    private FrameLayout pipRenderer;
    private LinearLayout inviteeInfoContainer;
    private boolean isFromFloatingView = false;
    private SurfaceViewRenderer localSurfaceView;
    private SurfaceViewRenderer remoteSurfaceView;

    private TMapView mMapView = null;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (callSingleActivity != null) {
            isFromFloatingView = callSingleActivity.isFromFloatingView();
        }
    }

    @Override
    int getLayout() {
        return R.layout.fragment_video;
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        fullscreenRenderer = view.findViewById(R.id.fullscreen_video_view);
        pipRenderer = view.findViewById(R.id.pip_video_view);
        inviteeInfoContainer = view.findViewById(R.id.inviteeInfoContainer);
        outgoingAudioOnlyImageView = view.findViewById(R.id.outgoingAudioOnlyImageView);
        audioLayout = view.findViewById(R.id.audioLayout);
        incomingAudioOnlyImageView = view.findViewById(R.id.incomingAudioOnlyImageView);
        hangupLinearLayout = view.findViewById(R.id.hangupLinearLayout);
        acceptLinearLayout = view.findViewById(R.id.acceptLinearLayout);
        connectedAudioOnlyImageView = view.findViewById(R.id.connectedAudioOnlyImageView);
        connectedHangupImageView = view.findViewById(R.id.connectedHangupImageView);
        switchCameraImageView = view.findViewById(R.id.switchCameraImageView);
        outgoingHangupImageView.setOnClickListener(this);
        incomingHangupImageView.setOnClickListener(this);
        minimizeImageView.setOnClickListener(this);
        connectedHangupImageView.setOnClickListener(this);
        acceptImageView.setOnClickListener(this);
        switchCameraImageView.setOnClickListener(this);
        pipRenderer.setOnClickListener(this);
        outgoingAudioOnlyImageView.setOnClickListener(this);
        incomingAudioOnlyImageView.setOnClickListener(this);
        connectedAudioOnlyImageView.setOnClickListener(this);

        //-----------
        CallSession session = gEngineKit.getCurrentSession();
        session.toggleSpeaker( true );
        //-----------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || OSUtils.isMiui() || OSUtils.isFlyme()) {
            lytParent.post(() -> {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) inviteeInfoContainer.getLayoutParams();
                params.topMargin = (int) (BarUtils.getStatusBarHeight() * 1.2);
                inviteeInfoContainer.setLayoutParams(params);
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) minimizeImageView.getLayoutParams();
                params1.topMargin = BarUtils.getStatusBarHeight();
                minimizeImageView.setLayoutParams(params1);
            });

            pipRenderer.post(() -> {
                FrameLayout.LayoutParams params2 = (FrameLayout.LayoutParams) pipRenderer.getLayoutParams();
                params2.topMargin = (int) (BarUtils.getStatusBarHeight() * 1.2);
                pipRenderer.setLayoutParams(params2);
            });
        }
//        if(isOutgoing){ //测试崩溃对方是否会停止
//            lytParent.postDelayed(() -> {
//                int i = 1 / 0;
//            }, 10000);
//        }




//        mMapView = view.findViewById(R.id.t_view);
//        mMapView.setSKTMapApiKey(mApiKey);





    }


    @Override
    public void init() {
        super.init();
        CallSession session = gEngineKit.getCurrentSession();
        if (session != null) {
            currentState = session.getState();
        }
        if (session == null || CallState.Idle == session.getState()) {
            if (callSingleActivity != null) {
                callSingleActivity.finish();
            }
        } else if (CallState.Connected == session.getState()) {
            incomingActionContainer.setVisibility(View.GONE);
            outgoingActionContainer.setVisibility(View.GONE);
            connectedActionContainer.setVisibility(View.VISIBLE);
            inviteeInfoContainer.setVisibility(View.GONE);
            minimizeImageView.setVisibility(View.VISIBLE);
            startRefreshTime();
        } else {
            if (isOutgoing) {
                incomingActionContainer.setVisibility(View.GONE);
                outgoingActionContainer.setVisibility(View.VISIBLE);
                connectedActionContainer.setVisibility(View.GONE);
                descTextView.setText(R.string.av_waiting);
            } else {
                incomingActionContainer.setVisibility(View.VISIBLE);
                outgoingActionContainer.setVisibility(View.GONE);
                connectedActionContainer.setVisibility(View.GONE);
                descTextView.setText(R.string.av_video_invite);
                if (currentState == CallState.Incoming) {
                    View surfaceView = gEngineKit.getCurrentSession().setupLocalVideo(false);
                    Log.d(TAG, "init surfaceView != null is " + (surfaceView != null) + "; isOutgoing = " + isOutgoing + "; currentState = " + currentState);
                    if (surfaceView != null) {
                        localSurfaceView = (SurfaceViewRenderer) surfaceView;
                        localSurfaceView.setZOrderMediaOverlay(false);
                        fullscreenRenderer.addView(localSurfaceView);
                    }
                }
            }
        }
        if (isFromFloatingView) {
            didCreateLocalVideoTrack();
            if (session != null) {
                didReceiveRemoteVideoTrack(session.mTargetId);
            }
        }
    }

    @Override
    public void didChangeState(CallState state) {
        currentState = state;
        Log.d(TAG, "didChangeState, state = " + state);
        runOnUiThread(() -> {
            if (state == CallState.Connected) {

                incomingActionContainer.setVisibility(View.GONE);
                outgoingActionContainer.setVisibility(View.GONE);
                connectedActionContainer.setVisibility(View.VISIBLE);
                inviteeInfoContainer.setVisibility(View.GONE);
                descTextView.setVisibility(View.GONE);
                minimizeImageView.setVisibility(View.VISIBLE);
                // 开启计时器
                startRefreshTime();
            } else {
                // do nothing now
            }
        });
    }

    @Override
    public void didChangeMode(Boolean isAudio) {
        runOnUiThread(() -> callSingleActivity.switchAudio());
    }


    @Override
    public void didCreateLocalVideoTrack() {
        if (localSurfaceView == null) {
            View surfaceView = gEngineKit.getCurrentSession().setupLocalVideo(true);
            if (surfaceView != null) {
                localSurfaceView = (SurfaceViewRenderer) surfaceView;
            } else {
                if (callSingleActivity != null) callSingleActivity.finish();
                return;
            }
        } else {
            localSurfaceView.setZOrderMediaOverlay(true);
        }
        Log.d(TAG,
                "didCreateLocalVideoTrack localSurfaceView != null is " + (localSurfaceView != null) + "; remoteSurfaceView == null = " + (remoteSurfaceView == null)
        );

        if (localSurfaceView.getParent() != null) {
            ((ViewGroup) localSurfaceView.getParent()).removeView(localSurfaceView);
        }
        if (isOutgoing && remoteSurfaceView == null) {
            if (fullscreenRenderer != null && fullscreenRenderer.getChildCount() != 0)
                fullscreenRenderer.removeAllViews();
            fullscreenRenderer.addView(localSurfaceView);
        } else {
            if (pipRenderer.getChildCount() != 0) pipRenderer.removeAllViews();
            pipRenderer.addView(localSurfaceView);
        }
    }


    @Override
    public void didReceiveRemoteVideoTrack(String userId) {
        pipRenderer.setVisibility(View.VISIBLE);
        if (localSurfaceView != null) {
            localSurfaceView.setZOrderMediaOverlay(true);
            if (isOutgoing) {
                if (localSurfaceView.getParent() != null) {
                    ((ViewGroup) localSurfaceView.getParent()).removeView(localSurfaceView);
                }
                pipRenderer.addView(localSurfaceView);
            }
        }


        View surfaceView = gEngineKit.getCurrentSession().setupRemoteVideo(userId, false);
        Log.d(TAG, "didReceiveRemoteVideoTrack,surfaceView = " + surfaceView);
        if (surfaceView != null) {
            fullscreenRenderer.setVisibility(View.VISIBLE);
            remoteSurfaceView = (SurfaceViewRenderer) surfaceView;
            fullscreenRenderer.removeAllViews();
            if (remoteSurfaceView.getParent() != null) {
                ((ViewGroup) remoteSurfaceView.getParent()).removeView(remoteSurfaceView);
            }
            fullscreenRenderer.addView(remoteSurfaceView);
        }
    }

    @Override
    public void didUserLeave(String userId) {

    }

    @Override
    public void didError(String error) {

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 接听
        CallSession session = gEngineKit.getCurrentSession();
        if (id == R.id.acceptImageView) {
            if (session != null && session.getState() == CallState.Incoming) {
                session.joinHome(session.getRoomId());
            } else if (session != null) {
                if (callSingleActivity != null) {
                    session.sendRefuse();
                    callSingleActivity.finish();
                }
            }
        }
        // 挂断电话
        if (id == R.id.incomingHangupImageView || id == R.id.outgoingHangupImageView || id == R.id.connectedHangupImageView) {
            if (session != null) {
                Log.d(TAG, "endCall");
                SkyEngineKit.Instance().endCall();
            }
            if (callSingleActivity != null) callSingleActivity.finish();
        }

        // 切换摄像头
        if (id == R.id.switchCameraImageView) {
            session.switchCamera();
        }
        if (id == R.id.pip_video_view) {
            boolean isFullScreenRemote = fullscreenRenderer.getChildAt(0) == remoteSurfaceView;
            fullscreenRenderer.removeAllViews();
            pipRenderer.removeAllViews();
            if (isFullScreenRemote) {
                remoteSurfaceView.setZOrderMediaOverlay(true);
                pipRenderer.addView(remoteSurfaceView);
                localSurfaceView.setZOrderMediaOverlay(false);
                fullscreenRenderer.addView(localSurfaceView);
            } else {
                localSurfaceView.setZOrderMediaOverlay(true);
                pipRenderer.addView(localSurfaceView);
                remoteSurfaceView.setZOrderMediaOverlay(false);
                fullscreenRenderer.addView(remoteSurfaceView);
            }
        }

        // 切换到语音拨打
        if (id == R.id.outgoingAudioOnlyImageView || id == R.id.incomingAudioOnlyImageView || id == R.id.connectedAudioOnlyImageView) {
            if (session != null) {
                if (callSingleActivity != null) callSingleActivity.isAudioOnly = true;
                session.switchToAudio();
            }
        }

        // 小窗
        if (id == R.id.minimizeImageView) {
            if (callSingleActivity != null) callSingleActivity.showFloatingView();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fullscreenRenderer.removeAllViews();
        pipRenderer.removeAllViews();
    }

    @Override
    public void onLocationChange(Location location) {
        /*
        LogManager.printLog("onLocationChange :::> " + location.getLatitude() + " " +
                location.getLongitude() + " " + location.getSpeed() + " " + location.getAccuracy());

        if(m_bTrackingMode && m_bSimulation_mode == true)
        {
            location.setLatitude( 35.837927205794486 ); //mMapView.getCenterPoint().getLatitude();
            location.setLongitude(  128.61044835627567 ); //mMapView.getCenterPoint().getLongitude();
        }
        else {
        }
        mMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        mMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        Log.i( " Point-","center" );
        */
    }

}