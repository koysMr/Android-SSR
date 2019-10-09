package com.koy.test;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.koy.ssrlibrary.SSRSDK;
import com.koy.ssrlibrary.database.Profile;
import com.koy.ssrlibrary.utils.ToastUtils;

import static com.koy.ssrlibrary.SSRSDK.ssrsdk;

public class Main2Activity extends AppCompatActivity implements SSRSDK.SSRCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareStartService();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             ssrsdk.stop();
            }
        });

        button.setText( ssrsdk.isRunning()?"Stop":"Start");
        ssrsdk.registerCallback(this);
    }

    private void prepareStartService() {
        Profile profile = new Profile();
        profile.host = "132.145.81.200";
        profile.remotePort = 1186;
        profile.password = "MeDGPo";
        profile.obfs = "plain";
        profile.protocol = "origin";
        profile.method = "rc4-md5";
        ssrsdk.start(this,profile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ssrsdk.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void connecting() {

    }

    @Override
    public void connected() {
        ToastUtils.showShort("连接成功");
    }

    @Override
    public void stopping() {

    }

    @Override
    public void stopped() {
        ToastUtils.showShort("断开连接");
    }

    @Override
    public void finish() {
        super.finish();
        ssrsdk.unregisterCallback(this);
    }
}
