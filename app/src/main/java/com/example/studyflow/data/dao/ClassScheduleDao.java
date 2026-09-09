package com.example.studyflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.entity.ClassSchedule;

import java.util.List;

@Dao
public interface ClassScheduleDao {

    @Insert
    long insert(ClassSchedule schedule);

    @Update
    void update(ClassSchedule schedule);

    @Delete
    void delete(ClassSchedule schedule);

    @Query("SELECT * FROM class_schedules ORDER BY dayOfWeek, startTime")
    LiveData<List<ClassSchedule>> getAllSchedules();

    @Query("SELECT * FROM class_schedules WHERE courseId = :courseId ORDER BY dayOfWeek, startTime")
    LiveData<List<ClassSchedule>> getSchedulesByCourse(int courseId);

    @Query("SELECT * FROM class_schedules WHERE dayOfWeek = :dayOfWeek ORDER BY startTime")
    LiveData<List<ClassSchedule>> getSchedulesByDay(int dayOfWeek);
    @Query("SELECT * FROM class_schedules ORDER BY dayOfWeek, startTime")
    List<ClassSchedule> getSnapshot();
    @Query("SELECT * FROM class_schedules WHERE scheduleId = :id")
    ClassSchedule getById(int id);
}