package com.github.shadowsocks;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.github.shadowsocks.aidl.IShadowsocksService;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        java.lang.System.out.println("-->>onBind Not yet implemented");
        return  new IShadowsocksService.Stub() {
            @Override
            public int getState() throws RemoteException {
                return 0;
            }

            @Override
            public String getProfileName() throws RemoteException {
                return null;
            }

            @Override
            public void registerCallback(IShadowsocksServiceCallback cb) throws RemoteException {

            }

            @Override
            public void unregisterCallback(IShadowsocksServiceCallback cb) throws RemoteException {

            }

            @Override
            public void use(int profileId) throws RemoteException {

            }

            @Override
            public void useSync(int profileId) throws RemoteException {

            }
        };
    }
}
