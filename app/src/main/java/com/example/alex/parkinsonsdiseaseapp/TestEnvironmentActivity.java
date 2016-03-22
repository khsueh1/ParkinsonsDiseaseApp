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
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class TestEnvironmentActivity extends AppCompatActivity implements SensorEventListener {
    private  SensorManager sm;
    private  Sensor mAcc;
    private Sensor gyro;
    private Sensor magnetic;

    private BufferedWriter out = null;

    private Calendar cal;

    List<String> a=new ArrayList<String>();
    List<String> g=new ArrayList<String>();

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
        sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
       // sm.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        System.out.println("in onResume");
    }

    protected void onPause(){
        super.onPause();
        sm.unregisterListener(this);
        System.out.println("in onpause");

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

        final Context context = getApplicationContext();
        final CharSequence text = "Stop Button Pressed";
        final int duration = Toast.LENGTH_SHORT;


        Button email = (Button) findViewById(R.id.emailButton);

        email.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendEmail();
            }
        });

        Button start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    startRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button stop = (Button) findViewById(R.id.stopButton);
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    stopRecording();
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

        if(e.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            output += "," + e.values[0] + "," + e.values[1] + "," + e.values[2];
            output += "\n";
            g.add(output);
        }
    }

    protected void startRecording() throws IOException {
        //checks to make sure the phone has the sensors that we are recording from
       if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
        }
       if(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
        }

        //File dataDir = new File(path, "Data");
        //dataDir.mkdirs(); //make if not exist
    }

    protected void stopRecording() throws IOException{
        sm.unregisterListener(this);

        verifyStoragePermissions(this);

        //right now we will save the files to Documents
        //** we should change file save destination to the application folder later
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //file name is the current date and time
        cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
        String output = sdf.format(cal.getTime()).toString();
        File file = new File(path, output + "_A" + ".csv");
        FileOutputStream fos = new FileOutputStream(file);
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

        File file1 = new File(path, output + "_G" + ".csv");
        FileOutputStream fos1 = new FileOutputStream(file1);
        BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(fos1));
        for( int i = 0; i < g.size(); i++){
            try {
                out1.write(g.get(i));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        try {
            if (out1 != null) {
                out1.flush();
                out1.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            Toast.makeText(TestEnvironmentActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
