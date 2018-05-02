package com.example.g00296814.send_sensor_data;

import android.content.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {
    private final String TAG = SecondActivity.class.getSimpleName();

    private Intent intentMyService;
    private IntentFilter filter;
    private BroadcastReceiver receiver;

    private ListView listView;

    private ArrayList<String> sensorList = new ArrayList<>(11);
    private ArrayAdapter<String> listAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        for (int i = 0; i < 11; i++)
            sensorList.add("");

        listView = (ListView) findViewById(R.id.mainListView);

        listAdapter = new ArrayAdapter<>(this, R.layout.row, sensorList);

        listView.setAdapter(listAdapter);

        Intent intent = getIntent();
        String IPAddress = intent.getStringExtra(MainActivity.IPADDRESS);
        String port = intent.getStringExtra(MainActivity.PORT);

        Log.i(TAG, "SecondActivity>> " + IPAddress + ":" + port);

        filter = new IntentFilter("send_sensor_data.action.service");
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);

        Log.i(TAG, "Registered Receiver");
        intentMyService = new Intent(this, SendSensorDataService.class);

        intentMyService.putExtra(MainActivity.IPADDRESS, IPAddress);
        intentMyService.putExtra(MainActivity.PORT, port);
        Log.i(TAG, "IPAddress: " + IPAddress);
        Log.i(TAG, "port: " + port+"");

        startService(intentMyService);

        Log.i(TAG, "Started Service");
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopService(intentMyService);
        unregisterReceiver(receiver);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startService(intentMyService);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        stopService(intentMyService);
        unregisterReceiver(receiver);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        private HashMap<String, Integer> location() {
            HashMap<String, Integer> values = new HashMap<>();
            values.put(SendSensorDataService.LIGHT,0);
            values.put(SendSensorDataService.ACCELEROMETER,1);
            values.put(SendSensorDataService.MAGNETOMETER ,2);
            values.put(SendSensorDataService.ORIENTATION ,3);
            values.put(SendSensorDataService.GYROSCOPE ,4);
            values.put(SendSensorDataService.PROXIMITY ,5);
            values.put(SendSensorDataService.GEO ,6);
            values.put(SendSensorDataService.BATTERY ,7);
            values.put(SendSensorDataService.HUMIDITY , 8);
            values.put(SendSensorDataService.PRESSURE,9);
            values.put(SendSensorDataService.TEMPERATURE ,10);

            return values;
        }

        private HashMap<String, Integer> locations = location();

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("send_sensor_data.action.service")) {
                Log.i(TAG, "In broadcast receiver");

                HashMap<String, String> sensors = (HashMap<String, String>) intent.getSerializableExtra("sensors");

                for(String sensor : sensors.keySet()) {
                    Log.i("hashmap", sensor + " " + sensors.get(sensor));

                    if(sensor.startsWith(SendSensorDataService.GYROSCOPE) || sensor.startsWith(SendSensorDataService.ACCELEROMETER) || sensor.startsWith(SendSensorDataService.MAGNETOMETER)) {
                        Log.i("parse", sensors.get(sensor));

                        try {
                            JSONArray jsonArray = new JSONArray(sensors.get(sensor));
                            StringBuilder value = new StringBuilder("x=").append(jsonArray.getString(0));
                            value.append(", y=").append(jsonArray.getString(1));
                            value.append(", z=").append(jsonArray.getString(2));

                            Log.d("DEBUG", ""+locations.get(sensor));

                            sensorList.set(locations.get(sensor), sensor.substring(0, 1).toUpperCase() + sensor.substring(1) + ": " + value);
                        } catch (JSONException jse) {
                            Log.e(TAG, "Error: " + jse.getMessage());
                        }
                    } else if (sensor.startsWith(SendSensorDataService.ORIENTATION)) {
                        try {
                            JSONArray jsonArray = new JSONArray(sensors.get(sensor));
                            StringBuilder value = new StringBuilder("yaw=").append(jsonArray.getString(0));
                            value.append(", pitch=").append(jsonArray.getString(1));
                            value.append(", roll=").append(jsonArray.getString(2));

                            sensorList.set(locations.get(sensor), sensor.substring(0, 1).toUpperCase() + sensor.substring(1) + ": " + value);
                        } catch (JSONException jse) {
                            Log.e(TAG, "Error: " + jse.getMessage());
                        }
                    } else if (sensor.startsWith(SendSensorDataService.GEO)) {
                        try {
                            JSONArray jsonArray = new JSONArray(sensors.get(sensor));
                            StringBuilder value = new StringBuilder("Latitude=").append(jsonArray.getString(0));
                            value.append(", Longitude=").append(jsonArray.getString(1));

                            sensorList.set(locations.get(sensor), sensor.substring(0, 1).toUpperCase() + sensor.substring(1) + ": " + value);
                        } catch (JSONException jse) {
                            Log.e(TAG, "Error: " + jse.getMessage());
                        }
                    } else {
                        sensorList.set(locations.get(sensor), sensor.substring(0, 1).toUpperCase() + sensor.substring(1) + ": " + sensors.get(sensor));
                    }
                }

                listAdapter.notifyDataSetChanged();
            }
        }
    }
}