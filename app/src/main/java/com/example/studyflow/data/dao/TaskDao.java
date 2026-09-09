package com.example.studyflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE courseId = :courseId ORDER BY deadline ASC")
    LiveData<List<Task>> getTasksByCourse(int courseId);

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY deadline ASC")
    LiveData<List<Task>> getActiveTasks();

    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    Task getTaskById(int taskId);
    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    Task getById(int taskId);
    @Query("SELECT * FROM tasks ORDER BY deadline")
    List<Task> getSnapshot();
}
