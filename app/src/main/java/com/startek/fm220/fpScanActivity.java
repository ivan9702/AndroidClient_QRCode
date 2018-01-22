package com.startek.fm220;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.startek.fingerprint.library.FP;
import com.orhanobut.logger.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by ivan.lin on 2017/7/12.
 */

/*
public class fpScanActivity extends Activity {
    UsbManager mUsbManager = null;
    IntentFilter filterAttached_and_Detached = null;

    private UsbDevice usbDevice;
    private UsbDeviceConnection conn;
    private UsbInterface usbIf;
    UsbEndpoint epIN;
    UsbEndpoint epOUT;
    UsbEndpoint ep2IN;
    private int connectrtn;
    byte[] srno= new byte[16];
    byte[] pak = new byte[16];
    byte[] fwver = new byte[16];
    byte[] Key2= new byte[16];
    byte[] newKey= new byte[16];

    private Button buttonConnect;
    private TextView theMessage;
    private ImageView pictImg;
    private AutoPlayImageView fpImg;
   // private TextView dbgMsg;
    private Button buttonEnroll;
    private String TAG = "fpScan";
    AlertDialog dialog;
    private static final String ACTION_USB_PERMISSION = "com.startek.fm210.USB_PERMISSION";
    private Integer[] imgThumbIds = {
            R.drawable.press_default, R.drawable.touch_id };
    private ProgressDialog progressDialog;
    protected static final int SWITCH_IMGE = 0;
    private boolean currentImgIndex = true;
    private PendingIntent mPermissionIntent;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(usbDevice != null){
                        //
                        Log.d(TAG,"DEATTCHED - " + usbDevice);
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
                            //checkUSBDevice();
                        }
                    }
                    else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(fpScanActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                        mUsbManager.requestPermission(device, mPermissionIntent);

                        if(device != null){
                            //
                            Log.d(TAG,"ATTACHED - ONE SHOT ");
                            //Log.d(TAG,"ATTACHED - " + device);

                            //checkUSBDevice();
                            dialog.dismiss();

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
                            Log.d(TAG,"PERMISSION GRANTED - ");
                           // checkUSBDevice();
                            connectreader();
                        }
                        else {
                            Log.d(TAG, "NONE USB DEVICE ");
                            goBackActivity();
                        }
                    }
                    else
                    {
                        Log.d(TAG,"no EXTRA_PERMISSION_GRANTED..");
                        goBackActivity();
                    }

                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpscan);
        buttonEnroll = (Button) findViewById(R.id.enrollB);
        buttonConnect = (Button) findViewById(R.id.connectB);
        theMessage = (TextView) findViewById(R.id.message);
       // dbgMsg = (TextView) findViewById(R.id.dbgmsg);

        // if (savedInstanceState == null) {
        //     getFragmentManager().beginTransaction()
        //             .add(R.id.container, new PlaceholderFragment()).commit();
        // }
        //
        pictImg = (ImageView)findViewById(R.id.pictImg);
        fpImg = (AutoPlayImageView)findViewById(R.id.fpImg);
        String imagePath = getIntent().getStringExtra("selectIma");
        Log.d(TAG, "imagePath: "+imagePath);
        Bitmap pictBitmap = BitmapFactory.decodeFile(imagePath);
        pictImg.setImageBitmap(pictBitmap);

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
        if(deviceList.size() == 0) {
            theMessage.setText(R.string.usb_disconnect);

            dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.usb_disconnect)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBackActivity();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkUSBDevice();
                        }


                    })
                    .create();
            dialog.show();
        }
        else{
//            theMessage.setText(R.string.usb_connected);
//            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//            while(deviceIterator.hasNext()){
//            usbDevice = deviceIterator.next();
//            Log.d(TAG,"USB default connect.." );
//                PendingIntent mPermissionIntent;
//                mPermissionIntent = PendingIntent.getBroadcast(fpScanActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
//                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
//
//            }
//            Log.d(TAG,"USB default connect.." );
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

            //PendingIntent mPermissionIntent;
            for (UsbDevice mdevice : mUsbManager.getDeviceList().values()) {

                int pid, vid;

                pid = mdevice.getProductId();
                vid = mdevice.getVendorId();

                if (((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca)) || ((pid == 0x8220) && (vid == 0x0b39)) || ((pid == 0x8210) && (vid == 0x0b39))) {
                    theMessage.setText("fm220 pid found");
                    usbDevice = mdevice;

                    mUsbManager.requestPermission(usbDevice, mPermissionIntent);

                    break;

                }

            }
        }
//
//       //Enroll ori
//        buttonEnroll.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(fpScanActivity.this,AlbumSelectorActivity.class);
//                //intent.putExtra("selectIma", targetPath);
//                startActivity(intent);
//
//
//            }
//
//        });
        //Connect
        buttonConnect.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {

                //Log.v("Fm210", "Marcus: Click");
                try {

                    if (conn.getFileDescriptor() == -1) {
                        connectreader1();
                        theMessage.setText("try connect without file descripter" + conn.getFileDescriptor());
                        connectrtn = FP.ConnectCaptureDriver(conn, usbDevice);
                        Log.d("FM220", "Fails to open DeviceConnection");
                    } else {
                        theMessage.setText("try connect with file descripter" + conn.getFileDescriptor());
                        connectrtn = FP.ConnectCaptureDriver(conn, usbDevice);
                        Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //String newSNStr="B1310543";
                String newSNStr="SH000856";
                byte[] tmp = newSNStr.getBytes();
                byte[] new_sn = new byte[16];
                System.arraycopy(tmp, 0, new_sn, 0,  tmp.length);
                FP.SetSerialNumber(new_sn);

                newKey[0]=65;		newKey[1]=67;		newKey[2]=80;		newKey[3]=76;
                newKey[4]=80;		newKey[5]=65;		newKey[6]=75;		newKey[7]=51;	//new PAK	ACPLPAK3
                FP.SetPreAllocatedKey(newKey);

                FP.GetSerialNumber(srno);
                String strSN = new String(srno);
                FP.GetPreAllocatedKey(pak);
                String strPAK = new String(pak);
                FP.GetFWVer(fwver);
                String strFWVer = new String(fwver);

                theMessage.setText("sn: " + strSN + " pak: " + strPAK +" fw ver: " + strFWVer);

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mUsbReceiver);
    }



    public void checkUSBDevice(){
        int pid, vid;

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(TAG,"check.."+ deviceList.size()+" USB device(s) found");
        if(deviceList.size() == 0)
        {
            dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.usb_disconnect)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBackActivity();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkUSBDevice();
                        }


                    })
                    .create();
            dialog.show();
        }
        else {

            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                usbDevice = deviceIterator.next();
                Log.d(TAG, "LIST: ");
                Log.d(TAG, String.valueOf(usbDevice));

                vid = usbDevice.getVendorId();
                pid = usbDevice.getProductId();

                if (((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca)) || ((pid == 0x8220) && (vid == 0x0b39)) || ((pid == 0x8210) && (vid == 0x0b39))) {
                    theMessage.setText(R.string.usb_connected);
                } else {
                    theMessage.setText(R.string.unknown_device);
                    DialogUtil.showToast(fpScanActivity.this, R.string.unknown_device);
                    goBackActivity();
                }
            }
        }

        //fpImg.setData(imgThumbIds);
        //fpImg.setVisibility(View.VISIBLE);

        connectreader1();
//        progressDialog = new ProgressDialog(fpScanActivity.this);
//        progressDialog.setTitle(R.string.information);
//        progressDialog.setMessage(getResources().getString(R.string.press_fp));
//        progressDialog.setCancelable(false);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.show();


    }



    //
    private void connectreader1() {
        // TODO Auto-generated method stub

        usbIf = usbDevice.getInterface(0);

        Log.d("FM220", "Interface Count: " + Integer.toString(usbDevice.getInterfaceCount()));
        Log.d("FM220", "getEndpoint Count: " + String.valueOf(usbIf.getEndpointCount()));
        //Log.d("USB", String.valueOf(usbIf.getEndpointCount()));
        //    final UsbEndpoint  usbEndpoint = usbInterface.getEndpoint(0);

        epIN = null;
        epOUT = null;
        ep2IN = null;

        theMessage.setText("num of ep" + usbIf.getEndpointCount());

        epOUT = usbIf.getEndpoint(0);
        epIN = usbIf.getEndpoint(1);
        ep2IN = usbIf.getEndpoint(2);

        //	 theMessage.setText("ep num "+ ep2IN.getEndpointNumber()+"packet size "+ ep2IN.getMaxPacketSize()+"dir "+ep2IN.getDirection());
        //	 theMessage.setText("ep num "+ epIN.getEndpointNumber()+"packet size "+ epIN.getMaxPacketSize()+"dir "+epIN.getDirection());
        theMessage.setText("ep num " + epOUT.getEndpointNumber() + "packet size " + epOUT.getMaxPacketSize() + "dir " + epOUT.getDirection());

        //	 theMessage.setText("manager.hasPermission()");
        //if (mUsbManager.hasPermission(usbDevice) == false) {
            //    	 theMessage.setText("manager.hasPermission() false");
          //  return;

        //}

        conn = mUsbManager.openDevice(usbDevice);

        if (conn.getFileDescriptor() == -1) {
            Log.d("FM220", "Fails to open DeviceConnection");
        } else {

            Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
        }

        if (conn.releaseInterface(usbIf)) {
            Log.d("USB", "Released OK");
        } else {
            Log.d("USB", "Released fails");
        }

        if (conn.claimInterface(usbIf, true)) {
            Log.d("USB", "Claim OK");
        } else {
            Log.d("USB", "Claim fails");
        }
        //     theMessage.setText("EEPROM_read");
        //     byte [] buf= new byte [48];
        //     eeprom_read(0,48,buf);
        theMessage.setText("fm220 fileDescriptor: " + conn.getFileDescriptor());
        //usbd_description();
    }
    //
    public void usbd_description() {

        //Log.v("Fm210", "Marcus: Click");
        try {

            if (conn.getFileDescriptor() == -1) {
                connectreader1();
                theMessage.setText("try connect without file descripter" + conn.getFileDescriptor());
                connectrtn = FP.ConnectCaptureDriver(conn, usbDevice);
                Log.d("FM220", "Fails to open DeviceConnection");
            } else {
                theMessage.setText("try connect with file descripter" + conn.getFileDescriptor());
                connectrtn = FP.ConnectCaptureDriver(conn, usbDevice);
                Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //String newSNStr="B1310543";
        String newSNStr="SH000856";
        byte[] tmp = newSNStr.getBytes();
        byte[] new_sn = new byte[16];
        System.arraycopy(tmp, 0, new_sn, 0,  tmp.length);
        FP.SetSerialNumber(new_sn);

        newKey[0]=65;		newKey[1]=67;		newKey[2]=80;		newKey[3]=76;
        newKey[4]=80;		newKey[5]=65;		newKey[6]=75;		newKey[7]=51;	//new PAK	ACPLPAK3
        FP.SetPreAllocatedKey(newKey);

        FP.GetSerialNumber(srno);
        String strSN = new String(srno);
        FP.GetPreAllocatedKey(pak);
        String strPAK = new String(pak);
        FP.GetFWVer(fwver);
        String strFWVer = new String(fwver);

        theMessage.setText("sn: " + strSN + " pak: " + strPAK +" fw ver: " + strFWVer);

    }
    /////////////
    private void connectreader() {
        // TODO Auto-generated method stub

        usbIf = usbDevice.getInterface(0);
        Log.d("FM220", "Interface:-" + String.valueOf(usbIf.getEndpointCount()));
        Log.d("FM220", "Interface Count: " + Integer.toString(usbDevice.getInterfaceCount()));

        Log.d("USB", String.valueOf(usbIf.getEndpointCount()));

        //    final UsbEndpoint  usbEndpoint = usbInterface.getEndpoint(0);

        epIN = null;
        epOUT = null;
        ep2IN = null;

        theMessage.setText("num of ep" + usbIf.getEndpointCount());

        epOUT = usbIf.getEndpoint(0);
        epIN = usbIf.getEndpoint(1);
        ep2IN = usbIf.getEndpoint(2);

        //	 theMessage.setText("ep num "+ ep2IN.getEndpointNumber()+"packet size "+ ep2IN.getMaxPacketSize()+"dir "+ep2IN.getDirection());
        //	 theMessage.setText("ep num "+ epIN.getEndpointNumber()+"packet size "+ epIN.getMaxPacketSize()+"dir "+epIN.getDirection());
        theMessage.setText("ep num " + epOUT.getEndpointNumber() + "packet size " + epOUT.getMaxPacketSize() + "dir " + epOUT.getDirection());

        //	 theMessage.setText("manager.hasPermission()");
        if (mUsbManager.hasPermission(usbDevice) == false) {
            //    	 theMessage.setText("manager.hasPermission() false");
            return;

        }

        conn = mUsbManager.openDevice(usbDevice);
        if(conn == null)
        {
            Log.d("FM220", "conn  is NULL !!!");
            return;
        }
        if (conn.getFileDescriptor() == -1) {
            Log.d("FM220", "Fails to open DeviceConnection");
        } else {

            Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
        }

        if (conn.releaseInterface(usbIf)) {
            Log.d("USB", "Released OK");
        } else {
            Log.d("USB", "Released fails");
        }

        if (conn.claimInterface(usbIf, true)) {
            Log.d("USB", "Claim OK");
        } else {
            Log.d("USB", "Claim fails");
        }
        //     theMessage.setText("EEPROM_read");
        //     byte [] buf= new byte [48];
        //     eeprom_read(0,48,buf);
        theMessage.setText("fm220 fileDesc" + conn.getFileDescriptor());
        //
        try {

            if (conn.getFileDescriptor() == -1) {
                connectreader();
                theMessage.setText("try connect without file descripter" + conn.getFileDescriptor());
                connectrtn = FP.ConnectCaptureDriver(conn, usbDevice);
                Log.d("FM220", "Fails to open DeviceConnection");
            } else {
                theMessage.setText("try connect with file descripter" + conn.getFileDescriptor());
                connectrtn = FP.ConnectCaptureDriver(conn, usbDevice);
                Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //String newSNStr="B1310543";
        String newSNStr="SH000856";
        byte[] tmp = newSNStr.getBytes();
        byte[] new_sn = new byte[16];
        System.arraycopy(tmp, 0, new_sn, 0,  tmp.length);
        FP.SetSerialNumber(new_sn);

        newKey[0]=65;		newKey[1]=67;		newKey[2]=80;		newKey[3]=76;
        newKey[4]=80;		newKey[5]=65;		newKey[6]=75;		newKey[7]=51;	//new PAK	ACPLPAK3
        FP.SetPreAllocatedKey(newKey);

        FP.GetSerialNumber(srno);
        String strSN = new String(srno);
        FP.GetPreAllocatedKey(pak);
        String strPAK = new String(pak);
        FP.GetFWVer(fwver);
        String strFWVer = new String(fwver);

        theMessage.setText("sn: " + strSN + " pak: " + strPAK +" fw ver: " + strFWVer);
//
    }



    /////////////
    public void goBackActivity() {

        startActivity(new Intent(getApplicationContext(), AlbumSelectorActivity.class));
        finish();
    }
}
*/


