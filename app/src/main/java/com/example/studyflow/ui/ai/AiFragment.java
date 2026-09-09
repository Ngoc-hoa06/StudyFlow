package com.example.studyflow.ui.ai;

import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import com.example.studyflow.R;
import com.example.studyflow.data.entity.*;
import com.example.studyflow.scheduler.SmartScheduler;
import com.example.studyflow.ui.common.*;
import java.util.*;

public class AiFragment extends DataFragment {
    private SmartScheduler.Analysis preview;
    private String fingerprint="";private boolean analyzing,applying;
    private String currentFingerprint(){return SmartScheduler.fingerprint(tasks,schedules,sessions,plans);}
    @Override protected void render(){
        Ui.heading(body,"AI Study Assistant",R.drawable.ic_sf_ai);
        Ui.text(body,"Automatic suggestions · Local SmartScheduler",13,false);
        Ui.text(body,"Analyze → Preview Plan → Apply Plan",16,true);
        LinearLayout sources=Ui.card(body);Ui.text(sources,"Data used",18,true);
        Ui.text(sources,tasks.size()+" tasks · "+schedules.size()+" class sessions · "+sessions.size()+" Focus sessions",14,false);
        Ui.text(sources,"Deadlines, priorities, estimated durations, task progress, class schedule, and Focus history are used to recommend session lengths.",14,false);
        LinearLayout method=Ui.card(body);Ui.text(method,"How is the plan created?",18,true);
        Ui.text(method,"• Rank work by priority, deadline, and remaining effort.\n• Use study hours from 08:00 to 22:00, up to 4 hours per day, across 14 days.\n• Suggest 25–50 minute sessions based on Focus history with 5-minute breaks.\n• Keep sessions with progress and preview new sessions before applying them.",14,false);
        Ui.button(body,analyzing?"Analyzing…":"Analyze plan",this::analyze).setEnabled(!analyzing&&!applying);
        if(!plans.isEmpty()){
            LinearLayout saved=Ui.card(body);Ui.text(saved,"Saved plan",18,true);
            int shown=0;for(StudyPlan p:plans){if(p.getStudyDate()<Ui.startOfDay(System.currentTimeMillis()))continue;Task t=task(p.getTaskId());Ui.text(saved,Ui.date(p.getStudyDate())+" · "+(t==null?"Task":t.getTitle())+" · "+p.getCompletedMinutes()+"/"+p.getPlannedMinutes()+" minutes",14,false);if(++shown==8)break;}
        }
        if(preview==null){Ui.text(body,"Press Analyze to receive suggestions. Analysis does not change your data.",14,false);if(tasks.isEmpty())Ui.button(body,"Add Task",()->host().navigateToTasks());return;}
        boolean stale=!fingerprint.equals(currentFingerprint());
        if(stale)Ui.text(body,"Your data changed. Analyze again before applying the plan.",14,true);
        LinearLayout suggestion=Ui.card(body);Ui.text(suggestion,"What should you do next?",20,true);
        if(preview.suggestions.isEmpty())Ui.text(suggestion,"No suitable tasks found.",14,false);
        for(int i=0;i<Math.min(4,preview.suggestions.size());i++){
            Task t=preview.suggestions.get(i);Ui.text(suggestion,(i+1)+". "+t.getTitle(),18,true);
            Ui.text(suggestion,"Why: "+preview.reasons.get(t.getTaskId()),14,false);
            int proposed=Math.min(preview.sessionMinutes,Math.max(1,t.getEstimatedMinutes()-t.getCompletedMinutes()));
            Ui.text(suggestion,"Suggested session: "+proposed+" minutes · "+Ui.date(t.getDeadline()),13,false);
            Ui.button(suggestion,"Start Suggested Focus",()->host().navigateToSuggestedFocus(t.getTaskId(),proposed)).setEnabled(!stale);
        }
        Ui.text(suggestion,"You can change the task or duration before starting.",12,false);
        if(!preview.warnings.isEmpty()){
            LinearLayout warnings=Ui.card(body);Ui.text(warnings,"Information to add or check",18,true);
            for(String warning:new LinkedHashSet<>(preview.warnings))Ui.text(warnings,"• "+warning,14,false);
            for(Map.Entry<Integer,Integer> e:preview.unplannedMinutes.entrySet()){Task t=task(e.getKey());Ui.text(warnings,(t==null?"Task":t.getTitle())+": "+e.getValue()+" minutes could not be scheduled",13,true);}
        }
        LinearLayout plan=Ui.card(body);Ui.text(plan,"Preview plan · "+preview.plans.size()+" new sessions",19,true);
        if(preview.plans.isEmpty())Ui.text(plan,"No suitable sessions could be created. Check the information above.",14,false);
        for(int i=0;i<Math.min(8,preview.plans.size());i++){
            StudyPlan p=preview.plans.get(i);Task t=task(p.getTaskId());
            Ui.text(plan,"● "+Ui.date(p.getStudyDate())+" – "+new java.text.SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(p.getStudyDate()+p.getPlannedMinutes()*60000L)),13,false);
            Ui.text(plan,(t==null?"Task":t.getTitle())+" · "+p.getPlannedMinutes()+" minutes",16,true);
        }
        if(preview.plans.size()>8)Ui.button(plan,"View full preview",()->{
            StringBuilder text=new StringBuilder();for(StudyPlan p:preview.plans){Task t=task(p.getTaskId());text.append(Ui.date(p.getStudyDate())).append(" · ").append(t==null?"Task":t.getTitle()).append(" · ").append(p.getPlannedMinutes()).append(" minutes\n\n");}
            new AlertDialog.Builder(requireContext()).setTitle("Preview plan").setMessage(text.toString()).setPositiveButton("Close",null).show();
        });
        Ui.button(plan,applying?"Saving…":"Apply plan",this::confirmApply).setEnabled(!stale&&!applying&&!preview.plans.isEmpty());
        Ui.text(plan,"Applying replaces unstarted sessions from today. Tasks, deadlines, and class schedules are not changed; sessions with progress are kept.",13,false);

    }
    private void analyze(){
        if(analyzing||applying)return;analyzing=true;refresh();
        android.os.Handler main=new android.os.Handler(android.os.Looper.getMainLooper());
        com.example.studyflow.data.database.StudyFlowDatabase.IO.execute(()->{
            try{
                List<Task> ts=db.taskDao().getSnapshot();List<ClassSchedule> cs=db.classScheduleDao().getSnapshot();List<FocusSession> fs=db.focusSessionDao().getSnapshot();List<StudyPlan> ps=db.studyPlanDao().getSnapshot();
                SmartScheduler.Analysis result=SmartScheduler.analyze(ts,cs,fs,ps,System.currentTimeMillis());String stamp=SmartScheduler.fingerprint(ts,cs,fs,ps);
                main.post(()->{analyzing=false;if(!isAdded()||getView()==null)return;preview=result;fingerprint=stamp;refresh();});
            }catch(Exception e){main.post(()->{analyzing=false;if(isAdded()){new AlertDialog.Builder(requireContext()).setMessage("Could not analyze the data. Please try again.").setPositiveButton("Close",null).show();refresh();}});}
        });
    }
    private void confirmApply(){
        if(preview==null||applying)return;
        new AlertDialog.Builder(requireContext()).setTitle("Apply this preview plan?")
            .setMessage("Replace unstarted sessions from today with "+preview.plans.size()+" preview sessions. Sessions with progress will be kept.")
            .setNegativeButton("Cancel",null).setPositiveButton("Apply plan",(d,w)->apply()).show();
    }
    private void apply(){
        applying=true;refresh();List<StudyPlan> accepted=new ArrayList<>(preview.plans);String stamp=fingerprint;
        android.os.Handler main=new android.os.Handler(android.os.Looper.getMainLooper());
        com.example.studyflow.data.database.StudyFlowDatabase.IO.execute(()->{
            try{
                db.runInTransaction(()->{
                    String latest=SmartScheduler.fingerprint(db.taskDao().getSnapshot(),db.classScheduleDao().getSnapshot(),db.focusSessionDao().getSnapshot(),db.studyPlanDao().getSnapshot());
                    if(!stamp.equals(latest))throw new IllegalStateException("Your data changed. Analyze again.");
                    long now=System.currentTimeMillis();for(StudyPlan p:accepted)if(p.getStudyDate()<now)throw new IllegalStateException("A preview start time has passed. Analyze again.");
                    db.studyPlanDao().deleteUnstartedFuture(SmartScheduler.startOfDay(now));
                    for(StudyPlan p:accepted)db.studyPlanDao().insert(p);
                });
                main.post(()->{applying=false;preview=null;if(isAdded()&&getView()!=null){android.widget.Toast.makeText(requireContext(),"Plan applied",android.widget.Toast.LENGTH_SHORT).show();refresh();}});
            }catch(Exception e){main.post(()->{applying=false;if(isAdded()&&getView()!=null){new AlertDialog.Builder(requireContext()).setMessage(e.getMessage()).setPositiveButton("Close",null).show();refresh();}});}
        });
    }
}
