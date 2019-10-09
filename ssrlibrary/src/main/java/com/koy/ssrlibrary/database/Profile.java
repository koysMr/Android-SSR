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

import android.util.Base64;

import java.util.Locale;

public class Profile  {

    public int id = 0;

    public String name = "Untitled";

    public String host = "";

    public int localPort = 1080;

    public int remotePort = 8388;

    public String password = "";

    public String protocol = "origin";

    public String protocol_param = "";

    public String obfs = "plain";

    public String obfs_param = "";

    public String method = "aes-256-cfb";

    public String route = "all";

    public boolean proxyApps = false;

    public boolean bypass = false;

    public boolean udpdns = false;

    public String url_group = "";

    public String dns = "8.8.8.8:53";

    public String china_dns = "114.114.114.114:53,223.5.5.5:53";

    public boolean ipv6 = false;

    public String individual = "";

    public long tx = 0;

    public long rx = 0;

    public long elapsed = 0;

  //  public final Date date = new Date();

    public long userOrder = 0;

    @Override
    public String toString() {
        String result = Base64.encodeToString(String.format(Locale.ENGLISH,
                "%s:%d:%s:%s:%s:%s/?obfsparam=%s&protoparam=%s&remarks=%s&group=%s",
                host, remotePort, protocol, method, obfs,
                Base64.encodeToString(String.format(Locale.ENGLISH, "%s", password).getBytes(), Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP),
                Base64.encodeToString(String.format(Locale.ENGLISH, "%s", obfs_param).getBytes(), Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP),
                Base64.encodeToString(String.format(Locale.ENGLISH, "%s", protocol_param).getBytes(), Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP),
                Base64.encodeToString(String.format(Locale.ENGLISH, "%s", name).getBytes(), Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP),
                Base64.encodeToString(String.format(Locale.ENGLISH, "%s", url_group).getBytes(), Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP)
                ).getBytes(),
                Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
        return "ssr://" + result;
    }

    /**
     * is method unsafe
     */
    public boolean isMethodUnsafe() {
        return "table".equalsIgnoreCase(method) || "rc4".equalsIgnoreCase(method);
    }


}
