package com.example.alex.parkinsonsdiseaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button restingTremorsButton;
    Button supAndProButton;
    Button fingerTappingButton;
    Button ConfigurationButton;

    long milli_per_day = 86400000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConfigurationButton = (Button) findViewById(R.id.configure);
        ConfigurationButton.setOnClickListener(this);

        restingTremorsButton = (Button) findViewById(R.id.restingtremors);
        restingTremorsButton.setOnClickListener(this);

        supAndProButton = (Button) findViewById(R.id.supAndPro);
        supAndProButton.setOnClickListener(this);

        fingerTappingButton = (Button) findViewById(R.id.fingerTapping);
        fingerTappingButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    boolean checkConfig() {
        String rootpath, folderpath, filepath;
        File F;
        long config;
        long time;

        rootpath = Environment.getExternalStorageDirectory().getPath();

        F = new File(rootpath, "Parkinsons");



        //base folder
        if(!F.exists()) {
            return true;
        }else{

            //configuration folder
            folderpath = rootpath + "/Parkinsons";
            F = new File(folderpath, "Configuration");
            if(!F.exists()){
                return true;
            }

            //configuration file
            filepath = folderpath + "/Configuration";

            F = new File(filepath);
            File file[] = F.listFiles();
            if(file.length == 0) {
                return true;
            }

            F = file[0];

            //config time
            config = (new Date(F.lastModified()).getTime());

            System.out.println("File last modified: " + config);

            //current time
            time = (new Date().getTime());

            //check if the difference is greater than 1 day
            if(time - config > milli_per_day){
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.restingtremors:
                if(checkConfig()) {
                    startActivity(new Intent(MainActivity.this, ConfigurationActivity.class));
                }else{
                    startActivity(new Intent(MainActivity.this, RestingTremorsActivity.class));
                }
                break;
            case R.id.supAndPro:
                if(checkConfig()){
                    startActivity(new Intent(MainActivity.this, ConfigurationActivity.class));
                }else {
                    startActivity(new Intent(MainActivity.this, SupinationPronationActivity.class));
                }
                break;
            case R.id.fingerTapping:
                if(checkConfig()) {
                    startActivity(new Intent(MainActivity.this, ConfigurationActivity.class));
                }else{
                    startActivity(new Intent(MainActivity.this, FingerTappingActivity.class));
                }
                break;
            case R.id.configure:
                startActivity(new Intent(MainActivity.this, ConfigurationActivity.class));
                break;
            default:
                break;
        }
    }
}
