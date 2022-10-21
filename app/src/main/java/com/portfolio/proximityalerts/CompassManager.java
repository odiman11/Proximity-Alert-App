package com.portfolio.proximityalerts;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompassManager extends AppCompatActivity implements SensorEventListener {

    public static final String TAG = "CompassManager";
    private static CompassManager singleInstance = null;

    RadarView radar;

    // device sensor manager
    private SensorManager sensorManage;
    // define the compass picture that will be use

    // record the angle turned of the compass picture
    private static float DegreeStart = 0f;
    private static double degree;
    private static double angle;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    static MutableLiveData<String[]>  compassDirectionLiveData;

    static String KEY_ANGLE = "angle";
    static String KEY_DIRECTION = "direction";
    static String KEY_BACKGROUND = "background";
    static String KEY_NOTIFICATION_ID = "notificationId";
    static String KEY_ON_SENSOR_CHANGED_ACTION = "com.portfolio.proximityalerts.CompassManager.ON_SENSOR_CHANGED";
    static String KEY_NOTIFICATION_STOP_ACTION = "com.portfolio.proximityalerts.CompassManager.NOTIFICATION_STOP";

    private CompassManager(Context context){
        sensorManage = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        compassDirectionLiveData = new MutableLiveData<String[]>();

    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        // get angle around the z-axis rotated
        if(event == null){return;}
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        updateOrientationAngles();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // "rotationMatrix" now has up-to-date information.
        float[] orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles);
        // "orientationAngles" now has up-to-date information.

        degree = (Math.toDegrees((double) orientation[0]) + 360.0) % 360.0;
        angle = Math.round(degree * 100) / 100;

        //Log.e(TAG, String.valueOf(degree));

        String direction = getDirection(degree);
        String[] compassData = {String.valueOf(angle),  direction};
        compassDirectionLiveData.setValue(compassData);


        /*
        //intent to send data to fragment
        Intent intent = new Intent();
        intent.putExtra(KEY_ANGLE, angle);
        intent.putExtra(KEY_DIRECTION, direction);
        intent.setAction(KEY_ON_SENSOR_CHANGED_ACTION);

        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);*/
    }

    public void unregisterListener (){
        // to stop the listener and save battery
        sensorManage.unregisterListener(this);
    }

    public void registerListener (){
        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManage.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManage.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManage.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private String getDirection(Double angle) {
        String direction = "";

        if (angle >= 350 || angle <= 10)
            direction = "N";
        if (angle < 350 && angle > 280)
            direction = "NW";
        if (angle <= 280 && angle > 260)
            direction = "W";
        if (angle <= 260 && angle > 190)
            direction = "SW";
        if (angle <= 190 && angle > 170)
            direction = "S";
        if (angle <= 170 && angle > 100)
            direction = "SE";
        if (angle <= 100 && angle > 80)
            direction = "E";
        if (angle <= 80 && angle > 10)
            direction = "NE";

        return direction;
    }

    public static CompassManager getInstance(Context context){
        if(singleInstance == null){
            singleInstance = new CompassManager(context);
        }
        return singleInstance;
    }

    public static MutableLiveData<String[]> getCompassDirection() {
        if (compassDirectionLiveData == null) {
            compassDirectionLiveData = new MutableLiveData<String[]>();
        }
        return compassDirectionLiveData;
    }



}
