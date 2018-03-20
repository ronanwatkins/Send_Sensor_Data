package com.example.g00296814.send_sensor_data;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    public final static String IPADDRESS = "IPAddress";
    public final static String PORT = "port";
    public static String PACKAGE_NAME;
    public static String FILES_DIRECTORY;
    public static String EXTERNAL_STORAGE_DIRECTORY;

    private EditText IPAddress;
    private EditText port;

    private TextView messageTextView;

    private HashMap<String, String> values;
    private SQLLiteUtils sqlLiteUtils;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        FILES_DIRECTORY = getApplicationContext().getFilesDir().getAbsolutePath();
        EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();

        sqlLiteUtils = new SQLLiteUtils();
        sqlLiteUtils.openDatabase();

        IPAddress = (EditText) findViewById(R.id.IPAddressField);
        port = (EditText) findViewById(R.id.portField);
        messageTextView = (TextView) findViewById(R.id.error_text_view);

        values = sqlLiteUtils.getData();
        if (values != null) {
            IPAddress.setText(values.get(IPADDRESS));
            port.setText(values.get(PORT));
        }

        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    public void connect(View clickedButton) {
        Log.i(TAG, "Connect Button pressed");

        String IPAddress_text = IPAddress.getText().toString();
        String port_text = port.getText().toString();

        if(values != null) {
            if (!IPAddress.getText().toString().equals(values.get(IPADDRESS)) || !port.getText().toString().equals(values.get(PORT))) {
                sqlLiteUtils.insertData(IPAddress_text, port_text);
            }
        }

        Log.i(TAG, "MainActivity>> " + IPAddress_text + ":" + port_text);

        String connectingText = getString(R.string.connecting);
        messageTextView.setText(connectingText);

        task = new Task();
        task.IP_Address = IPAddress_text;
        task.portNumber = port_text;
        task.execute(IPAddress_text ,port_text);
    }

    private class Task extends AsyncTask<String, Void, Boolean> {
        private String IP_Address;
        private String portNumber;

        @Override
        protected Boolean doInBackground(String... strings) {
            Log.i(TAG, "doInBackground");
            try {
                String URLString = "http://" + strings[0] + ":" + strings[1] + "/";

                URL url = new URL(URLString);
                Log.i(TAG, URLString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                Log.i(TAG, "waiting for response...");

                int responseCode = urlConnection.getResponseCode();
                Log.i(TAG, "response code: " + responseCode);

                return responseCode == 200;
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: " + e);
                Log.e(TAG, Log.getStackTraceString(e));

                return false;
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e);
                Log.e(TAG, Log.getStackTraceString(e));

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isValid) {
            Log.i(TAG, "onPostExecute>> ");
            Log.i(TAG, "Result: " + isValid);

            if(isValid) {
                Log.i(TAG, "its valid");

                Intent intent = new Intent(getBaseContext(), SecondActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(IPADDRESS, IP_Address);
                intent.putExtra(PORT, portNumber);
                startActivity(intent);
            } else {
                Log.i(TAG, "it's not valid");

                String errorText = getString(R.string.error);
                messageTextView.setText(errorText);
            }
        }
    }
}
