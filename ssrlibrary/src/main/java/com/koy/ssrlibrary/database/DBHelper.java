package com.koy.ssrlibrary.database;
/*
 * Shadowsocks - A shadowsocks client for Android
 * Copyright (C) 2013 <max.c.lv@gmail.com>
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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;


public class DBHelper  {

    private static final String TAG = DBHelper.class.getSimpleName();

    public static final String PROFILE = "profile.db";
    private static final int VERSION = 24;

    private List<ApplicationInfo> apps;

    /**
     * is all digits
     */
    private boolean isAllDigits(String x) {
        if (!TextUtils.isEmpty(x)) {
            for (char ch : x.toCharArray()) {
                boolean digit = Character.isDigit(ch);
                if (!digit) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * update proxied apps
     */
    private synchronized String updateProxiedApps(Context context, String old) {
        if (apps == null) {
            apps = context.getPackageManager().getInstalledApplications(0);
        }

        List<Integer> uidSet = new ArrayList<>();
        String[] split = old.split("|");
        for (String item : split) {
            if (isAllDigits(item)) {
                // add to uid list
                uidSet.add(Integer.parseInt(item));
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < apps.size(); i++) {
            ApplicationInfo ai = apps.get(i);
            if (uidSet.contains(ai.uid)) {
                if (i > 0) {
                    // adding separator
                    sb.append("\n");
                }
                sb.append(ai.packageName);
            }
        }
        return sb.toString();
    }


    private Context context;

    public DBHelper(Context context) {

    }

}
