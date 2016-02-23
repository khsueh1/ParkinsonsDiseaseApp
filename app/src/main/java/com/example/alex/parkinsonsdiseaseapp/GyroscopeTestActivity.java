package com.example.alex.parkinsonsdiseaseapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class GyroscopeTestActivity extends AppCompatActivity implements SensorEventListener {
    private  SensorManager sm;
    private  Sensor mAcc;
    private FileOutputStream out = null;
    private Sensor gyro;
    private Calendar cal;

    protected void onResume(){
        super.onResume();
        sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
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
        setContentView(R.layout.activity_gryoscope_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);


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

                stopRecording();
            }
        });
    }

    public void onSensorChanged(SensorEvent e) {
        if(e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            cal = Calendar.getInstance(TimeZone.getDefault());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS a");
            String output = "(A: " + sdf.format(cal.getTime()).toString() + ") X = " + e.values[0] + ", Y = " + e.values[1] + ", Z = " + e.values[2] + "\n";

            try {
                out.write(output.getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            System.out.println(output);
        }
        if(e.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            cal = Calendar.getInstance(TimeZone.getDefault());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS a");
            String output = "(G: " + sdf.format(cal.getTime()).toString() + ") X = " + e.values[0] + ", Y = " + e.values[1] + ", Z = " + e.values[2] + "\n";
            try {
                out.write(output.getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println(output);
        }
    }

    protected void startRecording() throws IOException {
        if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        //File dataDir = new File(path, "Data");
        //dataDir.mkdirs(); //make if not exist
        File file = new File(path, "Test_Data.txt");
        out = new FileOutputStream(file);
    }

    protected void stopRecording(){
         sm.unregisterListener(this);

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // System.out.println(fOut.getAbsolutepath());
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
            Toast.makeText(GyroscopeTestActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
