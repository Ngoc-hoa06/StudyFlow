package com.example.studyflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.studyflow.data.entity.Course;
import com.example.studyflow.data.repository.CourseRepository;

import java.util.List;

public class CourseViewModel extends AndroidViewModel {

    private final CourseRepository repository;
    private final LiveData<List<Course>> allCourses;

    public CourseViewModel(@NonNull Application application) {
        super(application);

        repository = new CourseRepository(application);
        allCourses = repository.getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return allCourses;
    }

    public void insert(Course course) {
        repository.insert(course);
    }

    public void update(Course course) {
        repository.update(course);
    }

    public void delete(Course course) {
        repository.delete(course);
    }
}