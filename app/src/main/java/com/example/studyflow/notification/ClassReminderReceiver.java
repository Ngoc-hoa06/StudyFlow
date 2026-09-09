package com.example.studyflow.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.studyflow.R;

public class ClassReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "class_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        String courseName =
                intent.getStringExtra("courseName");

        String room =
                intent.getStringExtra("room");

        String startTime =
                intent.getStringExtra("startTime");

        int reminderMinutes =
                intent.getIntExtra("reminderMinutes", 30);

        NotificationManager notificationManager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Class Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Notifications for upcoming classes"
            );

            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(
                                courseName + " starts soon"
                        )
                        .setContentText(
                                "Starts at "
                                        + startTime
                                        + " • Room "
                                        + room
                                        + " • "
                                        + reminderMinutes
                                        + " minutes remaining"
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true);

        notificationManager.notify(
                (int) System.currentTimeMillis(),
                builder.build()
        );
    }
}