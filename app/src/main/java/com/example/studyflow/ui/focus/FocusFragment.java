package com.example.studyflow.ui.focus;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import com.example.studyflow.R;
import com.example.studyflow.data.entity.Task;
import com.example.studyflow.focus.*;
import com.example.studyflow.ui.common.*;
import com.google.android.material.button.MaterialButton;
import java.util.*;

public class FocusFragment extends DataFragment {
    private FocusController controller;
    private FocusRing ring;
    private TextView status, todaySummary;
    private LinearLayout actions;
    private Spinner taskPicker;
    private EditText duration;
    private Integer selectedId;
    private int selectedMinutes=30;
    private boolean initialized;
    public static FocusFragment suggested(int taskId,int minutes){FocusFragment f=new FocusFragment();Bundle b=new Bundle();b.putInt("task",taskId);b.putInt("minutes",minutes);f.setArguments(b);return f;}

    @Override public void onViewCreated(@NonNull View v,@Nullable Bundle saved){
        controller=FocusController.get(requireContext());
        if(!initialized){
            if(saved!=null){int id=saved.getInt("selectedTask",-1);selectedId=id<0?null:id;selectedMinutes=saved.getInt("selectedMinutes",30);}
            else if(getArguments()!=null){selectedId=getArguments().getInt("task");selectedMinutes=getArguments().getInt("minutes",30);}
            initialized=true;
        }
        super.onViewCreated(v,saved);
        controller.observe().observe(getViewLifecycleOwner(),this::updateTimer);
    }

