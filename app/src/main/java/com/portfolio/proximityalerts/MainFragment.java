package com.portfolio.proximityalerts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.Priority;
import com.portfolio.proximityalerts.databinding.MainFragmentBinding;

import java.util.HashMap;

/* ProximityAlerts - personal radar.
    Application tracks phone GPS data
    creates a server-socket to communicates its location.
    the server returns data on other objects in the area of the phone,
    the phone uses the data from server to display objects in a visual radar activity on screen.

    MainFragment initiate the managers: Compass, GPS and client for server communication
    responsible for managing the GUI
    Listen button clicks and data changes
    and destroys bindings upon destroy

    METHODS
  *  convertGpsToPixels() - convert GPS coordinates to pixels
        PARAM:
            int width - Width of the parent view/viewgroup.
            int height - Height of the parent view/viewgroup.
            InRangeObj object - InRangeObject object, object made from the data from server.
            Location currentLocation - Location object from the latest GPS cycle.
        RETURN: Float list of X and Y pixels points

   * drawInRangeObj() - Add or Update a view representing an InRange object and update view
        PARAM:
            HashMap objectList = hashmap of objects from server with MMSI as keys
        RETURN: Void

    * crossFade() - fade animation between views
        PARAM:
            LinearLayout toFront - layout to fade to front
            LinearLayout toBack - layout to fade to back
        RETURN: Void
     */
public class MainFragment extends Fragment{

