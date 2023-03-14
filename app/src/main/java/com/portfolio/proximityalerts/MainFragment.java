package com.portfolio.proximityalerts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.Priority;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.portfolio.proximityalerts.databinding.MainFragmentBinding;

import java.util.HashMap;

/* ProximityAlerts - personal radar.
    Application tracks phone GPS data
    creates a server-socket to communicates its location.
    the server returns data on tracked objects in the area of the phone,
    the phone display objects in a visual, interactive radar activity on screen.

    MainFragment initiate the managers: Compass, GPS and client for server communication
    responsible for managing the GUI
    Listen button clicks and data changes
    and destroys bindings upon destroy and pause.

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
    //VIEW AND VIEW GROUPS
    TextView tvRadiusUI;
    ImageView ivAddTarget, ivZoomIn, ivZoomOut;
    //popupmenu
    TextInputLayout inputLayoutPopupDescription;
    TextInputEditText editTextPopupDescription;
    RadioButton targetLvlRadioButton;
    RadioButton previewsTargetLvlRadioButton;
    RadioGroup targetLvlRadioGroup;
    Button btnPopupSave;

    EncounterView currentActiveEncounterView;
    FrameLayout encounterLayer;
    LinearLayout userTextLayer;
    LinearLayout targetTextLayer;
    LinearLayout visibleDashboard;
    int nauticalMile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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

        nauticalMile = sharedPreferences.getInt("uiRadius", 20);

        //get necessary views
        ivAddTarget = view.findViewById(R.id.iv_target_add);
        ivZoomIn = view.findViewById(R.id.iv_zoom_in);
        ivZoomOut = view.findViewById(R.id.iv_zoom_out);

        encounterLayer = binding.encounterLayer;
        tvRadiusUI = view.findViewById(R.id.tv_radiusUI);
        tvRadiusUI.setText(String.valueOf( (int) nauticalMile) );
        userTextLayer = view.findViewById(R.id.text_board_insert);
        visibleDashboard = userTextLayer;
        targetTextLayer = view.findViewById(R.id.encounter_text_board_insert);
        //targetTextLayer.setVisibility(View.GONE);
        //shortAnimationDuration = view.getResources().getInteger(android.R.integer.config_shortAnimTime);

        //Initialize Managers
        radarManager = RadarManager.getInstance(getContext());
        compassManager = CompassManager.getInstance(getContext());
        gpsManager = GpsManager.getInstance(getContext());
        udpClient = UdpClient.getInstance(getContext());
        observerManager = ObserverManager.getInstance(CompassManager.getCompassDirection(), GpsManager.getGpsLocation(), UdpClient.getNewMessage());

        //LISTENERS
        //listen to zoon in
        ivZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nauticalMile--;
                tvRadiusUI.setText(String.valueOf( (int) nauticalMile) );
                drawEncounter();
            }
        });

        //listen to zoom out
        ivZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nauticalMile++;
                tvRadiusUI.setText(String.valueOf( (int) nauticalMile) );
                drawEncounter();
            }
        });

        //listen to add target
        ivAddTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow(view);
            }
        });
        //listen for click on empty space in radar
        binding.radarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivAddTarget.setColorFilter(getResources().getColor(R.color.dark_gray));
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
                            if (l.hasSpeed()) {
                                binding.textBoardInsert.displaySpeed.setText(String.format("%.1f", l.getSpeed()));
                            } else {
                                binding.textBoardInsert.displaySpeed.setText(R.string.not_available_shrt);
                            }

                            //check if screen data exist before rendering
                            if(encounterLayer.getHeight() > 0) {
                                drawEncounter();
                            }
                        }
                        break;
                    case "udpClient":
                        if (objectList[3] != null) {
                            //CLIENT
                            Message clientMsg = (Message) objectList[3];
                            EncounterView view = new EncounterView(getContext(), clientMsg);
                            RadarManager.setEncounter(view);
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
        sharedPreferences.edit().putInt("uiRadius", nauticalMile).apply();
        gpsManager.stopLocationUpdates();
        compassManager.unregisterListener();
        udpClient.stopClient();
        encounterLayer.removeAllViews();
        radarManager.saveSavedList();
    }

    //UI Functions
    public void drawEncounter(){
         /*
        Method update and add encounter views on the screen
        *RETURN: Void */
        HashMap<String, EncounterView> encounterList = RadarManager.getTargetList();
        HashMap<String, EncounterView> viewList = new HashMap<>();
        viewList.putAll(encounterList);

        binding.encounterTextBoardInsert.displayTargetsSum.setText(String.valueOf(viewList.size()));

        Log.e(TAG, "drawing");
        int width = encounterLayer.getWidth();
        int height = encounterLayer.getHeight();
        int TargetSize =  (int) Math.round(0.06 * Math.min(width, height));

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

                    targetView.setId(Integer.parseInt(targetView.mmsi));
                    Log.e(TAG, "new view created");
                    //set click listener fo every encounter view
                    targetView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            binding.encounterTextBoardInsert.displayEncounterName.setText(targetView.name);
                            binding.encounterTextBoardInsert.tvDisplayEncounterSpeed.setText(String.format("%.1f", targetView.speed));
                            binding.encounterTextBoardInsert.displayEncounterDistance.setText(String.valueOf(points[2]));
                            binding.encounterTextBoardInsert.displayEncounterMmsi.setText(targetView.mmsi);
                            binding.encounterTextBoardInsert.displayDescription.setText(targetView.getEncounterDescription());

                            //enable add action
                            ivAddTarget.setColorFilter(getResources().getColor(R.color.red));
                            currentActiveEncounterView = targetView;
                            //start fade animation
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
        //nauticalMile = Double.parseDouble( sharedPreferences.getString("radar_radius", "20"));
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
        // for miles(R), 6371 fo Km
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
            binding.encounterTextBoardInsert.displayEncounterName.setText(R.string.not_available);
            binding.encounterTextBoardInsert.tvDisplayEncounterSpeed.setText(R.string.not_available);
            binding.encounterTextBoardInsert.displayEncounterDistance.setText(R.string.not_available);
            binding.encounterTextBoardInsert.displayEncounterMmsi.setText(R.string.not_available);

            /*
            targetTextLayer.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            targetTextLayer.setVisibility(View.GONE);
                        }
                    });*/

            visibleDashboard = userTextLayer;

        } else if( (visibleDashboard.getId() == userTextLayer.getId()) &&  !mainView){
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            /*
            targetTextLayer.setAlpha(0f);
            targetTextLayer.setVisibility(View.VISIBLE);

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            targetTextLayer.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);*/
            visibleDashboard = targetTextLayer;
        }
    }

    public void popupWindow(View mView){
        //POPUP MENU
        //set popup menu and actions when 'plus' sign is clicked

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)getLayoutInflater();
        View popupView = inflater.inflate(R.layout.layout_popup_save_target, null);
        //set titles
        TextView tvName = popupView.findViewById(R.id.tv_popup_space_name);
        tvName.setText(currentActiveEncounterView.name);
        TextView tvMmsi = popupView.findViewById(R.id.tv_popup_space_mmsi);
        tvMmsi.setText(currentActiveEncounterView.mmsi);

        final int[] targetLvl = new int[1];
        //set radio group buttons;
        targetLvlRadioGroup = (RadioGroup) popupView.findViewById(R.id.target_lvl_radioGroup);
        targetLvlRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                targetLvlRadioButton = (RadioButton) popupView.findViewById(i);
                if(previewsTargetLvlRadioButton != null){
                    previewsTargetLvlRadioButton.setTextColor(getResources().getColor(R.color.transparent));
                }

                previewsTargetLvlRadioButton = targetLvlRadioButton;
                targetLvlRadioButton.setTextColor(getResources().getColor(R.color.black));
                switch (targetLvlRadioButton.getId()){
                    case R.id.target_lvl_high_radioButton:
                        targetLvl[0] = 3;
                        break;
                    case R.id.target_lvl_med_radioButton:
                        targetLvl[0] = 2;
                        break;
                    case R.id.target_lvl_low_radioButton:
                        targetLvl[0] = 1;
                        break;
                    default:
                        break;
                }
            }
        });

        //set description texEdit
        editTextPopupDescription = (TextInputEditText) popupView.findViewById(R.id.ti_edit_text_popup_description_input);
        inputLayoutPopupDescription = (TextInputLayout) popupView.findViewById(R.id.ti_layout_popup_description_input);
        editTextPopupDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > inputLayoutPopupDescription.getCounterMaxLength()) {
                    inputLayoutPopupDescription.setError("Max character length is " + inputLayoutPopupDescription.getCounterMaxLength());
                }else{
                    inputLayoutPopupDescription.setError(null);
                }
            }
        });

        //create pop menu
        //focusable = true; lets taps outside the popup also dismiss it
        int width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        int height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        //set save button
        btnPopupSave = (Button) popupView.findViewById(R.id.btn_popup_save);
        btnPopupSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentActiveEncounterView.setEncounterColor(targetLvl[0]);
                currentActiveEncounterView.setEncounterDescription(editTextPopupDescription.getText().toString());
                RadarManager.saveToTargetList(currentActiveEncounterView);
                popupWindow.dismiss();
                currentActiveEncounterView.invalidate();
            }
        });

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(mView, Gravity.CENTER, 0, 0);
        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
}