    @Override protected void render(){
        LinearLayout header=Ui.row(requireContext());body.addView(header);
        LinearLayout title=Ui.column(requireContext());header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Ui.text(title,"StudyFlow",15,true).setTextColor(Ui.BLUE);
        Ui.text(title,"Focus",30,true);
        Ui.text(title,"Stay focused and track your study time",14,false);
        LinearLayout quote=Ui.card(body);Ui.text(quote,"“Distraction today steals tomorrow’s progress.”",14,false);

        LinearLayout toggle=Ui.row(requireContext());body.addView(toggle);
        MaterialButton withTask=Ui.button(toggle,"▰  With task",()->{if(!controller.current().active()){selectedId=selectedId==null?(tasks.isEmpty()?null:tasks.get(0).getTaskId()):selectedId;refresh();}});
        MaterialButton free=Ui.button(toggle,"∞  Free focus",()->{if(!controller.current().active()){selectedId=null;refresh();}});
        Ui.weight(withTask);Ui.weight(free);
        if(selectedId==null){free.setTextColor(0xFFFFFFFF);free.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.BLUE));}
        else {withTask.setTextColor(0xFFFFFFFF);withTask.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.BLUE));}
        Ui.text(body,selectedId==null?"Focus without selecting a task. You can still track your time!":"Choose a task if you want to update its progress.",13,false);

        LinearLayout timerCard=Ui.card(body);
        ring=new FocusRing(requireContext());timerCard.addView(ring,new LinearLayout.LayoutParams(-1,Ui.dp(requireContext(),270)));
        status=Ui.text(timerCard,"",15,true);
        LinearLayout setup=Ui.card(body);Ui.heading(setup,"Focus setup",R.drawable.ic_sf_focus);
        List<Task> options=new ArrayList<>();List<String> names=new ArrayList<>();names.add("No task selected");
        int chosen=0;
        for(Task t:tasks)if(!"COMPLETED".equals(t.getStatus())){options.add(t);names.add(t.getTitle());if(selectedId!=null&&selectedId==t.getTaskId())chosen=options.size();}
        if(chosen==0)selectedId=null;
        taskPicker=Ui.spinner(setup,"Selected task (optional)",names,chosen);
        taskPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int pos,long id){if(!controller.current().active()){selectedId=pos==0?null:options.get(pos-1).getTaskId();}}});
        duration=Ui.input(setup,"Custom duration (minutes)",String.valueOf(selectedMinutes),InputType.TYPE_CLASS_NUMBER);
        LinearLayout quick=Ui.row(requireContext());setup.addView(quick);
        for(int min:new int[]{25,30,45,60,90}){MaterialButton b=Ui.button(quick,String.valueOf(min),()->{if(!controller.current().active())duration.setText(String.valueOf(min));});b.setMinWidth(0);b.setPadding(0,0,0,0);Ui.weight(b);}
        Ui.text(setup,"Enter your own duration: 25, 30, 35, 45…",12,false);

        LinearLayout stats=Ui.card(body);Ui.heading(stats,"Today",R.drawable.ic_sf_focus);
        int minutes=0,count=0;long start=Ui.startOfDay(System.currentTimeMillis());
        for(com.example.studyflow.data.entity.FocusSession s:sessions)if(s.getStartTime()>=start){minutes+=s.getDurationMinutes();count++;}
        todaySummary=Ui.text(stats,"Today: "+count+" sessions     Focused: "+minutes+" min",15,true);
        Ui.text(stats,"Small steps make big progress.",13,false);

        // Use a vertical action stack so every action remains visible on a phone-sized screen.
        actions=Ui.column(requireContext());body.addView(actions);updateTimer(controller.current());
        Ui.text(body,"Timer keeps running when you switch tabs or leave this screen. A free session is saved without changing any task.",13,false);
    }

    private String lastStatus="";
    private void updateTimer(FocusController.State s){
        if(ring==null||getView()==null)return;
        ring.update(s.active()?s.time():(duration==null?"00:00":formatInput()),s.totalMillis==0?0:1-(float)s.remainingMillis/s.totalMillis);
        status.setText(s.error.isEmpty()?(s.active()?s.taskTitle+" · "+s.status:"Ready to start"):s.error);
        if(taskPicker!=null)taskPicker.setEnabled(!s.active());
        if(duration!=null)duration.setEnabled(!s.active());
        if(actions.getChildCount()>0&&lastStatus.equals(s.status))return;
        lastStatus=s.status;actions.removeAllViews();
        if(!s.active()){
            MaterialButton start=Ui.button(actions,"▶  Start Focus",()->startTimer());stylePrimary(start);
        }else if(FocusController.SAVING.equals(s.status)){
            Ui.text(actions,"Saving session…",14,true);
        }else{
            MaterialButton toggle=Ui.button(actions,FocusController.RUNNING.equals(s.status)?"Ⅱ  Pause":"▶  Resume",()->send(FocusController.RUNNING.equals(controller.current().status)?FocusService.PAUSE:FocusService.RESUME));stylePrimary(toggle);
            MaterialButton delete=Ui.button(actions,"Delete session",()->confirmDeleteSession());
            delete.setTextColor(0xFFB3261E);
            delete.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFFB3261E));
            delete.setStrokeWidth(Ui.dp(requireContext(),1));
            Ui.button(actions,"Finish session",()->new AlertDialog.Builder(requireContext()).setTitle("Finish this session?").setMessage("The focused time will be saved and the task will be updated if selected.").setNegativeButton("Continue",null).setPositiveButton("Finish",(d,w)->send(FocusService.FINISH)).show());
        }
    }
    private String formatInput(){try{int min=Integer.parseInt(Ui.value(duration));return String.format(Locale.ROOT,"%02d:00",min);}catch(Exception e){return "00:00";}}
    private void startTimer(){try{int min=Integer.parseInt(Ui.value(duration));if(min<1||min>720)throw new IllegalArgumentException("Enter a duration from 1 to 720 minutes.");Task t=selectedId==null?null:task(selectedId);FocusService.send(requireContext(),FocusService.START,t==null?null:t.getTaskId(),t==null?"Free focus":t.getTitle(),min);}catch(Exception e){duration.setError(e.getMessage());}}
    private void confirmDeleteSession(){new AlertDialog.Builder(requireContext()).setTitle("Delete this session?").setMessage("The current timer and unsaved time will be discarded. You can choose a different task or duration afterward.").setNegativeButton("Keep session",null).setPositiveButton("Delete",(d,w)->send(FocusService.RESET)).show();}
    private void send(String action){FocusService.send(requireContext(),action,null,"",0);}
    private void stylePrimary(MaterialButton b){b.setTextColor(0xFFFFFFFF);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.BLUE));}
    @Override public void onSaveInstanceState(@NonNull Bundle out){super.onSaveInstanceState(out);out.putInt("selectedTask",selectedId==null?-1:selectedId);out.putInt("selectedMinutes",selectedMinutes);}
    @Override public void onDestroyView(){super.onDestroyView();ring=null;actions=null;lastStatus="";}
}
