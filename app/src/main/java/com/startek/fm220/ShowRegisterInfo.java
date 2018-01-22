package com.startek.fm220;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/*
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

//
//  Created by ivan.lin on 2017/7/25.
//

public class ShowRegisterInfo extends Activity {
    private TextView userName;
    private TextView userID;
    private TextView email;
    private TextView userAge;
    private TextView userPhone;
    private String TAG = "ShowRegisterInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showregisterinfo);

        userName = (TextView)findViewById(R.id.show_name);
        userID = (TextView)findViewById(R.id.show_staff_no);
        email = (TextView)findViewById(R.id.show_email);
        userAge = (TextView)findViewById(R.id.show_age);
        userPhone = (TextView)findViewById(R.id.show_phone);

        setContentView(R.layout.activity_showregisterinfo);
        getIntent().getStringExtra("userName");
        getIntent().getStringExtra("userID");
        getIntent().getStringExtra("email");
        getIntent().getStringExtra("age");
        getIntent().getStringExtra("phoneNum");

        Log.d(TAG, "Name: " +getIntent().getStringExtra("userName"));
        userName.setText(getIntent().getStringExtra("userName"));
        userName.postInvalidate();
        userID.setText("124");
        userID.postInvalidate();
    }



}
*/
public class ShowRegisterInfo extends Activity {
    private String TAG = "AboutusActivity";
    private TextView userName;
    private TextView userID;
    private TextView email;
    private TextView userAge;
    private TextView userPhone;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private ImageButton backButton;
    //public static final String version = BuildConfig.VERSION_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showregisterinfo);


        userName = (TextView) findViewById(R.id.show_name);
        userName.setText( getIntent().getStringExtra("userName"));
        userID = (TextView) findViewById(R.id.show_staff_no);
        userID.setText( getIntent().getStringExtra("userID"));
        email = (TextView) findViewById(R.id.show_email);
        email.setText( getIntent().getStringExtra("email"));
        userAge = (TextView) findViewById(R.id.show_age);
        userAge.setText( getIntent().getStringExtra("age"));
        userPhone = (TextView) findViewById(R.id.show_phone);
        userPhone.setText( getIntent().getStringExtra("phoneNum"));

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }
    public void onOKClick(View v){
        super.onBackPressed();
    }

}
