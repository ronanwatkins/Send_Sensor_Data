package com.example.g00296814.send_sensor_data;

import android.content.*;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SecondActivity extends AppCompatActivity {

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

        Log.i("hey", "SecondActivity>> " + IPAddress + ":" + port);

        IntentFilter filter = new IntentFilter("send_sensor_data.action.service");
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);

        Log.i("hey", "Registered Receiver");
        intentMyService = new Intent(this, SendSensorDataService.class);
        //intentMyService.putExtra(MainActivity.IPADDRESS_TAG, IPAddress);
        //intentMyService.putExtra(MainActivity.IPADDRESS_TAG, port);
        startService(intentMyService);
        Log.i("hey", "Started Service");
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //if(intent.getAction().equals("send_sensor_data.action.service")) {
                Log.i("hey", "In broadcast reciver");

                Log.i("hey", intent.getStringExtra(SendSensorDataService.ACCELEROMETER_TAG));
                Log.i("hey", intent.getStringExtra(SendSensorDataService.HUMIDITY_TAG));
                Log.i("hey", intent.getStringExtra(SendSensorDataService.PRESSURE_TAG));

            //}
        }
    }
}