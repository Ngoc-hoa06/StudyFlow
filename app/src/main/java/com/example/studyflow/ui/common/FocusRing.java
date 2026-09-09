package com.example.studyflow.ui.common;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** A scalable ring: elapsed fraction, remaining time and accessible spoken label. */
public class FocusRing extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private String time="00:00";private float fraction;
    public FocusRing(Context c){super(c);setMinimumHeight(Ui.dp(c,120));setContentDescription("Focus 00:00");}
    public void update(String text,float progress){time=text;fraction=Math.max(0,Math.min(1,progress));setContentDescription("Focus, remaining "+text);invalidate();}
    @Override protected void onMeasure(int w,int h){int width=MeasureSpec.getSize(w);int desired=Math.min(width,Ui.dp(getContext(),230));setMeasuredDimension(resolveSize(desired,w),resolveSize(desired,h));}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float side=Math.min(getWidth(),getHeight()),cx=getWidth()/2f,cy=getHeight()/2f,r=side*.40f;
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(5,side*.05f));paint.setColor(0xFFDDE3F2);c.drawCircle(cx,cy,r,paint);
        paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(Ui.BLUE);c.drawArc(new RectF(cx-r,cy-r,cx+r,cy+r),-90,360*fraction,false,paint);
        paint.setStyle(Paint.Style.FILL);paint.setTextAlign(Paint.Align.CENTER);paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));paint.setTextSize(side*.19f);paint.setColor(Ui.INK);c.drawText(time,cx,cy+side*.05f,paint);
        paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(side*.075f);paint.setColor(Ui.MUTED);c.drawText("Focus",cx,cy+side*.20f,paint);
    }
}
