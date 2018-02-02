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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SendSensorDataService extends Service implements SensorEventListener, LocationListener {

    private final String TAG = SendSensorDataService.class.getSimpleName();

    private static final String LIGHT_TAG = "light";
    private static final String ACCELEROMETER_TAG = "accelerometer";
    private static final String HUMIDITY_TAG = "humidity";
    private static final String PRESSURE_TAG = "pressure";
    private static final String MAGNETOMETER_TAG = "magnetic-field";
    private static final String PROXIMITY_TAG = "proximity";
    private static final String TEMPERATURE_TAG = "temperature";
    private static final String GEO_TAG = "location";
    private static final String BATTERY_TAG = "battery";

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;

    private String IPAddress;
    private String port;

    private boolean isRunning = true;
    private boolean isFirstRun = true;

    private BufferedWriter writer;

    private String lightValue;
    private String accelerometerValue;
    private String humidityValue;
    private String pressureValue;
    private String magneticFieldValue;
    private String proximityValue;
    private String temperatureValue;
    private String GPSValue;
    private String batteryValue;

    public SendSensorDataService() {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()) {
            case Sensor.TYPE_LIGHT:
                lightValue = event.values[0]+"";
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValue = "x=" + String.format("%.2f", event.values[0]) + ", y=" + String.format("%.2f", event.values[1]) + ", z=" + String.format("%.2f", event.values[2]);
                break;
            case Sensor.TYPE_PRESSURE:
                pressureValue = event.values[0]+"";
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                temperatureValue = event.values[0]+"";
                break;
            case Sensor.TYPE_PROXIMITY:
                proximityValue = event.values[0]+"";
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                humidityValue = event.values[0]+"";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldValue = "x=" + String.format("%.2f", event.values[0]) + ", y=" + String.format("%.2f", event.values[1]) + ", z=" + String.format("%.2f", event.values[2]);
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
                while(isRunning) {
                    try {
                        Thread.sleep(500);

                        batteryValue = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)+"";

                        Log.i("Battery precentage: ", "Battery precentage: " + batteryValue);

                        Intent intentData = new Intent("send_sensor_data.action.service");

                        intentData.putExtra(LIGHT_TAG, lightValue);
                        intentData.putExtra(ACCELEROMETER_TAG, accelerometerValue);
                        intentData.putExtra(HUMIDITY_TAG, humidityValue);
                        intentData.putExtra(PRESSURE_TAG, pressureValue);
                        intentData.putExtra(MAGNETOMETER_TAG, magneticFieldValue);
                        intentData.putExtra(PROXIMITY_TAG, proximityValue);
                        intentData.putExtra(TEMPERATURE_TAG, temperatureValue);
                        intentData.putExtra(GEO_TAG, GPSValue);
                        intentData.putExtra(BATTERY_TAG, batteryValue);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(LIGHT_TAG, lightValue);
                        jsonObject.put(ACCELEROMETER_TAG, accelerometerValue);
                        jsonObject.put(HUMIDITY_TAG, humidityValue);
                        jsonObject.put(PRESSURE_TAG, pressureValue);
                        jsonObject.put(MAGNETOMETER_TAG, magneticFieldValue);
                        jsonObject.put(PROXIMITY_TAG, proximityValue);
                        jsonObject.put(TEMPERATURE_TAG, temperatureValue);
                        jsonObject.put(GEO_TAG, GPSValue);
                        jsonObject.put(BATTERY_TAG, batteryValue);

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
        GPSValue = String.format("%.2f", location.getLongitude()) + " " + String.format("%.2f", location.getLatitude());
    }

    private void initializeService() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 1, this);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        GPSValue = String.format("%.2f", location.getLongitude()) + " " + String.format("%.2f", location.getLatitude());

        Sensor mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        Sensor mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHumidity, SensorManager.SENSOR_DELAY_NORMAL);

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

    //    private void connect(final Intent intent) throws MalformedURLException {
//        final String IPAddress = intent.getStringExtra(MainActivity.IPADDRESS_TAG);
//        final String port = intent.getStringExtra(MainActivity.PORT_TAG);
//
//        new AsyncTask<String, Long, String>() {
//            @Override
//            protected String doInBackground(String... paramNamesAndValues) {
//                String address = paramNamesAndValues[0];
//                Log.i("hey", address);
//
//                try {
//                    URL url = new URL();
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection()
//                    conn.setReadTimeout(10000);
//                    conn.setConnectTimeout(15000);
//                    conn.setRequestMethod("POST");
//                    conn.setDoInput(true);
//                    conn.setDoOutput(true);
//
//                    conn.c
//
//                    List<AbstractMap.SimpleEntry> params = new ArrayList<AbstractMap.SimpleEntry>();
//                    for (int i = 1; i < paramNamesAndValues.length - 1; i = i + 2) {
//                        String paramName = paramNamesAndValues[i];
//                        String paramValue = paramNamesAndValues[i + 1];  // NOT URL-Encoded
//                        Log.i("hey", "paramName: " + paramName);
//                        Log.i("hey", "paramValue: " + paramValue);
//                        params.add(new BasicNameValuePair(paramName, paramValue));
//                    }
//
//                    ResponseHandler<String> handler = new BasicResponseHandler();
//                    String result = "";
//                    Log.i("hey", "result: " + result);
//                    try {
//                        Log.i("hey", "here");
//                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
//                        httpPost.setEntity(entity);
//                        result = (client.execute(httpPost, handler));
//                        Log.i("hey", "result: " + result);
//                    } catch (Exception ee) {
//                        ee.printStackTrace();
//                        Log.i("hey", "error: " + ee.getMessage());
//                    }
//
//                    Log.i("hey", "result: " + result);
//                    return result;
//                } catch (Exception ee) {
//                    ee.printStackTrace();
//                }
//            }
//
//            @Override
//            protected void onPostExecute(String jsonString) {
//                try {
//                    JSONObject jsonResult = new JSONObject(jsonString);
//
//                    TextView person2 = (TextView) findViewById(R.id.p2result);
//                    person2.setText("Person 2: name: " + person2Name + "\n");
//                    person2.append("address: " + person2Address + "\n");
//                    person2.append("monthly payment: " + jsonResult.getString("formattedMonthlyPayment")+"\n");
//                    person2.append("total payments: " +jsonResult.getString("formattedTotalPayments")+"\n");
//                    person2.append("loan amount: $" + jsonResult.getString("loanAmount")+"\n");
//                    person2.append("interest rate: " + jsonResult.getString("annualInterestRateInPercent")+"%\n");
//                    person2.append("loan period: " + jsonResult.getString("loanPeriodInMonths")+"months\n");
//
//                } catch (Exception ee) {
//                    ee.printStackTrace();
//                }
//            }
//        }.execute(baseUrl, "loanInputs", inputsJson.toString());
//    }
}
