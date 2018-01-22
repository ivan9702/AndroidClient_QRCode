package com.startek.fm220;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Ivan on 2017/7/26.
 */

public class FirstScreenActivity extends Activity {

    private final String TAG = "FirstScreen";
    private final long SPLASH_LENGTH = 1000;
    private boolean mSplashTimeout = false;
    private static boolean Upgrade =false;
    private String mToken = null;
    private boolean mPermissionChecking = false;
    private String[] permissionArray = new String[] {
            //   Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            //     Manifest.permission.CHANGE_NETWORK_STATE
    };

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                default:
                    mSplashTimeout = true;
                    goNextActivity();
                    break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstscreen);

        mHandler.sendEmptyMessageDelayed(-1, SPLASH_LENGTH);
        if(!PermissionUtils.checkSelfPermissionArrayinFirstScreen(FirstScreenActivity.this, permissionArray)) {
            Log.d(TAG, "Grant Permission...");
            mPermissionChecking = true;
            PermissionUtils.checkPermissionArray(FirstScreenActivity.this, permissionArray, 2);

        }
    }

    ////////////// permission
    @TargetApi(Build.VERSION_CODES.M)
    private void checkWriteSettingsPermission() {
        if (!Settings.System.canWrite(this)) {
            mPermissionChecking = true;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_denied)
                    .setMessage(R.string.enable_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!Settings.System.canWrite(FirstScreenActivity.this)) {
                                    FirstScreenActivity.this.finish();
                                } else {
                                    goNextActivity();
                                }
                            }
                        }
                    })
                    .show();
        } else {
            mPermissionChecking = false;
            goNextActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Verify that each required permission has been granted, otherwise Close APP
        ArrayList<String> deniedPermissions = new ArrayList<>();

        for (int i=0; i<permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission Denied " + grantResults[i] +",  for  " + permissions[i]);
                deniedPermissions.add(permissions[i]);
            }
        }

        if (deniedPermissions.size() == 0) {
            mPermissionChecking = false;
            goNextActivity();
        } else if (deniedPermissions.size() == 1
                && deniedPermissions.get(0).equals(Manifest.permission.CHANGE_NETWORK_STATE)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Try manually enable write settings");
            checkWriteSettingsPermission();
        } else {
            Log.d(TAG, "Permission Denied. finish self");
            finish();
        }
    }

    public void goNextActivity() {

        if (!mSplashTimeout
 //               || mTaskRunning
                || mPermissionChecking
                || isFinishing()) {
            return;
        }

        if(Upgrade) {
            new android.app.AlertDialog.Builder(FirstScreenActivity.this)
        //                    .setTitle(R.string.soft_update_title)
        //                    .setMessage(R.string.soft_update_info)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DL_url));
//                            startActivity(browserIntent);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goNextActivity1();
                        }
                    })
                    .show();

        }else {
            goNextActivity1();
        }
    }
    public void goNextActivity1() {
        if (mToken == null) {
            startActivity(new Intent(FirstScreenActivity.this, AlbumSelectorActivity.class));
//        } else if (Devices.mDevices.size() == 0 && mPref.getShowNewFrame()) {
//            startActivity(new Intent(getApplicationContext(), NewFrameActivity.class));
//        } else {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        finish();
    }

}
