package com.startek.fm220;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.orhanobut.logger.Logger;
import com.startek.fingerprint.library.FP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class SignInActivity extends Activity {

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

    /**
     * Called when the activity is first created.
     */
    private TextView theMessage;
    private TextView dbgMsg;
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
    //private ImageView qrImage;

    //byte[] bMapArray= new byte[1078+(256*324)];
    byte[] bMapArray = new byte[1078 + (640 * 480)];
    byte[] bISOImgArray = new byte[32 + 14  + (264 * 324)];
    private byte[] minu_code1 = new byte[512];
    private byte[] minu_code2 = new byte[512];

    private String minutiaeStr="";
    byte[] srno= new byte[16];
    byte[] pak = new byte[16];
    byte[] fwver = new byte[16];
    byte[] Key2= new byte[16];
    byte[] newKey= new byte[16];

    private EventHandler m_eventHandler;
    private Bitmap bMap;

    private int counter = 0;

    private static Context Context;
     private  String mPath="";
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

    public static Bitmap sharebitmap;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            theMessage.setText("mUsbReceiver");
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            theMessage.setText("fm220 found and try connect");
                            Log.d("FM220", "fm220 found and try connect");
                            connectreader();
                        }
                    } else {
                        //      Log.d(TAG, "permission denied for device " + device);
                        theMessage.setText("fm220 found");
                        Log.d("FM220", "fm220 permission denied ..");

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
            return;

        }

        conn = manager.openDevice(d);

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

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mPath = getIntent().getStringExtra("selectIma");

        Context = getApplicationContext();
        //SetLibraryPath(Context.getFilesDir().getPath());
        FP.SetFPLibraryPath("/data/data/com.startek.fm210/lib/");
        FP.InitialSDK();
        Log.d("FM220", "FP Library init..");

        theMessage = (TextView) findViewById(R.id.message);
        dbgMsg = (TextView) findViewById(R.id.dbgmsg);

        theMessage.setText("STARTEK FM220 Android SDK 0.16 build 201512091130");
        dbgMsg.setText("TARTEK FM220 Android SDK 0.16 build 201707111700");
        buttonConnect = (Button) findViewById(R.id.connectB);
        buttonCapture = (Button) findViewById(R.id.captureB);
        buttonEnroll = (Button) findViewById(R.id.enrollB);
        buttonVerify = (Button) findViewById(R.id.verifyB);
        buttonShow = (Button) findViewById(R.id.showB);
        buttonDisC = (Button) findViewById(R.id.discB);
        myImage = (ImageView) findViewById(R.id.test_image);

//        qrImage = (ImageView) findViewById(R.id.minu_image);
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

        //PendingIntent mPermissionIntent;
        for (UsbDevice mdevice : manager.getDeviceList().values()) {

            int pid, vid;

            pid = mdevice.getProductId();
            vid = mdevice.getVendorId();

            if (((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca)) || ((pid == 0x8220) && (vid == 0x0b39)) || ((pid == 0x8210) && (vid == 0x0b39))) {
                theMessage.setText("fm220 pid found");
                d = mdevice;

                manager.requestPermission(d, mPermissionIntent);

                break;

            }

        }
        /////ori connect here
/* ////Ivan
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

*////Ivan
/*        //Enroll test
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

                       //     FP.CreateEnrollHandle();

                            Message msg0 = new Message();
                            msg0.what = PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS;
                            m_eventHandler.sendMessage(msg0);


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
                                FP.SaveISOminutia(minu_code1, Context.getFilesDir().getPath() + "/fpcode.dat");

                                    SystemClock.sleep(1000);
                                    Message msg3 = new Message();
                                    msg3.what = PublicData.TEXTVIEW_SUCCESS;
                                    m_eventHandler.sendMessage(msg3);




                       //     FP.DestroyEnrollHandle();
                        }
                    }.start();
                } else {
                    theMessage.setText("FP_ConnectCaptureDriver() failed!!");
                    FP.DisconnectCaptureDriver();
                    return;
                }

            }

        });
*/

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

