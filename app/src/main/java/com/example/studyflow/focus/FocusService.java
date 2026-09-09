package com.example.studyflow.focus;

import android.app.*;
import android.content.*;
import android.os.*;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.studyflow.MainActivity;
import com.example.studyflow.R;

/** User-started special-use foreground timer; never relies on a Fragment timer. */
public class FocusService extends Service {
    public static final String START="START",PAUSE="PAUSE",RESUME="RESUME",RESET="RESET",FINISH="FINISH",RESTORE="RESTORE";
    private static final String CHANNEL="focus_timer";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private FocusController controller;
    private PowerManager.WakeLock wakeLock;
    private int lastMinute=-1;private String lastStatus="";
    public static void send(Context c,String action,Integer taskId,String title,int minutes){
        Intent i=new Intent(c,FocusService.class).setAction(action).putExtra("task",taskId==null?-1:taskId).putExtra("title",title).putExtra("minutes",minutes);
        ContextCompat.startForegroundService(c,i);
    }
    @Override public void onCreate(){
        super.onCreate();controller=FocusController.get(this);
        if(Build.VERSION.SDK_INT>=26)getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(CHANNEL,"Phiên Focus",NotificationManager.IMPORTANCE_LOW));
        wakeLock=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"StudyFlow:FocusTimer");
        startForeground(2101,notification());
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        String action=intent==null?RESTORE:intent.getAction();
        if(START.equals(action)){int id=intent.getIntExtra("task",-1);controller.start(id<0?null:id,intent.getStringExtra("title"),intent.getIntExtra("minutes",25));}
        else if(PAUSE.equals(action))controller.pause();else if(RESUME.equals(action))controller.resume();
        else if(RESET.equals(action))controller.reset();else if(FINISH.equals(action))controller.finish();
        handler.removeCallbacks(ticker);handler.post(ticker);return START_STICKY;
    }
    private final Runnable ticker=new Runnable(){@Override public void run(){
        controller.tick();FocusController.State s=controller.current();
        if(!s.active()){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return;}
        if(FocusController.RUNNING.equals(s.status)){if(!wakeLock.isHeld())wakeLock.acquire(12*60*60*1000L+60000L);}
        else if(wakeLock.isHeld())wakeLock.release();
        int minute=(int)(s.remainingMillis/60000L);
        if(minute!=lastMinute||!s.status.equals(lastStatus)){
            getSystemService(NotificationManager.class).notify(2101,notification());lastMinute=minute;lastStatus=s.status;
        }
        handler.postDelayed(this,1000);
    }};
    private Notification notification(){
        FocusController.State s=controller.current();
        PendingIntent open=PendingIntent.getActivity(this,2101,new Intent(this,MainActivity.class).putExtra("openFocus",true).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_sf_focus)
            .setContentTitle(s.taskTitle).setContentText(FocusController.PAUSED.equals(s.status)?"Tạm dừng · "+s.time():"Còn "+s.time())
            .setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true);
        if(FocusController.RUNNING.equals(s.status))b.setWhen(System.currentTimeMillis()+s.remainingMillis).setUsesChronometer(true).setChronometerCountDown(true);
        if(!FocusController.SAVING.equals(s.status)){
            String a=FocusController.RUNNING.equals(s.status)?PAUSE:RESUME;
            PendingIntent toggle=PendingIntent.getService(this,2102,new Intent(this,FocusService.class).setAction(a),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            b.addAction(0,PAUSE.equals(a)?"Pause":"Resume",toggle);
        }
        return b.build();
    }
    @Override public void onDestroy(){handler.removeCallbacks(ticker);if(wakeLock!=null&&wakeLock.isHeld())wakeLock.release();super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