import android.os.AsyncTask;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;

//import static android.support.v7.appcompat.R.id.dialog;
import static com.startek.fm220.PixerApi.Identify;

public class fpScanActivity extends Activity {

    final int U_LEFT = -41;
    final int U_RIGHT = -42;
    final int U_UP = -43;
    final int U_DOWN = -44;
    final int U_POSITION_CHECK_MASK = 0x00002F00;
    final int U_POSITION_NO_FP = 0x00002000;
    final int U_POSITION_TOO_LOW = 0x00000100;
    final int U_POSITION_TOO_TOP = 0x00000200;
    final int U_POSITION_TOO_RIGHT = 0x00000400;
    final int U_POSITION_TOO_LEFT = 0x00000800;
    final int U_POSITION_TOO_LOW_RIGHT = (U_POSITION_TOO_LOW | U_POSITION_TOO_RIGHT);
    final int U_POSITION_TOO_LOW_LEFT = (U_POSITION_TOO_LOW | U_POSITION_TOO_LEFT);
    final int U_POSITION_TOO_TOP_RIGHT = (U_POSITION_TOO_TOP | U_POSITION_TOO_RIGHT);
    final int U_POSITION_TOO_TOP_LEFT = (U_POSITION_TOO_TOP | U_POSITION_TOO_LEFT);

