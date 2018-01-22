package com.startek.fm220;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.net.URL;

public class AlertDialog extends AsyncTask<URL, Integer, String> {

    private Context mContext;
    private ProgressDialog mDialog;

    public AlertDialog(Context mContext) {
        this.mContext = mContext;
    }

    protected void onPreExecute() {

        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.show();
    }

    protected String doInBackground(URL... urls) {
        // TODO Auto-generated method stub
        int progress =0;
        while(progress<=100){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            publishProgress(Integer.valueOf(progress));
            progress++;
        }

        return "ok";
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        // TODO Auto-generated method stub
        mDialog.setProgress(progress[0]);
    }

    protected void onPostExecute(String result) {
        if(result.equals("ok")){
            mDialog.dismiss();
        }
    }

}