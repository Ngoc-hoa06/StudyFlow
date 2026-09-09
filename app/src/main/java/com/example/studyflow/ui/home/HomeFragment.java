package com.example.studyflow.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import com.example.studyflow.R;
import com.example.studyflow.data.entity.*;
import com.example.studyflow.focus.*;
import com.example.studyflow.ui.common.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.card.MaterialCardView;
import java.util.*;

public class HomeFragment extends DataFragment {
    private final Calendar month=Calendar.getInstance();
    private FocusRing focusRing;
    private String profileName="";
    private final Handler focusHandler=new Handler(Looper.getMainLooper());
    private final Runnable focusTicker=new Runnable(){
        @Override public void run(){
            if(!isAdded()||getView()==null)return;
            timer(FocusController.get(requireContext()).current());
            focusHandler.postDelayed(this,1000);
        }
    };
    @Override public void onViewCreated(@NonNull View v,@Nullable Bundle saved){
        if(saved!=null)month.setTimeInMillis(saved.getLong("month",System.currentTimeMillis()));
        super.onViewCreated(v,saved);
        String uid=FirebaseAuth.getInstance().getUid();
        if(uid!=null)new com.example.studyflow.data.repository.UserProfileRepository().getUserProfile(uid,
                new com.example.studyflow.data.repository.UserProfileRepository.OnProfileLoadedListener(){
                    public void onSuccess(com.example.studyflow.data.model.UserProfile profile){
                        if(getView()!=null&&profile!=null&&profile.getName()!=null){profileName=profile.getName();refresh();}
                    }
                    public void onNotFound(){}
                    public void onFailure(Exception e){}
                });
        FocusController.get(requireContext()).observe().observe(getViewLifecycleOwner(),this::timer);
        focusHandler.removeCallbacks(focusTicker);
        focusHandler.post(focusTicker);
    }
    @Override protected void render(){
        LinearLayout header=Ui.row(requireContext());body.addView(header);
        com.google.firebase.auth.FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        String name=!profileName.trim().isEmpty()?profileName:(user==null||user.getDisplayName()==null?"there":user.getDisplayName());
        TextView greet=Ui.text(header,"Hello, "+name,23,true);greet.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        com.google.android.material.button.MaterialButton profile=Ui.button(header,"Profile",()->host().navigateToProfile());profile.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
        Ui.text(body,"Today is a great day to make progress!",14,false);
        renderCalendar();
        // This action is outside the calendar card, as requested.
        Ui.button(body,"+ Add Course",()->host().navigateToSchedule());
        LinearLayout pair=Ui.row(requireContext());pair.setGravity(Gravity.TOP);pair.setBaselineAligned(false);body.addView(pair);
        LinearLayout left=Ui.column(requireContext()),right=Ui.column(requireContext());pair.addView(left,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,-2,1);rp.leftMargin=Ui.dp(requireContext(),8);pair.addView(right,rp);
        LinearLayout focus=Ui.card(left);Ui.heading(focus,"Focus",R.drawable.ic_sf_focus);
        focusRing=new FocusRing(requireContext());focus.addView(focusRing,new LinearLayout.LayoutParams(-1,Ui.dp(requireContext(),145)));focusRing.setOnClickListener(v->host().navigateToFocus());focusRing.setFocusable(true);
        Ui.text(focus,"Tap the circle to open Focus",12,false);
        Ui.text(focus,"Focus today, build tomorrow.",12,false);
        LinearLayout taskCard=Ui.card(right);Ui.heading(taskCard,"Tasks",R.drawable.ic_sf_tasks);int shown=0;
        for(Task task:tasks){if("COMPLETED".equals(task.getStatus()))continue;
            TextView row=Ui.text(taskCard,task.getTitle()+" | "+Ui.progress(task)+"%",14,true);
            row.setMinHeight(Ui.dp(requireContext(),48));row.setGravity(Gravity.CENTER_VERTICAL);row.setOnClickListener(v->taskDetail(task));row.setFocusable(true);shown++;if(shown==4)break;
        }
        if(shown==0)Ui.text(taskCard,"No active tasks.",13,false);
        Ui.button(taskCard,"+ Add Task",()->host().navigateToTasks());
        LinearLayout stats=Ui.card(body);Ui.heading(stats,"Today's progress",R.drawable.ic_sf_tasks);
        long today=Ui.startOfDay(System.currentTimeMillis());Calendar tomorrow=Calendar.getInstance();tomorrow.setTimeInMillis(today);tomorrow.add(Calendar.DAY_OF_YEAR,1);
        int minutes=0,done=0,active=0,todayPlans=0;
        for(FocusSession s:sessions)if(s.getStartTime()>=today&&s.getStartTime()<tomorrow.getTimeInMillis())minutes+=s.getDurationMinutes();
        for(Task t:tasks){if("COMPLETED".equals(t.getStatus()))done++;else if("IN_PROGRESS".equals(t.getStatus()))active++;}
        for(StudyPlan p:plans)if(p.getStudyDate()>=today&&p.getStudyDate()<tomorrow.getTimeInMillis())todayPlans++;
        Ui.text(stats,"Saved Focus today: "+minutes/60+"h "+minutes%60+"m",17,true);
        Ui.text(stats,"Completed tasks: "+done+"  ·  Active: "+active,14,false);
        Ui.text(stats,"Today's plan: "+todayPlans+" sessions",14,true);
        for(StudyPlan p:plans)if(p.getStudyDate()>=today&&p.getStudyDate()<tomorrow.getTimeInMillis()){
            Task t=task(p.getTaskId());if(t!=null)Ui.text(stats,Ui.date(p.getStudyDate())+" · "+t.getTitle()+" · "+p.getCompletedMinutes()+"/"+p.getPlannedMinutes()+" minutes",13,false);
        }
        onboarding();
        LinearLayout ai=Ui.card(body);Ui.heading(ai,"AI Study Assistant",R.drawable.ic_sf_ai);
        Ui.text(ai,"See what to do next and preview your study plan.",14,false);
        Ui.button(ai,"View today's suggestions  →",()->host().navigateToAi());
        timer(FocusController.get(requireContext()).current());
    }
    private void renderCalendar(){
        LinearLayout card=Ui.card(body);Ui.heading(card,"Class schedule",R.drawable.ic_sf_schedule);
        Ui.text(card,new java.text.SimpleDateFormat("MMMM yyyy",Locale.US).format(month.getTime()),18,true);
        LinearLayout nav=Ui.row(requireContext());card.addView(nav);
        Ui.weight(Ui.button(nav,"‹",()->{month.add(Calendar.MONTH,-1);refresh();}));nav.getChildAt(0).setContentDescription("Previous month");
        Ui.weight(Ui.button(nav,"Today",()->{month.setTimeInMillis(System.currentTimeMillis());refresh();}));
        Ui.weight(Ui.button(nav,"›",()->{month.add(Calendar.MONTH,1);refresh();}));nav.getChildAt(2).setContentDescription("Next month");
        Ui.text(card,"Swipe horizontally to see the full week; scroll vertically to view other dates.",12,false);
        HorizontalScrollView horizontal=new HorizontalScrollView(requireContext());horizontal.setFillViewport(true);card.addView(horizontal);
        LinearLayout calendar=Ui.column(requireContext());horizontal.addView(calendar,new HorizontalScrollView.LayoutParams(Ui.dp(requireContext(),686),-2));
        LinearLayout weekdays=Ui.row(requireContext());calendar.addView(weekdays);
        for(String day:new String[]{"Mon","Tue","Wed","Thu","Fri","Sat","Sun"}){TextView t=Ui.text(weekdays,day,15,true);t.setGravity(Gravity.CENTER);t.setBackgroundColor(Ui.BG);t.setLayoutParams(new LinearLayout.LayoutParams(0,Ui.dp(requireContext(),40),1));}
        // Only dates scroll vertically: the weekday row stays above the calendar viewport.
        ScrollView datesScroll=new ScrollView(requireContext());calendar.addView(datesScroll,new LinearLayout.LayoutParams(-1,Ui.dp(requireContext(),355)));
        LinearLayout dates=Ui.column(requireContext());datesScroll.addView(dates);
        Calendar first=(Calendar)month.clone();first.set(Calendar.DAY_OF_MONTH,1);int offset=Ui.day(first)-1,days=first.getActualMaximum(Calendar.DAY_OF_MONTH);int cells=((offset+days+6)/7)*7;
        Calendar date=(Calendar)first.clone();date.add(Calendar.DAY_OF_YEAR,-offset);
        for(int index=0;index<cells;){LinearLayout week=Ui.row(requireContext());week.setGravity(Gravity.TOP);week.setBaselineAligned(false);dates.addView(week);
            for(int col=0;col<7;col++,index++,date.add(Calendar.DAY_OF_YEAR,1)){
                Calendar selected=(Calendar)date.clone();LinearLayout cell=Ui.column(requireContext());cell.setMinimumHeight(Ui.dp(requireContext(),95));cell.setPadding(4,4,4,8);
                cell.setBackground(Ui.background(requireContext(),Ui.startOfDay(date.getTimeInMillis())==Ui.startOfDay(System.currentTimeMillis())?Ui.LINE:0xFFFBFCFF,6));
                week.addView(cell,new LinearLayout.LayoutParams(0,-2,1));TextView number=Ui.text(cell,String.valueOf(date.get(Calendar.DAY_OF_MONTH)),14,true);
                if(date.get(Calendar.MONTH)!=month.get(Calendar.MONTH))number.setTextColor(Ui.MUTED);
                // A class session belongs to one concrete date. Do not match by
                // weekday, otherwise a Wednesday session would repeat every week.
                for(ClassSchedule s:schedules){
                    if(s.getDateMillis()<=0L||Ui.startOfDay(s.getDateMillis())!=Ui.startOfDay(date.getTimeInMillis()))continue;
                    Course c=course(s.getCourseId());if(c==null)continue;
                    MaterialCardView event=new MaterialCardView(requireContext());event.setRadius(Ui.dp(requireContext(),6));event.setCardElevation(0);event.setCardBackgroundColor(Ui.color(c));event.setContentDescription(c.getCourseName()+", "+s.getStartTime()+" to "+s.getEndTime());event.setFocusable(true);
                    event.setOnClickListener(v->new AlertDialog.Builder(requireContext()).setTitle("Class details")
                            .setMessage("Course: "+c.getCourseName()+"\nTeacher: "+c.getLecturer()+"\nRoom: "+Ui.room(c,s)+"\nDate: "+new java.text.SimpleDateFormat("dd/MM/yyyy",Locale.US).format(selected.getTime())+"\nTime: "+s.getStartTime()+" – "+s.getEndTime()).setPositiveButton("Close",null).show());
                    LinearLayout.LayoutParams eventParams=new LinearLayout.LayoutParams(-1,Ui.dp(requireContext(),32));eventParams.topMargin=Ui.dp(requireContext(),4);cell.addView(event,eventParams);
                }
            }
        }
    }
    private void timer(FocusController.State s){if(focusRing!=null)focusRing.update(s.active()?s.time():"00:00",s.totalMillis==0?0:1-(float)s.remainingMillis/s.totalMillis);}
    private void onboarding(){
        android.content.SharedPreferences prefs=requireContext().getSharedPreferences("onboarding",0);
        String uid=FirebaseAuth.getInstance().getUid();String key="dismissed_"+uid;if(prefs.getBoolean(key,false))return;
        LinearLayout card=Ui.card(body);Ui.text(card,"Get started with StudyFlow",18,true);
        Ui.text(card,"Course → Schedule → Task → Study Plan → Focus → Progress",14,false);
        Ui.text(card,"You can also create a personal task or start a free Focus session without a Course.",13,false);
        if(courses.isEmpty())Ui.button(card,"1. Create your first Course",()->host().navigateToSchedule());
        else if(schedules.isEmpty())Ui.button(card,"2. Add a class schedule",()->host().navigateToSchedule());
        else if(tasks.isEmpty())Ui.button(card,"3. Create a Task",()->host().navigateToTasks());
        else if(plans.isEmpty())Ui.button(card,"4. Preview a Study Plan",()->host().navigateToAi());
        else Ui.button(card,"5. Start Focus",()->host().navigateToFocus());
        Ui.button(card,"Got it · Hide guide",()->{prefs.edit().putBoolean(key,true).apply();refresh();});
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out){super.onSaveInstanceState(out);out.putLong("month",month.getTimeInMillis());}
    @Override public void onDestroyView(){
        focusHandler.removeCallbacks(focusTicker);
        super.onDestroyView();
        focusRing=null;
    }
}
