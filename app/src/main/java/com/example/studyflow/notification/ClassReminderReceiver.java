package com.example.studyflow.notification;

import android.app.*;
import android.content.*;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.studyflow.MainActivity;
import com.example.studyflow.R;
import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.*;
import com.example.studyflow.ui.common.Ui;

public class ClassReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        int id=intent.getIntExtra("scheduleId",-1);if(id<0)return;
        PendingResult result=goAsync();
        StudyFlowDatabase.IO.execute(()->{
            try{
                StudyFlowDatabase db=StudyFlowDatabase.getDatabase(context);
                ClassSchedule s=db.classScheduleDao().getById(id);if(s==null)return;
                Course course=db.courseDao().getCourseById(s.getCourseId());if(course==null)return;
                NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                String channel="class_reminder_channel";
                if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(new NotificationChannel(channel,"Class schedule",NotificationManager.IMPORTANCE_HIGH));
                PendingIntent open=PendingIntent.getActivity(context,id,new Intent(context,MainActivity.class).putExtra("openSchedule",true).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                if(Build.VERSION.SDK_INT<33||androidx.core.content.ContextCompat.checkSelfPermission(context,android.Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED)
                    manager.notify(id,new NotificationCompat.Builder(context,channel).setSmallIcon(R.drawable.ic_sf_schedule).setContentTitle(course.getCourseName()+" starts soon")
                            .setContentText(s.getStartTime()+" · Room "+Ui.room(course,s)).setContentIntent(open).setAutoCancel(true).build());
                // Date-specific sessions are one-time alarms. Only legacy weekday
                // schedules are rescheduled for the following week.
                if (s.getDateMillis() <= 0L) ClassReminderScheduler.scheduleReminder(context,course,s);
            }finally{result.finish();}
        });
    }
}
