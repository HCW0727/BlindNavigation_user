package com.dds;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.webrtc.R;

import java.util.Locale;

public class PlaybackActivity extends AppCompatActivity {
    private       final boolean           bTTS_Speech   = true;
    private       TextToSpeech            ttsObj;
    String        utteranceId=this.hashCode() + "";
    public static final String INTENT_NAME_VIDEO_PATH = "INTENT_NAME_VIDEO_PATH";

    private VideoView  mVvPlayback;
    MediaController    mediaController;

    private int mVideoCurPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        //TextView tvVideoPath = (TextView) findViewById(R.id.tv_video_path);
        mVvPlayback = (VideoView) findViewById(R.id.vv_playback);

        String path = getIntent().getStringExtra(INTENT_NAME_VIDEO_PATH);
        if (path == null) {
            finish();
        }
        //액티비티가 초기화될때 TextToSpeech를 생성
        //생성될때 초기화가 되는지 확인하기 위해 TextToSpeech.OnInitListener를 익명으로 생성하여
        //매개변수로 전달

        ttsObj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    ttsObj.setLanguage(Locale.KOREAN);
                    ttsObj.setPitch(1.f);
                    ttsObj.setSpeechRate(1.4f);
                    ttsObj.getVoice();
                    ttsObj.getVoices();

                }
            }
        });


        Button button1 =  findViewById(R.id.btn1);
        Button button2 =  findViewById(R.id.btn2);
        Button button3 =  findViewById(R.id.btn3);

        button1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu1();
            }
        });

        button2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu2();
            }
        });

        button3.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu4();
            }
        });

        //tvVideoPath.setText(path);
        mVvPlayback.setVideoPath(path);
        mVvPlayback.setKeepScreenOn(true);
        mediaController = new MediaController(this);
        mVvPlayback.setMediaController( mediaController );
        mVvPlayback.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
            }
        });
        mVvPlayback.start();
    }
    //-----------------------------
    public void   Menu1( ) {
        Log.i("-------------------", "resumeRecording()");
        mVvPlayback.stopPlayback();
        mVvPlayback.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blackbox1));
        mVvPlayback.start();
        mVvPlayback.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaController.hide();
                //videoView.pause();
            }
        }, 10);
    }
    //-----------------------------
    public void   Menu2( ) {
        Log.i("-------------------", "stopRecording()");
        mVvPlayback.stopPlayback();
        mVvPlayback.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blackbox2));
        mVvPlayback.start();
        mVvPlayback.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaController.hide();
                //videoView.pause();
            }
        }, 10);
    }
    //-----------------------------
    public void   Menu3( ) {
        Log.i("-------------------", "Play()");
        mVvPlayback.stopPlayback();
        mVvPlayback.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blackbox3));
        mVvPlayback.start();
        mVvPlayback.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaController.hide();
                //videoView.pause();
            }
        }, 10);

    }
    //-----------------------------
    public void   Menu4( ) {
        Log.i("-------------------", "Finish()");
        String  st = "미리보기를 종료합니다";

        if(bTTS_Speech ==true) ttsObj.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
        finish( );
    }
    //-----------------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String   st = "onKeyDown" + String.valueOf( keyCode );
        Log.i("-------------------", st);
        if( mVvPlayback.isPlaying() ) mVvPlayback.pause();;
        if( keyCode == 25 ) Menu1( );
        if( keyCode == 24 ) Menu2( );
        if( keyCode == 96 ) Menu3( );
        if( keyCode == 97 ) Menu4( );
        if( keyCode ==  4 ) Menu4( );
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVvPlayback.stopPlayback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVvPlayback.pause();
        mVideoCurPos = mVvPlayback.getCurrentPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVvPlayback.seekTo(mVideoCurPos);
        mVvPlayback.start();
    }
}
