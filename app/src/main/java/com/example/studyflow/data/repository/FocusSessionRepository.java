package com.example.studyflow.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.studyflow.data.dao.FocusSessionDao;
import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.FocusSession;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusSessionRepository {

    private final FocusSessionDao focusSessionDao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public FocusSessionRepository(Application application) {

        StudyFlowDatabase database =
                StudyFlowDatabase.getDatabase(application);

        focusSessionDao =
                database.focusSessionDao();
    }

    public void insert(FocusSession focusSession) {

        executor.execute(() ->
                focusSessionDao.insert(focusSession)
        );
    }

    public LiveData<List<FocusSession>> getAllSessions() {

        return focusSessionDao.getAllSessions();
    }

    public LiveData<Integer> getTotalFocusMinutes() {

        return focusSessionDao.getTotalFocusMinutes();
    }

    public LiveData<Integer> getTotalFocusMinutesForCourse(
            int courseId) {

        return focusSessionDao
                .getTotalFocusMinutesForCourse(courseId);
    }

    public LiveData<List<FocusSession>> getSessionsByTask(
            int taskId) {

        return focusSessionDao
                .getSessionsByTask(taskId);
    }
}