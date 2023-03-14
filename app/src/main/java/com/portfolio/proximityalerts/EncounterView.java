package com.portfolio.proximityalerts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

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
    private final int LOW = getResources().getColor(R.color.green);
    private final int MED = getResources().getColor(R.color.yellow);
    private final int HIGH = getResources().getColor(R.color.red);
    int mColor;

    Position position;
    float speed;
    String name;
    String mmsi;
    long timestamp;
    String description;


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

        this.position = message.position;
        this.speed = message.sog;
        this.mmsi = message.MMSI;
        this.name = message.name;
        this.timestamp = System.currentTimeMillis();
        this.description = "";
        this.mColor = 2;

        //setup close object
        this.encounter = new Paint();
        colorSetter(mColor);
        this.encounter.setAntiAlias(true);
        this.encounter.setStyle(Paint.Style.FILL);
        this.encounter.setStrokeWidth(1.0F);

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
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (6  * scale);

        canvas.drawCircle(width/2, height/2, radius/2 - dpAsPixels, encounter);
    }

    public void updateView(EncounterView view){
        timestamp = System.currentTimeMillis();
        position = view.position;
        speed = view.speed;
    }

    public void colorSetter(int i){
        switch (i){
            case 1:
                this.encounter.setColor(LOW);
                break;
            case 2:
                this.encounter.setColor(MED);
                break;
            case 3:
                this.encounter.setColor(HIGH);
                break;
            default:
                break;
        }
    }

    public void setEncounterColor(int lvl){
        this.mColor = lvl;
        colorSetter(lvl);
    }
    public int getEncounterColor(){
        switch (mColor){
            case 1:
                return LOW;
            case 2:
                return MED;
            case 3:
                return HIGH;
            default:
                return R.color.black;
        }
    }

    public void setEncounterDescription(String text){
        this.description = text;
    }

    public String getEncounterDescription(){return this.description;}


    @Override
    public String toString() {
        String toString = this.name + ";" + this.mmsi + ";" + this.description + ";" + this.mColor;
        return toString;
    }
}
