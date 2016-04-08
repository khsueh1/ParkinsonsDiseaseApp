package com.example.alex.parkinsonsdiseaseapp;

import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;

public class Circle extends View {
    private float x = 300;
    private float y = 300;
    private int r = 150;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Random random = new Random();


    // draws circle
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(x, y, r, mPaint);
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
        // logic for random call here;
    }


    // gets random number,
    void generateRandom() {

        System.out.println("width = " + getWidth());
        System.out.println("height = " + getHeight());
        int w = getWidth() - r - 50;
        int h = getHeight()- r - 50;

        System.out.println("w = " + w);
        System.out.println("h = " + h);

        int border = r + 50;

        this.x = border + random.nextInt(w-border);
        this.y = border + random.nextInt(h-border);

        System.out.println("x = " + x);
        System.out.println("y = " + y);
    }



    // when screen is tapped, old circle removed, new circle drawn
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        generateRandom();
        invalidate();
        return super.onTouchEvent(event);
    }
}
