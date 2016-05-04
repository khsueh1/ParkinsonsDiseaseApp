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
import android.net.Uri;
import android.os.Bundle;
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
    Supination Pronation test data will be in a folder which contains an accelerometer and
    gyroscope csv file

    csv output format for accelerometer and gyroscope data:
    timestamp , x, y, z
 */
public class SupinationPronationActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sm;
    private Sensor mAcc;
    private Sensor gyro;
    private BufferedWriter out = null;
    private Calendar cal;
    private String Afile;   //accelerometer test file name
    private String Gfile;   //gyroscope test file name
    int recording = 0;      //name of the subfolder
    String date;

    //will contain the accelerometer and gyroscope sensor data
    List<String> a = new ArrayList<>();
    List<String> g = new ArrayList<>();

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Explicitly check to see if there are read/write permissions
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
    public void onBackPressed()
    {
        //allow exit of activity only if a test is not recording
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supinationpronation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        String output = sdf.format(cal.getTime());
        //record the timestamp and accelerometer values from the x, y, and z axis
        if(e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            output += "," + e.values[0] + "," + e.values[1] + "," + e.values[2];
            output += "\n";
            a.add(output);
        }

        //record the timestamp and gyroscope values from the x, y, and z axis
        if(e.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            output += "," + e.values[0] + "," + e.values[1] + "," + e.values[2];
            output += "\n";
            g.add(output);
        }
    }

    /*
    Creates email pop up after the file popup. If the user selects, go to the sendEmail() function.
    If user selects no, go back to testing screen.
     */
    private void emailPopUp(){
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Email");
        helpBuilder.setMessage("Would you like to email the test data?");
        helpBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmail();
                    }
                });

        helpBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog and clear the stored accelerometer and gyroscope sensor data
                        a.clear();
                        g.clear();
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.setCancelable(false);
        helpDialog.setCanceledOnTouchOutside(false);
        helpDialog.show();
    }

    /*
    This method creates the popup for saving the file. If the user selects yes, it then proceeds to create
    the folders for the application and test if needed and the file for this run of the test. Then goes to
    emailPopUp for the second pop up.
     */
    private void filePopUp() {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("New Supination Pronation File");
        helpBuilder.setMessage("Save supination pronation file?");
        helpBuilder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        //make directories if they do not exist
                        String rootpath = Environment.getExternalStorageDirectory().getPath();

                        //checks to see if base folder "Parkinsons" exists
                        File F = new File(rootpath, "Parkinsons");
                        if (!F.exists()) {
                            F.mkdirs();
                        }

                        //checks to see if "SupinationPronation" folder exists
                        String folderpath = rootpath + "/Parkinsons";
                        F = new File(folderpath, "SupinationPronation");
                        if (!F.exists()) {
                            F.mkdirs();
                        }

                        String filepath = folderpath + "/SupinationPronation";

                        //sub-folders for each test will be created to contain the gyroscope and accelerometer files
                        cal = Calendar.getInstance(TimeZone.getDefault());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                        String output = sdf.format(cal.getTime());
                        //record the subfolder name
                        date = output;

                        //create the sub-folder, name by current date and time
                        F = new File(filepath, output);
                        if (!F.exists()) {
                            F.mkdirs();
                        }

                        filepath += "/" + output;

                        //file name is the current date and time
                        F = new File(filepath, output + "_SP_A.csv");

                        //record the accelerometer supination pronation file name
                        Afile = output + "_SP_A.csv";

                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(F);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        out = new BufferedWriter(new OutputStreamWriter(fos));

                        //write the accelerometer data to the .csv file
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

                        //clear the stored accelerometer data
                        a.clear();

                        //file name is the current date and time
                        F = new File(filepath, output + "_SP_G.csv");

                        //record the gyroscope supination pronation file name
                        Gfile = output + "_SP_G.csv";

                        fos = null;
                        try {
                            fos = new FileOutputStream(F);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        out = new BufferedWriter(new OutputStreamWriter(fos));

                        //write the gyroscope data to the .csv file
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

                        //clear the stored gyroscope data
                        g.clear();

                        Toast.makeText(SupinationPronationActivity.this, "File saved.", Toast.LENGTH_SHORT).show();

                        //show option to email this test data
                        emailPopUp();
                    }
                });

        helpBuilder.setNegativeButton("Discard",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        //clear the stored accelerometer and gyroscope data
                        a.clear();
                        g.clear();
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.setCancelable(false);
        helpDialog.setCanceledOnTouchOutside(false);
        helpDialog.show();
    }

    /*
    Method called when the start button is pressed. Changes the start button to a stop button and puts
    the test in the recording state, where we begin to record accelerometer and gyroscope values.
     */
    protected void startRecording() throws IOException {
        Button start;

        //checks to make sure the phone has the sensors that we are recording from
        if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
        }

        if(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
        }

        a.clear();
        g.clear();

        Toast.makeText(SupinationPronationActivity.this, "The test has begun.", Toast.LENGTH_SHORT).show();
        start = (Button)findViewById(R.id.startButton);
        start.setText("Stop");

        //test had started to record
        recording = 1;
    }

    /*
    Method is called when the test has been completed, resetting the interface and variables.
     */
    protected void stopRecording() throws IOException{
        sm.unregisterListener(this);
        Toast.makeText(SupinationPronationActivity.this, "The test has stopped.", Toast.LENGTH_SHORT).show();
        Button start = (Button) findViewById(R.id.startButton);
        start.setText("Start");

        //test has stopped recording
        recording = 0;

        //check to see if we have write permissions to create the file on internal storage
        verifyStoragePermissions(this);

        //show popup option for saving the test file
        filePopUp();
    }

    /*
    Method is called when the user selects YES to the email popup. Automatically attaches file created from
    this test and creates an appropriate subject line. Finishes activity after sending.
     */
    protected void sendEmail() {
        String[] TO = {""};
        String[] CC = {""};
        ArrayList <Uri> uris = new ArrayList<>();
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Supination Pronation Test Data");

        String rootpath = Environment.getExternalStorageDirectory().getPath();

        uris.add(Uri.parse("file://" + rootpath + "/Parkinsons/SupinationPronation/" + date + "/" + Afile));
        uris.add(Uri.parse("file://" + rootpath + "/Parkinsons/SupinationPronation/" + date + "/" + Gfile));
        uris.add(Uri.parse("file://" + rootpath + "/Parkinsons/Configuration/Configuration_A.csv"));
        uris.add(Uri.parse("file://" + rootpath + "/Parkinsons/Configuration/Configuration_G.csv"));

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        //emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(SupinationPronationActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
