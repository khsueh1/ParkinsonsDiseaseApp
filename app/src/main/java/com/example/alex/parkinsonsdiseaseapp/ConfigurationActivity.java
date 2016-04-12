package com.example.alex.parkinsonsdiseaseapp;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ConfigurationActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sm;
    private Sensor mAcc;
    private Sensor gyro;
    private BufferedWriter out = null;
    private Calendar cal;

    //will contain the accelerometer sensor data
    List<String> a=new ArrayList<>();
    List<String> g = new ArrayList<>();

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

    public void onSensorChanged(SensorEvent e) {
        cal = Calendar.getInstance(TimeZone.getDefault());
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

    protected void startRecording() throws IOException {
        Button start;

        a.clear();
        g.clear();

        //checks to make sure the phone has the sensors that we are recording from
        if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
        }

        if(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
        }

        Toast.makeText(ConfigurationActivity.this, "The test has begun.", Toast.LENGTH_SHORT).show();

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                try {
                    stopRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        start = (Button)findViewById(R.id.startButton);
        start.setVisibility(View.INVISIBLE);
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }



    protected void stopRecording() throws IOException {
        sm.unregisterListener(this);
        Toast.makeText(ConfigurationActivity.this, "The test has stopped.", Toast.LENGTH_SHORT).show();

        verifyStoragePermissions(this);

        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup_element, null);

        TextView tv1 = (TextView) popupView.findViewById(R.id.textView1);
        tv1.setText("Save Configuration test data?");

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        Button yes = (Button) popupView.findViewById(R.id.yes);
        yes.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String rootpath;
                String folderpath;
                String datepath;
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
                }else{
                    deleteRecursive(F);
                    F.mkdirs();
                }

                datepath = folderpath + "/Configuration";

                //file name is the current date and time
                cal = Calendar.getInstance(TimeZone.getDefault());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                String output = sdf.format(cal.getTime());

                F = new File(datepath, output + "_C_A" + ".csv");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(F);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                out = new BufferedWriter(new OutputStreamWriter(fos));

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

                //file name is the current date and time
                F = new File(datepath, output + "_C_G" + ".csv");
                fos = null;
                try {
                    fos = new FileOutputStream(F);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                out = new BufferedWriter(new OutputStreamWriter(fos));

                for (int i = 0; i < g.size(); i++) {
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

        Button no = (Button) popupView.findViewById(R.id.no);
        no.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Button start;

                start = (Button) findViewById(R.id.startButton);
                start.setVisibility(View.VISIBLE);

                popupWindow.dismiss();
                Toast.makeText(ConfigurationActivity.this, "File not saved.", Toast.LENGTH_SHORT).show();
                a.clear();
                g.clear();
            }
        });

        popupWindow.showAtLocation(this.findViewById(R.id.configuration), Gravity.CENTER, 0, 0);
    }
}
