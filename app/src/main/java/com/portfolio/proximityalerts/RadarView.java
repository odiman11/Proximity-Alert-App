package com.portfolio.proximityalerts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

public class RadarView extends View{
    /*Custom view of a radar for background
     * creates a custom view that defines the shape of the radar
     * creates equally distant circles for manual measurement*/

    //CONSTANTS
    public static final String TAG = "Graphic Radar";

    //VARIABLES
    Paint circle, radarFrame, align, background, mask;
    Drawable arrow = ResourcesCompat.getDrawable(getResources() ,R.drawable.ic_baseline_keyboard_arrow_up_24, null);
    Bitmap backgroundBitmap;
    Shader backgroundShader;
    Matrix backgroundMatrix;

    //CONSTRUCTOR
    //i dont know why 3 of em
    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Load the image from resources
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.radar_background);

        // Create a shader from the bitmap
        backgroundShader = new BitmapShader(backgroundBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        // Create a matrix to translate the shader
        backgroundMatrix = new Matrix();
        backgroundMatrix.setTranslate(-800, -800);

        backgroundShader.setLocalMatrix(backgroundMatrix);




        //start new Paint objects
        circle = new Paint();
        radarFrame = new Paint();
        align = new Paint();
        background = new Paint();
        mask = new Paint();

        //setup background
        background.setColor(getResources().getColor(R.color.black));
        background.setStyle(Paint.Style.FILL);
        background.setAntiAlias(true);

        //setup circles
        circle.setColor(Color.GREEN);
        circle.setAntiAlias(true);
        circle.setStyle(Paint.Style.STROKE);
        circle.setStrokeWidth(1.0F);

        //setup frame
        radarFrame.setStyle(Paint.Style.STROKE);
        radarFrame.setStrokeWidth(10.0F);
        radarFrame.setColor(Color.GRAY);

        //setup lines
        align.setColor(Color.RED);

        //setup mask
        mask.setColor(getResources().getColor(R.color.transparent));
        //mask.setStyle(Paint.Style.FILL);
        mask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

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

        int centerHeight = 80;

        //DRAW MASK
        canvas.drawCircle(center, center, j, mask);

        //DRAW CIRCLE
        canvas.drawCircle(center, center, j, background);
        canvas.drawCircle(center, center, j, circle);
        canvas.drawCircle(center, center, (j * 3) / 4, circle);
        canvas.drawCircle(center, center, j >> 1, circle);
        canvas.drawCircle(center, center, j >> 2, circle);

        //DRAW ALIGN LINE
        canvas.drawLine(0,center,radius, center, align);
        canvas.drawLine(center,0,center, radius, align);

        //DRAW CENTER
        //canvas.drawCircle(center, center, center/20, radarCenter);
        arrow.setBounds(center - centerHeight, center - centerHeight, center + centerHeight, center + centerHeight);
        arrow.draw(canvas);

        //DRAW FRAME
        //canvas.drawCircle(center, center, j, radarFrame);


    }//END OF onDraw METHOD
}
