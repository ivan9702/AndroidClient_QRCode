package com.startek.fm220;


import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Created by valkyrie.liu on 2016/5/24.
 */
public class PixerApi {
    private static String TAG = "PixerApi";

    static Preferences pref;

    // ordered list
    public static final String AVAILABLE_LANG_LIST[] = {
            "en-us",
            "ja-jp",
            "ru-ru",
            "zh-ch", "zh-tw"
    };

    public static String DEVICE_ALL_STATUS ="/Device/AllStatus";
    public static String DEVICE_STATUS ="/Device/Status";
    public static String DEVICE_RESET ="/Device/Reset";
    public static String DEVICE_SHARE_FRIEND = "/Device/Share/Friend";
    public static String DEVICE_SHARE_MULTIFRIEND = "/Device/Share/MultiFriend";
    public static String DEVICE_SHARE_LIST = "/Device/ShareList";
    public static String DEVICE_SHARE_PERMISSION = "/Device/Share/Permission";
    public static String DEVICE_UPDATE_NAME = "/Device/UpdateName";
    public static String DEVICE_UPDATE_FAVORITE = "/Device/UpdateFavorite";
    public static String DEVICE_UPDATE_RESUME = "/Device/UpdateEnableResume";
    public static String DEVICE_UPDATE_RESUMELOCK = "/Device/UpdateEnableResumeLock";

    public static String MEMBER_SELECT_INFO = "/Member/SelectInfo";
    public static String MEMBER_REGISTER = "/Member/Regist/Email";
    public static String MEMBER_REGISTER_RESEND = "/Member/Regist/Resend";
    public static String MEMBER_LOGIN = "/Member/Login/Email";
    public static String MEMBER_CHANGE_PASSWORD = "/Member/ChangePassword";
    public static String MEMBER_FORGOT_PASSWORD = "/Member/ForgotPassword";
    public static String MEMBER_VERIFY_CHANGE_PASSWORD = "/Member/VerifyChangePassword";
    public static String MEMBER_SEARCH = "/Member/Search";
    public static String MEMBER_CHECKZONE = "/Member/CheckZone";
    public static String MEMBER_INVITATION = "/Member/Invitation";
    public static String MEMBER_LOGOUT = "/Member/Logout";
    public static String MEMBER_UPLOAD_PERSON_HEAD_IMAGE = "/Member/UploadPersonHeadImage";
    public static String MEMBER_UPLOAD_PHOTO_FRAME_HEAD_IMAGE = "/Member/UploadPhotoFrameHeadImage";

    public static String Update_Device_Token = "/Member/UpdateDeviceToken";
    public static String MEMBER_UPDATE_INFO = "/Member/UpdateInfo";
    public static String MEMBER_LOGIN_FACEBOOK = "/Member/Login/Facebook";
    public static String MEMBER_LOGIN_WECHAT = "/Member/Login/WeChat";
    public static String MEMBER_LOGIN_WEIBO = "/Member/Login/WeiBo";
    public static String MEMBER_REGISTER_FACEBOOK = "/Member/Regist/Facebook";
    public static String MEMBER_REGISTER_WECHAT = "/Member/Regist/WeChat";
    public static String MEMBER_REGISTER_WEIBO = "/Member/Regist/WeiBo";
    public static String FRIEND_INVITE = "/Friend/Invite";
    public static String FRIEND_ASK_JOIN = "/Friend/AskJoin";
    public static String FRIEND_DELETE = "/Friend/Delete";
    public static String FRIEND_LIST = "/Friend/SelectList";

    public static final String NOTIFICATION_ALL = "/Notification/SelectAll";
    public static final String NOTIFICATION_REPLY = "/Notification/Reply";
    public static final String NOTIFICATION_CLEAN = "/Notification/Clean";

    public static final int NOTIFICATION_CAT_SELF = 100;
    public static final int NOTIFICATION_CAT_FRIEND = 101;
    public static final int NOTIFICATION_CAT_FAVORITE = 102;


    public static final String VERIFY_EMAIL = "/Verify/Email";

    public static final String VERIFY_VERSION = "/Member/AppVersion/Check";

