package com.dds.core.ui.setting;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SettingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("설정 파트입니다.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
