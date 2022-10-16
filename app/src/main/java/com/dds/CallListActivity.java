package com.dds;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dds.webrtc.R;

public class CallListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_list);

        Button button1 =  findViewById(R.id.Btn1);
        Button button2 =  findViewById(R.id.Btn2);
        Button button3 =  findViewById(R.id.Btn3);
        Button button4 =  findViewById(R.id.Btn4);
        Button button5 =  findViewById(R.id.Btn5);


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
    }
    //--------------------------------- menu
    //----------------------------------------
    public void Menu1() {
        String tel_number = "tel:01076373768";
        PermissionManager mPermissionManager = new PermissionManager();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){

            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel_number));
        startActivity(intent);
    }
    //----------------------------------------
    public void Menu2()
    {
        String tel_number = "tel:01076373768";
        PermissionManager mPermissionManager = new PermissionManager();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){

            return;
        }
       Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel_number));
        startActivity(intent);
    }
    //----------------------------------------
    public void Menu3() {
        String tel_number = "tel:01076373768";
        PermissionManager mPermissionManager = new PermissionManager();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){

            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel_number));
        startActivity(intent);
    }
    //----------------------------------------
    public void Menu4() {
        finish( );
        return;
    }
    //----------------------------------------

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String   st = "onKeyDown" + String.valueOf( keyCode );

        Log.i("-------------------", st);
        if( keyCode == 25 ) Menu1( );
        if( keyCode == 24 ) Menu2( );
        if( keyCode == 96 ) Menu3( );
        if( keyCode == 97 ) Menu4( );
        if( keyCode ==  4 ) Menu4( );
        return true;
    }

}