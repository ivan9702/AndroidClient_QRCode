package com.startek.fm220;

/**
 * Created by ivan.lin on 2017/7/12.
 */


/* MainActivity.java
 * LIST-USB-OTG
 * Created by danny on 2014/04/22
 * Copyright (c) 2014¦~ danny. All rights reserved.
 */


        import java.util.HashMap;
        import java.util.Iterator;

        import android.app.Activity;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.hardware.usb.UsbDevice;
        import android.hardware.usb.UsbManager;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;

public class USB_FP extends Activity {

    UsbManager mUsbManager = null;
    IntentFilter filterAttached_and_Detached = null;

    private TextView theMessage;
    private TextView dbgMsg;
    private Button buttonEnroll;
    private String TAG = "usb_fp";
    //
    private static final String ACTION_USB_PERMISSION = "com.startek.fm210.USB_PERMISSION";
    //
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(device != null){
                        //
                        Log.d(TAG,"DEATTCHED - " + device);
                    }
                }
            }
//
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if(device != null){
                            //
                            Log.d(TAG,"ATTACHED - " + device);
                        }
                    }
                    else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(USB_FP.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                        mUsbManager.requestPermission(device, mPermissionIntent);

                        if(device != null){
                            //
                            Log.d(TAG,"ATTACHED - ONE SHOT " + device);
                            Log.d(TAG,"ATTACHED - " + device);
                        }
                    }

                }
            }
//
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if(device != null){
                            //
                            Log.d(TAG,"PERMISSION -" + device);
                        }
                    }

                }
            }

        }
    };
//


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpscan);
        buttonEnroll = (Button) findViewById(R.id.enrollB);

        theMessage = (TextView) findViewById(R.id.message);
        dbgMsg = (TextView) findViewById(R.id.dbgmsg);

       // if (savedInstanceState == null) {
       //     getFragmentManager().beginTransaction()
       //             .add(R.id.container, new PlaceholderFragment()).commit();
       // }
        //
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //
        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);
        //
        registerReceiver(mUsbReceiver, filterAttached_and_Detached);
        //

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(TAG, deviceList.size()+" USB device(s) found");
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            Log.d(TAG,"" + device);
        }

        //Enroll ori
        buttonEnroll.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(USB_FP.this,AlbumSelectorActivity.class);
                //intent.putExtra("selectIma", targetPath);
                startActivity(intent);


            }

        });
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }



//

}