/*  ///IVan
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
*/
/*
        buttonShow.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        buttonDisC.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

               //conn.close();
                //FP.DisconnectCaptureDriver();
                //theMessage.setText("FP_DisconnectCaptureDriver() Succeeded!!");
                //theMessage.postInvalidate();

                //TBD: check minutiae is valild ?
                //minutiaeStr = byteArrayToHex(minu_code1);
                //onbuttonclick();
                

            }
        });
*/
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
            myImage.setImageBitmap(bMap);
            bMap = null;
            System.gc();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            myImage.postInvalidate();
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

    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PublicData.TEXTVIEW_SUCCESS:
                    buttonCapture.setEnabled(true);
                    buttonEnroll.setEnabled(true);
                    buttonVerify.setEnabled(true);
                    theMessage.setText("Success.");
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FAILURE:
                    theMessage.setText("Failure.");
                    theMessage.postInvalidate();
                    buttonEnroll.setEnabled(true);
                    break;
                case PublicData.TEXTVIEW_CAPTURE_PLEASE_PRESS:
                    theMessage.setText("Capture: Press your finger");
                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS:
                    theMessage.setText("Enroll: Press your finger");
                    theMessage.postInvalidate();
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
                    theMessage.setText("Please remove your finger");
                    theMessage.postInvalidate();
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");

                    break;
                case PublicData.TEXTVIEW_PRESS_AGAIN:
                    theMessage.setText("Please press your finger again");
                    theMessage.postInvalidate();
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");
                    break;
                case PublicData.SHOW_PIC:
                    //new showPic().execute("/system/data/fp_image.bmp");
                    new showPic().execute("");
                    buttonCapture.setEnabled(true);
                    buttonEnroll.setEnabled(true);
                    buttonVerify.setEnabled(true);
                    break;
                case PublicData.SHOW_NFIQ:
                    theMessage.setText("nfiq " + FP.GetNFIQ());
                    theMessage.postInvalidate();
                    break;

            }
            super.handleMessage(msg);
        }
    }

    public void onbuttonclick()
    {
        // QR code 的內容
        String QRCodeContent = "464d5200203230000000012c00000108014400c500c501000000642d402d001092644012001495644095001c12644023001e146440da00287b64402900298a644055002d94644067002f0f64409a00300a6440b500318764401800338b6480ed003886644020003ba16440ad0040ff64404b00441464809200459564401c00472464409c0047096440a9004a86644085004e0f6440f4005ff36480ae0063066440ce006ff964805400761264408000780464409a007ffd6440850093056440c9009cf46440ca00a8f064408000af0564409400bc7b64401300bf876440bc00c17a64404700d11264802300d21a6440f200d5626440e200e1e964409f00e2fe6440dc00f7e664402000fc93648071010790644064010d08644079010f846440340111166440870129f964000000ea8a64404600ec9164403800ee176480cc00f2fc6480ab00f4066440c600f8766440a60107896440d8010e7d6440a90110fe6440aa0114846480c401168364803501209a6440c30120056440cc01247b6440ba012a82648087012c9f64806b012da26400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        // QR code 寬度
        int QRCodeWidth = 400;
        // QR code 高度
        int QRCodeHeight = 400;
        // QR code 內容編碼
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        Log.d("TEST", "len: "+QRCodeContent.length());
        MultiFormatWriter writer = new MultiFormatWriter();
        try
        {
            // 容錯率姑且可以將它想像成解析度，分為 4 級：L(7%)，M(15%)，Q(25%)，H(30%)
            // 設定 QR code 容錯率為 H
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            // 建立 QR code 的資料矩陣
            BitMatrix result = writer.encode(QRCodeContent, BarcodeFormat.QR_CODE, QRCodeWidth, QRCodeHeight, hints);
            // ZXing 還可以生成其他形式條碼，如：BarcodeFormat.CODE_39、BarcodeFormat.CODE_93、BarcodeFormat.CODE_128、BarcodeFormat.EAN_8、BarcodeFormat.EAN_13...

            //建立點陣圖
            Bitmap bitmap = Bitmap.createBitmap(QRCodeWidth, QRCodeHeight, Bitmap.Config.ARGB_8888);
            // 將 QR code 資料矩陣繪製到點陣圖上
            for (int y = 0; y<QRCodeHeight; y++)
            {
                for (int x = 0;x<QRCodeWidth; x++)
                {
                    bitmap.setPixel(x, y, result.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            //ImageView imgView = (ImageView) findViewById(R.id.imageView);
            // 設定為 QR code 影像
            //qrImage.setImageBitmap(bitmap);
            Log.d("Merge", "qrImage H:"+bitmap.getHeight()+"   w:"+bitmap.getWidth());


            // BitmapFactory.Options options = createOption(mPath);
            Bitmap source = BitmapFactory.decodeFile(mPath);

            Log.d("Merge", "source H:"+source.getHeight()+"   w:"+source.getWidth());
            //saveBitmap(source);
            Bitmap dist =mergeBitmap_LR(source, bitmap, false);
            Log.d("Merge", "dist H:"+dist.getHeight()+"   w:"+dist.getWidth());


            //Bitmap rectBmp = Bitmap.createBitmap(bitmap.getWidth(), source.getHeight(),Bitmap.Config.ARGB_8888);
           // Bitmap dist = mergeBitmap(rectBmp, bitmap);
           // Log.d("Merge", "dist H:"+dist.getHeight()+"   w:"+dist.getWidth());

            myImage.setImageBitmap(dist);

            //saveBitmap(dist);



            sharebitmap = dist;
                new Thread("saveBitmap") {
                    @Override
                    public void run() {
                        saveBitmap(sharebitmap);

                        Log.d("merge", "saveBitmap done");
                    }
                }.start();

        }
        catch (WriterException e)
        {
            // TODO Auto-generated catch block
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

        if (leftBitmap.getHeight() != height) {
            tempBitmapL = Bitmap.createScaledBitmap(leftBitmap, (int)(leftBitmap.getWidth()*1f/leftBitmap.getHeight()*height), height, false);
        } else if (rightBitmap.getHeight() != height) {
            tempBitmapR = Bitmap.createScaledBitmap(rightBitmap, (int)(rightBitmap.getWidth()*1f/rightBitmap.getHeight()*height), height, false);
        }

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
        Rect rightRectT  = new Rect(tempBitmapL.getWidth(),300 , width, 700);
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

    public void saveBitmap(Bitmap bitmap) {
        FileOutputStream fOut;
        String tmp="";
        try {

            //TBD.... FILE NAME AND FLODER...
            String subName = mPath.substring(mPath.lastIndexOf("/IMG")+4);
            Log.d("merge", "sub name: "+subName);

            String tempName = mPath.substring(mPath.lastIndexOf("/")+1);
            String dirName = mPath.substring(0, mPath.length()- tempName.length());
            dirName = dirName+"Startek/";
            Log.d("merge", "folder: "+dirName);

            //String dirName = mPath.substring(mPath.lastIndexOf("/")+1);
            File dir = new File(dirName);

            if (!dir.exists()) {
                dir.mkdir();
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
    }



}
