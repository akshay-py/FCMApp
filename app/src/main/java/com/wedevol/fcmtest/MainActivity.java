package com.wedevol.fcmtest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;


/**
 * Main Activity for FCM Token
 */
public class MainActivity extends AppCompatActivity implements IRequestListener{

    private static final String TAG = "MainActivity";
    public static final String FCM_PROJECT_SENDER_ID = "362421666820";
    public static final String FCM_SERVER_CONNECTION = "@gcm.googleapis.com";
    public static final String BACKEND_ACTION_MESSAGE = "MESSAGE";
    public static final String BACKEND_ACTION_ECHO = "ECHO";
    public static final Random RANDOM = new Random();

    private EditText editTextEcho;
    private TextView deviceText;
    private Button buttonUpstreamEcho;
    private TokenService tokenService;
    private static final int REQUEST_LOCATION = 1;
    Button button;
    TextView textView;
    LocationManager locationManager;
    String lattitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseMessagingService.updateActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "FCM Token creation logic");

        // Get variables reference
        deviceText = (TextView) findViewById(R.id.deviceText);
        editTextEcho = (EditText) findViewById(R.id.editTextEcho);
        buttonUpstreamEcho = (Button) findViewById(R.id.buttonUpstreamEcho);

        //Get token from Firebase
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        final String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + token);
        deviceText.setText(token);

        //Call the token service to save the token in the database
        tokenService = new TokenService(this, this);
        tokenService.registerTokenInDB(token);

        buttonUpstreamEcho.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Echo Upstream message logic");
                String message = editTextEcho.getText().toString();
                Log.d(TAG, "Message: " + message + ", recipient: " + token);
                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(FCM_PROJECT_SENDER_ID + FCM_SERVER_CONNECTION)
                        .setMessageId(Integer.toString(RANDOM.nextInt()))
                        .addData("message", message)
                        .addData("action", BACKEND_ACTION_ECHO)
                        .build());
                // To send a message to other device through the XMPP Server, you should add the
                // receiverId and change the action name to BACKEND_ACTION_MESSAGE in the data
            }
        });
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        textView = (TextView)findViewById(R.id.text_location);
        button = (Button)findViewById(R.id.button_location);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        getLocation();
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "Token registered successfully in the DB");

    }

    @Override
    public void onError(String message) {
        Log.d(TAG, "Error trying to register the token in the DB: " + message);
    }

    void getLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null){
                double latti = location.getLatitude();
                double longi = location.getLongitude();

                ((TextView)findViewById(R.id.text_location)).setText("Latitude: " + latti + " Longitude: " + longi);
            } else {
                ((TextView)findViewById(R.id.text_location)).setText("Unable to find correct location.");
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                getLocation();
                break;
        }
    }

}

