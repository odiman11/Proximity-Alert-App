package com.portfolio.proximityalerts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View{
    /*Custom view of a radar for background
     * creates a custom view that defines the shape of the radar
     * creates equally distant circles for manual measurement*/

    //CONSTANTS
    public static final String TAG = "Graphic Radar";
    public static final int TEXT_SIZE = 50;
    public static final int HALF_FONT_Y_SIZE = (int)Math.ceil(TEXT_SIZE / 3);
    public static final int HALF_FONT_X_SIZE = TEXT_SIZE / 2;

    //VARIABLES
    Paint radarFrame, compass;

    //CONSTRUCTOR
    //i don't know why 3 of em
    public CompassView(Context context) {
        this(context, null);
    }

    public CompassView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //start new Paint objects
        radarFrame = new Paint();
        compass = new Paint();

        //setup frame
        radarFrame.setStyle(Paint.Style.STROKE);
        radarFrame.setStrokeWidth(10.0F);
        radarFrame.setColor(Color.GRAY);

        //setup compass
        compass.setColor(Color.GREEN);
        compass.setTextSize(TEXT_SIZE);
        compass.setStyle(Paint.Style.FILL_AND_STROKE);
        compass.setTextAlign(Paint.Align.CENTER);
        compass.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

    } //END OF CONSTRUCTOR

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        //get view size into variables
        //declare data variables
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height);
        //set size
        int center = radius /2;
        int j = center - 1;


        //DRAW COMPASS
        canvas.drawText("N", center, TEXT_SIZE, compass);
        canvas.drawText("S", center, radius - HALF_FONT_Y_SIZE, compass);
        canvas.drawText("E", radius - HALF_FONT_X_SIZE, center + HALF_FONT_Y_SIZE, compass);
        canvas.drawText("W", HALF_FONT_X_SIZE, center + HALF_FONT_Y_SIZE, compass);

        //DRAW FRAME
        //canvas.drawCircle(center, center, j, radarFrame);
    }//END OF onDraw METHOD
}
