package com.self.varun.mapproject;

import android.*;
import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;


    public static final int MAP_ZOOM_LEVEL = 7;
    public static final long UPDATE_INTERVAL_IN_MS = 120000;

    Firebase ref;

    public static final String TAG = MapsActivity.class.getSimpleName();
    private LocationRequest mLocationRequest;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        ref = new Firebase("https://vigilanti.firebaseio.com/");
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Create the LocationRequest object
       // if (!Permissions()) {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000); // 1 second, in milliseconds
            buildGoogleApiClient();
       // }




    }
// Check if location use is allowed
    private boolean Permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }

        {
            return false;
        }

    }

// request location permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                Permissions();
            }
        }
    }
// Send location of suspicious activity to firebase database under specific child
    public void Report() {

        TextView report = (TextView) findViewById(R.id.btn_Report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Sends location of suspicous activity to firebase once report button is clicked

                Map report = new HashMap();
                String name = intent.getStringExtra("Username").toString();
                report.put("Latitude", LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude());

                report.put("Longitude", LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude());
                ref.child("Suspicious Activity").child(name).setValue(report);

                Toast.makeText(MapsActivity.this, "Suspicious activity in your location has been reported", Toast.LENGTH_LONG).show();
            }
        });
        drawActivity();
    }

    // Log user out and stop notifications
    public void Logout() {

        TextView logout = (TextView) findViewById(R.id.btn_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this," Logging out will stop notifications", Toast.LENGTH_LONG).show();
                stopService(new Intent(getBaseContext(), Gps_Service.class));
                Intent main = new Intent(MapsActivity.this, Login.class);
                main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(main);
                finish();
            }
        });

    }
// Display user name and type at top of UI
    public void setUp() {
        final TextView welcome = (TextView) findViewById(R.id.txt_Welcome);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent i = getIntent();
                String s = i.getStringExtra("Username");
                String name = dataSnapshot.child(s).child("Name").getValue().toString();
               // String type = dataSnapshot.child(s).child("Type").getValue().toString();
                welcome.setText("Welcome " + name );
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

    // If safe button is clicked user will be removed from list that requires help, and suspicious activities they reported will be removed, and will not be shown on radar
    public void safe() {
         final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle);






            // Get name
            String name = intent.getStringExtra("Username").toString();
            // remove children of name under /Needs Help
            ref.child("Needs Help").child(name).removeValue();
// remove children of name under / Suspicious activity
            ref.child("Suspicious Activity").child(name).removeValue();
            drawMap();
            drawActivity();

        }







    // If help buttton is clicked, user will be saved to list that requires help in firebase, and will be shown on radar to all users, until safe button is clicked
    public void help() {
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle);
       toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               if (b){
                   Map help = new HashMap();
                   // get name
                   String name = intent.getStringExtra("Username").toString();
                   // add latitude and longitude to hashmap
                   if (mGoogleApiClient.isConnected()) {
                       help.put("Latitude", LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude());
                       help.put("Longitude", LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude());
                       // set value of hash map to child name under / Needs Help
                       ref.child("Needs Help").child(name).setValue(help);

                       Toast.makeText(MapsActivity.this, "Your Location has been sent to our database", Toast.LENGTH_LONG).show();
                   } else {
                       System.out.println("Wasnt connected try again");
                       mGoogleApiClient.connect();
                   }

               }
               else{
                   safe();
               }
           }
       });


                }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start Background service that updates users with notifications for new citizens requiring help!
        Intent i = getIntent();
        Intent serviceIntent = new Intent(MapsActivity.this, Gps_Service.class);
        String s = i.getStringExtra("Username").toString();
        serviceIntent.putExtra("UserID", s);
        MapsActivity.this.startService(serviceIntent);
        startService(new Intent(this, Gps_Service.class));
// connect location services
        mGoogleApiClient.connect();
        return;

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

    }


    @Override
    protected void onResume() {
        super.onResume();


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;





    }
    /*This call back is triggered once the api client is connected,
    all methods that require api client to be connected must be called here
    */

    @Override
    public void onConnected(Bundle bundle) {
        // create location based on the users current coordinates
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
            setUp();
            help();
            Logout();
            Report();
            drawActivity();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        return;

    }

    private void displayNotification(String title, String text) {
 // create a builder object with context this
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
   // set title to title
        notification.setContentTitle(title);
  // set text to text
        notification.setContentText(text);
   // set display
        notification.setSmallIcon(R.mipmap.ic_launcher);
// display
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(14, notification.build());

    }

    @Override
    protected void onPause() {
        super.onPause();

        // if api client is connected remove the location updates and disconnect

    }
// get a latitude from a latlng object
    public double getLatitude(LatLng x) {
        return x.latitude;
    }
