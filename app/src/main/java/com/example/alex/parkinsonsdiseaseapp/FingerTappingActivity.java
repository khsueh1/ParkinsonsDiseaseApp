package com.example.alex.parkinsonsdiseaseapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class FingerTappingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_tapping);

        Button start = (Button) findViewById(R.id.ft_startButton);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Circle.startFlag = 1;
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }
}
