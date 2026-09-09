package com.example.studyflow.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.studyflow.data.dao.StudyPlanDao;
import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.StudyPlan;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudyPlanRepository {

    private final StudyPlanDao studyPlanDao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public StudyPlanRepository(
            Application application) {

        StudyFlowDatabase database =
                StudyFlowDatabase.getDatabase(
                        application
                );

        studyPlanDao =
                database.studyPlanDao();
    }

    public LiveData<List<StudyPlan>>
    getAllStudyPlans() {

        return studyPlanDao.getAllStudyPlans();
    }

    public void insert(
            StudyPlan studyPlan) {

        executor.execute(() ->
                studyPlanDao.insert(studyPlan)
        );
    }

    public void insertAll(
            List<StudyPlan> plans) {

        executor.execute(() -> {

            for (StudyPlan plan : plans) {
                studyPlanDao.insert(plan);
            }
        });
    }
    public void replaceAll(List<StudyPlan> plans) {

        executor.execute(() -> {

            studyPlanDao.deleteAllPlans();

            for (StudyPlan plan : plans) {
                studyPlanDao.insert(plan);
            }
        });
    }
    public void update(StudyPlan studyPlan) {

        executor.execute(() ->
                studyPlanDao.update(studyPlan)
        );
    }
    public void updateProgressForTaskToday(
            int taskId,
            int addedMinutes) {

        executor.execute(() -> {

            java.util.Calendar start =
                    java.util.Calendar.getInstance();

            start.set(java.util.Calendar.HOUR_OF_DAY, 0);
            start.set(java.util.Calendar.MINUTE, 0);
            start.set(java.util.Calendar.SECOND, 0);
            start.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar end =
                    (java.util.Calendar) start.clone();

            end.add(
                    java.util.Calendar.DAY_OF_YEAR,
                    1
            );

            StudyPlan plan =
                    studyPlanDao.getPlanForTaskOnDate(
                            taskId,
                            start.getTimeInMillis(),
                            end.getTimeInMillis()
                    );

            if (plan == null) {
                return;
            }

            int newCompleted =
                    plan.getCompletedMinutes()
                            + addedMinutes;

            if (newCompleted >=
                    plan.getPlannedMinutes()) {

                newCompleted =
                        plan.getPlannedMinutes();

                plan.setStatus("COMPLETED");

            } else {

                plan.setStatus("IN_PROGRESS");
            }

            plan.setCompletedMinutes(
                    newCompleted
            );

            studyPlanDao.update(plan);
        });
    }
}