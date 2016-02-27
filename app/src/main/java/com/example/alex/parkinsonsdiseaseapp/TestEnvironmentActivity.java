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
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TestEnvironmentActivity extends AppCompatActivity implements SensorEventListener {
    private  SensorManager sm;
    private  Sensor mAcc;
    private Sensor gyro;
    private Sensor magnetic;

    private FileOutputStream out = null;

    private Calendar cal;


    long starttime = 0L;
    long timeInMilliseconds = 0L;
    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    int lsecs = -1;
    int lmins = -1;
    int lmilliseconds = -1;

    float[] acceleration = new float[3];
    float[] gyroscope = new float[3];
    float[] magneticField = new float[3];



    Handler handler = new Handler();


    protected void onResume(){
        super.onResume();
        sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
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
                stopRecording();
            }
        });
    }

    public void onSensorChanged(SensorEvent e) {
        //these 3 lines are temporary right now
        //i am recording the current time to compare against the stopwatch
        cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS a");
        String output = sdf.format(cal.getTime()).toString();

        //initialize the last mins, last seconds, lasts milliseconds
        if((lmins == -1) && (lsecs == -1) && (lmilliseconds == -1)){
            lmins = mins;
            lsecs = secs;
            lmilliseconds = milliseconds;

            //record sensor data
            if(e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acceleration[0] = e.values[0];
                acceleration[1] = e.values[1];
                acceleration[2] = e.values[2];
            }
            if(e.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                gyroscope[0] = e.values[0];
                gyroscope[1] = e.values[1];
                gyroscope[2] = e.values[2];
            }
            if(e.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticField[0] = e.values[0];
                magneticField[1] = e.values[1];
                magneticField[2] = e.values[2];
            }
        }else if((lmins == mins) && (lsecs == secs) && (lmilliseconds == milliseconds)) {
            //the given time is the same as the previous recorded time

            //do not print yet, perhaps there are other sensors that have not been recorded yet
            //we want to only print one line which has all the possible sensor at that time
            // do not want this to happen:
            //  time(0:0:1), .56, .34, .24,  - ,  - ,  - , -  , -  , -
            //  time(0:0:1),  - ,  - ,  - , .12, .23, .43, -  , -  , -
            //  time(0:0:1),  - ,  - ,  - ,  - ,  - ,  - , .67, .78, .89
            // we want this
            //  time(0:0:1), .56, .34, .24, .12, .23, .43, .67, .78, .89
            //record sensor data
            if(e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acceleration[0] = e.values[0];
                acceleration[1] = e.values[1];
                acceleration[2] = e.values[2];
            }
            if(e.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                gyroscope[0] = e.values[0];
                gyroscope[1] = e.values[1];
                gyroscope[2] = e.values[2];
            }
            if(e.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                magneticField[0] = e.values[0];
                magneticField[1] = e.values[1];
                magneticField[2] = e.values[2];
            }
        }else {
            //the given time is different from the previous recorded time
            //this means that all the possible sensor data should be recorded now
            //lets print out the data now

            output += ", " + lmins  + ":" + lsecs + ":" + lmilliseconds;

            for(float i: acceleration){
                if(i == -1){
                    output += ", -";
                }else{
                    output += ", " + i;
                }
            }

            for(float i: gyroscope){
                if(i == -1){
                    output += ", -";
                }else{
                    output += ", " + i;
                }
            }
            for(float i: magneticField){
                if(i == -1){
                    output += ", -\n";
                }else{
                    output += ", " + i + "\n";
                }
            }

            System.out.println(output);
            try {
                out.write(output.getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            lmins = mins;
            lsecs = secs;
            lmilliseconds = milliseconds;
        }
    }

    protected void startRecording() throws IOException {
        //checks to make sure the phone has the sensors that we are recording from
        if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if(sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sm.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        }

        //right now we will save the files to Documents
        //** we should change file save destination to the application folder later
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        //File dataDir = new File(path, "Data");
        //dataDir.mkdirs(); //make if not exist

        //file name is the current date and time
        cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS a");
        String output = sdf.format(cal.getTime()).toString();
        File file = new File(path, output + ".txt");
        out = new FileOutputStream(file);

        //start the stopwatch
        starttime = SystemClock.uptimeMillis();
        //want no delay in the stopwatch
        handler.postDelayed(updateTimer, 0);
    }

    protected void stopRecording(){
         sm.unregisterListener(this);

        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //reset stopwatch information
        starttime = 0L;
        timeInMilliseconds = 0L;
        secs = 0;
        mins = 0;
        milliseconds = 0;
        lsecs = -1;
        lmins = -1;
        lmilliseconds = -1;
        handler.removeCallbacks(updateTimer);

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
            Toast.makeText(TestEnvironmentActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public Runnable updateTimer = new Runnable() {
        public void run() {
          //  System.out.println("in here");
            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;
            secs = (int) (timeInMilliseconds / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (timeInMilliseconds % 1000);
            //do not delay
            handler.postDelayed(this, 0);
        }
    };






}
