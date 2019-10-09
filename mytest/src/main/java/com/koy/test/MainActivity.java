package com.koy.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;

import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.koy.ssrlibrary.ServiceBoundContext;
import com.koy.ssrlibrary.database.Profile;
import com.koy.ssrlibrary.utils.ToastUtils;

import static com.koy.ssrlibrary.SSRSDK.ssrsdk;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareStartService();
            }
        });
        attachService();


    }


    private static final int REQUEST_CONNECT = 5;

    private void prepareStartService() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CONNECT);
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    onActivityResult(REQUEST_CONNECT, RESULT_OK, null);
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            serviceLoad();
        } else {
            ToastUtils.showShort("取消授权");

        }
    }

    private void serviceLoad() {
        try {
            Profile profile = new Profile();
            profile.host = "132.145.81.200";
            profile.remotePort = 1186;
            profile.password = "MeDGPo";
            profile.obfs = "plain";
            profile.protocol = "origin";
            profile.method = "rc4-md5";
            mServiceBoundContext.bgService.use(profile.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    ServiceBoundContext mServiceBoundContext;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        mServiceBoundContext = new ServiceBoundContext(newBase) {
            @Override
            protected void onServiceConnected() {

            }

            @Override
            protected void onServiceDisconnected() {

            }

            @Override
            public void binderDied() {
                detachService();
                ssrsdk.crashRecovery();
                attachService();
            }
        };
    }

    public void detachService() {
        mServiceBoundContext.detachService();
    }
    @Override
    protected void onResume() {
        super.onResume();
        ssrsdk.refreshContainerHolder();
    }


    @Override
    public void finish() {
        super.finish();
        mServiceBoundContext.unregisterCallback();
        mServiceBoundContext.detachService();
    }

    public void attachService() {
        mServiceBoundContext.attachService(callback);
        mServiceBoundContext.registerCallback();
    }


    private IShadowsocksServiceCallback.Stub callback = new IShadowsocksServiceCallback.Stub() {
        Handler handler = new Handler();

        @Override
        public void stateChanged(int state, String profileName, String msg)   {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort(state + msg);
                }
            });

        }

        @Override
        public void trafficUpdated(long txRate, long rxRate, long txTotal, long rxTotal) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort(txRate + "," + rxRate + "," + txTotal + "," + rxTotal);
                }
            });

        }
    };
}
