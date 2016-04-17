package com.example.alex.parkinsonsdiseaseapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class FingerTappingActivity extends AppCompatActivity {
    private BufferedWriter out = null;
    private Calendar cal;
    private String Afile;
    private long DURATION = 120000;
    int recording = 0;
    final static long INTERVAL = 500;
    long elapsed;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onBackPressed() {
        if (recording == 0) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_tapping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView tv =(TextView) findViewById(R.id.parkinsonsTextView);
        tv.setText("Please tap the circles as they appear on the screen. The test will stop when " + Circle.TARGET + " circles have been tapped or when " + DURATION/60000 + " minutes have passed.");

        final Button start = (Button) findViewById(R.id.ft_startButton);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    TextView tv = (TextView) findViewById(R.id.parkinsonsTextView);
                    tv.setVisibility(View.INVISIBLE);

                    TextView timer = (TextView) findViewById(R.id.timer);
                    timer.setVisibility(View.VISIBLE);

                    TextView count = (TextView) findViewById(R.id.counter);
                    count.setVisibility(View.VISIBLE);

                    elapsed = DURATION;

                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            elapsed -= INTERVAL;
                            if (elapsed <= 0 || (Circle.numCorrect >= Circle.TARGET )) {
                                this.cancel();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            stopRecording();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView timer = (TextView) findViewById(R.id.timer);
                                    final SpannableString text = new SpannableString("Stopwatch\n" + new SimpleDateFormat("mm:ss").format(new Date(elapsed)));
                                    text.setSpan(new RelativeSizeSpan(.5f), 0, 9,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    text.setSpan(new RelativeSizeSpan(1f), 9, 15,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    timer.setText(text);

                                    TextView count = (TextView) findViewById(R.id.counter);
                                    final SpannableString text2 = new SpannableString("Circles to tap\n" + (Circle.TARGET -Circle.numCorrect));
                                    text2.setSpan(new RelativeSizeSpan(.5f), 0, 14,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    text2.setSpan(new RelativeSizeSpan(1f), 14, 16,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    count.setText(text2);

                                }
                            });
                        }
                    };
                    Timer t = new Timer();
                    t.scheduleAtFixedRate(task, 0, INTERVAL);

                    startRecording();

                    View c = findViewById(R.id.cir);
                    c.setVisibility(View.VISIBLE);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

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

    private void showEmailOption() {
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
                        Circle.distances.clear();
                        recording = 0;
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

                        Afile = output + "_FT.csv";

                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(F);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        out = new BufferedWriter(new OutputStreamWriter(fos));


                        for (int i = 0; i < Circle.distances.size(); i++) {
                            try {
                                out.write(Circle.distances.get(i));
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

                        Circle.distances.clear();

                        Toast.makeText(FingerTappingActivity.this, "File saved.", Toast.LENGTH_SHORT).show();

                        showEmailOption();
                    }
                });

        helpBuilder.setNegativeButton("Discard",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        Circle.distances.clear();
                        recording = 0;
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

        Circle.distances.clear();

        Circle.numCorrect = 0;

        start = (Button) findViewById(R.id.ft_startButton);
        start.setVisibility(View.INVISIBLE);

        Toast.makeText(FingerTappingActivity.this, "The test has begun.", Toast.LENGTH_SHORT).show();

        Circle.recordflag = 1;
        recording = 1;
    }

    public void stopRecording() throws IOException {
        Button start = (Button) findViewById(R.id.ft_startButton);
        start.setVisibility(View.VISIBLE);

        TextView timer = (TextView) findViewById(R.id.timer);
        timer.setVisibility(View.INVISIBLE);

        TextView count = (TextView) findViewById(R.id.counter);
        count.setVisibility(View.INVISIBLE);

        View c = (View) findViewById(R.id.cir);
        c.setVisibility(View.INVISIBLE);

        TextView tv = (TextView) findViewById(R.id.parkinsonsTextView);
        tv.setVisibility(View.VISIBLE);


        Circle.numCorrect = 0;

        Toast.makeText(FingerTappingActivity.this, "The test has stopped.", Toast.LENGTH_SHORT).show();

        Circle.recordflag = 0;

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
