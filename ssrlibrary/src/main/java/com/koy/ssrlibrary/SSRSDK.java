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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.LocaleList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;

import com.evernote.android.job.JobManager;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.koy.ssrlibrary.database.Profile;
import com.koy.ssrlibrary.job.DonaldTrump;
import com.koy.ssrlibrary.utils.Constants;
import com.koy.ssrlibrary.utils.IOUtils;
import com.koy.ssrlibrary.utils.ToastUtils;
import com.koy.ssrlibrary.utils.VayLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.chainfire.libsuperuser.Shell;

public class SSRSDK {

    private static final String TAG = SSRSDK.class.getSimpleName();

    public static void init(Context context) {
        ssrsdk = new SSRSDK(context.getApplicationContext());
        ssrsdk.attachService();
    }

    public static SSRSDK ssrsdk;


    private String[] EXECUTABLES = {
            Constants.Executable.PDNSD,
            Constants.Executable.REDSOCKS,
            Constants.Executable.SS_TUNNEL,
            Constants.Executable.SS_LOCAL,
            Constants.Executable.TUN2SOCKS,
            Constants.Executable.KCPTUN};

    /**
     * The ones in Locale doesn't have script included
     */
    private static final Locale SIMPLIFIED_CHINESE;
    private static final Locale TRADITIONAL_CHINESE;

    static {
        if (Build.VERSION.SDK_INT >= 21) {
            SIMPLIFIED_CHINESE = Locale.forLanguageTag("zh-Hans-CN");
            TRADITIONAL_CHINESE = Locale.forLanguageTag("zh-Hant-TW");
        } else {
            SIMPLIFIED_CHINESE = Locale.SIMPLIFIED_CHINESE;
            TRADITIONAL_CHINESE = Locale.TRADITIONAL_CHINESE;
        }
    }


    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;

    private Context context;

    public SSRSDK(Context context) {
        this.context = context;
        ToastUtils.init(this.context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        checkChineseLocale(getResources().getConfiguration());
        JobManager.create(this.context).addJobCreator(new DonaldTrump());
        mServiceBoundContext = new ServiceBoundContext(this.context) {
            @Override
            protected void onServiceConnected() {

            }

            @Override
            protected void onServiceDisconnected() {

            }

            @Override
            public void binderDied() {
                ssrsdk.detachService();
                ssrsdk.crashRecovery();
                ssrsdk.attachService();
            }
        };

    }

    int state;

    private void attachService() {
        mServiceBoundContext.attachService(new IShadowsocksServiceCallback.Stub() {
            Handler handler = new Handler();
            @Override
            public void stateChanged(int state, String profileName, String msg) throws RemoteException {
                ssrsdk.state = state;
                for (SSRCallback callback : ssrCallbacks) {
                    switch (state) {
                        case Constants.State.CONNECTING:
                            handler.post(() -> callback.connecting());

                            break;
                        case Constants.State.CONNECTED:
                            handler.post(() -> callback.connected());
                            break;
                        case Constants.State.STOPPED:
                            handler.post(() ->  callback.stopped());
                            break;
                        case Constants.State.STOPPING:
                            handler.post(() -> callback.stopping());
                            break;
                        default:
                            break;
                    }
                }


            }

            @Override
            public void trafficUpdated(long txRate, long rxRate, long txTotal, long rxTotal) throws RemoteException {

            }
        });
    }


    public Resources getResources() {
        return context.getResources();
    }

    public ApplicationInfo getApplicationInfo() {
        return context.getApplicationInfo();
    }

    private AssetManager getAssets() {
        return context.getAssets();
    }


    public boolean isNatEnabled() {
        return false;
    }


    @SuppressLint("NewApi")
    private Locale checkChineseLocale(Locale locale) {
        if ("zh".equals(locale.getLanguage())) {
            String country = locale.getCountry();
            if ("CN".equals(country) || "TW".equals(country)) {
                return null;
            } else {
                String script = locale.getScript();
                if ("Hans".equals(script)) {
                    return SIMPLIFIED_CHINESE;
                } else if ("Hant".equals(script)) {
                    return TRADITIONAL_CHINESE;
                } else {
                    VayLog.w(TAG, String.format("Unknown zh locale script: %s. Falling back to trying countries...", script));
                    if ("SG".equals(country)) {
                        return SIMPLIFIED_CHINESE;
                    } else if ("HK".equals(country) || "MO".equals(country)) {
                        return TRADITIONAL_CHINESE;
                    } else {
                        VayLog.w(TAG, String.format("Unknown zh locale: %s. Falling back to zh-Hans-CN...", locale.toLanguageTag()));
                        return SIMPLIFIED_CHINESE;
                    }
                }
            }
        } else {
            return null;
        }
    }

    /**
     * check chinese locale
     */
    private void checkChineseLocale(Configuration config) {
        if (Build.VERSION.SDK_INT >= 24) {
            LocaleList localeList = config.getLocales();
            Locale[] newList = new Locale[localeList.size()];
            boolean changed = false;
            for (int i = 0; i < localeList.size(); i++) {
                Locale locale = localeList.get(i);
                Locale newLocale = checkChineseLocale(locale);
                if (newLocale == null) {
                    newList[i] = locale;
                } else {
                    newList[i] = newLocale;
                    changed = true;
                }
            }
            if (changed) {
                Configuration newConfig = new Configuration(config);
                newConfig.setLocales(new LocaleList(newList));
                Resources res = getResources();
                res.updateConfiguration(newConfig, res.getDisplayMetrics());
            }
        } else {
            Locale newLocale = checkChineseLocale(config.locale);
            if (newLocale != null) {
                Configuration newConfig = new Configuration(config);
                newConfig.locale = newLocale;
                Resources res = getResources();
                res.updateConfiguration(newConfig, res.getDisplayMetrics());
            }
        }
    }


    /**
     * copy assets
     *
     * @param path assets path
     */
    private void copyAssets(String path) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(path);
        } catch (Exception e) {
            VayLog.e(TAG, e.getMessage());

        }

