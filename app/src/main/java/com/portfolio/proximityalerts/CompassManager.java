package com.portfolio.proximityalerts;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;


public class CompassManager extends AppCompatActivity implements SensorEventListener {

    public static final String TAG = "CompassManager";
    private static CompassManager singleInstance = null;

    RadarView radar;

    // device sensor manager
    private SensorManager sensorManage;

    // record the angle turned of the compass picture
    private static float DegreeStart = 0f;
    private static double degree;
    private static double angle;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    static MutableLiveData<String[]>  compassDirectionLiveData;

    private CompassManager(Context context){
        sensorManage = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        compassDirectionLiveData = new MutableLiveData<String[]>();
    }//END CONSTRUCTOR

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        // get angle around the z-axis rotated
        if(event == null){return;}
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lowPassFilter(event.values.clone(), accelerometerReading);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            lowPassFilter(event.values.clone(), magnetometerReading);
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
        float heading;
        heading = calculateHeading(accelerometerReading, magnetometerReading);
        heading = convertRadtoDeg(heading);
        heading = map180to360(heading);

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
        String[] compassData = {String.valueOf(heading),  direction};
        compassDirectionLiveData.setValue(compassData);
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
                    SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);
        }
        Sensor magneticField = sensorManage.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManage.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_GAME);
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

    //0 ≤ ALPHA ≤ 1
    //smaller ALPHA results in smoother sensor data but slower updates
    public static final float ALPHA = 0.15f;

    public static float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public static float calculateHeading(float[] accelerometerReading, float[] magnetometerReading) {
        float Ax = accelerometerReading[0];
        float Ay = accelerometerReading[1];
        float Az = accelerometerReading[2];

        float Ex = magnetometerReading[0];
        float Ey = magnetometerReading[1];
        float Ez = magnetometerReading[2];

        //cross product of the magnetic field vector and the gravity vector
        float Hx = Ey * Az - Ez * Ay;
        float Hy = Ez * Ax - Ex * Az;
        float Hz = Ex * Ay - Ey * Ax;

        //normalize the values of resulting vector
        final float invH = 1.0f / (float) Math.sqrt(Hx * Hx + Hy * Hy + Hz * Hz);
        Hx *= invH;
        Hy *= invH;
        Hz *= invH;

        //normalize the values of gravity vector
        final float invA = 1.0f / (float) Math.sqrt(Ax * Ax + Ay * Ay + Az * Az);
        Ax *= invA;
        Ay *= invA;
        Az *= invA;

        //cross product of the gravity vector and the new vector H
        final float Mx = Ay * Hz - Az * Hy;
        final float My = Az * Hx - Ax * Hz;
        final float Mz = Ax * Hy - Ay * Hx;

        //arctangent to obtain heading in radians
        return (float) Math.atan2(Hy, My);
    }


    public static float convertRadtoDeg(float rad) {
        return (float) (rad / Math.PI) * 180;
    }

    //map angle from [-180,180] range to [0,360] range
    public static float map180to360(float angle) {
        return (angle + 360) % 360;
    }
}
