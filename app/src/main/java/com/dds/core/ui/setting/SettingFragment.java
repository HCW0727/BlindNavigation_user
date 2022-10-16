package com.dds.core.ui.setting;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dds.CallListActivity;
import com.dds.FFmpegRecordActivity;
import com.dds.core.voip.CallMultiActivity;
import com.dds.core.voip.CallSingleActivity;
import com.dds.core.voip.NavigationTMapActivity;
import com.dds.webrtc.R;

import java.util.Locale;


public class SettingFragment extends Fragment {

    String      utteranceId=this.hashCode() + "";
    int         m_iCurTopMenuSel;
    private TextToSpeech tts;
    private final boolean bTTS_Speech = true;
    private int lang;

    private SettingViewModel notificationsViewModel;
    private Button button;
    private String userId;
    private String avatar;
    private String nickName;
    private View.OnKeyListener pressKeyListener;
    private BroadcastReceiver mMessageReceiver;
    private static final String TAG = "SettingFragment";



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //이해 못했음
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(SettingViewModel.class);

        //setcontentview와 같은 역할로 생각
        View root = inflater.inflate(R.layout.fragment_setting, container, false);

        Log.d(TAG,"Entered SettingFragment");

        // Our handler for received Intents. This will be called whenever an Intent
        // with an action named "custom-event-name" is broadcasted.
        //뒤로가기 눌렀을 때 작동. 뒤로 가기 누르면 onPause가 작동하는 것인가?
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int keyCode = intent.getIntExtra("KEY_CODE", 0);
                // Do something with the event
                Log.i("Key Receive------", intent.getAction());
                Log.d(TAG,"Received Broadcast");
                PrintCurTopMenu( keyCode );
                Log.i("Key Receive------", String.valueOf(keyCode));
                //doSomethingCauseVolumeKeyPressed();
            }
        };


