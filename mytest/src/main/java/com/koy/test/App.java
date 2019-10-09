package com.koy.test;

import android.app.Application;

import com.koy.ssrlibrary.ShadowsocksApplication;
import com.koy.ssrlibrary.utils.ToastUtils;

public class App extends ShadowsocksApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }
}
