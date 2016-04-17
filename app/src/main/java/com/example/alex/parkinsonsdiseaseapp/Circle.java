package com.example.alex.parkinsonsdiseaseapp;

import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Circle extends View {
    private float x = 300;
    private float y = 300;
    private int r = 150;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Random random = new Random();
    public static ArrayList<Double> distances;
    int flag = 1;
    static int startFlag = 0;
    //Data cData;

   /* public class Data {
        int count;
        ArrayList <Double> singleDist;

        public Data() {
            this.count = 0;
            this.singleDist = new ArrayList<Double>();
        }
    }*/

    // draws circle
    @Override
    protected void onDraw(Canvas canvas) {
        if (startFlag == 1) {
            super.onDraw(canvas);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.RED);
            canvas.drawCircle(x, y, r, mPaint);
        }
    }

    // constructors
    public Circle(Context context) {
        super(context);
        init();
    }

    public Circle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Circle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {

    }


    // gets random number,,
    void generateRandom() {

        int w = getWidth() - r - 50;
        int h = getHeight() - r - 50;

        int border = r + 50;

        this.x = border + random.nextInt(w-border);
        this.y = border + random.nextInt(h-border);
    }



    // when screen is tapped, old circle removed, new circle drawn
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (flag == 1) {
            flag = 0;
            distances = new ArrayList<Double>();
           // cData = new Data();

        }

        if (isInsideCircle(event.getX(), event.getY())) {
            //distances.add(cData.singleDist);
            //cData = new Data();

            generateRandom();
            invalidate();
        }

        distance(event.getX(), event.getY());

        return super.onTouchEvent(event);
    }


    void distance(double newX, double newY) {
        distances.add(Math.sqrt(Math.pow(x - newX, 2) + Math.pow(y - newY, 2)));
    }

    public boolean isInsideCircle(float xPoint, float yPoint) {
        float dx = (x - xPoint);
        float dxPow = (float) Math.pow(dx, 2);
        float dy = (y - yPoint);
        float dyPow = (float) Math.pow(dy, 2);
        float radPow = (float) Math.pow(r, 2);
        return ((dxPow + dyPow) <= radPow);
    }
}
