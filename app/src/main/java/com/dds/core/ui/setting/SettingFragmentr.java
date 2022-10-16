package com.dds.core.ui.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dds.webrtc.R;

public class SettingFragmentr extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settingr, container, false);
//        final TextView textView = root.findViewById(R.id.text_notifications);
//        button = root.findViewById(R.id.setting);
        return root;


    }
}
