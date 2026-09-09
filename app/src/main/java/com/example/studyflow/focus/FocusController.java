package com.example.studyflow.focus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.*;
import com.example.studyflow.data.sync.StudyDataSync;
import java.util.Calendar;
import java.util.UUID;

/** One durable timer shared by Home, Focus and the foreground service. */
public final class FocusController {
    public static final String IDLE="IDLE", RUNNING="RUNNING", PAUSED="PAUSED", SAVING="SAVING";
    public static final class State {
        public final String status, taskTitle, error;
        public final Integer taskId;
        public final long totalMillis, remainingMillis;
        State(String s,String title,Integer id,long total,long remaining,String e) {
            status=s;taskTitle=title;taskId=id;totalMillis=total;remainingMillis=remaining;error=e;
        }
        public boolean active(){return !IDLE.equals(status);}
        public String time(){long seconds=(remainingMillis+999)/1000;return String.format(java.util.Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);}
    }
    private static FocusController instance;
    public static synchronized FocusController get(Context context) {
        if(instance==null)instance=new FocusController(context.getApplicationContext());return instance;
    }
    private final Context context;
    private final SharedPreferences prefs;
    private final MutableLiveData<State> state=new MutableLiveData<>();
    private final Handler main=new Handler(Looper.getMainLooper());
    private String status,key,title,error="";
    private Integer taskId;
    private long total,left,resumedElapsed,startedAt;
    private boolean writing;
    private FocusController(Context c) {
        context=c;prefs=c.getSharedPreferences("focus_timer_v2",Context.MODE_PRIVATE);
        status=prefs.getString("status",IDLE);key=prefs.getString("key","");title=prefs.getString("title","Free focus");
        if ("Focus \u0074\u1ef1 do".equals(title)) {
            title = "Free focus";
            prefs.edit().putString("title", title).apply();
        }
        int id=prefs.getInt("task",-1);taskId=id<0?null:id;
        total=prefs.getLong("total",0);left=prefs.getLong("left",0);startedAt=prefs.getLong("started",0);
        resumedElapsed=prefs.getLong("elapsed",SystemClock.elapsedRealtime());
        int boot=android.provider.Settings.Global.getInt(c.getContentResolver(),"boot_count",0);
        if(RUNNING.equals(status) && boot!=prefs.getInt("boot",boot)) {
            // After a device restart use saved wall-clock deadline as a recovery fallback.
            left=Math.max(0,prefs.getLong("wallDeadline",0)-System.currentTimeMillis());resumedElapsed=SystemClock.elapsedRealtime();
        }
        publish();
    }
    public LiveData<State> observe(){return state;}
    public State current(){return snapshot();}
    private long remaining(){return FocusClock.remaining(left,resumedElapsed,SystemClock.elapsedRealtime(),RUNNING.equals(status));}
    private State snapshot(){return new State(status,title,taskId,total,remaining(),error);}
    private void publish(){state.setValue(snapshot());}
    private void persist(){
        prefs.edit().putString("status",status).putString("key",key).putString("title",title)
                .putInt("task",taskId==null?-1:taskId).putLong("total",total).putLong("left",left)
                .putLong("elapsed",resumedElapsed).putLong("started",startedAt)
                .putLong("wallDeadline",System.currentTimeMillis()+remaining())
                .putInt("boot",android.provider.Settings.Global.getInt(context.getContentResolver(),"boot_count",0)).commit();
        publish();
    }
    public void start(Integer id,String name,int minutes){
        if(!IDLE.equals(status))return;
        if(minutes<1||minutes>720)throw new IllegalArgumentException("Duration must be between 1 and 720 minutes.");
        key=UUID.randomUUID().toString();taskId=id;title=id==null?"Free focus":name;
        total=minutes*60000L;left=total;startedAt=System.currentTimeMillis();resumedElapsed=SystemClock.elapsedRealtime();status=RUNNING;error="";persist();
    }
    public void pause(){if(RUNNING.equals(status)){left=remaining();status=PAUSED;persist();}}
    public void resume(){if(PAUSED.equals(status)){resumedElapsed=SystemClock.elapsedRealtime();status=RUNNING;error="";persist();}}
    public void reset(){if(SAVING.equals(status))return;status=IDLE;left=0;total=0;taskId=null;error="";persist();}
    public void tick(){
        if(RUNNING.equals(status)&&remaining()==0)finish();
        else if(SAVING.equals(status)&&!writing)finish();
        else publish();
    }
    public void finish(){
        if(IDLE.equals(status)||writing)return;
        left=remaining();status=SAVING;writing=true;error="";persist();
        final int minutes=FocusClock.creditedMinutes(total,left);
        final String sessionKey=key;final Integer selected=taskId;final long start=startedAt;
        StudyFlowDatabase.IO.execute(()->{
            try {
                StudyFlowDatabase db=StudyFlowDatabase.getDatabase(context);
                db.runInTransaction(()->{
                    Task task=selected==null?null:db.taskDao().getTaskById(selected);
                    FocusSession session=new FocusSession(task==null?null:task.getCourseId(),task==null?null:task.getTaskId(),start,minutes);
                    session.setSessionKey(sessionKey);
                    // A repeated finish/recovery cannot credit the same session twice.
                    if(db.focusSessionDao().insert(session)==-1)return;
                    if(task!=null && minutes>0){
                        task.setCompletedMinutes(task.getCompletedMinutes()+minutes);
                        if(task.getEstimatedMinutes()>0 && task.getCompletedMinutes()>=task.getEstimatedMinutes())task.setStatus("COMPLETED");
                        else if(!"COMPLETED".equals(task.getStatus()))task.setStatus("IN_PROGRESS");
                        db.taskDao().update(task);
                        Calendar day=Calendar.getInstance();day.setTimeInMillis(start);day.set(Calendar.HOUR_OF_DAY,0);day.set(Calendar.MINUTE,0);day.set(Calendar.SECOND,0);day.set(Calendar.MILLISECOND,0);
                        long begin=day.getTimeInMillis();day.add(Calendar.DAY_OF_YEAR,1);int available=minutes;
                        for(StudyPlan p:db.studyPlanDao().getDailyPlans(task.getTaskId(),begin,day.getTimeInMillis())){
                            int credit=Math.min(available,Math.max(0,p.getPlannedMinutes()-p.getCompletedMinutes()));
                            if(credit>0){p.setCompletedMinutes(p.getCompletedMinutes()+credit);p.setStatus(p.getCompletedMinutes()>=p.getPlannedMinutes()?"COMPLETED":"IN_PROGRESS");db.studyPlanDao().update(p);available-=credit;}
                            if(available==0)break;
                        }
                    }
                });
                StudyDataSync.upload(context);
                main.post(()->{writing=false;status=IDLE;left=0;total=0;taskId=null;persist();});
            }catch(Exception e){main.post(()->{writing=false;status=PAUSED;error="The session could not be saved. Press Finish session to try again.";persist();});}
        });
    }
}
