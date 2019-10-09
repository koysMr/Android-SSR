package com.koy.test;

import android.app.Application;

import com.koy.ssrlibrary.SSRSDK;
import com.koy.ssrlibrary.utils.ToastUtils;

public class App  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SSRSDK.init(this);
    }
}