    public static String SERVER_URL;

    static {
//        if (BuildConfig.FLAVOR.equals("production")) {
//            SERVER_URL = "https://smapi.gplustore.cn/index.php";
//        } else {
//            SERVER_URL = "https://dev-smapi.gplustore.cn/index.php";
//        }
    }

    private static final String FIELD_ACCESS_TOKEN = "AccessToken";
    private static final String FIELD_LOGIN_TYPE = "LoginType";
    private static final String FIELD_USER_ID = "UserId";
    private static final String FIELD_REGISTER_ACCOUNT = "Account";
    private static final String FIELD_REGISTER_PASSWORD = "Password";
    private static final String FIELD_NEW_PASSWORD = "NewPassword";
    private static final String FIELD_REGISTER_NAME = "Name";
    private static final String FIELD_LANGUAGE = "Language";
    private static final String FIELD_FBID = "FBId";
    private static final String FIELD_FBTOKEN = "FBToken";
    private static final String FIELD_WXOPENID = "WeChatOpenId";
    private static final String FIELD_WXTOKEN = "WeChatToken";
    private static final String FIELD_WBID = "WeiBoId";
    private static final String FIELD_WBTOKEN = "WeiBoToken";
    private static final String LOGIN_TYPE_FB = "Login_Facebook";
    private static final String LOGIN_TYPE_WX = "Login_WeChat";
    private static final String LOGIN_TYPE_WB = "Login_WeiBo";
    private static final String FIELD_VERIFY_CODE ="VerifyCode";
    private static final String FIELD_VERIFY_TYPE ="Type";

    private static final String FIELD_USER_NAME = "UserName";

    private static final String FIELD_DEVICE_UUID = "DeviceUUID";
    private static final String FIELD_DEVICE_TYPE = "DeviceType";
    private static final String FIELD_DEVICE_TOKEN = "DeviceToken";
    private static final String FIELD_DEVICE_MAC_ADDRESS = "MacAddress";
    private static final String FIELD_DEVICE_ENABLE_RESUME = "EnableResume";
    private static final String FIELD_APP_VERSION = "AppVersion";
    private static final String FIELD_Backup_Status = "BackupStatus";
    private static final String FIELD_FAVORITE = "Favorite";
    private static final String FIELD_APP_Type = "AppType";
    private static final String FIELD_APP_Region = "AppRegion";
    private static final String FIELD_FRIEND_ID = "FriendId";
    private static final String FIELD_EMAIL = "Email";

    private static final String FIELD_STATUS = "Status";

    private static final String FIELD_NOTIFICATION_ID = "NotificationId";
    private static final String FIELD_NOTIFICATION_REPLY = "Reply";
    private static final String FIELD_NOTIFICATION_CAT = "Category";

    private static final String FIELD_Service = "Service";
    private static final String FIELD_FRIEND_EMAIL = "FriendEmail";

    private static final String LOGIN_TYPE_EMIAL = "Login_Email";
    private static final String DEVICE_TYPE = "DeviceType_Android";

    private static final String VERIFY_TYPE_ACTIVATION = "Activation";
    private static final String VERIFY_TYPE_FORGOTPASSWORD ="ForgotPassword";



    /**
     *
     * @return lang or "en-us" if it is not available
     */
    public static String getLang() {
        String locale[] = Locale.getDefault().toString().toLowerCase().split("_");
        String lang = locale[0] + "-" + locale[1];

        for (String l : AVAILABLE_LANG_LIST) {
            if (l.equals(lang)) {
                return l;
            }
        }

        if (lang.equals("zh-ch")) {
            return "zh-ch";
        }

        lang = locale[0];
        for (String l : AVAILABLE_LANG_LIST) {
            if (l.startsWith(lang)) {
                return l;
            }
        }

        return "en-us";
    }
    public static String getUserId() {
        return "0000";
    }
    public static String getAccessToken() {
        return "9999";
    }



    /**
     * Create request String
     */
    public static JSONObject basicRequestObj() throws JSONException {
        return new JSONObject()
                .put(FIELD_ACCESS_TOKEN, getAccessToken())
                .put(FIELD_LOGIN_TYPE, LOGIN_TYPE_EMIAL)
                .put(FIELD_USER_ID, getUserId());
    }

