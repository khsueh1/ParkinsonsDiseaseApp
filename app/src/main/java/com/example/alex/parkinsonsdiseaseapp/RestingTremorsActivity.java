package com.example.alex.parkinsonsdiseaseapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Handler;

public class RestingTremorsActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sm;
    private Sensor mAcc;
    private BufferedWriter out = null;
    private Calendar cal;

    //will contain the accelerometer sensor data
    List<String> a=new ArrayList<String>();

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    protected void onResume(){
        super.onResume();
        sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause(){
        super.onPause();
        sm.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_environment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        Button email = (Button) findViewById(R.id.emailButton);

        email.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendEmail();
            }
        });

        final Button start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (start.getText().toString().equals("Start")) {
                        startRecording();
                    } else {
                        stopRecording();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onSensorChanged(SensorEvent e) {
        cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String output = sdf.format(cal.getTime()).toString();
        //record sensor data
        if(e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            output += "," + e.values[0] + "," + e.values[1] + "," + e.values[2];
            output += "\n";
            a.add(output);
        }
    }

    protected void startRecording() throws IOException {
        Button start;

        //checks to make sure the phone has the sensors that we are recording from
       if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
        }
        a.clear();

        Toast.makeText(RestingTremorsActivity.this, "The test has begun.", Toast.LENGTH_SHORT).show();
        start = (Button)findViewById(R.id.startButton);
        start.setText("Stop");
    }

    protected void stopRecording() throws IOException{
        String rootpath;
        String folderpath;
        String filepath;
        File F;
        Button start;

        sm.unregisterListener(this);
        Toast.makeText(RestingTremorsActivity.this, "The test has stopped.", Toast.LENGTH_SHORT).show();
        start = (Button)findViewById(R.id.startButton);
        start.setText("Stop");

        verifyStoragePermissions(this);

        //make directories if they do not exist
        rootpath = Environment.getExternalStorageDirectory().getPath().toString();
        F = new File(rootpath, "Parkinsons");

        if(!F.exists()) {
            F.mkdirs();
        }

        folderpath = rootpath + "/Parkinsons";
        F = new File(folderpath, "RestingTremors");
        if(!F.exists()){
            F.mkdirs();
        }

        filepath = folderpath + "/RestingTremors";

        //file name is the current date and time
        cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
        String output = sdf.format(cal.getTime()).toString();

        F = new File(filepath, output + "_A" + ".csv");
        FileOutputStream fos = new FileOutputStream(F);
        out = new BufferedWriter(new OutputStreamWriter(fos));

        for( int i = 0; i < a.size(); i++){
            try {
                out.write(a.get(i));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        try {
            if (out != null) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        a.clear();
        //go back to the main screen
        startActivity(new Intent(RestingTremorsActivity.this, MainActivity.class));
    }

    protected void sendEmail() {
        Log.i("Send email", "");
        String[] TO = {""};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "test");
        //emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email.", "");
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(RestingTremorsActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
