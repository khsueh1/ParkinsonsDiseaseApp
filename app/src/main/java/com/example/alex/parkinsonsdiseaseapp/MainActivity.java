package com.example.alex.parkinsonsdiseaseapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button restingTremorsButton;
    Button supAndProButton;
    Button fingerTappingButton;
    Button ConfigurationButton;

    //indicates how often configuration needs to be performed
    //currently this is set to one day (time value in milliseconds)
    long CONFIG_TIME = 86400000;

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
    public void onBackPressed()
    {
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

    /* Alerts user that configuration needs to be done */
    private void ConfigurationPopUp() {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("New Configuration Needed");
        helpBuilder.setMessage("Configuration for your device will need to be performed daily prior to starting a test.");
        helpBuilder.setPositiveButton("Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, ConfigurationActivity.class));
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.setCancelable(false);
        helpDialog.setCanceledOnTouchOutside(false);
        helpDialog.show();
    }

    //checks the last modification date of the configuration file, indicates if configuration needs
    //to be done
    boolean checkConfig() {
        String rootpath, folderpath, filepath;
        File F;
        long config; //modification time of the configuration file
        long time; //current time

        rootpath = Environment.getExternalStorageDirectory().getPath();

        F = new File(rootpath, "Parkinsons");

        //check if base folder (Parkinsons) exists
        if(!F.exists()) {
            return true;
        }else{

            //check if Configuration folder exists
            folderpath = rootpath + "/Parkinsons";
            F = new File(folderpath, "Configuration");
            if(!F.exists()){
                return true;
            }

            filepath = folderpath + "/Configuration";

            //checks if there are any files in the Configuration folder
            F = new File(filepath);
            File file[] = F.listFiles();
            if(file.length == 0) {
                return true;
            }

            F = file[0];

            //Get the modification time of the configuration file
            config = (new Date(F.lastModified()).getTime());

            //get the current time
            time = (new Date().getTime());

            //check if the difference to see if configuration needs to be done
            if(time - config > CONFIG_TIME){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.restingtremors:
                //check to see if configuration files need to be updated
                if(checkConfig()) {
                    ConfigurationPopUp();
                }else{
                    startActivity(new Intent(MainActivity.this, RestingTremorsActivity.class));
                }
                break;
            case R.id.supAndPro:
                //check to see if configuration files need to be updated
                if(checkConfig()){
                    ConfigurationPopUp();
                }else {
                    startActivity(new Intent(MainActivity.this, SupinationPronationActivity.class));
                }
                break;
            case R.id.fingerTapping:
                //finger tapping test does not use sensors, so it does not need the configuration file
                startActivity(new Intent(MainActivity.this, FingerTappingActivity.class));
                break;
            case R.id.configure:
                startActivity(new Intent(MainActivity.this, ConfigurationActivity.class));
                break;
            default:
                break;
        }
    }
}
