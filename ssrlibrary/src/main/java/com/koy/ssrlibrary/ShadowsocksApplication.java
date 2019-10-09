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
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.evernote.android.job.JobManager;
import com.koy.ssrlibrary.database.DBHelper;
import com.koy.ssrlibrary.database.Profile;
import com.koy.ssrlibrary.job.DonaldTrump;
import com.koy.ssrlibrary.utils.Constants;
import com.koy.ssrlibrary.utils.IOUtils;
import com.koy.ssrlibrary.utils.TcpFastOpen;
import com.koy.ssrlibrary.utils.ToastUtils;
import com.koy.ssrlibrary.utils.VayLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import eu.chainfire.libsuperuser.Shell;

public class ShadowsocksApplication extends Application {
    public static ShadowsocksApplication app;

    private static final String TAG = ShadowsocksApplication.class.getSimpleName();
    public static final String SIG_FUNC = "getSignature";

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


    public SharedPreferences settings;
    public SharedPreferences.Editor editor;


    public Resources resources;

    public boolean isNatEnabled() {
        return false;
    }

    public boolean isVpnEnabled() {
        return !isNatEnabled();
    }

    public ScheduledExecutorService mThreadPool;

    public void init(Context context) {
        context = context.getApplicationContext();
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
    }

    /**
     * /// xhao: init variable
     */
    private void initVariable(Context context) {
        init(context);

        resources = context.getResources();

        mThreadPool = new ScheduledThreadPoolExecutor(10, new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("shadowsocks-thread");
                return thread;
            }
        });
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkChineseLocale(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        // init toast utils
        ToastUtils.init(getApplicationContext());
        initVariable(this);


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        checkChineseLocale(getResources().getConfiguration());

        JobManager.create(this).addJobCreator(new DonaldTrump());

        if (settings.getBoolean(Constants.Key.tfo, false) && TcpFastOpen.supported()) {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    TcpFastOpen.enabled(settings.getBoolean(Constants.Key.tfo, false));
                }
            });
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
        if (app.isNatEnabled()) {
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
     * convert list to string array
     *
     * @param list list
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
        if (settings.getInt(Constants.Key.currentVersionCode, -1) != BuildConfig.VERSION_CODE) {
            copyAssets();
        }
    }

    public void track(Throwable e) {
        ToastUtils.showShort(e.getMessage());
    }

    public void track(String tag, String start) {
    }

    public void refreshContainerHolder() {

    }
}
