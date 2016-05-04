package com.example.alex.parkinsonsdiseaseapp;

import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
/*
Circle class used to draw random circles to the screen in the finger tapping activity.
 */
public class Circle extends View {
    private float x;
    private float y;
    private float lastx = 150;
    private float lasty = 150;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Random random = new Random();
    public static int recordflag = 0;

    //Array that holds the file output.
    public static ArrayList<String> distances = new ArrayList<>();

    //Scale to adjust the circle depending on the resolution of the phone. Do not adjust this.
    final float scale = getResources().getDisplayMetrics().density;

    //Radius of the circle. Adjust this to change size of circle.
    int r = (int) (50 * scale + 0.5f);

    //number of circles the user has tapped.
    public static int numCorrect = 0;

    //Number of circles the user must hit to finish test.
    public static int TARGET = 15;

    /*
    Draws the circle to the screen, and is called when the constructor is called at the start of
    the activity and when invalidate() is called in OnTouchEvent().
    */
    @Override
    protected void onDraw(Canvas canvas) {

        if (x == 0 || y == 0) {
            generateRandom();
        }

        /*
        Generates random x and y coordinates for the circle that is at least the diameter distance away
        from the previous circle.
        */
        while( (Math.abs(x - (lastx ))  <= (2*r)) && (Math.abs(y - (lasty ))  <= (2*r))){
            generateRandom();
        }

        lastx = x;
        lasty = y;
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        System.out.println("x: " + x);
        System.out.println("y: " + y);
        System.out.println("r: " + r);
        canvas.drawCircle(x, y, r, mPaint);
    }

    // Constructors for the circle, Java forces this to be here.
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

    // Gets random number for x and y coordinates for new Circle. Used in OnDraw.
    void generateRandom() {
          this.x =  random.nextInt(getWidth() - (int) (2*r)) + r;
          this.y = random.nextInt(getHeight() - (int) (2*r)) + r;
    }

    /*
    Called when user taps the screen. If user hits the cirlce, generate a new one and record the event,
    if they missed, just record the value.
    */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(recordflag == 1) {
            distance(event.getX(), event.getY());

            if (isInsideCircle(event.getX(), event.getY())) {

                numCorrect++;
                invalidate();
            }
        }
        return super.onTouchEvent(event);
    }

    //This method generates the output that is stored in the global vector.
    void distance(double newX, double newY) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String output;

        output = sdf.format(cal.getTime());
        output += "," + newX + "," + newY + "," + x + "," + y + "," + Math.sqrt(Math.pow(x - newX, 2) + Math.pow(y - newY, 2)) + "\n";

        if(numCorrect <= TARGET) {
            distances.add(output);
        }
    }

    //This method checks if the user taps within the circle's bounds.
    public boolean isInsideCircle(float xPoint, float yPoint) {
        float dx = (x - xPoint);
        float dxPow = (float) Math.pow(dx, 2);
        float dy = (y - yPoint);
        float dyPow = (float) Math.pow(dy, 2);
        float radPow = (float) Math.pow(r, 2);
        return ((dxPow + dyPow) <= radPow);
    }
}
