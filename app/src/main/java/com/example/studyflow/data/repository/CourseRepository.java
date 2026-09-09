package com.example.studyflow.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.studyflow.data.dao.CourseDao;
import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.Course;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseRepository {

    private final CourseDao courseDao;
    private final LiveData<List<Course>> allCourses;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public CourseRepository(Application application) {

        StudyFlowDatabase database =
                StudyFlowDatabase.getDatabase(application);

        courseDao = database.courseDao();
        allCourses = courseDao.getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return allCourses;
    }

    public void insert(Course course) {
        executorService.execute(() ->
                courseDao.insert(course)
        );
    }

    public void update(Course course) {
        executorService.execute(() ->
                courseDao.update(course)
        );
    }

    public void delete(Course course) {
        executorService.execute(() ->
                courseDao.delete(course)
        );
    }
}