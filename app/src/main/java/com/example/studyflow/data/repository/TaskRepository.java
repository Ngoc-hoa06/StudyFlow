package com.example.studyflow.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.studyflow.data.dao.TaskDao;
import com.example.studyflow.data.database.StudyFlowDatabase;
import com.example.studyflow.data.entity.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final LiveData<List<Task>> allTasks;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public TaskRepository(Application application) {

        StudyFlowDatabase database =
                StudyFlowDatabase.getDatabase(application);

        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getActiveTasks() {
        return taskDao.getActiveTasks();
    }

    public void insert(Task task) {
        executorService.execute(() ->
                taskDao.insert(task)
        );
    }

    public void update(Task task) {
        executorService.execute(() ->
                taskDao.update(task)
        );
    }

    public void delete(Task task) {
        executorService.execute(() ->
                taskDao.delete(task)
        );
    }
}