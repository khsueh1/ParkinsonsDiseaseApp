package com.example.alex.parkinsonsdiseaseapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

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

public class FingerTappingActivity extends Activity {
    private BufferedWriter out = null;
    private Calendar cal;
    private String Afile;

    //will contain the accelerometer sensor data
    List<String> a = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_tapping);
        final Button start = (Button) findViewById(R.id.ft_startButton);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (start.getText().toString().equals("Start")) {
                        startRecording();
                        TextView tv = (TextView) findViewById(R.id.parkinsonsTextView);
                        tv.setVisibility(View.GONE);

                        View c = (View) findViewById(R.id.cir);
                        c.setVisibility(View.VISIBLE);

                    } else {
                        stopRecording();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

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

    private void showEmailOption(){
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Email");
        helpBuilder.setMessage("Would you like to email the test data?");
        helpBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        //save the data
                        sendEmail();
                    }
                });

        helpBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog

                        a.clear();
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.setCancelable(false);
        helpDialog.setCanceledOnTouchOutside(false);
        helpDialog.show();

    }

    private void showSimplePopUp() {

        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("New Finger Tapping File");
        helpBuilder.setMessage("Save finger tapping file?");
        helpBuilder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        //save the data
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
                        F = new File(folderpath, "FingerTapping");
                        if (!F.exists()) {
                            F.mkdirs();
                        }

                        filepath = folderpath + "/FingerTapping";

                        //file name is the current date and time
                        cal = Calendar.getInstance(TimeZone.getDefault());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                        String output = sdf.format(cal.getTime());

                        F = new File(filepath, output + "_FT.csv");

                        Afile = output + "_RT_A.csv";

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

                        Toast.makeText(FingerTappingActivity.this, "File saved.", Toast.LENGTH_SHORT).show();

                        showEmailOption();
                    }
                });

        helpBuilder.setNegativeButton("Discard",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        a.clear();
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.setCancelable(false);
        helpDialog.setCanceledOnTouchOutside(false);
        helpDialog.show();
    }

    protected void startRecording() throws IOException {
        Button start;

        a.clear();
        Toast.makeText(FingerTappingActivity.this, "The test has begun.", Toast.LENGTH_SHORT).show();

        start = (Button) findViewById(R.id.ft_startButton);
        //start.setText("Stop");
        start.setVisibility(View.INVISIBLE);
    }

    protected void stopRecording() throws IOException {
        Button start;

        Toast.makeText(FingerTappingActivity.this, "The test has stopped.", Toast.LENGTH_SHORT).show();
        start = (Button) findViewById(R.id.ft_startButton);
        //start.setText("Start");
        start.setVisibility(View.VISIBLE);

        verifyStoragePermissions(this);

        showSimplePopUp();
    }

    protected void sendEmail() {
        String[] TO = {""};
        String[] CC = {""};
        String rootpath;
        ArrayList<Uri> uris = new ArrayList<>();
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Finger Tapping Test Data");

        rootpath = Environment.getExternalStorageDirectory().getPath();

        uris.add(Uri.parse("file://" + rootpath + "/Parkinsons/FingerTapping/" + Afile));

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        //emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(FingerTappingActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
