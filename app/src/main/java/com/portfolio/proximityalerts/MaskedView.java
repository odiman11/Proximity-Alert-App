package com.portfolio.proximityalerts;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

public class MaskedView extends View {

    public MaskedView(Context context) {
        super(context);
    }

    public MaskedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

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
    protected void onDraw(Canvas canvas) {
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
        // create a new Paint object to apply the mask
        Paint paint = new Paint();

        // set the color to transparent to create a mask
        paint.setColor(Color.TRANSPARENT);

        // set the Xfermode to DST_IN to apply the mask
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        // draw a rectangle with the paint object to apply the mask
        canvas.drawCircle(center, center, j, paint);
    }
}
