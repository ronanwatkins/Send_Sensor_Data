package com.example.g00296814.send_sensor_data;

import android.content.*;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SecondActivity extends AppCompatActivity {

    private final String TAG = SecondActivity.class.getSimpleName();

    private ComponentName service;
    private Intent intentMyService;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Intent intent = getIntent();
        String IPAddress = intent.getStringExtra(MainActivity.IPADDRESS_TAG);
        String port = intent.getStringExtra(MainActivity.PORT_TAG);

        Log.i(TAG, "SecondActivity>> " + IPAddress + ":" + port);

        IntentFilter filter = new IntentFilter("send_sensor_data.action.service");
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);

        Log.i(TAG, "Registered Receiver");
        intentMyService = new Intent(this, SendSensorDataService.class);

        intentMyService.putExtra(MainActivity.IPADDRESS_TAG, IPAddress);
        intentMyService.putExtra(MainActivity.PORT_TAG, port);
        Log.i(TAG, "IPAddress: " + IPAddress);
        Log.i(TAG, "port: " + port+"");

        startService(intentMyService);
        Log.i(TAG, "Started Service");
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("send_sensor_data.action.service")) {
                Log.i(TAG, "In broadcast reciver");

//                Log.i("hey", "Accel value: " + intent.getStringExtra(SendSensorDataService.ACCELEROMETER_TAG));
//                Log.i("hey", "Temp value: " + intent.getStringExtra(SendSensorDataService.TEMPERATURE_TAG));
//                Log.i("hey", "Humidity value: " + intent.getStringExtra(SendSensorDataService.HUMIDITY_TAG));
//                Log.i("hey", "Pressure value: " + intent.getStringExtra(SendSensorDataService.PRESSURE_TAG));
//                Log.i("hey", "Proximity value: " + intent.getStringExtra(SendSensorDataService.PROXIMITY_TAG));
//                Log.i("hey", "Magnetometer value: " + intent.getStringExtra(SendSensorDataService.MAGNETOMETER_TAG));
//                Log.i("hey", "Light value: " + intent.getStringExtra(SendSensorDataService.LIGHT_TAG));

            }
        }
    }
}