    final int U_POSITION_OK = 0x00000000;

    final int U_DENSITY_CHECK_MASK = 0x000000E0;
    final int U_DENSITY_TOO_DARK = 0x00000020;
    final int U_DENSITY_TOO_LIGHT = 0x00000040;
    final int U_DENSITY_LITTLE_LIGHT = 0x00000060;
    final int U_DENSITY_AMBIGUOUS = 0x00000080;

    final int U_INSUFFICIENT_FP = -31;
    final int U_NOT_YET = -32;

    final int U_CLASS_A = 65;
    final int U_CLASS_B = 66;
    final int U_CLASS_C = 67;
    final int U_CLASS_D = 68;
    final int U_CLASS_E = 69;
    final int U_CLASS_R = 82;

    //
     // Called when the activity is first created.
     //
    private TextView theIndicator;
    private TextView theMessage;
    private Button buttonConnect;
    private Button buttonCapture;
    private Button buttonEnroll;
    private Button buttonVerify;
    private Button buttonShow;
    private Button buttonDisC;
    private int connectrtn;
    private int rtn;
    private int rtn2;
    private ImageView myImage;

    //byte[] bMapArray= new byte[1078+(256*324)];
    byte[] bMapArray = new byte[1078 + (640 * 480)];
    byte[] bISOImgArray = new byte[32 + 14  + (264 * 324)];
    private byte[] minu_code1 = new byte[512];
    private byte[] minu_code2 = new byte[512];

    byte[] srno= new byte[16];
    byte[] pak = new byte[16];
    byte[] fwver = new byte[16];
    byte[] Key2= new byte[16];
    byte[] newKey= new byte[16];

    private EventHandler m_eventHandler;
    private Bitmap bMap;

    private int counter = 0;

    private static Context Context;
    private String TAG = "fpScan";
    private ImageView pictImg;
    private ImageView fpImg;
    public static Bitmap sharebitmap;

