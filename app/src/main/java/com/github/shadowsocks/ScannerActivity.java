package com.github.shadowsocks;
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

import android.Manifest;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.utils.Parser;
import com.github.shadowsocks.utils.ToastUtils;
import com.google.zxing.Result;
import com.koy.ss.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.github.shadowsocks.ShadowsocksApplication.app;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private ZXingScannerView scannerView;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                ToastUtils.showShort(R.string.add_profile_scanner_permission_required);
                finish();
            }
        }
    }

    private void navigateUp() {
        Intent intent = getParentActivityIntent();
        if (shouldUpRecreateTask(intent) || isTaskRoot()) {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(intent).startActivities();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_scanner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateUp();
            }
        });

        scannerView = (ZXingScannerView) findViewById(R.id.scanner);

        if (Build.VERSION.SDK_INT >= 25) {
            ShortcutManager service = getSystemService(ShortcutManager.class);
            if (service != null) {
                service.reportShortcutUsed("scan");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // Register ourselves as a handler for scan results.
            scannerView.setResultHandler(this);
            scannerView.setAutoFocus(true);
            // Start camera on resume
            scannerView.startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop camera on pause
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String uri = rawResult.getText();
        if (!TextUtils.isEmpty(uri)) {
            List<Profile> all = Parser.findAll(uri);
            if (all != null && !all.isEmpty()) {
                for (Profile p : all) {
                    app.profileManager.createProfile(p);
                }
            }

            List<Profile> allSSR = Parser.findAll_ssr(uri);
            if (allSSR != null && !allSSR.isEmpty()) {
                for (Profile p : allSSR) {
                    app.profileManager.createProfile(p);
                }
            }
        }
        navigateUp();
    }
}