// get longitude form a latlng object
    public double getLongitude(LatLng x) {
        return x.longitude;
    }
    // Get distance between two coordinate systems
    public double compare(LatLng x, LatLng y) {
       // radius of earth
        double R = 6371;
   // latitude of first latlng object in radians
        double lat1 = Math.toRadians(getLatitude(x));
// latitude of second latlng object in radians
        double lat2 = Math.toRadians(getLatitude(y));
// difference between two latitudes
        double difLat = lat1 - lat2;
        double difLon = Math.toRadians(x.longitude - y.longitude);
        double a = Math.sin(difLat / 2) * Math.sin(difLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(difLon / 2) * Math.sin(difLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;// Killometers
    }

    private void handleNewLocation(Location location) {
        intent = getIntent();
        // get username from previous activity
        final String x = intent.getStringExtra("Username").toString();
        // set location latitude to current latitude
        double currentLatitude = location.getLatitude();
        // set location longitude to current longitude
        double currentLongitude = location.getLongitude();
        // LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        // use username intent to set location to specific user in firebase
        ref.child(x).child("Latitude").setValue(currentLatitude);
        ref.child(x).child("Longitude").setValue(currentLongitude);
        drawMap();

        Log.d(TAG, location.toString());

    }
// draw map with markers of Latlngs saved in firebase ref
    public void drawMap() {
        mMap.clear();
 // Set users Latlng to current location
        LatLng thisL = new LatLng(getLatLng(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)).latitude, getLatLng(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)).longitude);
        MarkerOptions current = new MarkerOptions();
        current.position(thisL);
        current.title("I am here");
        current.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_me));
 // Add thisL to map
        mMap.addMarker(current);
  // move camera to thisL
        mMap.moveCamera(CameraUpdateFactory.newLatLng(thisL));


        // Get list of people who need help saved to fire base
        Firebase newRef = new Firebase("https://vigilanti.firebaseio.com/Needs Help");
        // create query of all users who need help
        Query queryRef =
                newRef.orderByChild("Longitude").startAt(0);
        // Add listener for a child added at the data at this location
        queryRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // get lattitude and longitude of all users who need help
                Map data = (Map) dataSnapshot.getValue();
                LatLng thisL = new LatLng(getLatLng(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)).latitude, getLatLng(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)).longitude);
                // Instantiate current users location

                double latitude = Double.parseDouble(data.get("Latitude").toString());
                double longitude = Double.parseDouble(data.get("Longitude").toString());


                // Create LatLng for each locations of users who need help

                LatLng mLatlng = new LatLng(latitude, longitude);

                System.out.println(mLatlng);
  // zoom in to boundaries of latLngs within distance


// compare current user who is logged in location to all users who need help
                if (compare(thisL, mLatlng) < 5) {
                    Toast.makeText(MapsActivity.this, " Help needed in range!", Toast.LENGTH_LONG).show();
                    MarkerOptions options = new MarkerOptions();
                    // show users who need help
                    options.position(mLatlng);
                    //.position(latLng2)
                    options.title("I Need Help!");
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_help));
                    mMap.addMarker(options);
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    LatLngBounds boundaries;
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(mLatlng);
                    boundaries = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundaries,
                            MAP_ZOOM_LEVEL));




                } else {
                    Toast.makeText(MapsActivity.this, "Searching database for citizens requiring assistance..", Toast.LENGTH_LONG).show();
                    MarkerOptions options = new MarkerOptions();
                    options.position(mLatlng);
                    options.title("NOT IN RANGE");
                    mMap.addMarker(options);


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

    public void drawActivity() {
        Firebase newRef = new Firebase("https://vigilanti.firebaseio.com/Suspicious Activity");
        // create query of all users who need help
        Query queryRef =
                newRef.orderByChild("Longitude").startAt(0);
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map sus = (Map) dataSnapshot.getValue();

                LatLng thisL = new LatLng(getLatLng(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)).latitude, getLatLng(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)).longitude);
                // Instantiate current users location}

                double latitude = Double.parseDouble(sus.get("Latitude").toString());
                double longitude = Double.parseDouble(sus.get("Longitude").toString());


                // Create LatLng for each suspicioous activity area

                LatLng mLatlng = new LatLng(latitude, longitude);



// compare current user who is logged in location to all suspicious activities
                if (compare(thisL, mLatlng ) < 5 ) {
                    Toast.makeText(MapsActivity.this, "Suspicious Activity in range, stay clear!", Toast.LENGTH_LONG).show();
                    MarkerOptions options = new MarkerOptions();
                    // show users who need help
                    options.position(mLatlng);
                    //.position(latLng2)
                    options.title("Suspicious Activity");
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_help));
                    mMap.addMarker(options);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatlng));


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
    public LatLng getLatLng(Location location){// helper method


// request new location update
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
   // create a latlng with current location
        LatLng thisLatlng = new LatLng(location.getLatitude(),location.getLongitude());
        return thisLatlng;
    }

    @Override
    public void onLocationChanged(Location location) {


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}