    private Integer[] imgPress = {
            R.drawable.press, R.drawable.thumb };
    //public static final int UPDATE_TEXT_VIEW=0x0001;
////////holing add for usb host
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private UsbManager manager;
    private PendingIntent mPermissionIntent;
    private UsbDevice d;
    private UsbDeviceConnection conn;
    private UsbInterface usbIf;
    UsbEndpoint epIN;
    UsbEndpoint epOUT;
    UsbEndpoint ep2IN;
    AlertDialog dialog;
    private  String mPath="";
    private  String minu_str="";
    private  String userID_str="";
    private  int pid=0, vid=0;
    String tmp="";
    static final int BIO_TASK_Identify = 1;
    static final int Bio_Identify_Level = 2000;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    d = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(d != null){
                        //
                        Log.d(TAG,"DEATTCHED - " + d);
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
                           // checkUSBDevice();
                        }
                    }
                    else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(fpScanActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                        manager.requestPermission(device, mPermissionIntent);

                        if(device != null){
                            //
                            Log.d(TAG,"ATTACHED - ONE SHOT ");
                            //Log.d(TAG,"ATTACHED - " + device);

                            //checkUSBDevice();
                            dialog.dismiss();

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
                            Log.d(TAG,"PERMISSION GRANTED - ");
                            //checkUSBDevice();
                            connectreader();
                        }
                        else {
                            Log.d(TAG, "NONE USB DEVICE ");
                            goBackActivity();
                        }
                    }
                    else
                    {
                        Log.d(TAG,"no EXTRA_PERMISSION_GRANTED..");
                        goBackActivity();
                    }

                }
            }

        }
    };

    private void connectreader() {
        // TODO Auto-generated method stub

        usbIf = d.getInterface(0);
        Log.d("FM220", "Interface:-" + String.valueOf(usbIf.getEndpointCount()));
        Log.d("FM220", "Interface Count: " + Integer.toString(d.getInterfaceCount()));

        Log.d("USB", String.valueOf(usbIf.getEndpointCount()));

        //    final UsbEndpoint  usbEndpoint = usbInterface.getEndpoint(0);

        epIN = null;
        epOUT = null;
        ep2IN = null;

        theMessage.setText("num of ep" + usbIf.getEndpointCount());

        epOUT = usbIf.getEndpoint(0);
        epIN = usbIf.getEndpoint(1);
        ep2IN = usbIf.getEndpoint(2);

        //	 theMessage.setText("ep num "+ ep2IN.getEndpointNumber()+"packet size "+ ep2IN.getMaxPacketSize()+"dir "+ep2IN.getDirection());
        //	 theMessage.setText("ep num "+ epIN.getEndpointNumber()+"packet size "+ epIN.getMaxPacketSize()+"dir "+epIN.getDirection());
        theMessage.setText("ep num " + epOUT.getEndpointNumber() + "packet size " + epOUT.getMaxPacketSize() + "dir " + epOUT.getDirection());

        //	 theMessage.setText("manager.hasPermission()");
        if (manager.hasPermission(d) == false) {
            //    	 theMessage.setText("manager.hasPermission() false");
            Log.d("FM220", "hasPermission is false !!!");
            return;

        }

        conn = manager.openDevice(d);
        if(conn == null)
        {
            Log.d("FM220", "conn  is NULL !!!");
            FP.DisconnectCaptureDriver();
            DialogUtil.showToast(fpScanActivity.this, R.string.usb_conn_err);
            goBackActivity();
            return;
        }
        if (conn.getFileDescriptor() == -1) {
            Log.d("FM220", "Fails to open DeviceConnection");
        } else {

            Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
        }

        if (conn.releaseInterface(usbIf)) {
            Log.d("USB", "Released OK");
        } else {
            Log.d("USB", "Released fails");
        }

        if (conn.claimInterface(usbIf, true)) {
            Log.d("USB", "Claim OK");
        } else {
            Log.d("USB", "Claim fails");
        }
        //     theMessage.setText("EEPROM_read");
        //     byte [] buf= new byte [48];
        //     eeprom_read(0,48,buf);
        theMessage.setText("fm220 fileDesc" + conn.getFileDescriptor());
        //
        try {

            if (conn.getFileDescriptor() == -1) {
                connectreader();
                theMessage.setText("try connect without file descripter" + conn.getFileDescriptor());
                connectrtn = FP.ConnectCaptureDriver(conn, d);
                Log.d("FM220", "Fails to open DeviceConnection");
            } else {
                theMessage.setText("try connect with file descripter" + conn.getFileDescriptor());
                connectrtn = FP.ConnectCaptureDriver(conn, d);
                Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //String newSNStr="B1310543";
        String newSNStr="SH000856";
        byte[] tmp = newSNStr.getBytes();
        byte[] new_sn = new byte[16];
        System.arraycopy(tmp, 0, new_sn, 0,  tmp.length);
        FP.SetSerialNumber(new_sn);

        newKey[0]=65;		newKey[1]=67;		newKey[2]=80;		newKey[3]=76;
        newKey[4]=80;		newKey[5]=65;		newKey[6]=75;		newKey[7]=51;	//new PAK	ACPLPAK3
        FP.SetPreAllocatedKey(newKey);

        FP.GetSerialNumber(srno);
        String strSN = new String(srno);
        FP.GetPreAllocatedKey(pak);
        String strPAK = new String(pak);
        FP.GetFWVer(fwver);
        String strFWVer = new String(fwver);

        theMessage.setText("sn: " + strSN + " pak: " + strPAK +" fw ver: " + strFWVer);
//
        scanFPThread();
    }

    private void scanFPThread()
    {

        if (connectrtn == 0) {
            //buttonEnroll.setEnabled(false);
            m_eventHandler = new fpScanActivity.EventHandler(Looper.getMainLooper());

            //let thread do main job
            new Thread() {
                public void run() {
                    super.run();

                    FP.CreateEnrollHandle();

                    Message msg0 = new Message();
                    msg0.what = PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS;
                    m_eventHandler.sendMessage(msg0);

                    for (int i = 0; i < 6; i++) {
                        //theMessage.setText("Times: "+i);
                        SystemClock.sleep(500);
                        while ((rtn = FP.Capture()) != 0) {
                            Message msg1 = new Message();
                            msg1.what = PublicData.TEXTVIEW_PRESS_AGAIN;
                            m_eventHandler.sendMessage(msg1);
                            Message msg2 = new Message();
                            msg2.what = PublicData.SHOW_PIC;
                            m_eventHandler.sendMessage(msg2);
                        }
                        rtn = FP.GetTemplate(minu_code1);

                        //if(rtn==0)
                        //theMessage.setText("FP_GetTemplate() OK");

                        rtn = FP.ISOminutiaEnroll(minu_code1, minu_code2);
                        //theMessage.setText("enroll rtn="+rtn);

                        while (true) {
                            rtn2 = FP.CheckBlank();

                            Message msg2 = new Message();
                            msg2.what = PublicData.TEXTVIEW_REMOVE_FINGER;
                            m_eventHandler.sendMessage(msg2);

                            if (rtn2 != -1)
                                break;
                            //theMessage.setText("remove your finger!!!");
                        }

                        if (rtn == U_CLASS_A || rtn == U_CLASS_B) {
                            //FP.SaveISOminutia(minu_code2, "/system/data/fpcode.dat");
                            //FP.SaveISOminutia(minu_code2, "/data/data/com.startek.fm210/fpcode.dat");
                            FP.SaveISOminutia(minu_code2, Context.getFilesDir().getPath() + "/fpcode.dat");
                            Log.d(TAG,"i= "+i+"minutiae path"+Context.getFilesDir().getPath());
                            SystemClock.sleep(1000);
                            Message msg3 = new Message();
                            msg3.what = PublicData.TEXTVIEW_SUCCESS;
                            m_eventHandler.sendMessage(msg3);

                            break;
                        } else if (i == 5) {
                            Message msg4 = new Message();
                            msg4.what = PublicData.TEXTVIEW_FAILURE;
                            m_eventHandler.sendMessage(msg4);
                        }
                        Log.d(TAG, "Loop i: "+i);
                        //showPic();
                    }

                    FP.DestroyEnrollHandle();
                    minu_str = byteArrayToHex(minu_code2);
                    new RequestTask(BIO_TASK_Identify).execute(Identify,minu_str);

                }
            }.start();
        } else {
            theMessage.setText("FP_ConnectCaptureDriver() failed!!");
            FP.DisconnectCaptureDriver();
            return;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpscan);

        Context = getApplicationContext();
        //SetLibraryPath(Context.getFilesDir().getPath());
        FP.SetFPLibraryPath("/data/data/com.startek.fm210/lib/");
        FP.InitialSDK();
        Log.d("FM220", "FP Library init..");

        mPath = getIntent().getStringExtra("selectIma");

        theMessage = (TextView) findViewById(R.id.message);
        theIndicator = (TextView)findViewById(R.id.indicate);
        theMessage.setText("STARTEK FM220 Android SDK 0.16 build 201512091130");

        buttonConnect = (Button) findViewById(R.id.connectB);
        buttonCapture = (Button) findViewById(R.id.captureB);
        buttonEnroll = (Button) findViewById(R.id.enrollB);
        buttonVerify = (Button) findViewById(R.id.verifyB);
        buttonShow = (Button) findViewById(R.id.showB);
        buttonDisC = (Button) findViewById(R.id.discB);
       // myImage = (ImageView) findViewById(R.id.test_image);

        //holing reserve for android.hardware.usb test
        //UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        //theMessage.setText("STARTEK FM210 UsbManager manager test");
        // check for existing devices
        pictImg = (ImageView)findViewById(R.id.pictImg);
        fpImg = (ImageView)findViewById(R.id.fpImg);
        String imagePath = getIntent().getStringExtra("selectIma");
        Log.d(TAG, "imagePath: "+imagePath);
        Bitmap pictBitmap = BitmapFactory.decodeFile(imagePath);
        pictImg.setImageBitmap(pictBitmap);

/*
        new Thread(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub

                        try {
                            //PendingIntent mPermissionIntent;
                            for (UsbDevice mdevice : manager.getDeviceList().values()) {

                                int pid, vid;

                                pid = mdevice.getProductId();
                                vid = mdevice.getVendorId();

                                if (((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca)) || ((pid == 0x8220) && (vid == 0x0b39)) || ((pid == 0x8210) && (vid == 0x0b39))) {
                                    theMessage.setText("fm220 pid found");
                                    d = mdevice;

                                    manager.requestPermission(d, mPermissionIntent);

                                    theIndicator.setText(R.string.press_fp);
                                    //fpImg.setData(imgPress);
                                    //fpImg.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "FM220 DEVICE FOUND");
                                    break;

                                }

                            }
                        }
                        catch(Exception e){
                        e.printStackTrace();
                    }
            }
        }).start();
*/
        fpImg.setVisibility(View.VISIBLE);
        fpImg.postInvalidate();
        //PendingIntent mPermissionIntent;
        for (UsbDevice mdevice : manager.getDeviceList().values()) {



            pid = mdevice.getProductId();
            vid = mdevice.getVendorId();

            if (((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca)) || ((pid == 0x8220) && (vid == 0x0b39)) || ((pid == 0x8210) && (vid == 0x0b39))) {
                theMessage.setText("fm220 pid found");
                d = mdevice;

                manager.requestPermission(d, mPermissionIntent);

                theIndicator.setText(R.string.press_fp);
                //fpImg.setData(imgPress);
                //fpImg.setVisibility(View.VISIBLE);
                Log.d(TAG, "FM220 DEVICE FOUND");
                break;

            }

        }
        if(((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca)) || ((pid == 0x8220) && (vid == 0x0b39)) || ((pid == 0x8210) && (vid == 0x0b39))) {
            Log.d(TAG, "FM220 DEVICE FOUND");
        }
        else
        {
            Log.d(TAG, "FM220 DEVICE NOT FOUND");
            goBackActivity();
        }
            /////ori connect here
/*
        //Connect
        buttonConnect.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {

                //Log.v("Fm210", "Marcus: Click");
                try {

                    if (conn.getFileDescriptor() == -1) {
                        connectreader();
                        theMessage.setText("try connect without file descripter" + conn.getFileDescriptor());
                        connectrtn = FP.ConnectCaptureDriver(conn, d);
                        Log.d("FM220", "Fails to open DeviceConnection");
                    } else {
                        theMessage.setText("try connect with file descripter" + conn.getFileDescriptor());
                        connectrtn = FP.ConnectCaptureDriver(conn, d);
                        Log.d("FM220", "Opened DeviceConnection" + Integer.toString(conn.getFileDescriptor()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //String newSNStr="B1310543";
                String newSNStr="SH000856";
                byte[] tmp = newSNStr.getBytes();
                byte[] new_sn = new byte[16];
                System.arraycopy(tmp, 0, new_sn, 0,  tmp.length);
                FP.SetSerialNumber(new_sn);

                newKey[0]=65;		newKey[1]=67;		newKey[2]=80;		newKey[3]=76;
                newKey[4]=80;		newKey[5]=65;		newKey[6]=75;		newKey[7]=51;	//new PAK	ACPLPAK3
                FP.SetPreAllocatedKey(newKey);

                FP.GetSerialNumber(srno);
                String strSN = new String(srno);
                FP.GetPreAllocatedKey(pak);
                String strPAK = new String(pak);
                FP.GetFWVer(fwver);
                String strFWVer = new String(fwver);

                theMessage.setText("sn: " + strSN + " pak: " + strPAK +" fw ver: " + strFWVer);

            }
        });
  // button ...
        //Capture
        buttonCapture.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {

                Log.v("FM220", "Marcus: Click");
                try {

                    if (connectrtn == 0) {
                        m_eventHandler = new EventHandler(Looper.getMainLooper());
//					CaptureThread m_captureThread = new CaptureThread(m_eventHandler);
//					Thread m_capture = new Thread(m_captureThread);
//					m_capture.start();
                        buttonCapture.setEnabled(false);

                        new Thread() {
                            public void run() {
                                super.run();

                                FP.Capture();
//                                FP.Capture();// TODO ?
                                Message msg0 = new Message();
                                msg0.what = PublicData.TEXTVIEW_CAPTURE_PLEASE_PRESS;
                                m_eventHandler.sendMessage(msg0);

                                counter++;
                                if ((counter % 15) == 0) {
                                    Log.v("Fm210", "Start GC");
                                    System.gc();
                                }

                                Log.v("Fm210", "Marcus: run");
                                //InitialSDK();
                                //Log.v("Fm210", "Marcus: InitialSDK() OK");
                                //PublicData.captureDone=false;
                                counter = 0;
                                while ((rtn = FP.Capture()) != 0) {
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.SHOW_PIC;
                                    m_eventHandler.sendMessage(msg2);
                                    Message msg3 = new Message();
                                    msg3.what = PublicData.SHOW_NFIQ;
                                    m_eventHandler.sendMessage(msg3);
                                    if (counter > 10)
                                        break;
                                    counter++;
                                    if (rtn == -2)    //capture fail with abnormal behavior disconnect or device error
                                        break;

                                }
                                Log.d("FM220", "Marcus: FP_Capture OK");
                                //FP.SaveImageBMP("/system/data/fp_image.bmp");
                                //FP.SaveImageBMP("/data/data/com.startek.fm210/fp_image.bmp");
                                //FP.SaveImageBMP(Context.getFilesDir().getPath() + "/fp_image.bmp");

                                FP.SaveImageBMP(Environment.getExternalStorageDirectory().toString() + "/DCIM/fp_image.bmp");
                                FP.GetISOImageBuffer((byte)0,(byte)0,bISOImgArray);
                                Logger.d("ISO img  = " + Arrays.toString(bISOImgArray));


                                rtn = FP.GetTemplate(minu_code1);
                                // FP.SaveISOminutia(minu_code1, Context.getFilesDir().getPath() + "/fpcode.dat");
                                FP.SaveISOminutia(minu_code1, Environment.getExternalStorageDirectory().toString() + "/DCIM/fpcode.dat");

                                //byteArrayToInt(minu_code1);
                                Log.d("Fm220","minu_code1..: "+byteArrayToHex(minu_code1));
                                Log.d("FM220","path: "+Environment.getExternalStorageDirectory().toString() + "/DCIM/fpcode.dat");

                                Message msg1 = new Message();
                                msg1.what = PublicData.TEXTVIEW_SUCCESS;
                                m_eventHandler.sendMessage(msg1);
                                //Log.v("Fm210", "Marcus: FP_SaveImageBMP OK");
//							try{
//							//Thread.sleep(100);
//							}
//							catch(Exception e){}

                                Message msg2 = new Message();
                                //msg2 = new Message();
                                msg2.what = PublicData.SHOW_PIC;
                                m_eventHandler.sendMessage(msg2);

                                //	FP_LedOff();

                            }
                        }.start();
                    } else {
                        theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                        theMessage.postInvalidate();
                        FP.DisconnectCaptureDriver();
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        //Enroll ori
        buttonEnroll.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //		led_off();

                if (connectrtn == 0) {
                    buttonEnroll.setEnabled(false);
                    m_eventHandler = new EventHandler(Looper.getMainLooper());

                    //let thread do main job
                    new Thread() {
                        public void run() {
                            super.run();

                            FP.CreateEnrollHandle();

                            Message msg0 = new Message();
                            msg0.what = PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS;
                            m_eventHandler.sendMessage(msg0);

                            for (int i = 0; i < 6; i++) {
                                //theMessage.setText("Times: "+i);
                                SystemClock.sleep(500);
                                while ((rtn = FP.Capture()) != 0) {
                                    Message msg1 = new Message();
                                    msg1.what = PublicData.TEXTVIEW_PRESS_AGAIN;
                                    m_eventHandler.sendMessage(msg1);
                                    Message msg2 = new Message();
                                    msg2.what = PublicData.SHOW_PIC;
                                    m_eventHandler.sendMessage(msg2);
                                }
                                rtn = FP.GetTemplate(minu_code1);

                                //if(rtn==0)
                                //theMessage.setText("FP_GetTemplate() OK");

                                rtn = FP.ISOminutiaEnroll(minu_code1, minu_code2);
                                //theMessage.setText("enroll rtn="+rtn);

                                while (true) {
                                    rtn2 = FP.CheckBlank();

                                    Message msg2 = new Message();
                                    msg2.what = PublicData.TEXTVIEW_REMOVE_FINGER;
                                    m_eventHandler.sendMessage(msg2);

                                    if (rtn2 != -1)
                                        break;
                                    //theMessage.setText("remove your finger!!!");
                                }

                                if (rtn == U_CLASS_A || rtn == U_CLASS_B) {
                                    //FP.SaveISOminutia(minu_code2, "/system/data/fpcode.dat");
                                    //FP.SaveISOminutia(minu_code2, "/data/data/com.startek.fm210/fpcode.dat");
                                    FP.SaveISOminutia(minu_code2, Context.getFilesDir().getPath() + "/fpcode.dat");

                                    SystemClock.sleep(1000);
                                    Message msg3 = new Message();
                                    msg3.what = PublicData.TEXTVIEW_SUCCESS;
                                    m_eventHandler.sendMessage(msg3);

                                    break;
                                } else if (i == 5) {
                                    Message msg4 = new Message();
                                    msg4.what = PublicData.TEXTVIEW_FAILURE;
                                    m_eventHandler.sendMessage(msg4);
                                }
                                //showPic();
                            }

                            FP.DestroyEnrollHandle();
                        }
                    }.start();
                } else {
                    theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                    FP.DisconnectCaptureDriver();
                    return;
                }

            }

        });


        buttonVerify.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (connectrtn == 0) {
                        m_eventHandler = new EventHandler(Looper.getMainLooper());
                        buttonVerify.setEnabled(false);

                        new Thread() {
                            public void run() {
                                super.run();

                                //if((rtn=FP_LoadISOminutia(minu_code2, "/system/data/fpcode.dat"))==0){
                                //if((rtn=FP_LoadISOminutia(minu_code2, "/data/data/com.startek.fm210/fpcode.dat"))==0){
                                if ((rtn = FP.LoadISOminutia(minu_code2, Context.getFilesDir().getPath() + "/fpcode.dat")) == 0) {
                                    Message msg1 = new Message();
                                    msg1.what = PublicData.TEXTVIEW_FILE_EXIST;
                                    m_eventHandler.sendMessage(msg1);
                                    if (connectrtn == 0) {
                                    } else {
                                        FP.DisconnectCaptureDriver();
                                        return;
                                    }

                                    counter++;
                                    if ((counter % 15) == 0) {
                                        Log.v("Fm210", "Start GC");
                                        System.gc();
                                    }

                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                    }
                                    Message msg0 = new Message();
                                    msg0.what = PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS;
                                    m_eventHandler.sendMessage(msg0);

                                    while ((rtn = FP.Capture()) != 0) {
                                        Message msg2 = new Message();
                                        msg2.what = PublicData.SHOW_PIC;
                                        m_eventHandler.sendMessage(msg2);
                                    }

                                    //FP.SaveImageBMP("/system/data/fp_image.bmp");
                                    //FP.SaveImageBMP("/data/data/com.startek.fm210/fp_image.bmp");
                                    FP.SaveImageBMP(Context.getFilesDir().getPath() + "/fp_image.bmp");

                                    rtn = FP.GetTemplate(minu_code1);
                                    //rtn=FP.ISOminutiaMatchEx(minu_code1, minu_code2);
                                    //rtn=FP.ISOminutiaMatch180Ex(minu_code1, minu_code2);
                                    rtn = FP.ISOminutiaMatch360Ex(minu_code1, minu_code2);

                                    if (rtn >= -1) {
                                        Message msg2 = new Message();
                                        msg2 = new Message();
                                        msg2.what = PublicData.TEXTVIEW_SCORE;
                                        m_eventHandler.sendMessage(msg2);

                                        Message msg3 = new Message();
                                        msg3 = new Message();
                                        msg3.what = PublicData.SHOW_PIC;
                                        m_eventHandler.sendMessage(msg3);
                                    }

                                } else {
                                    Message msg4 = new Message();
                                    msg4.what = PublicData.TEXTVIEW_FILE_NOT_EXIST;
                                    m_eventHandler.sendMessage(msg4);
                                    return;
                                }
                            }
                        }.start();
                    } else {
                        theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                        theMessage.postInvalidate();
                        FP.DisconnectCaptureDriver();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        buttonShow.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        buttonDisC.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                conn.close();
                FP.DisconnectCaptureDriver();
                theMessage.setText("FP_DisconnectCaptureDriver() Succeeded!!");
                theMessage.postInvalidate();

            }
        });

        buttonVerify.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                new RequestTask(BIO_TASK_Identify).execute(Identify,"464d5200203230000000009c00000118015400c500c501000000641580d300465264808c0063686480c4006e5e644087009d686480fb00bce16480f300c55e6480a900d2df64807400d96d64803000e3156480e300ecdb64806b00f0746480c500f65764810000f9d96480a700fb576480540103926480fe011e5b64806001239e6440fc0128d6648065012cda6480630130c864409d013d556400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
            }
        });
        */
    }

    public void checkUSBDevice(){
        int pid, vid;

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Log.d(TAG,"check.."+ deviceList.size()+" USB device(s) found");
        if(deviceList.size() == 0)
        {
            dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.usb_disconnect)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBackActivity();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkUSBDevice();
                        }


                    })
                    .create();
            dialog.show();
        }
        else {

            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                d = deviceIterator.next();
                Log.d(TAG, "LIST: ");
                Log.d(TAG, String.valueOf(d));

                vid = d.getVendorId();
                pid = d.getProductId();

                if (((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca)) || ((pid == 0x8220) && (vid == 0x0b39)) || ((pid == 0x8210) && (vid == 0x0b39))) {
                    theMessage.setText(R.string.usb_connected);
                } else {
                    theMessage.setText(R.string.unknown_device);
                    DialogUtil.showToast(fpScanActivity.this, R.string.unknown_device);
                    goBackActivity();
                }
            }
        }

        //fpImg.setData(imgThumbIds);
        //fpImg.setVisibility(View.VISIBLE);

        //connectreader1();
