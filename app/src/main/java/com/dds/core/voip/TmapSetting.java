package com.dds.core.voip;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import com.dds.webrtc.R;

public class TmapSetting extends Activity {
    VideoView tmapview;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tmapsetting);

        tmapview = findViewById(R.id.tmapv);
        MediaController mediaController = new MediaController(this);
        mediaController.setMediaPlayer(tmapview);
        tmapview.setMediaController(mediaController);
        tmapview.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+ R.raw.tmap));


    }
}
