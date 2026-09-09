package com.example.studyflow.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.studyflow.data.entity.ClassSchedule;
import com.example.studyflow.data.entity.Course;

import java.util.Calendar;

public class ClassReminderScheduler {

    public static void scheduleReminder(
            Context context,
            Course course,
            ClassSchedule schedule) {

        String[] timeParts =
                schedule.getStartTime().split(":");

        int hour =
                Integer.parseInt(timeParts[0]);

        int minute =
                Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        boolean dateSpecific = schedule.getDateMillis() > 0L;

        if (dateSpecific) {
            calendar.setTimeInMillis(schedule.getDateMillis());
        } else {
            int today = calendar.get(Calendar.DAY_OF_WEEK);
            int targetDay = convertToCalendarDay(schedule.getDayOfWeek());
            int daysUntil = (targetDay - today + 7) % 7;
            calendar.add(Calendar.DAY_OF_YEAR, daysUntil);
        }

        calendar.set(
                Calendar.HOUR_OF_DAY,
                hour
        );

        calendar.set(
                Calendar.MINUTE,
                minute
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        calendar.add(
                Calendar.MINUTE,
                -schedule.getReminderMinutes()
        );

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            if (dateSpecific) return;
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        Intent intent =
                new Intent(
                        context,
                        ClassReminderReceiver.class
                );

        intent.setAction("studyflow.class." + schedule.getScheduleId());
        intent.putExtra("scheduleId", schedule.getScheduleId());
        intent.putExtra(
                "courseName",
                course.getCourseName()
        );

        intent.putExtra(
                "room",
                com.example.studyflow.ui.common.Ui.room(course, schedule)
        );

        intent.putExtra(
                "startTime",
                schedule.getStartTime()
        );

        intent.putExtra(
                "reminderMinutes",
                schedule.getReminderMinutes()
        );

        int requestCode =
                schedule.getScheduleId();

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    private static int convertToCalendarDay(
            int dayOfWeek) {

        switch (dayOfWeek) {

            case 1:
                return Calendar.MONDAY;

            case 2:
                return Calendar.TUESDAY;

            case 3:
                return Calendar.WEDNESDAY;

            case 4:
                return Calendar.THURSDAY;

            case 5:
                return Calendar.FRIDAY;

            case 6:
                return Calendar.SATURDAY;

            case 7:
                return Calendar.SUNDAY;

            default:
                return Calendar.MONDAY;
        }
    }
    public static void cancel(Context context, ClassSchedule schedule) {
        Intent intent = new Intent(context, ClassReminderReceiver.class).setAction("studyflow.class." + schedule.getScheduleId());
        PendingIntent pending = PendingIntent.getBroadcast(context, schedule.getScheduleId(), intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pending != null) { ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(pending); pending.cancel(); }
        // Remove the legacy course/day alarm if this schedule was created before the update.
        PendingIntent legacy = PendingIntent.getBroadcast(context, schedule.getCourseId()*100+schedule.getDayOfWeek(), new Intent(context,ClassReminderReceiver.class), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (legacy != null) { ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(legacy); legacy.cancel(); }
    }
}
