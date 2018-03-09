package com.example.g00296814.send_sensor_data;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SendSensorDataService extends Service implements SensorEventListener, LocationListener {

    private final String TAG = SendSensorDataService.class.getSimpleName();

    public static final String LIGHT_TAG = "light";
    public static final String ACCELEROMETER_TAG = "accelerometer";
    public static final String HUMIDITY_TAG = "humidity";
    public static final String PRESSURE_TAG = "pressure";
    public static final String MAGNETOMETER_TAG = "magnetic-field";
    public static final String PROXIMITY_TAG = "proximity";
    public static final String TEMPERATURE_TAG = "temperature";
    public static final String ORIENTATION_TAG = "orientation";
    public static final String GYROSCOPE_TAG = "gyroscope";
    public static final String GEO_TAG = "location";
    public static final String BATTERY_TAG = "battery";

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;

    private String IPAddress;
    private String port;

    private boolean isRunning = true;
    private boolean isFirstRun = true;

    private BufferedWriter writer;

    private HashMap<String, String> sensors;

    public SendSensorDataService() {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                sensors.put(LIGHT_TAG, event.values[0]+"");
                break;
            case Sensor.TYPE_ACCELEROMETER:
                sensors.put(ACCELEROMETER_TAG, "x=" + String.format("%.2f", event.values[0]) + ", y=" + String.format("%.2f", event.values[1]) + ", z=" + String.format("%.2f", event.values[2]));
                break;
            case Sensor.TYPE_PRESSURE:
                sensors.put(PRESSURE_TAG, event.values[0]+"");
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                sensors.put(TEMPERATURE_TAG, event.values[0]+"");
                break;
            case Sensor.TYPE_PROXIMITY:
                sensors.put(PROXIMITY_TAG, event.values[0]+"");
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                sensors.put(HUMIDITY_TAG, event.values[0]+"");
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                sensors.put(MAGNETOMETER_TAG, "x=" + String.format("%.2f", event.values[0]) + ", y=" + String.format("%.2f", event.values[1]) + ", z=" + String.format("%.2f", event.values[2]));
                break;
            case Sensor.TYPE_ORIENTATION:
                sensors.put(ORIENTATION_TAG, "yaw=" + String.format("%.2f", event.values[0]) + ", pitch=" + String.format("%.2f", event.values[1]) + ", roll=" + String.format("%.2f", event.values[2]));
                break;
            case Sensor.TYPE_GYROSCOPE:
                sensors.put(GYROSCOPE_TAG, "x=" + String.format("%.2f", event.values[0]) + ", y=" + String.format("%.2f", event.values[1]) + ", z=" + String.format("%.2f", event.values[2]));
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(TAG, "In Service");
        if (isFirstRun) {
            IPAddress = intent.getStringExtra(MainActivity.IPADDRESS_TAG);
            port = intent.getStringExtra(MainActivity.PORT_TAG);
            initializeService();
        }

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = this.registerReceiver(null, intentFilter);

        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(100);

//                        batteryValue = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) + "";
//
//                        Log.i("Battery percentage: ", "Battery percentage: " + batteryValue);
//
                        Intent intentData = new Intent("send_sensor_data.action.service");
                        intentData.putExtra("sensors", sensors);

                        JSONObject jsonObject = new JSONObject();
                        for(String sensor : sensors.keySet()) {
                            intentData.putExtra(sensor, sensors.get(sensor));
                            jsonObject.put(sensor, sensors.get(sensor));
                        }

                        sendJSON(jsonObject);

                        sendBroadcast(intentData);
                    } catch (Exception ie) {
                        Log.e(TAG, "Error: " + ie.getMessage());
                    }
                }
            }
        });
        serviceThread.start();

        return 1;
    }

    @Override
    public void onLocationChanged(Location location) {
        //GPSValue = String.format("%.2f", location.getLongitude()) + " " + String.format("%.2f", location.getLatitude());
    }

    private void initializeService() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 1, this);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //GPSValue = String.format("%.2f", location.getLongitude()) + " " + String.format("%.2f", location.getLatitude());

        sensors = new HashMap<>();

        List<Sensor> availableSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor : availableSensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        isFirstRun = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        mSensorManager.unregisterListener(this);
    }

    private void sendJSON(JSONObject jsonObject) {

        Log.i(TAG, "sendJSON >>");
        Log.i(TAG, "IPAddress: " + IPAddress);
        Log.i(TAG, "Port: " + port);
        //Log.i(TAG, "JSON: " + jsonObject.toString());

        new AsyncTask<JSONObject, Void, String>(){

            @Override
            protected String doInBackground(JSONObject... jsonObject) {
                Log.i(TAG, "In do in background");

                HttpClient client = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://"+IPAddress+":"+port+"/");
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                String paramName = "JSON";
                //String paramValue = "HI";
                String paramValue = jsonObject[0].toString();  // NOT URL-Encoded
                Log.i(TAG, "paramName: " + paramName);
                Log.i(TAG, "paramValue: " + paramValue);
                params.add(new BasicNameValuePair(paramName, paramValue));

                ResponseHandler<String> handler = new BasicResponseHandler();
                String result = "";
                Log.i(TAG, "result: "+ result);
                try {
                    Log.i(TAG, "here");
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
                    httpPost.setEntity(entity);
                    result = (client.execute(httpPost, handler));
                    Log.i(TAG, "result: "+ result);
                } catch (Exception ee) {
                    ee.printStackTrace();
                    Log.e(TAG, "Exception: "+ ee);
                    Log.i(TAG, Log.getStackTraceString(ee));
                }

                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                Log.i(TAG, "onPostExecute>> ");
                Log.i(TAG, "Result: " + s);
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, jsonObject);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

}
