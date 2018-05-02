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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.*;
import java.util.*;

public class SendSensorDataService extends Service implements SensorEventListener, LocationListener {

    private final String TAG = SendSensorDataService.class.getSimpleName();

    public static final String LIGHT = "light";
    public static final String ACCELEROMETER = "accelerometer";
    public static final String HUMIDITY = "humidity";
    public static final String PRESSURE = "pressure";
    public static final String MAGNETOMETER = "magnetic-field";
    public static final String PROXIMITY = "proximity";
    public static final String TEMPERATURE = "temperature";
    public static final String ORIENTATION = "orientation";
    public static final String GYROSCOPE = "gyroscope";
    public static final String GEO = "location";
    public static final String BATTERY = "battery";

    private final int INTERVAL = 200;

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;

    private String IPAddress;
    private String port;

    private boolean isRunning = true;
    private boolean isFirstRun = true;

    private final Object lock = new Object();

    private HashMap<String, String> sensors;

    public SendSensorDataService() {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        String value;

        synchronized (lock) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_LIGHT:
                    sensors.put(LIGHT, event.values[0] + "");
                    break;
                case Sensor.TYPE_PRESSURE:
                    sensors.put(PRESSURE, event.values[0] + "");
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    sensors.put(TEMPERATURE, event.values[0] + "");
                    break;
                case Sensor.TYPE_PROXIMITY:
                    sensors.put(PROXIMITY, event.values[0] + "");
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    sensors.put(HUMIDITY, event.values[0] + "");
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    value = "[\"";
                    value += String.format("%.2f", event.values[0]) + "\", \"";
                    value += String.format("%.2f", event.values[1]) + "\", \"";
                    value += String.format("%.2f", event.values[2]) + "\"]";
                    sensors.put(ACCELEROMETER, value);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    value = "[\"";
                    value += String.format("%.2f", event.values[0]) + "\", \"";
                    value += String.format("%.2f", event.values[1]) + "\", \"";
                    value += String.format("%.2f", event.values[2]) + "\"]";
                    sensors.put(MAGNETOMETER, value);
                    break;
                case Sensor.TYPE_ORIENTATION:
                    value = "[\"";
                    value += String.format("%.2f", event.values[0]) + "\", \"";
                    value += String.format("%.2f", event.values[1]) + "\", \"";
                    value += String.format("%.2f", event.values[2]) + "\"]";
                    sensors.put(ORIENTATION, value);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    value = "[\"";
                    value += String.format("%.2f", event.values[0]) + "\", \"";
                    value += String.format("%.2f", event.values[1]) + "\", \"";
                    value += String.format("%.2f", event.values[2]) + "\"]";
                    sensors.put(GYROSCOPE, value);
                    break;
            }
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
            IPAddress = intent.getStringExtra(MainActivity.IPADDRESS);
            port = intent.getStringExtra(MainActivity.PORT);
            initializeService();
        }

        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(INTERVAL);

                        synchronized (lock) {

                            Intent intentData = new Intent("send_sensor_data.action.service");
                            intentData.putExtra("sensors", sensors);

                            JSONObject jsonObject = new JSONObject();
                            for (String sensor : sensors.keySet()) {
                                intentData.putExtra(sensor, sensors.get(sensor));

                                if (sensor.equals(ORIENTATION) || sensor.equals(ACCELEROMETER) || sensor.equals(MAGNETOMETER) || sensor.equals(GYROSCOPE) || sensor.equals(GEO)) {
                                    jsonObject.put(sensor, new JSONArray(sensors.get(sensor)));
                                } else
                                    jsonObject.put(sensor, sensors.get(sensor));
                            }

                            sendJSON(jsonObject);
                            sendBroadcast(intentData);

                            sensors = new HashMap<>();
                        }

                    } catch (JSONException jse) {
                        Log.e(TAG, "JSON Exception: " + jse.getMessage());
                        Log.e(TAG, Log.getStackTraceString(jse));
                    } catch (InterruptedException ie) {
                        Log.e(TAG, "Interrupted Exception: " + ie.getMessage());
                        Log.e(TAG, Log.getStackTraceString(ie));
                    }
                }
            }
        });
        serviceThread.start();

        return 1;
    }

    @Override
    public void onLocationChanged(Location location) {
        String GPSValue = "[" + String.format("%.4f", location.getLatitude()) + ", " + String.format("%.4f", location.getLongitude()) + "]";
        sensors.put(GEO, GPSValue);
    }

    private void initializeService() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 1, this);

        sensors = new HashMap<>();

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        final Intent batteryStatus = this.registerReceiver(null, intentFilter);
        String batteryValue = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)+"";
        sensors.put(BATTERY, batteryValue);

        final Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            String GPSValue = "[" + String.format("%.4f", location.getLatitude()) + ", " + String.format("%.4f", location.getLongitude()) + "]";
            sensors.put(GEO, GPSValue);
            Log.i("GPS", GPSValue);
        }

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

        new AsyncTask<JSONObject, Void, String>() {

            @Override
            protected String doInBackground(JSONObject... jsonObject) {
                Log.i(TAG, "In do in background");

                HttpClient client = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://" + IPAddress + ":" + port + "/");

                List<NameValuePair> params = new ArrayList<>();

                String paramName = "JSON";
                String paramValue = jsonObject[0].toString();  // NOT URL-Encoded
                Log.i(TAG, "paramName: " + paramName);
                Log.i(TAG, "paramValue: " + paramValue);
                params.add(new BasicNameValuePair(paramName, paramValue));

                ResponseHandler<String> handler = new BasicResponseHandler();
                String result = "";
                Log.i(TAG, "result: " + result);
                try {
                    Log.i(TAG, "here");
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
                    httpPost.setEntity(entity);
                    result = (client.execute(httpPost, handler));
                    Log.i(TAG, "result: " + result);
                } catch (UnknownHostException uhe) {
                    Log.e(TAG, "UnknownHostException: " + uhe.getMessage());
                    Log.e(TAG, "Message: " + Log.getStackTraceString(uhe));
                } catch (IOException ioe) {
                    Log.e(TAG, "Exception: " + ioe.getMessage());
                    Log.e(TAG, "Message: " + Log.getStackTraceString(ioe));
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.i(TAG, "onPostExecute>> ");
                Log.i(TAG, "Result: " + result);
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