    //VARIABLES
    public static final String TAG = "main fragment";
    private MainFragmentBinding binding;
    private int shortAnimationDuration;
    private double DegreeStart = 0f;
    //managers
    CompassManager compassManager;
    GpsManager gpsManager;
    UdpClient udpClient;
    RadarManager radarManager;
    ObserverManager observerManager;
    SharedPreferences sharedPreferences;
    //views and view groups
    FrameLayout encounterLayer;
    LinearLayout userTextLayer;
    LinearLayout targetTextLayer;
    LinearLayout visibleDashboard;
    static double nauticalMile;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext()); //setting config global data
        binding = MainFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get necessary views
        encounterLayer = binding.encounterLayer;
        binding.tvRadiusUI.setText(String.valueOf( (int) nauticalMile) );
        userTextLayer = view.findViewById(R.id.text_board_insert);
        visibleDashboard = userTextLayer;
        targetTextLayer = view.findViewById(R.id.encounter_text_board_insert);
        targetTextLayer.setVisibility(View.GONE);
        shortAnimationDuration = view.getResources().getInteger(android.R.integer.config_shortAnimTime);

        //Initialize Managers
        compassManager = CompassManager.getInstance(getContext());
        gpsManager = GpsManager.getInstance(getContext());
        udpClient = UdpClient.getInstance(getContext());
        radarManager = RadarManager.getInstance();
        observerManager = ObserverManager.getInstance(CompassManager.getCompassDirection(), GpsManager.getGpsLocation(), UdpClient.getNewMessage());

        //Listeners
        //listen for click on empty space in radar
        binding.radarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crossFade(true);
            }
        });

        //listeners for changes in global data
        final Observer<Object[]> liveDataObserver = new Observer<Object[]>() {
            @Override
            public void onChanged(Object[] objectList) {
                switch ((String) objectList[0]){
                    case "compass":
                        if (objectList[1] != null && (sharedPreferences.getBoolean("switch_compass", true))) {
                            //COMPASS
                            String[] comp = (String[]) objectList[1];
                            String direction = comp[1];
                            double angle = Double.parseDouble(comp[0]) * (-1);
                            String angleS = String.valueOf( (int)Double.parseDouble(comp[0]) );
                            String angleWithDirection = angleS + direction;
                            binding.textBoardInsert.tvDisplayDirection.setText(angleWithDirection);
                            RotateAnimation ra = new RotateAnimation(
                                    (float)DegreeStart,
                                    (float)angle,
                                    Animation.RELATIVE_TO_SELF, 0.5f,
                                    Animation.RELATIVE_TO_SELF, 0.5f);
                            // set the compass animation after the end of the reservation status
                            ra.setFillAfter(true);
                            // set how long the animation for the compass image will take place
                            ra.setDuration(500);
                            // Start animation of compass image
                            binding.radarLayer.startAnimation(ra);
                            DegreeStart = angle;
                            }
                        break;
                    case "gps":
                        if (objectList[2] != null) {
                            //GPS
                            Location l = (Location) objectList[2];
                            UdpClient.setCurrentLocation(l);
                            binding.textBoardInsert.displayLat.setText(String.valueOf(l.getLatitude()));
                            binding.textBoardInsert.displayLong.setText(String.valueOf(l.getLongitude()));
                            if (l.hasSpeed()) {
                                binding.textBoardInsert.displaySpeed.setText(String.valueOf(l.getSpeed()));
                            } else {
                                binding.textBoardInsert.displaySpeed.setText(R.string.not_available);
                            }
                        }
                        break;
                    case "udpClient":
                        if (objectList[3] != null) {
                            //CLIENT
                            Message clientMsg = (Message) objectList[3];
                            EncounterView view = new EncounterView(getContext(), clientMsg);
                            RadarManager.setEncounter(view);
                            drawEncounter(RadarManager.getTargetList());
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        observerManager.observe(getViewLifecycleOwner(), liveDataObserver);
    }//END OF ONVIEWCREATED

    //OVERRIDES
    @Override
    public void onResume() {
        super.onResume();
        //Log.e(TAG, "moved to foreground");
        nauticalMile = (Double.parseDouble(sharedPreferences.getString("radar_radius", "20")));
        binding.tvRadiusUI.setText(String.valueOf( (int) nauticalMile) );
        udpClient.startClient();
        if(sharedPreferences.getBoolean("switch_compass", true)){
            compassManager.registerListener();
        } else {
            compassManager.unregisterListener();
        }

        if(sharedPreferences.getBoolean("sw_save_power", false)) {
            GpsManager.locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
        } else {
            GpsManager.locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        }

        if (sharedPreferences.getBoolean("sw_location_services", true)){
            gpsManager.startLocationUpdates();
        } else {
            gpsManager.stopLocationUpdates();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        //Log.e(TAG, "moved to background");
        compassManager.unregisterListener();
        udpClient.stopClient();
        gpsManager.stopLocationUpdates();
        binding.radarLayer.clearAnimation();

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Log.e(TAG, "fragment destroyed");
        gpsManager.stopLocationUpdates();
        compassManager.unregisterListener();
        udpClient.stopClient();
        encounterLayer.removeAllViews();
        binding = null;
    }

    //UI Functions
    public void drawEncounter(HashMap<String, EncounterView> viewList){
         /*
        Method update and add encounter views on the screen
        *PARAM:
            HashMap<String, EncounterView> viewList - hashmap containing all relevant views to loop on
        *RETURN: Void */
        Log.e(TAG, "drawing");
        int width = encounterLayer.getWidth();
        int height = encounterLayer.getHeight();
        int TargetSize =  (int) Math.round(0.03 * Math.min(width, height));

        //Loop for every value in the hashmap
        for (EncounterView targetView : viewList.values()) {
            double[] points = convertGpsToPixels(targetView, GpsManager.getGpsLocation().getValue(), width, height);
            int radius = Math.min(width, height)/2;
            int distance = (int) Math.round(Math.sqrt(Math.pow(radius - points[0], 2) + Math.pow(radius - points[1], 2)));
            targetView.setTranslationX((float)points[0]);
            targetView.setTranslationY((float)points[1]);
            boolean targetAdded = encounterLayer.findViewById(targetView.getId()) != null;

            //if view was not updated for 5 min, remove it from list
            if(System.currentTimeMillis() - targetView.timestamp >= 300000){
                Log.e(TAG, "target did not respond for 5 min");
                RadarManager.removeTarget(targetView.mmsi);
                continue;
            }

            if(distance > radius) {
                Log.e(TAG, "target too far");
                if(targetAdded){
                    encounterLayer.removeView(encounterLayer.findViewById(targetView.getId()));
                }
            } else {
                if(!targetAdded) {
                    Log.e(TAG, "drawing new target");
                    encounterLayer.addView(targetView);
                    targetView.setLayoutParams(new FrameLayout.LayoutParams(TargetSize, TargetSize)); //set view size, must be after render on screen
                    targetView.setId(View.generateViewId());
                    Log.e(TAG, "new view created");
                    targetView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            binding.encounterTextBoardInsert.displayEncounterLat.setText(String.valueOf(targetView.position.getLat()));
                            binding.encounterTextBoardInsert.displayEncounterLong.setText(String.valueOf(targetView.position.getLong()));
                            //binding.encounterTextBoardInsert.displayEncounterSpeed.setText(String.valueOf(targetView.speed));
                            binding.encounterTextBoardInsert.displayEncounterSpeed.setText(String.valueOf(points[2]));
                            binding.encounterTextBoardInsert.tvDisplayEncounterMmsi.setText(targetView.mmsi);

                            crossFade(false);
                        }
                    });
                } else{
                    Log.e(TAG, "target should be drawn");
                }
            }
        }
    }

    public double[] convertGpsToPixels(EncounterView view, Location location, int width, int height){
        /*
        Utility method to converts GPS coordinates to pixels on screen
        to determine the XY method find the difference between the host and the InRange object
        and calculate the proper XY coordinates in which the InRange object will be presented on screen
        * PARAM:
            int width - Width of the parent view/viewgroup.
            int height - Height of the parent view/viewgroup.
            InRangeObj object - InRangeObject object, object made from the data from server.
            Location currentLocation - Location object from the latest GPS cycle.
       * RETURN: Float list of X and Y pixels points
        */
        nauticalMile = Double.parseDouble( sharedPreferences.getString("radar_radius", "20"));
        int diameter = Math.min(width, height);
        int center = diameter /2; //radius
        double radiusDouble = (double) center;

        //Longitude and Latitude
        double closeObjectY = view.position.getLat();
        double closetObjectX = view.position.getLong();
        double hostX = location.getLongitude();
        double hostY = location.getLatitude();

        //METHOD 1 - find distance in km or miles
        //convert to radians
        double lat1 = Math.toRadians(hostY);
        double lon1 = Math.toRadians(hostX);
        double lat2 = Math.toRadians(closeObjectY);
        double lon2 = Math.toRadians(closetObjectX);
        //find the Radian Difference with Haversine formula(C)
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);
        double c = 2 * Math.asin(Math.sqrt(a));
        // Radius of earth in kilometers. Use 3956
        // for miles(R)
        double r = 3956;
        // calculate the result
        double distance = c * r;

        //METHOD 2 - convert to Minutes to find XY
        //convert coord to string in format - DDD:MM.SSSS
        //D indicates degrees, M indicates minutes of arc, and S indicates seconds of arc
        //(1 minute = 1/60th of a degree, 1 second = 1/3600th of a degree).
        double[] latMin1 = convertToDoubleList(Location.convert(hostY, Location.FORMAT_MINUTES).split(":"));
        double[] latMin2 = convertToDoubleList(Location.convert(closeObjectY, Location.FORMAT_MINUTES).split(":"));
        double[] lonMin1 = convertToDoubleList(Location.convert(hostX, Location.FORMAT_MINUTES).split(":"));
        double[] lonMin2 = convertToDoubleList(Location.convert(closetObjectX, Location.FORMAT_MINUTES).split(":"));

        //find the difference in minutes = nautical mile
        double latMileY = convertToMapCoord(latMin1, latMin2);
        double lonMileX = convertToMapCoord(lonMin1, lonMin2);

        //CONVERT TO SCREEN METHOD
        //find the percentage of the difference from radar radius(nautical mile)
        double percentX = lonMileX / nauticalMile;
        double percentY = latMileY / nauticalMile;

        double pixelPercX = percentX * radiusDouble;
        double pixelPercy = percentY * radiusDouble;

        //find how much is the percent from the radius in pixels
        double pixelX = radiusDouble + pixelPercX;
        double pixelY = radiusDouble + (-pixelPercy);

        //THE RETURN
        //return points in list
        return new double[]{pixelX, pixelY, distance};
    }

    private double convertToMapCoord(double[] host, double[] target){
        double degreeHost = (host[0] * 60) + host[1];
        double degreeTarget = (target[0] * 60) + target[1];
        return degreeTarget - degreeHost;
    }

    private double[] convertToDoubleList(String[] coord){
        double[] result = new double[2];
        int c = 0;
        for (String n : coord){
            result[c] = Double.parseDouble(n);
            c++;
        }
        return result;
    }

    private void crossFade(boolean mainView){
        /*function handles cross fade animation between two Layer Layouts
        * true = main text layer
        * false = target text layer*/

        if( (visibleDashboard.getId() == targetTextLayer.getId() ) && mainView){
            // Animate the loading view to 0% opacity. After the animation ends,
            // set its visibility to GONE as an optimization step (it won't
            // participate in layout passes, etc.)
            targetTextLayer.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            targetTextLayer.setVisibility(View.GONE);
                        }
                    });
            visibleDashboard = userTextLayer;

        } else if( (visibleDashboard.getId() == userTextLayer.getId()) &&  !mainView){
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            targetTextLayer.setAlpha(0f);
            targetTextLayer.setVisibility(View.VISIBLE);

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            targetTextLayer.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);
            visibleDashboard = targetTextLayer;
        }
    }
}