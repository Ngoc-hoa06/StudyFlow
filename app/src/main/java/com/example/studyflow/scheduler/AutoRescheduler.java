package com.example.studyflow.scheduler;

import com.example.studyflow.data.entity.StudyPlan;
import com.example.studyflow.data.entity.Task;

import java.util.Calendar;
import java.util.List;

public class AutoRescheduler {

    public static boolean needsReschedule(
            List<StudyPlan> plans) {

        if (plans == null || plans.isEmpty()) {
            return false;
        }

        long todayStart =
                getStartOfToday();

        for (StudyPlan plan : plans) {

            boolean unfinished =
                    "PLANNED".equals(plan.getStatus())
                            || "IN_PROGRESS".equals(plan.getStatus());

            boolean isPastDay =
                    plan.getStudyDate() < todayStart;

            if (unfinished && isPastDay) {
                return true;
            }
        }

        return false;
    }

    public static List<StudyPlan> reschedule(
            List<Task> tasks) {

        return SmartScheduler.generatePlan(tasks);
    }

    private static long getStartOfToday() {

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        return calendar.getTimeInMillis();
    }
}