package com.startek.fm220;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.startek.fingerprint.library.FP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;

import static com.startek.fm220.AlbumSelectorActivity.host;
import static com.startek.fm220.PixerApi.Enroll;
import static com.startek.fm220.PixerApi.ftpPort;
import static com.startek.fm220.PixerApi.ftp_password;
import static com.startek.fm220.PixerApi.ftp_username;


/**
 * Created by ivan.lin on 2017/7/20.
 */

public class SiguUpActivity extends Activity implements TextWatcher {
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private UsbDevice d;
    private UsbInterface usbIf;
    UsbEndpoint epIN;
    UsbEndpoint epOUT;
    UsbEndpoint ep2IN;
    private UsbManager manager;
    private UsbDeviceConnection conn;
    private PendingIntent mPermissionIntent;
    private int connectrtn;
    private String TAG = "SignUpActivity";
    byte[] bMapArray = new byte[1078 + (640 * 480)];
    byte[] srno= new byte[16];
    byte[] pak = new byte[16];
    byte[] fwver = new byte[16];
    byte[] Key2= new byte[16];
    byte[] newKey= new byte[16];
    android.app.AlertDialog dialog;
    private SiguUpActivity.EventHandler m_eventHandler;
    private int rtn;
    private int rtn2;
    private byte[] minu_code1 = new byte[512];
    private byte[] minu_code2 = new byte[512];
    final int U_CLASS_A = 65;
    final int U_CLASS_B = 66;
    private  String minu_str="";
    static final int BIO_TASK_Enroll = 2;
    static final int fpIndex = 1;
    private ImageView fpImg;
    private static Context Context;
    private TextView theIndicator;
    private Button mRegisterBtn;
    private EditText mRegName;
    private EditText mRegEmail;
    private EditText mRegStaff_no;
    private EditText mReg_age;
    private EditText mRegPhone;

    private String TEMP_FILENAME_U;
    private String TEMP_FILENAME="regFile.txt";
    private MyFTPClientFunctions ftpclient = null;
    private String signData="";


