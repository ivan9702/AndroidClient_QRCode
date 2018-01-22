package com.startek.fm220;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import static com.startek.fm220.PixerApi.Identify;

import static com.startek.fm220.PixerApi.default_ftpPort;
import static com.startek.fm220.PixerApi.ftp_password;
import static com.startek.fm220.PixerApi.ftp_username;
import static com.startek.fm220.PixerApi.getftpPort;
import static com.startek.fm220.PixerApi.setftpPort;
import static com.startek.fm220.fpScanActivity.BIO_TASK_Identify;
import static com.startek.fm220.fpScanActivity.Bio_Identify_Level;


public class AlbumSelectorActivity extends AppCompatActivity {
    private static final String TAG = "AlbumSelectorActivity";




    private static final int EVENT_GUEST_TIMEOUT = 104;
    public  static String host="";
    public  static String port="";
    private GridView mAlbumsGirdView;
    private AlbumsAdapter mAlbumsAdapter = null;
    PhotoUpAlbumHelper mPhotoUpAlbumHelper = null;
    public List<PhotoUpImageBucket> mList;

    private Uri mImgUri;
    private int mTutorial = 0;
    String targetPath = null;

    String imgName = "";
    String imgName_Rotate = "";
    private MyFTPClientFunctions ftpclient = null;
    private String TEMP_FILENAME_D;
    private String host_FILENAME= "host.txt";
    private String regFileNAME="reg.txt";
    private String portFileNAME="port.txt";
    final String srcFilePath =Environment.getExternalStorageDirectory().toString() + "/Pictures/Startek/";
    private String[] userdata;

    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

//            if (pd != null && pd.isShowing()) {
//                pd.dismiss();
//            }
            if (msg.what == 0) {
                //getFTPFileList();
                Log.d(TAG, "upload RegFile to FTP");
               //uploadRegFile();
            } else if (msg.what == 1) {
                // showCustomDialog(fileList);
                downloadRegFile();
            } else if (msg.what == 2) {
//                Toast.makeText(SiguUpActivity.this, "Uploaded Successfully!",
//                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "Save RegFile to FTP Done");
                DialogUtil.showToast(AlbumSelectorActivity.this, R.string.successful);
                //goBackActivity();

            } else if (msg.what == 3) {
                Toast.makeText(AlbumSelectorActivity.this, "Disconnected Successfully!",
                        Toast.LENGTH_LONG).show();
            }  else if (msg.what == 4) {
                Log.d(TAG, "Read RegFile to show..");
                File regFile = new File(srcFilePath+regFileNAME);

                try {
                    BufferedReader br = new BufferedReader(new FileReader(srcFilePath+regFileNAME));

                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        //sb.append("\n");
                        line = br.readLine();
                    }

                    Log.d(TAG, "user data: "+sb.toString());

                    br.close();
             /*       String phoneNumber = sb.substring(sb.lastIndexOf(",")+1);
                  //  Log.d(TAG, "phoneNum: "+phoneNumber);
                    String temp = sb.substring(0, sb.length()- phoneNumber.length()-1);
                   // Log.d(TAG, "temp len:"+temp.length());
                    String age = temp.substring(temp.lastIndexOf(",")+1);
                  //  Log.d(TAG, "age: "+age);

                    temp = temp.substring(0, temp.length()- age.length()-1);
                  //  Log.d(TAG, "temp len:"+temp.length());
                    String email = temp.substring(temp.lastIndexOf(",")+1);
                  //  Log.d(TAG, "email: "+email);

                    temp = temp.substring(0, temp.length()- email.length()-1);
                 //   Log.d(TAG, "temp len:"+temp.length());
                    String usrId = temp.substring(temp.lastIndexOf(",")+1);
                //    Log.d(TAG, "usrID: "+usrId);

                    temp = temp.substring(0, temp.length()- usrId.length()-1);
                 //   Log.d(TAG, "temp len:"+temp.length());
                    String userName = temp.substring(temp.lastIndexOf(",")+1);
                    Log.d(TAG, "userName: "+userName);

                    Intent intent = new Intent(AlbumSelectorActivity.this, ShowRegisterInfo.class);
                    intent.putExtra("userName", userName);
                    intent.putExtra("userID", usrId);
                    intent.putExtra("email", email);
                    intent.putExtra("age",age);
                    intent.putExtra("phoneNum",phoneNumber);
                    startActivity(intent);
                    */

             StringTokenizer st = new StringTokenizer(sb.toString(), ",");
                    Intent intent = new Intent(AlbumSelectorActivity.this, ShowRegisterInfo.class);
                    int i=0;
                    while(st.hasMoreTokens()){
                        //Log.d(TAG,"st "+i+":");
                        if(i==0)
                            intent.putExtra("userName", st.nextToken().toString());
                        if(i==1)
                            intent.putExtra("userID", st.nextToken().toString());
                        if(i==2)
                            intent.putExtra("email", st.nextToken().toString());
                        if(i==3)
                            intent.putExtra("age",st.nextToken().toString());
                        if(i==4)
                            intent.putExtra("phoneNum",st.nextToken().toString());
                        i++;
                    }
                    startActivity(intent);

                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }else if (msg.what == -2){
                Toast.makeText(AlbumSelectorActivity.this, "Unable to get RegFile!",
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(AlbumSelectorActivity.this, "Unable to Connect to FTP!",
                        Toast.LENGTH_LONG).show();
            }

        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.hostprompts, null);
        AlertDialog.Builder alertDialogBuilder;
        TextView ipInput;
        switch (item.getItemId()) {
            case R.id.new_game:
                Toast.makeText(AlbumSelectorActivity.this, "FM220 init..., please wait a moment！",
                        Toast.LENGTH_LONG).setGravity(Gravity.CENTER,0,0);

                Intent intent = new Intent(AlbumSelectorActivity.this, SiguUpActivity.class);
                startActivity(intent);
                //newGame();
                return true;
            case R.id.new_ip:
                // get prompts.xml view
//                LayoutInflater li = LayoutInflater.from(this);
//                View promptsView = li.inflate(R.layout.hostprompts, null);

                alertDialogBuilder = new AlertDialog.Builder(
                        this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);
                ipInput = (TextView)promptsView.findViewById(R.id.textView1);
                ipInput.setText(R.string.ip_Input);

                userInput.setHint(host.toString());
                Log.d(TAG, "hint: "+host.toString());
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        Log.d(TAG, "ip: "+userInput.getText().toString());

                                        ///////////////////////
                                        BufferedWriter bufferedWriter = null;
                                        try {
                                            // String strContent = "This example shows how to write string content to a file";
                                            File myFile = new File(srcFilePath+host_FILENAME);
                                            // check if file exist, otherwise create the file before writing
                                            if (!myFile.exists()) {
                                                myFile.createNewFile();
                                            }
                                            Writer writer = new FileWriter(myFile);
                                            bufferedWriter = new BufferedWriter(writer);
                                            bufferedWriter.write(userInput.getText().toString());
                                            host = userInput.getText().toString();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } finally{
                                            try{
                                                if(bufferedWriter != null) bufferedWriter.close();
                                            } catch(Exception ex){

                                            }
                                        }

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                       // DialogUtil.showToast(AlbumSelectorActivity.this, R.string.ftp_fail);
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                //newGame();
                return true;

            case R.id.new_port:
                // get prompts.xml view
//                LayoutInflater li = LayoutInflater.from(this);
//                View promptsView = li.inflate(R.layout.hostprompts, null);

                alertDialogBuilder = new AlertDialog.Builder(
                        this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput2 = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);
                ipInput = (TextView)promptsView.findViewById(R.id.textView1);
                ipInput.setText(R.string.port_Input);
               userInput2.setHint(Integer.toString(getftpPort()));
               Log.d(TAG, "hint: "+getftpPort());
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        Log.d(TAG, "port: "+userInput2.getText().toString());

                                        ///////////////////////
                                        BufferedWriter bufferedWriter = null;
                                        try {
                                            // String strContent = "This example shows how to write string content to a file";
                                            File myFile = new File(srcFilePath+portFileNAME);
                                            // check if file exist, otherwise create the file before writing
                                            if (!myFile.exists()) {
                                                myFile.createNewFile();
                                            }
                                            Writer writer = new FileWriter(myFile);
                                            bufferedWriter = new BufferedWriter(writer);
                                           // if(userInput2.getText().equals(""))
                                            //    return;
                                            Log.d(TAG, "input:"+userInput2.getText().toString());
                                            bufferedWriter.write(userInput2.getText().toString());
                                            //host = userInput2.getText().toString();
                                            setftpPort(Integer.valueOf("0"+userInput2.getText().toString()));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } finally{
                                            try{
                                                if(bufferedWriter != null) bufferedWriter.close();
                                            } catch(Exception ex){

                                            }
                                        }

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                        // DialogUtil.showToast(AlbumSelectorActivity.this, R.string.ftp_fail);
                                    }
                                });

                // create alert dialog
                 alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                //newGame();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(AlbumSelectorActivity.this, "横屏模式", 3000).show();
        } else {
            //Toast.makeText(AlbumSelectorActivity.this, "横屏模式", 3000).show();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(mImgUri != null)
        // Save the user's current state
        savedInstanceState.putString("media_url", mImgUri.toString());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  if (mImgUri != null) {
      //      mImgUri = Uri.parse(savedInstanceState.getString("media_url"));
     //   }

        setContentView(R.layout.activity_album_selector);

        mAlbumsGirdView = (GridView) findViewById(R.id.albums);

        mAlbumsAdapter = new AlbumsAdapter(getApplicationContext());

        mAlbumsGirdView.setAdapter(mAlbumsAdapter);


        mAlbumsGirdView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                    Intent intent = new Intent(AlbumSelectorActivity.this, GridViewActivity.class);
                    intent.putExtra("imagelist", mList.get(position));
                   // intent.putExtra("frameList", mSelectedMacAddressList.toArray(new String[mSelectedMacAddressList.size()]));
                    startActivity(intent);

            }
        });



        File root = new File(srcFilePath);
        if (!root.exists())
        {
                root.mkdir();
                MediaScannerConnection.scanFile(getApplicationContext(),
                        new String[]{root.getAbsolutePath()},
                        null,
                        null);
        }
        //Read host.txt
        File hostFile = new File(srcFilePath+host_FILENAME);
        if(!hostFile.exists())
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
            ipInput.setText(R.string.ip_Input);
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // get user input and set it to result
                                    // edit text
                                   Log.d(TAG, "ip: "+userInput.getText().toString());

                                    ///////////////////////
                                    BufferedWriter bufferedWriter = null;
                                    try {
                                       // String strContent = "This example shows how to write string content to a file";
                                        File myFile = new File(srcFilePath+host_FILENAME);
                                        // check if file exist, otherwise create the file before writing
                                        if (!myFile.exists()) {
                                            myFile.createNewFile();
                                        }
                                        Writer writer = new FileWriter(myFile);
                                        bufferedWriter = new BufferedWriter(writer);
                                        bufferedWriter.write(userInput.getText().toString());
                                        host = userInput.getText().toString();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally{
                                        try{
                                            if(bufferedWriter != null) bufferedWriter.close();
                                        } catch(Exception ex){

                                        }
                                    }

                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                    DialogUtil.showToast(AlbumSelectorActivity.this, R.string.ftp_fail);
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

        }
        else
        {

            try {
            BufferedReader br = new BufferedReader(new FileReader(srcFilePath+host_FILENAME));

                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    //sb.append("\n");
                    line = br.readLine();
                }

               // Log.d(TAG, "ip:port "+sb.length());
                host = sb.toString();
                br.close();

            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }

        // Read port.txt
        File portFile = new File(srcFilePath+portFileNAME);
        if(!portFile.exists())
        {
           setftpPort(default_ftpPort);
        }
        else
        {

            try {
                BufferedReader br = new BufferedReader(new FileReader(srcFilePath+portFileNAME));

                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    //sb.append("\n");
                    line = br.readLine();
                }

                // Log.d(TAG, "ip:port "+sb.length());
                port = sb.toString();
                br.close();

                setftpPort(Integer.valueOf(port));

            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        Log.d(TAG, "ip: "+host);
        loadData();

    }

    @Override
    protected void onResume() {
        super.onResume();

        getContentResolver().registerContentObserver(MediaStore.getMediaScannerUri(), true, mContentObserver);

    }

    @Override
    protected void onPause() {
        super.onPause();

        getContentResolver().unregisterContentObserver(mContentObserver);

        mHandler.removeMessages(EVENT_GUEST_TIMEOUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPhotoUpAlbumHelper != null) {
            mPhotoUpAlbumHelper.setGetAlbumList(null);
            mPhotoUpAlbumHelper = null;
        }
    }



    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            loadData();
        }
    };



    public void onBackClick(View view) {
        finish();
    }

    public void loadData() {
        mPhotoUpAlbumHelper = PhotoUpAlbumHelper.getHelper();
        mPhotoUpAlbumHelper.init(this);
        mPhotoUpAlbumHelper.setGetAlbumList(new PhotoUpAlbumHelper.GetAlbumList() {
            @Override
            public void getAlbumList(List<PhotoUpImageBucket> list) {
                mAlbumsAdapter.setArrayList(list);
                mList = list;
                mAlbumsAdapter.notifyDataSetChanged();
            }
        });
        mPhotoUpAlbumHelper.execute(false);
    }


    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        WeakReference<AlbumSelectorActivity> mActivity;

        MyHandler(AlbumSelectorActivity a) {
            mActivity = new WeakReference<AlbumSelectorActivity>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_GUEST_TIMEOUT:
                    mActivity.get().finish();
                    break;
            }
        }
    }

    public void onCameraClick(View v) {
        String dir = Environment.getExternalStorageDirectory().toString() + "/Pictures";

        Date date = new Date();
        imgName = date.getTime() + ".jpg";
        imgName_Rotate = imgName.toString();
        File img = new File(dir, imgName);


        Log.d(TAG, "img"+img);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, img.getPath());

        mImgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Log.d(TAG, "insert uri=" + mImgUri);

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, mImgUri);
        startActivityForResult(i, 10);
    }

    public void onScannerClick(View v) {
        IntentIntegrator integrator = new IntentIntegrator(AlbumSelectorActivity.this);
        integrator.setCaptureActivity(QrcodeScanActivity.class);
        integrator.setPrompt("Scanning"); //底部的提示文字，设为""可以置空
        integrator.setCameraId(0); //前置或者后置摄像头
        integrator.setBeepEnabled(false); //扫描成功的「哔哔」声，默认开启
        integrator.initiateScan();

    }

    public void onSignUpClick(View V){
        Toast.makeText(AlbumSelectorActivity.this, "FM220 init..., please wait a moment！",
                Toast.LENGTH_LONG).setGravity(Gravity.CENTER,0,0);

        Intent intent = new Intent(AlbumSelectorActivity.this, SiguUpActivity.class);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + requestCode + ", " + resultCode);
        //Log.d(TAG, "data =" + data);


        if (requestCode == 10) {
            ContentResolver cr = getContentResolver();
            String id="";
            if(mImgUri != null)
                    id = mImgUri.toString().substring(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString().length() + 1);
            else {

                Log.d(TAG, "mImgUri = null.. get it again~");
                mImgUri = data.getData();
                Log.d(TAG, "Media Uri: " + mImgUri);
                String dir = Environment.getExternalStorageDirectory().toString() + "/Pictures";

                File img = new File(dir, imgName_Rotate);
                Log.d(TAG, "img: "+img);
                Log.d(TAG, "imgName: "+imgName_Rotate);
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, img.getPath());

                mImgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                id = mImgUri.toString().substring(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString().length() + 1);

            }
            Log.d(TAG, "mImgUri id: " +id);

            Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , null
                    , MediaStore.Images.Media._ID + ">=" + id
                    , null
                    , null);

            if (cursor == null) {
                return;
            }

            ArrayList<String> deleteIds = new ArrayList<>();
            ArrayList<String> deleteFiles = new ArrayList<>();

            cursor.moveToFirst();
            int fixOrientationExif = ExifInterface.ORIENTATION_NORMAL;
            int fixOrientationDegree = 0;
            int targetId = Integer.valueOf(id);
            int lastId = targetId;

            int targetDegree = 0;
            int targetOrientationExif = ExifInterface.ORIENTATION_NORMAL;

            do {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int _id = cursor.getInt(idIndex);

                int pathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                String imgPath = cursor.getString(pathIndex);

                int orientationIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
                int degree = cursor.getInt(orientationIndex);

                int orientation = ExifInterface.ORIENTATION_UNDEFINED;
                try {
                    ExifInterface exif = new ExifInterface(imgPath);
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                } catch (Exception e) {
                    Log.e(TAG, "ExifInterface:" + e);
                }

                Log.d(TAG, "_id=" + _id + ", lastId="+lastId+ ",targetDegree=" + targetDegree + ", degree=" + degree + ", orientation=" + orientation+ ", fixOrientationDegree="+fixOrientationDegree);


                if (_id > lastId) {
                    lastId = _id;
                    fixOrientationExif = orientation;
                    fixOrientationDegree = degree;

                    deleteIds.add(String.valueOf(_id));
                    deleteFiles.add(imgPath);
                } else if (_id == targetId) {
                    targetPath = imgPath;
                    targetDegree = degree;
                    targetOrientationExif = orientation;
                }

            } while (cursor.moveToNext());

            cursor.close();

            for (String f : deleteFiles) {
                Log.d(TAG, "delete " + f);
                new File(f).delete();
            }

            // fail to construct args.
            //if (deleteIds.length() > 0) {
            //    Log.d(TAG, "delete rows:" + deleteIds);
            //    try {
            //        cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            //                , MediaStore.Images.Media._ID + "=?"
            //                , new String[]{"(" + deleteIds + ")"});
            //    } catch (Exception e) {
            //        Log.d(TAG, "delete row", e);
            //    }
            // }

            for (String row : deleteIds) {
                Log.d(TAG, "delete row " + row);
                cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        , MediaStore.Images.Media._ID + "=" + row
                        , null);
            }
            Log.d(TAG, "targetPath =" + targetPath+" ,resultCode="+resultCode);
            if (targetPath != null && resultCode == RESULT_OK) {
                Date date = new Date();
                long timeStamp = date.getTime();
                String timeString = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
                String newName = "IMG_" + timeString + ".jpg";
                Log.d(TAG, "targetPath =" + targetPath);
                File img = new File(targetPath);
                File newImg = new File(img.getParent(), newName);

                if (img.renameTo(newImg)) {
                    targetPath = newImg.getPath();
                    Log.d(TAG, "mImgUri renameTo=" + targetPath);

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, newImg.getPath());
                    values.put(MediaStore.MediaColumns.TITLE, newName.split("\\.")[0]);
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, newName);
                    values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, timeStamp);
                    values.put(MediaStore.Images.ImageColumns.DATE_ADDED, timeStamp / 1000);
                    values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, timeStamp / 1000);

                    if (targetDegree != fixOrientationDegree) {
                        Log.d(TAG, "update rotation " + targetDegree + " -> " + fixOrientationDegree);
                        values.put(MediaStore.Images.ImageColumns.ORIENTATION, fixOrientationDegree);
                    }
                    Log.d(TAG,   "targetDegree=" + targetDegree + " fixOrientationDegree="+fixOrientationDegree);

                    cr.update(mImgUri, values, null, null);

                    if (targetOrientationExif != fixOrientationExif) {
                        Log.d(TAG, "update exif " + targetOrientationExif + " -> " + fixOrientationExif);

                        try {
                            ExifInterface exif = new ExifInterface(targetPath);
                           // exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + fixOrientationExif);

                        } catch (IOException e) {
                            Log.d(TAG, "update exif ", e);
                        }
                    }
                    Toast.makeText(AlbumSelectorActivity.this, "FM220 init ...",
                            Toast.LENGTH_SHORT).show();

                    // startActivity(new Intent(this, EditActivity.class).putExtra("selectIma", targetPath));

                    //Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + targetPath));
                    //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //sendBroadcast(intent);

                    // Try to update MTP
                    MediaScannerConnection.scanFile(getApplicationContext(),
                            new String[]{newImg.getAbsolutePath()},
                            null,
                            null);
                }
            } else {
                cr.delete(mImgUri, null, null);

                if (targetPath != null) {
                    new File(targetPath).delete();
                }
            }
            Log.d("FM220", "Capture finish! lanuch FM220... " );

            Intent intent = new Intent(AlbumSelectorActivity.this,fpScanActivity.class);
            //Intent intent = new Intent(AlbumSelectorActivity.this,tstlib.class);
            intent.putExtra("selectIma", targetPath);
            startActivity(intent);

        }else{
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result!=null){
                String scanContent = result.getContents();
                String scanFormat = result.getFormatName();
//                Log.d(TAG, "QRcode content:"+scanContent);
//                Log.d(TAG, "QRcode format:"+""+scanFormat);
                //txt_url.setText(scanFormat+" \n"+scanContent);
                Toast.makeText(getApplicationContext(), R.string.search, Toast.LENGTH_SHORT).show();
                if(scanContent.length()==6)
                {
                    TEMP_FILENAME_D = scanContent.toString()+".txt";
                    Log.d(TAG, "scan ID:"+TEMP_FILENAME_D);
                    ftpclient = new MyFTPClientFunctions();
                    getRegDatafromFTP();
                }
                else
                    new RequestTask(BIO_TASK_Identify).execute(Identify,scanContent);
            }else{
                Toast.makeText(getApplicationContext(), R.string.scan_fail, Toast.LENGTH_LONG).show();
            }
        }

    }

    private void downloadRegFile()
    {
        new Thread(new Runnable() {
            public void run() {
                boolean status = false;
               // TEMP_FILENAME_D = "888002"+".txt";
                status = ftpclient.ftpDownload(TEMP_FILENAME_D, srcFilePath + regFileNAME);
                if (status == true) {
                    Log.d(TAG, "Download success");
                    handler.sendEmptyMessage(4);
                } else {
                    Log.d(TAG, "Downlaod failed");
                    handler.sendEmptyMessage(-2);
                }
            }
        }).start();
    }

    private void connectToFTPAddress() {

        //       final String host = edtHostName.getText().toString().trim();
        //        final String username = edtUserName.getText().toString().trim();
        //        final String password = edtPassword.getText().toString().trim();

        if (host.length() < 1) {
            Toast.makeText(AlbumSelectorActivity.this, "Please Enter Host Address!",
                    Toast.LENGTH_LONG).show();
        } else if (ftp_username.length() < 1) {
            Toast.makeText(AlbumSelectorActivity.this, "Please Enter User Name!",
                    Toast.LENGTH_LONG).show();
        } else if (ftp_password.length() < 1) {
            Toast.makeText(AlbumSelectorActivity.this, "Please Enter Password!",
                    Toast.LENGTH_LONG).show();
        } else {

//                pd = ProgressDialog.show(SiguUpActivity.this, "", "wait..",
//                        true, false);

            new Thread(new Runnable() {
                public void run() {
                    boolean status = false;
                    Log.d(TAG, "Connection to:"+host+ftp_username+ftp_password+getftpPort());
                    status = ftpclient.ftpConnect(host, ftp_username, ftp_password, getftpPort());

                    if (status == true) {
                        Log.d(TAG, "Connection Success");
                        handler.sendEmptyMessage(1);
                    } else {
                        Log.d(TAG, "Connection failed");
                        handler.sendEmptyMessage(-1);
                    }
                }
            }).start();
        }
    }

    private  void getRegDatafromFTP()
    {
        connectToFTPAddress();

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

            if(Integer.parseInt(score) <= Bio_Identify_Level ){
                Toast.makeText(this, R.string.Identify_err, Toast.LENGTH_LONG).show();

            }else
            {
                Log.d(TAG, "Get minutiae Done");
                TEMP_FILENAME_D = userID+".txt";
                ftpclient = new MyFTPClientFunctions();
                getRegDatafromFTP();
            }
        } else if (code == 105) {
            // goNextView(R.id.activate_scene);
        } else {
            Log.d(TAG,"ERROR ERROR ");
             DialogUtil.showToast(this, R.string.general_request_error, code, message);
        }

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
