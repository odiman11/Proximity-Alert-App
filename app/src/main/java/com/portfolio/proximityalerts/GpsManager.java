package com.portfolio.proximityalerts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/*
Singleton Class initiate gps tracking on phone
 class is responsible for requesting user for permissions to use phone gps
 after receiving permissions object creates the a local-request object which:
    implement gps configurations
    and saves current locations data in a local variable

 METHODS
 requestPermission() - for api 23 and above will request permission on runtime. return boolean of permissions.
 stopLocationUpdates() - start updating GPS in set intervals
 startLocationUpdates() - stop updating GPS
 updateGPS() - manual gps update, to get the latest location
 getLastLocation() - return a current location Location object
*/
public class GpsManager extends AppCompatActivity {
    //CONSTANTS
    private static final int DEFAULT_UPDATE_INTERVAL = 1;
    private static final int FAST_UPDATE_INTERVAL = 500;
    private static final int UPDATE_INTERVAL = DEFAULT_UPDATE_INTERVAL;
    private static final int PERMISSIONS_ALL = 99;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final String TAG = "GpsManager";


    //VARIABLES
    Handler handler;
    static Context appContext;
    private static MutableLiveData<Location> gpsLocationLiveData;
    private static GpsManager singleInstance = null;
    static Location currentLocation;

    FusedLocationProviderClient fusedLocationProviderClient;
    public static LocationRequest locationRequest; //config file for all setting related to fusedLocationProviderClient.
    static LocationCallback locationCallBack;

    //CONSTRUCTOR
    private GpsManager(Context context) {

        gpsLocationLiveData = new MutableLiveData<Location>();

        appContext = context;
        //set all properties of locationRequest
        locationRequest = LocationRequest.create();
        // how often location check occur in ms.
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        // how often location check occur when set to most frequent update
        locationRequest.setFastestInterval(FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context); // Googles api for location services.;

        //event that triggers whenever the update interval is met.
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult == null){
                    Log.e(TAG, "location is null");
                    return;}
                Log.e(TAG, locationResult.getLastLocation().toString());
                gpsLocationLiveData.setValue(locationResult.getLastLocation());

            }
        };
        requestPermission();

        //requestPermissionsLauncher = singleInstance.registerForActivityResult(ActivityResultContracts.RequestPermission());
    }//END OF CONSTRUCTOR


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_ALL:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "handler made from onrequest");
                            updateGPS();
                            handler.postDelayed(this,UPDATE_INTERVAL * 1000 );
                        }
                    }, 1000);

                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(appContext, "this app requires permissions in order to work", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


    //METHODS
    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        handler.removeCallbacksAndMessages(null);
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "gps not started");
        } else{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
            Log.e(TAG, "gps started");
        }
    }

    private void requestPermission() {
        //only API 23 and above can use request permission method at runtime, all other request should be done at manifest level and will be set upon install
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions((MainActivity) appContext, PERMISSIONS, PERMISSIONS_ALL);
        }
            //startLocationUpdates();
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Log.e(TAG, "handler made from request permissions");
                    updateGPS();
                    handler.postDelayed(this,UPDATE_INTERVAL * 1000 );
                }
            }, 1000);
        }

    private void updateGPS() {
        //get permission from user to track GPS
        //get current location from fused client
        //update the UI

        if(fusedLocationProviderClient == null){
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        }
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                //got permission. Put Value of location in UI
                if(location != null) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    double speed;
                    if (location.hasSpeed()) {
                        speed = location.getSpeed();
                    } else {
                        speed = 0.0;
                    }
                    currentLocation = location;
                    gpsLocationLiveData.setValue(location);
                    Log.e(TAG, location.toString());
                } else {
                    Log.e(TAG, "no location");
                }
            });

        } else {
            //permissions not granted yet
        }
    }

    public static Location getLastLocation(){
        return currentLocation;
    }

    public static GpsManager getInstance(Context context) {
        if (singleInstance == null) {
            singleInstance = new GpsManager(context);
        }
        return singleInstance;
    }

    public static MutableLiveData<Location> getGpsLocation() {
        if (gpsLocationLiveData == null) {
            gpsLocationLiveData = new MutableLiveData<Location>();
        }
        return gpsLocationLiveData;
    }


}
