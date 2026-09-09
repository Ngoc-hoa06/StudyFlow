package com.example.studyflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.studyflow.data.entity.ClassSchedule;
import com.example.studyflow.data.repository.ClassScheduleRepository;

import java.util.List;

public class ClassScheduleViewModel extends AndroidViewModel {

    private final ClassScheduleRepository repository;

    public ClassScheduleViewModel(@NonNull Application application) {
        super(application);

        repository = new ClassScheduleRepository(application);
    }

    public LiveData<List<ClassSchedule>> getSchedulesByCourse(int courseId) {
        return repository.getSchedulesByCourse(courseId);
    }

    public LiveData<List<ClassSchedule>> getAllSchedules() {
        return repository.getAllSchedules();
    }

    public void insert(ClassSchedule schedule) {
        repository.insert(schedule);
    }

    public void update(ClassSchedule schedule) {
        repository.update(schedule);
    }

    public void delete(ClassSchedule schedule) {
        repository.delete(schedule);
    }
}