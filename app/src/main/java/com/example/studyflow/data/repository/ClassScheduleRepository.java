package com.example.studyflow.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.studyflow.data.dao.ClassScheduleDao;
import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.ClassSchedule;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassScheduleRepository {

    private final ClassScheduleDao classScheduleDao;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public ClassScheduleRepository(Application application) {

        StudyFlowDatabase database =
                StudyFlowDatabase.getDatabase(application);

        classScheduleDao = database.classScheduleDao();
    }

    public LiveData<List<ClassSchedule>> getSchedulesByCourse(int courseId) {
        return classScheduleDao.getSchedulesByCourse(courseId);
    }

    public LiveData<List<ClassSchedule>> getAllSchedules() {
        return classScheduleDao.getAllSchedules();
    }

    public void insert(ClassSchedule schedule) {
        executorService.execute(() ->
                classScheduleDao.insert(schedule)
        );
    }

    public void update(ClassSchedule schedule) {
        executorService.execute(() ->
                classScheduleDao.update(schedule)
        );
    }

    public void delete(ClassSchedule schedule) {
        executorService.execute(() ->
                classScheduleDao.delete(schedule)
        );
    }
}