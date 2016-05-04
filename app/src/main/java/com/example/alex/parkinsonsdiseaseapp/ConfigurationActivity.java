package com.example.alex.parkinsonsdiseaseapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
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

/*
Configuration test for the accelerometer and gyroscope of the phone. Must be conducted with the phone
lying on a flat surface for the duration of the test. Used to eliminate noise produced by the sensors
when performing data analysis
 */
public class ConfigurationActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sm;
    private Sensor mAcc;
    private Sensor gyro;
    private BufferedWriter out = null;
    int recording = 0;

    //List Containing Accelerometer readings and gyroscope readings, that will be printed to seperate files.
    List<String> a=new ArrayList<>();
    List<String> g = new ArrayList<>();

    // Gain storage permissions, allowing us to read and write files to the device.
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

    @Override
    //Method is called when the back button is presses, which simply finishes the activity.
    public void onBackPressed()
    {
        if(recording == 0) {
            finish();
        }
    }

    protected void onResume(){
        super.onResume();
        sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause(){
        super.onPause();
        sm.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    /*
    Method is called when the activity is created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Button start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    startRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    Method is called anytime there is a sensor change. Depending on which sensor, the data is recorded in
    a string and stored in the global vectors.
     */
    public void onSensorChanged(SensorEvent e) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String output = sdf.format(cal.getTime());

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

    /*
    Method is called after the initial creation of the activity. Sets up the accelerometer and gyroscope
    for reading. For this test, it also initializes a countdown timer, currently set to 5 seconds, before
    the activity stops.
     */
    protected void startRecording() throws IOException {
        recording = 1;
        a.clear();
        g.clear();

        //checks to make sure the phone has the sensors that we are recording from
        if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //Sample the sensor at the fastest speed possible
            sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
        }

        if(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            //Sample the sensor at the fastest speed possible
            sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
        }

        Toast.makeText(ConfigurationActivity.this, "Configuration is in progress.", Toast.LENGTH_SHORT).show();
        Toast.makeText(ConfigurationActivity.this, "Configuration is in progress.", Toast.LENGTH_SHORT).show();

        //Timer for config activity. Change first number (in MS) to adjust time.
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            //Called when timer is complete. We call stop recording, which will clean up the test and print to file.
            public void onFinish() {
                try {
                    stopRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

       Button start = (Button)findViewById(R.id.startButton);
       start.setVisibility(View.INVISIBLE);
    }

    //Deletes a directory and all files in that directory. Used in stop recording.
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    /*
    This method creates the popup for saving the file. If the user selects yes, it then proceeds to create
    the folders for the application and test if needed, and the file for this run of the test.
     */
    private void filePopUp() {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("New Configuration File");
        helpBuilder.setMessage("Save configuration file?");
        helpBuilder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String rootpath;
                        String folderpath;
                        String filepath;
                        File F;

                        //make directories if they do not exist
                        rootpath = Environment.getExternalStorageDirectory().getPath();
                        F = new File(rootpath, "Parkinsons");

                        if (!F.exists()) {
                            F.mkdirs();
                        }

                        folderpath = rootpath + "/Parkinsons";
                        F = new File(folderpath, "Configuration");
                        if (!F.exists()) {
                            F.mkdirs();
                        }else {
                            deleteRecursive(F);
                            F.mkdirs();
                        }

                        filepath = folderpath + "/Configuration";

                        F = new File(filepath, "Configuration_A" + ".csv");

                        //Establishing the output stream to write to file.
                        FileOutputStream fos;
                        try {
                            fos = new FileOutputStream(F);
                            out = new BufferedWriter(new OutputStreamWriter(fos));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        //write accelerometer data values to file from global vector
                        for (int i = 0; i < a.size(); i++) {
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

                        //Establishing gyroscope file output stream.
                        F = new File(filepath, "Configuration_G" + ".csv");
                        try {
                            fos = new FileOutputStream(F);
                            out = new BufferedWriter(new OutputStreamWriter(fos));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        //write gyroscope data values to file from global vector
                        for( int i = 0; i < g.size(); i++){
                            try {
                                out.write(g.get(i));
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

                        g.clear();

                        Toast.makeText(ConfigurationActivity.this, "File saved.", Toast.LENGTH_SHORT).show();

                        //go back to the main screen
                        startActivity(new Intent(ConfigurationActivity.this, MainActivity.class));
                    }
                });
        //Called when user doesn't save data. Cleans up data structures to reset test.
        helpBuilder.setNegativeButton("Discard",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Button start;

                        a.clear();
                        g.clear();

                        start = (Button)findViewById(R.id.startButton);
                        start.setVisibility(View.VISIBLE);
                        recording = 0;

                        Toast.makeText(ConfigurationActivity.this, "File not saved.", Toast.LENGTH_SHORT).show();
                    }
                });

        //Displays the popup, and force the user to select an answer before continuing.
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.setCancelable(false);
        helpDialog.setCanceledOnTouchOutside(false);
        helpDialog.show();
    }

    /*
    Called when the test is over, cleans up sensors and calls the popup message to determine if
    the user wants to save.
    */
    protected void stopRecording() throws IOException {
        sm.unregisterListener(this);
        Toast.makeText(ConfigurationActivity.this, "Configuration has finished.", Toast.LENGTH_SHORT).show();

        verifyStoragePermissions(this);

        filePopUp();
    }
}
