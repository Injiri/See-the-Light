package com.injiri.cymoh.tracker.tracker_settings;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClipboardMonitorService extends Service {
    private static final String TAG = "ClipboardManager";
    public static String CLIPBORD_MONITOR_SERVICE_ACTION = ClipboardMonitorService.class.getName() + "clipboardservice_broadcast";
    public static String ML_RESPONSE = "extra_mlresponse";
    private ExecutorService mThreadPool = Executors.newSingleThreadExecutor();
    private ClipboardManager mClipboardManager;

    @Override
    public void onCreate() {
        super.onCreate();
        //start the clipboard monito service
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(
                mOnPrimaryClipChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(
                    mOnPrimaryClipChangedListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    Log.d(TAG, "onPrimaryClipChanged");
                    //	clearPrimaryClip()
//                    read only url values on the clip not just any copied item
//                    determine if you should enable or disable the "paste" option in the current Activity. You should verify that the clipboard contains a clip and that
//                    you can handle the type of data represented by the clip:

                    if (!(mClipboardManager.hasPrimaryClip()) ) {
                        ClipData clip = mClipboardManager.getPrimaryClip();
                        ClipData.Item item = clip.getItemAt(0);
                        String clip_text_url) = item.getText().toString();

                        if (isValid(clip_text_url)) {

                            publish_ml_feedback(clip_text_url);

                        }else {
                            Toast.makeText(getApplicationContext(), "copy a valid news url", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            };

    /* Returns true if url is valid */
    public static boolean isValid(String url)
    {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    public void publish_ml_feedback(String endpoint,String clipboad_url){
        Toast.makeText(getApplicationContext(), "clip val: " + clipboad_url, Toast.LENGTH_LONG).show();

        //request modelvalidation
        String ml_response =ServerUtilities.query_ML_Model(endpoint,clipboad_url);

        Log.d(TAG, "sending ml responce info...");
        Intent intent = new Intent(CLIPBORD_MONITOR_SERVICE_ACTION);
        intent.putExtra(ML_RESPONSE, ml_response);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static String query_ML_Model (String endpoint, Map < String, String > params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            //open the HTTP connection
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }

            // Get Response
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            rd.close();
            return response.toString();

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
}