package com.dds;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.dds.core.MainActivity;
import com.dds.core.base.BaseActivity;
import com.dds.core.consts.Urls;
import com.dds.core.socket.IUserState;
import com.dds.core.socket.SocketManager;
import com.dds.webrtc.R;

public class LauncherActivity extends BaseActivity implements IUserState {
//    private Toolbar toolbar;
    private EditText etUser;
    private Button button8;
    private static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        initView();

        Log.d(TAG,"Entered LauncherActivity");
        Log.d(TAG,"UserState : " + SocketManager.getInstance().getUserState());

        if (SocketManager.getInstance().getUserState() == 1) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        userLogin();
        String username = "menti";
//        if (TextUtils.isEmpty(username)) {
//            Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_LONG).show();
//            return;
//        }

        // 设置用户名
        App.getInstance().setUsername(username);
        // 添加登录回调
        SocketManager.getInstance().addUserStateCallback(this);
        // 连接socket:登录
        SocketManager.getInstance().connect(Urls.WS, username, 0);
    }

    private void initView() {
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar = findViewById(R.id.toolbar);
        button8 = findViewById(R.id.button8);

//        etUser.setText("menti");
    }

    public void java(View view) {
//        String username = etUser.getText().toString().trim();
        String username = "menti";
//        if (TextUtils.isEmpty(username)) {
//            Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_LONG).show();
//            return;
//        }

        // 设置用户名
        App.getInstance().setUsername(username);
        // 添加登录回调
        SocketManager.getInstance().addUserStateCallback(this);
        // 连接socket:登录
        SocketManager.getInstance().connect(Urls.WS, username, 0);


    }

    @Override
    public void userLogin() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void userLogout() {

    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            super.onBackPressed();
        }

    }
}