//        progressDialog = new ProgressDialog(fpScanActivity.this);
//        progressDialog.setTitle(R.string.information);
//        progressDialog.setMessage(getResources().getString(R.string.press_fp));
//        progressDialog.setCancelable(false);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.show();


    }

    public static String byteArrayToHex(byte[] b)
    {
        String result ="";
        for (int i=0 ; i<b.length ; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mUsbReceiver);
    }

    class showPic extends AsyncTask<String, Void, String> {
        //       private ImageView image;
        private Bitmap bMap = null;

        @Override
        protected String doInBackground(String... path) {
            tryGetStream();
            return null;
        }

        protected void onPostExecute(String a) {
           //myImage.setImageBitmap(bMap);
            fpImg.setImageBitmap(bMap);
            bMap = null;
            System.gc();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            fpImg.postInvalidate();
            //myImage.postInvalidate();
            //Log.v("Fm210", "Marcus: onProgressUpdate");
        }

        private void tryGetStream() {
            try {
                //buf = FP_GetImageBuffer);
                FP.GetImageBuffer(bMapArray);
                Logger.d(Arrays.toString(Arrays.copyOf(bMapArray, 512)));

                bMap = BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class EventHandler extends android.os.Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PublicData.TEXTVIEW_SUCCESS:
//                    buttonCapture.setEnabled(true);
//                    buttonEnroll.setEnabled(true);
//                    buttonVerify.setEnabled(true);
                    theIndicator.setText("Success");
                    theIndicator.postInvalidate();
                    theMessage.setText("Success.");
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FAILURE:
                    theMessage.setText("Failure.");
                    theMessage.postInvalidate();
                    buttonEnroll.setEnabled(true);
                    break;
                case PublicData.TEXTVIEW_CAPTURE_PLEASE_PRESS:
                    //theMessage.setText("Capture: Press your finger");
                    //theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS:
                    theIndicator.setText("Enroll: Press your finger");
                    theIndicator.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS:
                    theMessage.setText("Verify: Press your finger");
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_SCORE:
                    theMessage.setText("matching score=" + (int) FP.Score());
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FILE_EXIST:
                    theMessage.setText("Verify: File exist");
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FILE_NOT_EXIST:
                    theMessage.setText("File not exist, please enroll first");
                    theMessage.postInvalidate();
                    buttonVerify.setEnabled(true);
                    break;
                case PublicData.TEXTVIEW_REMOVE_FINGER:
                    theIndicator.setText("Please remove your finger");
                    theIndicator.postInvalidate();
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");

                    break;
                case PublicData.TEXTVIEW_PRESS_AGAIN:
                    theIndicator.setText("Please press your finger again");
                    theIndicator.postInvalidate();
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");
                    break;
                case PublicData.SHOW_PIC:
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");
                    //buttonCapture.setEnabled(true);
                    //buttonEnroll.setEnabled(true);
                    //buttonVerify.setEnabled(true);
                    break;
                case PublicData.SHOW_NFIQ:
                    theMessage.setText("nfiq " + FP.GetNFIQ());
                    theMessage.postInvalidate();
                    break;

            }
            super.handleMessage(msg);
        }
    }


    ///////////////////////
    public void Minutiae2QRcode() {
        // QR code 的內容
        // minu_str = "464d5200203230000000012c00000108014400c500c501000000642d402d001092644012001495644095001c12644023001e146440da00287b64402900298a644055002d94644067002f0f64409a00300a6440b500318764401800338b6480ed003886644020003ba16440ad0040ff64404b00441464809200459564401c00472464409c0047096440a9004a86644085004e0f6440f4005ff36480ae0063066440ce006ff964805400761264408000780464409a007ffd6440850093056440c9009cf46440ca00a8f064408000af0564409400bc7b64401300bf876440bc00c17a64404700d11264802300d21a6440f200d5626440e200e1e964409f00e2fe6440dc00f7e664402000fc93648071010790644064010d08644079010f846440340111166440870129f964000000ea8a64404600ec9164403800ee176480cc00f2fc6480ab00f4066440c600f8766440a60107896440d8010e7d6440a90110fe6440aa0114846480c401168364803501209a6440c30120056440cc01247b6440ba012a82648087012c9f64806b012da26400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        // userID_str = "654321";

        // QR code 寬度
        int QRCodeWidth = 600;
        // QR code 高度
        int QRCodeHeight = 600;
        // QR code 內容編碼
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        Log.d("Merge", "minu_len: " + minu_str.length());
        Log.d("Merge", "ID_len: " + userID_str.length());

        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            // 容錯率姑且可以將它想像成解析度，分為 4 級：L(7%)，M(15%)，Q(25%)，H(30%)
            // 設定 QR code 容錯率為 H
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            // 建立 QR code 的資料矩陣
            BitMatrix result = writer.encode(minu_str, BarcodeFormat.QR_CODE, QRCodeWidth, QRCodeHeight, hints);
            // ZXing 還可以生成其他形式條碼，如：BarcodeFormat.CODE_39、BarcodeFormat.CODE_93、BarcodeFormat.CODE_128、BarcodeFormat.EAN_8、BarcodeFormat.EAN_13...

            //建立點陣圖
            Bitmap bitmap = Bitmap.createBitmap(QRCodeWidth, QRCodeHeight, Bitmap.Config.ARGB_8888);
            // 將 QR code 資料矩陣繪製到點陣圖上
            for (int y = 0; y < QRCodeHeight; y++) {
                for (int x = 0; x < QRCodeWidth; x++) {
                    bitmap.setPixel(x, y, result.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            //ImageView imgView = (ImageView) findViewById(R.id.imageView);
            // 設定為 QR code 影像
            fpImg.setImageBitmap(bitmap);
            Log.d("Merge", "qrImage H:" + bitmap.getHeight() + "   w:" + bitmap.getWidth());

            ///////////////////////////////////////////////
            //                user ID to QRCDOE          //
            //////////////////////////////////////////////
            QRCodeWidth = 200;
            // QR code 高度
            QRCodeHeight = 200;
            // QR code 內容編碼
            //Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            //hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            Log.d("Merge", "minu_len: " + minu_str.length());
            Log.d("Merge", "ID_len: " + userID_str.length());

            //MultiFormatWriter writer = new MultiFormatWriter();
            // 容錯率姑且可以將它想像成解析度，分為 4 級：L(7%)，M(15%)，Q(25%)，H(30%)
            // 設定 QR code 容錯率為 H
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            // 建立 QR code 的資料矩陣
            result = writer.encode(userID_str, BarcodeFormat.QR_CODE, QRCodeWidth, QRCodeHeight, hints);
            // ZXing 還可以生成其他形式條碼，如：BarcodeFormat.CODE_39、BarcodeFormat.CODE_93、BarcodeFormat.CODE_128、BarcodeFormat.EAN_8、BarcodeFormat.EAN_13...

            //建立點陣圖
            Bitmap bitmap1 = Bitmap.createBitmap(QRCodeWidth, QRCodeHeight, Bitmap.Config.ARGB_8888);
            // 將 QR code 資料矩陣繪製到點陣圖上
            for (int y = 0; y < QRCodeHeight; y++) {
                for (int x = 0; x < QRCodeWidth; x++) {
                    bitmap1.setPixel(x, y, result.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            //ImageView imgView = (ImageView) findViewById(R.id.imageView);
            // 設定為 QR code 影像
            fpImg.setImageBitmap(bitmap1);
            Log.d("Merge", "qrImage H:" + bitmap1.getHeight() + "   w:" + bitmap1.getWidth());


            Bitmap qrBitmap = mergeBitmap_TB(bitmap, bitmap1, true);
            //////////////////////////////////////////////
            // BitmapFactory.Options options = createOption(mPath);
            Bitmap source = BitmapFactory.decodeFile(mPath);

            Log.d("Merge", "source H:" + source.getHeight() + "   w:" + source.getWidth());
            //saveBitmap(source);
            Bitmap dist = mergeBitmap_LR(source, qrBitmap, false);
            Log.d("Merge", "dist H:" + dist.getHeight() + "   w:" + dist.getWidth());


            //Bitmap rectBmp = Bitmap.createBitmap(bitmap.getWidth(), source.getHeight(),Bitmap.Config.ARGB_8888);
            // Bitmap dist = mergeBitmap(rectBmp, bitmap);
            // Log.d("Merge", "dist H:"+dist.getHeight()+"   w:"+dist.getWidth());

            //myImage.setImageBitmap(dist);
            fpImg.setImageBitmap(dist);
            //saveBitmap(dist);


            sharebitmap = dist;
            new Thread("saveBitmap") {
                @Override
                public void run() {
                    saveBitmap(sharebitmap);

                    Log.d("merge", "saveBitmap done");
                }
            }.start();

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

        /**
     * 把两个位图覆盖合成为一个位图，左右拼接
     * @param leftBitmap
     * @param rightBitmap
     * @param isBaseMax 是否以宽度大的位图为准，true则小图等比拉伸，false则大图等比压缩
     * @return
     */
    public static Bitmap mergeBitmap_LR(Bitmap leftBitmap, Bitmap rightBitmap, boolean isBaseMax) {

        if (leftBitmap == null || leftBitmap.isRecycled()
                || rightBitmap == null || rightBitmap.isRecycled()) {
            Log.d("Merge", "leftBitmap=" + leftBitmap + ";rightBitmap=" + rightBitmap);
            return null;
        }
        int height = 0; // 拼接后的高度，按照参数取大或取小
        /*if (isBaseMax) {
            height = leftBitmap.getHeight() > rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
        } else {
            height = leftBitmap.getHeight() < rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
        }*/

        //Ivan
        height = leftBitmap.getHeight();
        while(height>3000) {
            height = height/ 2;
            Log.d("Merge", "height: " + height + "  w: " + (leftBitmap.getWidth() * 1f / leftBitmap.getHeight() * height));
        }
        //

        // 缩放之后的bitmap
        Bitmap tempBitmapL = leftBitmap;
        Bitmap tempBitmapR = rightBitmap;

        Log.d("Merge", "BitmapL H:"+leftBitmap.getHeight()+",  W:"+leftBitmap.getWidth());
        Log.d("Merge", "BitmapR H:"+rightBitmap.getHeight()+",  W:"+rightBitmap.getWidth());

        if (leftBitmap.getHeight() != height) {
            tempBitmapL = Bitmap.createScaledBitmap(leftBitmap, (int)(leftBitmap.getWidth()*1f/leftBitmap.getHeight()*height), height, false);
        }
//        else if (rightBitmap.getHeight() != height) {
//            tempBitmapR = Bitmap.createScaledBitmap(rightBitmap, (int)(rightBitmap.getWidth()*1f/rightBitmap.getHeight()*height), height, false);
//        }
        Log.d("Merge", "Scaling BitmapL H:"+tempBitmapL.getHeight()+",  W:"+tempBitmapL.getWidth());
        Log.d("Merge", "Scaling BitmapR H:"+tempBitmapR.getHeight()+",  W:"+tempBitmapR.getWidth());


        // 拼接后的宽度
        int width = tempBitmapL.getWidth() + tempBitmapR.getWidth();
        Log.d("Merge", "New W:"+width);
        Log.d("Merge", "tempBitmapL.getWidth()"+tempBitmapL.getWidth()+"getHeight()"+tempBitmapL.getHeight());
        Log.d("Merge", "tempBitmapR.getWidth()"+tempBitmapR.getWidth()+"getHeight()"+tempBitmapR.getHeight());
        // 定义输出的bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 缩放后两个bitmap需要绘制的参数
        Rect leftRect = new Rect(0, 0, tempBitmapL.getWidth(), tempBitmapL.getHeight());
        Rect rightRect  = new Rect(0, 0, tempBitmapR.getWidth(), tempBitmapR.getHeight());

        // 右边图需要绘制的位置，往右边偏移左边图的宽度，高度是相同的
        //Rect rightRectT  = new Rect(tempBitmapL.getWidth(), 0, width, height);
        Log.d("Merge", "qrcode W:"+width);
        Rect rightRectT  = new Rect(tempBitmapL.getWidth(),(height-1000)/2 , width,(height-1000)/2+1000 );
        canvas.drawBitmap(tempBitmapL, leftRect, leftRect, null);
        canvas.drawBitmap(tempBitmapR, rightRect, rightRectT, null);
        return bitmap;
    }
    /**
     * 把两个位图覆盖合成为一个位图，以底层位图的长宽为基准
     * @param backBitmap 在底部的位图
     * @param frontBitmap 盖在上面的位图
     * @return
     */
    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {

        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            Log.e("Merge", "backBitmap=" + backBitmap + ";frontBitmap=" + frontBitmap);
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Rect baseRect  = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null);
        return bitmap;
    }

    /*
     * 把两个位图覆盖合成为一个位图，上下拼接
     * @param leftBitmap
     * @param rightBitmap
     * @param isBaseMax 是否以高度大的位图为准，true则小图等比拉伸，false则大图等比压缩
     * @return
     */
    public static Bitmap mergeBitmap_TB(Bitmap topBitmap, Bitmap bottomBitmap, boolean isBaseMax) {

        if (topBitmap == null || topBitmap.isRecycled()
                || bottomBitmap == null || bottomBitmap.isRecycled()) {
            Log.d("Merge", "topBitmap=" + topBitmap + ";bottomBitmap=" + bottomBitmap);
            return null;
        }
        int width = 0;
 /*       if (isBaseMax) {
            width = topBitmap.getWidth() > bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        } else {
            width = topBitmap.getWidth() < bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        }
*/

        Bitmap tempBitmapT = topBitmap;
        Bitmap tempBitmapB = bottomBitmap;
        width = tempBitmapT.getWidth();
        Log.d("Merge", "BitmapT H:"+tempBitmapT.getHeight()+",  W:"+tempBitmapT.getWidth());
        Log.d("Merge", "BitmapB H:"+tempBitmapB.getHeight()+",  W:"+tempBitmapB.getWidth());

/*
        if (topBitmap.getWidth() != width) {
            tempBitmapT = Bitmap.createScaledBitmap(topBitmap, width, (int)(topBitmap.getHeight()*1f/topBitmap.getWidth()*width), false);
        } else if (bottomBitmap.getWidth() != width) {
            tempBitmapB = Bitmap.createScaledBitmap(bottomBitmap, width, (int)(bottomBitmap.getHeight()*1f/bottomBitmap.getWidth()*width), false);
        }
*/
        int height = tempBitmapT.getHeight() + tempBitmapB.getHeight()+200;
        Log.d("Merge", "New H:"+height+"  ,W:"+width);
        Log.d("Merge", "tempBitmapT.getWidth()"+tempBitmapT.getWidth()+"getHeight()"+tempBitmapT.getHeight());
        Log.d("Merge", "tempBitmapB.getWidth()"+tempBitmapB.getWidth()+"getHeight()"+tempBitmapB.getHeight());


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Rect topRect = new Rect(0, 0, tempBitmapT.getWidth(), tempBitmapT.getHeight());
        Rect bottomRect  = new Rect(0, 0, tempBitmapB.getWidth(), tempBitmapB.getHeight());

        //Rect bottomRectT  = new Rect(0, tempBitmapT.getHeight(), width, height);
        Rect bottomRectT  = new Rect(200, tempBitmapT.getHeight()+200, 200+200, height);

        canvas.drawBitmap(tempBitmapT, topRect, topRect, null);
        canvas.drawBitmap(tempBitmapB, bottomRect, bottomRectT, null);

        Log.d("Merge", "QR bitmap:"+bitmap.getWidth()+"getHeight()"+bitmap.getHeight());

        return bitmap;
    }

    public void saveBitmap(Bitmap bitmap) {
        FileOutputStream fOut;
        //tmp="";
        try {

            //TBD.... FILE NAME AND FLODER...
            String subName = mPath.substring(mPath.lastIndexOf("/IMG")+4);
            Log.d("merge", "sub name: "+subName);

            String tempName = mPath.substring(mPath.lastIndexOf("/")+1);
            String dirName = mPath.substring(0, mPath.length()- tempName.length());
            dirName = dirName+"Startek/";
            Log.d("merge", "folder: "+dirName);

            //String dirName = mPat h.substring(mPath.lastIndexOf("/")+1);
            File dir = new File(dirName);

            if (!dir.exists()) {
                dir.mkdir();
                MediaScannerConnection.scanFile(getApplicationContext(),
                        new String[]{dir.getAbsolutePath()},
                        null,
                        null);
            }

            tmp = dirName+"FMR" + subName;
            //String tmp = "/sdcard/demo/takepicture.jpg";
            Log.d("merge", "tmp: "+tmp);

            fOut = new FileOutputStream(tmp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                // e.printStackTrace();
                Log.d("merge", "IOException: ",e);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File img = new File(tmp);

        // Try to update MTP
       MediaScannerConnection.scanFile(getApplicationContext(),
                new String[]{img.getAbsolutePath()},
                null,
                null);

        runOnUiThread(new Runnable() {


            @Override
            public void run() {
                new AlertDialog.Builder(fpScanActivity.this)
                        .setTitle(R.string.done)
                        .setMessage(tmp)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                goBackActivity();
                            }
                        })
                        .show();
            }
        });

    }

    //////////////////////
    private  void Identify_Checkagain()
    {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.hostprompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);
        TextView ipInput = (TextView)promptsView.findViewById(R.id.textView1);
        userInput.setVisibility(View.GONE);

        ipInput.setText(R.string.try_again);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                scanFPThread();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                DialogUtil.showToast(fpScanActivity.this, R.string.scan_fail);
                                goBackActivity();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private  void Identify_Done(String message) throws JSONException {
        //String message =json.getString("Message");
        JSONObject json = new JSONObject(message);
        int code = json.getInt("code");

        if (code == 200) {
            JSONObject jsonObj = json.getJSONObject("data");
            String userID = jsonObj.getString("userId");
            String fpIndex = jsonObj.getString("fpIndex");
            String score = jsonObj.getString("score");

            Log.d(TAG,"score  "+score);
            Log.d(TAG,"fpIndex  "+fpIndex);
            Log.d(TAG,"userID "+userID);
            userID_str= userID;

            if(Integer.parseInt(score) <= Bio_Identify_Level ){
                //Toast.makeText(this, R.string.Identify_err, Toast.LENGTH_LONG).show();
                //TBD
                Log.d(TAG, "try again or abort");
                Identify_Checkagain();
            }else
            {
                Log.d(TAG, "Get minutiae Done");
                Minutiae2QRcode();
            }




        } else if (code == 404) {
            //scanAgain();
            Log.d(TAG,"Identify Again...");
            Identify_Checkagain();
           // goNextView(R.id.activate_scene);
        } else {
            Log.d(TAG,"ERROR ERROR ");
            DialogUtil.showToast(this, R.string.general_request_error, code, message);
            goBackActivity();
        }

    }

    public void goBackActivity() {

        Intent intent = new Intent(fpScanActivity.this, AlbumSelectorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //startActivity(new Intent(getApplicationContext(), AlbumSelectorActivity.class));
        startActivity(intent);
        finish();
    }
    class RequestTask extends PixerApi.NetworkOperation {
        int mId;

        RequestTask(int id) {
            mId = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showWaitingDialog();
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG,"Server response onPostExecute"+result);

            switch (mId){
                case BIO_TASK_Identify:
                    try {
                        Identify_Done(result);
                    }catch (JSONException e){
                        Log.d(TAG, e.toString());
                    }

                    Log.d(TAG,"Server response switch"+mId);
                    break;
            }
//            dismissWaiting();
//
//            try {
//                switch (mId) {
//                    case TASK_LOGIN:
//                        loginDone(json);
//                        break;
//
//                    case TASK_REGISTER:
//                        registerDone(json);
//                        break;
//
//                    case TASK_FORGOT_PASSWORD:
//                        forgetPasswordDone(json);
//                        break;
//
//                    case TASK_VERIFY_EMAIL:
//                        verifyEmailDone(json);
//                        break;
//
//                    case TASK_REGISTER_RESEND:
//                        registerResendDone(json);
//                        break;
//
//                    case TASK_VERIFY_CHANGE_PASSWORD:
//                        verifyChangePasswordDone(json);
//                        break;
//
//                    case TASK_LOGIN_3PARTY:
//                        login3PartyDone(json);
//                        break;
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "exception", e);
//                DialogUtil.showToast(EntryActivity.this, R.string.connection_error);
//            }
        }

        @Override
        protected String doInBackground(String... params) {
            return super.doInBackground(params);
        }
    }
}
