package com.portfolio.proximityalerts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/*Custom view representing an object close to the phone
 * the view holds the object data inside it
 * and update its location and color according to it
 * at the moment shape of view is a circle filled with color
 *
 * METHODS:
 * updateView() - updates the instance data
 *  PARAM EncounterView - encounter view object with same MMSI
 *  RETURN - Void
*/

public class EncounterView extends View {

    Position position;
    float speed;
    String name;
    String mmsi;
    long timestamp;

    Paint encounter;

    int height;
    int width;
    int radius;
    int center;

    //constructor
    public EncounterView(Context context, Message message) {this(context, null, message);}
    public EncounterView(Context context, AttributeSet attrs, Message message) {this(context, attrs, 0, message);}
    public EncounterView(Context context, AttributeSet attrs, int defStyleAttr, Message message) {
        super(context, attrs, defStyleAttr);

        position = message.position;
        speed = message.sog;
        mmsi = message.MMSI;
        name = message.name;
        timestamp = System.currentTimeMillis();

        //setup close object
        encounter = new Paint();
        encounter.setColor(Color.RED);
        encounter.setAntiAlias(true);
        encounter.setStyle(Paint.Style.FILL);
        encounter.setStrokeWidth(1.0F);

    }
    //OVERRIDES
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the circle
        // get as big as it can
        int minh = MeasureSpec.getSize(w) + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
        int sqr = Math.min(w, h);

        setMeasuredDimension(sqr, sqr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        height = getHeight();
        width = getWidth();
        radius = Math.min(width, height);
        center = radius /2;

        canvas.drawCircle(width/2, height/2, radius/2, encounter);
    }

    public void updateView(EncounterView view){
        timestamp = System.currentTimeMillis();
        position = view.position;
        speed = view.speed;
    }
}