    public static String requestRegister(String email, String pass, String name) {
        try {
            return new JSONObject()
                    .put(FIELD_REGISTER_ACCOUNT, email)
                    .put(FIELD_REGISTER_PASSWORD, pass)
                    .put(FIELD_REGISTER_NAME, name)
                    .put(FIELD_LANGUAGE, getLang())
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestReset(String macaddress) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_MAC_ADDRESS, macaddress)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestLogout() {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_UUID, Build.SERIAL)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestShareFriend(String macAddress, String friendId, Boolean share) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_MAC_ADDRESS, macAddress)
                    .put(FIELD_FRIEND_ID, friendId)
                    .put(FIELD_STATUS, share)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestShareMultiFriend(Boolean share,String macAddress, String friendId ) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_MAC_ADDRESS, macAddress)
                    .put(FIELD_FRIEND_ID, friendId)
                    .put(FIELD_STATUS, share)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestSharePermission(String macAddress, String friendId, Boolean perm) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_MAC_ADDRESS, macAddress)
                    .put(FIELD_FRIEND_ID, friendId)
                    .put(FIELD_STATUS, perm)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestDeviceShareList(String macAddress) {
        return requestReset(macAddress);
    }

    public static String requestDeviceUpdateName(String macAddress, String name) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_MAC_ADDRESS, macAddress)
                    .put(FIELD_REGISTER_NAME, name)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestDeviceUpdateFavorite(String macAddress, boolean favorite) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_MAC_ADDRESS, macAddress)
                    .put(FIELD_FAVORITE, favorite)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestDeviceUpdateResume(String macAddress, boolean enable) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_MAC_ADDRESS, macAddress)
                    .put(FIELD_DEVICE_ENABLE_RESUME, enable)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestDeviceUpdateResumeLock(boolean enable) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_ENABLE_RESUME, enable)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestRegisterVerify(String email, String code) {
        try {
            return new JSONObject()
                    .put(FIELD_USER_ID, email)
                    .put(FIELD_VERIFY_CODE, code)
                    .put(FIELD_VERIFY_TYPE, VERIFY_TYPE_ACTIVATION)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestLoginVerify(String email, String code) {
        try {
            return new JSONObject()
                    .put(FIELD_USER_ID, email)
                    .put(FIELD_VERIFY_CODE, code)
                    .put(FIELD_VERIFY_TYPE, VERIFY_TYPE_FORGOTPASSWORD)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestVerifyChangePassword(String email, String password , String code) {
        try {
            return new JSONObject()
                    .put(FIELD_USER_ID, email)
                    .put(FIELD_NEW_PASSWORD, password)
                    .put(FIELD_VERIFY_CODE, code)

                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }



    public static String requestLogin(String email, String pass) {
        try {
            return new JSONObject()
                    .put(FIELD_REGISTER_ACCOUNT, email)
                    .put(FIELD_REGISTER_PASSWORD, pass)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestFBLogin(String FB_Token, String FB_id) {
        try {
            return new JSONObject()
                    .put(FIELD_LOGIN_TYPE, LOGIN_TYPE_FB)
                    .put(FIELD_FBID, FB_id)
                    .put(FIELD_FBTOKEN, FB_Token)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestWXLogin(String WX_OpenId, String WX_Token) {
        try {
            return new JSONObject()
                    .put(FIELD_LOGIN_TYPE, LOGIN_TYPE_WX)
                    .put(FIELD_WXOPENID, WX_OpenId)
                    .put(FIELD_WXTOKEN, WX_Token)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestWBLogin(String WB_Id, String WB_Token) {
        try {
            return new JSONObject()
                    .put(FIELD_LOGIN_TYPE, LOGIN_TYPE_WB)
                    .put(FIELD_WBID, WB_Id)
                    .put(FIELD_WBTOKEN, WB_Token)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestFBRegister(String email, String pass, String name, String FB_id, String FB_Token) {
        try {
            return new JSONObject()
                    .put(FIELD_REGISTER_ACCOUNT, email)
                    .put(FIELD_REGISTER_PASSWORD, pass)
                    .put(FIELD_REGISTER_NAME, name)
                    .put(FIELD_LANGUAGE, getLang())
                    .put(FIELD_FBID, FB_id)
                    .put(FIELD_FBTOKEN, FB_Token)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestWXRegister(String email, String pass, String name, String WX_OpenId, String WX_Token) {
        try {
            return new JSONObject()
                    .put(FIELD_REGISTER_ACCOUNT, email)
                    .put(FIELD_REGISTER_PASSWORD, pass)
                    .put(FIELD_REGISTER_NAME, name)
                    .put(FIELD_LANGUAGE, getLang())
                    .put(FIELD_WXOPENID, WX_OpenId)
                    .put(FIELD_WXTOKEN, WX_Token)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestWBRegister(String email, String pass, String name, String WB_Id, String WB_Token) {
        try {
            return new JSONObject()
                    .put(FIELD_REGISTER_ACCOUNT, email)
                    .put(FIELD_REGISTER_PASSWORD, pass)
                    .put(FIELD_REGISTER_NAME, name)
                    .put(FIELD_LANGUAGE, getLang())
                    .put(FIELD_WBID, WB_Id)
                    .put(FIELD_WBTOKEN, WB_Token)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestforgotPassword(String email) {
        try {
            return new JSONObject()
                    .put(FIELD_REGISTER_ACCOUNT, email)
                    .put(FIELD_EMAIL, email)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestSelectInfo() {
        try {
            return basicRequestObj().toString();
        } catch (JSONException e) {
            return null;
        }

    }

    public static String requestUpdateInfo(String name, Boolean BackupStatus) {
        try {
            return basicRequestObj()
                    .put(FIELD_USER_NAME, name)
                    .put(FIELD_LANGUAGE, getLang())
                    .put(FIELD_Backup_Status, BackupStatus)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestChangePassword(String pwd, String newPwd) {
        try {
            return basicRequestObj()
                    .put("OldPassword" ,pwd)
                    .put("NewPassword" ,newPwd)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestMemberSearch(String searchId) {
        try {
            return basicRequestObj()
                    .put("SearchId" ,searchId)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestEmailInvitation(String email ) {
        try {
            return basicRequestObj()
                    .put(FIELD_FRIEND_EMAIL, email)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestDeviceStatus() {
        return requestSelectInfo();
    }

    public static String requestDeviceStatus(String mac) {
        return requestReset(mac);
    }


    public static String requestUpdateDeviceToken(String DeviceToken, String AppVersion) {
        try {
            return basicRequestObj()
                    .put(FIELD_DEVICE_TOKEN, DeviceToken)
                    .put(FIELD_DEVICE_UUID, Build.SERIAL)
                    .put(FIELD_DEVICE_TYPE,DEVICE_TYPE)
                    .put(FIELD_APP_VERSION, AppVersion)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestInvite(String friendId) {
        try {
            return basicRequestObj()
                    .put(FIELD_FRIEND_ID ,friendId)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestAskJoin(String friendId) {
        try {
            return basicRequestObj()
                    .put(FIELD_FRIEND_ID ,friendId)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestDeleteFriend(String friendId) {
        return requestInvite(friendId);
    }

    public static String requestFriends() {
        try {
            return basicRequestObj()
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestNotificationAll() {
        try {
            return basicRequestObj().toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestNotificationReply(String notificationId, Boolean reply) {
        try {
            return basicRequestObj()
                    .put(FIELD_NOTIFICATION_ID, notificationId)
                    .put(FIELD_NOTIFICATION_REPLY,reply)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestNotificationClean(int cat) {
        try {
            return basicRequestObj()
                    .put(FIELD_NOTIFICATION_CAT, cat)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestVerifyVersion(String appversion, String apptype, String appregion) {
        try {
            return new JSONObject()
                     .put(FIELD_APP_VERSION,appversion)
                     .put(FIELD_APP_Type, apptype)
                    .put(FIELD_APP_Region, appregion)
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public static String requestCheckZone() {
        try {
            return new JSONObject()
                    .put(FIELD_Service, "CHECK_ZONE")
                    .toString();
        } catch (JSONException e) {
            return null;
        }
    }
    public static void  setServerUrl(String url, String Country, String City) {
        SERVER_URL = url;
    }
    public static String  getServerUrl() {
        return SERVER_URL;
    }

;    public static boolean isOfficialUrl() {
        return !SERVER_URL.contains("dev");
    }

    public static String UploadImageUrl() {
        return SERVER_URL + "/Device/UploadImage";
    }
    public static String UploadMultiImageUrl() {
        return SERVER_URL + "/Device/UploadMultiImage";
    }



    public static JSONObject callApi(String apiPath, String params) {
        JSONObject inObj = null;
        String in = null;

        try {
            JSONObject outObj = new JSONObject();
            outObj.put("JsonObject", params);

            HttpURLConnection conn = (HttpURLConnection)new URL(SERVER_URL + apiPath).openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.write(outObj.toString().getBytes("utf-8"));
            out.flush();

            int res = conn.getResponseCode();

            if (res == 200) {
                InputStream is = conn.getInputStream();
                //in = NetUtils.readString(is);

                inObj = new JSONObject(in);
            } else {
                Log.e(TAG, "callApi, res=" + res);
            }

        } catch (Exception e) {
            Log.d(TAG, "callApi", e);
        }

//        if (BuildConfig.DEBUG) {
//            Log.d(TAG, "apiPath=" + apiPath);
//            Log.d(TAG, "params=" + params);
//            Log.d(TAG, "in=" + in);
//        }
        return inObj;
    }

    /**
     *
     */
    public static class PostTask extends AsyncTask<String, String, JSONObject> {

        /**
         *
         * @param params 2 Strings, URL, object
         * @return
         */
        @Override
        protected JSONObject doInBackground(String... params) {
            //return callApi(params[0], params[1]);
            String dddd = doNetworkOperation("", "");
            return callApi(params[0], params[1]);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(TAG, "PostTask: Progress=" + values[0]);
        }
    }

    /*
    public static class UploadIconTask extends AsyncTask<Pair, Integer, Integer> {
        HashMap<String, String> mData;
        File mIconFile;
        String mPath;

        public UploadIconTask(String path, HashMap<String, String> data, File iconFile) {
            mPath = path;
            mData = data;
            mIconFile = iconFile;
        }

        @Override
        protected  Integer doInBackground(Pair... params) {
           // String result = doNetworkOperation("minutiae", "9999999999999999999999999999");
            return 0;
        }
    }
    */
    public static final String Identify = "http://52.175.157.145:3000/api/identifyFP";
    public static final String Enroll = "http://52.175.157.145:3000/api/addFP";
    public static final String ftp_username = "ftp";
    public static final String ftp_password = "ftp";
    //public static final String ftpHost = "192.168.1.34";
    public static final int default_ftpPort = 2121;
    public static int ftpPort=0;

    public static void  setftpPort(int port) {
        //Log.d(TAG, "port:"+port); ftpPort = port;
        ftpPort = port;
    }
    public static int   getftpPort() {
        return ftpPort;
    }


    public static class NetworkOperation extends AsyncTask<String, Void, String> {

        String minutiae_Key = "minutiae=";
        String userId_Key = "userId=";
        String fpIndex_Key = "fpIndex=1"; //It's hard code for fpIdnex

        @Override
        protected String doInBackground(String... params) {

            if(params[0].equals(Identify)) {
                //String minutiae_Key = "minutiae=";
                Log.d(TAG,"url="+minutiae_Key + params[1]);
                String result = doNetworkOperation(params[0], minutiae_Key + params[1]);

                return result;
            }
            else
            {
                String dataUrl = userId_Key+params[1]+"&"+minutiae_Key + params[2]+"&"+fpIndex_Key;
                Log.d(TAG,"url: "+dataUrl);
                String result = doNetworkOperation(params[0], dataUrl);

                return result;
            }
        }

        //String urlParameters  = "param1=data1&param2=data2&param3=data3";
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

        @Override
        protected void onPostExecute(String result) {

        }


    }


    protected static String doNetworkOperation(String dataUrl, String dataUrlParameters) {
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
