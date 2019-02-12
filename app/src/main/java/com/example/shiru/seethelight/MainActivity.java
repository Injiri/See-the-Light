package com.example.shiru.seethelight;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText etName;

    WebView myWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.editText);

        myWebView = (WebView) findViewById(R.id.webview);


        // initiate progress bar and start button
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        Button startButton = (Button) findViewById(R.id.startButton);

        // perform click event on button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // visible the progress bar
                simpleProgressBar.setVisibility(View.VISIBLE);


                myWebView.loadUrl("https://see-the-light.herokuapp.com/news/get?link=" + etName.getText().toString());
// this will start the clip monitoring service
                LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String ML_VALUE = intent.getStringExtra(ClipboardMonitorService.ML_RESPONSE);
                        if ( !=null){
                            Toast.makeText(getApplicationContext(), ML_VALUE, Toast.LENGTH_LONG).show();
                            //LEVEL
                            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.new_mail)
                                    .setContentTitle("STL FEEDBACK")
                                    .setContentText(ML_VALUE)
                                    .setLargeIcon(emailObject.getSenderAvatar())
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(emailObject.getSubjectAndSnippet()))
                                    .build();
                        }
                    }
                }, new IntentFilter(userlocation_service.CLIPBORD_MONITOR_SERVICE_ACTION));

            }
        });


}
