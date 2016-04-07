package com.example.alex.parkinsonsdiseaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button restingTremorsButton;
    Button supAndProButton;
    Button fingerTappingButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restingTremorsButton = (Button) findViewById(R.id.testButton);
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

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.testButton:
                startActivity(new Intent(MainActivity.this, RestingTremorsActivity.class));
                break;
            case R.id.supAndPro:
                startActivity(new Intent(MainActivity.this, SupinationPronationActivity.class));
                break;
            case R.id.fingerTapping:
                startActivity(new Intent(MainActivity.this, FingerTappingActivity.class));
            default:
                break;
        }
    }
}