    private Context cntx = null;

    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

//            if (pd != null && pd.isShowing()) {
//                pd.dismiss();
//            }
            if (msg.what == 0) {
                //getFTPFileList();
                Log.d(TAG, "upload RegFile to FTP");
                uploadRegFile();
            } else if (msg.what == 1) {
               // showCustomDialog(fileList);
            } else if (msg.what == 2) {
//                Toast.makeText(SiguUpActivity.this, "Uploaded Successfully!",
//                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "Save RegFile to FTP Done");
                    DialogUtil.showToast(SiguUpActivity.this, R.string.successful);
                    goBackActivity();

            } else if (msg.what == 3) {
                Toast.makeText(SiguUpActivity.this, "Disconnected Successfully!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SiguUpActivity.this, "Unable to Perform Action!",
                        Toast.LENGTH_LONG).show();
            }

        }

    };

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
                        mPermissionIntent = PendingIntent.getBroadcast(SiguUpActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
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

        //theMessage.setText("num of ep" + usbIf.getEndpointCount());

        epOUT = usbIf.getEndpoint(0);
        epIN = usbIf.getEndpoint(1);
        ep2IN = usbIf.getEndpoint(2);

        //	 theMessage.setText("ep num "+ ep2IN.getEndpointNumber()+"packet size "+ ep2IN.getMaxPacketSize()+"dir "+ep2IN.getDirection());
        //	 theMessage.setText("ep num "+ epIN.getEndpointNumber()+"packet size "+ epIN.getMaxPacketSize()+"dir "+epIN.getDirection());
        //theMessage.setText("ep num " + epOUT.getEndpointNumber() + "packet size " + epOUT.getMaxPacketSize() + "dir " + epOUT.getDirection());

        //	 theMessage.setText("manager.hasPermission()");
        if (manager.hasPermission(d) == false) {
            //    	 theMessage.setText("manager.hasPermission() false");
            return;

        }

        conn = manager.openDevice(d);
        if(conn == null)
        {
            Log.d("FM220", "conn  is NULL !!!");

            FP.DisconnectCaptureDriver();
            DialogUtil.showToast(SiguUpActivity.this, R.string.usb_conn_err);
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
        //theMessage.setText("fm220 fileDesc" + conn.getFileDescriptor());
        //
        try {

            if (conn.getFileDescriptor() == -1) {
                connectreader();
               // theMessage.setText("try connect without file descripter" + conn.getFileDescriptor());
                connectrtn = FP.ConnectCaptureDriver(conn, d);
                Log.d("FM220", "Fails to open DeviceConnection");
            } else {
                //theMessage.setText("try connect with file descripter" + conn.getFileDescriptor());
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
        Log.d(TAG, "FWVer:"+strFWVer);
        Log.d(TAG, "strSN:"+strSN);
        //theMessage.setText("sn: " + strSN + " pak: " + strPAK +" fw ver: " + strFWVer);
//
    }

    // --- TextWatcher start
    @Override
    public void afterTextChanged(Editable arg0) {
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void onTextChanged(CharSequence cs, int start, int before, int count) {

//        Log.d(TAG, "mRegName.length: "+mRegName.length());
//        Log.d(TAG, "mRegEmail.length: "+mRegEmail.length());
//        Log.d(TAG, "mReg_age.length: "+mReg_age.length());
//        Log.d(TAG, "mRegStaff_no.length: "+mRegStaff_no.length());
        mRegisterBtn.setEnabled(mRegName.length() > 0 && mRegEmail.length() > 0 && mReg_age.length() > 0 && mRegStaff_no.length() > 0 && mRegPhone.length()>0);

    }
    // --- TextWatcher end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        Context = getApplicationContext();
        cntx = this.getBaseContext();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        FP.SetFPLibraryPath("/data/data/com.startek.fm210/lib/");
        FP.InitialSDK();
        Log.d("FM220", "FP Library init..");

        fpImg = (ImageView)findViewById(R.id.fp_enroll);
        theIndicator = (TextView)findViewById(R.id.indicator);
        mRegisterBtn = (Button)findViewById(R.id.btn_register);

        //listen text
        mRegName = (EditText) findViewById(R.id.reg_name);
        mRegName.addTextChangedListener(this);
        mRegEmail = (EditText) findViewById(R.id.reg_email);
        mRegEmail.addTextChangedListener(this);
        mReg_age = (EditText) findViewById(R.id.reg_age);
        mReg_age.addTextChangedListener(this);
        mRegStaff_no = (EditText) findViewById(R.id.reg_staff_no);
        mRegStaff_no.addTextChangedListener(this);
        mRegPhone = (EditText) findViewById(R.id.reg_phone);
        mRegPhone.addTextChangedListener(this);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        //PendingIntent mPermissionIntent;
        for (UsbDevice mdevice : manager.getDeviceList().values()) {

            int pid, vid;

            pid = mdevice.getProductId();
            vid = mdevice.getVendorId();

            if (((pid == 0x8220) && (vid == 0x0bca)) || ((pid == 0x8225) && (vid == 0x0bca)) || ((pid == 0x8226) && (vid == 0x0bca)) || ((pid == 0x8220) && (vid == 0x0b39)) || ((pid == 0x8210) && (vid == 0x0b39))) {
                //theMessage.setText("fm220 pid found");
                d = mdevice;

                manager.requestPermission(d, mPermissionIntent);

                break;
            }
        }
    }

    private void scanFPThread()
    {

        theIndicator.setVisibility(View.VISIBLE);
        if (connectrtn == 0) {
            //buttonEnroll.setEnabled(false);
            m_eventHandler = new SiguUpActivity.EventHandler(Looper.getMainLooper());

            //let thread do main job
            new Thread() {
                public void run() {
                    super.run();
                    int i;
                    FP.CreateEnrollHandle();

                    Message msg0 = new Message();
                    msg0.what = PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS;
                    m_eventHandler.sendMessage(msg0);

                    for (i = 0; i < 6; i++) {
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
                    if(i>=5)
                        goBackActivity();

                    minu_str = byteArrayToHex(minu_code2);
                    Date date = new Date();

                   // userID = String.valueOf(date.getTime());
                    String userID = String.valueOf(date.getTime()).substring(4,10);

                   // Log.d(TAG, "minu_code2"+minu_code2);
                    Log.d(TAG,"UserID: "+userID);
                    TEMP_FILENAME_U = userID+".txt";
                    new SiguUpActivity.RequestTask(BIO_TASK_Enroll).execute(Enroll,userID,minu_str);

                }
            }.start();
        } else {
            //theMessage.setText("FP_ConnectCaptureDriver() failed!!");
            FP.DisconnectCaptureDriver();
            return;
        }

    }

    public void createRegFile() {

        try {
            Log.d("FTP", "File create: "+Environment.getExternalStorageDirectory().toString()+"/Pictures/Startek/"+TEMP_FILENAME);
            File root = new File(Environment.getExternalStorageDirectory().toString()+"/Pictures",
                    "Startek");
            if (!root.exists()) {
                root.mkdirs();
            }

            /*File gpxfile = new File(root, TEMP_FILENAME_U);

            FileWriter writer = new FileWriter(gpxfile);
            //PrintWriter pwOb = new PrintWriter(writer, false);


            signData = mRegName.getText().toString()+","+mRegStaff_no.getText().toString()+","+mRegEmail.getText().toString()+","+mReg_age.getText().toString()+","+mRegPhone.getText().toString();
            //writer.append("Hi this is a sample file to upload for android FTP client example from TheAppGuruz!");
//			pwOb.flush();
//			pwOb.close();
            writer.write(signData);

            writer.flush();
            writer.close();*/
            signData = mRegName.getText().toString()+","+mRegStaff_no.getText().toString()+","+mRegEmail.getText().toString()+","+mReg_age.getText().toString()+","+mRegPhone.getText().toString();
            Log.d(TAG, "fileData: " +signData);

            OutputStream outputStream = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/Pictures/Startek/"+TEMP_FILENAME);
            Writer outputStreamWriter = new OutputStreamWriter(outputStream);

            outputStreamWriter.write(signData);
            outputStreamWriter.close();
//            Toast.makeText(this, "Saved : " + gpxfile.getAbsolutePath(),
//                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectToFTPAddress() {

     //       final String host = edtHostName.getText().toString().trim();
    //        final String username = edtUserName.getText().toString().trim();
    //        final String password = edtPassword.getText().toString().trim();

            if (host.length() < 1) {
                Toast.makeText(SiguUpActivity.this, "Please Enter Host Address!",
                        Toast.LENGTH_LONG).show();
            } else if (ftp_username.length() < 1) {
                Toast.makeText(SiguUpActivity.this, "Please Enter User Name!",
                        Toast.LENGTH_LONG).show();
            } else if (ftp_password.length() < 1) {
                Toast.makeText(SiguUpActivity.this, "Please Enter Password!",
                        Toast.LENGTH_LONG).show();
            } else {

//                pd = ProgressDialog.show(SiguUpActivity.this, "", "wait..",
//                        true, false);

                new Thread(new Runnable() {
                    public void run() {
                        boolean status = false;
                        Log.d(TAG, "Connection to:"+host+ftp_username+ftp_password+ftpPort);
                         status = ftpclient.ftpConnect(host, ftp_username, ftp_password, ftpPort);

                        if (status == true) {
                            Log.d(TAG, "Connection Success");
                            handler.sendEmptyMessage(0);
                        } else {
                            Log.d(TAG, "Connection failed");
                            handler.sendEmptyMessage(-1);
                        }
                    }
                }).start();
            }
    }

    private void uploadRegFile()
    {
//        pd = ProgressDialog.show(MainActivity.this, "", "Uploading...",
//                true, false);
        new Thread(new Runnable() {
            public void run() {
                boolean status = false;
                status = ftpclient.ftpUpload(
                        Environment.getExternalStorageDirectory()
                                + "/Pictures/Startek/" + TEMP_FILENAME,
                        TEMP_FILENAME_U, "/", cntx);
                if (status == true) {
                    Log.d(TAG, "Upload success");
                    handler.sendEmptyMessage(2);
                } else {
                    Log.d(TAG, "Upload failed");
                    handler.sendEmptyMessage(-1);
                }
            }
        }).start();
    }

    private  void SignData2FTP()
    {
        createRegFile();

        connectToFTPAddress();
    }

    private  void Identify_Done(String message) throws JSONException {
        //String message =json.getString("Message");
        JSONObject json = new JSONObject(message);
        int code = json.getInt("code");

        if (code == 200) {
            Log.d(TAG, "Server Enroll Done");
            theIndicator.setVisibility(View.INVISIBLE);

            ftpclient = new MyFTPClientFunctions();

            if (isOnline(SiguUpActivity.this)) {
                //Log.d(TAG,"addr: "+edtHostName.getText());
                SignData2FTP();

            } else {
                Toast.makeText(SiguUpActivity.this,
                        "Please check your internet connection!",
                        Toast.LENGTH_LONG).show();
            }

        } else if (code == 105) {
            // goNextView(R.id.activate_scene);
        } else {
            Log.d(TAG,"ERROR ERROR ");
            // DialogUtil.showToast(this, R.string.general_request_error, code, message);
        }

    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void goBackActivity() {

        Intent intent = new Intent(SiguUpActivity.this, AlbumSelectorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //startActivity(new Intent(getApplicationContext(), AlbumSelectorActivity.class));
        startActivity(intent);
        finish();
    }

    public void onRegisterClick(View v) {
        if (!NetUtils.isNetworkAvailable(getApplicationContext())) {
            DialogUtil.showToast(this, R.string.connect_err);
            return;
        }

        Log.d(TAG, "fileData: " + mRegName.getText().toString() + "," + mRegStaff_no.getText().toString() + "," + mRegEmail.getText().toString() + "," + mReg_age.getText().toString() + "," + mRegPhone.getText().toString());

        //TB recover
        scanFPThread();
//        TEMP_FILENAME_U = "1234.txt";
  //      createRegFile();


//        minu_str = "123456789000000";
//        userID = "987654";
//        new SiguUpActivity.RequestTask(BIO_TASK_Enroll).execute(Enroll,userID,minu_str);
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

    public static String byteArrayToHex(byte[] b)
    {
        String result ="";
        for (int i=0 ; i<b.length ; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
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
                   // theMessage.setText("Success.");
                    //theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FAILURE:
//                    theMessage.setText("Failure.");
//                    theMessage.postInvalidate();
//                    buttonEnroll.setEnabled(true);
                    break;
                case PublicData.TEXTVIEW_CAPTURE_PLEASE_PRESS:
                    //theMessage.setText("Capture: Press your finger");
                    //theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_ENROLL_PLEASE_PRESS:
//                    theIndicator.setText("Enroll: Press your finger");
//                    theIndicator.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_VERIFY_PLEASE_PRESS:
//                    theMessage.setText("Verify: Press your finger");
//                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_SCORE:
//                    theMessage.setText("matching score=" + (int) FP.Score());
//                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FILE_EXIST:
//                    theMessage.setText("Verify: File exist");
//                    theMessage.postInvalidate();
                    break;
                case PublicData.TEXTVIEW_FILE_NOT_EXIST:
//                    theMessage.setText("File not exist, please enroll first");
//                    theMessage.postInvalidate();
//                    buttonVerify.setEnabled(true);
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
//                    theMessage.setText("nfiq " + FP.GetNFIQ());
//                    theMessage.postInvalidate();
                    break;

            }
            super.handleMessage(msg);
        }
    }

    private class RequestTask extends PixerApi.NetworkOperation {
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
                case BIO_TASK_Enroll:
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
