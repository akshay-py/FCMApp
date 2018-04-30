package com.wedevol.fcmtest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * Firebase Messaging Service to handle push notifications
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FCMMessagingService";
    LocationManager locationManager;
    private static final int REQUEST_LOCATION = 1;
    private static WeakReference<MainActivity> mActivityRef;
    public static final String FCM_PROJECT_SENDER_ID = "362421666820";
    public static final String FCM_SERVER_CONNECTION = "@gcm.googleapis.com";
    public static final String BACKEND_ACTION_MESSAGE = "MESSAGE";
    public static final String BACKEND_ACTION_ECHO = "ECHO";
    public static final Random RANDOM = new Random();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // In this case the XMPP Server sends a payload data
            String message = remoteMessage.getData().get("message");
            Log.d(TAG, "Message received: " + message);
            if (message.equals("GETLOC")) {
                String location = getLocation();
                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(FCM_PROJECT_SENDER_ID + FCM_SERVER_CONNECTION)
                        .setMessageId(Integer.toString(RANDOM.nextInt()))
                        .addData("message", location)
                        .addData("action", BACKEND_ACTION_ECHO)
                        .build());
            }
            showBasicNotification(message);
            //showInboxStyleNotification(message);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }



    }

    private void showBasicNotification(String message) {
        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("Basic Notification")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());

    }

    public void showInboxStyleNotification(String message) {
        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Inbox Style notification")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(R.mipmap.ic_launcher, "show activity", pendingIntent);

        Notification notification = new Notification.InboxStyle(builder)
                .addLine(message).addLine("Second message")
                .addLine("Third message")
                .setSummaryText("+3 more").build();
        // Put the auto cancel notification flag
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

    }

    String getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(mActivityRef.get(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return null;
        } else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();

                return ("Latitude: " + latti + " Longitude: " + longi);
            } else {
                return ("Unable to find correct location.");
            }
        }
    }


    public static void updateActivity(MainActivity activity) {
        mActivityRef = new WeakReference<MainActivity>(activity);
    }
}
