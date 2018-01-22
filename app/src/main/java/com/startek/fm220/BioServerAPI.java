package com.startek.fm220;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ivan.lin on 2017/7/17.
 */

public class BioServerAPI {
    private static String TAG = "BioServerApi";

    public class NetworkOperation extends AsyncTask<String, Void, String> {

        String urlParameters  = "param1=data1&param2=data2&param3=data3";
        List<NameValuePair> uu = new ArrayList<NameValuePair>();



        @Override
        protected String doInBackground(String... params) {

            uu.add(new BasicNameValuePair("firstParam",  "111111111111"));
            uu.add(new BasicNameValuePair("secondParam", "222222222222"));

            String result = doNetworkOperation("minutiae", "9999999999999999999999999999");

            // more task if you have
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

        }


    }

    protected String doNetworkOperation(String dataUrl, String dataUrlParameters) {
        URL url;
        HttpURLConnection connection = null;
        String responseStr = "";
        try {
            // Create connection
            url = new URL(dataUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length","" + Integer.toString(dataUrlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            // Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(dataUrlParameters);
            wr.flush();
            wr.close();
            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            responseStr = response.toString();
            Log.d("Server response",responseStr);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
        return responseStr;
    }
}
