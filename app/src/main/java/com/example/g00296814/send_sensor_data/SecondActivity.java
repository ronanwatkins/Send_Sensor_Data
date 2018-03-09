package com.example.g00296814.send_sensor_data;

import android.content.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class SecondActivity extends AppCompatActivity {

    private final String TAG = SecondActivity.class.getSimpleName();

    private Intent intentMyService;
    private BroadcastReceiver receiver;

    private ListView listView;

    private ArrayList<String> sensorList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        listView = (ListView) findViewById(R.id.mainListView);

        listAdapter = new ArrayAdapter<>(this, R.layout.row, sensorList);

        listView.setAdapter(listAdapter);

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
                Log.i(TAG, "In broadcast receiver");

                sensorList.clear();

                HashMap<String, String> sensors = (HashMap<String, String>) intent.getSerializableExtra("sensors");

                for(String sensor : sensors.keySet()) {
                    Log.i("hashmap", sensor + " " + sensors.get(sensor));
                    sensorList.add(sensor.substring(0, 1).toUpperCase() + sensor.substring(1) + ": " + sensors.get(sensor));
                }

                listAdapter.notifyDataSetChanged();
            }
        }
    }
}