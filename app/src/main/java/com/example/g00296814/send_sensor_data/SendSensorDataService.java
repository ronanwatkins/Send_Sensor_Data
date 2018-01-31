package com.example.g00296814.send_sensor_data;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SendSensorDataService extends Service implements SensorEventListener {

    public static final String LIGHT_TAG = "light";
    public static final String ACCELEROMETER_TAG = "accelerometer";
    public static final String HUMIDITY_TAG = "humidity";
    public static final String PRESSURE_TAG = "pressure";
    public static final String MAGNETOMETER_TAG = "magnetometer";
    public static final String PROXIMITY_TAG = "proximity";
    public static final String TEMPERATURE_TAG = "temperature";

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;

    private Sensor mLight;
    private Sensor mAccelerometer;
    private Sensor mHumidity;
    private Sensor mPressure;
    private Sensor mMagnetometer;
    private Sensor mProximity;
    private Sensor mTemperature;

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
        Log.i("hey", "In Service");
        if (isFirstRun) {
            initializeService(intent);
        }

        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(isRunning) {
                    try {
                        Thread.sleep(5000);

                        Log.i("hey", "In Service Loop");

                        Intent intentData = new Intent("send_sensor_data.action.service");

                        intentData.putExtra(LIGHT_TAG, lightValue);
                        intentData.putExtra(ACCELEROMETER_TAG, accelerometerValue);
                        intentData.putExtra(HUMIDITY_TAG, humidityValue);
                        intentData.putExtra(PRESSURE_TAG, pressureValue);
                        intentData.putExtra(MAGNETOMETER_TAG, magneticFieldValue);
                        intentData.putExtra(PROXIMITY_TAG, proximityValue);
                        intentData.putExtra(TEMPERATURE_TAG, pressureValue);

                        sendBroadcast(intentData);
                    } catch (Exception ie) {
                        Log.e("hey", "Error: " + ie.getMessage());
                    }
                }
            }
        });
        serviceThread.start();

        return 1;
    }

    private void initializeService(final Intent intent) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHumidity, SensorManager.SENSOR_DELAY_NORMAL);

        try {
            writer = establishConnection(intent);
            isFirstRun = false;
        } catch (IOException io) {
            Log.e("hey", io.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        mSensorManager.unregisterListener(this);
    }

    private BufferedWriter establishConnection(final Intent intent) throws IOException{
        final String IPAddress = intent.getStringExtra(MainActivity.IPADDRESS_TAG);
        final String port = intent.getStringExtra(MainActivity.PORT_TAG);

        URL url = new URL(port, IPAddress, null);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

        return writer;
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