        if (files != null) {
            for (String file : files) {
                InputStream in = null;
                FileOutputStream fos = null;
                try {
                    if (!TextUtils.isEmpty(path)) {
                        in = assetManager.open(path + File.separator + file);
                    } else {
                        in = assetManager.open(file);
                    }
                    fos = new FileOutputStream(getApplicationInfo().dataDir + '/' + file);
                    IOUtils.copy(in, fos);
                } catch (IOException e) {
                    VayLog.e(TAG, "copyAssets", e);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (Exception e) {
                        VayLog.e(TAG, "copyAssets", e);
                    }

                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (Exception e) {
                        VayLog.e(TAG, "copyAssets", e);
                    }
                }
            }
        }
    }


    /**
     * arash recovery
     */
    public void crashRecovery() {
        ArrayList<String> cmd = new ArrayList<>();

        String[] paramsArray = {"ss-local", "ss-tunnel", "pdnsd", "redsocks", "tun2socks", "proxychains"};
        for (String task : paramsArray) {
            cmd.add(String.format(Locale.ENGLISH, "killall %s", task));
            cmd.add(String.format(Locale.ENGLISH, "rm -f %1$s/%2$s-nat.conf %1$s/%2$s-vpn.conf", getApplicationInfo().dataDir, task));
        }

        // convert to cmd array
        String[] cmds = convertListToStringArray(cmd);
        if (ssrsdk.isNatEnabled()) {
            cmd.add("iptables -t nat -F OUTPUT");
            cmd.add("echo done");
            List<String> result = Shell.SU.run(cmds);
            if (result != null && !result.isEmpty()) {
                // fallback to SH
                return;
            }
        }

        Shell.SH.run(cmds);
    }

    /**
     * convert ssrCallbacks to string array
     *
     * @param list ssrCallbacks
     * @return convert failed return {}
     */
    private String[] convertListToStringArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return new String[]{};
        }

        // start convert
        String[] result = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * copy assets
     */
    public void copyAssets() {
        // ensure executables are killed before writing to them
        crashRecovery();
        copyAssets(System.getABI());
        copyAssets("acl");

        // exec cmds
        String[] cmds = new String[EXECUTABLES.length];
        for (int i = 0; i < cmds.length; i++) {
            cmds[i] = "chmod 755 " + getApplicationInfo().dataDir + File.separator + EXECUTABLES[i];
        }
        Shell.SH.run(cmds);

        // save current version code
        editor.putInt(Constants.Key.currentVersionCode, BuildConfig.VERSION_CODE).apply();
    }

    /**
     * update assets
     */
    public void updateAssets() {
        if (preferences.getInt(Constants.Key.currentVersionCode, -1) != BuildConfig.VERSION_CODE) {
            copyAssets();
        }
    }


    public void track(Throwable e) {
        // ToastUtils.showShort(e.getMessage());
    }

    public void track(String tag, String start) {
    }

    public void refreshContainerHolder() {

    }

    public Profile getProfile() {
        return profile;
    }

    Profile profile;
    final int REQUEST_CONNECT = 5;

    public void start(Activity activity, Profile profile) {
        this.profile = profile;
        Intent intent = VpnService.prepare(activity);
        if (intent != null) {
            activity.startActivityForResult(intent, REQUEST_CONNECT);
        } else {
            onActivityResult(REQUEST_CONNECT, Activity.RESULT_OK, null);
        }

    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CONNECT) return false;
        if (resultCode == Activity.RESULT_OK) {
            serviceLoad();
            return true;
        } else {
            ToastUtils.showShort("取消授权");
        }
        return false;
    }

    ServiceBoundContext mServiceBoundContext;

    private boolean serviceLoad() {
        try {
            mServiceBoundContext.bgService.use(profile.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void detachService() {
        mServiceBoundContext.detachService();
    }

    public void stop() {
        profile = null;
        if (mServiceBoundContext.bgService != null) {
            try {
                mServiceBoundContext.bgService.use(null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return state == Constants.State.CONNECTED;

    }


    List<SSRCallback> ssrCallbacks = new ArrayList<>();

    public void registerCallback(SSRCallback callback) {
        if (!ssrCallbacks.contains(callback))
            ssrCallbacks.add(callback);
    }

    public void unregisterCallback(SSRCallback callback) {
        ssrCallbacks.remove(callback);
    }

    public interface SSRCallback {
        void connecting();

        void connected();

        void stopping();

        void stopped();
    }

}
