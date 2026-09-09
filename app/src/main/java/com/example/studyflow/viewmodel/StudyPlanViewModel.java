package com.example.studyflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.studyflow.data.entity.StudyPlan;
import com.example.studyflow.data.repository.StudyPlanRepository;

import java.util.List;

public class StudyPlanViewModel
        extends AndroidViewModel {

    private final StudyPlanRepository repository;

    public StudyPlanViewModel(
            @NonNull Application application) {

        super(application);

        repository =
                new StudyPlanRepository(
                        application
                );
    }

    public LiveData<List<StudyPlan>>
    getAllStudyPlans() {

        return repository.getAllStudyPlans();
    }

    public void insert(
            StudyPlan studyPlan) {

        repository.insert(studyPlan);
    }

    public void insertAll(
            List<StudyPlan> plans) {

        repository.insertAll(plans);
    }
    public void replaceAll(List<StudyPlan> plans) {
        repository.replaceAll(plans);
    }
    public void update(StudyPlan studyPlan) {
        repository.update(studyPlan);
    }
    public void updateProgressForTaskToday(
            int taskId,
            int addedMinutes) {

        repository.updateProgressForTaskToday(
                taskId,
                addedMinutes
        );
    }
}