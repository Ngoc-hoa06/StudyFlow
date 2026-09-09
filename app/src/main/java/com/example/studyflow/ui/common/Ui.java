package com.example.studyflow.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.*;
import com.example.studyflow.data.entity.*;

public final class Ui {
    // Shared StudyFlow visual language: soft blue background, deep navy text,
    // blue/purple primary actions, and white cards with a very light lavender border.
    public static final int INK=0xFF17351A,MUTED=0xFF617B63,BLUE=0xFF2F7D20,PURPLE=0xFF6B55C9,BG=0xFFF2F9EC,LINE=0xFFD7EBCB;
    public static final String[] COLORS={"#DDF1D2","#E7DCF8","#D9EFD8","#F8DCE8","#FFF0C9","#CDEEDB"};
    public static final String[] COLOR_NAMES={"Leaf green","Lavender","Sage green","Pastel pink","Cream yellow","Mint green"};
    public static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    public static LinearLayout column(Context c){LinearLayout x=new LinearLayout(c);x.setOrientation(LinearLayout.VERTICAL);return x;}
    public static LinearLayout row(Context c){LinearLayout x=new LinearLayout(c);x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    public static GradientDrawable background(Context c,int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));d.setStroke(dp(c,1),LINE);return d;}
    public static TextView text(LinearLayout p,String text,int size,boolean bold){
        TextView t=new TextView(p.getContext());t.setText(text);t.setTextSize(size);t.setTextColor(bold?INK:MUTED);t.setLineSpacing(dp(p.getContext(),2),1);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(p.getContext(),6);p.addView(t,lp);return t;
    }
    public static void heading(LinearLayout p,String label,int icon){TextView t=text(p,label,20,true);t.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);t.setCompoundDrawablePadding(dp(p.getContext(),10));}
    public static LinearLayout card(LinearLayout parent){
        Context c=parent.getContext();MaterialCardView card=new MaterialCardView(c);card.setRadius(dp(c,20));card.setCardElevation(0);card.setStrokeColor(LINE);card.setStrokeWidth(dp(c,1));card.setCardBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(c,14);parent.addView(card,lp);
        LinearLayout body=column(c);body.setPadding(dp(c,14),dp(c,12),dp(c,14),dp(c,16));card.addView(body);return body;
    }
    public static MaterialButton button(LinearLayout parent,String label,Runnable action){
        MaterialButton b=new MaterialButton(parent.getContext());b.setText(label);b.setAllCaps(false);b.setTextColor(BLUE);b.setTextSize(14);b.setCornerRadius(dp(parent.getContext(),14));b.setBackgroundTintList(ColorStateList.valueOf(LINE));b.setMinHeight(dp(parent.getContext(),48));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(parent.getContext(),8);parent.addView(b,lp);b.setOnClickListener(v->action.run());return b;
    }
    public static void weight(View v){v.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));}
    public static EditText input(LinearLayout parent,String label,String value,int type){
        text(parent,label,13,true);EditText e=new EditText(parent.getContext());e.setText(value==null?"":value);e.setTextSize(16);e.setTextColor(INK);e.setSingleLine(true);e.setInputType(type);e.setMinHeight(dp(parent.getContext(),48));parent.addView(e,new LinearLayout.LayoutParams(-1,-2));return e;
    }
    public static Spinner spinner(LinearLayout parent,String label,List<String> choices,int selected){
        if(!label.isEmpty())text(parent,label,13,true);Spinner s=new Spinner(parent.getContext());ArrayAdapter<String>a=new ArrayAdapter<>(parent.getContext(),android.R.layout.simple_spinner_item,choices);a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);s.setAdapter(a);s.setSelection(Math.max(0,selected));s.setMinimumHeight(dp(parent.getContext(),48));parent.addView(s,new LinearLayout.LayoutParams(-1,-2));return s;
    }
    public static ScrollView form(LinearLayout body){ScrollView s=new ScrollView(body.getContext());body.setPadding(dp(body.getContext(),20),dp(body.getContext(),8),dp(body.getContext(),20),dp(body.getContext(),16));s.addView(body);return s;}
    public static String value(EditText e){return e.getText().toString().trim();}
    public static int minutes(String value){String[] t=value.split(":");if(t.length!=2)throw new IllegalArgumentException("Enter time in HH:mm format.");int h=Integer.parseInt(t[0]),m=Integer.parseInt(t[1]);if(h<0||h>23||m<0||m>59)throw new IllegalArgumentException("Invalid time.");return h*60+m;}
    public static String time(int m){return String.format(Locale.ROOT,"%02d:%02d",m/60,m%60);}
    public static int day(Calendar c){return (c.get(Calendar.DAY_OF_WEEK)+5)%7+1;}
    public static long startOfDay(long now){Calendar c=Calendar.getInstance();c.setTimeInMillis(now);c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.getTimeInMillis();}
    public static int progress(Task t){
        if("COMPLETED".equals(t.getStatus()))return 100;
        long minutes=t.getCompletedMinutes();
        if("IN_PROGRESS".equals(t.getStatus())&&t.getProgressStartedAt()>0)
            minutes+=(System.currentTimeMillis()-t.getProgressStartedAt())/60000L;
        return (int)Math.max(0,Math.min(100,100L*minutes/Math.max(1,t.getEstimatedMinutes())));
    }
    public static int progressPermille(Task t){
        if("COMPLETED".equals(t.getStatus()))return 1000;
        long elapsed=t.getCompletedMinutes()*60000L;
        if("IN_PROGRESS".equals(t.getStatus())&&t.getProgressStartedAt()>0)
            elapsed+=Math.max(0,System.currentTimeMillis()-t.getProgressStartedAt());
        long total=Math.max(1,t.getEstimatedMinutes())*60000L;
        return (int)Math.max(0,Math.min(1000,1000L*elapsed/total));
    }
    public static String status(Task t){return "COMPLETED".equals(t.getStatus())?"Completed":"IN_PROGRESS".equals(t.getStatus())?"In Progress":"Not Started";}
    public static String priority(Task t){return "HIGH".equals(t.getPriority())?"High":"LOW".equals(t.getPriority())?"Low":"Medium";}
    public static int color(Course course){
        if(course==null||course.getColor()==null)return Color.parseColor(COLORS[0]);
        try{if(course.getColor().startsWith("#"))return Color.parseColor(course.getColor());}catch(Exception ignored){}
        {
            switch(course.getColor().toUpperCase(Locale.ROOT)){case "BLUE":return Color.parseColor(COLORS[1]);case "PURPLE":return Color.parseColor(COLORS[2]);case "PINK":case "RED":return Color.parseColor(COLORS[3]);case "YELLOW":return Color.parseColor(COLORS[4]);case "ORANGE":return Color.parseColor(COLORS[5]);default:return Color.parseColor(COLORS[0]);}
        }
    }
    public static String room(Course c,ClassSchedule s){return s.getRoom()==null||s.getRoom().trim().isEmpty()?c.getDefaultRoom():s.getRoom();}
    public static String date(long millis){return millis<=0?"No deadline":new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.US).format(new Date(millis));}
}
