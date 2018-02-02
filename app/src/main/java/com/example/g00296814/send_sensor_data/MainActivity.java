package com.example.g00296814.send_sensor_data;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    public final static String IPADDRESS_TAG = "IPAddress";
    public final static String PORT_TAG = "port";

    private EditText IPAddress;
    private EditText port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IPAddress = (EditText) findViewById(R.id.IPAddressField);
        port = (EditText) findViewById(R.id.portField);


        ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION },
                1);
    }

    public void connect(View clickedButton) {

        String IPAddress_text = IPAddress.getText().toString();
        String port_text = port.getText().toString();

        Log.i(TAG, "MainActivity>> " + IPAddress_text + ":" + port_text);

        Intent intent = new Intent(this, SecondActivity.class);

        intent.putExtra(IPADDRESS_TAG, IPAddress_text);
        intent.putExtra(PORT_TAG, port_text);

        startActivity(intent);
    }
}
