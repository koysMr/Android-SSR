package com.koy.ssrlibrary;
/*
 * Shadowsocks - A shadowsocks client for Android
 * Copyright (C) 2014 <max.c.lv@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *                            ___====-_  _-====___
 *                      _--^^^#####//      \\#####^^^--_
 *                   _-^##########// (    ) \\##########^-_
 *                  -############//  |\^^/|  \\############-
 *                _/############//   (@::@)   \\############\_
 *               /#############((     \\//     ))#############\
 *              -###############\\    (oo)    //###############-
 *             -#################\\  / VV \  //#################-
 *            -###################\\/      \//###################-
 *           _#/|##########/\######(   /\   )######/\##########|\#_
 *           |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 *           `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 *              `   `  `      `   / | |  | | \   '      '  '   '
 *                               (  | |  | |  )
 *                              __\ | |  | | /__
 *                             (vvv(VVV)(VVV)vvv)
 *
 *                              HERE BE DRAGONS
 *
 */

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.RemoteException;

import androidx.core.app.NotificationCompat;

import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.koy.ssrlibrary.utils.TrafficMonitor;

import java.util.Locale;

/**
 * @author Mygod
 */
public class ShadowsocksNotification {

    private Service service;
    private String profileName;
    private boolean visible;
    private PowerManager pm;

    private KeyguardManager keyGuard;
    private NotificationManager nm;

    private IShadowsocksServiceCallback.Stub callback = new IShadowsocksServiceCallback.Stub() {

        @Override
        public void stateChanged(int state, String profileName, String msg) throws RemoteException {
            // Ignore
        }

        @Override
        public void trafficUpdated(long txRate, long rxRate, long txTotal, long rxTotal) throws RemoteException {
            String txr = TrafficMonitor.formatTraffic(txRate);
            String rxr = TrafficMonitor.formatTraffic(rxRate);
            builder.setContentText(String.format(Locale.ENGLISH, service.getString(R.string.traffic_summary), txr, rxr));

            style.bigText(String.format(Locale.ENGLISH,
                    service.getString(R.string.stat_summary),
                    txr,
                    rxr,
                    TrafficMonitor.formatTraffic(txTotal),
                    TrafficMonitor.formatTraffic(rxTotal)));
            show();
        }
    };

    private boolean callbackRegistered;

    private NotificationCompat.Builder builder;

    private NotificationCompat.BigTextStyle style;
    private boolean isVisible = true;

    /**
     * loca receiver
     */
    private BroadcastReceiver lockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            update(intent.getAction());
        }
    };


    public ShadowsocksNotification(Service service, String profileName) {
        this(service, profileName, false);
    }

    public ShadowsocksNotification(Service service, String profileName, boolean visible) {

    }

    private void update(String action) {
        update(action, false);
    }

    private void update(String action, boolean forceShow) {

    }

    public void destroy() {

    }


    public void setVisible(boolean visible, boolean forceShow) {

    }

    public void show() {

    }








}