// Register to receive messages.
// We are registering an observer (mMessageReceiver) to receive Intents
// with actions named "custom-event-name".
//        LocalBroadcastManager.getInstance( getContext() ).registerReceiver(mMessageReceiver,
//                new IntentFilter("activity-says-hi"));

        Log.i("Activity 11------", "main");
        Button button1 =  root.findViewById(R.id.Btn1);
        Button button2 =  root.findViewById(R.id.Btn2);
        Button button3 =  root.findViewById(R.id.Btn3);
        Button button4 =  root.findViewById(R.id.Btn4);
        Button button5 =  root.findViewById(R.id.Btn5);


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
                Menu3();
            }
        });

        button4.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu4();
            }
        });

        button5.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu5();
            }
        });

        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(1.4f);
                }
            }
        });

        return root;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
     //   LocalBroadcastManager.getInstance( getContext() ).unregisterReceiver(mMessageReceiver);

    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG,"onPause");
        LocalBroadcastManager.getInstance( getContext() ).unregisterReceiver(mMessageReceiver);
    }
    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance( getContext() ).registerReceiver(mMessageReceiver,
                new IntentFilter("activity-says-hi"));
        Log.d(TAG,"onResume");
    }

    //-------------------------------------------------
    public int   RunCurTopMenu( int iCurTopMenuSel ) {
        String   st = "";
        if( iCurTopMenuSel == 25 ) {
            m_iCurTopMenuSel = 0;
            Menu1();
        }
        if( iCurTopMenuSel == 24 ) {
            m_iCurTopMenuSel = 0;
            Menu2();
        }
        if( iCurTopMenuSel == 96 ) {
            m_iCurTopMenuSel = 0;
            Menu3();
        }
        if( iCurTopMenuSel == 97 ) {
            m_iCurTopMenuSel = 0;
            Menu4();
        }
        Log.i("-------------------", st);
        return 1;
    }
    //----------------------------------------------------
    public int   PrintCurTopMenu( int iCurTopMenuSel ) {
        String   st = "";
        if( iCurTopMenuSel == 25 ) {
            if( m_iCurTopMenuSel == 25 ) {
                RunCurTopMenu(iCurTopMenuSel);
                return 1;
            }
            else {
                st = "AI 장애물 인식기능";
                m_iCurTopMenuSel = iCurTopMenuSel;
            }
        }
        if( iCurTopMenuSel == 24 ) {
            if( m_iCurTopMenuSel == 24 ) {
                RunCurTopMenu(iCurTopMenuSel);
                return 1;
            }
            else {
                st = "네비게이션 기능";
                m_iCurTopMenuSel = iCurTopMenuSel;
            }
        }
        if( iCurTopMenuSel == 96 ) {
            if( m_iCurTopMenuSel == 96 ) {
                RunCurTopMenu(iCurTopMenuSel);
                return 1;
            }
            else {
                st = "원격접속 기능";
                m_iCurTopMenuSel = iCurTopMenuSel;
            }
        }
        if( iCurTopMenuSel == 97 ) {
            if( m_iCurTopMenuSel == 97 ) {
                RunCurTopMenu(iCurTopMenuSel);
                return 1;
            }
            else {
                st = "블랙박스 기능";
                m_iCurTopMenuSel = iCurTopMenuSel;
            }
        }
        if(bTTS_Speech ==true){
            Log.d(TAG,"bTTS_Speech == True" + st);
            tts.speak( st,TextToSpeech.QUEUE_ADD,null, utteranceId);
        }
        Log.i("-------------------", st);
        return 1;
    }
    //----------------------------------------

    //--------------------------------- menu
    //----------------------------------------
    public void Menu1() {
        String st = "AI 장애물 인식기능을 실행합니다";
        if(bTTS_Speech ==true) tts.speak( st,TextToSpeech.QUEUE_ADD,null, utteranceId);
        //Intent intent = new Intent(MainActivity.this, ActivityTTS.class);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        //    intent.setComponent(new ComponentName("com.example.stt_demo2","com.example.stt_demo2.MainActivity"));
        intent.setComponent(new ComponentName("org.tensorflow.lite.examples.detection",
                "org.tensorflow.lite.examples.detection.DetectorActivity"));
        startActivity(intent);
//        Intent intent = new Intent(getContext(), AiDetectActivity.class);
//        startActivity(intent);
    }
    //----------------------------------------
    public void Menu2()
    {
        String st = "네비게이션 기능을 실행합니다";
        if(bTTS_Speech ==true) tts.speak( st,TextToSpeech.QUEUE_ADD,null, utteranceId);
        //    speech( );
        Intent intent = new Intent(getContext(), NavigationTMapActivity.class);
        startActivity(intent);
    }
    //----------------------------------------
    public void Menu3() {
        String st = "원격접속 기능을 실행합니다";
        if(bTTS_Speech ==true) tts.speak( st,TextToSpeech.QUEUE_ADD,null, utteranceId);
        //intent.setComponent(new ComponentName("com.dds.webrtc.debug","com.dds.LauncherActivity"));
        //startActivity(intent);

        String  ss;
        ss = "원격--nickname :" + nickName + "id :" +     userId + "getnick() : " + nickName;
        Log.i( "huh", ss );
        CallSingleActivity.openActivity(getContext(), "mento", true, getNickName(), false, false);
    }
    //----------------------------------------
    public void Menu4() {
        String st = "블랙박스 기능을 실행합니다";
        if(bTTS_Speech ==true) tts.speak( st,TextToSpeech.QUEUE_ADD,null, utteranceId);
        Intent intent = new Intent(getContext(), FFmpegRecordActivity.class);
        startActivity(intent);
    }
    //----------------------------------------
    public void Menu5() {
        String st = "긴급 전화를 실행합니다";
        if(bTTS_Speech ==true) tts.speak( st,TextToSpeech.QUEUE_ADD,null, utteranceId);

        Intent intent = new Intent(getContext(), CallListActivity.class);
        startActivity(intent);

        /*
        String tel_number = "tel:01076373768";
        PermissionManager mPermissionManager = new PermissionManager();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){

            return;
        }
       Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel_number));
        startActivity(intent);
       */
    }
    private void createRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("바로 룸을 만들고 룸에 들어갑니다.");
        builder.setPositiveButton("확인", (dialog, which) -> {
            String room = "testing";
            // 방을 만들고 들어갑니다.
            CallMultiActivity.openActivity(getActivity(),
                    "room-" + room, false);
            //랜덤 방 코드 생성
//            Toast.makeText(getContext(), room, Toast.LENGTH_LONG).show();

        }).setNegativeButton("취소", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public String getNickName() {
        if (TextUtils.isEmpty(nickName)) {
            return userId;
        }
        return nickName;
    }

}