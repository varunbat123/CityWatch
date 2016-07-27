package com.self.varun.mapproject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Varun on 7/12/2016.
 */
public class Gps_Service extends Service {
    private LocationListener listener;
    private LocationManager locationManager;
    Location location;
    Firebase ref;
    String x = " ";
    LatLng latLng;
    LatLng latLng2;
    LatLng thisL = null;
    // constant
    public static final long NOTIFY_INTERVAL = 50 * 1000; // 50 seconds

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {





// run until stopped, see logout method of MApsActivity.java

        return START_STICKY;
    }




    @Override
    public void onCreate() {

// Set firebase contex ,and reference
        Firebase.setAndroidContext(this);
        ref = new Firebase("https://vigilanti.firebaseio.com/");

        System.out.println("running");
        // cancel if already existed
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);





    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        System.out.println("Stopped Service");
        mTimer.cancel();
    }

    private void displayNotification(String title, String text) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setContentTitle(title);
        notification.setContentText(text);
        notification.setSmallIcon(R.mipmap.ic_launcher);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

// if notification is clicked launch login activity
        PendingIntent contentIntent = PendingIntent.getActivity(Gps_Service.this, 0,
                new Intent(Gps_Service.this, Login.class), PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(contentIntent);
        // build notification
        notificationManager.notify(14, notification.build());




    }

    public double compare(LatLng x, LatLng y) {
        double R = 6371;
        double lat1 = Math.toRadians(getLatitude(x));
        double lat2 = Math.toRadians(getLatitude(y));
        double difLat = lat1 - lat2;
        double difLon = Math.toRadians(x.longitude - y.longitude);
        double a = Math.sin(difLat / 2) * Math.sin(difLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(difLon / 2) * Math.sin(difLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;// Killometers
    }

    public double getLatitude(LatLng x) {
        return x.latitude;
    }

    public double getLongitude(LatLng x) {
        return x.longitude;
    }






    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {




                @Override
                public void run() {
                    {
Firebase newRef = new Firebase("https://vigilanti.firebaseio.com/Needs Help");
                        Query queryRef =
                                newRef.orderByChild("Longitude").startAt(0);
                        // Add listener for a child added at the data at this location
                        queryRef.addChildEventListener(new ChildEventListener() {

                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                Map data = (Map) dataSnapshot.getValue();


                                double latitude = Double.parseDouble(data.get("Latitude").toString());
                                double longitude = Double.parseDouble(data.get("Longitude").toString());



                                // Create LatLng for each locations
                                LatLng mLatlng = new LatLng(latitude, longitude);


// if  a citizen presses the help button they will be added to schema, and notification will push to homme screen.
                                if (Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString())>0) {
                                    displayNotification("Alert", "Help Needed ");
                                   System.out.println (Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString())>0);




                                }
                                // If there is only default value in firebase schema, then there are no citizens requiring help
                                else if (Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString())==0) {
                                    displayNotification("Alert", "No new citizens requiring help");


                                }


                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }


                        });


                    }
        }
    });

}}


}

