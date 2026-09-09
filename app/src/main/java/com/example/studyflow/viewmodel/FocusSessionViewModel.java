package com.example.studyflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.studyflow.data.entity.FocusSession;
import com.example.studyflow.data.repository.FocusSessionRepository;

import java.util.List;

public class FocusSessionViewModel
        extends AndroidViewModel {

    private final FocusSessionRepository repository;

    public FocusSessionViewModel(
            @NonNull Application application) {

        super(application);

        repository =
                new FocusSessionRepository(application);
    }

    public void insert(FocusSession focusSession) {

        repository.insert(focusSession);
    }

    public LiveData<List<FocusSession>>
    getAllSessions() {

        return repository.getAllSessions();
    }

    public LiveData<Integer>
    getTotalFocusMinutes() {

        return repository.getTotalFocusMinutes();
    }

    public LiveData<Integer>
    getTotalFocusMinutesForCourse(int courseId) {

        return repository
                .getTotalFocusMinutesForCourse(courseId);
    }

    public LiveData<List<FocusSession>>
    getSessionsByTask(int taskId) {

        return repository
                .getSessionsByTask(taskId);
    }
}